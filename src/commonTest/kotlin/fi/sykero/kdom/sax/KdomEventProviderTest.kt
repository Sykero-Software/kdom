package fi.sykero.kdom.sax

import fi.sykero.kdom.input.KdomBuilder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class KdomEventProviderTest {

    private class Recorder : DefaultHandler() {
        val events = ArrayList<String>()
        override fun setDocumentLocator(locator: Locator?) { events += "locator(${locator?.lineNumber},${locator?.columnNumber},${locator?.publicId},${locator?.systemId})" }
        override fun startDocument() { events += "startDocument" }
        override fun endDocument() { events += "endDocument" }
        override fun startPrefixMapping(prefix: String, uri: String) { events += "startPrefixMapping($prefix,$uri)" }
        override fun endPrefixMapping(prefix: String) { events += "endPrefixMapping($prefix)" }
        override fun startElement(uri: String, localName: String, qName: String, atts: Attributes) {
            val a = (0 until atts.length).joinToString(",") { "${atts.getQName(it)}{${atts.getURI(it)}}:${atts.getType(it)}=${atts.getValue(it)}" }
            events += "start($uri,$localName,$qName)[$a]"
        }
        override fun endElement(uri: String, localName: String, qName: String) { events += "end($uri,$localName,$qName)" }
        override fun characters(ch: CharArray, start: Int, length: Int) { events += "chars($start,$length,${ch.size},${ch.concatToString(start, start + length)})" }
        override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { events += "ignorable" }
        override fun processingInstruction(target: String, data: String?) { events += "pi($target,$data)" }
        override fun skippedEntity(name: String) { events += "skipped($name)" }
    }

    private fun events(xml: String): List<String> {
        val r = Recorder()
        KdomEventProvider(KdomBuilder().build(xml)).provideSAXEvents(r)
        return r.events
    }

    @Test fun namespacedDocumentSequence() {
        assertEquals(
            listOf(
                "locator(-1,-1,null,null)",
                "startDocument",
                "startPrefixMapping(,urn:d)",
                "startPrefixMapping(p,urn:p)",
                "start(urn:d,a,a)[p:x{urn:p}:CDATA=1,y{}:CDATA=2]",
                "start(urn:p,b,p:b)[]",
                "chars(0,3,3,t&u)",
                "chars(0,1,1,c)",
                "pi(pi,d)",
                "end(urn:p,b,p:b)",
                "end(urn:d,a,a)",
                "endPrefixMapping(p)",
                "endPrefixMapping()",
                "endDocument",
            ),
            events("""<a xmlns="urn:d" xmlns:p="urn:p" p:x="1" y="2"><p:b>t&amp;u<![CDATA[c]]><!--k--><?pi d?></p:b></a>"""))
    }

    @Test fun elementConstructorKeepsExistingDocument() {
        val doc = KdomBuilder().build("<?x y?><r><c/></r>")
        val r = Recorder()
        KdomEventProvider(doc.rootElement).provideSAXEvents(r)
        assertEquals(events("<?x y?><r><c/></r>"), r.events)
        assertSame(doc, doc.rootElement.document)
    }

    @Test fun detachedElementIsWrapped() {
        val e = fi.sykero.kdom.Element("e").setAttribute("k", "v")
        val r = Recorder()
        KdomEventProvider(e).provideSAXEvents(r)
        assertEquals(listOf("locator(-1,-1,null,null)", "startDocument", "start(,e,e)[k{}:CDATA=v]", "end(,e,e)", "endDocument"), r.events)
    }

    @Test fun attributesImplSemantics() {
        val a = AttributesImpl()
        a.addAttribute("urn:p", "x", "p:x", "CDATA", "1")
        a.addAttribute("", "x", "x", "ID", "2")
        assertEquals(2, a.length)
        assertEquals(0, a.getIndex("urn:p", "x")); assertEquals(1, a.getIndex("", "x")); assertEquals(1, a.getIndex("x"))
        assertEquals(-1, a.getIndex("q")); assertEquals(-1, a.getIndex("urn:q", "x"))
        assertEquals("ID", a.getType("x")); assertEquals("1", a.getValue("urn:p", "x"))
        assertNull(a.getValue("q")); assertNull(a.getType("urn:q", "x")); assertNull(a.getValue(2)); assertNull(a.getURI(-1))
        a.clear()
        assertEquals(0, a.length); assertEquals(-1, a.getIndex("x"))
    }
}
