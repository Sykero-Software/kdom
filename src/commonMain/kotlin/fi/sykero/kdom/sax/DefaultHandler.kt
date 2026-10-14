package fi.sykero.kdom.sax

/**
 * A [ContentHandler] whose methods all do nothing; override what is needed. Counterpart of
 * `org.xml.sax.helpers.DefaultHandler` (only the `ContentHandler` part).
 */
public open class DefaultHandler : ContentHandler {
    override fun setDocumentLocator(locator: Locator?) {}
    override fun startDocument() {}
    override fun endDocument() {}
    override fun startPrefixMapping(prefix: String, uri: String) {}
    override fun endPrefixMapping(prefix: String) {}
    override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {}
    override fun endElement(uri: String, localName: String, qName: String) {}
    override fun characters(ch: CharArray, start: Int, length: Int) {}
    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {}
    override fun processingInstruction(target: String, data: String?) {}
    override fun skippedEntity(name: String) {}
}
