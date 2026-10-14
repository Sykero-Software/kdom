package fi.sykero.kdom.test.util

import fi.sykero.kdom.output.XMLOutputter

/** Kotlin/Native (iOS, linuxX64) has no `OutputStream` form of the outputter. */
actual fun outputViaStream(outputter: XMLOutputter, methodprefix: String, content: Any): String? = null
