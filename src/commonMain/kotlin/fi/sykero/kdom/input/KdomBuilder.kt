package fi.sykero.kdom.input

import fi.sykero.kdom.Document
import fi.sykero.kdom.JDOMException
import fi.sykero.kdom.sax.SAXException
import fi.sykero.kdom.sax.SAXParseException
import kotlin.Throws

/**
 * Replacement for jdom2's `SAXBuilder` (namespace-aware, no DTD, no external entities). Not thread-safe per instance,
 * like SAXBuilder; cheap to create.
 *
 * The tree is the one jdom2 2.0.6.1's `SAXBuilder` builds with its defaults (see [TreeBuildingHandler]); the input is
 * parsed by [XmlEventParser]. Malformed input throws [JDOMParseException] (message `Error on line N: ...`, the
 * [SAXParseException] as cause, its line/column, and the partial document when the root element was started), as
 * jdom2's `SAXBuilder` did.
 */
public class KdomBuilder {
    private val parser = XmlEventParser(namespaceAware = true)

    @Throws(JDOMException::class)
    public fun build(xml: String): Document = build { parser.parse(xml, it) }

    @Throws(JDOMException::class)
    public fun build(xml: ByteArray): Document = build { parser.parse(xml, it) }

    private inline fun build(parse: (TreeBuildingHandler) -> Unit): Document {
        val handler = TreeBuildingHandler()
        try {
            parse(handler)
        } catch (e: SAXParseException) {
            val doc = handler.document.takeIf { it.hasRootElement() }
            throw JDOMParseException("Error on line " + e.lineNumber + ": " + e.message, e, doc, null, null, e.lineNumber, e.columnNumber)
        } catch (e: SAXException) {
            throw JDOMParseException("Error in building: " + e.message, e, handler.document)
        }
        return handler.document
    }
}
