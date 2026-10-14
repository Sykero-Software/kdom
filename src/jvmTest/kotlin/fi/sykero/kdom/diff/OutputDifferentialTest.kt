package fi.sykero.kdom.diff

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Light differential check of the Kotlin output package against jdom2 2.0.6.1 itself (the full outputter
 * differential is a later task). The same small document trees are built once in each library and written
 * with raw / pretty / compact formats, several encodings, line separators and the string, writer and
 * stream entry points; the output must be identical to the character.
 */
class OutputDifferentialTest {

    /** A library neutral description of a tree. */
    private sealed class N {
        class E(
            val name: String,
            val ns: Pair<String, String>? = null,
            val attrs: List<Triple<String, String, Pair<String, String>?>> = emptyList(),
            val extraNs: List<Pair<String, String>> = emptyList(),
            val kids: List<N> = emptyList()
        ) : N()

        class T(val s: String) : N()
        class C(val s: String) : N()
        class Cm(val s: String) : N()
        class P(val target: String, val data: String) : N()
        class R(val name: String) : N()
    }

    private fun e(name: String, vararg kids: N) = N.E(name, kids = kids.toList())

    private val docs: List<Pair<String, List<N>>> = listOf(
        "simple" to listOf(e("root", e("a", N.T("hello")), e("b"), e("c", N.T("x"), e("d"), N.T("y")))),
        "escapes" to listOf(
            N.E(
                "root",
                attrs = listOf(Triple("q", "a\"b<c>&d\te\nf\rg", null)),
                kids = listOf(N.T("1 < 2 & 3 > 2\r\n\n tail "), N.C("raw <&> data"), N.R("copy"), N.T("é€😀 end"))
            )
        ),
        "mixed" to listOf(
            e(
                "p",
                N.T("  lead "), e("b", N.T(" bold ")), N.T("  \n  between   words  "), N.C("  cd  "),
                N.T(" "), N.R("amp"), N.T("\n"), N.Cm(" a comment "), N.P("pi", "some data"), N.P("empty", ""),
                e("i", N.T("   ")), N.T("tail  ")
            )
        ),
        "namespaces" to listOf(
            N.E(
                "root", ns = "p" to "urn:p",
                attrs = listOf(Triple("id", "1", null), Triple("lang", "en", "xml" to "http://www.w3.org/XML/1998/namespace")),
                extraNs = listOf("x" to "urn:x"),
                kids = listOf(
                    N.E("child", ns = "x" to "urn:x", kids = listOf(N.T("t"))),
                    N.E("other", ns = "" to "urn:default", kids = listOf(e("inner")))
                )
            )
        ),
        "space" to listOf(
            e(
                "root",
                N.E(
                    "keep",
                    attrs = listOf(Triple("space", "preserve", "xml" to "http://www.w3.org/XML/1998/namespace")),
                    kids = listOf(N.T("  a  "), e("x", N.T(" y ")), N.T("  b  "))
                ),
                N.E(
                    "dflt",
                    attrs = listOf(Triple("space", "default", "xml" to "http://www.w3.org/XML/1998/namespace")),
                    kids = listOf(N.T("  a  "), e("x", N.T(" y ")), N.T("  b  "))
                ),
                e("deep", e("l1", e("l2", e("l3", N.T("t"))), e("l2b")))
            )
        ),
        "prolog" to listOf(N.Cm("before"), N.P("xml-stylesheet", "href=\"a.css\""), e("root", N.T("x")), N.Cm("after"))
    )

    // ---- builders ----

    private fun kdom(n: N): fi.sykero.kdom.Content = when (n) {
        is N.T -> fi.sykero.kdom.Text(n.s)
        is N.C -> fi.sykero.kdom.CDATA(n.s)
        is N.Cm -> fi.sykero.kdom.Comment(n.s)
        is N.P -> fi.sykero.kdom.ProcessingInstruction(n.target, n.data)
        is N.R -> fi.sykero.kdom.EntityRef(n.name)
        is N.E -> {
            val el = if (n.ns == null) fi.sykero.kdom.Element(n.name)
            else fi.sykero.kdom.Element(n.name, fi.sykero.kdom.Namespace.getNamespace(n.ns.first, n.ns.second))
            n.extraNs.forEach { el.addNamespaceDeclaration(fi.sykero.kdom.Namespace.getNamespace(it.first, it.second)) }
            n.attrs.forEach { (name, value, ns) ->
                el.setAttribute(
                    if (ns == null) fi.sykero.kdom.Attribute(name, value)
                    else fi.sykero.kdom.Attribute(name, value, fi.sykero.kdom.Namespace.getNamespace(ns.first, ns.second))
                )
            }
            n.kids.forEach { el.addContent(kdom(it)) }
            el
        }
    }

    private fun jdom(n: N): org.jdom2.Content = when (n) {
        is N.T -> org.jdom2.Text(n.s)
        is N.C -> org.jdom2.CDATA(n.s)
        is N.Cm -> org.jdom2.Comment(n.s)
        is N.P -> org.jdom2.ProcessingInstruction(n.target, n.data)
        is N.R -> org.jdom2.EntityRef(n.name)
        is N.E -> {
            val el = if (n.ns == null) org.jdom2.Element(n.name)
            else org.jdom2.Element(n.name, org.jdom2.Namespace.getNamespace(n.ns.first, n.ns.second))
            n.extraNs.forEach { el.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace(it.first, it.second)) }
            n.attrs.forEach { (name, value, ns) ->
                el.setAttribute(
                    if (ns == null) org.jdom2.Attribute(name, value)
                    else org.jdom2.Attribute(name, value, org.jdom2.Namespace.getNamespace(ns.first, ns.second))
                )
            }
            n.kids.forEach { el.addContent(jdom(it)) }
            el
        }
    }

    private fun kdomDoc(top: List<N>): fi.sykero.kdom.Document {
        val d = fi.sykero.kdom.Document()
        top.forEach { d.addContent(kdom(it)) }
        d.docType = fi.sykero.kdom.DocType("root", "-//X//EN", "x.dtd")
        return d
    }

    private fun jdomDoc(top: List<N>): org.jdom2.Document {
        val d = org.jdom2.Document()
        top.forEach { d.addContent(jdom(it)) }
        d.docType = org.jdom2.DocType("root", "-//X//EN", "x.dtd")
        return d
    }

    // ---- format pairs ----

    private class Fmt(val name: String, val k: fi.sykero.kdom.output.Format, val j: org.jdom2.output.Format)

    private fun formats(): List<Fmt> {
        val out = mutableListOf<Fmt>()
        out += Fmt("raw", fi.sykero.kdom.output.Format.getRawFormat(), org.jdom2.output.Format.getRawFormat())
        out += Fmt("pretty", fi.sykero.kdom.output.Format.getPrettyFormat(), org.jdom2.output.Format.getPrettyFormat())
        out += Fmt("compact", fi.sykero.kdom.output.Format.getCompactFormat(), org.jdom2.output.Format.getCompactFormat())
        out += Fmt(
            "pretty-4-lf-expand-ascii",
            fi.sykero.kdom.output.Format.getPrettyFormat().setIndent("    ").setLineSeparator("\n")
                .setExpandEmptyElements(true).setEncoding("US-ASCII"),
            org.jdom2.output.Format.getPrettyFormat().setIndent("    ").setLineSeparator("\n")
                .setExpandEmptyElements(true).setEncoding("US-ASCII")
        )
        out += Fmt(
            "fullwhite-latin1-nodecl",
            fi.sykero.kdom.output.Format.getRawFormat()
                .setTextMode(fi.sykero.kdom.output.Format.TextMode.TRIM_FULL_WHITE)
                .setOmitDeclaration(true).setEncoding("ISO-8859-1").setLineSeparator(fi.sykero.kdom.output.LineSeparator.UNIX),
            org.jdom2.output.Format.getRawFormat()
                .setTextMode(org.jdom2.output.Format.TextMode.TRIM_FULL_WHITE)
                .setOmitDeclaration(true).setEncoding("ISO-8859-1").setLineSeparator(org.jdom2.output.LineSeparator.UNIX)
        )
        out += Fmt(
            "normalize-noenc-utf16",
            fi.sykero.kdom.output.Format.getCompactFormat().setIndent("\t").setOmitEncoding(true).setEncoding("UTF-16")
                .setLineSeparator(fi.sykero.kdom.output.LineSeparator.NONE),
            org.jdom2.output.Format.getCompactFormat().setIndent("\t").setOmitEncoding(true).setEncoding("UTF-16")
                .setLineSeparator(org.jdom2.output.LineSeparator.NONE)
        )
        return out
    }

    @Test
    fun documentsMatchJdom2() {
        for ((docName, top) in docs) {
            for (f in formats()) {
                val k = fi.sykero.kdom.output.XMLOutputter(f.k).outputString(kdomDoc(top))
                val j = org.jdom2.output.XMLOutputter(f.j).outputString(jdomDoc(top))
                assertEquals("document $docName / ${f.name}", j, k)
            }
        }
    }

    @Test
    fun elementsListsAndLeafContentMatchJdom2() {
        for ((docName, top) in docs) {
            val root = top.filterIsInstance<N.E>().first()
            for (f in formats()) {
                val k = fi.sykero.kdom.output.XMLOutputter(f.k)
                val j = org.jdom2.output.XMLOutputter(f.j)
                val ke = kdom(root) as fi.sykero.kdom.Element
                val je = jdom(root) as org.jdom2.Element
                val tag = "$docName / ${f.name}"
                assertEquals("element $tag", j.outputString(je), k.outputString(ke))
                assertEquals("content string $tag", j.outputElementContentString(je), k.outputElementContentString(ke))
                assertEquals("list $tag", j.outputString(je.content), k.outputString(ke.content))
                assertEquals("attr escape $tag", j.escapeAttributeEntities("a<\"&\né"), k.escapeAttributeEntities("a<\"&\né"))
                assertEquals("text escape $tag", j.escapeElementEntities("a<>&\r\né"), k.escapeElementEntities("a<>&\r\né"))
                assertEquals("text $tag", j.outputString(org.jdom2.Text("  a < b  ")), k.outputString(fi.sykero.kdom.Text("  a < b  ")))
                assertEquals("cdata $tag", j.outputString(org.jdom2.CDATA(" a ")), k.outputString(fi.sykero.kdom.CDATA(" a ")))
                assertEquals("comment $tag", j.outputString(org.jdom2.Comment("c")), k.outputString(fi.sykero.kdom.Comment("c")))
                assertEquals("pi $tag", j.outputString(org.jdom2.ProcessingInstruction("t", "d")), k.outputString(fi.sykero.kdom.ProcessingInstruction("t", "d")))
                assertEquals("entity $tag", j.outputString(org.jdom2.EntityRef("e")), k.outputString(fi.sykero.kdom.EntityRef("e")))
                assertEquals("doctype $tag", j.outputString(org.jdom2.DocType("r", "p", "s")), k.outputString(fi.sykero.kdom.DocType("r", "p", "s")))
                // jdom2 itself throws a NullPointerException from toString() when the line separator is null.
                if (f.j.lineSeparator != null) {
                    assertEquals("toString $tag", j.toString(), k.toString())
                }
            }
        }
    }

    @Test
    fun streamOutputUsesTheFormatEncoding() {
        val top = docs.first { it.first == "escapes" }.second
        for (f in formats()) {
            val kb = java.io.ByteArrayOutputStream()
            val jb = java.io.ByteArrayOutputStream()
            fi.sykero.kdom.output.XMLOutputter(f.k).output(kdomDoc(top), kb)
            org.jdom2.output.XMLOutputter(f.j).output(jdomDoc(top), jb)
            assertEquals("stream ${f.name}", jb.toByteArray().toList(), kb.toByteArray().toList())
        }
    }

    @Test
    fun trAxEscapingProcessingInstructionsMatchJdom2() {
        val k = fi.sykero.kdom.Element("r").addContent(
            listOf(
                fi.sykero.kdom.ProcessingInstruction("javax.xml.transform.disable-output-escaping", ""),
                fi.sykero.kdom.Text("<&>"),
                fi.sykero.kdom.ProcessingInstruction("javax.xml.transform.enable-output-escaping", ""),
                fi.sykero.kdom.Text("<&>")
            )
        )
        val j = org.jdom2.Element("r").addContent(
            listOf(
                org.jdom2.ProcessingInstruction("javax.xml.transform.disable-output-escaping", ""),
                org.jdom2.Text("<&>"),
                org.jdom2.ProcessingInstruction("javax.xml.transform.enable-output-escaping", ""),
                org.jdom2.Text("<&>")
            )
        )
        assertEquals(
            org.jdom2.output.XMLOutputter().outputString(j),
            fi.sykero.kdom.output.XMLOutputter().outputString(k)
        )
        val kf = fi.sykero.kdom.output.Format.getRawFormat().apply { ignoreTrAXEscapingPIs = true }
        val jf = org.jdom2.output.Format.getRawFormat().apply { ignoreTrAXEscapingPIs = true }
        assertEquals(
            org.jdom2.output.XMLOutputter(jf).outputString(j),
            fi.sykero.kdom.output.XMLOutputter(kf).outputString(k)
        )
    }
}
