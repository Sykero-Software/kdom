package fi.sykero.kdom.query

import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.input.KdomBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class AxesTest {
    private val xml = """<r xmlns="urn:x" xmlns:o="urn:other" id="r"><a id="a1"><b/><o:b/>t1<![CDATA[cd]]></a><a><c><b/></c></a><!--c--><p/><p/><p/></r>"""
    private fun doc(): Document = KdomBuilder().build(xml)
    private val x = Namespace.getNamespace("q", "urn:x")   // other prefix, same URI
    private val o = Namespace.getNamespace("urn:other")
    private fun Sequence<Element>.names() = map { it.name + "/" + it.namespaceURI.substringAfter(':') }.toList()

    @Test fun xpathRootAttachedDetachedAndSubtree() {
        val d = doc()
        val a = d.rootElement.childElements().first()
        assertSame(d, d.xpathRoot())
        assertSame(d, d.rootElement.xpathRoot())
        assertSame(d, a.xpathRoot())
        val detached = d.rootElement.childElements().drop(1).first().detach()
        assertNull(detached.xpathRoot())
        assertNull(detached.childElements().first().childElements().first().xpathRoot())
        assertNull(Element("lonely").xpathRoot())
    }

    @Test fun absoluteQueriesFromDetachedTreesAreEmptyButRelativeWork() {
        val d = doc()
        val c = d.rootElement.childElements().drop(1).first().detach()
        assertEquals(emptyList(), c.findAll("b", x))
        assertEquals(1, c.descendantElements().filter { it.isNamed("b", x) }.count())
        assertEquals(2, d.findAll("b", x).size + d.findAll("b", o).size)   // c and its b were detached
    }

    @Test fun allElementsInDocumentOrder() {
        val d = doc()
        assertEquals(listOf("r", "a", "b", "b", "a", "c", "b", "p", "p", "p"), d.allElements().map { it.name }.toList())
        assertEquals(listOf("a", "b", "b", "a", "c", "b", "p", "p", "p"), d.rootElement.descendantElements().map { it.name }.toList())
    }

    @Test fun findAllMatchesNamespaceByUriNotPrefix() {
        val d = doc()
        assertEquals(2, d.findAll("b", x).size)                   // default-ns b, prefix irrelevant
        assertEquals(1, d.findAll("b", o).size)                   // o:b, prefix "" here, URI matches
        assertEquals(0, d.findAll("b", Namespace.NO_NAMESPACE).size)
        assertEquals(0, d.findAll("b", Namespace.getNamespace("o", "urn:x2")).size)
        assertEquals(listOf("urn:x"), d.findAll("r", Namespace.getNamespace("zz", "urn:x")).map { it.namespaceURI })
    }

    @Test fun noNamespaceNameTest() {
        val e = KdomBuilder().build("""<r xmlns="urn:x"><n xmlns=""/><n/></r>""").rootElement
        val ns = e.childElements().filter { it.isNamed("n", Namespace.NO_NAMESPACE) }.toList()
        assertEquals(1, ns.size)
        assertEquals("", ns[0].namespaceURI)
    }

    @Test fun descendantTextsIncludeCdataAndSkipCommentsAndElements() {
        val d = doc()
        assertEquals(listOf("t1", "cd"), d.descendantTexts().map { it.value }.toList())
        assertEquals(emptyList(), d.rootElement.childElements().last().descendantTexts().toList())
    }

    @Test fun ancestorsAndSiblingsNearestFirst() {
        val d = doc()
        val inner = d.rootElement.childElements().drop(1).first().childElements().first().childElements().first()
        assertEquals(listOf("c", "a", "r"), inner.ancestorElements().map { it.name }.toList())
        val ps = d.rootElement.childElements().filter { it.name == "p" }.toList()
        assertEquals(listOf(ps[1], ps[0]), ps[2].precedingSiblingElements().filter { it.name == "p" }.toList())
        assertEquals(listOf("a", "a"), ps[0].precedingSiblingElements().map { it.name }.toList())
        assertEquals(listOf("p", "p"), ps[0].followingSiblingElements().map { it.name }.toList())
        assertEquals(emptyList(), d.rootElement.followingSiblingElements().toList())
        assertEquals(emptyList(), d.rootElement.precedingSiblingElements().toList())
        assertEquals(emptyList(), d.rootElement.ancestorElements().toList())
        assertEquals(listOf("a", "a", "p", "p", "p"), d.rootElement.childElements().map { it.name }.toList())
    }

    @Test fun attributeTests() {
        val r = doc().rootElement
        assertTrue(r.hasAttribute("id"))
        assertFalse(r.hasAttribute("nope"))
        assertTrue(r.attributeIs("id", "r"))
        assertFalse(r.attributeIs("id", "R"))
        assertFalse(r.attributeIs("nope", ""))
    }

    @Test fun sequencesAreLazy() {
        val d = doc()
        var touched = 0
        val s = d.rootElement.descendantElements().onEach { touched++ }
        assertEquals(0, touched)
        s.first()
        assertEquals(1, touched)
    }

    @Test fun childStepsKeepDocumentOrderWithNestedParents() {
        val d = KdomBuilder().build("""<n xmlns="urn:x"><np id="o"><np id="i"><c src="inner"/></np><c src="outer"/></np></n>""")
        assertEquals(listOf("inner", "outer"), d.findChildrenOf("np", x, "c", x).map { it.getAttribute("src")!!.value })
        assertEquals(listOf("outer"), d.findChildrenOf("np", x, "c", x) { it.attributeIs("src", "outer") }.map { it.getAttribute("src")!!.value })
        assertEquals(emptyList(), d.findChildrenOf("n", x, "c", x))          // c is not a child of n
        assertEquals(emptyList(), d.findChildrenOf("np", x, "c", Namespace.NO_NAMESPACE))
        assertEquals(emptyList(), d.rootElement.clone().let { (it as Element).findChildrenOf("np", x, "c", x) })
    }
}
