package fi.sykero.kdom

import kotlin.test.Test
import kotlin.test.assertEquals

class KdomVersionTest {
    @Test
    fun versionIsSnapshot() {
        assertEquals("0.1.0-SNAPSHOT", KdomVersion.VERSION)
    }
}
