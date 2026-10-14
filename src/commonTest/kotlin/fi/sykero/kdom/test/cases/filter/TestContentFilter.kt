/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter

import kotlin.test.*
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.filter.ContentFilter
import fi.sykero.kdom.filter.ElementFilter
import fi.sykero.kdom.test.util.UnitTestUtil

class TestContentFilter : AbstractTestFilter() {
	
	private val allContent: IntArray = intArrayOf(
			ContentFilter.CDATA, ContentFilter.COMMENT,
			ContentFilter.DOCTYPE, ContentFilter.DOCUMENT, 
			ContentFilter.ELEMENT, ContentFilter.ENTITYREF,
			ContentFilter.PI, ContentFilter.TEXT
	)
	
	@Test fun testSetters() {
		var mask: Int = 0
		var flag: Boolean = false
		val cfa = ContentFilter(false)
		do {
			flag = !flag
			for (m in allContent) {
				when (m) {
			ContentFilter.CDATA -> {
					cfa.setCDATAVisible(flag)
			}
			ContentFilter.COMMENT -> {
					cfa.setCommentVisible(flag)
			}
			ContentFilter.DOCTYPE -> {
					cfa.setDocTypeVisible(flag)
			}
			ContentFilter.DOCUMENT -> {
					if (flag) {
						cfa.filterMask = cfa.filterMask or ContentFilter.DOCUMENT
					} else {
						cfa.filterMask = cfa.filterMask and (ContentFilter.DOCUMENT.inv()) 
					}
			}
			ContentFilter.ELEMENT -> {
					cfa.setElementVisible(flag)
			}
			ContentFilter.ENTITYREF -> {
					cfa.setEntityRefVisible(flag)
			}
			ContentFilter.PI -> {
					cfa.setPIVisible(flag)
			}
			ContentFilter.TEXT -> {
					cfa.setTextVisible(flag)
			}
		}
				if (flag) {
					mask = mask or m
				} else {
					mask = mask and (m.inv())
				}
				if (cfa.filterMask != mask) {
					fail("ContentFilter Mask is out of sync after " +
							"setting flag " + m + " with value " + flag)
				}
			}
		} while (flag)
	}
	
	@Test fun testDefaultDocumentContent() { 
		val cf = ContentFilter()
		cf.setDocumentContent()
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				if (c is Element) {
					return true
				}
				if (c is ProcessingInstruction) {
					return true
				}
				if (c is DocType) {
					return true
				}
				if (c is Comment) {
					return true
				}
				return false
			}
		}
		
		exerciseContent(cb, ContentFilter.PI, ContentFilter.ELEMENT,
				ContentFilter.COMMENT, ContentFilter.DOCTYPE)

	}
	
	@Test fun testAllElementContent() { 
		val cf = ContentFilter()
		cf.setElementContent()
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				if (c is Element) {
					return true
				}
				if (c is ProcessingInstruction) {
					return true
				}
				if (c is EntityRef) {
					return true
				}
				if (c is CDATA) {
					return true
				}
				if (c is Text) {
					return true
				}
				if (c is Comment) {
					return true
				}
				return false
			}
		}
		
		exerciseContent(cb, ContentFilter.ELEMENT, ContentFilter.ENTITYREF,
				ContentFilter.COMMENT, ContentFilter.CDATA,
				ContentFilter.TEXT, ContentFilter.PI)

	}
	
	
	@Test fun testAllContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c != null
			}
		}
		exerciseContent(cb, ContentFilter.CDATA, ContentFilter.COMMENT,
				ContentFilter.DOCTYPE, ContentFilter.DOCUMENT, 
				ContentFilter.ELEMENT, ContentFilter.ENTITYREF,
				ContentFilter.PI, ContentFilter.TEXT)
	}
	
	@Test fun testElementContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c is Element
			}
		}
		exerciseContent(cb, ContentFilter.ELEMENT)
	}
	
//	@Test
//	public void testDocumentContentFilter() {
//		ContentFilter cf = new ContentFilter(ContentFilter.DOCUMENT);
//		assertTrue(cf.matches(getDocument()));
//		assertFalse(cf.matches(getRoot()));
//		ContentFilter cfe = new ContentFilter(ContentFilter.ELEMENT);
//		assertFalse(cfe.matches(getDocument()));
//	}
	
	@Test fun testDocTypeContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c is DocType
			}
		}
		exerciseContent(cb, ContentFilter.DOCTYPE)
	}
	
	@Test fun testPIContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c is ProcessingInstruction
			}
		}
		exerciseContent(cb, ContentFilter.PI)
	}
	
	@Test fun testCDATAContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c is CDATA
			}
		}
		exerciseContent(cb, ContentFilter.CDATA)
	}
	
	@Test fun testCommentContentFilter() {
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c is Comment
			}
		}
		exerciseContent(cb, ContentFilter.COMMENT)
	}
	
	private fun exerciseContent(cb: CallBack, vararg types: Int) {
		var mask: Int = 0
		val cfa = ContentFilter(0)
		for (m in types) {
			mask = mask or m
			when (m) {
			ContentFilter.CDATA -> {
				cfa.setCDATAVisible(true)
			}
			ContentFilter.COMMENT -> {
				cfa.setCommentVisible(true)
			}
			ContentFilter.DOCTYPE -> {
				cfa.setDocTypeVisible(true)
			}
			ContentFilter.DOCUMENT -> {
				cfa.filterMask = cfa.filterMask or ContentFilter.DOCUMENT
			}
			ContentFilter.ELEMENT -> {
				cfa.setElementVisible(true)
			}
			ContentFilter.ENTITYREF -> {
				cfa.setEntityRefVisible(true)
			}
			ContentFilter.PI -> {
				cfa.setPIVisible(true)
			}
			ContentFilter.TEXT -> {
				cfa.setTextVisible(true)
			}
		}
		}
		val cf = ContentFilter(mask)
		// K5: the twin of the filter built flag by flag is the filter built from the mask (equal, another instance)
		exercise(cfa, cf, getRoot(), cb)
		exercise(cfa, cf, getDocument(), cb)
		exercise(cf, cfa, getRoot(), cb)
		exercise(cf, cfa, getDocument(), cb)

		assertFilterEquals(cf, cfa)
		
		assertTrue(cf.filter(Any()) == null)
		assertTrue(cfa.filter(Any()) == null)
	}

	@Test fun testEqualsObject() {
		assertFilterEquals(ContentFilter(), ContentFilter(true))
		
		val cfa = ContentFilter(ContentFilter.CDATA)
		val cfb = ContentFilter(ContentFilter.CDATA) // K5: was a deserialized copy (no serialization)
		assertFilterEquals(cfa, cfb)
		cfa.setCommentVisible(true)
		assertFilterNotEquals(cfa, cfb)
		
		assertFilterNotEquals(cfa, ContentFilter(true))
		assertFilterNotEquals(cfa, ContentFilter(false))
		assertFilterNotEquals(cfa, ElementFilter())
		
	}

}
