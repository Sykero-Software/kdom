package fi.sykero.kdom.input

import fi.sykero.kdom.Attribute
import fi.sykero.kdom.AttributeType
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.sax.Attributes
import fi.sykero.kdom.sax.ContentHandler
import fi.sykero.kdom.sax.Locator
import fi.sykero.kdom.sax.SAXException

/**
 * Builds a [Document] from namespace-aware SAX events exactly as jdom2 2.0.6.1's `SAXHandler` does with
 * `SAXBuilder`'s defaults (expand entities, keep all white space, `DefaultJDOMFactory`), minus everything DTD related
 * (kdom never sees a DTD):
 *
 * - character data is buffered and flushed as one [Text] at the next other event; CDATA content becomes a [CDATA]
 *   node (an empty CDATA section too), never merged with neighbouring text;
 * - comments (except empty ones, which jdom2 drops) and processing instructions are added to the current element, or
 *   to the document before/after the root;
 * - `startPrefixMapping` namespaces become the next element's additional namespaces unless identical to its own
 *   namespace; `xmlns` attributes are never attributes;
 * - attributes keep document order, with the reported type (`CDATA`) and `specified = true`.
 */
internal class TreeBuildingHandler : ContentHandler, LexicalEventHandler {
    val document: Document = Document(null as Element?)
    private var currentElement: Element? = null
    private var atRoot = true
    private val declaredNamespaces = ArrayList<Namespace>()
    private val textBuffer = StringBuilder()
    private var previousCDATA = false
    private var inCDATA = false

    override fun setDocumentLocator(locator: Locator?) {}
    override fun startDocument() {}
    override fun endDocument() {}

    override fun startPrefixMapping(prefix: String, uri: String) {
        declaredNamespaces.add(Namespace.getNamespace(prefix, uri))
    }

    override fun endPrefixMapping(prefix: String) {}

    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
        var prefix = ""
        var local = localName
        if (qName != "") {
            val colon = qName.indexOf(':')
            if (colon > 0) prefix = qName.substring(0, colon)
            if (local == "") local = qName.substring(colon + 1)
        }
        val element = Element(local, Namespace.getNamespace(prefix, uri))
        if (declaredNamespaces.isNotEmpty()) {
            for (ns in declaredNamespaces) if (ns !== element.namespace) element.addNamespaceDeclaration(ns)
            declaredNamespaces.clear()
        }
        flushCharacters()
        if (atRoot) {
            document.setRootElement(element)
            atRoot = false
        } else {
            currentElement!!.addContent(element)
        }
        currentElement = element
        for (i in 0 until atts.length) {
            var attPrefix = ""
            var attLocalName = atts.getLocalName(i) ?: ""
            val attQName = atts.getQName(i) ?: ""
            if (attQName != "") {
                if (attQName.startsWith("xmlns:") || attQName == "xmlns") continue
                val attColon = attQName.indexOf(':')
                if (attColon > 0) attPrefix = attQName.substring(0, attColon)
                if (attLocalName == "") attLocalName = attQName.substring(attColon + 1)
            }
            val attType = AttributeType.getAttributeType(atts.getType(i))
            val attURI = atts.getURI(i) ?: ""
            if (attLocalName == "xmlns" || attPrefix == "xmlns" || attURI == XMLNS_URI) continue
            element.setAttribute(Attribute(attLocalName, atts.getValue(i), attType, Namespace.getNamespace(attPrefix, attURI)))
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        flushCharacters()
        if (atRoot) throw SAXException("Ill-formed XML document (missing opening tag for $localName)")
        val p = currentElement!!.parent
        if (p is Document) atRoot = true else currentElement = p as Element
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        if (length == 0 && !inCDATA) return
        if (previousCDATA != inCDATA) flushCharacters()
        textBuffer.appendRange(ch, start, start + length)
    }

    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
        characters(ch, start, length)
    }

    override fun processingInstruction(target: String, data: String?) {
        flushCharacters()
        val pi = ProcessingInstruction(target, data)
        if (atRoot) document.addContent(pi) else currentElement!!.addContent(pi)
    }

    override fun skippedEntity(name: String) {
        // XmlEventParser never reports one (an undefined entity is a parse error).
        throw SAXException("Unexpected skipped entity $name")
    }

    override fun comment(ch: CharArray, start: Int, length: Int) {
        flushCharacters()
        val commentText = ch.concatToString(start, start + length)
        if (commentText != "") {
            val comment = Comment(commentText)
            if (atRoot) document.addContent(comment) else currentElement!!.addContent(comment)
        }
    }

    override fun startCDATA() {
        inCDATA = true
    }

    override fun endCDATA() {
        previousCDATA = true
        flushCharacters()
        previousCDATA = false
        inCDATA = false
    }

    private fun flushCharacters() {
        val data = textBuffer.toString()
        textBuffer.clear()
        if (data.isEmpty() && !inCDATA) {
            previousCDATA = inCDATA
            return
        }
        if (previousCDATA) {
            currentElement!!.addContent(CDATA(data))
        } else if (data.isNotEmpty()) {
            currentElement!!.addContent(Text(data))
        }
        previousCDATA = inCDATA
    }

    private companion object {
        const val XMLNS_URI = "http://www.w3.org/2000/xmlns/"
    }
}
