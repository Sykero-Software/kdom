package fi.sykero.kdom.sax

/**
 * SAX2 `org.xml.sax.SAXParseException`: a well-formedness error at [lineNumber]/[columnNumber] (1-based; -1 when the
 * position is not known).
 */
public open class SAXParseException(
    message: String?,
    public val lineNumber: Int,
    public val columnNumber: Int,
    cause: Throwable? = null,
) : SAXException(message, cause)
