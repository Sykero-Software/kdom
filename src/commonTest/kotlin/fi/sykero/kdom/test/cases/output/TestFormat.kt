/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.output

import kotlin.jvm.JvmStatic
import kotlin.test.*
import fi.sykero.kdom.test.util.nullT
import fi.sykero.kdom.output.EscapeStrategy
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.Format.TextMode

class TestFormat {

	companion object {
	@JvmStatic fun checkEquals(a: Format, b: Format) {
		assertEquals(a.encoding, b.encoding, "Expect formatters to have the same Encoding")
		//assertEquals("Expect formatters to have the same EscapeStrategy", 
		//		a.getEscapeStrategy(), b.getEscapeStrategy());
		assertEquals(a.expandEmptyElements, b.expandEmptyElements, "Expect formatters to have the same ExpandEmptyElements")
		assertEquals(a.ignoreTrAXEscapingPIs, b.ignoreTrAXEscapingPIs, "Expect formatters to have the same TrAXEscapingPIs")
		assertEquals(a.indent, b.indent, "Expect formatters to have the same Indent")
		assertEquals(a.lineSeparator, b.lineSeparator, "Expect formatters to have the same LineSeparator")
		assertEquals(a.omitDeclaration, b.omitDeclaration, "Expect formatters to have the same OmitDeclaration")
		assertEquals(a.omitEncoding, b.omitEncoding, "Expect formatters to have the same OmitEncoding")
		assertEquals(a.textMode, b.textMode, "Expect formatters to have the same TextMode")
		
	}
	}

	@Test fun testGetRawFormat() {
		val mine = Format.getRawFormat()
		assertTrue(mine.textMode === TextMode.PRESERVE)
		assertEquals(mine.encoding, "UTF-8")		
	}

	@Test fun testGetPrettyFormat() {
		val mine = Format.getPrettyFormat()
		assertTrue(mine.textMode === TextMode.TRIM)
		assertEquals(mine.encoding, "UTF-8")
	}

	@Test fun testGetCompactFormat() {
		val mine = Format.getCompactFormat()
		assertTrue(mine.textMode === TextMode.NORMALIZE)
		assertEquals(mine.encoding, "UTF-8")		
	}

	@Test fun testEscapeStrategy() {
		val es = object : EscapeStrategy {
			override fun shouldEscape(ch: Char): Boolean {
				return false
			}
		}
		val mine = Format.getRawFormat()
		assertTrue(mine.escapeStrategy !== es)
		mine.setEscapeStrategy(es)
		assertTrue(mine.escapeStrategy === es)
	}

	@Test fun testLineSeparator() {
		assertEquals("\r\n", Format.getPrettyFormat().lineSeparator)
		assertEquals("\r\n", Format.getCompactFormat().lineSeparator)
		
		val mine = Format.getRawFormat()
		assertEquals("\r\n", mine.lineSeparator)
		mine.setLineSeparator("  \n  ")
		assertEquals("  \n  ", mine.lineSeparator)
	}

	@Test fun testOmitEncoding() {
		assertFalse(Format.getPrettyFormat().omitEncoding)
		assertFalse(Format.getCompactFormat().omitEncoding)
		val mine = Format.getRawFormat()
		assertFalse(mine.omitEncoding)
		mine.setOmitEncoding(true)
		assertTrue (mine.omitEncoding)
	}

	@Test fun testOmitDeclaration() {
		assertFalse(Format.getPrettyFormat().omitDeclaration)
		assertFalse(Format.getCompactFormat().omitDeclaration)
		val mine = Format.getRawFormat()
		assertFalse(mine.omitDeclaration)
		mine.setOmitDeclaration(true)
		assertTrue (mine.omitDeclaration)
	}

	@Test fun testSpecifiedAttributesOnly() {
		assertFalse(Format.getPrettyFormat().isSpecifiedAttributesOnly)
		assertFalse(Format.getCompactFormat().isSpecifiedAttributesOnly)
		val mine = Format.getRawFormat()
		assertFalse(mine.isSpecifiedAttributesOnly)
		mine.isSpecifiedAttributesOnly = true
		assertTrue (mine.isSpecifiedAttributesOnly)
	}

	@Test fun testExpandEmptyElements() {
		assertFalse(Format.getPrettyFormat().expandEmptyElements)
		assertFalse(Format.getCompactFormat().expandEmptyElements)
		val mine = Format.getRawFormat()
		assertFalse(mine.expandEmptyElements)
		mine.setExpandEmptyElements(true)
		assertTrue (mine.expandEmptyElements)
	}

	@Test fun testIgnoreTrAXEscapingPIs() {
		assertFalse(Format.getPrettyFormat().ignoreTrAXEscapingPIs)
		assertFalse(Format.getCompactFormat().ignoreTrAXEscapingPIs)
		val mine = Format.getRawFormat()
		assertFalse(mine.ignoreTrAXEscapingPIs)
		mine.ignoreTrAXEscapingPIs = true
		assertTrue (mine.ignoreTrAXEscapingPIs)
	}

	@Test fun testTextMode() {
		assertEquals(TextMode.TRIM, Format.getPrettyFormat().textMode)
		assertEquals(TextMode.NORMALIZE, Format.getCompactFormat().textMode)
		
		val mine = Format.getRawFormat()
		assertEquals(TextMode.PRESERVE, mine.textMode)
		mine.setTextMode(TextMode.TRIM_FULL_WHITE)
		assertEquals(TextMode.TRIM_FULL_WHITE, mine.textMode)
		
		assertEquals("TRIM", TextMode.TRIM.toString())
	}

	@Test fun testIndent() {
		assertEquals("  ", Format.getPrettyFormat().indent)
		assertNull(Format.getCompactFormat().indent)
		
		val mine = Format.getRawFormat()
		assertNull(mine.indent)
		mine.setIndent("    ")
		assertEquals("    ", mine.indent)
	}

	@Test fun testEncoding() {
		assertEquals("UTF-8", Format.getPrettyFormat().encoding)
		assertEquals("UTF-8", Format.getCompactFormat().encoding)
		
		val mine = Format.getRawFormat()
		assertEquals("UTF-8", mine.encoding)
		mine.setEncoding("US-ASCII")
		assertEquals("US-ASCII", mine.encoding)
	}

	@Test fun testClone() {
		val mine = Format.getRawFormat()
		val clone = mine.clone()
		assertFalse(mine === clone)
		checkEquals(mine, clone)
	}
	
	@Test fun test7BitEscapes() {
		checkBitEscape("US-ASCII", 
				charArrayOf('a', 'b', '\n', '!'),
				charArrayOf(128.toChar(), 255.toChar(), 1234.toChar()))
	}
	
	@Test fun testASCIIBitEscapes() {
		// ISO646-US is another name for US-ASCII
		// JDOM does not know that, but Java does.
		checkBitEscape("ISO646-US", 
				charArrayOf('a', 'b', '\n', '!'),
				charArrayOf(128.toChar(), 255.toChar(), 1234.toChar()))
	}
	
	@Test fun test8BitEscapes() {
		checkBitEscape("Latin1", 
				charArrayOf('a', 'b', '\n', '!', 128.toChar(), 255.toChar()),
				charArrayOf(1234.toChar()))
	}
	
	@Test fun test16BitEscapes() {
		checkBitEscape("UTF-16", 
				charArrayOf('a', 'b', '\n', '!', 128.toChar(), 255.toChar(), 1234.toChar()),
				charArrayOf(0xD800.toChar()))
	}
	
	@Test fun testCharsetEncodingEscapes() {
		// K5: kdom has no java.nio.charset; an encoding name outside its mapped list escapes every
		// non-ASCII character (jdom2 kept (char)255 for windows-1252). See translation-notes.md.
		checkBitEscape("windows-1252",
				charArrayOf('a', 'b', '\n', '!', 127.toChar()),
				charArrayOf(0xD800.toChar(), 1234.toChar(), 128.toChar(), 255.toChar()))
	}
	
	@Test fun testIllegalEncodingEscapes() {
		// K5: an unknown encoding name escapes every non-ASCII character (jdom2 escaped only high
		// surrogates). See translation-notes.md.
		checkBitEscape("junk",
				charArrayOf('a', 'b', '\n', '!'),
				charArrayOf(0xD800.toChar(), 128.toChar(), 255.toChar(), 1234.toChar()))
	}
	
	private fun checkBitEscape(encoding: String, keep: CharArray, escape: CharArray) {
		val form = Format.getPrettyFormat()
		form.setEncoding(encoding)
		val es = form.escapeStrategy
		for (ch in keep) {
			assertFalse(es.shouldEscape(ch), "Should Not Escape " + ch)
		}
		for (ch in escape) {
			assertTrue(es.shouldEscape(ch), "Should Escape " + ch)
		}
	}
	
	private fun checkTrim(base: String, both: String, left: String, right: String, compact: String) {
		assertEquals(both,    Format.trimBoth(base))
		assertEquals(left,    Format.trimLeft(base))
		assertEquals(right,   Format.trimRight(base))
		assertEquals(compact, Format.compact(base))
	}
	
	@Test fun testTrimming() {
		checkTrim("", "", "", "", "")
		checkTrim(" ", "", "", "", "")
		checkTrim("x", "x", "x", "x", "x")
		checkTrim("foo", "foo", "foo", "foo", "foo")
		checkTrim(" \r\n  ", "", "", "", "")
		checkTrim(" \rx\n  ", "x", "x\n  ", " \rx", "x")
		checkTrim(" \rx \t y\n  ", "x \t y", "x \t y\n  ", " \rx \t y", "x y")
	}
	
	private fun checkEscapes(eol: String, base: String, txt: String, att: String) {
		val strategy = Format.getPrettyFormat().escapeStrategy
		assertEquals(txt, Format.escapeText(strategy, eol, base))
		assertEquals(att, Format.escapeAttribute(strategy, base))
	}
	
	@Test fun testEscapeText() {
		checkEscapes(nullT(), "", "", "")
		checkEscapes(nullT(), " \n ", " \n ", " &#xA; ")
		checkEscapes("\r\n", " \n ", " \r\n ", " &#xA; ")
		checkEscapes(nullT(), " \" \n ", " \" \n ", " &quot; &#xA; ")
		checkEscapes("\r\n", " \" \n ", " \" \r\n ", " &quot; &#xA; ")
	}

}
