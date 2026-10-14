package fi.sykero.kdom.input

import fi.sykero.kdom.sax.SAXParseException

/**
 * Decodes an XML document given as bytes, like a JAXP SAX parser reading an `InputStream` (common code, no `java.nio`).
 *
 * The encoding is taken from a byte order mark (UTF-8 `EF BB BF`, UTF-16 BE `FE FF`, UTF-16 LE `FF FE`, consumed), from
 * the `<?xml` pattern of BOM-less UTF-16 (`00 3C 00 3F` / `3C 00 3F 00`), else from the XML declaration's `encoding`
 * pseudo-attribute, else UTF-8. Supported encodings: UTF-8, UTF-16 (BE/LE), ISO-8859-1, US-ASCII (with their usual
 * aliases). Malformed input (an invalid or truncated UTF-8 sequence, an overlong form, an encoded surrogate, a byte
 * above 0x7F in US-ASCII, an odd UTF-16 length) and an unsupported encoding name are rejected with a
 * [SAXParseException] carrying the line/column of the decoded text so far. (A JAXP parser reports malformed bytes as a
 * `java.io.CharConversionException`; kdom reports every input error as a parse error.)
 */
internal object XmlByteDecoder {
    fun decode(b: ByteArray): String {
        val n = b.size
        fun u(i: Int) = if (i < n) b[i].toInt() and 0xFF else -1
        if (u(0) == 0xEF && u(1) == 0xBB && u(2) == 0xBF) return utf8(b, 3)
        if (u(0) == 0xFE && u(1) == 0xFF) return utf16(b, 2, bigEndian = true)
        if (u(0) == 0xFF && u(1) == 0xFE) return utf16(b, 2, bigEndian = false)
        if (u(0) == 0x00 && u(1) == 0x3C && u(2) == 0x00 && u(3) == 0x3F) return utf16(b, 0, bigEndian = true)
        if (u(0) == 0x3C && u(1) == 0x00 && u(2) == 0x3F && u(3) == 0x00) return utf16(b, 0, bigEndian = false)
        return when (val enc = declaredEncoding(b)?.uppercase()) {
            null, "UTF-8", "UTF8" -> utf8(b, 0)
            "ISO-8859-1", "ISO8859-1", "ISO_8859-1", "ISO8859_1", "LATIN1", "LATIN-1", "L1", "ISO-LATIN-1", "CP819", "IBM819" -> latin1(b)
            "US-ASCII", "ASCII", "ISO646-US", "US" -> ascii(b)
            "UTF-16", "UTF-16BE", "UTF-16LE", "UTF16" ->
                throw SAXParseException("Encoding \"$enc\" declared, but the bytes are not UTF-16 (no byte order mark).", 1, 1)
            else -> throw SAXParseException("Unsupported encoding \"$enc\" (kdom reads UTF-8, UTF-16, ISO-8859-1 and US-ASCII).", 1, 1)
        }
    }

    /** The `encoding` value of an XML declaration at the very start of [b] (read as ASCII), or null. */
    private fun declaredEncoding(b: ByteArray): String? {
        if (b.size < 5 || b[0] != '<'.code.toByte() || b[1] != '?'.code.toByte() || b[2] != 'x'.code.toByte() ||
            b[3] != 'm'.code.toByte() || b[4] != 'l'.code.toByte()) return null
        val sb = StringBuilder()
        var i = 5
        while (i < b.size && i < 1024) {
            val c = (b[i].toInt() and 0xFF).toChar()
            sb.append(c)
            if (c == '>') break
            i++
        }
        val decl = sb.toString()
        var k = decl.indexOf("encoding")
        if (k < 0) return null
        k += "encoding".length
        while (k < decl.length && isXmlSpace(decl[k])) k++
        if (k >= decl.length || decl[k] != '=') return null
        k++
        while (k < decl.length && isXmlSpace(decl[k])) k++
        if (k >= decl.length || (decl[k] != '"' && decl[k] != '\'')) return null
        val end = decl.indexOf(decl[k], k + 1)
        return if (end < 0) null else decl.substring(k + 1, end)
    }

    private fun fail(decoded: StringBuilder, message: String): Nothing {
        val lc = lineColumnOf(decoded, decoded.length)
        throw SAXParseException(message, lc[0], lc[1])
    }

    private fun utf8(b: ByteArray, from: Int): String {
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
                else -> fail(sb, "Invalid byte 1 of 1-byte UTF-8 sequence.")
            }
            for (k in 1 until len) {
                if (i + k >= n) fail(sb, "Invalid byte ${k + 1} of $len-byte UTF-8 sequence (end of input).")
                val bk = b[i + k].toInt() and 0xFF
                val lo = if (k == 1) min2 else 0x80
                val hi = if (k == 1) max2 else 0xBF
                if (bk < lo || bk > hi) fail(sb, "Invalid byte ${k + 1} of $len-byte UTF-8 sequence.")
                cp = (cp shl 6) or (bk and 0x3F)
            }
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

    private fun latin1(b: ByteArray): String {
        val sb = StringBuilder(b.size)
        for (x in b) sb.append((x.toInt() and 0xFF).toChar())
        return sb.toString()
    }

    private fun ascii(b: ByteArray): String {
        val sb = StringBuilder(b.size)
        for (x in b) {
            val c = x.toInt() and 0xFF
            if (c > 0x7F) fail(sb, "Byte 0x" + c.toString(16) + " is not 7-bit US-ASCII.")
            sb.append(c.toChar())
        }
        return sb.toString()
    }
}
