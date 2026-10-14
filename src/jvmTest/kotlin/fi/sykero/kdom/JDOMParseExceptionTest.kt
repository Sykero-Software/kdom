package fi.sykero.kdom

import fi.sykero.kdom.input.JDOMParseException
import org.xml.sax.SAXParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** Pins the kdom re-shape of JDOMParseException: the location is never derived from the cause. */
class JDOMParseExceptionTest {

    private val sax = SAXParseException("bad", "pub", "sys", 7, 9)

    @Test
    fun jdom2StyleConstructorsIgnoreSaxCauseLocation() {
        val e1 = JDOMParseException("msg", sax)
        val e2 = JDOMParseException("msg", sax, null)
        for (e in listOf(e1, e2)) {
            assertEquals(-1, e.lineNumber)
            assertEquals(-1, e.columnNumber)
            assertNull(e.publicId)
            assertNull(e.systemId)
            assertEquals("msg", e.message)
            assertSame(sax, e.cause)
        }
    }

    @Test
    fun messageOnlyConstructor() {
        val e = JDOMParseException("only")
        assertEquals("only", e.message)
        assertNull(e.cause)
        assertEquals(-1, e.lineNumber)
        assertEquals(-1, e.columnNumber)
        assertNull(e.partialDocument)
    }

    @Test
    fun explicitLocationConstructor() {
        val cause = RuntimeException("x")
        val e = JDOMParseException("m", cause, 12, 34)
        assertEquals(12, e.lineNumber)
        assertEquals(34, e.columnNumber)
        assertNull(e.publicId)
        assertNull(e.systemId)
        assertEquals("m", e.message)
        assertSame(cause, e.cause)
    }

    @Test
    fun fullConstructorCarriesIds() {
        val doc = Document()
        val e = JDOMParseException("m", null, doc, "p", "s", 1, 2)
        assertSame(doc, e.partialDocument)
        assertEquals("p", e.publicId)
        assertEquals("s", e.systemId)
        assertEquals(1, e.lineNumber)
        assertEquals(2, e.columnNumber)
    }
}
