package fi.sykero.kdom.sax

/** SAX2 `org.xml.sax.ContentHandler`, same call order and arguments. */
public interface ContentHandler {
    public fun setDocumentLocator(locator: Locator?)
    public fun startDocument()
    public fun endDocument()
    public fun startPrefixMapping(prefix: String, uri: String)
    public fun endPrefixMapping(prefix: String)
    public fun startElement(uri: String, localName: String, qName: String, atts: Attributes)
    public fun endElement(uri: String, localName: String, qName: String)
    public fun characters(ch: CharArray, start: Int, length: Int)
    public fun ignorableWhitespace(ch: CharArray, start: Int, length: Int)
    public fun processingInstruction(target: String, data: String?)
    public fun skippedEntity(name: String)
}
