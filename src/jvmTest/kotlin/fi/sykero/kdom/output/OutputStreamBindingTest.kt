package fi.sykero.kdom.output

import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue

/**
 * Which `output` overload a JVM caller binds to (translation-notes, "Behaviour differences"). A `PrintStream` is both an
 * `OutputStream` and an `Appendable`: the member `output(x, Appendable)` wins over the `output(x, OutputStream)`
 * extension of `XMLOutputterJvm.kt`, so the stream's own encoding is used and `Format.encoding` is ignored for the text
 * (it still names the encoding in the XML declaration). Only a value whose static type is `OutputStream` reaches the
 * extension, which encodes with `Format.encoding` as jdom2 did.
 */
class OutputStreamBindingTest {
    private val doc = Document(Element("r").setText("é")) // e acute: 0xE9 in ISO-8859-1, 0xC3 0xA9 in UTF-8

    private fun outputter(encoding: String) = XMLOutputter(Format.getRawFormat().setEncoding(encoding))

    private fun textBytes(all: ByteArray): ByteArray {
        // "<r>" start tag, then the text, then "</r>"
        val s = String(all, Charsets.ISO_8859_1)
        val from = s.indexOf("<r>") + 3
        val to = s.indexOf("</r>")
        return all.copyOfRange(from, to)
    }

    @Test fun printStreamBindsToAppendableAndIgnoresFormatEncoding() {
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos, true, "UTF-8")
        outputter("ISO-8859-1").output(doc, ps) // member output(Document, Appendable)
        ps.flush()
        // the PrintStream's UTF-8 encoded the text, although the format says ISO-8859-1
        assertContentEquals(byteArrayOf(0xC3.toByte(), 0xA9.toByte()), textBytes(baos.toByteArray()))
        assertTrue(String(baos.toByteArray(), Charsets.UTF_8).startsWith("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"))
    }

    @Test fun printStreamBindsToAppendableWithItsOwnEncodingWhateverTheFormat() {
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos, true, "ISO-8859-1")
        outputter("UTF-8").output(doc, ps) // member output(Document, Appendable)
        ps.flush()
        assertContentEquals(byteArrayOf(0xE9.toByte()), textBytes(baos.toByteArray()))
    }

    @Test fun outputStreamTypedArgumentUsesFormatEncoding() {
        // a raw ByteArrayOutputStream is not an Appendable: the extension output(Document, OutputStream)
        val raw = ByteArrayOutputStream()
        outputter("ISO-8859-1").output(doc, raw)
        assertContentEquals(byteArrayOf(0xE9.toByte()), textBytes(raw.toByteArray()))

        // the same PrintStream, but typed as OutputStream: the extension again, so Format.encoding wins over the stream's
        val baos = ByteArrayOutputStream()
        val asStream: OutputStream = PrintStream(baos, true, "ISO-8859-1")
        outputter("UTF-8").output(doc, asStream)
        asStream.flush()
        assertContentEquals(byteArrayOf(0xC3.toByte(), 0xA9.toByte()), textBytes(baos.toByteArray()))
    }
}
