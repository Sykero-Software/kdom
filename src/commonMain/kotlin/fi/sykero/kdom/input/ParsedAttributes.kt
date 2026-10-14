package fi.sykero.kdom.input

import fi.sykero.kdom.sax.Attributes

/** The attributes of one start tag as [XmlEventParser] reports them (SAX2 semantics, every type is `CDATA`). */
internal class ParsedAttributes : Attributes {
    private val uris = ArrayList<String>()
    private val localNames = ArrayList<String>()
    private val qNames = ArrayList<String>()
    private val values = ArrayList<String>()

    fun add(uri: String, localName: String, qName: String, value: String) {
        uris.add(uri); localNames.add(localName); qNames.add(qName); values.add(value)
    }

    override val length: Int get() = qNames.size
    override fun getURI(index: Int): String? = uris.getOrNull(index)
    override fun getLocalName(index: Int): String? = localNames.getOrNull(index)
    override fun getQName(index: Int): String? = qNames.getOrNull(index)
    override fun getType(index: Int): String? = if (index in qNames.indices) CDATA else null
    override fun getValue(index: Int): String? = values.getOrNull(index)

    override fun getIndex(uri: String, localName: String): Int {
        for (i in qNames.indices) if (uris[i] == uri && localNames[i] == localName) return i
        return -1
    }

    override fun getIndex(qName: String): Int = qNames.indexOf(qName)
    override fun getType(uri: String, localName: String): String? = getType(getIndex(uri, localName))
    override fun getType(qName: String): String? = getType(getIndex(qName))
    override fun getValue(uri: String, localName: String): String? = getValue(getIndex(uri, localName))
    override fun getValue(qName: String): String? = getValue(getIndex(qName))

    private companion object {
        const val CDATA = "CDATA"
    }
}

/**
 * The `org.xml.sax.ext.LexicalHandler` events kdom needs (comments and CDATA boundaries). A [fi.sykero.kdom.sax.ContentHandler]
 * passed to [XmlEventParser] that also implements this interface receives them, as a SAX `LexicalHandler` would.
 * Internal: only kdom's own tree builder uses it.
 */
internal interface LexicalEventHandler {
    fun comment(ch: CharArray, start: Int, length: Int)
    fun startCDATA()
    fun endCDATA()
}
