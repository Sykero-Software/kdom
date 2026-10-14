package fi.sykero.kdom.input

import fi.sykero.kdom.probe.ProbeInputs
import fi.sykero.kdom.sax.Attributes
import fi.sykero.kdom.sax.ContentHandler
import fi.sykero.kdom.sax.Locator
import fi.sykero.kdom.sax.SAXParseException
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Every probe of [ProbeInputs] through [XmlEventParser] with a no-op handler. The expected verdicts are the JAXP SAX
 * verdicts pinned by `SaxReferenceProbeTest` (namespace-aware and namespace-unaware), except the two DOCTYPE probes,
 * which kdom rejects in both modes by plan rule P7 (SAX accepts them). Every rejection must be a [SAXParseException]
 * (requirement P3: no xmlutil or other exception may leak).
 */
class XmlEventParserProbeTest {
    @Test fun namespaceAwareVerdicts() {
        assertEquals(expected(nsAware = true), ProbeInputs.probes.mapValues { verdict(it.value, true) })
    }

    @Test fun namespaceUnawareVerdicts() {
        assertEquals(expected(nsAware = false), ProbeInputs.probes.mapValues { verdict(it.value, false) })
    }

    @Test fun validControlParsesInBothModes() {
        val xml = ProbeInputs.probes.getValue("valid control")
        XmlEventParser(namespaceAware = true).parse(xml, NoOpHandler)
        XmlEventParser(namespaceAware = false).parse(xml, NoOpHandler)
    }

    @Test fun byteInputGivesTheSameVerdicts() {
        assertEquals(expected(nsAware = true), ProbeInputs.probes.mapValues { verdict(it.value.encodeToByteArray(), true) })
    }

    private fun expected(nsAware: Boolean): Map<String, String> = ProbeInputs.probes.mapValues { (name, _) ->
        when (name) {
            "valid control" -> ACCEPTED
            "undeclared prefix" -> if (nsAware) REJECTED else ACCEPTED
            else -> REJECTED // includes the DOCTYPE probes (P7)
        }
    }

    private fun verdict(xml: String, nsAware: Boolean): String = try {
        XmlEventParser(namespaceAware = nsAware).parse(xml, NoOpHandler)
        ACCEPTED
    } catch (e: SAXParseException) {
        REJECTED
    } catch (e: Exception) {
        "leaked:" + e::class.simpleName
    }

    private fun verdict(xml: ByteArray, nsAware: Boolean): String = try {
        XmlEventParser(namespaceAware = nsAware).parse(xml, NoOpHandler)
        ACCEPTED
    } catch (e: SAXParseException) {
        REJECTED
    } catch (e: Exception) {
        "leaked:" + e::class.simpleName
    }

    private object NoOpHandler : ContentHandler {
        override fun setDocumentLocator(locator: Locator?) {}
        override fun startDocument() {}
        override fun endDocument() {}
        override fun startPrefixMapping(prefix: String, uri: String) {}
        override fun endPrefixMapping(prefix: String) {}
        override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {}
        override fun endElement(uri: String, localName: String, qName: String) {}
        override fun characters(ch: CharArray, start: Int, length: Int) {}
        override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {}
        override fun processingInstruction(target: String, data: String?) {}
        override fun skippedEntity(name: String) {}
    }

    private companion object {
        const val ACCEPTED = "accepted"
        const val REJECTED = "rejected:SAXParseException"
    }
}
