package fi.sykero.kdom.diff

import fi.sykero.kdom.input.KdomBuilder
import java.io.StringReader
import org.jdom2.input.SAXBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

/**
 * The tree [KdomBuilder] builds against jdom2 2.0.6.1's `SAXBuilder` (JDK SAX parser, defaults) for every accepted input
 * of [SaxEventDifferentialTest] plus tree-shape cases: a structural dump (node kinds, names, namespaces, additional
 * namespaces, attributes with type/specified, text values) must be identical, and so must the raw serialization.
 */
class KdomBuilderDifferentialTest {

    @Test fun treesMatchSaxBuilder() {
        val mismatches = ArrayList<String>()
        for (xml in SaxEventDifferentialTest.ACCEPTED + TREE_CASES) {
            val expected = jdomDump(SAXBuilder().build(StringReader(xml)))
            val actual = try { kdomDump(KdomBuilder().build(xml)) } catch (e: Exception) { "threw $e" }
            if (expected != actual) mismatches += "$xml\n  jdom2: $expected\n  kdom : $actual"
            val expectedRaw = org.jdom2.output.XMLOutputter(org.jdom2.output.Format.getRawFormat()).outputString(SAXBuilder().build(StringReader(xml)))
            val actualRaw = try { fi.sykero.kdom.output.XMLOutputter(fi.sykero.kdom.output.Format.getRawFormat()).outputString(KdomBuilder().build(xml)) } catch (e: Exception) { "threw $e" }
            if (expectedRaw != actualRaw) mismatches += "raw $xml\n  jdom2: $expectedRaw\n  kdom : $actualRaw"
        }
        if (mismatches.isNotEmpty()) fail(mismatches.size.toString() + " mismatches:\n" + mismatches.joinToString("\n"))
    }

    /**
     * Inputs the JDK parser accepts but jdom2's tree layer rejects with an unchecked exception: kdom throws a
     * JDOMParseException whose cause is the same kind of exception (fix round 1: never bypass `catch (JDOMException)`).
     */
    @Test fun treeLayerRejectionsAreWrapped() {
        for (xml in SaxEventDifferentialTest.ACCEPTED_SAX_ONLY) {
            val jdom = runCatching { SAXBuilder().build(StringReader(xml)) }.exceptionOrNull()
            val kdom = runCatching { KdomBuilder().build(xml) }.exceptionOrNull()
            assertEquals("kdom for $xml", fi.sykero.kdom.input.JDOMParseException::class.java, kdom?.javaClass)
            assertEquals("cause for $xml", jdom?.javaClass?.simpleName, kdom?.cause?.javaClass?.simpleName)
        }
    }

    @Test fun rejectionsMatchSaxBuilder() {
        for (xml in SaxEventDifferentialTest.REJECTED) {
            val jdom = runCatching { SAXBuilder().build(StringReader(xml)) }.exceptionOrNull()
            val kdom = runCatching { KdomBuilder().build(xml) }.exceptionOrNull()
            assertEquals("jdom2 for $xml", org.jdom2.input.JDOMParseException::class.java, jdom?.javaClass)
            assertEquals("kdom for $xml", fi.sykero.kdom.input.JDOMParseException::class.java, kdom?.javaClass)
        }
    }

    private fun esc(s: String?) = s?.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }?.joinToString("")

    private fun jdomDump(d: org.jdom2.Document): String = d.content.joinToString(" ") { jdomNode(it) }

    private fun jdomNode(c: org.jdom2.Content): String = when (c) {
        is org.jdom2.Element -> "E(" + c.namespacePrefix + ":" + c.name + "{" + c.namespaceURI + "}" +
            " ns=" + c.additionalNamespaces.joinToString(",") { it.prefix + "=" + it.uri } +
            " atts=" + c.attributes.joinToString(",") { it.namespacePrefix + ":" + it.name + "{" + it.namespaceURI + "}=" + esc(it.value) + "/" + it.attributeType + "/" + it.isSpecified } +
            " [" + c.content.joinToString(" ") { jdomNode(it) } + "])"
        is org.jdom2.CDATA -> "CDATA(" + esc(c.text) + ")"
        is org.jdom2.Text -> "T(" + esc(c.text) + ")"
        is org.jdom2.Comment -> "C(" + esc(c.text) + ")"
        is org.jdom2.ProcessingInstruction -> "PI(" + c.target + "," + esc(c.data) + ")"
        else -> c.javaClass.simpleName
    }

    private fun kdomDump(d: fi.sykero.kdom.Document): String = d.content.joinToString(" ") { kdomNode(it) }

    private fun kdomNode(c: fi.sykero.kdom.Content): String = when (c) {
        is fi.sykero.kdom.Element -> "E(" + c.namespacePrefix + ":" + c.name + "{" + c.namespaceURI + "}" +
            " ns=" + c.additionalNamespaces.joinToString(",") { it.prefix + "=" + it.uri } +
            " atts=" + c.attributes.joinToString(",") { it.namespacePrefix + ":" + it.name + "{" + it.namespaceURI + "}=" + esc(it.value) + "/" + it.attributeType + "/" + it.isSpecified } +
            " [" + c.content.joinToString(" ") { kdomNode(it) } + "])"
        is fi.sykero.kdom.CDATA -> "CDATA(" + esc(c.text) + ")"
        is fi.sykero.kdom.Text -> "T(" + esc(c.text) + ")"
        is fi.sykero.kdom.Comment -> "C(" + esc(c.text) + ")"
        is fi.sykero.kdom.ProcessingInstruction -> "PI(" + c.target + "," + esc(c.data) + ")"
        else -> c::class.simpleName ?: "?"
    }

    private companion object {
        val TREE_CASES = listOf(
            "<a>x<![CDATA[<c>]]>y<![CDATA[]]><![CDATA[d]]></a>",
            "<a><![CDATA[]]></a>",
            "<a>t<![CDATA[]]>u</a>",
            "<a><![CDATA[c]]><![CDATA[d]]>e<!--x--><![CDATA[]]></a>",
            "<!--c1--><?p d?>\n<a>t<!--c2--><?q?>u</a>\n<!--c3--><?r  x  y?>",
            "<a><!----></a>",
            "<p:a xmlns:p=\"urn:p\" xmlns=\"urn:d\" xmlns:q=\"urn:q\" q:x=\"1\" y=\"2\"><b/><p:c xmlns:p=\"urn:p\"/></p:a>",
            "<a xmlns=\"urn:d\"><b xmlns=\"urn:d\"/></a>",
            "<a z=\"1\" a=\"2\" m=\"3\"/>",
            "<a xml:lang=\"fi\" xml:space=\"preserve\"> </a>",
            "<a> <b/>\n\t</a>",
        )
    }
}
