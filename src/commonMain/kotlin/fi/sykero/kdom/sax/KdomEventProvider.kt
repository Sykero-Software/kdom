package fi.sykero.kdom.sax

import fi.sykero.kdom.AttributeType
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Content
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.util.NamespaceStack

/**
 * Replays a kdom tree as SAX [ContentHandler] events: the sequence jdom2's `SAXOutputter(handler).output(doc)` produced
 * with its defaults and the raw format (what JSword's `JDOMSAXEventProvider` does):
 *
 * - `setDocumentLocator` (all values `-1`/`null`), `startDocument`, the document's top-level elements and processing
 *   instructions, `endDocument`; top-level comments and the DocType are not reported.
 * - per element: `startPrefixMapping` for each namespace introduced by it (element's own namespace first, then the
 *   declared additional ones, then those of its attributes; in [NamespaceStack] order), `startElement` with the attributes
 *   in tree order (no `xmlns` attributes; type `CDATA` for [AttributeType.UNDECLARED], otherwise the enum name),
 *   children, `endElement`, then `endPrefixMapping` in reverse order.
 * - `characters` is called once per [Text] / [CDATA] node with a fresh array (offset 0, full length), also for empty
 *   nodes; text is never merged, whitespace is never "ignorable"; comments are dropped; [EntityRef] gives
 *   `skippedEntity(name)`; a processing instruction gives `processingInstruction(target, data)`.
 */
public class KdomEventProvider(private val doc: Document) {
    /**
     * `frag.document ?: Document(frag)` as `SwordContentFacade` does: an element that already belongs to a document
     * reports that document (it is not detached); a detached element is wrapped in a new [Document], which makes the
     * element its root (so it must not have a parent element).
     */
    public constructor(element: Element) : this(element.document ?: Document(element))

    public fun provideSAXEvents(handler: ContentHandler) {
        handler.setDocumentLocator(NullLocator)
        handler.startDocument()
        val nstack = NamespaceStack()
        for (c in doc.content) {
            when (c.cType) {
                Content.CType.Element -> element(handler, nstack, c as Element)
                Content.CType.ProcessingInstruction -> pi(handler, c as ProcessingInstruction)
                else -> {}
            }
        }
        handler.endDocument()
    }

    private fun pi(handler: ContentHandler, pi: ProcessingInstruction) {
        handler.processingInstruction(pi.target, pi.data)
    }

    private fun element(handler: ContentHandler, nstack: NamespaceStack, element: Element) {
        nstack.push(element)
        try {
            val atts = AttributesImpl()
            for (ns in nstack.addedForward()) handler.startPrefixMapping(ns.prefix, ns.uri)
            if (element.hasAttributes()) {
                for (a in element.attributes) {
                    val type = a.attributeType
                    atts.addAttribute(a.namespaceURI, a.name, a.qualifiedName,
                        if (type == AttributeType.UNDECLARED) "CDATA" else type.name, a.value)
                }
            }
            handler.startElement(element.namespaceURI, element.name, element.qualifiedName, atts)
            for (c in element.content) {
                when (c.cType) {
                    Content.CType.CDATA -> chars(handler, (c as CDATA).text)
                    Content.CType.Text -> chars(handler, (c as Text).text)
                    Content.CType.Element -> element(handler, nstack, c as Element)
                    Content.CType.EntityRef -> handler.skippedEntity((c as EntityRef).name)
                    Content.CType.ProcessingInstruction -> pi(handler, c as ProcessingInstruction)
                    Content.CType.Comment, Content.CType.DocType -> {}
                }
            }
            handler.endElement(element.namespaceURI, element.name, element.qualifiedName)
            for (ns in nstack.addedReverse()) handler.endPrefixMapping(ns.prefix)
        } finally {
            nstack.pop()
        }
    }

    private fun chars(handler: ContentHandler, text: String) {
        val chars = text.toCharArray()
        handler.characters(chars, 0, chars.size)
    }

    private object NullLocator : Locator {
        override val lineNumber: Int get() = -1
        override val columnNumber: Int get() = -1
        override val publicId: String? get() = null
        override val systemId: String? get() = null
    }
}
