package fi.sykero.kdom.output

import kotlin.test.Test
import kotlin.test.assertEquals

/** kdom has no system-property override for the default line separator: DEFAULT is jdom2's hard default CRNL. */
class LineSeparatorTest {
    @Test fun defaultIsCrnl() {
        assertEquals("\r\n", LineSeparator.DEFAULT.value())
        assertEquals("\r\n", Format.getRawFormat().lineSeparator)
    }

    @Test fun systemIsThePlatformSeparator() {
        assertEquals(System.lineSeparator(), LineSeparator.SYSTEM.value())
    }
}
