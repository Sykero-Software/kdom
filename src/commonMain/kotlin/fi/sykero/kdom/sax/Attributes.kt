package fi.sykero.kdom.sax

/** SAX2 `org.xml.sax.Attributes`, same semantics; property rule applied to `getLength()`. */
public interface Attributes {
    public val length: Int
    public fun getURI(index: Int): String?
    public fun getLocalName(index: Int): String?
    public fun getQName(index: Int): String?
    public fun getType(index: Int): String?
    public fun getValue(index: Int): String?
    public fun getIndex(uri: String, localName: String): Int
    public fun getIndex(qName: String): Int
    public fun getType(uri: String, localName: String): String?
    public fun getType(qName: String): String?
    public fun getValue(uri: String, localName: String): String?
    public fun getValue(qName: String): String?
}
