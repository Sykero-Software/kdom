package fi.sykero.kdom.test.util

import fi.sykero.kdom.output.XMLOutputter

/** iOS has no `OutputStream` form of the outputter. */
actual fun outputViaStream(outputter: XMLOutputter, methodprefix: String, content: Any): String? = null
