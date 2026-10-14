/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter

import kotlin.test.*
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.filter.ElementFilter

class TestElementFilter : AbstractTestFilter() {
	
	@Test fun testElementFilter() {
		val ef = ElementFilter()
		val eft = ElementFilter()
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c != null && c is Element
			}
		}
		exercise(ef, eft, getRoot(), cb)
		exercise(ef, eft, getDocument(), cb)
	}

	@Test fun testElementFilterString() {
		val name = "four"
		val ef = ElementFilter(name)
		val eft = ElementFilter(name)
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Element) &&
						name.equals(((c as Element)).name)
			}
		}
		exercise(ef, eft, getRoot(), cb)
		exercise(ef, eft, getDocument(), cb)
	}

	@Test fun testElementFilterNamespace() {
		val efa = ElementFilter(Namespace.NO_NAMESPACE)
		val efat = ElementFilter(Namespace.NO_NAMESPACE)
		val cba = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Element) &&
						Namespace.NO_NAMESPACE.equals(((c as Element)).namespace)
			}
		}
		exercise(efa, efat, getRoot(), cba)
		exercise(efa, efat, getDocument(), cba)

		val efb = ElementFilter(getTestNamespace())
		val efbt = ElementFilter(getTestNamespace())
		val cbb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Element) &&
						getTestNamespace().equals(((c as Element)).namespace)
			}
		}
		exercise(efb, efbt, getRoot(), cbb)
		exercise(efb, efbt, getDocument(), cbb)
	
		
	}

	@Test fun testElementFilterStringNamespace() {
		val namea = "four"
		val efa = ElementFilter(namea, Namespace.NO_NAMESPACE)
		val efat = ElementFilter(namea, Namespace.NO_NAMESPACE)
		val cba = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Element) &&
						namea.equals(((c as Element)).name) && 
						Namespace.NO_NAMESPACE.equals(((c as Element)).namespace)
			}
		}
		exercise(efa, efat, getRoot(), cba)
		exercise(efa, efat, getDocument(), cba)

		val nameb = "three"
		val efb = ElementFilter(nameb, getTestNamespace())
		val efbt = ElementFilter(nameb, getTestNamespace())
		val cbb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Element) &&
						nameb.equals(((c as Element)).name) && 
						getTestNamespace().equals(((c as Element)).namespace)
			}
		}
		exercise(efb, efbt, getRoot(), cbb)
		exercise(efb, efbt, getDocument(), cbb)
	
	}

	@Test fun testEqualsObject() {
		val URI1 = "http://jdom.org/test1"
		val URI2 = "http://jdom.org/test2"
		val ns1a = Namespace.getNamespace("pfxa", URI1)
		val ns1b = Namespace.getNamespace("pfxb", URI1)
		val ns1c = Namespace.getNamespace(URI1)
		
		val ns2a = Namespace.getNamespace("pfxa", URI2)
		val ns2c = Namespace.getNamespace(URI2)
		
		assertFilterEquals(ElementFilter("test"), 
				ElementFilter("test"))
		
		// same namespace
		assertFilterEquals(ElementFilter("test", ns1a), 
				ElementFilter("test", ns1a))
		
		// same namespace URI (different prefix)
		assertFilterEquals(ElementFilter("test", ns1a), 
				ElementFilter("test", ns1b))
		
		// same namespace URI (different prefix)
		assertFilterEquals(ElementFilter("test", ns1a), 
				ElementFilter("test", ns1c))
		
		// same namespace URI (different prefix)
		assertFilterEquals(ElementFilter(ns1a), 
				ElementFilter(ns1a))
		
		// same namespace URI (different prefix)
		assertFilterEquals(ElementFilter(ns1a), 
				ElementFilter(ns1b))
		
		// same namespace URI (different prefix)
		assertFilterEquals(ElementFilter(ns1a), 
				ElementFilter(ns1c))
		
		assertFilterNotEquals(ElementFilter("test", ns1a), 
				ElementFilter(ns1a))
		
		assertFilterNotEquals(ElementFilter("test"), 
				ElementFilter("testfoo"))

		assertFilterNotEquals(ElementFilter("test"), 
				ElementFilter("test", Namespace.NO_NAMESPACE))

		assertFilterNotEquals(ElementFilter("test", ns1a), 
				ElementFilter("test", Namespace.NO_NAMESPACE))

		assertFilterNotEquals(ElementFilter("test", Namespace.NO_NAMESPACE), 
				ElementFilter("test", ns1a))

		assertFilterNotEquals(ElementFilter(ns1a), 
				ElementFilter(ns2a))
		
		assertFilterNotEquals(ElementFilter(ns1c), 
				ElementFilter(ns2c))
		
		assertFilterNotEquals(ElementFilter(ns1c), 
				ElementFilter(ns1c).negate())
		
}

}
