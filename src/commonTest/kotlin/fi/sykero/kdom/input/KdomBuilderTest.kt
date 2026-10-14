package fi.sykero.kdom.input

import fi.sykero.kdom.AttributeType
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.sax.SAXParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** One test per jdom2 `SAXBuilder` tree-building rule that [KdomBuilder] reproduces, plus byte input and errors. */
class KdomBuilderTest {
    private fun build(xml: String) = KdomBuilder().build(xml)
    private fun raw(e: Element) = XMLOutputter(Format.getRawFormat()).outputString(e)

    @Test fun adjacentCharacterEventsMergeIntoOneText() {
        val root = build("<a>x&amp;y&#65;&#x42;&lt;z&gt;\r\nw</a>").rootElement
        assertEquals(1, root.contentSize)
        val t = assertIs<Text>(root.getContent(0))
        assertEquals("x&yAB<z>\nw", t.text)
    }

    @Test fun cdataSectionsAreSeparateCdataNodes() {
        val root = build("<a>x<![CDATA[<c>]]>y<![CDATA[]]><![CDATA[d]]></a>").rootElement
        val kinds = root.content.map { it::class.simpleName + ":" + it.value }
        // jdom2 quirk kept: text directly before an *empty* CDATA section is flushed as that CDATA ("y"), because
        // SAXHandler only flushes pending text at the first character inside the section (pinned against jdom2 by
        // KdomBuilderDifferentialTest).
        assertEquals(listOf("Text:x", "CDATA:<c>", "CDATA:y", "CDATA:d"), kinds)
        assertIs<CDATA>(root.getContent(1))
        assertEquals(listOf("CDATA:"), build("<a><![CDATA[]]></a>").rootElement.content.map { it::class.simpleName + ":" + it.value })
    }

    @Test fun commentsAndProcessingInstructionsAreKeptInsideAndOutsideTheRoot() {
        val doc = build("<!--c1--><?p d?>\n<a>t<!--c2--><?q?>u</a>\n<!--c3--><?r  x  y?>")
        val top = doc.content
        assertEquals(5, top.size)
        assertEquals("c1", assertIs<Comment>(top[0]).text)
        assertEquals("p", assertIs<ProcessingInstruction>(top[1]).target)
        assertEquals("d", (top[1] as ProcessingInstruction).data)
        assertIs<Element>(top[2])
        assertEquals("c3", assertIs<Comment>(top[3]).text)
        assertEquals("x  y", assertIs<ProcessingInstruction>(top[4]).data)
        val inner = doc.rootElement.content.map { it::class.simpleName + ":" + it.value }
        assertEquals(listOf("Text:t", "Comment:c2", "ProcessingInstruction:", "Text:u"), inner)
    }

    @Test fun emptyCommentsAreDroppedAsInJdom2() {
        assertEquals(0, build("<a><!----></a>").rootElement.contentSize)
    }

    @Test fun whitespaceOnlyTextIsKept() {
        val root = build("<a> <b/>\n\t</a>").rootElement
        assertEquals(listOf("Text: ", "Element:", "Text:\n\t"), root.content.map { it::class.simpleName + ":" + it.value })
    }

    @Test fun namespaceDeclarationsBecomeNamespacesNeverAttributes() {
        val root = build("<p:a xmlns:p=\"urn:p\" xmlns=\"urn:d\" xmlns:q=\"urn:q\" q:x=\"1\" y=\"2\"><b/><p:c/></p:a>").rootElement
        assertEquals(Namespace.getNamespace("p", "urn:p"), root.namespace)
        assertEquals(listOf(Namespace.getNamespace("urn:d"), Namespace.getNamespace("q", "urn:q")), root.additionalNamespaces)
        assertEquals(listOf("q:x", "y"), root.attributes.map { it.qualifiedName })
        assertEquals("urn:q", root.attributes[0].namespaceURI)
        assertEquals(Namespace.getNamespace("urn:d"), root.children[0].namespace)
        assertEquals(Namespace.getNamespace("p", "urn:p"), root.children[1].namespace)
        assertTrue(root.children[0].additionalNamespaces.isEmpty())
    }

    @Test fun attributeOrderIsDocumentOrderAndTypeIsCdata() {
        val root = build("<a z=\"1\" a=\"2\" m=\"3\"/>").rootElement
        assertEquals(listOf("z", "a", "m"), root.attributes.map { it.name })
        assertTrue(root.attributes.all { it.attributeType == AttributeType.CDATA && it.isSpecified })
    }

    @Test fun xmlDeclarationIsNotPartOfTheTree() {
        val doc = build("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<a/>")
        assertEquals(1, doc.contentSize)
        assertNull(doc.docType)
    }

    @Test fun xmlPrefixUsesTheXmlNamespace() {
        val root = build("<a xml:lang=\"fi\" xml:space=\"preserve\"> </a>").rootElement
        assertEquals(Namespace.XML_NAMESPACE, root.getAttribute("lang", Namespace.XML_NAMESPACE)!!.namespace)
        assertEquals("fi", root.getAttributeValue("lang", Namespace.XML_NAMESPACE))
    }

    @Test fun doctypeIsRejected() {
        val e = assertFailsWith<JDOMParseException> { build("<!DOCTYPE a [<!ENTITY e \"v\">]><a>&e;</a>") }
        assertIs<SAXParseException>(e.cause)
        assertFailsWith<JDOMParseException> { build("<!DOCTYPE a><a/>") }
    }

    @Test fun surrogatePairsFromReferencesAndRawCharacters() {
        val root = build("<a x=\"&#x10000;\">&#65536;\uD83D\uDE00</a>").rootElement
        assertEquals("\uD800\uDC00", root.getAttributeValue("x"))
        assertEquals("\uD800\uDC00\uD83D\uDE00", root.text)
    }

    @Test fun malformedInputThrowsJdomParseExceptionWithPosition() {
        val e = assertFailsWith<JDOMParseException> { build("<a>\n<b>\n</a>") }
        assertTrue(e.lineNumber > 0, "line ${e.lineNumber}")
        assertEquals(3, e.lineNumber)
        assertTrue(e.columnNumber > 0)
        val cause = assertIs<SAXParseException>(e.cause)
        assertEquals(cause.lineNumber, e.lineNumber)
        assertTrue(e.message!!.startsWith("Error on line 3: "), e.message)
    }

    @Test fun preScanErrorsCarryTheirPosition() {
        val e = assertFailsWith<JDOMParseException> { build("<a>\n  <b x=\"<\"/></a>") }
        assertEquals(2, e.lineNumber)
        assertEquals(9, e.columnNumber) // the offending character (SAX reports the column after it)
    }

    @Test fun partialDocumentIsKeptWhenTheRootWasStarted() {
        val e = assertFailsWith<JDOMParseException> { build("<a><b/>t</c>") }
        val partial = assertNotNull(e.partialDocument)
        assertEquals("a", partial.rootElement.name)
        assertEquals(1, partial.rootElement.children.size)
        assertNull(assertFailsWith<JDOMParseException> { build("") }.partialDocument)
    }

    @Test fun aBuilderCanBeReused() {
        val b = KdomBuilder()
        assertFailsWith<JDOMParseException> { b.build("<a>") }
        assertEquals("<a><b>1</b></a>", raw(b.build("<a><b>1</b></a>").rootElement))
        assertEquals("<c />", raw(b.build("<c/>").rootElement))
    }

    @Test fun bytesWithUtf8Bom() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "<a>\u00E9\u20AC\uD83D\uDE00</a>".encodeToByteArray()
        assertEquals("\u00E9\u20AC\uD83D\uDE00", KdomBuilder().build(bytes).rootElement.text)
    }

    @Test fun bytesWithoutBomAreUtf8() {
        assertEquals("\u00E9", KdomBuilder().build("<?xml version=\"1.0\"?><a>\u00E9</a>".encodeToByteArray()).rootElement.text)
    }

    @Test fun bytesWithUtf16LeBom() {
        val s = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><a>\u00E9\uD83D\uDE00</a>"
        val bytes = ArrayList<Byte>()
        bytes += 0xFF.toByte(); bytes += 0xFE.toByte()
        for (c in s) { bytes += (c.code and 0xFF).toByte(); bytes += (c.code shr 8).toByte() }
        assertEquals("\u00E9\uD83D\uDE00", KdomBuilder().build(bytes.toByteArray()).rootElement.text)
    }

    @Test fun bytesWithUtf16BeBom() {
        val s = "<a>\u00E9</a>"
        val bytes = ArrayList<Byte>()
        bytes += 0xFE.toByte(); bytes += 0xFF.toByte()
        for (c in s) { bytes += (c.code shr 8).toByte(); bytes += (c.code and 0xFF).toByte() }
        assertEquals("\u00E9", KdomBuilder().build(bytes.toByteArray()).rootElement.text)
    }

    @Test fun bytesWithIso88591Declaration() {
        val head = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>".encodeToByteArray()
        val bytes = head + byteArrayOf(0xE9.toByte()) + "</a>".encodeToByteArray()
        assertEquals("\u00E9", KdomBuilder().build(bytes).rootElement.text)
    }

    @Test fun bytesWithUsAsciiDeclaration() {
        val ok = "<?xml version='1.0' encoding='US-ASCII'?><a>x&#233;</a>".encodeToByteArray()
        assertEquals("x\u00E9", KdomBuilder().build(ok).rootElement.text)
        val bad = "<?xml version='1.0' encoding='US-ASCII'?><a>".encodeToByteArray() + byteArrayOf(0xE9.toByte()) + "</a>".encodeToByteArray()
        assertFailsWith<JDOMParseException> { KdomBuilder().build(bad) }
    }

    @Test fun malformedUtf8IsRejected() {
        val cases = listOf(
            byteArrayOf(0xE9.toByte()), // Latin-1 byte without a declaration
            byteArrayOf(0xC0.toByte(), 0xAF.toByte()), // overlong '/'
            byteArrayOf(0xED.toByte(), 0xA0.toByte(), 0x80.toByte()), // encoded surrogate
            byteArrayOf(0xF4.toByte(), 0x90.toByte(), 0x80.toByte(), 0x80.toByte()), // above U+10FFFF
            byteArrayOf(0xE2.toByte(), 0x82.toByte()), // truncated
        )
        for (c in cases) {
            val bytes = "<a>\n".encodeToByteArray() + c + "</a>".encodeToByteArray()
            val e = assertFailsWith<JDOMParseException> { KdomBuilder().build(bytes) }
            assertEquals(2, e.lineNumber)
        }
    }

    @Test fun loneCarriageReturnsAreEachALineEnd() {
        // fix round 1: xmlutil folded "\r\r" into one line end; SAX normalizes each lone CR to LF (a space in attributes).
        val root = build("<a b='x\r\ry' c='\r\n\r'>x\r\ry\r\r\r<![CDATA[\r\r]]><!--\r\r--></a>").rootElement
        assertEquals("x  y", root.getAttributeValue("b"))
        assertEquals("  ", root.getAttributeValue("c"))
        assertEquals(listOf("Text:x\n\ny\n\n\n", "CDATA:\n\n", "Comment:\n\n"), root.content.map { it::class.simpleName + ":" + it.value })
        assertEquals(3, assertFailsWith<JDOMParseException> { build("<a>\r\r</b>") }.lineNumber)
    }

    @Test fun namespaceUrisKeepNelAndLineSeparator() {
        val root = build("<a xmlns=\"u\u2028\" xmlns:p=\"urn:\u0085x\" p:b=\"1\"/>").rootElement
        assertEquals("u\u2028", root.namespaceURI)
        assertEquals("urn:\u0085x", root.attributes[0].namespaceURI)
    }

    @Test fun treeLayerFailuresAreWrappedInJdomParseException() {
        // fix round 1: accepted by the parser (as by SAX), rejected by kdom's Namespace/Element/ProcessingInstruction
        // checks; never an unchecked exception (JSword's fallback chain catches JDOMException).
        val ns = assertFailsWith<JDOMParseException> { build("<a>\n<b xmlns:p=\" urn:p\"/></a>") }
        assertIs<fi.sykero.kdom.IllegalNameException>(ns.cause)
        assertEquals(2, ns.lineNumber)
        assertEquals("a", assertNotNull(ns.partialDocument).rootElement.name)
        assertIs<fi.sykero.kdom.IllegalTargetException>(assertFailsWith<JDOMParseException> { build("<?p:i d?><a/>") }.cause)
        assertIs<fi.sykero.kdom.IllegalNameException>(assertFailsWith<JDOMParseException> { build("<:a/>") }.cause)
    }

    @Test fun declarationWinsOverUtf8Bom() {
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) +
            "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>".encodeToByteArray() + byteArrayOf(0xE9.toByte()) + "</a>".encodeToByteArray()
        assertEquals("\u00E9", KdomBuilder().build(bytes).rootElement.text)
    }

    @Test fun utf16BomWithAConflictingDeclarationIsRejected() {
        fun le(s: String): ByteArray {
            val out = ArrayList<Byte>()
            out += 0xFF.toByte(); out += 0xFE.toByte()
            for (c in s) { out += (c.code and 0xFF).toByte(); out += (c.code shr 8).toByte() }
            return out.toByteArray()
        }
        assertEquals("x", KdomBuilder().build(le("<?xml version=\"1.0\" encoding=\"UTF-16LE\"?><a>x</a>")).rootElement.text)
        assertFailsWith<JDOMParseException> { KdomBuilder().build(le("<?xml version=\"1.0\" encoding=\"UTF-8\"?><a/>")) }
        assertFailsWith<JDOMParseException> { KdomBuilder().build(le("<?xml version=\"1.0\" encoding=\"UTF-16BE\"?><a/>")) }
    }

    // ---- permissive DOCTYPE mode (R11) ----

    private fun buildEpub(xml: String) = KdomBuilder(allowDocType = true).build(xml)

    @Test fun permissiveModeBuildsTheDocTypeLikeJdom2() {
        val doc = buildEpub("<!--c-->\n<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\n<html/>")
        assertEquals(listOf("Comment", "DocType", "Element"), doc.content.map { it::class.simpleName })
        val dt = assertNotNull(doc.docType)
        assertEquals("html", dt.elementName)
        assertEquals("-//W3C//DTD XHTML 1.1//EN", dt.publicID)
        assertEquals("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd", dt.systemID)
        assertEquals("", dt.internalSubset)
        val html5 = assertNotNull(buildEpub("<!DOCTYPE html><html/>").docType)
        assertNull(html5.publicID)
        assertNull(html5.systemID)
    }

    @Test fun undeclaredEntitiesUnderAnUnreadExternalSubsetBecomeEntityRefs() {
        val root = buildEpub("<!DOCTYPE html SYSTEM \"x.dtd\"><html t=\"a&nbsp;b\">x&nbsp;y&copy;</html>").rootElement
        assertEquals(listOf("Text:x", "EntityRef:nbsp", "Text:y", "EntityRef:copy"),
            root.content.map { it::class.simpleName + ":" + (if (it is fi.sykero.kdom.EntityRef) it.name else it.value) })
        assertEquals("ab", root.getAttributeValue("t")) // dropped from the attribute value, as by the JDK parser
        // no external id, or standalone="yes": an undeclared entity stays an error
        assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE html><html>&nbsp;</html>") }
        assertFailsWith<JDOMParseException> { buildEpub("<?xml version=\"1.0\" standalone=\"yes\"?><!DOCTYPE html SYSTEM \"x\"><html>&nbsp;</html>") }
        assertFailsWith<JDOMParseException> { buildEpub("<html>&nbsp;</html>") }
    }

    @Test fun internalSubsetEntitiesAreExpandedAndTheSubsetIsKept() {
        val doc = buildEpub("<!DOCTYPE a [\n<!ENTITY e \"v&#233;\">\n<!ELEMENT a (#PCDATA | b)*>\n<!-- k -->\n]><a x=\"&e;\">1&e;2</a>")
        assertEquals("1v\u00E92", doc.rootElement.text)
        assertEquals("v\u00E9", doc.rootElement.getAttributeValue("x"))
        assertEquals("  <!ENTITY e \"v\u00E9\">\n  <!ELEMENT a (#PCDATA|b)*>\n  <!-- k -->\n", doc.docType!!.internalSubset)
    }

    @Test fun unsupportedInternalSubsetFeaturesAreRejected() {
        assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a [<!ATTLIST a b CDATA \"d\">]><a/>") }
        assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a [<!ENTITY % p \"x\"> %p;]><a/>") }
        assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a [<!ENTITY e \"<b/>\">]><a>&e;</a>") }
    }

    @Test fun elementDeclarationsAreValidatedAndCdataEndInEntityTextIsRejected() {
        // round 2 (d): content-model syntax as SAX; the model is kept with white space removed (as SAX reports it)
        assertEquals("  <!ELEMENT a (b,c)*>\n  <!ELEMENT b EMPTY>\n",
            buildEpub("<!DOCTYPE a [<!ELEMENT a ( b , c )*><!ELEMENT b EMPTY>]><a/>").docType!!.internalSubset)
        for (bad in listOf("(b", "EMPTY ANY", "(#PCDATA|b)", "(b|c,d)", "()")) {
            assertFailsWith<JDOMParseException>(bad) { buildEpub("<!DOCTYPE a [<!ELEMENT a $bad>]><a/>") }
        }
        // round 2 (e): "]]>" from an internal entity is an error in content, not in an attribute value
        assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a [<!ENTITY e \"x]]>y\">]><a>&e;</a>") }
        assertEquals("x]]>y", buildEpub("<!DOCTYPE a [<!ENTITY e \"x]]>y\">]><a b=\"&e;\"/>").rootElement.getAttributeValue("b"))
    }

    @Test fun positionsAfterADocTypeAreUnchanged() {
        val e = assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a\n PUBLIC \"p\"\n \"s\" [\n]\n>\n<a>\n</b>") }
        assertEquals(7, e.lineNumber)
        val p = assertFailsWith<JDOMParseException> { buildEpub("<!DOCTYPE a [<!ENTITY q \"<\">]>\n<a x=\"<\"/>") }
        assertEquals(2, p.lineNumber) // the raw '<' in the attribute (R7), not the one inside the entity value
    }

    @Test fun strictModeIsTheDefault() {
        assertFalse(KdomBuilder().allowDocType)
        assertFailsWith<JDOMParseException> { KdomBuilder().build("<!DOCTYPE html><html/>") }
        val b = KdomBuilder(allowDocType = true)
        assertFailsWith<JDOMParseException> { b.build("<!DOCTYPE a><!DOCTYPE a><a/>") }
        assertEquals("a", b.build("<!DOCTYPE a><a/>").docType!!.elementName) // reusable after a failure
        assertNull(b.build("<a/>").docType)
    }

    @Test fun windows1252AndJdkEncodingAliases() {
        val head = "<?xml version=\"1.0\" encoding=\"windows-1252\"?><a>".encodeToByteArray()
        val bytes = head + byteArrayOf(0x80.toByte(), 0x93.toByte(), 0xE9.toByte(), 0x81.toByte()) + "</a>".encodeToByteArray()
        assertEquals("\u20AC\u201C\u00E9\uFFFD", KdomBuilder().build(bytes).rootElement.text)
        // the Java alias "utf8" decodes leniently (U+FFFD), "UTF-8" strictly, as the JDK parser
        val bad = byteArrayOf(0xE9.toByte())
        assertEquals("\uFFFD", KdomBuilder().build("<?xml version='1.0' encoding='utf8'?><a>".encodeToByteArray() + bad + "</a>".encodeToByteArray()).rootElement.text)
        assertFailsWith<JDOMParseException> { KdomBuilder().build("<?xml version='1.0' encoding='utf-8'?><a>".encodeToByteArray() + bad + "</a>".encodeToByteArray()) }
        assertEquals("\u00E9", KdomBuilder().build("<?xml version='1.0' encoding='iso_8859_1'?><a>".encodeToByteArray() + bad + "</a>".encodeToByteArray()).rootElement.text)
    }

    @Test fun unsupportedEncodingIsRejected() {
        assertFailsWith<JDOMParseException> {
            KdomBuilder().build("<?xml version=\"1.0\" encoding=\"x-unknown\"?><a/>".encodeToByteArray())
        }
    }
}
