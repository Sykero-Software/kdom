package fi.sykero.kdom.input

import fi.sykero.kdom.sax.SAXParseException

/** XML 1.0 white space (`S` production). */
internal fun isXmlSpace(c: Char): Boolean = c == ' ' || c == '\t' || c == '\n' || c == '\r'

/** XML 1.0 `Char` production for a code point. */
internal fun isXmlCodePoint(cp: Int): Boolean =
    cp == 0x9 || cp == 0xA || cp == 0xD || cp in 0x20..0xD7FF || cp in 0xE000..0xFFFD || cp in 0x10000..0x10FFFF

/** 1-based line and column of [offset] in [s]; `\r\n`, `\r` and `\n` each end a line (XML 1.0 line ends). */
internal fun lineColumnOf(s: CharSequence, offset: Int): IntArray {
    var line = 1
    var col = 1
    var i = 0
    val end = minOf(offset, s.length)
    while (i < end) {
        when (s[i]) {
            '\n' -> { line++; col = 1 }
            '\r' -> {
                line++; col = 1
                if (i + 1 < end && s[i + 1] == '\n') i++
            }
            else -> col++
        }
        i++
    }
    return intArrayOf(line, col)
}

/**
 * Raw-source pre-scan of a document before xmlutil reads it. It enforces the well-formedness rules that xmlutil
 * 1.0.2.1's reader does not (docs/xmlutil-probes.md):
 *
 * - P5: every character of the document is an XML 1.0 `Char` (surrogates paired);
 * - P6: no literal `<` in a quoted attribute value (ruling R7);
 * - P8: every character reference names a legal `Char`, and is well-formed (`&#;`, `&#x;` are rejected);
 * - P7: no `<!DOCTYPE` (plan rule), and no other `<!` markup declaration;
 * - P2 (raw part): no attribute qualified name twice in a start tag (this also catches a repeated `xmlns:p`);
 * - white space between attributes, no `]]>` in character data, no byte order mark character at the start of a
 *   string (SAX: "Content is not allowed in prolog").
 *
 * It also records the positions the parser masks before handing the text to xmlutil (see [XmlSourceMask]):
 * the U+0085 / U+2028 characters (xmlutil applies XML 1.1 line-end handling to them, SAX 1.0 keeps them), and in
 * namespace-unaware mode the colons in element and attribute names and attribute names that are exactly `xmlns`.
 *
 * Comments, CDATA sections and processing instructions are skipped as wholes; their inner syntax is left to xmlutil.
 */
internal class SourceScanner(private val s: String, private val namespaceAware: Boolean) {
    private val n = s.length
    private var i = 0

    /** Offsets of `:` inside element/attribute names (namespace-unaware mode only). */
    val nameColons: MutableList<Int> = ArrayList()

    /** Offsets of the final `s` of attribute names that are exactly `xmlns` (namespace-unaware mode only). */
    val xmlnsNames: MutableList<Int> = ArrayList()

    /** Offsets of carriage returns not followed by a line feed (xmlutil folds `\r\r` into one line end). */
    val loneCarriageReturns: MutableList<Int> = ArrayList()

    /** Offsets of U+0085 and U+2028 characters. */
    val lineEndChars: MutableList<Int> = ArrayList()

    /** Values of character references in the private use range U+E000..U+F8FF (candidates for mask characters). */
    val privateUseRefs: MutableSet<Int> = HashSet()

    fun scan() {
        checkCharacters()
        if (n > 0 && s[0] == '\uFEFF') fail(0, "Content is not allowed in prolog.")
        while (i < n) {
            when (s[i]) {
                '<' -> markup()
                '&' -> if (i + 1 < n && s[i + 1] == '#') i = charRef(i) else i++
                ']' -> {
                    if (s.startsWith("]]>", i)) {
                        fail(i, "The character sequence \"]]>\" must not appear in content unless used to mark the end of a CDATA section.")
                    }
                    i++
                }
                else -> i++
            }
        }
    }

    private fun checkCharacters() {
        var k = 0
        while (k < n) {
            val c = s[k]
            when {
                c.isHighSurrogate() -> {
                    if (k + 1 < n && s[k + 1].isLowSurrogate()) k++ else invalidChar(k, c)
                }
                c.isLowSurrogate() -> invalidChar(k, c)
                c == '\u0085' || c == '\u2028' -> lineEndChars.add(k)
                c == '\r' -> if (k + 1 >= n || s[k + 1] != '\n') loneCarriageReturns.add(k)
                !isXmlCodePoint(c.code) -> invalidChar(k, c)
            }
            k++
        }
    }

    private fun invalidChar(at: Int, c: Char): Nothing =
        fail(at, "An invalid XML character (Unicode: 0x" + c.code.toString(16) + ") was found in the document.")

    private fun markup() {
        when {
            s.startsWith("<!--", i) -> i = skipPast("-->", i + 4, "comment")
            s.startsWith("<![CDATA[", i) -> i = skipPast("]]>", i + 9, "CDATA section")
            s.startsWith("<!DOCTYPE", i) -> fail(i, "DOCTYPE is not allowed (kdom does not process DTDs).")
            s.startsWith("<!", i) -> fail(i, "The markup in the document must be well-formed.")
            s.startsWith("<?", i) -> i = skipPast("?>", i + 2, "processing instruction")
            s.startsWith("</", i) -> endTag()
            else -> startTag()
        }
    }

    private fun skipPast(terminator: String, from: Int, what: String): Int {
        val end = s.indexOf(terminator, from)
        if (end < 0) fail(n, "XML document structures must start and end within the same entity (unterminated $what).")
        return end + terminator.length
    }

    /** End of a name starting at [from]; names end at white space, markup delimiters or the end of input. */
    private fun nameEnd(from: Int): Int {
        var k = from
        while (k < n) {
            val c = s[k]
            if (isXmlSpace(c) || c == '/' || c == '>' || c == '=' || c == '"' || c == '\'' || c == '<') break
            k++
        }
        return k
    }

    private fun recordColons(from: Int, to: Int) {
        if (namespaceAware) return
        for (k in from until to) if (s[k] == ':') nameColons.add(k)
    }

    private fun skipSpace(from: Int): Int {
        var k = from
        while (k < n && isXmlSpace(s[k])) k++
        return k
    }

    private fun endTag() {
        val start = i + 2
        val end = nameEnd(start)
        if (end == start) fail(start, "The end-tag must have a name.")
        recordColons(start, end)
        val k = skipSpace(end)
        if (k >= n || s[k] != '>') fail(k, "The end-tag for element type \"" + s.substring(start, end) + "\" must end with a '>' delimiter.")
        i = k + 1
    }

    private fun startTag() {
        val start = i + 1
        val end = nameEnd(start)
        if (end == start) fail(start, "The markup in the document must be well-formed (a start tag needs a name).")
        recordColons(start, end)
        val element = s.substring(start, end)
        var seen: MutableSet<String>? = null
        var k = end
        while (true) {
            val wsStart = k
            k = skipSpace(k)
            if (k >= n) fail(k, "XML document structures must start and end within the same entity.")
            val c = s[k]
            if (c == '>') { i = k + 1; return }
            if (c == '/') {
                if (k + 1 < n && s[k + 1] == '>') { i = k + 2; return }
                fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            }
            if (k == wsStart) fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            // attribute name
            val aStart = k
            val aEnd = nameEnd(k)
            if (aEnd == aStart) fail(k, "Element type \"$element\" must be followed by either attribute specifications, \">\" or \"/>\".")
            val aName = s.substring(aStart, aEnd)
            if (seen == null) seen = HashSet()
            if (!seen.add(aName)) fail(aStart, "Attribute \"$aName\" was already specified for element \"$element\".")
            recordColons(aStart, aEnd)
            if (!namespaceAware && aName == "xmlns") xmlnsNames.add(aStart + 4)
            k = skipSpace(aEnd)
            if (k >= n || s[k] != '=') fail(k, "Attribute name \"$aName\" associated with an element type \"$element\" must be followed by the ' = ' character.")
            k = skipSpace(k + 1)
            if (k >= n || (s[k] != '"' && s[k] != '\'')) fail(k, "Open quote is expected for attribute \"$aName\" associated with an element type \"$element\".")
            val quote = s[k]
            k++
            while (true) {
                if (k >= n) fail(k, "XML document structures must start and end within the same entity.")
                val v = s[k]
                when {
                    v == quote -> break
                    v == '<' -> fail(k, "The value of attribute \"$aName\" associated with an element type \"$element\" must not contain the '<' character.")
                    v == '&' && k + 1 < n && s[k + 1] == '#' -> k = charRef(k)
                    else -> k++
                }
            }
            k++
        }
    }

    /** Checks the character reference at [at] (`&#...;`) and returns the offset after its `;`. */
    private fun charRef(at: Int): Int {
        var k = at + 2
        val hex = k < n && s[k] == 'x'
        if (hex) k++
        val digitsStart = k
        var value = 0
        while (k < n) {
            val d = when (val c = s[k]) {
                in '0'..'9' -> c - '0'
                in 'a'..'f' -> if (hex) c - 'a' + 10 else -1
                in 'A'..'F' -> if (hex) c - 'A' + 10 else -1
                else -> -1
            }
            if (d < 0) break
            value = if (value > 0x10FFFF) value else value * (if (hex) 16 else 10) + d
            k++
        }
        if (k == digitsStart) {
            fail(k, if (hex) "A hexadecimal representation must immediately follow the \"&#x\" in a character reference."
                    else "A decimal representation must immediately follow the \"&#\" in a character reference.")
        }
        if (k >= n || s[k] != ';') fail(k, "The character reference must end with the ';' delimiter.")
        if (!isXmlCodePoint(value)) fail(k, "Character reference \"" + s.substring(at, k) + "\" is an invalid XML character.")
        if (value in 0xE000..0xF8FF) privateUseRefs.add(value)
        return k + 1
    }

    private fun fail(at: Int, message: String): Nothing {
        val lc = lineColumnOf(s, at)
        throw SAXParseException(message, lc[0], lc[1])
    }
}
