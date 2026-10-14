/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.*
import fi.sykero.kdom.filter.ContentFilter
import fi.sykero.kdom.filter.ElementFilter
import fi.sykero.kdom.output.*
import fi.sykero.kdom.test.util.UnitTestUtil
import fi.sykero.kdom.test.util.nullT

/* Please run replic.pl on me ! */
/**
 * Please put a description of your test here.
 * 
 * @author unascribed
 * @version 0.1
 */










class TestDocument {

	

	/**
	 * Test constructor of Document with a List of content including the root element.
	 */
	@Test fun test_TCC___List() {
		val bogus = Element("bogus-root")
		val element = Element("element")
		val comment = Comment("comment")
		var list: MutableList<Content> = ArrayList<Content>()

		list.add(element)
		list.add(comment)
		var doc = Document(list)
		// Get a live list back
		list = doc.content
		assertEquals(element, doc.rootElement, "incorrect root element returned")

		//no root element
		element.detach()
		assertFailsWith<IllegalStateException>("didn't catch missing root element") {
			doc.rootElement
		}

		//set root back, then try to add another element to our
		//live list
		doc.setRootElement(element)
		assertFailsWith<IllegalAddException>("didn't catch duplicate root element") {
			list.add(bogus)
		}
		assertEquals(element, doc.rootElement, "incorrect root element returned")

		//how about replacing it in our live list
		try {
			val oldRoot = doc.rootElement
			val i: Int = doc.indexOf(oldRoot)
			list.set(i, bogus)
		} catch (e: Exception) {
			fail("Root replacement shouldn't have throw a exception")
		}
		//and through the document
		try {
			doc.setRootElement(element)
		} catch (e: Exception) {
			fail("Root replacement shouldn't have throw a exception")
		}

		list = nullT()
		try {
			doc = Document(list)
		} catch (e: IllegalAddException) {
			fail("didn't handle null list")
		} catch (e: NullPointerException) {
			fail("didn't handle null list")
		}

	}

	/**
	 * Test constructor of a Document with a List of content and a DocType.
	 */
	@Test fun test_TCC___List_OrgJdomDocType() {
		val bogus = Element("bogus-root")
		val element = Element("element")
		val comment = Comment("comment")
		val docType = DocType("element")
		var list: MutableList<Content> = ArrayList<Content>()

		list.add(docType)
		list.add(element)
		list.add(comment)
		var doc = Document(list)
		// Get a live list back
		list = doc.content
		assertEquals(element, doc.rootElement, "incorrect root element returned")
		assertEquals(docType, doc.docType, "incorrect doc type returned")

		element.detach()
		assertFailsWith<IllegalStateException>("didn't catch missing root element") {
			doc.rootElement
		}

		//set root back, then try to add another element to our
		//live list
		doc.setRootElement(element)
		assertFailsWith<IllegalAddException>("didn't catch duplicate root element") {
			list.add(bogus)
		}
		assertEquals(element, doc.rootElement, "incorrect root element returned")

		//how about replacing it in our live list
		try {
			val oldRoot = doc.rootElement
			val i: Int = doc.indexOf(oldRoot)
			list.set(i,bogus)
		} catch (e: Exception) {
			fail("Root replacement shouldn't have throw a exception")
		}
		//and through the document
		try {
			doc.setRootElement(element)
		} catch (e: Exception) {
			fail("Root replacement shouldn't have throw a exception")
		}

		list = nullT()
		try {
			doc = Document(list)
		} catch (e: IllegalAddException) {
			fail("didn't handle null list")
		} catch (e: NullPointerException) {
			fail("didn't handle null list")
		}

	}

	/**
	 * Test the constructor with only a root element.
	 */
	@Test fun test_TCC___OrgJdomElement() {
		var element = Element("element")

		var doc = Document(element)
		assertEquals(element, doc.rootElement, "incorrect root element returned")

		element = nullT()
		try {
			doc = Document(element)
		} catch (e: IllegalAddException) {
			fail("didn't handle null element")
		} catch (e: NullPointerException) {
			fail("didn't handle null element")
		}

	}

	/**
	 * Test constructor of Document with and Element and Doctype
	 */
	@Test fun test_TCC___OrgJdomElement_OrgJdomDocType() {
		var element = Element("element")
		var docType = DocType("element")

		var doc = Document(element, docType)
		assertEquals(element, doc.rootElement, "incorrect root element returned")
		assertEquals(docType, doc.docType, "incorrect doc type returned")

		docType = DocType("element")
		element = nullT()
		try {
			doc = Document(element, docType)
		} catch (e: IllegalAddException) {
			fail("didn't handle null element")
		} catch (e: NullPointerException) {
			fail("didn't handle null element")
		}

	}

	/**
	 * Test constructor of Document with and Element and Doctype
	 */
	@Test fun test_TCC___OrgJdomElement_OrgJdomDocType_JavaLangString() {
		val element = Element("element")
		val docType = DocType("element")
		val baseuri = "BaseURI"

		var doc = Document(element, docType, baseuri)
		assertTrue(doc.hasRootElement())
		assertEquals(element, doc.rootElement, "incorrect root element returned")
		assertEquals(docType, doc.docType, "incorrect doc type returned")
		assertEquals("BaseURI", doc.baseURI, "incorrect BaseURI returned")

		try {
			element.detach()
			docType.detach()
			doc = Document(null, docType, baseuri)
			assertFalse(doc.hasRootElement())
			assertEquals(docType, doc.docType, "incorrect doc type returned")
			assertEquals("BaseURI", doc.baseURI, "incorrect BaseURI returned")
			try {
				assertNull(doc.rootElement, "incorrect root element returned")
				fail ("Should not be ableto query the root element if it is not set...")
			} catch (ise: IllegalStateException) {
				// OK, Root Element not set.
			}
		} catch (e: IllegalAddException) {
			fail("didn't handle null element")
		} catch (e: NullPointerException) {
			fail("didn't handle null element")
		}

		try {
			element.detach()
			docType.detach()
			doc = Document(element, null, baseuri)
			assertTrue(doc.hasRootElement())
			assertEquals(element, doc.rootElement, "incorrect root element returned")
			assertNull(doc.docType, "incorrect doc type returned")
			assertEquals("BaseURI", doc.baseURI, "incorrect BaseURI returned")
		} catch (e: IllegalAddException) {
			fail("didn't handle null docType")
		} catch (e: NullPointerException) {
			fail("didn't handle null docType")
		}

		try {
			element.detach()
			docType.detach()
			doc = Document(element, docType, null)
			assertTrue(doc.hasRootElement())
			assertEquals(element, doc.rootElement, "incorrect root element returned")
			assertEquals(docType, doc.docType, "incorrect doc type returned")
			assertNull(doc.baseURI, "incorrect BaseURI returned")
		} catch (e: IllegalAddException) {
			fail("didn't handle null baseuri")
		} catch (e: NullPointerException) {
			fail("didn't handle null baseuri")
		}

		try {
			element.detach()
			docType.detach()
			doc = Document(null, docType, null)
			assertFalse(doc.hasRootElement())
			try {
				assertNull(doc.rootElement, "incorrect root element returned")
				fail ("Should not be ableto query the root element if it is not set...")
			} catch (ise: IllegalStateException) {
				// OK, Root Element not set.
			}
			assertEquals(docType, doc.docType, "incorrect doc type returned")
			assertNull(doc.baseURI, "incorrect BaseURI returned")
		} catch (e: IllegalAddException) {
			fail("didn't handle null element and baseuri")
		} catch (e: NullPointerException) {
			fail("didn't handle null element and baseuri")
		}

		try {
			element.detach()
			docType.detach()
			doc = Document(null, null, null)
			assertFalse(doc.hasRootElement())
			try {
				assertNull(doc.rootElement, "incorrect root element returned")
				fail ("Should not be ableto query the root element if it is not set...")
			} catch (ise: IllegalStateException) {
				// OK, Root Element not set.
			}
			assertNull(doc.docType, "incorrect doc type returned")
			assertNull(doc.baseURI, "incorrect BaseURI returned")
		} catch (e: IllegalAddException) {
			fail("didn't handle null parameters")
		} catch (e: NullPointerException) {
			fail("didn't handle null parameters")
		}

	}

	/**
	 * Test object equality.
	 */
	@Test fun test_TCM__boolean_equals_Object() {
		val element = Element("element")

		val doc: Any = Document(element)
		assertEquals(doc, doc, "invalid object equality")

	}

	/**
	 * Test removeContent for a given Comment
	 */
	@Test fun test_TCM__boolean_removeContent_OrgJdomComment() {
		val element = Element("element")
		val comment = Comment("comment")
		val list = ArrayList<Content>()

		list.add(element)
		list.add(comment)
		val doc = Document(list)
		assertTrue(! doc.removeContent(Comment("hi")), "incorrect comment removed")
		assertTrue(doc.removeContent(comment), "didn't remove comment")

		assertTrue(doc.content.size == 1, "comment not removed")
	}

	/**
	 * Test removeContent with the supplied ProcessingInstruction.
	 */
	@Test fun test_TCM__boolean_removeContent_OrgJdomProcessingInstruction() {
		val element = Element("element")
		val pi = ProcessingInstruction("test", "comment")
		val list = ArrayList<Content>()

		list.add(element)
		list.add(pi)
		val doc = Document(list)
		assertTrue(! doc.removeContent(ProcessingInstruction("hi", "there")), "incorrect pi removed")
		assertTrue(doc.removeContent(pi), "didn't remove pi")

		assertTrue(doc.content.size == 1, "PI not removed")

	}

	/**
	 * Test hashcode function.
	 */
	@Test fun test_TCM__int_hashCode() {
		val element = Element("test")
		val doc = Document(element)

		//only an exception would be a problem
		var i: Int = -1
		try {
			i = doc.hashCode()
		}
		catch (e: Exception) {
			fail("bad hashCode")
		}


		val element2 = Element("test")
		val doc2 = Document(element2)
		//different Documents, same text
		val x: Int = doc2.hashCode()
		assertTrue(x != i, "Different Elements with same value have same hashcode")

	}

	/**
	 * Test code goes here. Replace this comment.
	 */
	@Test fun test_TCM__Object_clone() {
		//do the content tests to 2 levels deep to verify recursion
		var element = Element("el")
		val ns = Namespace.getNamespace("urn:hogwarts")
		element.setAttribute(Attribute("name", "anElement"))
		var child1 = Element("child", ns)
		child1.setAttribute(Attribute("name", "first"))

		var child2 = Element("firstChild", ns)
		child2.setAttribute(Attribute("name", "second"))
		var child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))
		child1.addContent(child2)
		element.addContent(child1)
		element.addContent(child3)

		//add mixed content to the nested child2 element
		var comment = Comment("hi")
		child2.addContent(comment)
		val cdata = CDATA("gotcha")
		child2.addContent(cdata)
		var pi = ProcessingInstruction("tester", "do=something")
		child2.addContent(pi)
		var entity = EntityRef("wizards")
		child2.addContent(entity)
		child2.addContent("finally a new wand!")

		//a little more for the element
		element.addContent("top level element text")
		val topComment = Comment("some comment")

		val doc = Document(element)
		doc.addContent(topComment)
		val docClone = doc.clone()
		element = nullT()
		child3 = nullT()
		child2 = nullT()
		child1 = nullT()

		val list = docClone.rootElement.content

		//finally the test
		assertEquals(((docClone.content.get(1) as Comment)).text, "some comment", "wrong comment")
		assertEquals(((list.get(0) as Element)).name, "child", "wrong child element")
		assertEquals(((list.get(1) as Element)).name, "child", "wrong child element")
		val deepClone = ((list.get(0) as Element)).getChild("firstChild", Namespace.getNamespace("urn:hogwarts"))!!

		assertEquals("firstChild", deepClone.name, "wrong nested element")
		//comment
		assertTrue(deepClone.content.get(0) !== comment, "deep clone comment not a clone")
		comment = nullT()
		assertEquals("hi", ((deepClone.content.get(0) as Comment)).text, "incorrect deep clone comment")
		//CDATA

		assertEquals("gotcha", ((deepClone.content.get(1) as CDATA)).text, "incorrect deep clone CDATA")
		//PI
		assertTrue(deepClone.content.get(2) !== pi, "deep clone PI not a clone")
		pi = nullT()
		assertEquals("do=something", ((deepClone.content.get(2) as ProcessingInstruction)).data, "incorrect deep clone PI")
		//entity
		assertTrue(deepClone.content.get(3) !== entity, "deep clone Entity not a clone")
		entity = nullT()
		assertEquals("wizards", ((deepClone.content.get(3) as EntityRef)).name, "incorrect deep clone Entity")
		//text
		assertEquals("finally a new wand!", ((deepClone.content.get(4) as Text)).text, "incorrect deep clone test")


	}

	/**
	 * Test getDocType.
	 */
	@Test fun test_TCM__OrgJdomDocType_getDocType() {
		val element = Element("element")
		val docType = DocType("element")

		val doc = Document(element, docType)
		assertEquals(element, doc.rootElement, "incorrect root element returned")
		assertEquals(docType, doc.docType, "incorrect doc type returned")

	}

	/**
	 * Test the addition of comments to Documents.
	 */
	@Test fun test_TCM__OrgJdomDocument_addContent_OrgJdomComment() {
		val element = Element("element")
		val comment = Comment("comment")
		val comment2 = Comment("comment 2")

		val doc = Document(element)
		doc.addContent(comment)
		doc.addContent(comment2)
		val content = doc.content

		assertEquals(3, content.size, "wrong number of comments in List")
		assertEquals(comment, content.get(1), "wrong comment")
		assertEquals(comment2, content.get(2), "wrong comment")
	}

	/**
	 * Test the addition of ProcessingInstructions to Documents.
	 */
	@Test fun test_TCM__OrgJdomDocument_addContent_OrgJdomProcessingInstruction() {
		val element = Element("element")
		val pi = ProcessingInstruction("test", "comment")
		val pi2 = ProcessingInstruction("test", "comment 2")

		val doc = Document(element)
		doc.addContent(pi)
		doc.addContent(pi2)
		val content = doc.content

		assertEquals(3, content.size, "wrong number of PI's in List")
		assertEquals(pi, content.get(1), "wrong PI")
		assertEquals(pi2, content.get(2), "wrong PI")
	}

	/**
	 * Test that setRootElement works as expected.
	 */
	@Test fun test_TCM__OrgJdomDocument_setRootElement_OrgJdomElement() {
		val element = Element("element")

		val doc1 = Document(element)
		assertEquals(element, doc1.rootElement, "incorrect root element returned")
		val doc2 = Document()
		assertFailsWith<IllegalAddException>("didn't catch element already attached to another document") {
			doc2.setRootElement(element)
		}
	}

	/**
	 * Test that setDocType works as expected.
	 */
	@Test fun test_TCM__OrgJdomDocument_setDocType_OrgJdomDocType() {
		val element = Element("element")
		val docType = DocType("element")

		val doc = Document(element)
		doc.setDocType(docType)
		assertEquals(element, doc.rootElement, "incorrect root element returned")
		assertEquals(docType, doc.docType, "incorrect doc type returned")
	}

	/**
	 * Test that a Document can return a root element.
	 */
	@Test fun test_TCM__OrgJdomElement_getRootElement() {
		val element = Element("element")

		val doc = Document(element)
		assertEquals(element, doc.rootElement, "incorrect root element returned")
	}

	/**
	 * Test that the toString method returns the expected result.
	 */
	@Test fun test_TCM__String_toString() {
		val element = Element("element")
		val docType = DocType("element")

		val doc = Document(element, docType)
		val buf = "[Document: [DocType: <!DOCTYPE element>], Root is [Element: <element/>]]"
		assertEquals(buf, doc.toString(), "incorrect root element returned")

	}


	/**
	 * Test getContent
	 */
    @Test fun test_TCM__List_getContent() {
		val element = Element("element")
		val comment = Comment("comment")
		val list = ArrayList<Content>()

		list.add(element)
		list.add(comment)
		val doc = Document(list)
		assertEquals(list, doc.content, "missing mixed content")
		assertEquals(2, doc.content.size, "wrong number of elements")
	}

	/**
	 * Test that setContent works according to specs.
	 */
    @Test fun test_TCM__OrgJdomDocument_setContent_List() {
		val element = Element("element")
		val newElement = Element("newEl")
		val comment = Comment("comment")
		val pi = ProcessingInstruction("foo", "bar")
		val list = ArrayList<Content>()

		list.add(newElement)
		list.add(comment)
		list.add(pi)


		val doc = Document(element)
		doc.setContent(list)
		assertEquals(3, doc.content.size, "wrong number of elements")
		assertEquals(newElement, doc.content.get(0), "missing element")
		assertEquals(comment, doc.content.get(1), "missing comment")
		assertEquals(pi, doc.content.get(2), "missing pi")
	}
    
	@Test fun testDocumentAddDocType() {
		try {
			val doc = Document()
			doc.addContent(Element("tag"))
			val list = doc.content
			list.add(DocType("elementname"))
			fail ("Should not be able to add DocType to a document after an Element")
		} catch (iae: IllegalAddException) {
			// good!
		} catch (e: Exception) {
			fail ("We expect an IllegalAddException, but got " + e::class.qualifiedName)
		}
	}

	@Test fun testDocType() {
		val doc = Document()
		assertTrue(doc === doc.setDocType(null))
		val dta = DocType("DocTypeA")
		val dtb = DocType("DocTypeB")
		doc.setDocType(dta)
		assertTrue(doc.docType === dta)
		assertTrue(dta.parent === doc)
		assertTrue(dtb.parent == null)
		doc.setDocType(dtb)
		assertTrue(doc.docType === dtb)
		assertTrue(dta.parent == null)
		assertTrue(dtb.parent === doc)
		assertFailsWith<IllegalAddException>("Should not be able to add an already attached DocType") {
			doc.setDocType(dtb)
		}
		assertTrue(doc.docType === dtb)
		assertTrue(dta.parent == null)
		assertTrue(dtb.parent === doc)
		doc.setDocType(null)
		assertTrue(doc.docType == null)
		assertTrue(dta.parent == null)
		assertTrue(dtb.parent == null)

	}
	
	@Test fun testDocumentProperties() {
		val doc = Document()
		assertTrue(doc.getProperty("one") == null)
		doc.setProperty("one", "one1")
		assertTrue("one1" === doc.getProperty("one"))
		doc.setProperty("two", "two2")
		assertTrue("one1" === doc.getProperty("one"))
		assertTrue("two2" === doc.getProperty("two"))
	}
	
	@Test fun testDocumentContent() {
		val doc = Document()
		assertTrue(doc.contentSize == 0)
		assertTrue(doc.docType == null)
		assertFalse(doc.hasRootElement())
		assertTrue(doc.cloneContent().size == 0)
		
		val doctype = DocType("element")
		val comment1 = Comment("comment1")
		val comment2 = Comment("comment2")
		val root = Element("root")
		
		doc.setDocType(doctype)
		assertTrue(doc.contentSize == 1)
		assertTrue(doc.docType === doctype)
		assertFalse(doc.hasRootElement())
		assertTrue(doc.cloneContent().size == 1)
		assertTrue(doc.cloneContent().get(0) is DocType)
		assertTrue(doc.indexOf(doctype) == 0)
		
		assertTrue(doctype.parent === doc)
		assertTrue(doc.removeContent().get(0) === doctype)
		
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 0)
		assertTrue(doc.docType == null)
		assertFalse(doc.hasRootElement())
		assertTrue(doc.cloneContent().size == 0)
		
		doc.addContent(comment1)
		doc.addContent(comment2)
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 2)
		doc.setContent(1, doctype)
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent == null)
		assertTrue(doctype.parent === doc)
		assertTrue(doc.contentSize == 2)
		
		doc.setContent(comment2)
		assertTrue(comment1.parent == null)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 1)
		assertTrue(comment2 === doc.removeContent(0))
		
		doc.addContent(setOf(comment2))
		assertTrue(comment1.parent == null)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 1)
		
		doc.addContent(0, setOf(comment1))
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 2)
		
		doc.addContent(2, setOf(doctype))
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent === doc)
		assertTrue(doc.contentSize == 3)
		assertTrue(doc.indexOf(comment1) == 0)
		assertTrue(doc.indexOf(comment2) == 1)
		assertTrue(doc.indexOf(doctype) == 2)
		
		doc.setContent(2, setOf(doctype))
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent === doc)
		assertTrue(doc.contentSize == 3)
		assertTrue(doc.indexOf(comment1) == 0)
		assertTrue(doc.indexOf(comment2) == 1)
		assertTrue(doc.indexOf(doctype) == 2)
		
		val empty = emptySet<Content>()
		doc.setContent(2, empty)
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent == null)
		assertTrue(doc.contentSize == 2)
		assertTrue(doc.indexOf(comment1) == 0)
		assertTrue(doc.indexOf(comment2) == 1)
		assertTrue(doc.indexOf(doctype) == -1)
		
		doc.addContent(2, doctype)
		assertTrue(comment1.parent === doc)
		assertTrue(comment2.parent === doc)
		assertTrue(doctype.parent === doc)
		assertTrue(doc.contentSize == 3)
		assertTrue(doc.indexOf(comment1) == 0)
		assertTrue(doc.indexOf(comment2) == 1)
		assertTrue(doc.indexOf(doctype) == 2)
		
		doc.addContent(root)
		assertTrue(doc.indexOf(root) == 3)
		assertTrue(doc.contentSize == 4)
		assertTrue(doc.rootElement === root)
		assertTrue(root.parent === doc)
		assertTrue(doc.content.size == 4)
		assertTrue(doc.getContent(ElementFilter()).size == 1)
		assertTrue(doc.getContent(ContentFilter(ContentFilter.COMMENT)).size == 2)
		assertTrue(root === doc.detachRootElement())
		assertTrue(null == doc.detachRootElement())

		assertFailsWith<IllegalStateException>("Should not be able to get content when there's no root element") {
			doc.content
		}
		
		assertFailsWith<IllegalStateException>("Should not be able to get content when there's no root element") {
			doc.getContent(ElementFilter())
		}
		
		assertTrue(doc.removeContent(ContentFilter(ContentFilter.COMMENT)).size == 2)
		
		// at this point, all the Document has is a doctype
		assertTrue(doc.contentSize == 1)
		assertTrue(doc.docType === doctype)
		
		// time for more broken content.
		assertFailsWith<IllegalAddException>("Should not be able to put root element before the doctype") {
			doc.addContent(0, root)
		}
		
		doc.addContent(1, root)
		doc.setDocType(null)
		assertFailsWith<IllegalAddException>("Should not be able to put doctype after the root") {
			doc.addContent(1, doctype)
		}
		
		doc.setDocType(doctype)
		
		assertFailsWith<IllegalAddException>("Should not be able to add second doctype") {
			doc.addContent(doc.indexOf(doctype) + 1, DocType("anotherdup"))
		}
		
		doc.addContent(1, comment1)
		
		assertFailsWith<IllegalAddException>("Should not be able to set second doctype") {
			doc.setContent(1, DocType("anotherdup"))
		}
		
		assertFailsWith<IllegalAddException>("Should not be able to set second root element") {
			doc.setContent(1, Element("anotherdup"))
		}
		
		// but you should be able to replace the root element
		assertTrue(doc === doc.setContent(doc.indexOf(root), Element("root2")))
		// and you should be able to replace the doctype
		assertTrue(doc === doc.setContent(doc.indexOf(doctype), DocType("doctype2")))
		// and you should be able to set the root element if there's no other root element.
		assertTrue(doc.removeContent(doc.rootElement))
		assertTrue(doc === doc.setContent(doc.indexOf(comment1), root))
		
		
		assertFailsWith<IllegalAddException>("Should not be able to add EntityRef") {
			doc.addContent(EntityRef("myref"))
		}
		
		assertFailsWith<IllegalAddException>("Should not be able to add CDATA") {
			doc.addContent(CDATA("mycdata"))
		}
		
		assertFailsWith<IllegalAddException>("Should not be able to add Text") {
			doc.addContent(Text("text"))
		}
		
		
		
	}
	
	@Test fun testClone() {
		val doc = Document()
		val doctype = DocType("element")
		val comment1 = Comment("comment1")
		val comment2 = Comment("comment2")
		val pi = ProcessingInstruction("tstpi", "pidata")
		val root = Element("root")
		
		doc.setDocType(doctype)
		doc.addContent(comment1)
		doc.addContent(comment2)
		doc.addContent(pi)
		doc.addContent(root)
		
		val clone = doc.clone()
		assertTrue(doc.equals(doc))
		assertTrue(clone.equals(clone))
		assertFalse(clone.equals(doc))
		assertFalse(doc.equals(clone))
		
		assertTrue(doc.content.get(0) is DocType)
		assertTrue(doc.content.get(1) is Comment)
		assertTrue(doc.content.get(2) is Comment)
		assertTrue(doc.content.get(3) is ProcessingInstruction)
		assertTrue(doc.content.get(4) is Element)
		
		
	}

	@Test fun testParent() {
		val doc = Document()
		assertTrue(null == doc.parent)
		doc.addContent(Element("root"))
		assertTrue(null == doc.parent)
	}
	
	@Test fun testToString() {
		val doc = Document()
		assertTrue(doc.toString() != null)
		assertTrue(doc.toString().indexOf("Document") >= 0)
		assertTrue(doc.toString().indexOf("root") >= 0)

		doc.addContent(Comment("tstcomment"))
		assertTrue(doc.toString().indexOf("tstcomment") < 0)
		assertTrue(doc.toString().indexOf("root") >= 0)

		doc.addContent(DocType("tstdoctype"))
		assertTrue(doc.toString().indexOf("Document") >= 0)
		assertTrue(doc.toString().indexOf("tstcomment") < 0)
		assertTrue(doc.toString().indexOf("tstdoctype") >= 0)
		assertTrue(doc.toString().indexOf("root") >= 0)
		
		doc.addContent(Element("tstelement"))
		assertTrue(doc.toString().indexOf("Document") >= 0)
		assertTrue(doc.toString().indexOf("tstcomment") < 0)
		assertTrue(doc.toString().indexOf("tstdoctype") >= 0)
		assertTrue(doc.toString().indexOf("root") < 0)
		assertTrue(doc.toString().indexOf("tstelement") >= 0)
	}
	
	@Test fun testSPaceInProlog() {
		val doc = Document()
		
		doc.addContent(Text(" "))
		assertTrue(doc.contentSize == 1)
		doc.addContent(Element("root"))
		assertTrue(doc.contentSize == 2)
		doc.addContent(Text("\n"))
		assertTrue(doc.contentSize == 3)
		doc.addContent(Comment("foo"))
		assertTrue(doc.contentSize == 4)
		doc.addContent(Comment("bar"))
		assertTrue(doc.contentSize == 5)
		
		assertFailsWith<IllegalArgumentException>("Expected an add of non-space content to fail, but it did not") {
			doc.addContent(Text("bad"))
		}
		
		val out = XMLOutputter()
		out.format.setLineSeparator(LineSeparator.NL)
		val sw = StringBuilder()
		out.output(doc, sw)
				
		//System.out.println(sw.toString());
		
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <root />\n<!--foo--><!--bar-->\n", sw.toString(), "Space-in-prolog output")
	}

//	@Test
//	public void testDocumentAddAttribute() {
//		try {
//			List<Content> list = (new Document(new Element("root")).getContent());
//			list.add(new Attribute("att", "value"));
//			fail ("Should not be able to add Attribute to an Document's ContentList");
//		} catch (IllegalAddException iae) {
//			// good!
//		} catch (Exception e) {
//			fail ("We expect an IllegalAddException, but got " + e.getClass().getName());
//		}
//	}
	
}
