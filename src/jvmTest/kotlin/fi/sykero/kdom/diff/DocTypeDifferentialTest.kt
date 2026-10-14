package fi.sykero.kdom.diff

import fi.sykero.kdom.input.KdomBuilder
import fi.sykero.kdom.input.LexicalEventHandler
import fi.sykero.kdom.input.XmlEventParser
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory
import org.jdom2.input.SAXBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.xml.sax.InputSource
import org.xml.sax.ext.DefaultHandler2

/**
 * Permissive DOCTYPE mode (R11, `allowDocType = true`) against the JDK: jdom2 `SAXBuilder` configured as AndBible's
 * `CommonUtils.useSaxBuilder` (`external-general-entities` off, which in jdom2 also turns entity expansion off) plus
 * `load-external-dtd` off (determinism; Android's parser never reads it), and the JDK SAX parser with the same two features
 * (events incl. `startDTD`/`endDTD`, `DeclHandler`, `skippedEntity`). The JDK expands relative system ids of declarations
 * against the working directory; that prefix is removed before comparing.
 */
class DocTypeDifferentialTest {

    @Test fun treesMatchSaxBuilder() {
        val problems = ArrayList<String>()
        for (xml in TREE_INPUTS) {
            val jdom = try { jdomRaw(xml) } catch (e: Exception) { "REJECT" }
            val kdom = try { kdomRaw(xml) } catch (e: fi.sykero.kdom.input.JDOMParseException) { "REJECT" } catch (e: Exception) { "LEAK $e" }
            if (jdom != kdom) problems += "${show(xml)}\n  jdom2: $jdom\n  kdom : $kdom"
        }
        if (problems.isNotEmpty()) fail(problems.size.toString() + " mismatches:\n" + problems.joinToString("\n"))
    }

    @Test fun eventsMatchSax() {
        val problems = ArrayList<String>()
        for (xml in TREE_INPUTS + ENTITY_EXPANSION_INPUTS) {
            for (ns in listOf(true, false)) {
                val sax = try { sax(xml, ns) } catch (e: org.xml.sax.SAXParseException) { "REJECT" }
                val kdom = try { kdom(xml, ns) } catch (e: fi.sykero.kdom.sax.SAXParseException) { "REJECT" } catch (e: Exception) { "LEAK $e" }
                if (sax != kdom) problems += "${show(xml)} ns=$ns\n  SAX : $sax\n  kdom: $kdom"
            }
        }
        if (problems.isNotEmpty()) fail(problems.size.toString() + " mismatches:\n" + problems.joinToString("\n"))
    }

    @Test fun strictModeStillRejectsEveryDoctype() {
        for (xml in TREE_INPUTS.filter { it.contains("<!DOCTYPE") }) {
            val r = runCatching { KdomBuilder().build(xml) }.exceptionOrNull()
            assertEquals(show(xml), fi.sykero.kdom.input.JDOMParseException::class.java, r?.javaClass)
        }
    }

    // ---- oracles ----

    private fun jdomBuilder() = SAXBuilder().apply {
        setFeature("http://xml.org/sax/features/external-general-entities", false) // as CommonUtils.useSaxBuilder
        setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    }

    private fun jdomRaw(xml: String): String {
        val d = jdomBuilder().build(StringReader(xml))
        return esc(org.jdom2.output.XMLOutputter(org.jdom2.output.Format.getRawFormat()).outputString(d)).replace(CWD_URI, "") +
            " | subset=" + esc(d.docType?.internalSubset ?: "-").replace(CWD_URI, "") + " | " + jdomKinds(d.content)
    }

    private fun jdomKinds(c: List<org.jdom2.Content>): String = c.joinToString(",") {
        if (it is org.jdom2.Element) "E[" + jdomKinds(it.content) + "]" else it.javaClass.simpleName
    }

    private fun kdomRaw(xml: String): String {
        val d = KdomBuilder(allowDocType = true).build(xml)
        return esc(fi.sykero.kdom.output.XMLOutputter(fi.sykero.kdom.output.Format.getRawFormat()).outputString(d)) +
            " | subset=" + esc(d.docType?.internalSubset ?: "-") + " | " + kdomKinds(d.content)
    }

    private fun kdomKinds(c: List<fi.sykero.kdom.Content>): String = c.joinToString(",") {
        if (it is fi.sykero.kdom.Element) "E[" + kdomKinds(it.content) + "]" else it::class.simpleName!!
    }

    private class Events {
        val list = ArrayList<String>()
        private val text = StringBuilder()
        fun chars(ch: CharArray, s: Int, l: Int) { text.append(ch, s, l) }
        fun add(e: String) {
            if (text.isNotEmpty()) { list += "chars(" + esc(text.toString()) + ")"; text.setLength(0) }
            list += esc(e).replace(CWD_URI, "")
        }
        fun done(): String { add("end"); return list.joinToString(" ") }
    }

    private fun sax(xml: String, ns: Boolean): String {
        val ev = Events()
        val h = object : DefaultHandler2() {
            override fun startDocument() { ev.add("startDocument") }
            override fun endDocument() { ev.add("endDocument") }
            override fun startPrefixMapping(p: String, u: String) { ev.add("map($p=$u)") }
            override fun endPrefixMapping(p: String) { ev.add("unmap($p)") }
            override fun startElement(u: String, l: String, q: String, a: org.xml.sax.Attributes) {
                ev.add("start($u|$l|$q " + (0 until a.length).joinToString(";") { a.getQName(it) + "=" + a.getValue(it) + "/" + a.getType(it) } + ")")
            }
            override fun endElement(u: String, l: String, q: String) { ev.add("end($q)") }
            override fun characters(ch: CharArray, s: Int, l: Int) { ev.chars(ch, s, l) }
            override fun processingInstruction(t: String, d: String?) { ev.add("pi($t|$d)") }
            override fun skippedEntity(n: String) { ev.add("skipped($n)") }
            override fun comment(ch: CharArray, s: Int, l: Int) { ev.add("comment(" + String(ch, s, l) + ")") }
            override fun startCDATA() { ev.add("[cdata") }
            override fun endCDATA() { ev.add("cdata]") }
            override fun startDTD(n: String, p: String?, s: String?) { ev.add("startDTD($n|$p|$s)") }
            override fun endDTD() { ev.add("endDTD") }
            override fun elementDecl(n: String, m: String) { ev.add("elementDecl($n|$m)") }
            override fun internalEntityDecl(n: String, v: String) { ev.add("internalEntityDecl($n|$v)") }
            override fun externalEntityDecl(n: String, p: String?, s: String?) { ev.add("externalEntityDecl($n|$p|$s)") }
            override fun notationDecl(n: String, p: String?, s: String?) { ev.add("notationDecl($n|$p|$s)") }
            override fun unparsedEntityDecl(n: String, p: String?, s: String?, nn: String) { ev.add("unparsedEntityDecl($n|$p|$s|$nn)") }
        }
        val f = SAXParserFactory.newInstance().apply {
            isNamespaceAware = ns
            setFeature("http://xml.org/sax/features/external-general-entities", false)
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
        }
        val p = f.newSAXParser()
        p.setProperty("http://xml.org/sax/properties/lexical-handler", h)
        p.setProperty("http://xml.org/sax/properties/declaration-handler", h)
        p.parse(InputSource(StringReader(xml)), h)
        return ev.done()
    }

    private fun kdom(xml: String, ns: Boolean): String {
        val ev = Events()
        val h = object : fi.sykero.kdom.sax.ContentHandler, LexicalEventHandler {
            override fun setDocumentLocator(locator: fi.sykero.kdom.sax.Locator?) {}
            override fun startDocument() { ev.add("startDocument") }
            override fun endDocument() { ev.add("endDocument") }
            override fun startPrefixMapping(prefix: String, uri: String) { ev.add("map($prefix=$uri)") }
            override fun endPrefixMapping(prefix: String) { ev.add("unmap($prefix)") }
            override fun startElement(uri: String, localName: String, qName: String, atts: fi.sykero.kdom.sax.Attributes) {
                ev.add("start($uri|$localName|$qName " + (0 until atts.length).joinToString(";") { atts.getQName(it) + "=" + atts.getValue(it) + "/" + atts.getType(it) } + ")")
            }
            override fun endElement(uri: String, localName: String, qName: String) { ev.add("end($qName)") }
            override fun characters(ch: CharArray, start: Int, length: Int) { ev.chars(ch, start, length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { ev.add("ignorableWhitespace") }
            override fun processingInstruction(target: String, data: String?) { ev.add("pi($target|$data)") }
            override fun skippedEntity(name: String) { ev.add("skipped($name)") }
            override fun comment(ch: CharArray, start: Int, length: Int) { ev.add("comment(" + String(ch, start, length) + ")") }
            override fun startCDATA() { ev.add("[cdata") }
            override fun endCDATA() { ev.add("cdata]") }
            override fun startDTD(name: String, publicId: String?, systemId: String?) { ev.add("startDTD($name|$publicId|$systemId)") }
            override fun endDTD() { ev.add("endDTD") }
            override fun elementDecl(name: String, model: String) { ev.add("elementDecl($name|$model)") }
            override fun internalEntityDecl(name: String, value: String) { ev.add("internalEntityDecl($name|$value)") }
            override fun externalEntityDecl(name: String, publicId: String?, systemId: String?) { ev.add("externalEntityDecl($name|$publicId|$systemId)") }
            override fun notationDecl(name: String, publicId: String?, systemId: String?) { ev.add("notationDecl($name|$publicId|$systemId)") }
            override fun unparsedEntityDecl(name: String, publicId: String?, systemId: String?, notationName: String) { ev.add("unparsedEntityDecl($name|$publicId|$systemId|$notationName)") }
        }
        XmlEventParser(namespaceAware = ns, allowDocType = true).parse(xml, h)
        return ev.done()
    }

    companion object {
        private val CWD_URI = Regex("file://file:[^\"|)]*/")

        fun esc(s: String) = s.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }.joinToString("")
        private fun show(s: String) = esc(s)

        private const val XHTML11 = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">"
        private const val XHTML10T = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
        private const val NCX = "<!DOCTYPE ncx PUBLIC \"-//NISO//DTD ncx 2005-1//EN\"\n \"http://www.daisy.org/z3986/2005/ncx-2005-1.dtd\">"

        /** EPUB-like inputs whose trees are compared with jdom2 (no internal-entity use in content, see below). */
        val TREE_INPUTS: List<String> = listOf(
            // XHTML 1.1 / 1.0 with entity references the (unread) DTD would define: skippedEntity -> EntityRef
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$XHTML11\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"><head><title>T&nbsp;1</title></head>" +
                "<body><p class=\"x\">a&nbsp;b&copy;&mdash;c &amp; d&#160;</p></body></html>",
            "$XHTML10T\r\n<html xmlns=\"http://www.w3.org/1999/xhtml\"><body><p>&ldquo;Q&rdquo;&hellip;</p></body></html>\r\n",
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\"><html><p title=\"a&nbsp;b\">x</p></html>",
            // HTML5 doctype (no external id)
            "<!DOCTYPE html>\n<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:epub=\"http://www.idpf.org/2007/ops\"><body epub:type=\"bodymatter\"><p>x &lt; y</p></body></html>",
            "<?xml version='1.0' encoding='utf-8'?><!DOCTYPE html><html><p>&#169; 2020</p></html>",
            // NCX and OPF
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$NCX\n<ncx xmlns=\"http://www.daisy.org/z3986/2005/ncx/\" version=\"2005-1\"><head><meta name=\"dtb:uid\" content=\"u\"/></head>" +
                "<docTitle><text>Book&nbsp;One</text></docTitle><navMap><navPoint id=\"n1\" playOrder=\"1\"><navLabel><text>Ch 1</text></navLabel><content src=\"c1.xhtml\"/></navPoint></navMap></ncx>",
            "<?xml version=\"1.0\"?><package xmlns=\"http://www.idpf.org/2007/opf\" version=\"2.0\" unique-identifier=\"id\"><metadata xmlns:dc=\"http://purl.org/dc/elements/1.1/\"><dc:title>T</dc:title></metadata></package>",
            // internal subsets
            "<!DOCTYPE a [<!ENTITY e \"v\">]><a b=\"&e;!\">x</a>",
            "<!DOCTYPE a [\n  <!ELEMENT a (b , c)*>\n  <!ELEMENT b EMPTY>\n  <!-- note -->\n  <!ENTITY unused \"u&#10;v\">\n]>\n<a/>",
            "<!DOCTYPE a SYSTEM \"a.dtd\" [<!ENTITY x SYSTEM \"x.xml\"><!NOTATION n PUBLIC \"-//N//EN\"><!ENTITY u SYSTEM \"u.png\" NDATA n><!ENTITY % p \"pv\">]><a>&x;|&nbsp;</a>",
            "<!DOCTYPE html PUBLIC \"p\" \"s\" [<!ENTITY q \"]>'\">]><html>&nbsp;</html>",
            "<!DOCTYPE a [<!ENTITY e \"v\"><!ENTITY e \"w\">]><a b=\"&e;\"/>",
            "<!DOCTYPE a [<?pi in subset?>]><a/>",
            "<!DOCTYPE a []><a/>",
            "<!DOCTYPE a[]><a/>",
            "<!DOCTYPE a SYSTEM 'x'[ ]><a/>",
            // around the DOCTYPE
            "<!--before--><?p d?>\n<!DOCTYPE a PUBLIC \"  -//A  B//EN \" \"s\">\n<!--after--><a/>",
            "<!DOCTYPE b><a/>",
            "<!DOCTYPE\r\n a\r PUBLIC\r\n \"p\"\r\r \"s\"\r>\r<a>\r\r&nbsp;</a>",
            "<!DOCTYPE a SYSTEM \"s\"><a x=\"a&nbsp;b\" y=\"&amp;\"/>",
            // malformed or not allowed: rejected like SAX
            "<!DOCTYPE html><html>&nbsp;</html>",
            "<?xml version=\"1.0\" standalone=\"yes\"?>$XHTML11<html>&nbsp;</html>",
            "<!DOCTYPE a [<!ENTITY e \"v\"><a/>",
            "<!DOCTYPE ><a/>",
            "<!DOCTYPE><a/>",
            "<a/><!DOCTYPE a>",
            "<!DOCTYPE a><!DOCTYPE a><a/>",
            "<!doctype a><a/>",
            "<!DOCTYPE a PUBLIC \"p\"><a/>",
            "<!DOCTYPE a SYSTEM\"x\"><a/>",
            "<!DOCTYPE a PUBLIC \"p<\" \"s\"><a/>",
            "<!DOCTYPE a [<!ENTITY x SYSTEM \"x.xml\">]><a b=\"&x;\"/>",
            "<!DOCTYPE a [<!NOTATION n SYSTEM \"n\"><!ENTITY u SYSTEM \"u\" NDATA n>]><a>&u;</a>",
            "<!DOCTYPE a SYSTEM \"s\"><a x=\"<\"/>",
            "<!DOCTYPE a [<!ELEMENT>]><a/>",
            "<!DOCTYPE a [<!-- a -- b -->]><a/>",
            "<!DOCTYPE a [<!ENTITY e \"x\">] junk><a/>",
            "<!DOCTYPE a SYSTEM \"unterminated><a/>",
        )

        /**
         * Internal entities used in content: kdom expands them (SAX events equal). jdom2 with AndBible's configuration
         * (expansion off) builds an EntityRef instead and, on the JDK, keeps part of the replacement text (a Xerces chunking
         * artefact), so these are compared through the SAX events only.
         */
        val ENTITY_EXPANSION_INPUTS: List<String> = listOf(
            "<!DOCTYPE a [<!ENTITY e \"v\">]><a>x&e;y</a>",
            "<!DOCTYPE a [<!ENTITY e \"v&#10;w\">]><a>&e;|&e;</a>",
            "<!DOCTYPE a [<!ENTITY e \"a&amp;b&lt;\">]><a b=\"&e;\">&e;</a>",
            "<!DOCTYPE html PUBLIC \"p\" \"s\" [<!ENTITY nbsp \"&#160;\">]><html>a&nbsp;b&copy;</html>",
        )
    }
}
