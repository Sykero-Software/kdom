package fi.sykero.kdom.probe

import java.io.StringReader
import javax.xml.parsers.SAXParserFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/** JAXP SAX reference verdicts for the same probes as [XmlUtilReaderProbeTest] (the bar XmlEventParser must meet). */
class SaxReferenceProbeTest {
    private val probes = ProbeInputs.probes

    private fun verdict(xml: String, nsAware: Boolean): String = try {
        val f = SAXParserFactory.newInstance().apply { isNamespaceAware = nsAware }
        f.newSAXParser().parse(InputSource(StringReader(xml)), DefaultHandler())
        "accepted"
    } catch (e: Exception) {
        "rejected:" + e::class.simpleName
    }

    @Test fun saxVerdicts() {
        val ns = probes.mapValues { verdict(it.value, true) }
        val noNs = probes.mapValues { verdict(it.value, false) }
        assertEquals(EXPECTED_NS, ns)
        assertEquals(EXPECTED_NO_NS, noNs)
    }

    private companion object {
        val EXPECTED_NS: Map<String, String> = mapOf(
            "unclosed root" to "rejected:SAXParseException",
            "stray ampersand" to "rejected:SAXParseException",
            "undefined entity" to "rejected:SAXParseException",
            "unquoted attribute" to "rejected:SAXParseException",
            "unterminated attribute value" to "rejected:SAXParseException",
            "mis-nested tags" to "rejected:SAXParseException",
            "stray end tag" to "rejected:SAXParseException",
            "text after root" to "rejected:SAXParseException",
            "two roots" to "rejected:SAXParseException",
            "duplicate attribute" to "rejected:SAXParseException",
            "less-than in text" to "rejected:SAXParseException",
            "empty input" to "rejected:SAXParseException",
            "undeclared prefix" to "rejected:SAXParseException",
            "valid control" to "accepted",
            "undefined entity in attribute" to "rejected:SAXParseException",
            "illegal character" to "rejected:SAXParseException",
            "less-than in attribute" to "rejected:SAXParseException",
            "doctype with entity decl" to "accepted",
            "plain doctype" to "accepted",
            "out-of-range character reference" to "rejected:SAXParseException",
        )
        val EXPECTED_NO_NS: Map<String, String> = mapOf(
            "unclosed root" to "rejected:SAXParseException",
            "stray ampersand" to "rejected:SAXParseException",
            "undefined entity" to "rejected:SAXParseException",
            "unquoted attribute" to "rejected:SAXParseException",
            "unterminated attribute value" to "rejected:SAXParseException",
            "mis-nested tags" to "rejected:SAXParseException",
            "stray end tag" to "rejected:SAXParseException",
            "text after root" to "rejected:SAXParseException",
            "two roots" to "rejected:SAXParseException",
            "duplicate attribute" to "rejected:SAXParseException",
            "less-than in text" to "rejected:SAXParseException",
            "empty input" to "rejected:SAXParseException",
            "undeclared prefix" to "accepted",
            "valid control" to "accepted",
            "undefined entity in attribute" to "rejected:SAXParseException",
            "illegal character" to "rejected:SAXParseException",
            "less-than in attribute" to "rejected:SAXParseException",
            "doctype with entity decl" to "accepted",
            "plain doctype" to "accepted",
            "out-of-range character reference" to "rejected:SAXParseException",
        )
    }
}
