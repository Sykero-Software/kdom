package fi.sykero.kdom.probe

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins what xmlutil 1.0.2.1's generic reader (`xmlStreaming.newGenericReader(String)`, the KtXmlReader-backed
 * common implementation) does with each probe. A JAXP SAX parser rejects all 13 malformed probes; see
 * docs/xmlutil-probes.md for the comparison and the resulting XmlEventParser requirements.
 */
class XmlUtilReaderProbeTest {
    private val probes = linkedMapOf(
        "unclosed root" to "<a><b></b>",
        "stray ampersand" to "<a>x & y</a>",
        "undefined entity" to "<a>&nbsp;</a>",
        "unquoted attribute" to "<a b=1/>",
        "unterminated attribute value" to "<a b=\"1/>",
        "mis-nested tags" to "<a><b><c></b></c></a>",
        "stray end tag" to "<a></b></a>",
        "text after root" to "<a/>tail",
        "two roots" to "<a/><b/>",
        "duplicate attribute" to "<a b=\"1\" b=\"2\"/>",
        "less-than in text" to "<a>1 < 2</a>",
        "empty input" to "",
        "undeclared prefix" to "<p:a/>",
        "valid control" to "<a x=\"&lt;&amp;&#233;\"><![CDATA[<raw>]]>t&gt;</a>",
    )

    @Test fun recordBehaviour() {
        val observed = probes.mapValues { (_, xml) -> readAll(xml) }
        assertEquals(EXPECTED, observed)
    }

    private fun readAll(xml: String): String = try {
        val r = nl.adaptivity.xmlutil.xmlStreaming.newGenericReader(xml)
        while (r.hasNext()) r.next()
        "accepted"
    } catch (e: Exception) {
        "rejected:" + (e::class.simpleName ?: "?")
    }

    private companion object {
        val EXPECTED: Map<String, String> = mapOf(
            "unclosed root" to "rejected:XmlException",
            "stray ampersand" to "rejected:XmlException",
            "undefined entity" to "accepted",
            "unquoted attribute" to "rejected:XmlException",
            "unterminated attribute value" to "rejected:IllegalStateException",
            "mis-nested tags" to "rejected:XmlException",
            "stray end tag" to "rejected:XmlException",
            "text after root" to "rejected:IllegalStateException",
            "two roots" to "rejected:XmlException",
            "duplicate attribute" to "accepted",
            "less-than in text" to "rejected:XmlException",
            "empty input" to "rejected:XmlException",
            "undeclared prefix" to "rejected:XmlException",
            "valid control" to "accepted",
        )
    }
}
