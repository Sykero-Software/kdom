package fi.sykero.kdom.input

import fi.sykero.kdom.sax.SAXParseException

/**
 * Decodes an XML document given as bytes, like a JAXP SAX parser reading an `InputStream` (common code, no `java.nio`).
 *
 * The encoding is taken from a UTF-16 byte order mark (BE `FE FF`, LE `FF FE`, consumed) or the `<?xml` pattern of
 * BOM-less UTF-16 (`00 3C 00 3F` / `3C 00 3F 00`), where a declaration must name UTF-16 of that byte order; otherwise
 * (after a UTF-8 byte order mark, consumed) from the XML declaration's `encoding` pseudo-attribute, else UTF-8. As in
 * the JDK parser the declaration wins over a UTF-8 BOM. Supported encodings: UTF-8, UTF-16 (BE/LE), ISO-8859-1, US-ASCII,
 * windows-1252 (with the aliases the JDK parser accepts, matched case-insensitively). Malformed input (an invalid or
 * truncated UTF-8 sequence, an overlong form, an encoded surrogate under the name `UTF-8`, a byte above 0x7F in US-ASCII,
 * an odd UTF-16 length) and an unsupported encoding name are rejected with a
 * [SAXParseException] carrying the line/column of the decoded text so far. (A JAXP parser reports malformed bytes as a
 * `java.io.CharConversionException`; kdom reports every input error as a parse error.)
 */
internal object XmlByteDecoder {
    fun decode(b: ByteArray): String {
        val n = b.size
        fun u(i: Int) = if (i < n) b[i].toInt() and 0xFF else -1
        if (u(0) == 0xEF && u(1) == 0xBB && u(2) == 0xBF) return eightBit(b, 3)
        if (u(0) == 0xFE && u(1) == 0xFF) return checkedUtf16(b, 2, bigEndian = true)
        if (u(0) == 0xFF && u(1) == 0xFE) return checkedUtf16(b, 2, bigEndian = false)
        if (u(0) == 0x00 && u(1) == 0x3C && u(2) == 0x00 && u(3) == 0x3F) return checkedUtf16(b, 0, bigEndian = true)
        if (u(0) == 0x3C && u(1) == 0x00 && u(2) == 0x3F && u(3) == 0x00) return checkedUtf16(b, 0, bigEndian = false)
        return eightBit(b, 0)
    }

    /**
     * An ASCII-compatible document (after a UTF-8 byte order mark, if any): the declaration chooses the encoding, as in
     * the JDK parser (a UTF-8 BOM followed by `encoding="ISO-8859-1"` is read as ISO-8859-1).
     */
    private fun eightBit(b: ByteArray, from: Int): String {
        val enc = declaredEncoding(b.size - from) { (b[from + it].toInt() and 0xFF).toChar() }?.uppercase()
        return when {
            enc == null || enc == "UTF-8" -> utf8(b, from, lenient = false)
            enc == "UTF8" -> utf8(b, from, lenient = true)
            enc in LATIN1 -> latin1(b, from)
            enc in ASCII -> ascii(b, from)
            enc in WINDOWS_1252 -> windows1252(b, from)
            enc.startsWith("UTF-16") || enc == "UTF16" ->
                throw SAXParseException("Encoding \"$enc\" declared, but the bytes are not UTF-16.", 1, 1)
            else -> throw SAXParseException("Unsupported encoding \"$enc\" (kdom reads UTF-8, UTF-16, ISO-8859-1, US-ASCII and windows-1252).", 1, 1)
        }
    }

    /** UTF-16 whose declaration (if any) must name UTF-16 of the same byte order (as the JDK parser checks). */
    private fun checkedUtf16(b: ByteArray, from: Int, bigEndian: Boolean): String {
        val text = utf16(b, from, bigEndian)
        val enc = declaredEncoding(text.length) { text[it] }?.uppercase()
        val ok = enc == null || enc == "UTF-16" || enc == "UTF16" || enc == "UNICODE" ||
            (bigEndian && (enc == "UTF-16BE" || enc == "UNICODEBIG")) || (!bigEndian && (enc == "UTF-16LE" || enc == "UNICODELITTLE"))
        if (!ok) throw SAXParseException("Encoding \"$enc\" declared, but the bytes are UTF-16" + (if (bigEndian) "BE" else "LE") + ".", 1, 1)
        return text
    }

    /** Scan only an exact XML declaration through `?>`, in linear time and without copying the document. */
    private inline fun declaredEncoding(length: Int, charAt: (Int) -> Char): String? {
        val prefix = "<?xml"
        if (length <= prefix.length || prefix.indices.any { charAt(it) != prefix[it] } || !isXmlSpace(charAt(5))) return null
        var close = 5
        while (close + 1 < length && !(charAt(close) == '?' && charAt(close + 1) == '>')) close++
        if (close + 1 >= length) return null // The XML parser reports the unterminated declaration.
        val token = "encoding"
        var k = 5
        while (k + token.length <= close) {
            if (isXmlSpace(charAt(k - 1)) && token.indices.all { charAt(k + it) == token[it] }) break
            k++
        }
        k += token.length
        while (k < close && isXmlSpace(charAt(k))) k++
        if (k >= close || charAt(k) != '=') return null
        k++
        while (k < close && isXmlSpace(charAt(k))) k++
        if (k >= close || (charAt(k) != '"' && charAt(k) != '\'')) return null
        val quote = charAt(k++)
        val start = k
        while (k < close && charAt(k) != quote) k++
        return if (k == close) null else buildString(k - start) { for (p in start until k) append(charAt(p)) }
    }

    // Encoding names, matched case-insensitively, as accepted by the JDK parser (each checked against it, see
    // XmlByteDecoderDifferentialTest). "UTF-8" is decoded strictly (the JDK's own UTF-8 reader); the Java alias "UTF8" goes
    // through the JDK's charset decoder, which replaces malformed bytes by U+FFFD.
    private val LATIN1 = setOf("ISO-8859-1", "ISO8859-1", "ISO_8859-1", "ISO8859_1", "ISO_8859_1", "LATIN1", "L1", "CP819", "IBM819")
    private val ASCII = setOf("US-ASCII", "ASCII", "ISO646-US", "US")
    private val WINDOWS_1252 = setOf("WINDOWS-1252", "CP1252")

    /** windows-1252 0x80..0x9F as the JDK decodes them (0x81, 0x8D, 0x8F, 0x90, 0x9D are undefined: U+FFFD). */
    private val CP1252_HIGH = charArrayOf(
        '\u20AC', '\uFFFD', '\u201A', '\u0192', '\u201E', '\u2026', '\u2020', '\u2021',
        '\u02C6', '\u2030', '\u0160', '\u2039', '\u0152', '\uFFFD', '\u017D', '\uFFFD',
        '\uFFFD', '\u2018', '\u2019', '\u201C', '\u201D', '\u2022', '\u2013', '\u2014',
        '\u02DC', '\u2122', '\u0161', '\u203A', '\u0153', '\uFFFD', '\u017E', '\u0178',
    )

    private fun windows1252(b: ByteArray, from: Int): String {
        val sb = StringBuilder(b.size)
        for (k in from until b.size) {
            val c = b[k].toInt() and 0xFF
            sb.append(if (c in 0x80..0x9F) CP1252_HIGH[c - 0x80] else c.toChar())
        }
        return sb.toString()
    }

    private fun fail(decoded: StringBuilder, message: String): Nothing {
        val lc = lineColumnOf(decoded, decoded.length)
        throw SAXParseException(message, lc[0], lc[1])
    }

    /**
     * UTF-8. Strict: a malformed sequence is a parse error. [lenient]: it becomes U+FFFD (the lead byte with the valid
     * continuation bytes read so far, then decoding resumes at the offending byte), as the JDK's charset decoder does.
     */
    private fun utf8(b: ByteArray, from: Int, lenient: Boolean): String {
        val n = b.size
        val sb = StringBuilder(n)
        var i = from
        while (i < n) {
            val b0 = b[i].toInt() and 0xFF
            if (b0 < 0x80) { sb.append(b0.toChar()); i++; continue }
            val len: Int
            var cp: Int
            var min2 = 0x80
            var max2 = 0xBF
            when (b0) {
                in 0xC2..0xDF -> { len = 2; cp = b0 and 0x1F }
                in 0xE0..0xEF -> {
                    len = 3; cp = b0 and 0x0F
                    if (b0 == 0xE0) min2 = 0xA0 // overlong
                    if (b0 == 0xED) max2 = 0x9F // UTF-16 surrogates
                }
                in 0xF0..0xF4 -> {
                    len = 4; cp = b0 and 0x07
                    if (b0 == 0xF0) min2 = 0x90 // overlong
                    if (b0 == 0xF4) max2 = 0x8F // above U+10FFFF
                }
                else -> {
                    if (!lenient) fail(sb, "Invalid byte 1 of 1-byte UTF-8 sequence.")
                    sb.append('\uFFFD'); i++; continue
                }
            }
            var bad = 0
            if (lenient && b0 == 0xED && i + 2 < n && (b[i + 1].toInt() and 0xFF) in 0xA0..0xBF && (b[i + 2].toInt() and 0xC0) == 0x80) {
                // an encoded surrogate: the JDK's decoder replaces the whole 3-byte sequence by one U+FFFD
                sb.append('\uFFFD'); i += 3; continue
            }
            for (k in 1 until len) {
                if (i + k >= n) {
                    if (!lenient) fail(sb, "Invalid byte ${k + 1} of $len-byte UTF-8 sequence (end of input).")
                    bad = k; break
                }
                val bk = b[i + k].toInt() and 0xFF
                val lo = if (k == 1) min2 else 0x80
                val hi = if (k == 1) max2 else 0xBF
                if (bk < lo || bk > hi) {
                    if (!lenient) fail(sb, "Invalid byte ${k + 1} of $len-byte UTF-8 sequence.")
                    bad = k; break
                }
                cp = (cp shl 6) or (bk and 0x3F)
            }
            if (bad > 0) { sb.append('\uFFFD'); i += bad; continue }
            if (cp >= 0x10000) {
                val v = cp - 0x10000
                sb.append((0xD800 + (v shr 10)).toChar())
                sb.append((0xDC00 + (v and 0x3FF)).toChar())
            } else {
                sb.append(cp.toChar())
            }
            i += len
        }
        return sb.toString()
    }

    private fun utf16(b: ByteArray, from: Int, bigEndian: Boolean): String {
        val sb = StringBuilder((b.size - from) / 2)
        if ((b.size - from) % 2 != 0) {
            // decode what is there for the position, then reject the dangling byte
            var i = from
            while (i + 1 < b.size) { sb.append(char16(b, i, bigEndian)); i += 2 }
            fail(sb, "Invalid UTF-16 input: odd number of bytes.")
        }
        var i = from
        while (i < b.size) { sb.append(char16(b, i, bigEndian)); i += 2 }
        return sb.toString()
    }

    private fun char16(b: ByteArray, i: Int, bigEndian: Boolean): Char {
        val hi = b[i].toInt() and 0xFF
        val lo = b[i + 1].toInt() and 0xFF
        return (if (bigEndian) (hi shl 8) or lo else (lo shl 8) or hi).toChar()
    }

    private fun latin1(b: ByteArray, from: Int): String {
        val sb = StringBuilder(b.size)
        for (k in from until b.size) sb.append((b[k].toInt() and 0xFF).toChar())
        return sb.toString()
    }

    private fun ascii(b: ByteArray, from: Int): String {
        val sb = StringBuilder(b.size)
        for (k in from until b.size) {
            val c = b[k].toInt() and 0xFF
            if (c > 0x7F) fail(sb, "Byte 0x" + c.toString(16) + " is not 7-bit US-ASCII.")
            sb.append(c.toChar())
        }
        return sb.toString()
    }
}
