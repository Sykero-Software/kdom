package fi.sykero.kdom.input

import java.io.ByteArrayInputStream
import java.io.File
import java.io.StringReader
import org.junit.Assert.assertEquals
import org.junit.Test

/** The JVM `Reader` / `InputStream` / `File` overloads of [KdomBuilder.build]. */
class KdomBuilderJvmTest {
    @Test fun readerInputStreamAndFile() {
        assertEquals("\u00E9", KdomBuilder().build(StringReader("<a>\u00E9</a>")).rootElement.text)
        val latin1 = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>".toByteArray() + byteArrayOf(0xE9.toByte()) + "</a>".toByteArray()
        assertEquals("\u00E9", KdomBuilder().build(ByteArrayInputStream(latin1)).rootElement.text)
        val f = File.createTempFile("kdom", ".xml")
        try {
            f.writeBytes(latin1)
            assertEquals("\u00E9", KdomBuilder().build(f).rootElement.text)
        } finally {
            f.delete()
        }
    }
}
