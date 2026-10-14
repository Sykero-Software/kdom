package fi.sykero.kdom.probe

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Pins what xmlutil 1.0.2.1's generic reader (`xmlStreaming.newGenericReader(String)`, the KtXmlReader-backed
 * common implementation) does with each probe in [ProbeInputs]. A JAXP SAX parser rejects every malformed probe
 * except where docs/xmlutil-probes.md says otherwise. Only exception class names are pinned, never messages.
 */
class XmlUtilReaderProbeTest {
    @Test fun recordBehaviour() {
        val observed = ProbeInputs.probes.mapValues { (_, xml) -> readAll(xml) }
        assertEquals(EXPECTED, observed)
    }

    /**
     * The exact event stream: `EVENT`, `EVENT(local[attr='v'])` or `EVENT('text')`; non-ASCII and control
     * characters are written as backslash-uXXXX. Shows that every entity reference, predefined or numeric, is an
     * ENTITY_REF event carrying the resolved text, and that an undefined entity is an ENTITY_REF with empty text.
     */
    @Test fun eventSequences() {
        val inputs = linkedMapOf(
            "valid control" to ProbeInputs.probes.getValue("valid control"),
            "content refs" to "<a>&lt;&amp;&#233;&#x41;&apos;&quot;&gt;x</a>",
            "undefined entity between text" to "<a>p&nbsp;q</a>",
            "undefined entity in attribute" to "<a x=\"&nbsp;\"/>",
            "doctype entity use" to "<!DOCTYPE a [<!ENTITY e \"v\">]><a>&e;</a>",
            "illegal character" to "<a>\u0001</a>",
        )
        val expected = linkedMapOf(
            "valid control" to "START_DOCUMENT START_ELEMENT(a[x='<&\\u00e9']) CDSECT('<raw>') TEXT('t') ENTITY_REF('>') END_ELEMENT(a) END_DOCUMENT",
            "content refs" to "START_DOCUMENT START_ELEMENT(a[]) ENTITY_REF('<') ENTITY_REF('&') ENTITY_REF('\\u00e9') ENTITY_REF('A') ENTITY_REF(''') ENTITY_REF('\"') ENTITY_REF('>') TEXT('x') END_ELEMENT(a) END_DOCUMENT",
            "undefined entity between text" to "START_DOCUMENT START_ELEMENT(a[]) TEXT('p') ENTITY_REF('') TEXT('q') END_ELEMENT(a) END_DOCUMENT",
            "undefined entity in attribute" to "START_DOCUMENT rejected:XmlException",
            "doctype entity use" to "START_DOCUMENT DOCDECL START_ELEMENT(a[]) ENTITY_REF('v') END_ELEMENT(a) END_DOCUMENT",
            "illegal character" to "START_DOCUMENT START_ELEMENT(a[]) TEXT('\\u0001') END_ELEMENT(a) END_DOCUMENT",
        )
        assertEquals(expected, inputs.mapValues { (_, xml) -> events(xml) })
    }

    private fun esc(s: String) =
        s.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }
            .joinToString("")

    private fun events(xml: String): String {
        val out = ArrayList<String>()
        try {
            val r = nl.adaptivity.xmlutil.xmlStreaming.newGenericReader(xml)
            while (r.hasNext()) {
                val e = r.next()
                out += when (e.name) {
                    "START_ELEMENT" -> "START_ELEMENT(" + r.localName + (0 until r.attributeCount).joinToString(",", "[", "]") {
                        r.getAttributeLocalName(it) + "='" + esc(r.getAttributeValue(it)) + "'"
                    } + ")"
                    "END_ELEMENT" -> "END_ELEMENT(" + r.localName + ")"
                    "TEXT", "ENTITY_REF", "CDSECT" -> e.name + "('" + esc(r.text) + "')"
                    else -> e.name
                }
            }
        } catch (e: Exception) {
            out += "rejected:" + (e::class.simpleName ?: "?")
        }
        return out.joinToString(" ")
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
            "undefined entity in attribute" to "rejected:XmlException",
            "illegal character" to "accepted",
            "less-than in attribute" to "accepted",
            "doctype with entity decl" to "accepted",
            "plain doctype" to "accepted",
            "out-of-range character reference" to "accepted",
        )
    }
}
