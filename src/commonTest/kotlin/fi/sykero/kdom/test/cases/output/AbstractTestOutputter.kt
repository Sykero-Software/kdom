/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.output

import kotlin.test.*
import kotlin.reflect.KClass
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.Parent
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.Format.TextMode

abstract class AbstractTestOutputter(
		private val cr2xD: Boolean,
		private val pademptyelement: Boolean,
		private val padpi: Boolean,
		private val forceexpand: Boolean,
		private val usesrawxmlout: Boolean) {
	
	protected fun interface FormatSetup {
		fun setup(fmt: Format)
	}
	
	
	protected fun expect(input: String): String {
		var expect = input
		if (cr2xD) {
			expect = expect.replace(Regex("\r"), "&#xD;")
		}
		if (forceexpand) {
			expect = expect.replace(Regex("<(\\w+(:\\w+)?)\\s*/>"), "<$1></$1>")
			expect = expect.replace(Regex("<(\\w+(:\\w+)?)\\s+(.+?)\"\\s*/>"), "<$1 $3\"></$1>")
		}
		if (padpi) {
			expect = expect.replace(Regex("(<\\?\\w+)(\\?>)"), "$1 $2")
		}
		if (pademptyelement) {
			//expect = expect.replaceAll("\">", "\" >");
			//expect = expect.replaceAll("<(\\w+)>", "<$1 >");
			expect = expect.replace(Regex("<(\\w+(:\\w+)?)/>"), "<$1 />")
			expect = expect.replace(Regex("<(\\w+(:\\w+)?\\s.+\")/>"), "<$1 />")
		} else {
			//expect = expect.replaceAll("<(\\w+)\\s+>", "<$1>");
			expect = expect.replace(Regex("<(\\w+)(\\s+.+?)?\\s+/>"), "<$1$2/>")
			expect = expect.replace(Regex("<(\\w+:\\w+)(\\s+.+?)?\\s+/>"), "<$1$2/>")
		}
//		if (rawoutsideroot) {
//			// outside the root element will be raw-formatted.
//			StringBuilder sb = new StringBuilder(expect.length());
//			int gotstuff = 0;
//			boolean indoctype = false;
//			boolean gotroot = false;
//			int depth = 0;
//			char[] chars = expect.toCharArray();
//			int i = 0;
//			while (i < chars.length && Verifier.isXMLWhitespace(chars[i])) {
//				// skip initial whitespace.
//				i++;
//			}
//			for (; i < chars.length; i++) {
//				char c = chars[i];
//				sb.append(c);
//				if (!gotroot) {
//					if (c == '<') {
//						if (depth == 0) {
//							if (i < chars.length - 2) {
//								if (chars[i + 1] == '?') {
//									// PI or XML Declaration
//									gotstuff++;
//								} else if (chars[i + 1] == '!') {
//									// Comment of DOCTYPE
//									gotstuff++;
//									if (chars[i + 2] == 'D') {
//										// DOCTYPE
//										indoctype = true;
//									}
//								} else {
//									// root element
//									gotroot = true;
//								}
//							} else {
//								gotroot = true;
//							}
//						}
//						depth++;
//					} else if (c == '>') {
//						depth--;
//						if (depth == 0) {
//							if (indoctype) {
//								sb.append('\n');
//								indoctype = false;
//							}
//							while (i+1 < chars.length && Verifier.isXMLWhitespace(chars[i + 1])) {
//								// skip whitespace after top-level content.
//								i++;
//							}
//						}
//					}
//				}
//			}
//			while (Verifier.isXMLWhitespace(sb.charAt(sb.length() - 1))) {
//				// eliminate trailing whitespace.
//				sb.setLength(sb.length() - 1);
//			}
//			if (gotstuff > 1 || (gotroot && gotstuff > 0)) {
//				// there is multiple content stuff, need to trim the whitespace....
//				expect = sb.toString();
//			}
//		}
		return expect
	}

	/**
	 * Return a string representing a {@link Document}. Uses an internal
	 * StringBuilder.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param doc
	 *        <code>Document</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputDocumentAsString(format: Format, doc: Document): String

	/**
	 * Return a string representing a {@link DocType}.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param doctype
	 *        <code>DocType</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputDocTypeAsString(format: Format, doctype: DocType): String

	/**
	 * Return a string representing an {@link Element}.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param element
	 *        <code>Element</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputElementAsString(format: Format, element: Element): String

	/**
	 * Return a string representing a List of {@link Content} nodes. <br>
	 * The list is assumed to contain legal JDOM nodes. If other content is
	 * coerced on to the list it will cause ClassCastExceptions, and null List
	 * members will cause NullPointerException.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param list
	 *        <code>List</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws ClassCastException
	 *         if non-{@link Content} is forced in to the list
	 * @throws NullPointerException
	 *         if the List is null or contains null members.
	 */
	abstract fun outputListAsString(format: Format, list: List<Content>): String
	
	/**
	 * Return a string representing a {@link CDATA} node.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param cdata
	 *        <code>CDATA</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputCDataAsString(format: Format, cdata: CDATA): String

	/**
	 * Return a string representing a {@link Text} node.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param text
	 *        <code>Text</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputTextAsString(format: Format, text: Text): String
	
	/**
	 * Return a string representing a {@link Comment}.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param comment
	 *        <code>Comment</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputCommentAsString(format: Format, comment: Comment): String

	/**
	 * Return a string representing a {@link ProcessingInstruction}.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param pi
	 *        <code>ProcessingInstruction</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputPIAsString(format: Format, pi: ProcessingInstruction): String

	/**
	 * Return a string representing an {@link EntityRef}.
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param entity
	 *        <code>EntityRef</code> to format.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputEntityRefAsString(format: Format, entity: EntityRef): String

	/**
	 * This will handle printing out an <code>{@link
	 * Element}</code>'s content only, not including its tag, and attributes.
	 * This can be useful for printing the content of an element that contains
	 * HTML, like "&lt;description&gt;JDOM is
	 * &lt;b&gt;fun&gt;!&lt;/description&gt;".
	 * <p>
	 * <b>Warning</b>: a String is Unicode, which may not match the outputter's
	 * specified encoding.
	 * 
	 * @param element
	 *        <code>Element</code> to output.
	 * @return the input content formatted as an XML String.
	 * @throws NullPointerException
	 *         if the specified content is null.
	 */
	abstract fun outputElementContentString(format: Format, element: Element): String

	protected val fraw: Format = Format.getRawFormat()
    protected val frawfp: Format = Format.getPrettyFormat().setTextMode(TextMode.PRESERVE)
	protected val fcompact: Format = Format.getCompactFormat()
	protected val fpretty: Format = Format.getPrettyFormat()
	protected val ftso: Format = Format.getPrettyFormat()
	protected val ftfw: Format = Format.getPrettyFormat()
	
	init {
		fraw.setLineSeparator("\n")
		frawfp.setLineSeparator("\n")
		fcompact.setLineSeparator("\n")
		fpretty.setLineSeparator("\n")
		ftso.setLineSeparator("\n")
		ftso.isSpecifiedAttributesOnly = true
		ftfw.setLineSeparator("\n")
		ftfw.setTextMode(TextMode.TRIM_FULL_WHITE)
	}
	

	@Test fun testTextEmpty() {
		val content = Text("")
		assertEquals("", outputTextAsString(fraw,     content))
        assertEquals("", outputTextAsString(frawfp,   content))
		assertEquals("", outputTextAsString(fcompact, content))
		assertEquals("", outputTextAsString(fpretty,  content))
		assertEquals("", outputTextAsString(ftso,     content))
		assertEquals("", outputTextAsString(ftfw,     content))
	}
	
	@Test fun testTextWhitespace() {
		val content = Text(" \r \n \t ")
		assertEquals(expect(" \r \n \t "), 
				outputTextAsString(fraw,     content))
        assertEquals(expect(" \r \n \t "), 
                outputTextAsString(frawfp,   content))
		assertEquals("", 
				outputTextAsString(fcompact, content))
		assertEquals("", 
				outputTextAsString(fpretty,  content))
		assertEquals("", 
				outputTextAsString(ftso,     content))
		assertEquals("", 
				outputTextAsString(ftfw,     content))
	}

	@Test fun testTextWithText() {
		val content = Text(" \r & \n \t ")
		assertEquals(expect(" \r &amp; \n \t "), 
				outputTextAsString(fraw,     content))
        assertEquals(expect(" \r &amp; \n \t "), 
                outputTextAsString(frawfp,   content))
		assertEquals(expect("&amp;"), 
				outputTextAsString(fcompact, content))
		assertEquals(expect("&amp;"), 
				outputTextAsString(fpretty,  content))
		assertEquals(expect("&amp;"), 
				outputTextAsString(ftso,     content))
		assertEquals(expect(" \r &amp; \n \t "), 
				outputTextAsString(ftfw,     content))
	}

	@Test fun testCDATAEmpty() {
		val content = CDATA("")
		assertEquals("<![CDATA[]]>", 
				outputCDataAsString(fraw,     content))
        assertEquals("<![CDATA[]]>", 
                outputCDataAsString(frawfp,   content))
		assertEquals("",
				outputCDataAsString(fcompact, content))
		assertEquals("",
				outputCDataAsString(fpretty,  content))
		assertEquals("",
				outputCDataAsString(ftso,     content))
		assertEquals("",
				outputCDataAsString(ftfw,     content))
	}
	
	@Test fun testCDATAWhitespace() {
		val content = CDATA(" \r \n \t ")
		assertEquals("<![CDATA[ \r \n \t ]]>", 
				outputCDataAsString(fraw,     content))
        assertEquals("<![CDATA[ \r \n \t ]]>", 
                outputCDataAsString(frawfp,   content))
		assertEquals("", 
				outputCDataAsString(fcompact, content))
		assertEquals("", 
				outputCDataAsString(fpretty,  content))
		assertEquals("", 
				outputCDataAsString(ftso,      content))
		assertEquals("", 
				outputCDataAsString(ftfw,     content))
	}

	@Test fun testCDATAWithText() {
		val content = CDATA(" \r & \n \t ")
		assertEquals("<![CDATA[ \r & \n \t ]]>", 
				outputCDataAsString(fraw,     content))
        assertEquals("<![CDATA[ \r & \n \t ]]>", 
                outputCDataAsString(frawfp,   content))
		assertEquals("<![CDATA[&]]>", 
				outputCDataAsString(fcompact, content))
		assertEquals("<![CDATA[&]]>", 
				outputCDataAsString(fpretty,  content))
		assertEquals("<![CDATA[&]]>", 
				outputCDataAsString(ftso,  content))
		assertEquals("<![CDATA[ \r & \n \t ]]>", 
				outputCDataAsString(ftfw,     content))
	}

	@Test fun testEntityRef() {
		val content = EntityRef("ref")
		assertEquals("&ref;", 
				outputEntityRefAsString(fraw,     content))
        assertEquals("&ref;", 
                outputEntityRefAsString(frawfp,   content))
		assertEquals("&ref;", 
				outputEntityRefAsString(fcompact, content))
		assertEquals("&ref;", 
				outputEntityRefAsString(fpretty,  content))
		assertEquals("&ref;", 
				outputEntityRefAsString(ftso,  content))
		assertEquals("&ref;", 
				outputEntityRefAsString(ftfw,     content))
	}
	
	@Test fun testProcessingInstructionTargetOnly() {
		val content = ProcessingInstruction("target")
		assertEquals(expect("<?target?>"), 
				outputPIAsString(fraw,     content))
        assertEquals(expect("<?target?>"), 
                outputPIAsString(frawfp,   content))
		assertEquals(expect("<?target?>"), 
				outputPIAsString(fcompact, content))
		assertEquals(expect("<?target?>"), 
				outputPIAsString(fpretty,  content))
		assertEquals(expect("<?target?>"), 
				outputPIAsString(ftfw,     content))
	}
	
	@Test fun testProcessingInstructionTargetWithData() {
		val content = 
				ProcessingInstruction("target", "data")
		assertEquals("<?target data?>", 
				outputPIAsString(fraw,     content))
        assertEquals("<?target data?>", 
                outputPIAsString(frawfp,   content))
		assertEquals("<?target data?>", 
				outputPIAsString(fcompact, content))
		assertEquals("<?target data?>", 
				outputPIAsString(fpretty,  content))
		assertEquals("<?target data?>", 
				outputPIAsString(ftso,  content))
		assertEquals("<?target data?>", 
				outputPIAsString(ftfw,     content))
	}
	
	@Test fun testComment() {
		val content = Comment("comment")
		assertEquals("<!--comment-->", 
				outputCommentAsString(fraw,     content))
        assertEquals("<!--comment-->", 
                outputCommentAsString(frawfp,   content))
		assertEquals("<!--comment-->", 
				outputCommentAsString(fcompact, content))
		assertEquals("<!--comment-->", 
				outputCommentAsString(fpretty,  content))
		assertEquals("<!--comment-->", 
				outputCommentAsString(ftso,  content))
		assertEquals("<!--comment-->", 
				outputCommentAsString(ftfw,     content))
	}
	

	@Test fun testDocTypeSimple() {
		val content = DocType("root")
		assertEquals("<!DOCTYPE root>", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root>", 
                outputDocTypeAsString(frawfp,   content))
		assertEquals("<!DOCTYPE root>", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root>", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root>", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root>", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testDocTypeSimpleISS() {
		val content = DocType("root")
		content.internalSubset = "<!ENTITY name \"value\">"
		assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
                outputDocTypeAsString(frawfp,   content))
		assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root [\n<!ENTITY name \"value\">]>", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testDocTypeSystemID() {
		val content = DocType("root", "sysid")
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
                outputDocTypeAsString(frawfp,   content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\">", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testDocTypeSystemIDISS() {
		val content = DocType("root", "sysid")
		content.internalSubset = "internal"
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
                outputDocTypeAsString(frawfp,   content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root SYSTEM \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testDocTypePublicSystemID() {
		val content = DocType("root", "pubid", "sysid")
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
                outputDocTypeAsString(frawfp,   content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\">", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testDocTypePublicSystemIDISS() {
		val content = DocType("root", "pubid", "sysid")
		content.internalSubset = "internal"
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fraw,     content))
        assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
                outputDocTypeAsString(frawfp,     content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fcompact, content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(fpretty,  content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(ftso,  content))
		assertEquals("<!DOCTYPE root PUBLIC \"pubid\" \"sysid\" [\ninternal]>", 
				outputDocTypeAsString(ftfw,     content))
	}
	
	@Test fun testMultiWhiteText() {
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(Text(""))
		root.addContent(Text(" "))
		root.addContent(Text("  \n \n "))
		root.addContent(Text("  \t "))
		root.addContent(Text("  "))
		assertEquals(expect("<root><![CDATA[ ]]>        \n \n   \t   </root>"), 
				outputElementAsString(fraw,     root))
        assertEquals(expect("<root><![CDATA[ ]]>        \n \n   \t   </root>"), 
                outputElementAsString(frawfp,   root))
		assertEquals(expect("<root/>"), 
				outputElementAsString(fcompact, root))
		assertEquals(expect("<root/>"), 
				outputElementAsString(fpretty,  root))
		assertEquals(expect("<root/>"), 
				outputElementAsString(ftso,  root))
		assertEquals(expect("<root/>"), 
				outputElementAsString(ftfw,     root))
	}
	
	@Test fun testMultiText() {
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(Text(""))
		root.addContent(Text("X"))
		root.addContent(Text("  \n \n "))
		root.addContent(Text("  \t "))
		root.addContent(Text("  "))
		assertEquals(expect("<root><![CDATA[ ]]>     X  \n \n   \t   </root>"), 
				outputElementAsString(fraw,     root))
        assertEquals(expect("<root><![CDATA[ ]]>     X  \n \n   \t   </root>"), 
                outputElementAsString(frawfp,   root))
		assertEquals(expect("<root>X</root>"), 
				outputElementAsString(fcompact, root))
		assertEquals(expect("<root>X</root>"), 
				outputElementAsString(fpretty,  root))
		assertEquals(expect("<root>X</root>"), 
				outputElementAsString(ftso,  root))
		assertEquals(expect("<root><![CDATA[ ]]>     X  \n \n   \t   </root>"), 
				outputElementAsString(ftfw,     root))
	}
	
	@Test fun testDocumentSimple() {
		val content = Document()
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", 
				outputDocumentAsString(fraw,     content))
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", 
                outputDocumentAsString(frawfp,   content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", 
				outputDocumentAsString(fcompact, content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", 
				outputDocumentAsString(fpretty,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n", 
				outputDocumentAsString(ftso,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n",
				outputDocumentAsString(ftfw,     content))
	}
	
	@Test fun testDocumentDocType() {
		val content = Document()
		content.setDocType(DocType("root"))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n", 
				outputDocumentAsString(fraw,     content))
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n", 
                outputDocumentAsString(frawfp,   content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n", 
				outputDocumentAsString(fcompact, content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n", 
				outputDocumentAsString(fpretty,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n", 
				outputDocumentAsString(ftso,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE root>\n",
				outputDocumentAsString(ftfw,     content))
	}
	
	@Test fun testDocumentComment() {
		val content = Document()
		content.addContent(Comment("comment"))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n", 
				outputDocumentAsString(fraw,     content))
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n", 
                outputDocumentAsString(frawfp,   content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n", 
				outputDocumentAsString(fcompact, content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n", 
				outputDocumentAsString(fpretty,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n", 
				outputDocumentAsString(ftso,  content))
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!--comment-->\n",
				outputDocumentAsString(ftfw,     content))
	}

	
	
	
	
	
	
	
	
	
	@Test fun testXXX() {
		val content = Text("")
		assertEquals("", 
				outputTextAsString(fraw,     content))
        assertEquals("", 
                outputTextAsString(frawfp,     content))
		assertEquals("", 
				outputTextAsString(fcompact, content))
		assertEquals("", 
				outputTextAsString(fpretty,  content))
		assertEquals("", 
				outputTextAsString(ftfw,     content))
	}
	
	
	
	
	
	@Test fun testOutputText() {
		checkOutput(Text(" hello  there  "), " hello  there  ", "hello there", "hello  there", "hello  there", " hello  there  ")
	}

	@Test fun testOutputCDATA() {
		val indata = "   hello   there  bozo !   "
		val rawcdata = "<![CDATA[   hello   there  bozo !   ]]>"
		val compdata = "<![CDATA[hello there bozo !]]>"
		val prettydata = "<![CDATA[hello   there  bozo !]]>"
		val trimdata = "<![CDATA[   hello   there  bozo !   ]]>"
		
		checkOutput(CDATA(indata), rawcdata, compdata, prettydata, prettydata, trimdata)
	}

	@Test fun testOutputComment() {
		val incomment = "   hello   there  bozo !   "
		val outcomment = "<!--" + incomment + "-->"
		checkOutput(Comment(incomment), outcomment, outcomment, outcomment, outcomment, outcomment)
	}

	@Test fun testOutputProcessingInstructionSimple() {
		val inpi = ProcessingInstruction("jdomtest", "")
		val outpi = "<?jdomtest?>"
		checkOutput(inpi, outpi, outpi, outpi, outpi, outpi)
	}

	@Test fun testOutputProcessingInstructionData() {
		val pi = "  hello   there  "
		val inpi = ProcessingInstruction("jdomtest", pi)
		val outpi = "<?jdomtest " + pi + "?>"
		checkOutput(inpi, outpi, outpi, outpi, outpi, outpi)
	}

	@Test fun testOutputEntityRef() {
		checkOutput(EntityRef("name", "publicID", "systemID"),
				"&name;", "&name;", "&name;", "&name;", "&name;")
	}

	@Test fun testOutputElementSimple() {
		val txt = "<root/>"
		checkOutput(Element("root"), txt, txt, txt, txt, txt)
	}

	@Test fun testOutputElementAttribute() {
		val txt = "<root att=\"val\" />"
		checkOutput(Element("root").setAttribute("att", "val"), txt, txt, txt, txt, txt)
	}

	@Test fun testOutputElementAttributeNotSpecifiedA() {
		val txt = "<root att=\"val\" />"
		val root = Element("root")
		val att = Attribute("att", "val")
		root.setAttribute(att)
		att.isSpecified = false
		checkOutput(root, txt, txt, txt, "<root />", txt)
	}

	@Test fun testOutputElementAttributeNotSpecifiedB() {
		val txt = "<root atta=\"val\" attb=\"attb\" />"
		val root = Element("root")
		val atta = Attribute("atta", "val")
		val attb = Attribute("attb", "attb")
		root.setAttribute(atta)
		root.setAttribute(attb)
		atta.isSpecified = false
		checkOutput(root, txt, txt, txt, "<root attb=\"attb\" />", txt)
	}

	@Test fun testOutputElementCDATA() {
		val txt = "<root><![CDATA[xx]]></root>"
		val root = Element("root")
		root.addContent(CDATA("xx"))
		checkOutput(root, txt, txt, txt, txt, txt)
	}

	@Test fun testOutputElementExpandEmpty() {
		val txt = "<root></root>"
		val setup = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(Element("root"), setup, txt, txt, txt, txt, txt)
	}

	@Test fun testOutputElementPreserveSpace() {
		val txt = "<root xml:space=\"preserve\">    <child xml:space=\"default\">abc</child> </root>"
		val root = Element("root")
		root.setAttribute("space", "preserve", Namespace.XML_NAMESPACE)
		root.addContent("    ")
		val child = Element("child")
		child.setAttribute("space", "default", Namespace.XML_NAMESPACE)
		child.addContent("abc")
		root.addContent(child)
		root.addContent(" ")
		checkOutput(root, txt, txt, txt, txt, txt)
	}
	
	@Test fun testOutputElementPreserveSpaceComplex() {
		// the purpose of this test is to ensure that the different
		// formatting values are used when going down one level,
		// back up, then in to 'preserve', back up, and then again
		// down in to normal (not preserve).
		
		// this is essentially a test of the FormatStack code....

		val tst = Element("child")
		val cmt = Comment("comment")
		tst.addContent(cmt)
		val spaced = "  <child>\n    <!--comment-->\n  </child>\n"
		val compact = "<child><!--comment--></child>"
		val preserved = "<child xml:space=\"preserve\"><!--comment--></child>"
		val root = Element("root")
		root.addContent(tst.clone())
		root.addContent(tst.clone().setAttribute("space", "preserve", Namespace.XML_NAMESPACE))
		root.addContent(tst.clone())
		val rawcompact = "<root>" + compact + preserved + compact + "</root>"
		val pretty = "<root>\n" + spaced + "  " + preserved + "\n" + spaced + "</root>"
		checkOutput(root, rawcompact, rawcompact, pretty, pretty, pretty)
	}
	

	@Test fun testOutputElementMultiText() {
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" xx "))
		root.addContent(Text("yy"))
		root.addContent(Text("    "))
		root.addContent(Text("zz"))
		root.addContent(Text("  ww"))
		root.addContent(EntityRef("amp"))
		root.addContent(Text("vv"))
		root.addContent(Text("  "))
		checkOutput(root,
				"<root><![CDATA[ ]]> xx yy    zz  ww&amp;vv  </root>", 
				"<root>xx yy zz ww&amp;vv</root>",
				"<root>xx yy    zz  ww&amp;vv</root>",
				"<root>xx yy    zz  ww&amp;vv</root>",
				// This should be changed with issue #31.
				// The real value should have one additional
				// space at the beginning and two at the end
				// for now we leave the broken test here because it
				// helps with the coverage reports.
				// the next test is added to be a failing test.
				"<root><![CDATA[ ]]> xx yy    zz  ww&amp;vv  </root>")
	}

	@Test fun testOutputElementMultiAllWhite() {
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(Text(""))
		root.addContent(Text(" "))
		root.addContent(Text("  \n \n "))
		root.addContent(Text("  \t "))
		root.addContent(Text("  "))
		checkOutput(root,
				"<root><![CDATA[ ]]>        \n \n   \t   </root>", 
				"<root />",
				"<root />",
				"<root />",
				"<root />")
	}

	@Test fun testOutputElementMultiAllWhiteExpandEmpty() {
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(Text(""))
		root.addContent(Text(" "))
		root.addContent(Text("  \n \n "))
		root.addContent(Text("  \t "))
		root.addContent(Text("  "))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><![CDATA[ ]]>        \n \n   \t   </root>", 
				"<root></root>",
				"<root></root>",
				"<root></root>",
				"<root></root>")
	}

	@Test fun testOutputElementMultiMostWhiteExpandEmpty() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(CDATA(" "))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(Text(""))
		root.addContent(Text(" "))
		root.addContent(Text("  \n \n "))
		root.addContent(Comment("Boo"))
		root.addContent(Text("  \t "))
		root.addContent(Text("  "))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><![CDATA[ ]]>        \n \n <!--Boo-->  \t   </root>", 
				"<root><!--Boo--></root>",
				"<root>\n  <!--Boo-->\n</root>",
				"<root>\n  <!--Boo-->\n</root>",
				"<root>\n  <!--Boo-->\n</root>")
	}

	@Test fun testOutputElementMixedMultiCDATA() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Comment("Boo"))
		root.addContent(Text(" "))
		root.addContent(CDATA("A"))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><!--Boo--> <![CDATA[A]]></root>", 
				"<root><!--Boo--><![CDATA[A]]></root>",
				"<root>\n  <!--Boo-->\n  <![CDATA[A]]>\n</root>",
				"<root>\n  <!--Boo-->\n  <![CDATA[A]]>\n</root>",
				"<root>\n  <!--Boo-->\n   <![CDATA[A]]>\n</root>")
	}

	@Test fun testOutputElementMixedMultiEntityRef() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Comment("Boo"))
		root.addContent(Text(" "))
		root.addContent(EntityRef("aer"))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><!--Boo--> &aer;</root>", 
				"<root><!--Boo-->&aer;</root>",
				"<root>\n  <!--Boo-->\n  &aer;\n</root>",
				"<root>\n  <!--Boo-->\n  &aer;\n</root>",
				"<root>\n  <!--Boo-->\n   &aer;\n</root>")
	}

	@Test fun testOutputElementMixedMultiText() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Comment("Boo"))
		root.addContent(Text(" "))
		root.addContent(Text("txt"))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><!--Boo--> txt</root>", 
				"<root><!--Boo-->txt</root>",
				"<root>\n  <!--Boo-->\n  txt\n</root>",
				"<root>\n  <!--Boo-->\n  txt\n</root>",
				"<root>\n  <!--Boo-->\n   txt\n</root>")
	}

	@Test fun testOutputElementMixedMultiZeroText() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Comment("Boo"))
		root.addContent(Text(""))
		root.addContent(Text(" "))
		root.addContent(Text(""))
		root.addContent(Text("txt"))
		root.addContent(Text(""))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><!--Boo--> txt</root>", 
				"<root><!--Boo-->txt</root>",
				"<root>\n  <!--Boo-->\n  txt\n</root>",
				"<root>\n  <!--Boo-->\n  txt\n</root>",
				"<root>\n  <!--Boo-->\n   txt\n</root>")
	}

	@Test fun testOutputElementInterleavedEmptyText() {
		// this is to test issue #72
		// Compact format only prints first child.
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Text(" "))
		root.addContent(Comment("Boo"))
		root.addContent(Text(" "))
		root.addContent(Element("child"))
		root.addContent(Text(" "))
		root.addContent(ProcessingInstruction("pitarget"))
		root.addContent(Text(" "))
		checkOutput(root,  
				"<root> <!--Boo--> <child /> <?pitarget?> </root>", 
				"<root><!--Boo--><child /><?pitarget?></root>", 
				"<root>\n  <!--Boo-->\n  <child />\n  <?pitarget?>\n</root>",
				"<root>\n  <!--Boo-->\n  <child />\n  <?pitarget?>\n</root>",
				"<root>\n  <!--Boo-->\n  <child />\n  <?pitarget?>\n</root>")
	}

	@Test fun testOutputElementMultiEntityLeftRight() {
		val root = Element("root")
		root.addContent(EntityRef("erl"))
		root.addContent(Text(" "))
		root.addContent(Text("    "))
		root.addContent(EntityRef("err"))
		checkOutput(root,
				"<root>&erl;     &err;</root>", 
				"<root>&erl; &err;</root>",
				"<root>&erl;     &err;</root>",
				"<root>&erl;     &err;</root>",
				"<root>&erl;     &err;</root>")
	}

	@Test fun testOutputElementMultiTrimLeftRight() {
		val root = Element("root")
		root.addContent(Text(" tl "))
		root.addContent(Text(" mid "))
		root.addContent(Text(" tr "))
		checkOutput(root,
				"<root> tl  mid  tr </root>", 
				"<root>tl mid tr</root>",
				"<root>tl  mid  tr</root>",
				"<root>tl  mid  tr</root>",
				"<root> tl  mid  tr </root>")
	}

	@Test fun testOutputElementMultiCDATALeftRight() {
		val root = Element("root")
		root.addContent(CDATA(" tl "))
		root.addContent(Text(" mid "))
		root.addContent(CDATA(" tr "))
		checkOutput(root,
				"<root><![CDATA[ tl ]]> mid <![CDATA[ tr ]]></root>", 
				"<root><![CDATA[tl]]> mid <![CDATA[tr]]></root>",
				"<root><![CDATA[tl ]]> mid <![CDATA[ tr]]></root>",
				"<root><![CDATA[tl ]]> mid <![CDATA[ tr]]></root>",
				"<root><![CDATA[ tl ]]> mid <![CDATA[ tr ]]></root>")
	}
	
	
	
	@Test fun testOutputElementNamespaces() {
		val txt = "<ns:root xmlns:ns=\"myns\" xmlns:ans=\"attributens\" xmlns:two=\"two\" ans:att=\"val\"/>"
		val emt = Element("root", Namespace.getNamespace("ns", "myns"))
		val ans = Namespace.getNamespace("ans", "attributens")
		emt.setAttribute(Attribute("att", "val", ans))
		emt.addNamespaceDeclaration(Namespace.getNamespace("two", "two"))
		checkOutput(emt,
				txt, txt,txt, txt, txt)
	}

	@Test fun testOutputDocTypeSimple() {
		checkOutput(DocType("root"), "<!DOCTYPE root>", "<!DOCTYPE root>", "<!DOCTYPE root>", 
				"<!DOCTYPE root>", "<!DOCTYPE root>")
	}

	@Test fun testOutputDocTypeInternalSubset() {
		val dec = "<!DOCTYPE root [\ninternal]>"
		val dt = DocType("root")
		dt.internalSubset = "internal"
		checkOutput(dt, dec, dec, dec, dec, dec)
	}

	@Test fun testOutputDocTypeSystem() {
		val dec = "<!DOCTYPE root SYSTEM \"systemID\">"
		checkOutput(DocType("root", "systemID"), dec, dec, dec, dec, dec)
	}

	@Test fun testOutputDocTypePublic() {
		val dec = "<!DOCTYPE root PUBLIC \"publicID\">"
		checkOutput(DocType("root", "publicID", null), dec, dec, dec, dec, dec)
	}

	@Test fun testOutputDocTypePublicSystem() {
		val dec = "<!DOCTYPE root PUBLIC \"publicID\" \"systemID\">"
		checkOutput(DocType("root", "publicID", "systemID"), dec, dec, dec, dec, dec)
	}

	@Test fun testOutputDocumentSimple() {
		val doc = Document()
		doc.addContent(Element("root"))
		val xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		val rtdec = "<root />"
		checkOutput(doc, 
				xmldec + "\n" + rtdec + "\n", 
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n")
	}

	@Test fun testOutputDocumentOmitEncoding() {
		val doc = Document()
		doc.addContent(Element("root"))
		val xmldec = "<?xml version=\"1.0\"?>"
		val setup = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setOmitEncoding(true)
			}
		}
		val rtdec = "<root />"
		checkOutput(doc, setup,
				xmldec + "\n" + rtdec + "\n", 
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n",
				xmldec + "\n" + rtdec + "\n")
	}

	@Test fun testOutputDocumentOmitDeclaration() {
		val doc = Document()
		doc.addContent(Element("root"))
		val setup = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setOmitDeclaration(true)
			}
		}
		val rtdec = "<root />"
		checkOutput(doc, setup,
				rtdec + "\n", 
				rtdec + "\n",
				rtdec + "\n",
				rtdec + "\n",
				rtdec + "\n")
	}

	@Test fun testOutputDocumentFull() {
		val dt = DocType("root")
		val comment = Comment("comment")
		val pi = ProcessingInstruction("jdomtest", "")
		val root = Element("root")
		val doc = Document()
		doc.addContent(dt)
		doc.addContent(comment)
		doc.addContent(pi)
		doc.addContent(root)
		val xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		val dtdec = "<!DOCTYPE root>"
		val commentdec = "<!--comment-->"
		val pidec = "<?jdomtest?>"
		val rtdec = "<root />"
		val lf = "\n"
		val dlf = if (usesrawxmlout) "" else lf
		checkOutput(doc, 
				xmldec + lf + dtdec + commentdec + pidec + rtdec + lf, 
				xmldec + lf + dtdec + commentdec + pidec + rtdec + lf,
				xmldec + lf + dtdec + dlf + commentdec + dlf + pidec  + dlf + rtdec + lf,
				xmldec + lf + dtdec + dlf + commentdec + dlf + pidec  + dlf + rtdec + lf,
				xmldec + lf + dtdec + dlf + commentdec + dlf + pidec  + dlf + rtdec + lf)
	}
	
	@Test fun testDeepNesting() {
		// need to get beyond 16 levels of XML.
		val dt = DocType("root")
		val root = Element("root")
		val doc = Document()
		doc.addContent(dt)
		doc.addContent(root)
		val xmldec = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
		val dtdec = "<!DOCTYPE root>"
		val lf = "\n"
		
		val raw = StringBuilder()
		raw.append(xmldec).append(lf).append(dtdec)
		val pretty = StringBuilder()
		pretty.append(xmldec).append(lf).append(dtdec)
		if (!usesrawxmlout) {
			// most test systems use the XMLOutputter in raw mode to output
			// the results of the conversion. In Raw mode the XMLOutputter will
			// not make pretty content outside of the root element (but it will
			// put the XMLDeclaration on its own line).
			// so, in the cases where the actual pretty format is used, we add
			// this newline after the DocType...
			pretty.append(lf)
		}
		raw.append("<root>")
		pretty.append("<root>")
		pretty.append(lf)
		val depth: Int = 40
		var cnt: Int = depth
		var parent = root
		val indent = StringBuilder()
		while (--cnt > 0) {
			val emt = Element("emt")
			parent.content.add(emt)
			parent = emt
			raw.append("<emt>")
			indent.append("  ")
			pretty.append(indent.toString())
			pretty.append("<emt>")
			pretty.append(lf)
		}
		
		parent.content.add(Element("bottom"))
		raw.append("<bottom />")
		pretty.append(indent.toString())
		pretty.append("  <bottom />")
		pretty.append(lf)
		
		cnt = depth
		while (--cnt > 0) {
			raw.append("</emt>")
			pretty.append(indent.toString())
			pretty.append("</emt>")
			indent.setLength(indent.length - 2)
			pretty.append(lf)
		}
		raw.append("</root>")
		raw.append(lf)
		pretty.append("</root>")
		pretty.append(lf)
		
		checkOutput(doc, raw.toString(), raw.toString(), pretty.toString(), pretty.toString(), pretty.toString()) 
	}
	
	@Test fun testOutputElementContent() {
		val root = Element("root")
		root.addContent(Element("child"))
		checkOutput(root, "outputElementContent", null, "<child />", "<child />", "<child />", "<child />", "<child />")
	}

	@Test fun testOutputList() {
		val c: MutableList<Any?> = ArrayList<Any?>()
		c.add(Element("root"))
		checkOutput(c, "output", null, "<root />", "<root />", "<root />", "<root />", "<root />")
	}

	
	@Test fun testOutputEscapedMixedMultiText() {
		// this test has mixed content (text-type and not text type).
		// and, it has a multi-text-type at the end.
		val root = Element("root")
		root.addContent(Comment("Boo"))
		root.addContent(Text(" xx "))
		root.addContent(Text("<emb>"))
		root.addContent(Text(" xx "))
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		checkOutput(root, fs,  
				"<root><!--Boo--> xx &lt;emb&gt; xx </root>", 
				"<root><!--Boo-->xx &lt;emb&gt; xx</root>",
				"<root>\n  <!--Boo-->\n  xx &lt;emb&gt; xx\n</root>",
				"<root>\n  <!--Boo-->\n  xx &lt;emb&gt; xx\n</root>",
				"<root>\n  <!--Boo-->\n   xx &lt;emb&gt; xx \n</root>")
	}

	@Test fun testOutputLotsOfMixedMultiText() {
		// this test has many text content members.
		val root = Element("root")
		val sb = StringBuilder()
		sb.append("<root>i")
		root.addContent("i")
		for (i in 0 until 100) {
			sb.append("&ent;")
			sb.append(i)
			root.addContent(EntityRef("ent"))
			root.addContent("" + i)
		}
		sb.append("</root>")
		val fs = object : FormatSetup {
			override fun setup(fmt: Format) {
				fmt.setExpandEmptyElements(true)
			}
		}
		val expect = sb.toString()
		checkOutput(root, fs, expect, expect, expect, expect, expect) 
	}

	
	
	protected fun checkOutput(content: Any, raw: String, compact: String, pretty: String, tso: String, trimfw: String) {
		checkOutput(content, "output", null, raw, compact, pretty, tso, trimfw)
	}

	protected fun checkOutput(content: Any, setup: FormatSetup, raw: String, compact: String, pretty: String, tso: String, trimfw: String) {
		checkOutput(content, "output", setup, raw, compact, pretty, tso, trimfw)
	}

	/**
	 * Outputs [data] through the abstract `output*AsString` method that matches its type (the jdom2 original found
	 * it through a table of runners keyed by class; `outputElementContent` always uses
	 * [outputElementContentString]).
	 */
	private fun outputString(methodprefix: String, format: Format, data: Any): String {
		if (methodprefix == "outputElementContent") {
			return outputElementContentString(format, data as Element)
		}
		@Suppress("UNCHECKED_CAST")
		return when (data) {
			is Document -> outputDocumentAsString(format, data)
			is Element -> outputElementAsString(format, data)
			is EntityRef -> outputEntityRefAsString(format, data)
			is CDATA -> outputCDataAsString(format, data)
			is Comment -> outputCommentAsString(format, data)
			is DocType -> outputDocTypeAsString(format, data)
			is ProcessingInstruction -> outputPIAsString(format, data)
			is Text -> outputTextAsString(format, data)
			is List<*> -> outputListAsString(format, data as List<Content>)
			else -> throw IllegalStateException("Unable to find a runner for type " + data::class)
		}
	}

	/**
	 * The following method will run the output data through each of the base formatters, raw, compact, and
	 * pretty, and check the result of the String form of the output against the expected values.
	 * (`TestXMLOutputter` extends this with the Appendable and OutputStream forms.)
	 *
	 * @param content The content to output
	 * @param methodprefix What the methods are called
	 * @param setup A callback mechanism to modify the formatters
	 * @param raw  What we expect the content to look like with the RAW format
	 * @param compact What we expect the content to look like with the COMPACT format
	 * @param pretty What we expect the content to look like with the PRETTY format
	 * @param trimfw What we expect the content to look like with the TRIM_FULL_WHITE format
	 */
	protected open fun checkOutput(content: Any, methodprefix: String,
			setup: FormatSetup?, raw: String, compact: String, pretty: String, tso: String, trimfw: String) {
		val descn = arrayOf<String>("Raw", "PrettyPreserve", "Compact", "Pretty", "PrettySpecifiedOnly", "TrimFullWhite")

		val ftrimfw = Format.getPrettyFormat()
		ftrimfw.setTextMode(TextMode.TRIM_FULL_WHITE)
		val fattspec = Format.getPrettyFormat()
		fattspec.isSpecifiedAttributesOnly = true
		val formats = arrayOf<Format>(
				getFormat(setup, Format.getRawFormat()), 
				getFormat(setup, Format.getPrettyFormat().setTextMode(TextMode.PRESERVE)), 
				getFormat(setup, Format.getCompactFormat()),
				getFormat(setup, Format.getPrettyFormat()),
				getFormat(setup, fattspec),
				getFormat(setup, ftrimfw))
		val result = arrayOf<String>(raw, raw, compact, pretty, tso, trimfw)

		for (i in 0 until result.size) {
			val mstring = outputString(methodprefix, formats[i], content)
			val msg = "outputString Format " + descn[i]
			assertEquals(expect(result[i]), mstring, msg)
		}
	}

	protected fun getFormat(setup: FormatSetup?, input: Format): Format {
		if (setup == null) {
			input.setLineSeparator("\n")
			return input
		}
		input.setLineSeparator("\n")
		setup.setup(input)
		return input
	}
}
