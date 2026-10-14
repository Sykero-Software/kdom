package fi.sykero.kdom.sax

/** SAX2 `org.xml.sax.Locator`: the parser's current position, valid only during a callback. */
public interface Locator {
    public val lineNumber: Int
    public val columnNumber: Int
    public val publicId: String?
    public val systemId: String?
}
