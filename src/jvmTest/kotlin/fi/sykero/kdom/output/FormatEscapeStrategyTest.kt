package fi.sykero.kdom.output

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * kdom chooses the default [EscapeStrategy] from the encoding name alone (no `java.nio.charset` in common code).
 * For the names listed in translation-notes.md the answers equal jdom2's (which asked a `CharsetEncoder`);
 * every other name escapes all non-ASCII characters, where jdom2 asked the JDK charset.
 */
class FormatEscapeStrategyTest {
    private val probes = charArrayOf('A', '<', '\u007f', '\u0080', 'é', 'ÿ', 'Ā', '€', '一', '\ud800', '�')

    private fun kdom(encoding: String, ch: Char) = Format.getRawFormat().setEncoding(encoding).escapeStrategy.shouldEscape(ch)
    private fun jdom2(encoding: String, ch: Char) =
        org.jdom2.output.Format.getRawFormat().setEncoding(encoding).escapeStrategy.shouldEscape(ch)

    @Test fun mappedNamesAgreeWithJdom2() {
        val names = listOf(
            "UTF-8", "utf-8", "UTF8", "UTF-16", "UTF-16BE", "UTF-16LE", "UTF-32", "UTF-32BE", "UTF-32LE",
            "ISO-8859-1", "iso-8859-1", "Latin1", "LATIN1", "ISO8859_1", "US-ASCII", "ASCII", "ascii",
        )
        for (name in names) for (ch in probes) {
            assertEquals(jdom2(name, ch), kdom(name, ch), "encoding $name, char U+${ch.code.toString(16)}")
        }
    }

    /** Behaviour difference: names outside the mapped list escape every non-ASCII character. */
    @Test fun otherNamesEscapeNonAscii() {
        for (name in listOf("windows-1252", "Shift_JIS", "x-kdom-unknown")) {
            for (ch in probes) assertEquals(ch.code > 0x7f, kdom(name, ch), "encoding $name, char U+${ch.code.toString(16)}")
        }
        // jdom2 asked the JDK: windows-1252 encodes U+00E9 and U+20AC, an unknown name escapes only high surrogates.
        assertFalse(jdom2("windows-1252", 'é'))
        assertFalse(jdom2("windows-1252", '€'))
        assertFalse(jdom2("x-kdom-unknown", '一'))
        assertTrue(jdom2("x-kdom-unknown", '\ud800'))
    }
}
