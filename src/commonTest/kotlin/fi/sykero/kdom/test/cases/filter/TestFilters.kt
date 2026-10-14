/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter

import kotlin.test.*
import kotlin.reflect.KClass
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.filter.ContentFilter
import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.test.util.UnitTestUtil

class TestFilters : AbstractTestFilter() {
	
	
	private fun <F> checkFilter(filter: Filter<F>, match: F, not: Any?) {
		assertNotNull(filter)
		assertNotNull(filter.toString())
		// K5: the deserialized-copy check is gone (no serialization)
		assertFilterNotEquals(filter, filter.negate())
		assertFalse(filter.equals(null))
		assertTrue(filter.matches(match))
		assertFalse(filter.matches(not))
		var al: ArrayList<Any?>? = null
		assertTrue(filter.filter(al).isEmpty())
		al = ArrayList<Any?>(3)
		al.add(not)
		al.add(match)
		al.add(null)
		val mat = filter.filter(al)
		
		assertTrue(mat.size == 1)
		assertTrue(mat.get(0) === match)
	}

	@Test fun testContent() {
		checkFilter(Filters.content(), Element("tag"), Any())
		checkFilter(Filters.content(), Element("tag"), null)
	}

	@Test fun testDocument() {
		checkFilter(Filters.document(), Document(), Any())
	}

	@Test fun testAttribute() {
		checkFilter(Filters.attribute(), Attribute("tag", "val"), Any())
	}

	@Test fun testAttributeString() {
		checkFilter(Filters.attribute("mat"), Attribute("mat", "val"), Attribute("not", "val"))
	}

	@Test fun testAttributeStringNamespace() {
		val nsa = Namespace.getNamespace("pfa", "uria")
		val nsb = Namespace.getNamespace("pfb", "urib")
		checkFilter(Filters.attribute("mat", nsa), 
				Attribute("mat", "val", nsa), Any())
		checkFilter(Filters.attribute("mat", nsa), 
				Attribute("mat", "val", nsa), Attribute("mat", "val", nsb))
		checkFilter(Filters.attribute("mat", nsa), 
				Attribute("mat", "val", nsa), Attribute("not", "val", nsa))
		checkFilter(Filters.attribute("mat", nsa), 
				Attribute("mat", "val", nsa), Attribute("mat", "val"))
		checkFilter(Filters.attribute("mat", null), 
				Attribute("mat", "val", nsa), Attribute("not", "val", nsa))
		checkFilter(Filters.attribute(null, nsa), 
				Attribute("mat", "val", nsa), Attribute("mat", "val", nsb))
		checkFilter(Filters.attribute(null, null), 
				Attribute("mat", "val", nsa), Element("mat", nsb))
	}

	@Test fun testAttributeNamespace() {
		val nsa = Namespace.getNamespace("pfa", "uria")
		val nsb = Namespace.getNamespace("pfb", "urib")
		checkFilter(Filters.attribute(nsa), 
				Attribute("mat", "val", nsa), Any())
		checkFilter(Filters.attribute(nsa), 
				Attribute("mat", "val", nsa), Attribute("mat", "val", nsb))
	}

	@Test fun testComment() {
		checkFilter(Filters.comment(), Comment("comment"), Any())
	}

	@Test fun testCdata() {
		checkFilter(Filters.cdata(), CDATA("comment"), Any())
	}

	@Test fun testDoctype() {
		checkFilter(Filters.doctype(), DocType("root"), Any())
	}

	@Test fun testEntityref() {
		checkFilter(Filters.entityref(), EntityRef("er"), Any())
	}

	@Test fun testElement() {
		checkFilter(Filters.element(), Element("emt"), Any())
	}

	@Test fun testElementString() {
		val nsa = Namespace.getNamespace("pfa", "uria")
		checkFilter(Filters.element("mat"), 
				Element("mat"), Any())
		checkFilter(Filters.element("mat"), 
				Element("mat"), Element("mat", nsa))
		checkFilter(Filters.element("mat"), 
				Element("mat"), Element("not", nsa))
	}

	@Test fun testElementStringNamespace() {
		val nsa = Namespace.getNamespace("pfa", "uria")
		val nsb = Namespace.getNamespace("pfb", "urib")
		checkFilter(Filters.element("mat", nsa), 
				Element("mat", nsa), Any())
		checkFilter(Filters.element("mat", nsa), 
				Element("mat", nsa), Element("mat", nsb))
		checkFilter(Filters.element("mat", nsa), 
				Element("mat", nsa), Element("not", nsa))
		checkFilter(Filters.element("mat", nsa), 
				Element("mat", nsa), Element("mat"))
	}

	@Test fun testElementNamespace() {
		val nsa = Namespace.getNamespace("pfa", "uria")
		val nsb = Namespace.getNamespace("pfb", "urib")
		checkFilter(Filters.element(nsa), 
				Element("mat", nsa), Any())
		checkFilter(Filters.element(nsa), 
				Element("mat", nsa), Element("mat", nsb))
		checkFilter(Filters.element(nsa), 
				Element("mat", nsa), Element("mat"))
	}

	@Test fun testProcessinginstruction() {
		checkFilter(Filters.processinginstruction(), 
				ProcessingInstruction("jdomtest", ""), Any())
	}

	@Test fun testText() {
		checkFilter(Filters.text(), Text("txt"), Any())
		checkFilter(Filters.text(), CDATA("txt"), Any())
	}

	@Test fun testTextOnly() {
		checkFilter(Filters.textOnly(), Text("txt"), CDATA("txt"))
	}

	@Test fun testFBoolean() {
		checkFilter(Filters.fboolean(), true, Any())
	}

	@Test fun testFString() {
		checkFilter(Filters.fstring(), "txt", Any())
	}

	@Test fun testFDouble() {
		checkFilter(Filters.fdouble(), 123.45, Any())
	}
	
	@Test fun testFClass() {
		checkFilter(Filters.fclass(Int::class), // K5: fclass takes a KClass
				123, Any())
	}
	
	@Test fun testRefine() {
		assertFailsWith<NullPointerException>("Should not be able to set null refine filter") {
			ContentFilter().refine<Any?>(null)
		}
	}

}
