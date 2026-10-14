package fi.sykero.kdom.sax

/** SAX2 `org.xml.sax.SAXException`: a general SAX error or warning, optionally wrapping a [cause]. */
public open class SAXException(message: String?, cause: Throwable? = null) : Exception(message, cause) {
    public constructor(cause: Throwable) : this(cause.message, cause)
}
