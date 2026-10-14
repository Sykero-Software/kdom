@file:OptIn(ExperimentalXmlUtilApi::class)

package fi.sykero.kdom.input

import fi.sykero.kdom.JDOMConstants.Companion.NS_URI_XML
import fi.sykero.kdom.JDOMConstants.Companion.NS_URI_XMLNS
import fi.sykero.kdom.sax.ContentHandler
import fi.sykero.kdom.sax.Locator
import fi.sykero.kdom.sax.SAXException
import fi.sykero.kdom.sax.SAXParseException
import nl.adaptivity.xmlutil.EventType
import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.XmlReader
import nl.adaptivity.xmlutil.xmlStreaming
import kotlin.Throws
import kotlin.jvm.JvmOverloads

/**
 * Streams SAX-shaped events from xmlutil's reader; rejects every input a JAXP SAX parser rejects (no DTD support).
 *
 * The document is first pre-scanned on its raw text ([SourceScanner]: illegal characters, `<` in attribute values,
 * character references, DOCTYPE, raw duplicate attributes, ...) and, when needed, masked ([XmlSourceMask]) before
 * xmlutil 1.0.2.1's generic reader (entity-expanding mode, so an undefined entity is an error) reads it. Every reader
 * event is translated into the SAX2 `ContentHandler` call a JAXP `SAXParserFactory` parser makes: adjacent character
 * data (text, entity and character references) is delivered as one [ContentHandler.characters] call; a CDATA section's
 * content is its own `characters` call; comments and CDATA boundaries go to a handler that also implements the internal
 * [LexicalEventHandler] (kdom's tree builder); `ignorableWhitespace` is never called (no DTD is read), and
 * `skippedEntity` only in permissive DOCTYPE mode ([allowDocType]: an undeclared entity under an unread external subset,
 * or an external entity).
 * Errors found by the pre-scan are reported before any event (a SAX parser would report the events before the error
 * first). See docs/xmlutil-probes.md (P1..P8) and docs/translation-notes.md ("XmlEventParser").
 *
 * An instance holds no parse state: one instance may be used by several threads at once.
 */
public class XmlEventParser @JvmOverloads constructor(
    /** true: SAX2 namespace processing (uri/localName set, xmlns not reported as attributes, prefix mappings reported);
     *  false: like a default `SAXParserFactory` (uri = "", localName = "", qName = raw name, xmlns* reported as attributes). */
    public val namespaceAware: Boolean = true,
    /**
     * false (default, strict, requirement P7): every DOCTYPE is a parse error. true (permissive, for EPUB and other
     * non-Sword XML, ruling R11): a DOCTYPE is accepted and reported; its external subset is never read; entities of the
     * internal subset are expanded; an undeclared entity in a document whose DOCTYPE has an (unread) external id is reported
     * as `skippedEntity` (dropped in an attribute value), as by the JDK parser with `load-external-dtd` off.
     */
    public val allowDocType: Boolean = false,
) {
    /** Calls the handler; on malformed input throws [fi.sykero.kdom.sax.SAXParseException] with line/column when known. */
    @Throws(SAXException::class)
    public fun parse(xml: String, handler: ContentHandler) {
        val scanner = SourceScanner(xml, namespaceAware, allowDocType)
        scanner.scan()
        val mask = XmlSourceMask.create(xml, scanner)
        EventRun(mask?.masked ?: xml, mask, handler, namespaceAware, scanner.docType).run()
    }

    /** Bytes with BOM / XML-declaration encoding detection (UTF-8, UTF-16 BE/LE, ISO-8859-1, US-ASCII, windows-1252). */
    @Throws(SAXException::class)
    public fun parse(xml: ByteArray, handler: ContentHandler) {
        parse(XmlByteDecoder.decode(xml), handler)
    }
}

/** One parse: reads [source] with xmlutil and calls [handler]. */
private class EventRun(
    private val source: String,
    private val mask: XmlSourceMask?,
    private val handler: ContentHandler,
    private val namespaceAware: Boolean,
    private val docType: ScannedDocType?,
) {
    private val lexical = handler as? LexicalEventHandler
    private var reader: XmlReader? = null
    private val text = StringBuilder()
    private var depth = 0
    private val elements = ArrayList<Array<String>>() // uri, localName, qName
    private val mappings = ArrayList<List<String>>() // prefixes reported by startPrefixMapping per open element

    private val locator = object : Locator {
        override val lineNumber: Int get() = location()?.line ?: -1
        override val columnNumber: Int get() = location()?.col ?: -1
        override val publicId: String? get() = null
        override val systemId: String? get() = null
    }

    private fun location(): XmlReader.ExtLocationInfo? =
        try { reader?.extLocationInfo as? XmlReader.ExtLocationInfo } catch (e: Exception) { null }

    private fun name(s: String) = mask?.name(s) ?: s
    private fun content(s: String) = mask?.content(s) ?: s

    /** Runs a reader call; any exception it throws becomes a [SAXParseException] at the reader's position (P3). */
    private inline fun <T> guarded(block: () -> T): T = try {
        block()
    } catch (e: SAXParseException) {
        throw e
    } catch (e: Exception) {
        val loc = location()
        throw SAXParseException(readerMessage(e), loc?.line ?: -1, loc?.col ?: -1, e)
    }

    private fun failHere(message: String): Nothing {
        val loc = location()
        throw SAXParseException(message, loc?.line ?: -1, loc?.col ?: -1)
    }

    fun run() {
        val r = guarded { xmlStreaming.newGenericReader(source, true) }
        reader = r
        handler.setDocumentLocator(locator)
        handler.startDocument()
        while (true) {
            val type = guarded { if (r.hasNext()) r.next() else null } ?: break
            when (type) {
                EventType.START_DOCUMENT -> {
                    val version = guarded { r.version }
                    if (version != null && version != "1.0" && version != "1.1") {
                        failHere("XML version \"$version\" is not supported, only XML 1.0 is supported.")
                    }
                }
                EventType.DOCDECL -> failHere("DOCTYPE is not allowed (kdom does not process DTDs).") // P7
                EventType.START_ELEMENT -> startElement(r)
                EventType.END_ELEMENT -> endElement()
                EventType.TEXT, EventType.IGNORABLE_WHITESPACE, EventType.ENTITY_REF -> {
                    val t = guarded { r.text }
                    // Outside the root element only white space reaches here (the reader rejects other text); SAX
                    // does not report it.
                    if (depth > 0) {
                        if (mask == null) text.append(t)
                        else mask.textSegments(t, { text.append(it) }, { name -> flush(); handler.skippedEntity(name) })
                    }
                }
                EventType.CDSECT -> {
                    val t = content(guarded { r.text })
                    flush()
                    lexical?.startCDATA()
                    if (t.isNotEmpty()) handler.characters(t.toCharArray(), 0, t.length)
                    lexical?.endCDATA()
                }
                EventType.COMMENT -> {
                    val t = content(guarded { r.text })
                    flush()
                    lexical?.comment(t.toCharArray(), 0, t.length)
                }
                EventType.PROCESSING_INSTRUCTION -> {
                    val t = content(guarded { r.text })
                    flush()
                    if (docType != null && t == mask?.docTypeTarget) {
                        reportDocType(docType)
                        continue
                    }
                    var k = 0
                    while (k < t.length && !isXmlSpace(t[k])) k++
                    val target = t.substring(0, k)
                    while (k < t.length && isXmlSpace(t[k])) k++
                    handler.processingInstruction(target, t.substring(k))
                }
                else -> {} // END_DOCUMENT, ATTRIBUTE: nothing to report
            }
        }
        handler.endDocument()
    }

    /** The DOCTYPE's SAX events: startDTD, the internal subset's declarations/comments/PIs in order, endDTD. */
    private fun reportDocType(dt: ScannedDocType) {
        lexical?.startDTD(dt.name, dt.publicId, dt.systemId)
        for (item in dt.items) {
            when (item) {
                is DtdItem.ElementDecl -> lexical?.elementDecl(item.name, item.model)
                is DtdItem.Entity -> when {
                    item.value != null -> lexical?.internalEntityDecl(item.name, item.value)
                    item.notation != null -> lexical?.unparsedEntityDecl(item.name, item.publicId, item.systemId, item.notation)
                    else -> lexical?.externalEntityDecl(item.name, item.publicId, item.systemId)
                }
                is DtdItem.Notation -> lexical?.notationDecl(item.name, item.publicId, item.systemId)
                is DtdItem.Comment -> lexical?.comment(item.text.toCharArray(), 0, item.text.length)
                is DtdItem.Pi -> {} // the JDK parser does not report processing instructions of the internal subset
            }
        }
        lexical?.endDTD()
    }

    private fun flush() {
        if (text.isEmpty()) return
        val chars = text.toString().toCharArray()
        text.clear()
        handler.characters(chars, 0, chars.size)
    }

    private fun startElement(r: XmlReader) {
        flush()
        val atts = ParsedAttributes()
        val reported: List<String>
        val uri: String
        val localName: String
        val qName: String
        if (namespaceAware) {
            val decls = guarded { r.namespaceDecls.map { it.prefix to content(it.namespaceURI) } }
            val prefixes = ArrayList<String>(decls.size)
            for ((p, u) in decls) {
                if (p == "xmlns" || u == NS_URI_XMLNS) {
                    failHere("The prefix \"xmlns\" cannot be bound to any namespace explicitly; neither can the namespace for \"xmlns\" be bound to any prefix explicitly.")
                }
                if ((p == "xml") != (u == NS_URI_XML)) {
                    failHere("The prefix \"xml\" cannot be bound to any namespace other than its usual namespace; neither can the namespace for \"xml\" be bound to any prefix other than \"xml\".")
                }
                if (p.isNotEmpty() && u.isEmpty()) failHere("Prefixed namespace bindings may not be empty (prefix \"$p\").")
                if (p == "xml") continue // SAX does not report the predeclared xml prefix
                prefixes.add(p)
            }
            uri = content(guarded { r.namespaceURI })
            localName = guarded { r.localName }
            val prefix = guarded { r.prefix }
            qName = if (prefix.isEmpty()) localName else "$prefix:$localName"
            val count = guarded { r.attributeCount }
            var seen: MutableSet<Pair<String, String>>? = null
            for (i in 0 until count) {
                val aUri = content(guarded { r.getAttributeNamespace(i) })
                val aLocal = guarded { r.getAttributeLocalName(i) }
                val aPrefix = guarded { r.getAttributePrefix(i) }
                val value = content(guarded { r.getAttributeValue(i) })
                if (seen == null) seen = HashSet()
                if (!seen.add(aUri to aLocal)) {
                    failHere("Attribute \"$aLocal\" bound to namespace \"$aUri\" was already specified for element \"$qName\".") // P2
                }
                atts.add(aUri, aLocal, if (aPrefix.isEmpty()) aLocal else "$aPrefix:$aLocal", value)
            }
            for (p in prefixes) handler.startPrefixMapping(p, decls.first { it.first == p }.second)
            reported = prefixes
        } else {
            // Names were masked to plain names; the reader saw no prefixes and no namespace declarations (P4).
            val prefix = guarded { r.prefix }
            val local = guarded { r.localName }
            uri = ""
            localName = ""
            qName = name(if (prefix.isEmpty()) local else "$prefix:$local")
            val count = guarded { r.attributeCount }
            for (i in 0 until count) {
                val aPrefix = guarded { r.getAttributePrefix(i) }
                val aLocal = guarded { r.getAttributeLocalName(i) }
                val value = content(guarded { r.getAttributeValue(i) })
                val aQName = name(if (aPrefix.isEmpty()) aLocal else "$aPrefix:$aLocal")
                // A JAXP SAX parser without namespace processing reports localName = qName for attributes.
                atts.add("", aQName, aQName, value)
            }
            reported = emptyList()
        }
        handler.startElement(uri, localName, qName, atts)
        elements.add(arrayOf(uri, localName, qName))
        mappings.add(reported)
        depth++
    }

    private fun endElement() {
        flush()
        val e = elements.removeAt(elements.size - 1)
        val reported = mappings.removeAt(mappings.size - 1)
        depth--
        handler.endElement(e[0], e[1], e[2])
        for (p in reported) handler.endPrefixMapping(p)
    }

    private companion object {
        private val LOCATION_PREFIX = Regex("^\\d+:\\d+ - ")

        fun readerMessage(e: Exception): String {
            val m = e.message ?: return e::class.simpleName ?: "parse error"
            return m.replace(LOCATION_PREFIX, "")
        }
    }
}
