/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.test.util.UnitTestUtil.checkException
import fi.sykero.kdom.test.util.UnitTestUtil.failNoException
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Element
import fi.sykero.kdom.internal.ArrayCopy
import fi.sykero.kdom.test.util.AbstractTestList
import fi.sykero.kdom.test.util.javaAsList

class TestContentList : AbstractTestList<Content>(Content::class, false) {

	
	override fun buildEmptyList(): MutableList<Content> {
		val e = Element("dummy")
		return e.content
	}

	override fun buildSampleContent(): Array<Content> {
		return arrayOf<Content>( Element("zero"), 
				Element("one"), Element("two"), 
				Element("three"), Element("four"),
				Element("five"), Element("six"))
	}
	
	override fun buildAdditionalContent(): Array<Content> {
		return arrayOf<Content>( Element("seven"), 
				Element("eight"))
	}

	override fun buildIllegalClassContent(): Array<Any?> {
		val ret = arrayOf<Any?>(
				10,
				StringBuilder("Hello!")
		)
		return ret
	}
	
	override fun buildIllegalArgumentContent(): Array<Content> {
		return arrayOf<Content>(DocType("root"))
	}
	
	@BeforeTest fun detatchAll() {
		// make sure all content is detached before each test.
		for (c in buildSampleContent()) {
			c.detach()
		}
	}
	
	@Test fun testIllegalSetContent() {
		val illegal: Array<Content> = buildIllegalArgumentContent()
		if (illegal.size <= 0) {
			//Assume.assumeTrue(illegal.length > 0);
			return
		}
		val extra: Array<Content> = buildAdditionalContent()
		if (extra.size <= 0) {
			//Assume.assumeTrue(extra.length > 0);
			return
		}
		val content: Array<Content> = buildSampleContent()
		if (content.size <= 0) {
			// Assume.assumeTrue(content.length > 0);
			return
		}
		// the ' + 1' ensures a null value too!
		val toadd: Array<Content> = ArrayCopy.copyOf(extra, extra.size + illegal.size + 1)
		illegal.copyInto(toadd, extra.size, 0, 0 + illegal.size)
		
		// right, we have legal content in 'content', and then in 'illegal' we
		// have some legal content, and then some illegal content.
		
		// populate the list.
		val list = buildEmptyList()
		assertTrue(list.addAll(0, javaAsList(content)))
		quickCheck(list, content)
		
		// OK, we have a list of attributes.... behind the scenes, we have an
		// an Element too... we need the element to get the setContents()
		// method which in turn accesses the clearAndSet().
		val myelement = list.get(0).parentElement
		assertNotNull(myelement)
		
		// check that the first to-add can be added.
		list.add(0, toadd[0])
		//then remove it again.
		assertTrue(toadd[0] === list.removeAt(0))
		
		quickCheck(list, content)
		
		// now, add the illegal, and then inspect the list...
		assertFailsWith<IllegalArgumentException> {
			myelement.setContent(javaAsList(toadd))
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
			myelement.setContent(javaAsList(toadd))
		}
		// we should be able to move the iterator because the modCount should
		// not have been affected.....
		assertTrue(content[1] === it.next())
	}
	
}
