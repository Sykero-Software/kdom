package fi.sykero.kdom.output

import java.net.URLClassLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/** kdom has no system-property override for the default line separator: DEFAULT is jdom2's hard default CRNL. */
class LineSeparatorTest {
    @Test fun defaultIsCrnl() {
        assertEquals("\r\n", LineSeparator.DEFAULT.value())
        assertEquals("\r\n", Format.getRawFormat().lineSeparator)
    }

    /**
     * jdom2 read `-Dorg.jdom2.output.LineSeparator=...` when the class was initialised; kdom must ignore it (the property
     * name was also renamed with the package, so both spellings are set).
     */
    @Test fun defaultIgnoresTheJdom2SystemProperty() {
        val names = listOf("org.jdom2.output.LineSeparator", "fi.sykero.kdom.output.LineSeparator")
        val saved = names.associateWith { System.getProperty(it) }
        try {
            names.forEach { System.setProperty(it, "\n") }
            // the class of this JVM is already initialised, so also load a fresh copy of it that reads the properties
            // as it initialises
            val fresh = freshLineSeparatorClass()
            assertNotSame(LineSeparator::class.java, fresh)
            val default = fresh.getField("DEFAULT").get(null)
            assertEquals("\r\n", fresh.getMethod("value").invoke(default))
            assertEquals("\r\n", LineSeparator.DEFAULT.value())
            assertEquals("\r\n", Format.getRawFormat().lineSeparator)
        } finally {
            saved.forEach { (name, value) ->
                if (value == null) System.clearProperty(name) else System.setProperty(name, value)
            }
        }
    }

    /** `LineSeparator` loaded by a class loader of its own (kdom main classes and the Kotlin stdlib only). */
    private fun freshLineSeparatorClass(): Class<*> {
        val urls = listOf(LineSeparator::class.java, Unit::class.java)
            .map { it.protectionDomain.codeSource.location }
        val loader = URLClassLoader(urls.toTypedArray(), ClassLoader.getPlatformClassLoader())
        return Class.forName("fi.sykero.kdom.output.LineSeparator", true, loader)
    }

    @Test fun systemIsThePlatformSeparator() {
        assertEquals(System.lineSeparator(), LineSeparator.SYSTEM.value())
    }
}
