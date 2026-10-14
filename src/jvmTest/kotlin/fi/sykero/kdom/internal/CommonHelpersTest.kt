package fi.sykero.kdom.internal

import fi.sykero.kdom.filter.Filters
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The common replacements of `Character.isWhitespace`, `String.format("%0nx")` and `java.lang.Class` filters. */
class CommonHelpersTest {
    @Test fun isJavaWhitespaceMatchesCharacterIsWhitespace() {
        for (c in 0..0xFFFF) {
            assertEquals(Character.isWhitespace(c.toChar()), isJavaWhitespace(c.toChar()), "U+" + c.toString(16))
        }
    }

    @Test fun hexMatchesStringFormat() {
        for (v in listOf(0, 9, 0x1f, 0xd800, 0xffff, 0x10000, 0x10ffff)) {
            assertEquals(String.format("%04x", v), hex(v, 4))
            assertEquals(String.format("%06x", v), hex(v, 6))
        }
    }

    @Test fun boxedClassFilters() {
        assertTrue(Filters.fdouble().matches(1.5))
        assertFalse(Filters.fdouble().matches(1))
        assertTrue(Filters.fboolean().matches(true))
        assertTrue(Filters.fclass(Int::class).matches(123))
        assertEquals("[ClassFilter: Class java.lang.Double]", Filters.fdouble().toString())
        assertEquals("[ClassFilter: Class fi.sykero.kdom.Element]", Filters.element().toString())
    }
}
