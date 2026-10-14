/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalAddException
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.Text
import fi.sykero.kdom.internal.ArrayCopy
import fi.sykero.kdom.test.util.AbstractTestList
import fi.sykero.kdom.test.util.javaAsList

class TestAttributeList : AbstractTestList<Attribute>(Attribute::class, false) {
	
	private val base: Element = Element("dummy")


	
	override fun buildEmptyList(): MutableList<Attribute> {
		base.attributes.clear()
		return base.attributes
	}

	override fun buildSampleContent(): Array<Attribute> {
		return arrayOf<Attribute>( Attribute("zero", "val"), 
				Attribute("one", "val"), Attribute("two", "val"), 
				Attribute("three", "val"), Attribute("four", "val"),
				Attribute("five", "val"), Attribute("six", "val"),
				Attribute("att", "val", Namespace.getNamespace("pfx", "nsX")),
		        Attribute("sec", "val", Namespace.getNamespace("pfx", "nsX"))
		)
	}
	
	

	override fun buildAdditionalContent(): Array<Attribute> {
		return arrayOf<Attribute>( Attribute("seven", "val"),
				Attribute("eight", "val"))
	}

	override fun buildIllegalClassContent(): Array<Any?> {
		val ret = arrayOf<Any?>(
				Text("Hello"),
				Comment("Hello!")
		)
		return ret
	}
	
	override fun buildIllegalArgumentContent(): Array<Attribute> {
		// this is illegal because it redefines the namespace from uri nsY to nsZ
		return arrayOf<Attribute>(
				Attribute("att", "val", Namespace.getNamespace("pfx", "nsY")),
				Attribute("att", "val", Namespace.getNamespace("pfx", "nsZ"))
				
		)
	}
	
	@BeforeTest fun detatchAll() {
		// make sure all content is detached before each test.
		for (c in buildSampleContent()) {
			c.detach()
		}
	}
	
	@Test fun testDuplicateAttribute() {
		val emt = Element("mine")
		val attlist = emt.attributes
		val att = Attribute("hi", "there")
		val frodo = Attribute("hi", "frodo")
		val bilbo = Attribute("boo", "bilbo")
		attlist.add(att)
		assertFailsWith<IllegalAddException> {
			val e2 = Element("gandalph")
			e2.setAttribute(frodo)
			attlist.add(frodo)
		}
		frodo.detach()
		
		// adding one 'hi' attribute should displace the other.
		assertTrue(att.parent === emt)
		assertTrue(attlist.add(frodo))
		assertTrue(att.parent == null)
		assertTrue(attlist.add(att))
		assertTrue(att.parent === emt)
		assertTrue(att === att.detach())
		
		// list is now empty.
		assertTrue(attlist.isEmpty())
		assertTrue(attlist.add(frodo))
		assertTrue(attlist.add(bilbo))
		assertTrue(frodo === attlist.set(0, att))
		
		assertFailsWith<IllegalAddException> {
			attlist.add(attlist.size, frodo)
		}
		assertFailsWith<IllegalAddException> {
			attlist.set(1, frodo)
		}
	}
	
	@Test fun testAttributeNamspaceCollision() {
		val emt = Element("mine")
		val attlist = emt.attributes
		val atta = Attribute("hi", "there", Namespace.getNamespace("mypfx", "nsa"))
		val attb = Attribute("hi", "there", Namespace.getNamespace("mypfx", "nsb"))
		attlist.add(atta)
		assertFailsWith<IllegalAddException> {
			// cannot add two different namespaces with same prefix.
			attlist.add(attb)
		}
		val attc = Attribute("bilbo", "baggins", Namespace.getNamespace("mypfc", "nsc"))
		// can add a different prefix.
		attlist.add(attc)
		assertFailsWith<IllegalAddException> {
			// cannot set an attribute that causes a conflict.
			attlist.set(1, attb)
		}
		
		// but you can swap an existing attribute with a different one that changes
		// the namespace URI for a prefix.
		attlist.set(0, attb)
		
	}
	
	@Test fun testSetAttributes() {
		val extra: Array<Attribute> = buildAdditionalContent()
		if (extra.size <= 0) {
			// android
			//Assume.assumeTrue(extra.length > 0);
			return
		}
		val content: Array<Attribute> = buildSampleContent()
		if (content.size <= 0) {
			// android
			// Assume.assumeTrue(content.length > 0);
			return
		}

		// populate the list.
		val list = buildEmptyList()
		assertTrue(list.addAll(0, javaAsList(content)))
		quickCheck(list, content)
		
		// OK, we have a list of attributes.... behind the scenes, we have an
		// an Element too... we need the element to get the setAttributes()
		// method which in turn accesses the clearAndSet().
		val myelement = list.get(0).parent
		assertNotNull(myelement)
		
		var toset = ArrayList<Attribute>(extra.size)
		toset.addAll(javaAsList(extra))
		
		// OK, test the setAttributes first.
		assertTrue(myelement === myelement.setAttributes(toset))
		// attributes should be the new ones.
		quickCheck(list, extra)
		// restore the old ones.
		assertTrue(myelement === myelement.setAttributes(javaAsList(content)))
		// ensure an empty list clears...
		toset.clear()
		assertTrue(myelement === myelement.setAttributes(toset))
		assertTrue(list.isEmpty())
		// restore the old ones.
		assertTrue(myelement === myelement.setAttributes(javaAsList(content)))
		// ensure a null list clears...
		assertTrue(myelement === myelement.setAttributes(null))
		assertTrue(list.isEmpty())
		
		
	}
	
	
	@Test fun testIllegalSetAttributes() {
		val illegal: Array<Attribute> = buildIllegalArgumentContent()
		if (illegal.size <= 0) {
			//Assume.assumeTrue(illegal.length > 0);
			return
		}
		val extra: Array<Attribute> = buildAdditionalContent()
		if (extra.size <= 0) {
			// Assume.assumeTrue(extra.length > 0);
			return
		}
		val content: Array<Attribute> = buildSampleContent()
		if (content.size <= 0) {
			//Assume.assumeTrue(content.length > 0);
			return
		}
		// the ' + 1' ensures a null value too!
		val toadd: Array<Attribute> = ArrayCopy.copyOf(extra, extra.size + illegal.size + 1)
		illegal.copyInto(toadd, extra.size, 0, 0 + illegal.size)
		
		// right, we have legal content in 'content', and then in 'illegal' we
		// have some legal content, and then some illegal content.
		
		// populate the list.
		val list = buildEmptyList()
		assertTrue(list.addAll(0, javaAsList(content)))
		quickCheck(list, content)
		
		// OK, we have a list of attributes.... behind the scenes, we have an
		// an Element too... we need the element to get the setAttributes()
		// method which in turn accesses the clearAndSet().
		val myelement = list.get(0).parent
		assertNotNull(myelement)
		
		// check that the first to-add can be added.
		list.add(0, toadd[0])
		//then remove it again.
		assertTrue(toadd[0] === list.removeAt(0))
		
		quickCheck(list, content)
		
		// now, add the illegal, and then inspect the list...
		assertFailsWith<IllegalArgumentException> {
			myelement.setAttributes(javaAsList(toadd))
		}
		
		// make sure that the member that previously could be added can
		// still be added.
		list.add(0, toadd[0])
		//then remove it again.
		assertTrue(toadd[0] === list.removeAt(0))
		
		// make sure it's all OK.
		exercise(list, *content)
		
		if (content.size < 2) {
			//Assume.assumeTrue(content.length >= 2);
			return
		}
		
		// now check to make sure that concurrency is not affected....
		val it = list.iterator()
		// move it along at least once.....
		assertTrue(content[0] === it.next())
		// now do a failed addAll.
		assertFailsWith<IllegalArgumentException> {
			myelement.setAttributes(javaAsList(toadd))
		}
		// we should be able to move the iterator because the modCount should
		// not have been affected.....
		assertTrue(content[1] === it.next())
	}
	
	@Test fun testAlreadyHasParent() {
		// create an attribute.
		val att = Attribute("att", "val")
		// give this attribute a parent.
		val parent = Element("parent")
		parent.setAttribute(att)
		
		// none will have no attributes.
		val none = Element("none")
		// same will have an attribute with the same name as att.
		val same = Element("same")
		same.setAttribute("att", "val")
		// other will have an attribute not the same as att.
		val other = Element("other")
		other.setAttribute("diff", "other")
		
		assertFailsWith<IllegalAddException> {
			// cannot set an attribute with an existing parent.
			none.setAttribute(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot set an attribute with an existing parent.
			same.setAttribute(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot set an attribute with an existing parent.
			other.setAttribute(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			none.attributes.add(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			same.attributes.add(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			other.attributes.add(att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			none.attributes.add(0, att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			same.attributes.add(0, att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			other.attributes.add(0, att)
		}

		assertFailsWith<IndexOutOfBoundsException> {
			// cannot set an empty value.
			// cannot add an attribute with an existing parent.
			none.attributes.set(0, att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			same.attributes.set(0, att)
		}
		
		assertFailsWith<IllegalAddException> {
			// cannot add an attribute with an existing parent.
			other.attributes.set(0, att)
		}
		
		assertEquals("val", same.getAttributeValue("att"))
		assertEquals("other", other.getAttributeValue("diff"))
		assertFalse(none.hasAttributes())
		
	}
	
}
