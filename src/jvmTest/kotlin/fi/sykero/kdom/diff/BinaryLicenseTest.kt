package fi.sykero.kdom.diff

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/** Check the resources shipped by jvmProcessResources, not a shortened attribution string. */
class BinaryLicenseTest {
    @Test fun fullLicenseAndNoticeAreClasspathResources() {
        for (name in listOf("LICENSE", "NOTICE")) {
            val stream = assertNotNull(javaClass.getResourceAsStream("/META-INF/kdom/$name"), name)
            assertEquals(File(name).readText(), stream.bufferedReader().use { it.readText() }, name)
        }
    }
}
