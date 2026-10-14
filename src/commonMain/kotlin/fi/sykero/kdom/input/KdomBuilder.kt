package fi.sykero.kdom.input

import fi.sykero.kdom.Document
import fi.sykero.kdom.JDOMException
import fi.sykero.kdom.sax.SAXException
import fi.sykero.kdom.sax.SAXParseException
import kotlin.Throws
import kotlin.jvm.JvmOverloads

/**
 * Replacement for jdom2's `SAXBuilder` (namespace-aware, no DTD, no external entities). Not thread-safe per instance,
 * like SAXBuilder; cheap to create.
 *
 * The tree is the one jdom2 2.0.6.1's `SAXBuilder` builds with its defaults (see [TreeBuildingHandler]); the input is
 * parsed by [XmlEventParser]. Malformed input throws [JDOMParseException] (message `Error on line N: ...`, the
 * [SAXParseException] as cause, its line/column, and the partial document when the root element was started), as
 * jdom2's `SAXBuilder` did.
 *
 * Unlike jdom2, a failure of the tree layer (`IllegalNameException`, `IllegalTargetException`, `IllegalDataException`,
 * `IllegalAddException` or any other `RuntimeException` while building, e.g. a namespace URI starting with white space
 * that the parser accepts but `Namespace` rejects) is also wrapped in a [JDOMParseException] with the position of the
 * event, so callers that catch [JDOMException] (JSword's fallback chain) see every rejected input.
 */
public class KdomBuilder @JvmOverloads constructor(
    /**
     * false (default, strict, P7): a DOCTYPE is a parse error; JSword's OSIS/ThML parsing keeps this. true (permissive, R11):
     * for EPUB XHTML/OPF/NCX and other non-Sword XML; the DOCTYPE becomes a [fi.sykero.kdom.DocType] (as jdom2's
     * SAXBuilder builds it), the external subset is never read, internal-subset entities are expanded, and an undeclared
     * entity under an unread external subset becomes an [fi.sykero.kdom.EntityRef] (see [XmlEventParser.allowDocType]).
     */
    public val allowDocType: Boolean = false,
) {
    private val parser = XmlEventParser(namespaceAware = true, allowDocType = allowDocType)

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
        } catch (e: RuntimeException) {
            val line = handler.locator?.lineNumber ?: -1
            val column = handler.locator?.columnNumber ?: -1
            val doc = handler.document.takeIf { it.hasRootElement() }
            throw JDOMParseException("Error on line $line: " + e.message, e, doc, null, null, line, column)
        }
        return handler.document
    }
}
