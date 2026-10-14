package fi.sykero.kdom.diff

import fi.sykero.kdom.input.XmlEventParser
import java.io.ByteArrayInputStream
import javax.xml.parsers.SAXParserFactory
import org.junit.Assert.fail
import org.junit.Test
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/**
 * Byte input decoding of [XmlEventParser.parse] (ByteArray) against the JDK parser reading an `InputStream`: for each
 * declared encoding name (several spellings) and each body (all high bytes 0x80..0xFF, UTF-8 text, malformed UTF-8), the
 * verdict and the decoded character data must be equal (M0 gate fix I3).
 */
class XmlByteDecoderDifferentialTest {
    private val names = listOf(
        null, "UTF-8", "utf-8", "UTF8", "utf8", "ISO-8859-1", "iso-8859-1", "latin1", "LATIN1", "iso_8859_1", "ISO8859_1", "l1",
        "cp819", "ibm819", "US-ASCII", "ascii", "us", "ISO646-US", "windows-1252", "WINDOWS-1252", "cp1252", "Cp1252",
        "iso-latin-1", "foo",
    )

    private val bodies: List<ByteArray> = listOf(
        ByteArray(0x80) { (0x80 + it).toByte() }.filter { it.toInt() and 0xFF != 0x85 }.toByteArray() +
            byteArrayOf(0x85.toByte()), // NEL last
        "\u00E9\u20AC\uD83D\uDE00 x".encodeToByteArray(),
        byteArrayOf(0xE9.toByte()),
        byteArrayOf(0xC0.toByte(), 0xAF.toByte()),
        byteArrayOf(0xED.toByte(), 0xA0.toByte(), 0x80.toByte()),
        byteArrayOf(0xF4.toByte(), 0x90.toByte(), 0x80.toByte(), 0x80.toByte()),
        byteArrayOf(0xE2.toByte(), 0x82.toByte()),
        byteArrayOf(0xF0.toByte(), 0x9F.toByte(), 0x98.toByte()),
        byteArrayOf(0xE2.toByte(), 0x82.toByte(), 0xAC.toByte(), 0xFF.toByte(), 0xFE.toByte(), 0x41),
    )

    @Test fun decodingMatchesTheJdk() {
        val problems = ArrayList<String>()
        for (name in names) for ((bi, body) in bodies.withIndex()) {
            val head = (if (name == null) "" else "<?xml version=\"1.0\" encoding=\"$name\"?>") + "<a>"
            val bytes = head.encodeToByteArray() + body + "</a>".encodeToByteArray()
            val jdk = try { jdk(bytes) } catch (e: Exception) { "REJECT" }
            val kdom = try { kdom(bytes) } catch (e: fi.sykero.kdom.sax.SAXParseException) { "REJECT" } catch (e: Exception) { "LEAK $e" }
            if (jdk != kdom) problems += "encoding=$name body#$bi\n  JDK : $jdk\n  kdom: $kdom"
        }
        if (problems.isNotEmpty()) fail(problems.size.toString() + " mismatches:\n" + problems.joinToString("\n"))
    }

    private fun hex(sb: CharSequence) = sb.map { it.code.toString(16) }.joinToString(" ")

    private fun jdk(bytes: ByteArray): String {
        val sb = StringBuilder()
        SAXParserFactory.newInstance().newSAXParser().parse(InputSource(ByteArrayInputStream(bytes)), object : DefaultHandler() {
            override fun characters(ch: CharArray, start: Int, length: Int) { sb.append(ch, start, length) }
        })
        return hex(sb)
    }

    private fun kdom(bytes: ByteArray): String {
        val sb = StringBuilder()
        XmlEventParser(namespaceAware = false).parse(bytes, object : fi.sykero.kdom.sax.ContentHandler {
            override fun setDocumentLocator(locator: fi.sykero.kdom.sax.Locator?) {}
            override fun startDocument() {}
            override fun endDocument() {}
            override fun startPrefixMapping(prefix: String, uri: String) {}
            override fun endPrefixMapping(prefix: String) {}
            override fun startElement(uri: String, localName: String, qName: String, atts: fi.sykero.kdom.sax.Attributes) {}
            override fun endElement(uri: String, localName: String, qName: String) {}
            override fun characters(ch: CharArray, start: Int, length: Int) { sb.appendRange(ch, start, start + length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {}
            override fun processingInstruction(target: String, data: String?) {}
            override fun skippedEntity(name: String) {}
        })
        return hex(sb)
    }
}
