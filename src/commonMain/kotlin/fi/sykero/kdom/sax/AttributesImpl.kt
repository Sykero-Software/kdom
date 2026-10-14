package fi.sykero.kdom.sax

/**
 * Mutable [Attributes] list with the semantics of `org.xml.sax.helpers.AttributesImpl`: out-of-range indexes and absent
 * names give `null` (`-1` for `getIndex`); the first attribute with a matching name wins; the type is returned as stored.
 */
public class AttributesImpl : Attributes {
    private val uris = ArrayList<String>()
    private val localNames = ArrayList<String>()
    private val qNames = ArrayList<String>()
    private val types = ArrayList<String>()
    private val values = ArrayList<String>()

    override val length: Int get() = uris.size

    public fun addAttribute(uri: String, localName: String, qName: String, type: String, value: String) {
        uris += uri; localNames += localName; qNames += qName; types += type; values += value
    }

    public fun clear() {
        uris.clear(); localNames.clear(); qNames.clear(); types.clear(); values.clear()
    }

    override fun getURI(index: Int): String? = uris.getOrNull(index)
    override fun getLocalName(index: Int): String? = localNames.getOrNull(index)
    override fun getQName(index: Int): String? = qNames.getOrNull(index)
    override fun getType(index: Int): String? = types.getOrNull(index)
    override fun getValue(index: Int): String? = values.getOrNull(index)

    override fun getIndex(uri: String, localName: String): Int {
        for (i in uris.indices) if (uris[i] == uri && localNames[i] == localName) return i
        return -1
    }

    override fun getIndex(qName: String): Int = qNames.indexOf(qName)

    override fun getType(uri: String, localName: String): String? = getType(getIndex(uri, localName))
    override fun getType(qName: String): String? = getType(getIndex(qName))
    override fun getValue(uri: String, localName: String): String? = getValue(getIndex(uri, localName))
    override fun getValue(qName: String): String? = getValue(getIndex(qName))
}
