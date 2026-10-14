/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.output

import kotlin.test.*
import kotlin.reflect.KClass
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.JDOMException
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.Format.TextMode
import fi.sykero.kdom.output.support.AbstractXMLOutputProcessor
import fi.sykero.kdom.output.support.XMLOutputProcessor
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.test.util.outputViaStream

/* Please run replic.pl on me ! */
/**
 * Please put a description of your test here.
 * 
 * @author unascribed
 * @version 0.1
 */


class TestXMLOutputter : AbstractTestOutputter(true, true, false, false, false) {

    

    
    private fun getOutputter(format: Format): XMLOutputter {
    	return XMLOutputter(format)
    }

    override fun outputDocumentAsString(format: Format, doc: Document): String {
		return getOutputter(format).outputString(doc)
	}

	override fun outputDocTypeAsString(format: Format, doctype: DocType): String {
		return getOutputter(format).outputString(doctype)
	}

	override fun outputElementAsString(format: Format, element: Element): String {
		return getOutputter(format).outputString(element)
	}

	override fun outputListAsString(format: Format, list: List<Content>): String {
		return getOutputter(format).outputString(list)
	}

	override fun outputCDataAsString(format: Format, cdata: CDATA): String {
		return getOutputter(format).outputString(cdata)
	}

	override fun outputTextAsString(format: Format, text: Text): String {
		return getOutputter(format).outputString(text)
	}

	override fun outputCommentAsString(format: Format, comment: Comment): String {
		return getOutputter(format).outputString(comment)
	}

	override fun outputPIAsString(format: Format, pi: ProcessingInstruction): String {
		return getOutputter(format).outputString(pi)
	}

	override fun outputEntityRefAsString(format: Format, entity: EntityRef): String {
		return getOutputter(format).outputString(entity)
	}

	override fun outputElementContentString(format: Format, element: Element): String {
		return getOutputter(format).outputElementContentString(element)
	}


    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_HighSurrogatePair() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root>&#x10000; &#x10000;</root>")
      val format = Format.getCompactFormat().setEncoding("ISO-8859-1")
      val outputter = XMLOutputter(format)
      val sw = StringBuilder()
      outputter.output(doc, sw)
      val xml = sw.toString()
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.lineSeparator +
                   "<root>&#x10000; &#x10000;</root>" + format.lineSeparator, xml)
    }

    /**
     * K5: placeholder for the SAXBuilder parse of the original jdom2 tests. Task 9 replaces the body with a
     * KdomBuilder parse (expanding entities) and removes the {@code @Ignore} markers.
     */
    private /*STATIC*/ fun parseWithKdomBuilder(xml: String): Document {
      throw UnsupportedOperationException("needs KdomBuilder, Task 9")
    }


    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_HighSurrogatePairDecimal() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root>&#x10000; &#65536;</root>")
      val format = Format.getCompactFormat().setEncoding("ISO-8859-1")
      val outputter = XMLOutputter(format)
      val xml = outputToString(outputter, doc) // K5: OutputStream form on the JVM, Appendable form elsewhere
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.lineSeparator +
                   "<root>&#x10000; &#x10000;</root>" + format.lineSeparator, xml)
    }

    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_HighSurrogateAttPair() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root att=\"&#x10000; &#x10000;\" />")
      val format = Format.getCompactFormat().setEncoding("ISO-8859-1")
      val outputter = XMLOutputter(format)
      val xml = outputToString(outputter, doc) // K5: OutputStream form on the JVM, Appendable form elsewhere
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.lineSeparator +
                   "<root att=\"&#x10000; &#x10000;\" />" + format.lineSeparator, xml)
    }

    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_HighSurrogateAttPairDecimal() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root att=\"&#x10000; &#65536;\" />")
      val format = Format.getCompactFormat().setEncoding("ISO-8859-1")
      val outputter = XMLOutputter(format)
      val xml = outputToString(outputter, doc) // K5: OutputStream form on the JVM, Appendable form elsewhere
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.lineSeparator +
                   "<root att=\"&#x10000; &#x10000;\" />" + format.lineSeparator, xml)
    }

    // Construct a raw surrogate pair character and confirm it outputs hex escaped
    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_RawSurrogatePair() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root>\uD800\uDC00</root>")
      val format = Format.getCompactFormat().setEncoding("ISO-8859-1")
      val outputter = XMLOutputter(format)
      val xml = outputToString(outputter, doc) // K5: OutputStream form on the JVM, Appendable form elsewhere
      assertEquals("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + format.lineSeparator +
                   "<root>&#x10000;</root>" + format.lineSeparator, xml)
    }

    // Construct a raw surrogate pair character and confirm it outputs hex escaped, when UTF-8 too
    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_RawSurrogatePairUTF8() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root>\uD800\uDC00</root>")
      val format = Format.getCompactFormat().setEncoding("UTF-8")
      val outputter = XMLOutputter(format)
      val xml = outputToString(outputter, doc) // K5: OutputStream form on the JVM, Appendable form elsewhere
      assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + format.lineSeparator +
                   "<root>&#x10000;</root>" + format.lineSeparator, xml)
    }

    // Construct illegal XML and check if the parser notices
    // K5: needs KdomBuilder (Task 9)
    @Ignore /* needs KdomBuilder, Task 9 */ @Test fun test_ErrorSurrogatePair() {
      val doc = parseWithKdomBuilder("<?xml version=\"1.0\"?><root></root>")
      assertFailsWith<IllegalDataException>("Illegal surrogate pair should have thrown an exception") {
        doc.rootElement.setText("\uD800\uDBFF")
      }
    }

	@Test fun testXMLOutputter() {
		val out = XMLOutputter()
		TestFormat.checkEquals(out.format, Format.getRawFormat())
	}


	@Test fun testXMLOutputterFormat() {
		val mine = Format.getCompactFormat()
		mine.setEncoding("US-ASCII")
		val out = XMLOutputter(mine)
		TestFormat.checkEquals(mine, out.format)
	}

	@Test fun testXMLOutputterXMLOutputter() {
		val mine = Format.getCompactFormat()
		val xoutp = XMLOutputter().xmlOutputProcessor
		mine.setEncoding("US-ASCII")
		// double-constcut it.
		val out = XMLOutputter(XMLOutputter(mine))
		TestFormat.checkEquals(mine, out.format)
		assertTrue(xoutp === out.xmlOutputProcessor)
	}

	@Test fun testXMLOutputterXMLOutputProcessor() {
		val xoutp = object : AbstractXMLOutputProcessor() {
			// nothing;
		}
		// double-constrcut it.
		val out = XMLOutputter(xoutp)
		TestFormat.checkEquals(Format.getRawFormat(), out.format)
		assertTrue(xoutp === out.xmlOutputProcessor)
	}

	@Test fun testFormat() {
		val mine = Format.getCompactFormat()
		mine.setEncoding("US-ASCII")
		// double-constcut it.
		val out = XMLOutputter()
		TestFormat.checkEquals(Format.getRawFormat(), out.format)
		out.format = mine
		TestFormat.checkEquals(mine, out.format)
	}

	@Test fun testXMLOutputProcessor() {
		val xoutp = object : AbstractXMLOutputProcessor() {
			// nothing;
		}
		// double-constcut it.
		val out = XMLOutputter()
		val xop = out.xmlOutputProcessor
		out.xmlOutputProcessor = xoutp
		assertTrue(xoutp !== xop)
		assertTrue(xoutp === out.xmlOutputProcessor)
	}

	@Test fun testEscapeAttributeEntities() {
		val totest: MutableMap<String, String> = LinkedHashMap<String, String>()
		
		totest.put("\"", "&quot;")
		totest.put("&", "&amp;")
		totest.put("<", "&lt;")
		totest.put(">", "&gt;")
		totest.put("\t", "&#x9;")
		totest.put("\r", "&#xD;")
		totest.put("\n", "&#xA;")
		totest.put("\nz\n\n", "&#xA;z&#xA;&#xA;")
		
		totest.put("'", "'")

		totest.put("Frodo's Journey", "Frodo's Journey")
		
		
		val out = XMLOutputter()
		
		for (me in totest.entries) {
			if (!me.value.equals(out.escapeAttributeEntities(me.key))) {
				assertEquals(me.value, out.escapeAttributeEntities(me.key), "Failed attempt to escape '" + me.key + "'")
			}
		}
	}

	@Test fun testEscapeElementEntities() {
		val totest: MutableMap<String, String> = LinkedHashMap<String, String>()
		
		totest.put("\"", "\"")
		totest.put("&", "&amp;")
		totest.put("<", "&lt;")
		totest.put(">", "&gt;")
		totest.put("> >>", "&gt; &gt;&gt;")
		totest.put("\t", "\t")
		totest.put("\r", "&#xD;")
		totest.put("\n", "\r\n")
		
		totest.put("'", "'")

		totest.put("Frodo's Journey", "Frodo's Journey")
		
		
		val out = XMLOutputter()
		
		for (me in totest.entries) {
			if (!me.value.equals(out.escapeElementEntities(me.key))) {
				assertEquals(me.value, out.escapeElementEntities(me.key), "Failed attempt to escape '" + me.key + "'")
			}
		}
	}


	@Test fun testTrimFullWhite() {
		// See issue #31.
		// https://github.com/hunterhacker/jdom/issues/31
		// This tests should pass when issue 31 is resolved.
		val root = Element("root")
		root.addContent(Text(" "))
		root.addContent(Text("x"))
		root.addContent(Text(" "))
		val mf = Format.getRawFormat()
		mf.setTextMode(TextMode.TRIM_FULL_WHITE)
		val xout = XMLOutputter(mf)
		val output = xout.outputString(root)
		assertEquals("<root> x </root>", output)
	}

	
	@Test fun testOutputElementIgnoreTrAXEscapingPIs() {
		val root = Element("root")
		root.addContent(Text("&"))
		root.addContent(ProcessingInstruction("javax.xml.transform.disable-output-escaping", ""))
		root.addContent(Text(" && "))
		root.addContent(ProcessingInstruction("javax.xml.transform.enable-output-escaping", ""))
		root.addContent(Text("&"))
		val expect = "<root>&amp; && &amp;</root>"
		val excompact = "<root>&amp;&&&amp;</root>"
		val expretty = "<root>\n  &amp;\n  &&\n  &amp;\n</root>"
		val extfw = "<root>\n  &amp;\n   && \n  &amp;\n</root>"
		checkOutput(root,
				expect, 
				excompact, 
				expretty,
				expretty,
				extfw)
	}
	
	
	@Test fun testClone() {
		val xo = XMLOutputter()
		assertTrue(xo !== xo.clone())
	}

	@Test fun testToString() {
		val fmt = Format.getCompactFormat()
		fmt.setLineSeparator("\n\t ")
		val out = XMLOutputter(fmt)
		assertNotNull(out.toString())
	}
	
	@Test fun testCRNLEscaping() {
		val doc = Document()
		val root = Element("root")
		val child1 = Element("child1")
		val child2 = Element("child2")
		val stuff = Text("foo")
		root.addContent(child1)
		root.addContent(stuff)
		root.addContent(child2)
		doc.setRootElement(root)
		val xout = XMLOutputter(Format.getPrettyFormat())
		val actual = xout.outputString(doc)
		val expect = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n" +
				"<root>\r\n" +
				"  <child1 />\r\n" +
				"  foo\r\n" +
				"  <child2 />\r\n" +
				"</root>\r\n"
		assertEquals(expect, actual)
	}
	
	/**
	 * The following method will run the output data through each of the base formatters, raw, compact, and pretty. It
	 * will also run each of those formatters as the outputString(content), output(content, OutputStream) (JVM only:
	 * `outputViaStream`) and output(content, Appendable).
	 *
	 * The expectation is that the results of the three output forms (String, OutputStream, and Appendable) will be
	 * identical, and that it will match the expected value for the appropriate formatter.
	 *
	 * K5: jdom2 found the `output*` methods by reflection; this dispatches on the type of the content.
	 */
	override fun checkOutput(content: Any, methodprefix: String, setup: FormatSetup?,
			raw: String, compact: String, pretty: String, tso: String, trimfw: String) {

		super.checkOutput(content, methodprefix, setup, raw, compact, pretty, tso, trimfw)

		val descn = arrayOf<String>("Raw", "Compact", "Pretty", "TrimFullWhite")
		val ftrimfw = Format.getPrettyFormat()
		ftrimfw.setTextMode(TextMode.TRIM_FULL_WHITE)
		val formats = arrayOf<Format>(
				getFormat(setup, Format.getRawFormat()), 
				getFormat(setup, Format.getCompactFormat()),
				getFormat(setup, Format.getPrettyFormat()),
				getFormat(setup, ftrimfw))
		val result = arrayOf<String>(expect(raw), expect(compact), expect(pretty), expect(trimfw))

		for (i in 0 until 4) {
			val out = XMLOutputter(formats[i])
			try {
				val rstring = outputStringOf(out, methodprefix, content)
				assertEquals(result[i], rstring, "outputString Format " + descn[i])
				val rstream = outputViaStream(out, methodprefix, content)
				if (rstream != null) {
					assertEquals(result[i], rstream, "output OutputStream Format " + descn[i])
				}
				val rwriter = outputToAppendable(out, methodprefix, content)
				assertEquals(result[i], rwriter, "output Writer Format " + descn[i])
			} catch (e: Exception) {
				fail("Failed to process " + descn[i] + " on content " + content::class + ": " + e.message)
			}
		}
	}

	@Suppress("UNCHECKED_CAST")
	private fun outputStringOf(out: XMLOutputter, methodprefix: String, content: Any): String {
		if (methodprefix == "outputElementContent") {
			return out.outputElementContentString(content as Element)
		}
		return when (content) {
			is Document -> out.outputString(content)
			is Element -> out.outputString(content)
			is EntityRef -> out.outputString(content)
			is CDATA -> out.outputString(content)
			is Comment -> out.outputString(content)
			is DocType -> out.outputString(content)
			is ProcessingInstruction -> out.outputString(content)
			is Text -> out.outputString(content)
			is List<*> -> out.outputString(content as List<Content>)
			else -> throw IllegalStateException("Unable to find an outputString overload for " + content::class)
		}
	}

	/** The `output(content, Appendable)` form (jdom2's `Writer` form: a `java.io.Writer` is an `Appendable`). */
	@Suppress("UNCHECKED_CAST")
	private fun outputToAppendable(out: XMLOutputter, methodprefix: String, content: Any): String {
		val sb = StringBuilder()
		if (methodprefix == "outputElementContent") {
			out.outputElementContent(content as Element, sb)
			return sb.toString()
		}
		when (content) {
			is Document -> out.output(content, sb)
			is Element -> out.output(content, sb)
			is EntityRef -> out.output(content, sb)
			is CDATA -> out.output(content, sb)
			is Comment -> out.output(content, sb)
			is DocType -> out.output(content, sb)
			is ProcessingInstruction -> out.output(content, sb)
			is Text -> out.output(content, sb)
			is List<*> -> out.output(content as List<Content>, sb)
			else -> throw IllegalStateException("Unable to find an Appendable overload for " + content::class)
		}
		return sb.toString()
	}

	/** K5: the OutputStream form where the platform has one (JVM), otherwise the Appendable form. */
	private fun outputToString(outputter: XMLOutputter, doc: Document): String {
		return outputViaStream(outputter, "output", doc) ?: outputToAppendable(outputter, "output", doc)
	}
}