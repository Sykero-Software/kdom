package fi.sykero.kdom.diff

import java.io.StringWriter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * The broad outputter differential (Task 12): programmatically built, identical trees in jdom2 2.0.6.1 and kdom
 * (never parsed), serialized with raw / pretty / compact formats, each in UTF-8, ISO-8859-1 and US-ASCII, and the
 * strings must be identical. The Task 6 [OutputDifferentialTest] already pins the generic document shapes (simple,
 * escapes, mixed, namespaces, xml:space, prolog), the stream/writer entry points, line separators and TrAX PIs; this
 * class adds what the corpus and AndBible depend on and that class does not cover:
 *  - every character class that needs escaping or encoding: `& < > " '` in text and attribute values, `]]>` in text,
 *    `\r \n \t`, U+007F, U+0085, U+2028/2029, U+00A0, U+FEFF, U+FFFD, surrogate pairs, in element and attribute
 *    content and in comments / PIs / CDATA, under each encoding (numeric character references for unencodable ones);
 *  - whitespace-only text between and inside elements, xml:space=preserve / default nesting, under all text modes;
 *  - nested default and prefixed namespaces, including `xmlns=""` re-declaration and prefixed attributes;
 *  - the legal neighbours of `]]>` in CDATA (and that the illegal forms are rejected the same way by both);
 *  - illegal control characters and other illegal data (constructor rejection must be the same exception class);
 *  - a custom [fi.sykero.kdom.output.support.AbstractXMLOutputProcessor] overriding `printText` (AndBible's
 *    `elementToString`) in raw and pretty mode.
 */
class OutputterDifferentialTest {

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
    }

    private fun e(name: String, vararg kids: N) = N.E(name, kids = kids.toList())
    private fun a(name: String, value: String) = Triple<String, String, Pair<String, String>?>(name, value, null)
    private val xmlNs = "xml" to "http://www.w3.org/XML/1998/namespace"
    private val space = { v: String -> Triple<String, String, Pair<String, String>?>("space", v, xmlNs) }

    private val specials = "& < > \" ' ]]> ]] > \r\n \r \n \t \u007f \u0085       ﻿ � é € 😀 𝄞 end"

    private val trees: List<Pair<String, N>> = listOf(
        "text-specials" to e("r", N.T(specials), e("x", N.T("a&b<c>d\"e'f"))),
        "attr-specials" to N.E("r", attrs = listOf(
            a("q", "&<>\"'"), a("w", "\r\n\t \u0085  "), a("u", "é€😀𝄞 ﻿"), a("e", ""), a("sp", "  a  b  ")
        ), kids = listOf(N.E("c", attrs = listOf(a("x", "]]>")), kids = listOf(N.T("t"))))),
        "cdata-neighbours" to e("r",
            N.C("]]"), N.C("]>"), N.C("] ]>"), N.C("a]]"), N.C(">b"), N.C("<&>\r\n\t é😀 \u0085 "), N.C(""), N.C("  "),
            N.T("x]]>y"), N.T("]]"), N.T(">")),
        "comments-pis" to N.E("r", kids = listOf(
            N.Cm(" a - b "), N.Cm(""), N.Cm("<&>\"' é😀 \r\n\t\u0085 "), N.P("t", "d"), N.P("t2", ""),
            N.P("t3", "  x <&> \"' é😀 \r\n  y  "), e("c", N.Cm("in"), N.P("p", "q"), N.T(" ")))),
        "mixed-whitespace" to e("p",
            N.T(" "), e("a"), N.T("\n  \t"), e("b", N.T(" "), e("c")), N.T("  x  \n y  "), N.C(" "), N.T(" "), N.Cm("c"), N.T("\n"),
            e("i", N.T("  ")), e("j", N.T("\r\n")), N.T("tail  ")),
        "whitespace-only-tree" to e("r", N.T("   "), e("a", N.T("\n\n")), N.T("\t")),
        "space-preserve" to e("r",
            N.E("keep", attrs = listOf(space("preserve")), kids = listOf(
                N.T("  a  "), e("x", N.T(" y  z "), e("deep", N.T(" \n "))), N.T("\n  b \t "), N.C("  "), e("empty"))),
            N.E("dflt", attrs = listOf(space("default")), kids = listOf(N.T("  a  "), e("x", N.T(" y "), N.T("  ")))),
            N.E("outer", attrs = listOf(space("preserve")), kids = listOf(
                N.E("reset", attrs = listOf(space("default")), kids = listOf(N.T("  p  "), e("q", N.T(" r "))))))),
        "space-preserve-escapes" to N.E("r", attrs = listOf(space("preserve")), kids = listOf(
            N.T("  <&>\r\n\"'\u0085 😀 "), e("x", N.T(" \r ")))),
        "namespaces" to N.E("root", ns = "" to "urn:default", extraNs = listOf("p" to "urn:p"),
            attrs = listOf(a("plain", "1"), Triple("pa", "2", "p" to "urn:p"), Triple("lang", "en", xmlNs)),
            kids = listOf(
                N.E("a", kids = listOf(N.T("in default"))),
                N.E("b", ns = "p" to "urn:p", kids = listOf(
                    N.E("c", ns = "q" to "urn:q", attrs = listOf(Triple("z", "9", "q" to "urn:q")), kids = listOf(
                        N.E("d", ns = "" to "urn:other", kids = listOf(N.E("e", ns = "" to "", kids = listOf(N.T("no namespace"))))))),
                    N.E("rebound", ns = "p" to "urn:p2", kids = listOf(N.T("x"))))),
                N.E("f", ns = "" to "", kids = listOf(N.E("g", ns = "" to "urn:default")))
            )),
        "unicode-names" to N.E("root", kids = listOf(
            N.E("élément", attrs = listOf(a("attribüt", "v")), kids = listOf(N.T("t"))),
            N.E("a.b-c_d", kids = listOf(N.E("日本語", kids = listOf(N.T("𝄞")))))))
    )

    // ---- builders ----

    private fun kd(n: N): fi.sykero.kdom.Content = when (n) {
        is N.T -> fi.sykero.kdom.Text(n.s)
        is N.C -> fi.sykero.kdom.CDATA(n.s)
        is N.Cm -> fi.sykero.kdom.Comment(n.s)
        is N.P -> fi.sykero.kdom.ProcessingInstruction(n.target, n.data)
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
            n.kids.forEach { el.addContent(kd(it)) }
            el
        }
    }

    private fun jd(n: N): org.jdom2.Content = when (n) {
        is N.T -> org.jdom2.Text(n.s)
        is N.C -> org.jdom2.CDATA(n.s)
        is N.Cm -> org.jdom2.Comment(n.s)
        is N.P -> org.jdom2.ProcessingInstruction(n.target, n.data)
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
            n.kids.forEach { el.addContent(jd(it)) }
            el
        }
    }

    private class Fmt(val name: String, val k: fi.sykero.kdom.output.Format, val j: org.jdom2.output.Format)

    /** raw / pretty / compact x UTF-8 / ISO-8859-1 / US-ASCII, plus the two remaining text modes. */
    private fun formats(): List<Fmt> {
        val out = ArrayList<Fmt>()
        for (enc in listOf("UTF-8", "ISO-8859-1", "US-ASCII")) {
            out += Fmt("raw/$enc", fi.sykero.kdom.output.Format.getRawFormat().setEncoding(enc), org.jdom2.output.Format.getRawFormat().setEncoding(enc))
            out += Fmt("pretty/$enc", fi.sykero.kdom.output.Format.getPrettyFormat().setEncoding(enc), org.jdom2.output.Format.getPrettyFormat().setEncoding(enc))
            out += Fmt("compact/$enc", fi.sykero.kdom.output.Format.getCompactFormat().setEncoding(enc), org.jdom2.output.Format.getCompactFormat().setEncoding(enc))
        }
        out += Fmt("trim-full-white/US-ASCII",
            fi.sykero.kdom.output.Format.getRawFormat().setTextMode(fi.sykero.kdom.output.Format.TextMode.TRIM_FULL_WHITE).setEncoding("US-ASCII"),
            org.jdom2.output.Format.getRawFormat().setTextMode(org.jdom2.output.Format.TextMode.TRIM_FULL_WHITE).setEncoding("US-ASCII"))
        out += Fmt("pretty-preserve-unix",
            fi.sykero.kdom.output.Format.getPrettyFormat().setTextMode(fi.sykero.kdom.output.Format.TextMode.PRESERVE)
                .setLineSeparator(fi.sykero.kdom.output.LineSeparator.UNIX).setIndent("\t"),
            org.jdom2.output.Format.getPrettyFormat().setTextMode(org.jdom2.output.Format.TextMode.PRESERVE)
                .setLineSeparator(org.jdom2.output.LineSeparator.UNIX).setIndent("\t"))
        out += Fmt("compact-expand-cr",
            fi.sykero.kdom.output.Format.getCompactFormat().setExpandEmptyElements(true).setLineSeparator("\r"),
            org.jdom2.output.Format.getCompactFormat().setExpandEmptyElements(true).setLineSeparator("\r"))
        return out
    }

    // ---- tests ----

    @Test
    fun elementsMatchJdom2InAllFormatsAndEncodings() {
        for ((name, tree) in trees) for (f in formats()) {
            val k = fi.sykero.kdom.output.XMLOutputter(f.k)
            val j = org.jdom2.output.XMLOutputter(f.j)
            val ke = kd(tree) as fi.sykero.kdom.Element
            val je = jd(tree) as org.jdom2.Element
            assertEquals("element $name / ${f.name}", j.outputString(je), k.outputString(ke))
            assertEquals("content $name / ${f.name}", j.outputElementContentString(je), k.outputElementContentString(ke))
            assertEquals("children $name / ${f.name}", j.outputString(je.content), k.outputString(ke.content))
        }
    }

    @Test
    fun documentsWithPrologMatchJdom2() {
        for ((name, tree) in trees) for (f in formats()) {
            val kdoc = fi.sykero.kdom.Document().apply {
                addContent(fi.sykero.kdom.Comment(" pro<&>log é😀 "))
                addContent(fi.sykero.kdom.ProcessingInstruction("pi", "a=\"b\" é"))
                addContent(kd(tree)); addContent(fi.sykero.kdom.Comment("epi"))
            }
            val jdoc = org.jdom2.Document().apply {
                addContent(org.jdom2.Comment(" pro<&>log é😀 "))
                addContent(org.jdom2.ProcessingInstruction("pi", "a=\"b\" é"))
                addContent(jd(tree)); addContent(org.jdom2.Comment("epi"))
            }
            assertEquals("document $name / ${f.name}",
                org.jdom2.output.XMLOutputter(f.j).outputString(jdoc), fi.sykero.kdom.output.XMLOutputter(f.k).outputString(kdoc))
            val kw = StringWriter(); val jw = StringWriter()
            fi.sykero.kdom.output.XMLOutputter(f.k).output(kdoc, kw)
            org.jdom2.output.XMLOutputter(f.j).output(jdoc, jw)
            assertEquals("writer $name / ${f.name}", jw.toString(), kw.toString())
        }
    }

    @Test
    fun everyLegalCodePointClassMatchesJdom2InTextAttributeAndEncodedOutput() {
        val points = listOf(0x09, 0x0A, 0x0D, 0x20, 0x22, 0x26, 0x27, 0x3C, 0x3E, 0x7F, 0x80, 0x85, 0x9F, 0xA0, 0xFF, 0x100, 0x2028, 0x2029,
            0xD7FF, 0xE000, 0xFDD0, 0xFEFF, 0xFFFD, 0x10000, 0x1D11E, 0x1F600, 0x10FFFF)
        for (cp in points) {
            val s = "a" + String(Character.toChars(cp)) + "b"
            val tree = N.E("r", attrs = listOf(a("v", s)), kids = listOf(N.T(s), N.C(s.replace("]]>", "")), N.Cm(s), N.P("t", s)))
            for (f in formats()) {
                val j = org.jdom2.output.XMLOutputter(f.j)
                val k = fi.sykero.kdom.output.XMLOutputter(f.k)
                val ref = try { j.outputString(jd(tree) as org.jdom2.Element) } catch (e: Exception) { "threw ${e::class.simpleName}" }
                val got = try { k.outputString(kd(tree) as fi.sykero.kdom.Element) } catch (e: Exception) { "threw ${e::class.simpleName}" }
                assertEquals("U+${cp.toString(16)} / ${f.name}", ref, got)
            }
        }
    }

    @Test
    fun illegalDataIsRejectedWithTheSameExceptionClass() {
        val lone = listOf("\uD800", "\uDC00", "a\uD800b", "\uDC00\uD800")
        val ctl = (0x00..0x1F).filter { it != 0x09 && it != 0x0A && it != 0x0D }.map { String(Character.toChars(it)) } + listOf("￾", "￿")
        fun <T> outcome(f: () -> T): String = try { f(); "ok" } catch (e: Exception) { e::class.simpleName!! }
        val cases = ArrayList<Triple<String, () -> Any, () -> Any>>()
        for (s in lone + ctl) {
            val t = "a${s}b"
            cases += Triple("Text ${show(t)}", { org.jdom2.Text(t) }, { fi.sykero.kdom.Text(t) })
            cases += Triple("CDATA ${show(t)}", { org.jdom2.CDATA(t) }, { fi.sykero.kdom.CDATA(t) })
            cases += Triple("Comment ${show(t)}", { org.jdom2.Comment(t) }, { fi.sykero.kdom.Comment(t) })
            cases += Triple("PI ${show(t)}", { org.jdom2.ProcessingInstruction("t", t) }, { fi.sykero.kdom.ProcessingInstruction("t", t) })
            cases += Triple("Attribute ${show(t)}", { org.jdom2.Attribute("n", t) }, { fi.sykero.kdom.Attribute("n", t) })
        }
        cases += Triple("CDATA ]]>", { org.jdom2.CDATA("a]]>b") }, { fi.sykero.kdom.CDATA("a]]>b") })
        cases += Triple("CDATA setText ]]>", { org.jdom2.CDATA("x").setText("]]>") }, { fi.sykero.kdom.CDATA("x").setText("]]>") })
        cases += Triple("Comment --", { org.jdom2.Comment("a--b") }, { fi.sykero.kdom.Comment("a--b") })
        cases += Triple("Comment trailing -", { org.jdom2.Comment("a-") }, { fi.sykero.kdom.Comment("a-") })
        cases += Triple("PI xml", { org.jdom2.ProcessingInstruction("xml", "d") }, { fi.sykero.kdom.ProcessingInstruction("xml", "d") })
        cases += Triple("PI ?>", { org.jdom2.ProcessingInstruction("t", "a?>b") }, { fi.sykero.kdom.ProcessingInstruction("t", "a?>b") })
        cases += Triple("Element name with space", { org.jdom2.Element("a b") }, { fi.sykero.kdom.Element("a b") })
        cases += Triple("Element name with colon", { org.jdom2.Element("a:b") }, { fi.sykero.kdom.Element("a:b") })
        cases += Triple("Attribute name xmlns", { org.jdom2.Attribute("xmlns", "x") }, { fi.sykero.kdom.Attribute("xmlns", "x") })
        cases += Triple("Namespace reserved prefix", { org.jdom2.Namespace.getNamespace("xml", "urn:x") }, { fi.sykero.kdom.Namespace.getNamespace("xml", "urn:x") })
        for ((name, j, k) in cases) assertEquals("rejection of $name", outcome(j), outcome(k))
        // sanity: the table really exercises rejections
        assertNotNull(cases.firstOrNull { outcome(it.second) != "ok" })
        assertEquals("IllegalDataException", outcome { org.jdom2.Text("\u0001") })
    }

    private fun show(s: String) = s.map { if (it.code < 32 || it.code > 126) "\\u%04x".format(it.code) else it.toString() }.joinToString("")

    /** AndBible's `elementToString`: a processor overriding printText, here decoding a few HTML entities first. */
    private fun decode(s: String) = s.replace("&quot;", "\"").replace("&lt;", "<").replace("&gt;", ">").replace("&amp;", "&")

    @Test
    fun customPrintTextProcessorMatchesJdom2() {
        val tree = e("r", N.T("a &amp; b &quot;q&quot; &lt;x&gt; & raw < \" é😀"), e("c", N.T("&amp;&amp;"), N.T(" &lt; ")),
            N.E("k", attrs = listOf(space("preserve"), a("at", "&quot;&amp;")), kids = listOf(N.T("  &gt;  "))), N.C("&amp;"), N.Cm("&amp;"))
        for (f in formats()) {
            val jp = object : org.jdom2.output.support.AbstractXMLOutputProcessor() {
                override fun printText(out: java.io.Writer, fstack: org.jdom2.output.support.FormatStack, text: org.jdom2.Text) {
                    text.text = decode(text.text)
                    super.printText(out, fstack, text)
                }
            }
            val kp = object : fi.sykero.kdom.output.support.AbstractXMLOutputProcessor() {
                override fun printText(out: Appendable, fstack: fi.sykero.kdom.output.support.FormatStack, text: fi.sykero.kdom.Text) {
                    text.text = decode(text.text)
                    super.printText(out, fstack, text)
                }
            }
            val j = org.jdom2.output.XMLOutputter(f.j, jp).outputString(jd(tree) as org.jdom2.Element)
            val k = fi.sykero.kdom.output.XMLOutputter(f.k, kp).outputString(kd(tree) as fi.sykero.kdom.Element)
            assertEquals("custom printText / ${f.name}", j, k)
        }
    }

    @Test
    fun detachedAndNestedFragmentsMatchJdom2() {
        val tree = trees.first { it.first == "namespaces" }.second as N.E
        // a sub-element printed on its own must re-declare the inherited namespaces exactly as jdom2 does
        val kdeep = (kd(tree) as fi.sykero.kdom.Element).content.filterIsInstance<fi.sykero.kdom.Element>().first { it.name == "b" }
        val jdeep = (jd(tree) as org.jdom2.Element).children.first { it.name == "b" }
        for (f in formats()) {
            assertEquals("sub-element ${f.name}", org.jdom2.output.XMLOutputter(f.j).outputString(jdeep), fi.sykero.kdom.output.XMLOutputter(f.k).outputString(kdeep))
        }
    }
}
