package fi.sykero.kdom.diff

import fi.sykero.kdom.input.LexicalEventHandler
import fi.sykero.kdom.input.XmlEventParser
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.xml.sax.InputSource
import org.xml.sax.ext.DefaultHandler2

/**
 * The event stream of [XmlEventParser] against the JDK's JAXP SAX parser (`SAXParserFactory.newInstance()`, the
 * oracle), namespace-aware and namespace-unaware. An event is the method name with its arguments; for
 * `startElement` every attribute index with `getURI/getLocalName/getQName/getType/getValue` and the by-name lookups.
 * Character data is concatenated between two other events (chunking legally differs). Comments and CDATA boundaries
 * are compared through SAX's `LexicalHandler` and kdom's internal [LexicalEventHandler].
 *
 * [ACCEPTED] inputs must give identical event lists; [REJECTED] inputs must be rejected by both parsers (SAX with a
 * `SAXParseException`, kdom with [fi.sykero.kdom.sax.SAXParseException]); a few are mode-specific.
 */
class SaxEventDifferentialTest {

    @Test fun namespaceAwareEventsMatchSax() = compareAccepted(nsAware = true)

    @Test fun namespaceUnawareEventsMatchSax() = compareAccepted(nsAware = false)

    @Test fun namespaceAwareRejectionsMatchSax() = compareRejected(nsAware = true)

    @Test fun namespaceUnawareRejectionsMatchSax() = compareRejected(nsAware = false)

    @Test fun modeSpecificVerdictsMatchSax() {
        for ((xml, nsAccepts) in MODE_SPECIFIC) {
            for (ns in listOf(true, false)) {
                val sax = runCatching { sax(xml, ns) }
                val kdom = runCatching { kdom(xml, ns) }
                val saxOk = sax.isSuccess
                assertEquals("SAX verdict (ns=$ns) for $xml", if (ns) nsAccepts else true, saxOk)
                assertEquals("kdom verdict (ns=$ns) for $xml", saxOk, kdom.isSuccess)
                if (saxOk) assertEquals("events (ns=$ns) for $xml", sax.getOrThrow(), kdom.getOrThrow())
                else assertParseErrors(xml, sax.exceptionOrNull(), kdom.exceptionOrNull())
            }
        }
    }

    private fun compareAccepted(nsAware: Boolean) {
        val mismatches = ArrayList<String>()
        for (xml in ACCEPTED + ACCEPTED_SAX_ONLY) {
            val expected = try { sax(xml, nsAware) } catch (e: Exception) { fail("oracle rejected ${show(xml)}: $e"); return }
            val actual = try { kdom(xml, nsAware) } catch (e: Exception) { listOf("threw $e") }
            if (expected != actual) mismatches += "${show(xml)}\n  SAX : $expected\n  kdom: $actual"
        }
        if (mismatches.isNotEmpty()) fail(mismatches.size.toString() + " mismatches:\n" + mismatches.joinToString("\n"))
    }

    private fun compareRejected(nsAware: Boolean) {
        val problems = ArrayList<String>()
        for (xml in REJECTED) {
            val sax = runCatching { sax(xml, nsAware) }
            val kdom = runCatching { kdom(xml, nsAware) }
            if (sax.isSuccess) problems += "oracle accepted ${show(xml)}"
            else if (kdom.isSuccess) problems += "kdom accepted ${show(xml)}: ${kdom.getOrThrow()}"
            else if (kdom.exceptionOrNull() !is fi.sykero.kdom.sax.SAXParseException) problems += "kdom leaked ${kdom.exceptionOrNull()} for ${show(xml)}"
        }
        if (problems.isNotEmpty()) fail(problems.joinToString("\n"))
    }

    private fun assertParseErrors(xml: String, sax: Throwable?, kdom: Throwable?) {
        assertEquals("SAX error type for $xml", org.xml.sax.SAXParseException::class.java, sax?.javaClass)
        assertEquals("kdom error type for $xml", fi.sykero.kdom.sax.SAXParseException::class.java, kdom?.javaClass)
    }

    private fun show(xml: String) = xml.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }.joinToString("")

    // ---- recorders ----

    private class Events {
        val list = ArrayList<String>()
        private val text = StringBuilder()
        fun chars(ch: CharArray, start: Int, length: Int) { text.append(ch, start, length) }
        fun add(e: String) {
            if (text.isNotEmpty()) { list += "characters('${esc(text.toString())}')"; text.setLength(0) }
            list += e
        }
        fun done(): List<String> { add("<end>"); return list }

        fun element(uri: String?, localName: String?, qName: String?, n: Int, att: (Int) -> List<String?>,
                    byQName: (String) -> List<String?>, byName: (String, String) -> List<String?>): String {
            val sb = StringBuilder("startElement($uri,$localName,$qName)")
            for (i in 0 until n) {
                val a = att(i)
                sb.append(" [").append(i).append(": ").append(a.joinToString("|") { if (it == null) "null" else esc(it) })
                sb.append(" byQName=").append(byQName(a[2]!!).joinToString("|"))
                sb.append(" byName=").append(byName(a[0]!!, a[1]!!).joinToString("|"))
                sb.append("]")
            }
            return sb.toString()
        }

        companion object {
            fun esc(s: String) = s.map { if (it.code < 32 || it.code > 126) "\\u" + it.code.toString(16).padStart(4, '0') else it.toString() }.joinToString("")
        }
    }

    private fun sax(xml: String, nsAware: Boolean): List<String> {
        val ev = Events()
        val h = object : DefaultHandler2() {
            override fun setDocumentLocator(locator: org.xml.sax.Locator?) { ev.add("setDocumentLocator") }
            override fun startDocument() { ev.add("startDocument") }
            override fun endDocument() { ev.add("endDocument") }
            override fun startPrefixMapping(prefix: String, uri: String) { ev.add("startPrefixMapping($prefix,$uri)") }
            override fun endPrefixMapping(prefix: String) { ev.add("endPrefixMapping($prefix)") }
            override fun startElement(uri: String?, localName: String?, qName: String?, a: org.xml.sax.Attributes) {
                ev.add(ev.element(uri, localName, qName, a.length,
                    { i -> listOf(a.getURI(i), a.getLocalName(i), a.getQName(i), a.getType(i), a.getValue(i)) },
                    { q -> listOf(a.getIndex(q).toString(), a.getType(q), a.getValue(q)) },
                    { u, l -> listOf(a.getIndex(u, l).toString(), a.getType(u, l), a.getValue(u, l)) }))
            }
            override fun endElement(uri: String?, localName: String?, qName: String?) { ev.add("endElement($uri,$localName,$qName)") }
            override fun characters(ch: CharArray, start: Int, length: Int) { ev.chars(ch, start, length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { ev.add("ignorableWhitespace") }
            override fun processingInstruction(target: String?, data: String?) { ev.add("processingInstruction($target,'${data?.let { Events.esc(it) }}')") }
            override fun skippedEntity(name: String?) { ev.add("skippedEntity($name)") }
            override fun comment(ch: CharArray, start: Int, length: Int) { ev.add("comment('${Events.esc(String(ch, start, length))}')") }
            override fun startCDATA() { ev.add("startCDATA") }
            override fun endCDATA() { ev.add("endCDATA") }
        }
        val parser = SAXParserFactory.newInstance().apply { isNamespaceAware = nsAware }.newSAXParser()
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", h)
        parser.parse(InputSource(StringReader(xml)), h)
        return ev.done()
    }

    private fun kdom(xml: String, nsAware: Boolean): List<String> {
        val ev = Events()
        val h = object : fi.sykero.kdom.sax.ContentHandler, LexicalEventHandler {
            override fun setDocumentLocator(locator: fi.sykero.kdom.sax.Locator?) { ev.add("setDocumentLocator") }
            override fun startDocument() { ev.add("startDocument") }
            override fun endDocument() { ev.add("endDocument") }
            override fun startPrefixMapping(prefix: String, uri: String) { ev.add("startPrefixMapping($prefix,$uri)") }
            override fun endPrefixMapping(prefix: String) { ev.add("endPrefixMapping($prefix)") }
            override fun startElement(uri: String, localName: String, qName: String, atts: fi.sykero.kdom.sax.Attributes) {
                val a = atts
                ev.add(ev.element(uri, localName, qName, a.length,
                    { i -> listOf(a.getURI(i), a.getLocalName(i), a.getQName(i), a.getType(i), a.getValue(i)) },
                    { q -> listOf(a.getIndex(q).toString(), a.getType(q), a.getValue(q)) },
                    { u, l -> listOf(a.getIndex(u, l).toString(), a.getType(u, l), a.getValue(u, l)) }))
            }
            override fun endElement(uri: String, localName: String, qName: String) { ev.add("endElement($uri,$localName,$qName)") }
            override fun characters(ch: CharArray, start: Int, length: Int) { ev.chars(ch, start, length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { ev.add("ignorableWhitespace") }
            override fun processingInstruction(target: String, data: String?) { ev.add("processingInstruction($target,'${data?.let { Events.esc(it) }}')") }
            override fun skippedEntity(name: String) { ev.add("skippedEntity($name)") }
            override fun comment(ch: CharArray, start: Int, length: Int) { ev.add("comment('${Events.esc(String(ch, start, length))}')") }
            override fun startCDATA() { ev.add("startCDATA") }
            override fun endCDATA() { ev.add("endCDATA") }
        }
        XmlEventParser(nsAware).parse(xml, h)
        return ev.done()
    }

    companion object {
        /** Well-formed in both modes. */
        val ACCEPTED: List<String> = listOf(
            // the brief's inputs
            "<a x=\"&lt;&amp;&#233;\"><![CDATA[<raw>]]>t&gt;</a>",
            "<a xmlns=\"urn:d\" xmlns:p=\"urn:p\" p:x=\"1\" y=\"2\"><p:b>t&amp;u<![CDATA[c]]><!--k--><?pi d?></p:b></a>",
            "<div xmlns=\"http://www.bibletechnologies.net/2003/OSIS/namespace\"><verse osisID=\"Gen.1.1\" sID=\"Gen.1.1\"/>" +
                "<title type=\"x-gen\">The Creation</title><w lemma=\"strong:H07225 strong:H01254\" morph=\"robinson:N\">In the beginning</w> " +
                "<q who=\"Jesus\" marker=\"\" sID=\"q1\"/>Truly<note type=\"x-StudyNote\" n=\"a\" osisRef=\"Gen.1.1\">see <i>it</i> &amp; " +
                "<reference osisRef=\"John.1.1\">Jn 1:1</reference></note> I say<q eID=\"q1\" marker=\"\"/><verse eID=\"Gen.1.1\"/></div>",
            "<div><scripRef passage=\"Jn 3:16\">John 3:16</scripRef> For God so loved<sync type=\"Strongs\" value=\"G25\"/> the " +
                "<font color=\"red\">world</font><br/>\n<note n=\"1\" place=\"foot\">a <i>note</i></note></div>",
            // line ends, white space and normalisation
            "<a>x\r\ny\rz\n</a>",
            "<a b=\"x\r\ny\tz\" c=\"&#10;&#9;&#13; \" d='\"' e=\"'\"/>",
            "<a>\t \n</a>",
            "<a>\n  <b> </b>\n</a>",
            "<a\n  x=\"1\"\n  y = '2'\n/>",
            "<a  x = \"1\"  ></a >",
            // XML 1.0 keeps U+0085 / U+2028 (xmlutil would apply XML 1.1 line ends)
            "<a b=\"\u0085\u2028\">\u0085\u2028\r\u0085<![CDATA[\u0085]]><!--\u2028--><?p \u0085?></a>",
            // mask characters present in the input
            "<a b=\"\uE000\u2C00\">\uE000\uE001\u2C00 \u0085 &#xE002;</a>",
            // references
            "<a>&lt;&#x41;&apos;&quot;&gt;&amp;&#65;</a>",
            "<a x=\"&#60;&gt;>\" y=\"&#x10000; &#65536;\">&#x10000; &#65536;\uD800\uDC00</a>",
            "<a>]] ]&gt; ]]</a>",
            // processing instructions, comments, CDATA
            "<?pi   d e ?><a/>",
            "<?pi\nd?><a>x<?pi?>y<?q \t z?></a>",
            "<a><!----><!-- c --><![CDATA[]]><![CDATA[x]]]></a>",
            "<?xml version=\"1.0\"?>\n<!-- c --><?p d?><a/>\n<!--after-->  <?p2 x?>  \n",
            "<?xml version='1.0' encoding='ISO-8859-1' standalone='yes'?><a/>",
            "<?xml version=\"1.1\"?><a/>",
            // namespaces
            "<a xml:lang=\"en\" xml:space=\"preserve\"/>",
            "<xml:a/>",
            "<a xmlns:xml=\"http://www.w3.org/XML/1998/namespace\"/>",
            "<p:a xmlns:p=\"u\"><p:b/></p:a>",
            "<a xmlns:p=\"u\"><b xmlns:p=\"v\" p:x=\"1\"/><p:c/></a>",
            "<a xmlns=\"u\"><b xmlns=\"\"/><c/></a>",
            "<a xmlns:p=\"u\" p:x=\"1\" x=\"2\" xmlns:q=\"v\" q:y=\"3\"/>",
            "<a xmlns=\"\"/>",
            // text characters at the edges of the legal ranges
            "<a>\uFFFD\uD7FF\uE000 \uD83D\uDE00</a>",
            // fix round 1: lone CRs (xmlutil folded "\r\r" into one line end), everywhere a line end can occur
            "<a b='x\r\ry' c='\r\r\r' d='\r\n\r' e='\n\r'>x\r\ry|\r\r\r|\r\n\r|\n\r|\r\r\n" +
                "<![CDATA[\r\r\r\n\r]]><!--\r\r\r\n--><?p \r\r x\r\r?>\u0085\r\r\u2028\r\u0085\r\n\u2028</a>\r\r<!--\r-->\r",
            "<a\rb='1'\r\rc='2'\r/>",
            // fix round 1: U+0085 / U+2028 / CR in namespace URIs
            "<a xmlns:p=\"urn:\u0085x\" xmlns=\"u\u2028\" p:b=\"1\"><p:c/></a>",
            "<a xmlns:p=\"urn:\rx\" p:b=\"\u2028\"/>",
            // fix round 1: processing instruction targets and the XML declaration
            "<a>a<?\u00E9?>d<?xml-stylesheet?>Who?<?really?><?p\u00B7 x?></a>",
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?><a/>",
            "<?xml version='1.0'\n\tencoding='UTF-8'\r\nstandalone='no'?><a/>",
            // fix round 1: name characters at the 4th-edition table edges that the JDK accepts
            "<\u00C0\u0101 \u0E01\u0E31=\"1\" _-.\u00B7=\"2\"/>",
        )

        /**
         * Accepted by the JDK SAX parser in both modes, but jdom2's tree layer rejects them with an unchecked exception
         * (a leading `:` in a name, a `:` in a PI target, a namespace URI starting with white space); kdom's
         * `KdomBuilder` wraps that into a `JDOMParseException` (KdomBuilderDifferentialTest).
         */
        val ACCEPTED_SAX_ONLY: List<String> = listOf(
            "<:a/>",
            "<a :b='1'/>",
            "<?p:i d?><a/>",
            "<a xmlns:p=\" urn:p\"/>",
        )

        /** Malformed in both modes (SAX rejects in both; kdom must reject too). */
        val REJECTED: List<String> = fi.sykero.kdom.probe.ProbeInputs.probes
            .filterKeys { it !in setOf("valid control", "undeclared prefix", "doctype with entity decl", "plain doctype") }
            .values.toList() + listOf(
            "<a>]]></a>",
            "<a x=\"1\"y=\"2\"/>",
            "<a xmlns:p=\"u\" xmlns:p=\"v\"/>",
            "<a/><?xml version=\"1.0\"?>",
            " <?xml version=\"1.0\"?><a/>",
            "<?XML version=\"1.0\"?><a/>",
            "<a><?xml x?></a>",
            "<?xml version=\"2.0\"?><a/>",
            "<?xml encoding=\"UTF-8\"?><a/>",
            "<?xml version=\"1.0\" standalone=\"maybe\"?><a/>",
            "\uFEFF<a/>",
            "x<a/>",
            "<a>\uD83D</a>",
            "<a>\uDE00x</a>",
            "<a x=\"\uD83D\"/>",
            "<a>\uFFFE</a>",
            "<a>\uFFFF</a>",
            "<a x=\"\u0001\"/>",
            "<a><![CDATA[\u0001]]></a>",
            "<a><!--\u0001--></a>",
            "<?pi \u0001?><a/>",
            "<a>&#0;</a>",
            "<a>&#xD800;</a>",
            "<a>&#55357;&#56832;</a>",
            "<a x=\"&#1;\"/>",
            "<a>&#x;</a>",
            "<a>&#;</a>",
            "<a>&#12a;</a>",
            "<a>&amp</a>",
            "<a>&#x41</a>",
            "<a><!-- x -- y --></a>",
            "<a><!-- a ---></a>",
            "<a><!---></a>",
            "<a/ >",
            "< a/>",
            "<1a/>",
            "<a 1x=\"2\"/>",
            "<a x/>",
            "<a x=/>",
            "<a></a",
            "<a><b></a></b>",
            "<p:a xmlns:p=\"u\"></q:a>",
            "<?pi?>",
            "<!ELEMENT a ANY><a/>",
            "<a><!ELEMENT a ANY></a>",
            "<a><![CDATA[x</a>",
            "<a><?pi x</a>",
            "<a>&nbsp;</a>",
            "<a>&unknown;</a>",
            "<a x=\"&lt\"/>",
            "<a x=\"a&b\"/>",
            "<a>\u0000</a>",
            "",
            "   ",
            // fix round 1: processing instruction targets (JDK: white space required after the target / name expected)
            "<a><?p!x?></a>",
            "<a><?p=?></a>",
            "<?b,c?><a/>",
            "<?p\"d?><a/>",
            "<a><??></a>",
            "<a><? x?></a>",
            "<a><?1?></a>",
            "<a><?-x?></a>",
            "<a>what<?</a>",
            // fix round 1: white space inside the XML declaration
            "<?xml version=\"1.0\"encoding=\"UTF-8\"?><a/>",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"standalone=\"yes\"?><a/>",
            "<?xml version='1.0'standalone='yes'?><a/>",
            // fix round 1: name characters outside the XML 1.0 4th-edition tables (xmlutil uses the 5th edition)
            "<\u037E/>",
            "<a\u037E/>",
            "<\u2C01/>",
            "<a\u2C01/>",
            "<\u0E00/>",
            "<a\u0F00/>",
            "<a \u037E=\"1\"/>",
            "<a\uD800\uDC00/>",
            "<a>x</a\u037E>",
        )

        /** Input to SAX verdict in namespace-aware mode (namespace-unaware SAX accepts all of these). */
        val MODE_SPECIFIC: List<Pair<String, Boolean>> = listOf(
            "<p:a/>" to false,
            "<a:b:c xmlns:a=\"u\"/>" to false,
            "<a xmlns:p=\"u\" xmlns:q=\"u\" p:x=\"1\" q:x=\"2\"/>" to false,
            "<a xmlns:p=\"\"/>" to false,
            "<a xmlns:xmlns=\"u\"/>" to false,
            "<a xmlns:p=\"http://www.w3.org/XML/1998/namespace\"/>" to false,
            "<a xmlns:xml=\"urn:other\"/>" to false,
            "<a p:x=\"1\"/>" to false,
            "<a><b:c xmlns:b=\"u\" b:d=\"1\"/><b:e/></a>" to false,
            "<a xmlns:p=\"u\"><p:b x:y=\"1\" xmlns:x=\"v\" xmlns=\"w\"/></a>" to true,
            // fix round 1: QName rules of the JDK parser
            "<a p::b=\"1\" xmlns:p=\"u\"/>" to false,
            "<a xml::ang=\"1\"/>" to false,
            "<a b:=\"1\"/>" to false,
            "<a: />" to false,
            "<a xmlns::p=\"u\"/>" to false,
            "<xmlns:a/>" to false,
            "<xmlns:a xmlns:xmln=\"u\"/>" to false,
            "<a xmlns:a=\"u\"><xmlns:b/></a>" to false,
            "<p:xmlns xmlns:p=\"u\"/>" to true,
            "<a xml:xmlns=\"1\"/>" to true,
            "<a:b xmlns:a=\"u\" a:c=\"1\" xmlns=\"d\" xmlns:x=\"y\" x:z=\"2\" plain=\"3\"/>" to true,
        )
    }
}
