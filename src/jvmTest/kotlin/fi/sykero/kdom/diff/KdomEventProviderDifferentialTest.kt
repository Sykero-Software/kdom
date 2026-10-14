package fi.sykero.kdom.diff

import fi.sykero.kdom.input.KdomBuilder
import fi.sykero.kdom.sax.KdomEventProvider
import org.jdom2.input.SAXBuilder
import org.jdom2.output.SAXOutputter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.io.StringReader

/**
 * [KdomEventProvider] against jdom2's `SAXOutputter(handler).output(doc)`, the oracle (what JSword's
 * `JDOMSAXEventProvider` does). Both recorders write the same event strings; `characters` is recorded per call
 * (offset, length, array size and text), nothing is merged. Every attribute is probed by index and by both name lookups.
 */
class KdomEventProviderDifferentialTest {

    // ---- recorders ----

    private fun esc(s: String?): String =
        if (s == null) "null" else s.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }.joinToString("")

    private fun chars(ch: CharArray, s: Int, l: Int) = "characters(start=$s,len=$l,array=${ch.size},'${esc(String(ch, s, l))}')"

    private fun attEvent(uri: String?, local: String?, q: String?, n: Int, a: (Int) -> List<String?>,
                         byQ: (String) -> List<String?>, byName: (String, String) -> List<String?>): String {
        val sb = StringBuilder("startElement(${esc(uri)},${esc(local)},${esc(q)}) n=$n")
        for (i in 0 until n) {
            val x = a(i)
            sb.append(" [$i: ").append(x.joinToString("|") { esc(it) })
            sb.append(" byQ=").append(byQ(x[2]!!).joinToString("|") { esc(it) })
            sb.append(" byName=").append(byName(x[0]!!, x[1]!!).joinToString("|") { esc(it) }).append("]")
        }
        sb.append(" miss=").append(byQ("nope:none").joinToString("|")).append("/").append(byName("urn:nope", "none").joinToString("|"))
        return sb.toString()
    }

    private fun jdkRecorder(out: MutableList<String>) = object : org.xml.sax.helpers.DefaultHandler() {
        override fun setDocumentLocator(l: org.xml.sax.Locator?) {
            out += "setDocumentLocator(${l != null}" + (if (l != null) ",${l.lineNumber},${l.columnNumber},${esc(l.publicId)},${esc(l.systemId)}" else "") + ")"
        }
        override fun startDocument() { out += "startDocument" }
        override fun endDocument() { out += "endDocument" }
        override fun startPrefixMapping(p: String, u: String) { out += "startPrefixMapping(${esc(p)},${esc(u)})" }
        override fun endPrefixMapping(p: String) { out += "endPrefixMapping(${esc(p)})" }
        override fun startElement(uri: String?, localName: String?, qName: String?, a: org.xml.sax.Attributes) {
            out += attEvent(uri, localName, qName, a.length,
                { i -> listOf(a.getURI(i), a.getLocalName(i), a.getQName(i), a.getType(i), a.getValue(i)) },
                { q -> listOf(a.getIndex(q).toString(), a.getType(q), a.getValue(q)) },
                { u, l -> listOf(a.getIndex(u, l).toString(), a.getType(u, l), a.getValue(u, l)) })
        }
        override fun endElement(uri: String?, localName: String?, qName: String?) { out += "endElement(${esc(uri)},${esc(localName)},${esc(qName)})" }
        override fun characters(ch: CharArray, start: Int, length: Int) { out += chars(ch, start, length) }
        override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { out += "ignorable" + chars(ch, start, length) }
        override fun processingInstruction(target: String, data: String?) { out += "processingInstruction(${esc(target)},${esc(data)})" }
        override fun skippedEntity(name: String) { out += "skippedEntity(${esc(name)})" }
    }

    private fun kdomRecorder(out: MutableList<String>) = object : fi.sykero.kdom.sax.DefaultHandler() {
        override fun setDocumentLocator(locator: fi.sykero.kdom.sax.Locator?) {
            val l = locator
            out += "setDocumentLocator(${l != null}" + (if (l != null) ",${l.lineNumber},${l.columnNumber},${esc(l.publicId)},${esc(l.systemId)}" else "") + ")"
        }
        override fun startDocument() { out += "startDocument" }
        override fun endDocument() { out += "endDocument" }
        override fun startPrefixMapping(prefix: String, uri: String) { out += "startPrefixMapping(${esc(prefix)},${esc(uri)})" }
        override fun endPrefixMapping(prefix: String) { out += "endPrefixMapping(${esc(prefix)})" }
        override fun startElement(uri: String, localName: String, qName: String, atts: fi.sykero.kdom.sax.Attributes) {
            val a = atts
            out += attEvent(uri, localName, qName, a.length,
                { i -> listOf(a.getURI(i), a.getLocalName(i), a.getQName(i), a.getType(i), a.getValue(i)) },
                { q -> listOf(a.getIndex(q).toString(), a.getType(q), a.getValue(q)) },
                { u, l -> listOf(a.getIndex(u, l).toString(), a.getType(u, l), a.getValue(u, l)) })
        }
        override fun endElement(uri: String, localName: String, qName: String) { out += "endElement(${esc(uri)},${esc(localName)},${esc(qName)})" }
        override fun characters(ch: CharArray, start: Int, length: Int) { out += chars(ch, start, length) }
        override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { out += "ignorable" + chars(ch, start, length) }
        override fun processingInstruction(target: String, data: String?) { out += "processingInstruction(${esc(target)},${esc(data)})" }
        override fun skippedEntity(name: String) { out += "skippedEntity(${esc(name)})" }
    }

    private fun oracle(doc: org.jdom2.Document): List<String> {
        val out = ArrayList<String>(); SAXOutputter(jdkRecorder(out)).output(doc); return out
    }
    private fun oracleElement(e: org.jdom2.Element): List<String> {
        val out = ArrayList<String>(); SAXOutputter(jdkRecorder(out)).output(org.jdom2.Document(e)); return out
    }
    private fun kdom(doc: fi.sykero.kdom.Document): List<String> {
        val out = ArrayList<String>(); KdomEventProvider(doc).provideSAXEvents(kdomRecorder(out)); return out
    }

    private fun check(xml: String) {
        val expected = oracle(SAXBuilder().build(StringReader(xml)))
        val actual = kdom(KdomBuilder().build(xml))
        assertEquals("events for $xml", expected.joinToString("\n"), actual.joinToString("\n"))
    }

    // ---- parsed inputs ----

    @Test fun validControl() = check("<a>hello</a>")

    @Test fun namespacedDocument() =
        check("""<a xmlns="urn:d" xmlns:p="urn:p" p:x="1" y="2"><p:b>t&amp;u<![CDATA[c]]><!--k--><?pi d?></p:b></a>""")

    @Test fun osisVerseWithNoteStrongsQuoteTitle() = check(OSIS_1)

    @Test fun thmlSample() = check(
        """<div class="x"><scripRef passage="Jn 3:16">John 3:16</scripRef> <sync type="Strongs" value="G25"/> love</div>""")

    @Test fun osisKjvStrongsDivineNameTransChange() = check(OSIS_KJV)

    @Test fun osisEsvStudyNoteRedLetterPoetry() = check(OSIS_ESV)

    @Test fun osisPsalmTitleCanonical() = check(OSIS_PSALM)

    @Test fun nestedDefaultNamespaceRedeclarations() = check(
        """<a xmlns="urn:1"><b xmlns="urn:2"><c xmlns=""><d xmlns="urn:1"/></c></b><e xmlns:p="urn:p"><f xmlns:p="urn:q" p:z="1"/><g p:z="2"/></e></a>""")

    @Test fun emptyElementsAndMixedContent() = check(
        """<r><e/><e></e> <m>a<i>b</i>c<!--x-->d<?t?>e<?t2 data  here?></m>
        </r>""")

    @Test fun attributesWithNamespaces() = check(
        """<r xmlns:a="urn:a" xmlns:b="urn:b" a:x="1" b:x="2" plain="3" xml:lang="fi" xml:space="preserve"><a:c b:y="v" xml:id="i"/></r>""")

    @Test fun namespacePrefixDeclaredOnlyOnAttribute() = check("""<r xmlns:q="urn:q"><s q:a="1" xmlns:z="urn:z"/></r>""")

    @Test fun surrogatesAndEntities() = check("<r>&lt;&#x1F600;&#233;&gt;</r>")

    @Test fun commentsAndPisAtDocumentLevel() = check("<?a b?><!--c--><r/><!--d--><?e f?>")

    @Test fun xmlSpaceElements() = check(
        """<r xml:space="default">  <a xml:space="preserve"> x <b/> </a>  </r>""")

    // ---- detached elements and programmatic trees ----

    private fun jdomTree(): org.jdom2.Element {
        val ns = org.jdom2.Namespace.getNamespace("p", "urn:p")
        return org.jdom2.Element("root", ns).apply {
            addNamespaceDeclaration(org.jdom2.Namespace.getNamespace("z", "urn:z"))
            setAttribute(org.jdom2.Attribute("id", "i1", org.jdom2.AttributeType.ID))
            setAttribute(org.jdom2.Attribute("kind", "k", org.jdom2.AttributeType.ENUMERATION, ns))
            setAttribute("plain", "pv")
            addContent(org.jdom2.Text(""))
            addContent("a")
            addContent(org.jdom2.CDATA("<c>"))
            addContent(org.jdom2.EntityRef("nbsp"))
            addContent(org.jdom2.Comment("c"))
            addContent(org.jdom2.ProcessingInstruction("tgt", "d d"))
            addContent(org.jdom2.ProcessingInstruction("bare"))
            addContent(org.jdom2.Element("child").setAttribute("lang", "en", org.jdom2.Namespace.XML_NAMESPACE).addContent(org.jdom2.Text(" ")))
            addContent(org.jdom2.Element("e", org.jdom2.Namespace.getNamespace("p", "urn:p")))
        }
    }

    private fun kdomTree(): fi.sykero.kdom.Element {
        val ns = fi.sykero.kdom.Namespace.getNamespace("p", "urn:p")
        return fi.sykero.kdom.Element("root", ns).apply {
            addNamespaceDeclaration(fi.sykero.kdom.Namespace.getNamespace("z", "urn:z"))
            setAttribute(fi.sykero.kdom.Attribute("id", "i1", fi.sykero.kdom.AttributeType.ID))
            setAttribute(fi.sykero.kdom.Attribute("kind", "k", fi.sykero.kdom.AttributeType.ENUMERATION, ns))
            setAttribute("plain", "pv")
            addContent(fi.sykero.kdom.Text(""))
            addContent("a")
            addContent(fi.sykero.kdom.CDATA("<c>"))
            addContent(fi.sykero.kdom.EntityRef("nbsp"))
            addContent(fi.sykero.kdom.Comment("c"))
            addContent(fi.sykero.kdom.ProcessingInstruction("tgt", "d d"))
            addContent(fi.sykero.kdom.ProcessingInstruction("bare"))
            addContent(fi.sykero.kdom.Element("child").setAttribute("lang", "en", fi.sykero.kdom.Namespace.XML_NAMESPACE).addContent(fi.sykero.kdom.Text(" ")))
            addContent(fi.sykero.kdom.Element("e", fi.sykero.kdom.Namespace.getNamespace("p", "urn:p")))
        }
    }

    @Test fun programmaticTreeWithEntityRefAttributeTypesAndEmptyText() {
        val expected = oracleElement(jdomTree())
        val out = ArrayList<String>()
        KdomEventProvider(kdomTree()).provideSAXEvents(kdomRecorder(out))
        assertEquals(expected.joinToString("\n"), out.joinToString("\n"))
    }

    @Test fun detachedElementConstructor() {
        val e = jdomTree()
        val expected = oracleElement(e)
        val k = kdomTree()
        val out = ArrayList<String>()
        KdomEventProvider(k).provideSAXEvents(kdomRecorder(out))
        assertEquals(expected.joinToString("\n"), out.joinToString("\n"))
    }

    @Test fun elementWithExistingDocumentIsNotDetached() {
        val xml = """<?a b?><r xmlns="urn:r"><c>t</c></r><!--end-->"""
        val jdoc = SAXBuilder().build(StringReader(xml))
        val expected = oracle(jdoc)
        val kdoc = KdomBuilder().build(xml)
        val root = kdoc.rootElement
        val out = ArrayList<String>()
        KdomEventProvider(root).provideSAXEvents(kdomRecorder(out))
        assertEquals(expected.joinToString("\n"), out.joinToString("\n"))
        assertSame(kdoc, root.document)
        assertSame(root, kdoc.rootElement)
        assertNotNull(root.document)
    }

    @Test fun childElementOfDocumentUsesItsOwnDocument() {
        val kdoc = KdomBuilder().build("<r><c a='1'>t</c></r>")
        val child = kdoc.rootElement.getChild("c")!!
        val out = ArrayList<String>()
        KdomEventProvider(child).provideSAXEvents(kdomRecorder(out))
        // the whole owning document is reported, as with `frag.document ?: Document(frag)` + SAXOutputter
        assertEquals(oracle(SAXBuilder().build(StringReader("<r><c a='1'>t</c></r>"))).joinToString("\n"), out.joinToString("\n"))
        assertSame(kdoc, child.document)
        assertSame(child, kdoc.rootElement.getChild("c"))
    }

    @Test fun handlerCanBeReusedAndProviderRepeated() {
        val doc = KdomBuilder().build("<a><b/></a>")
        val p = KdomEventProvider(doc)
        val o1 = ArrayList<String>(); val o2 = ArrayList<String>()
        p.provideSAXEvents(kdomRecorder(o1)); p.provideSAXEvents(kdomRecorder(o2))
        assertEquals(o1, o2)
    }

    private companion object {
        val OSIS_1 =
            """<verse osisID="Gen.1.1" sID="Gen.1.1"/>In the beginning <w lemma="strong:H07225">God</w><note type="study" n="a">a note <q who="Jesus">words</q></note> <title>Heading</title><verse eID="Gen.1.1"/>"""
                .let { "<div>$it</div>" }

        val OSIS_KJV =
            """<div><verse osisID="John.3.16"><w lemma="strong:G3779" morph="robinson:ADV">For</w> <w lemma="strong:G2316" morph="robinson:N-NSM"><divineName>God</divineName></w> so <w lemma="strong:G25 strong:G3588" morph="robinson:V-AAI-3S">loved</w> the world, that he gave his only begotten Son, <transChange type="added">that</transChange> whosoever believeth in him should not perish<milestone type="x-p"/>, but have everlasting life.</verse></div>"""

        val OSIS_ESV =
            """<div><verse osisID="Matt.5.3" sID="Matt.5.3"/><q who="Jesus" marker=""><q level="2" who="Jesus" marker="">Blessed are the poor in spirit,</q></q><note type="study" n="1" osisRef="Matt.5.3" osisID="Matt.5.3!note.1"><reference type="source" osisRef="Matt.5.3">5:3</reference> <catchWord>poor</catchWord> Luke 6:20 &amp; <hi type="italic">more</hi>.</note> <lg><l level="1">for theirs is the kingdom</l><l level="2" type="x-indent">of heaven.</l></lg><verse eID="Matt.5.3"/></div>"""

        val OSIS_PSALM =
            """<div type="book"><title type="psalm" canonical="true">A Psalm of David.</title><lg><l>The <divineName>Lord</divineName> is my shepherd;</l><l> I shall not want.</l></lg><title type="acrostic" subType="x-preverse"><foreign xml:lang="he">א</foreign></title></div>"""
    }
}
