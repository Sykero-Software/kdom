/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter

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
import fi.sykero.kdom.filter.AttributeFilter
import fi.sykero.kdom.filter.ContentFilter
import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.test.util.UnitTestUtil
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Base for the filter tests: a fixed document, and the "exercise" checks that run a filter (and the filters derived from
 * it by negate/or/and/refine) against that content.
 *
 * K5: jdom2 compared each filter with a deserialized copy of itself (equal, but another instance). There is no
 * serialization any more, so [exercise]/[exerciseAtt] take a second, separately constructed filter that must be equal
 * to the first (`twin`), and every derived filter is built a second time from the twin.
 */
abstract class AbstractTestFilter {

	/** Compares two filters that are not supposed to be equal. */
	protected fun assertFilterNotEquals(a: Filter<*>?, b: Filter<*>?) {
		assertTrue(a != null, "A Filter is null.")
		assertTrue(b != null, "B Filter is null.")
		assertTrue(!a.equals(null))
		assertTrue(!b.equals(null))
		if (a.equals(b) || b.equals(a)) {
			fail("Filters are equals(), but they are not supposed to be: " +
					a.toString() + " and " + b.toString())
		}
		if (a.hashCode() == b.hashCode()) {
			println("Two different (not equals() ) Filters have " +
					"the same hashCode(): " + a.hashCode() + "\n   " +
					a.toString() + " \n   " + b.toString())
		}
		val base: Filter<Content> = ContentFilter()
		assertFalse(base.refine(a).equals(base.refine(b)))
	}

	/** Compares two filters that are supposed to be equal (and have equal hash codes). */
	protected fun assertFilterEquals(a: Filter<*>?, b: Filter<*>?) {
		assertTrue(a != null, "A Filter is null.")
		assertTrue(b != null, "B Filter is null.")
		assertTrue(!a.equals(null))
		assertTrue(!b.equals(null))
		if (!a.equals(a)) {
			fail("Filter " + a.toString() + " is not equals() to itself")
		}
		if (!b.equals(b)) {
			fail("Filter " + b.toString() + " is not equals() to itself")
		}
		if (!a.equals(b)) {
			fail("Filters are not equals(), but they are supposed to be: " +
					a.toString() + " and " + b.toString())
		}
		if (!b.equals(a)) {
			fail("Filters a.equals(b), but not b.equals(a) : " +
					a.toString() + " and " + b.toString())
		}
		if (a.hashCode() != b.hashCode()) {
			fail("Both filters are equals(), but their hashCode() values differ: " +
					a.toString() + " and " + b.toString())
		}
		val base: Filter<Content> = ContentFilter()
		assertTrue(base.refine(a).equals(base.refine(b)))
	}

	protected fun interface CallBack {
		fun isValid(c: Any?): Boolean
	}

	protected inner class NegateCallBack(private val basecallback: CallBack) : CallBack {
		override fun isValid(c: Any?): Boolean {
			return !basecallback.isValid(c)
		}
	}

	protected inner class AndCallBack(private val onecallback: CallBack, private val twocallback: CallBack) : CallBack {
		override fun isValid(c: Any?): Boolean {
			// do not want to do short-circuit || logic.
			// Make separate statements
			val one = onecallback.isValid(c)
			val two = twocallback.isValid(c)
			return one && two
		}
	}

	protected inner class OrCallBack(private val onecallback: CallBack, private val twocallback: CallBack) : CallBack {
		override fun isValid(c: Any?): Boolean {
			// do not want to do short-circuit || logic.
			// Make separate statements
			val one = onecallback.isValid(c)
			val two = twocallback.isValid(c)
			return one || two
		}
	}

	private class TrueCallBack : CallBack {
		override fun isValid(c: Any?): Boolean {
			return true
		}
	}

	private class FalseCallBack : CallBack {
		override fun isValid(c: Any?): Boolean {
			return false
		}
	}

	private val doc: Document
	private val root: Element = Element("root")
	private val testns: Namespace = Namespace.getNamespace("testns", "http://jdom.org/testns")
	private val rootcontent: Array<Content>
	private val doccontent: Array<Content>

	init {
		val zero = Element("zero")
		val one = Element("one")
		val two = Element("two")
		val three = Element("three", testns)
		val four = Element("four")
		val five = Element("five", testns)
		val six = Element("six")
		val seven = Element("seven")

		val e0 = EntityRef("erent", "ERSystemID")

		val t0 = Text("t0")
		val t1 = Text("t1")
		val t2 = Text("t2")
		val t3 = Text("t3")
		val t4 = Text("t4")
		val t5 = Text("t5")
		val t6 = Text("t6")
		val t7 = Text("t7")
		val t8 = Text("t8")
		val t9 = Text("t9")

		val c0 = CDATA("c0")
		val c1 = CDATA("c1")

		val com0 = Comment("Comment0")
		val com1 = Comment("Comment1")
		val com2 = Comment("Comment2")

		val doctype = DocType("root")
		val pi = ProcessingInstruction("dummy", "name=value")
		val doccom = Comment("DocComment")
		doc = Document()

		doc.addContent(doctype)
		doc.addContent(pi)
		doc.addContent(doccom)
		doc.addContent(root)

		doccontent = arrayOf<Content>(doctype, pi, doccom, root)

		rootcontent = arrayOf<Content>(t0, com0, t1, zero, t2, one, t3, com1,
				t4, two, t5, three, c0, four, t6, e0, five, t7, six, c1, t8,
				seven, t9, com2)

		for (c in rootcontent) {
			root.addContent(c)
		}
	}

	protected fun getDocument(): Document {
		return doc
	}

	protected fun getRoot(): Element {
		return root
	}

	protected fun getDocumentContent(): Array<Content> {
		return doccontent
	}

	protected fun getRootContent(): Array<Content> {
		return rootcontent
	}

	protected fun getTestNamespace(): Namespace {
		return testns
	}

	protected fun filter(input: Array<Content>, vararg types: KClass<*>): Array<Content> {
		val al = ArrayList<Content>(input.size)
		content@ for (c in input) {
			for (cclass in types) {
				if (cclass.isInstance(c)) {
					al.add(c)
					continue@content
				}
			}
		}
		return al.toTypedArray()
	}

	/**
	 * Runs [af] against [parent] and checks it against [callback].
	 * @param twin a separately constructed filter that is equal to [af] (not the same instance)
	 */
	protected fun <F : Content> exercise(af: Filter<F>?, twin: Filter<F>?, parent: Parent?, callback: CallBack?) {
		assertTrue(af != null, "filter is null")
		assertTrue(twin != null, "twin filter is null")
		assertTrue(parent != null, "list is null")
		assertTrue(callback != null, "callback is null")
		assertTrue(twin !== af, "the twin must be another instance")
		assertTrue(af.toString() != null) // basic test to ensure toString is run.
		// can never match null if it returns a <F extends Content>
		assertFalse(af.matches(null))
		// can never match Object if it returns a <F extends Content>
		assertFalse(af.matches(Any()))
		assertFilterEquals(af, twin)
		exerciseCore(af, twin, parent, callback)
		exerciseCore(twin, af, parent, callback)

		assertFailsWith<RuntimeException>("expected an exception from af.or(null)") {
			af.or(null)
		}

		assertFailsWith<RuntimeException>("expected an exception from af.and(null)") {
			af.and(null)
		}

		val c = Filters.content()
		exerciseCore(af.negate().refine(c), twin.negate().refine(c), parent, NegateCallBack(callback))
		exerciseCore(af.or(af.negate()).refine(c), twin.or(twin.negate()).refine(c), parent, TrueCallBack())
		exerciseCore(af.or(twin).refine(c), twin.or(af).refine(c), parent, callback)
		exerciseCore(af.negate().and(af).refine(c), twin.negate().and(twin).refine(c), parent, FalseCallBack())
		exerciseCore(af.and(af).refine(c), twin.and(twin).refine(c), parent, callback)
		exerciseCore(af.and(twin).refine(c), twin.and(af).refine(c), parent, callback)

		val nf = af.negate()
		val nft = twin.negate()
		exerciseCore(nf.negate().refine(c), nft.negate().refine(c), parent, callback)
		exerciseCore(nf.or(nf.negate()).refine(c), nft.or(nft.negate()).refine(c), parent, TrueCallBack())
		exerciseCore(nf.and(nf.negate()).refine(c), nft.and(nft.negate()).refine(c), parent, FalseCallBack())

		val afor = twin.or(nf)
		val bfor = nf.or(af)
		assertFilterEquals(afor, bfor)

		val afand = twin.and(nf)
		val bfand = nf.and(af)
		assertFilterEquals(afand, bfand)

		assertFalse(af.equals(null))
		assertFalse(nf.equals(null))
		assertFalse(afor.equals(null))
		assertFalse(bfor.equals(null))
		assertFalse(afand.equals(null))
		assertFalse(bfand.equals(null))
	}

	/** [ef] is exercised; [cf] is an equal, separately constructed filter. */
	private fun exerciseCore(ef: Filter<out Content>, cf: Filter<out Content>, parent: Parent, callback: CallBack) {
		// exercise the toString()
		assertTrue(ef.toString() != null)
		val oc = ArrayList<Content>()
		val mc = ArrayList<Content>()
		val cont = parent.content
		for (c in cont) {
			oc.add(c)
			assertTrue(parent === c.parent)
			if (parent is Document) {
				assertTrue(null == c.parentElement)
			} else {
				assertTrue(parent === c.parentElement)
			}
			val mat = ef.matches(c)
			if (mat) {
				mc.add(c)
			}
			val cbv = callback.isValid(c)
			if (mat != cbv) {
				fail("Filter " + ef + " returned " + mat
						+ " but isValid CallBack returned " + cbv
						+ " for value " + c)
			}
		}
		val fc = ef.filter(oc)
		assertTrue(fc is RandomAccess)
		assertTrue(fc.size == mc.size)
		for (i in 0 until fc.size) {
			assertTrue(fc.get(i) === mc.get(i))
		}
		assertFilterEquals(cf, ef)
		val xf = ContentFilter()
		assertFilterEquals(cf.refine(xf), ef.refine(xf))
		assertFilterEquals(xf.refine(cf), xf.refine(ef))
		assertFalse(ef.equals(null))
		assertFilterNotEquals(ef, ef.negate())
		assertFilterNotEquals(ef.refine(xf), ef.negate().refine(xf))
		assertFilterNotEquals(xf.refine(ef), xf.refine(ef.negate()))
		val depth = ArrayList<Content>()
		depth.addAll(cont)
		var sz = depth.size
		for (i in 0 until sz) {
			if (depth.get(i) is Element) {
				val kdata = ((depth.get(i) as Element)).content
				depth.addAll(i + 1, kdata)
				sz += kdata.size
			}
		}
		var di: Iterator<Any?> = parent.descendants

		// confirm that the DepthIterator iterates over all content.
		UnitTestUtil.testReadIterator(di, *depth.toTypedArray())

		val filtered = ArrayList<Content>(depth.size)
		val it = depth.iterator()
		while (it.hasNext()) {
			val c = it.next()
			if (callback.isValid(c)) {
				filtered.add(c)
			}
		}

		//
		di = parent.getDescendants(ef)
		UnitTestUtil.testReadIterator(di, *filtered.toTypedArray())
		assertEquals(filtered, ef.filter(depth))
	}

	/**
	 * As [exercise] for attribute filters.
	 * @param twin a separately constructed filter that is equal to [af] (not the same instance)
	 */
	protected fun <F : Attribute> exerciseAtt(af: Filter<F>?, twin: Filter<F>?, parent: Parent?, callback: CallBack?) {
		assertTrue(af != null, "filter is null")
		assertTrue(twin != null, "twin filter is null")
		assertTrue(parent != null, "list is null")
		assertTrue(callback != null, "callback is null")
		assertTrue(twin !== af, "the twin must be another instance")
		assertTrue(af.toString() != null) // basic test to ensure toString is run.
		// can never match null if it returns a <F extends Content>
		assertFalse(af.matches(null))
		// can never match Object if it returns a <F extends Content>
		assertFalse(af.matches(Any()))
		assertFilterEquals(af, twin)
		exerciseCoreAtt(af, twin, parent, callback)
		exerciseCoreAtt(twin, af, parent, callback)
		assertFailsWith<RuntimeException>("expected an exception from af.or(null)") {
			af.or(null)
		}

		assertFailsWith<RuntimeException>("expected an exception from af.and(null)") {
			af.and(null)
		}

		val a = Filters.attribute()
		//exerciseCoreAtt(af.negate().refine(Filters.attribute()), parent, new NegateCallBack(callback));
		//exerciseCoreAtt(af.or(af.negate()).refine(Filters.attribute()), parent, new TrueCallBack());
		exerciseCoreAtt(af.or(twin).refine(a), twin.or(af).refine(a), parent, callback)
		//exerciseCoreAtt(af.negate().and(af).refine(Filters.attribute()), parent, new FalseCallBack());
		exerciseCoreAtt(af.and(af).refine(a), twin.and(twin).refine(a), parent, callback)
		exerciseCoreAtt(af.and(twin).refine(a), twin.and(af).refine(a), parent, callback)

		val nf = af.negate()
//		exerciseCore(nf.negate().refine(Filters.content()), parent, callback);
//		exerciseCore(nf.or(nf.negate()).refine(Filters.content()), parent, new TrueCallBack());
//		exerciseCore(nf.and(nf.negate()).refine(Filters.content()), parent, new FalseCallBack());

		val afor = twin.or(nf)
		val bfor = nf.or(af)
		assertFilterEquals(afor, bfor)

		val afand = twin.and(nf)
		val bfand = nf.and(af)
		assertFilterEquals(afand, bfand)

		assertFalse(af.equals(null))
		assertFalse(nf.equals(null))
		assertFalse(afor.equals(null))
		assertFalse(bfor.equals(null))
		assertFalse(afand.equals(null))
		assertFalse(bfand.equals(null))
	}

	/** [ef] is exercised; [cf] is an equal, separately constructed filter. */
	private fun exerciseCoreAtt(ef: Filter<out Attribute>, cf: Filter<out Attribute>, parent: Parent, callback: CallBack) {
		// exercise the toString()
		assertTrue(ef.toString() != null)
		val cont = parent.content
		for (c in cont) {
			assertTrue(parent === c.parent)
			if (parent is Document) {
				assertTrue(null == c.parentElement)
			} else {
				assertTrue(parent === c.parentElement)
			}
			val mat = ef.matches(c)
			val cbv = callback.isValid(c)
			if (mat != cbv) {
				fail("Filter " + ef + " returned " + mat
						+ " but isValid CallBack returned " + cbv
						+ " for value " + c)
			}
		}
		assertFilterEquals(cf, ef)
		val xf = AttributeFilter()
		assertFilterEquals(cf.refine(xf), ef.refine(xf))
		assertFilterEquals(xf.refine(cf), xf.refine(ef))
		assertFalse(ef.equals(null))
		assertFilterNotEquals(ef, ef.negate())
		assertFilterNotEquals(ef.refine(xf), ef.negate().refine(xf))
		assertFilterNotEquals(xf.refine(ef), xf.refine(ef.negate()))

		val cnt = ArrayList<Any>()
		val atts = ArrayList<Attribute>()
		val itc = parent.descendants
		while (itc.hasNext()) {
			val c = itc.next()
			cnt.add(c)
			if (c is Element) {
				cnt.addAll(c.attributes)
				for (a in c.attributes) {
					if (ef.matches(a)) {
						atts.add(a)
					}
				}
			}
		}

		val filtered = ef.filter(cnt)

		assertEquals(atts, filtered)
	}
}
