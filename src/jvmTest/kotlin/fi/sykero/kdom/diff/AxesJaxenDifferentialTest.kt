package fi.sykero.kdom.diff

import fi.sykero.kdom.input.KdomBuilder
import fi.sykero.kdom.query.allElements
import fi.sykero.kdom.query.ancestorElements
import fi.sykero.kdom.query.attributeIs
import fi.sykero.kdom.query.childElements
import fi.sykero.kdom.query.descendantElements
import fi.sykero.kdom.query.descendantTexts
import fi.sykero.kdom.query.followingSiblingElements
import fi.sykero.kdom.query.hasAttribute
import fi.sykero.kdom.query.isNamed
import fi.sykero.kdom.query.precedingSiblingElements
import fi.sykero.kdom.query.xpathRoot
import java.io.StringReader
import org.jdom2.filter.Filters
import org.jdom2.input.SAXBuilder
import org.jdom2.xpath.XPathFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The query axes of `fi.sykero.kdom.query` against jaxen (via jdom2's XPathFactory) on the XPath expressions `:app`
 * evaluates today (EpubBackendState, EpubOptimization, SwordContentFacade). Every expression is evaluated from EVERY
 * node that can be a context (the document, every element) of every fixture, with the `ns` prefix bound to each of the
 * namespace URIs in play, on attached trees and on a tree whose `<body>` was detached. Results are compared in order,
 * by a path key computed on each tree. The axes compositions written here are the templates Task 15 copies into `:app`.
 */
class AxesJaxenDifferentialTest {

    private companion object {
        const val OPF = "http://www.idpf.org/2007/opf"
        const val DC = "http://purl.org/dc/elements/1.1/"
        const val XHTML = "http://www.w3.org/1999/xhtml"
        const val NCX = "http://www.daisy.org/z3986/2005/ncx/"
        const val CONTAINER = "urn:oasis:names:tc:opendocument:xmlns:container"
        const val SVG = "http://www.w3.org/2000/svg"
        const val XLINK = "http://www.w3.org/1999/xlink"

        val CONTAINER_XML = """<?xml version="1.0"?>
<container version="1.0" xmlns="$CONTAINER"><rootfiles><rootfile full-path="OEBPS/content.opf" media-type="application/oebps-package+xml"/></rootfiles></container>"""

        val OPF_XML = """<?xml version="1.0" encoding="UTF-8"?>
<package xmlns="$OPF" xmlns:dc="$DC" version="3.0" unique-identifier="bookid">
 <metadata><dc:title>Title</dc:title><dc:creator>A</dc:creator><dc:creator>B</dc:creator><dc:language>fi</dc:language></metadata>
 <manifest>
  <item id="c1" href="c1.xhtml" media-type="application/xhtml+xml"/>
  <item id="ncx" href="toc.ncx" media-type="application/x-dtbncx+xml"/>
  <item id="toc" href="toc.xhtml" media-type="application/xhtml+xml"/>
  <item id="navitem" href="nav.xhtml" properties="nav" media-type="application/xhtml+xml"/>
  <item id="c2" href="c%202.xhtml" media-type="application/xhtml+xml"/>
 </manifest>
 <spine toc="ncx"><itemref idref="c1"/><itemref idref="c2"/></spine>
</package>"""

        val NCX_XML = """<?xml version="1.0" encoding="UTF-8"?>
<ncx xmlns="$NCX" version="2005-1"><head><meta name="x" content="y"/></head><docTitle><text>T</text></docTitle>
 <navMap>
  <navPoint id="n1"><navLabel><text>One</text></navLabel><content src="c1.xhtml"/>
    <navPoint id="n1a"><navLabel><text>One A</text></navLabel><content src="c1.xhtml#a"/></navPoint>
    <navPoint id="n1b"><content src="c1.xhtml#b"/><navLabel><text>One B</text></navLabel></navPoint>
  </navPoint>
  <navPoint id="n2"><navLabel><text>Two</text></navLabel><content src="c%202.xhtml"/></navPoint>
 </navMap></ncx>"""

        val XHTML_XML = """<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="$XHTML" xmlns:svg="$SVG" xmlns:xlink="$XLINK" id="root">
<head><title>t</title></head>
<body id="b">
 <div id="d1"><p id="p1"><BVA ordinal="1">One</BVA> <BVA ordinal="2">two</BVA></p>
  <note id="n-x">xhtml note <BVA ordinal="3">three</BVA></note>
  <note xmlns="" id="n-none">plain note <b>bold</b></note>
  <p><a href="x.html">link <img src="i.png"/></a><BVA ordinal="4">four</BVA></p></div>
 <div><svg:svg><svg:image xlink:href="pic.png"/><svg:image href="no.png"/><image xlink:href="wrongns"/></svg:svg>
  <h1 id="h">Head<![CDATA[ cdata ]]><!-- c --></h1>
  <section><div><p><BVA ordinal="5">five</BVA></p></div><span><BVA ordinal="6">six</BVA></span><p>last</p></section>
  <ul><li><BVA ordinal="7">seven</BVA></li><li>plain</li></ul><p>after</p>
  <img src="j.png"/><a name="z"/></div>
</body></html>"""

        val NAMESPACES = listOf(OPF, XHTML, NCX, CONTAINER, DC, SVG, "urn:none")
    }

    // ---- path keys, computed on each tree ----------------------------------------------------------------------

    private fun key(n: org.jdom2.Content): String {
        val p = n.parent ?: return label(n)
        return (if (p is org.jdom2.Element) key(p) else "") + "/" + p.indexOf(n) + label(n)
    }
    private fun label(n: org.jdom2.Content) = when (n) {
        is org.jdom2.Element -> "<{" + n.namespaceURI + "}" + n.name + ">"
        is org.jdom2.Text -> "#text"
        else -> "?"
    }

    private fun key(n: fi.sykero.kdom.Content): String {
        val p = n.parent ?: return label(n)
        return (if (p is fi.sykero.kdom.Element) key(p) else "") + "/" + p.indexOf(n) + label(n)
    }
    private fun label(n: fi.sykero.kdom.Content) = when (n) {
        is fi.sykero.kdom.Element -> "<{" + n.namespaceURI + "}" + n.name + ">"
        is fi.sykero.kdom.Text -> "#text"
        else -> "?"
    }

    // ---- the expressions and their axes compositions ------------------------------------------------------------

    private class Case(
        val xpath: String,
        val text: Boolean = false,
        val elementContextOnly: Boolean = false,
        val axes: (ctx: fi.sykero.kdom.Parent, ns: fi.sykero.kdom.Namespace) -> List<fi.sykero.kdom.Content>,
    )

    /** Absolute all-elements step from this context: jaxen has no root for a detached tree, so nothing. */
    private fun fi.sykero.kdom.Parent.root(): Sequence<fi.sykero.kdom.Element> = xpathRoot()?.allElements() ?: emptySequence()

    private fun cases(): List<Case> {
        val none = fi.sykero.kdom.Namespace.NO_NAMESPACE
        val dc = fi.sykero.kdom.Namespace.getNamespace("dc", DC)
        val svg = fi.sykero.kdom.Namespace.getNamespace("svg", SVG)
        val xlink = fi.sykero.kdom.Namespace.getNamespace("xlink", XLINK)
        val out = ArrayList<Case>()
        out += Case("//ns:rootfile") { c, ns -> c.root().filter { it.isNamed("rootfile", ns) }.toList() }
        out += Case("//ns:manifest/ns:item") { c, ns ->
            c.root().filter { it.isNamed("manifest", ns) }
                .flatMap { it.childElements() }.filter { it.isNamed("item", ns) }.toList()
        }
        out += Case("//ns:manifest/ns:item[@media-type='application/x-dtbncx+xml']") { c, ns ->
            c.root().filter { it.isNamed("manifest", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("item", ns) && it.attributeIs("media-type", "application/x-dtbncx+xml") }.toList()
        }
        out += Case("//ns:manifest/ns:item[@id='toc']") { c, ns ->
            c.root().filter { it.isNamed("manifest", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("item", ns) && it.attributeIs("id", "toc") }.toList()
        }
        out += Case("//ns:manifest/ns:item[@properties='nav']") { c, ns ->
            c.root().filter { it.isNamed("manifest", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("item", ns) && it.attributeIs("properties", "nav") }.toList()
        }
        out += Case("//ns:navPoint/ns:content") { c, ns ->
            c.root().filter { it.isNamed("navPoint", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("content", ns) }.toList()
        }
        out += Case("../ns:navLabel/ns:text", elementContextOnly = true) { c, ns ->
            (c as fi.sykero.kdom.Element).parentElement?.childElements().orEmpty()
                .filter { it.isNamed("navLabel", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("text", ns) }.toList()
        }
        for (k in listOf("title", "creator", "language")) {
            out += Case("//dc:$k") { c, _ -> c.root().filter { it.isNamed(k, dc) }.toList() }
        }
        out += Case("//ns:spine/ns:itemref") { c, ns ->
            c.root().filter { it.isNamed("spine", ns) }.flatMap { it.childElements() }
                .filter { it.isNamed("itemref", ns) }.toList()
        }
        out += Case("//ns:BVA") { c, ns -> c.root().filter { it.isNamed("BVA", ns) }.toList() }
        out += Case(".//ns:BVA") { c, ns -> c.descendantElements().filter { it.isNamed("BVA", ns) }.toList() }
        out += Case("//svg:image[@xlink:href]") { c, _ ->
            c.root().filter { it.isNamed("image", svg) && it.getAttribute("href", xlink) != null }.toList()
        }
        out += Case("//ns:img") { c, ns -> c.root().filter { it.isNamed("img", ns) }.toList() }
        out += Case("//ns:a") { c, ns -> c.root().filter { it.isNamed("a", ns) }.toList() }
        out += Case("//ns:body") { c, ns -> c.root().filter { it.isNamed("body", ns) }.toList() }
        out += Case("//*[@id]") { c, _ -> c.root().filter { it.hasAttribute("id") }.toList() }
        for (n in listOf(1, 2, 3, 4, 5, 6, 7, 99)) {
            out += Case("//*[descendant::ns:BVA[@ordinal='$n'] and (following-sibling::ns:p or preceding-sibling::ns:p or self::ns:p)]") { c, ns ->
                c.root().filter { e ->
                    e.descendantElements().any { it.isNamed("BVA", ns) && it.attributeIs("ordinal", "$n") } &&
                        (e.followingSiblingElements().any { it.isNamed("p", ns) } ||
                            e.precedingSiblingElements().any { it.isNamed("p", ns) } ||
                            e.isNamed("p", ns))
                }.toList()
            }
        }
        out += Case(".//text()[not(ancestor::note)]", text = true) { c, _ ->
            c.descendantTexts().filter { t -> t.parentElement!!.let { p -> (sequenceOf(p) + p.ancestorElements()).none { it.isNamed("note", none) } } }.toList()
        }
        // Extra axes exercised directly (not :app expressions, but the helpers' XPath equivalents).
        out += Case("ancestor::*", elementContextOnly = true) { c, _ -> (c as fi.sykero.kdom.Element).ancestorElements().toList().asReversed() }
        out += Case("*", elementContextOnly = true) { c, _ -> (c as fi.sykero.kdom.Element).childElements().toList() }
        out += Case("following-sibling::*", elementContextOnly = true) { c, _ -> (c as fi.sykero.kdom.Element).followingSiblingElements().toList() }
        out += Case("preceding-sibling::*", elementContextOnly = true) { c, _ -> (c as fi.sykero.kdom.Element).precedingSiblingElements().toList().asReversed() }
        out += Case("descendant::*") { c, _ -> c.descendantElements().toList() }
        return out
    }

    // ---- harness ------------------------------------------------------------------------------------------------

    private fun jdomContexts(d: org.jdom2.Document, detachedBody: Boolean): List<Any> {
        val ctx = ArrayList<Any>()
        if (detachedBody) {
            val body = d.rootElement.content.filterIsInstance<org.jdom2.Element>().firstOrNull { it.name == "body" }?.detach() ?: return emptyList()
            ctx += body; body.descendants.forEach { if (it is org.jdom2.Element) ctx += it }
        } else {
            ctx += d; d.descendants.forEach { if (it is org.jdom2.Element) ctx += it }
        }
        return ctx
    }

    private fun kdomContexts(d: fi.sykero.kdom.Document, detachedBody: Boolean): List<Any> {
        val ctx = ArrayList<Any>()
        if (detachedBody) {
            val body = d.rootElement.content.filterIsInstance<fi.sykero.kdom.Element>().firstOrNull { it.name == "body" }?.detach() ?: return emptyList()
            ctx += body; body.descendants.forEach { if (it is fi.sykero.kdom.Element) ctx += it }
        } else {
            ctx += d; d.descendants.forEach { if (it is fi.sykero.kdom.Element) ctx += it }
        }
        return ctx
    }

    @Test fun axesMatchJaxen() {
        val fixtures = listOf(CONTAINER_XML, OPF_XML, NCX_XML, XHTML_XML)
        val cases = cases()
        val nonEmpty = HashMap<String, Int>()
        val mismatches = ArrayList<String>()
        var compared = 0
        for (xml in fixtures) for (detached in listOf(false, true)) {
            val jd = SAXBuilder().build(StringReader(xml))
            val kd = KdomBuilder().build(xml)
            val jc = jdomContexts(jd, detached)
            val kc = kdomContexts(kd, detached)
            assertEquals("context counts", jc.size, kc.size)
            for (nsUri in NAMESPACES) {
                val jns = org.jdom2.Namespace.getNamespace("ns", nsUri)
                val kns = fi.sykero.kdom.Namespace.getNamespace("ns", nsUri)
                for (case in cases) {
                    val xp = if (case.text)
                        XPathFactory.instance().compile(case.xpath, Filters.text(), null, jns,
                            org.jdom2.Namespace.getNamespace("dc", DC), org.jdom2.Namespace.getNamespace("svg", SVG),
                            org.jdom2.Namespace.getNamespace("xlink", XLINK))
                    else
                        XPathFactory.instance().compile(case.xpath, Filters.element(), null, jns,
                            org.jdom2.Namespace.getNamespace("dc", DC), org.jdom2.Namespace.getNamespace("svg", SVG),
                            org.jdom2.Namespace.getNamespace("xlink", XLINK))
                    for (i in jc.indices) {
                        if (case.elementContextOnly && jc[i] is org.jdom2.Document) continue
                        val expected = xp.evaluate(jc[i]).map { key(it as org.jdom2.Content) }
                        val actual = case.axes(kc[i] as fi.sykero.kdom.Parent, kns).map { key(it) }
                        compared++
                        if (expected.isNotEmpty()) nonEmpty.merge(case.xpath, 1, Int::plus)
                        if (expected != actual) {
                            mismatches += "${case.xpath} ns=$nsUri detached=$detached ctx#$i ${if (jc[i] is org.jdom2.Element) key(jc[i] as org.jdom2.Element) else "document"}\n  jaxen: $expected\n  axes : $actual"
                        }
                    }
                }
            }
        }
        assertTrue("${mismatches.size} mismatches of $compared:\n" + mismatches.distinctBy { it.substringBefore(" ctx#") }.take(40).joinToString("\n"), mismatches.isEmpty())
        // Guard against a vacuous test: every expression must select something from at least one context.
        for (c in cases) if (c.xpath.contains("'99'")) continue else assertTrue("never non-empty: ${c.xpath}", (nonEmpty[c.xpath] ?: 0) > 0)
        assertTrue(compared > 5000)
    }

    /** The jaxen quirk the plan names: an unprefixed `note` in XPath 1.0 is a no-namespace name test. */
    @Test fun unprefixedNoteMatchesOnlyNoNamespaceNote() {
        val kd = KdomBuilder().build(XHTML_XML)
        val texts = kd.descendantTexts().map { it.value }.toList()
        assertTrue(texts.any { it.contains("xhtml note") })
        val kept = kd.rootElement.descendantTexts()
            .filter { t -> t.parentElement!!.let { p -> (sequenceOf(p) + p.ancestorElements()).none { it.isNamed("note", fi.sykero.kdom.Namespace.NO_NAMESPACE) } } }
            .map { it.value }.toList()
        assertTrue(kept.any { it.contains("xhtml note") })
        assertTrue(kept.none { it.contains("plain note") || it == "bold" })
    }
}
