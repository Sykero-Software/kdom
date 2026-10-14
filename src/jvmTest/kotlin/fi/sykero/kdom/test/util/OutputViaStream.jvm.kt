package fi.sykero.kdom.test.util

import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.output.output
import fi.sykero.kdom.output.outputElementContent
import java.io.ByteArrayOutputStream

/** JVM: the `OutputStream` extension functions of `XMLOutputterJvm.kt`, decoded with the default charset. */
actual fun outputViaStream(outputter: XMLOutputter, methodprefix: String, content: Any): String? {
    val baos = ByteArrayOutputStream()
    if (methodprefix == "outputElementContent") {
        outputter.outputElementContent(content as Element, baos)
    } else {
        @Suppress("UNCHECKED_CAST")
        when (content) {
            is Document -> outputter.output(content, baos)
            is Element -> outputter.output(content, baos)
            is EntityRef -> outputter.output(content, baos)
            is CDATA -> outputter.output(content, baos)
            is Comment -> outputter.output(content, baos)
            is DocType -> outputter.output(content, baos)
            is ProcessingInstruction -> outputter.output(content, baos)
            is Text -> outputter.output(content, baos)
            is List<*> -> outputter.output(content as List<Content>, baos)
            else -> throw IllegalStateException("Unable to find an OutputStream overload for " + content::class)
        }
    }
    return baos.toString()
}
