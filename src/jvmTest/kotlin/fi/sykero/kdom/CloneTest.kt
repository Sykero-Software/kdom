package fi.sykero.kdom

import fi.sykero.kdom.output.EscapeStrategy
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.LineSeparator
import fi.sykero.kdom.output.XMLOutputter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

/** kdom's clone() is a per-class shallowCopy() plus jdom2's deep-copy code (no Object.clone() in common Kotlin). */
class CloneTest {
    @Test fun elementCloneIsDeepAndDetached() {
        val root = Element("root", "p", "urn:p")
        root.addNamespaceDeclaration(Namespace.getNamespace("q", "urn:q"))
        root.setAttribute("a", "1")
        root.addContent(Element("child").setText("t"))
        Document(root)
        val c = root.clone()
        assertNull(c.parent)
        assertEquals("root", c.name)
        assertSame(root.namespace, c.namespace)
        assertEquals(root.additionalNamespaces, c.additionalNamespaces)
        assertNotSame(root.getAttribute("a"), c.getAttribute("a"))
        assertSame(c, c.getAttribute("a")!!.parent)
        assertEquals("t", c.getChild("child")!!.text)
        assertNotSame(root.getChild("child"), c.getChild("child"))
    }

    @Test fun documentCloneSharesPropertiesAsJdom2() {
        val doc = Document(Element("r"))
        doc.baseURI = "http://example.org/"
        doc.setProperty("k", "v")
        val c = doc.clone()
        assertEquals("http://example.org/", c.baseURI)
        assertEquals("v", c.getProperty("k"))
        assertNotSame(doc.rootElement, c.rootElement)
    }

    /** Behaviour difference: a subclass outside kdom clones to its kdom superclass (jdom2 kept the runtime class). */
    @Test fun subclassClonesToKdomClass() {
        class MyElement : Element("x")
        val c = MyElement().clone()
        assertEquals(Element::class.java, c.javaClass)
        assertEquals("x", c.name)
    }

    @Test fun formatCloneCopiesEveryField() {
        val strategy = EscapeStrategy { false }
        val f = Format.getPrettyFormat().setEncoding("US-ASCII").setLineSeparator(LineSeparator.UNIX)
            .setOmitDeclaration(true).setOmitEncoding(true).setExpandEmptyElements(true).setIndent("\t")
            .setTextMode(Format.TextMode.NORMALIZE).setEscapeStrategy(strategy)
        f.isSpecifiedAttributesOnly = true
        f.ignoreTrAXEscapingPIs = true
        val c = f.clone()
        assertEquals(f.indent, c.indent)
        assertEquals(f.lineSeparator, c.lineSeparator)
        assertEquals(f.encoding, c.encoding)
        assertEquals(true, c.omitDeclaration)
        assertEquals(true, c.omitEncoding)
        assertEquals(true, c.expandEmptyElements)
        assertEquals(true, c.isSpecifiedAttributesOnly)
        assertEquals(true, c.ignoreTrAXEscapingPIs)
        assertEquals(Format.TextMode.NORMALIZE, c.textMode)
        assertSame(strategy, c.escapeStrategy)
    }

    @Test fun outputterCloneSharesFormat() {
        val o = XMLOutputter(Format.getCompactFormat())
        val c = o.clone()
        assertSame(o.format, c.format)
        assertSame(o.xmlOutputProcessor, c.xmlOutputProcessor)
    }
}
