/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter

import kotlin.test.*
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.filter.AttributeFilter

class TestAtributeFilter : AbstractTestFilter() {

	@Test fun testAttributeFilter() {
		val ef = AttributeFilter()
		val eft = AttributeFilter()
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return c != null && c is Attribute
			}
		}
		exerciseAtt(ef, eft, getRoot(), cb)
		exerciseAtt(ef, eft, getDocument(), cb)
	}

	@Test fun testElementFilterString() {
		val name = "four"
		val ef = AttributeFilter(name)
		val eft = AttributeFilter(name)
		val cb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Attribute) &&
						name.equals(((c as Attribute)).name)
			}
		}
		exerciseAtt(ef, eft, getRoot(), cb)
		exerciseAtt(ef, eft, getDocument(), cb)
	}

	@Test fun testElementFilterNamespace() {
		val efa = AttributeFilter(Namespace.NO_NAMESPACE)
		val efat = AttributeFilter(Namespace.NO_NAMESPACE)
		val cba = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Attribute) &&
						Namespace.NO_NAMESPACE.equals(((c as Attribute)).namespace)
			}
		}
		exerciseAtt(efa, efat, getRoot(), cba)
		exerciseAtt(efa, efat, getDocument(), cba)

		val efb = AttributeFilter(getTestNamespace())
		val efbt = AttributeFilter(getTestNamespace())
		val cbb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Attribute) &&
						getTestNamespace().equals(((c as Attribute)).namespace)
			}
		}
		exerciseAtt(efb, efbt, getRoot(), cbb)
		exerciseAtt(efb, efbt, getDocument(), cbb)


	}

	@Test fun testElementFilterStringNamespace() {
		val namea = "four"
		val efa = AttributeFilter(namea, Namespace.NO_NAMESPACE)
		val efat = AttributeFilter(namea, Namespace.NO_NAMESPACE)
		val cba = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Attribute) &&
						namea.equals(((c as Attribute)).name) && 
						Namespace.NO_NAMESPACE.equals(((c as Attribute)).namespace)
			}
		}
		exerciseAtt(efa, efat, getRoot(), cba)
		exerciseAtt(efa, efat, getDocument(), cba)

		val nameb = "three"
		val efb = AttributeFilter(nameb, getTestNamespace())
		val efbt = AttributeFilter(nameb, getTestNamespace())
		val cbb = object : CallBack {
			override fun isValid(c: Any?): Boolean {
				return (c != null) && (c is Attribute) &&
						nameb.equals(((c as Attribute)).name) && 
						getTestNamespace().equals(((c as Attribute)).namespace)
			}
		}
		exerciseAtt(efb, efbt, getRoot(), cbb)
		exerciseAtt(efb, efbt, getDocument(), cbb)

	}

	@Test fun testEqualsObject() {
		val URI1 = "http://jdom.org/test1"
		val URI2 = "http://jdom.org/test2"
		val ns1a = Namespace.getNamespace("pfxa", URI1)
		val ns1b = Namespace.getNamespace("pfxb", URI1)
		val ns1c = Namespace.getNamespace(URI1)

		val ns2a = Namespace.getNamespace("pfxa", URI2)
		val ns2c = Namespace.getNamespace(URI2)

		assertFilterEquals(AttributeFilter("test"), 
				AttributeFilter("test"))

		// same namespace
		assertFilterEquals(AttributeFilter("test", ns1a), 
				AttributeFilter("test", ns1a))

		// same namespace URI (different prefix)
		assertFilterEquals(AttributeFilter("test", ns1a), 
				AttributeFilter("test", ns1b))

		// same namespace URI (different prefix)
		assertFilterEquals(AttributeFilter("test", ns1a), 
				AttributeFilter("test", ns1c))

		// same namespace URI (different prefix)
		assertFilterEquals(AttributeFilter(ns1a), 
				AttributeFilter(ns1a))

		// same namespace URI (different prefix)
		assertFilterEquals(AttributeFilter(ns1a), 
				AttributeFilter(ns1b))

		// same namespace URI (different prefix)
		assertFilterEquals(AttributeFilter(ns1a), 
				AttributeFilter(ns1c))

		assertFilterNotEquals(AttributeFilter("test", ns1a), 
				AttributeFilter(ns1a))

		assertFilterNotEquals(AttributeFilter("test"), 
				AttributeFilter("testfoo"))

		assertFilterNotEquals(AttributeFilter("test"), 
				AttributeFilter("test", Namespace.NO_NAMESPACE))

		assertFilterNotEquals(AttributeFilter("test", ns1a), 
				AttributeFilter("test", Namespace.NO_NAMESPACE))

		assertFilterNotEquals(AttributeFilter("test", Namespace.NO_NAMESPACE), 
				AttributeFilter("test", ns1a))

		assertFilterNotEquals(AttributeFilter(ns1a), 
				AttributeFilter(ns2a))

		assertFilterNotEquals(AttributeFilter(ns1c), 
				AttributeFilter(ns2c))

		assertFilterNotEquals(AttributeFilter(ns1c), 
				AttributeFilter(ns1c).negate())

	}

}
