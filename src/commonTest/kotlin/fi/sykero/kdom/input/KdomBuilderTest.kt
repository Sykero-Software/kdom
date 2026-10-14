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
        val root = build("<a x=\"&#x10000;\">&#65536;😀</a>").rootElement
        assertEquals("𐀀", root.getAttributeValue("x"))
        assertEquals("𐀀😀", root.text)
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
        val bytes = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()) + "<a>é€😀</a>".encodeToByteArray()
        assertEquals("é€😀", KdomBuilder().build(bytes).rootElement.text)
    }

    @Test fun bytesWithoutBomAreUtf8() {
        assertEquals("é", KdomBuilder().build("<?xml version=\"1.0\"?><a>é</a>".encodeToByteArray()).rootElement.text)
    }

    @Test fun bytesWithUtf16LeBom() {
        val s = "<?xml version=\"1.0\" encoding=\"UTF-16\"?><a>é😀</a>"
        val bytes = ArrayList<Byte>()
        bytes += 0xFF.toByte(); bytes += 0xFE.toByte()
        for (c in s) { bytes += (c.code and 0xFF).toByte(); bytes += (c.code shr 8).toByte() }
        assertEquals("é😀", KdomBuilder().build(bytes.toByteArray()).rootElement.text)
    }

    @Test fun bytesWithUtf16BeBom() {
        val s = "<a>é</a>"
        val bytes = ArrayList<Byte>()
        bytes += 0xFE.toByte(); bytes += 0xFF.toByte()
        for (c in s) { bytes += (c.code shr 8).toByte(); bytes += (c.code and 0xFF).toByte() }
        assertEquals("é", KdomBuilder().build(bytes.toByteArray()).rootElement.text)
    }

    @Test fun bytesWithIso88591Declaration() {
        val head = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><a>".encodeToByteArray()
        val bytes = head + byteArrayOf(0xE9.toByte()) + "</a>".encodeToByteArray()
        assertEquals("é", KdomBuilder().build(bytes).rootElement.text)
    }

    @Test fun bytesWithUsAsciiDeclaration() {
        val ok = "<?xml version='1.0' encoding='US-ASCII'?><a>x&#233;</a>".encodeToByteArray()
        assertEquals("xé", KdomBuilder().build(ok).rootElement.text)
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

    @Test fun unsupportedEncodingIsRejected() {
        assertFailsWith<JDOMParseException> {
            KdomBuilder().build("<?xml version=\"1.0\" encoding=\"windows-1252\"?><a/>".encodeToByteArray())
        }
    }
}
