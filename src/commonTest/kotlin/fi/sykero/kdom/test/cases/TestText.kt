/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.Content
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.Text

class TestText {

	@Test fun testTextString() {
		val txt = Text("  frodo  baggins  ")
		assertTrue("  frodo  baggins  ".equals(txt.value))
		assertTrue("  frodo  baggins  ".equals(txt.text))
		assertTrue("frodo baggins".equals(txt.textNormalize))
		assertTrue("frodo  baggins".equals(txt.textTrim))
	}
	
	@Test fun testNormalizeString() {
		assertEquals("", Text.normalizeString(null))
		assertEquals("", Text.normalizeString(""))
		assertEquals("boo", Text.normalizeString("boo"))
		assertEquals("boo", Text.normalizeString(" boo"))
		assertEquals("boo", Text.normalizeString("boo "))
		assertEquals("boo", Text.normalizeString("  boo  "))
		assertEquals("boo hoo", Text.normalizeString("boo hoo"))
		assertEquals("boo hoo", Text.normalizeString("boo\nhoo"))
		assertEquals("boo hoo", Text.normalizeString("boo \n hoo"))
		assertEquals("boo hoo", Text.normalizeString("boo \n hoo"))
		assertEquals("boo hoo", Text.normalizeString("\rboo\thoo\n"))
	}

	@Test fun testClone() {
		val txt = Text("frodo baggins")
		val clone = txt.clone()
		assertTrue(clone != null)
		assertTrue(txt !== clone)
		assertFalse(txt.equals(clone))
		assertFalse(clone.equals(txt))
		assertTrue("frodo baggins".equals(clone.text))
	}

	@Test fun testSetText() {
		val txt = Text("frodo baggins")
		assertTrue("frodo baggins".equals(txt.text))
		assertTrue(txt.setText("bilbo baggins") === txt)
		assertTrue("bilbo baggins".equals(txt.text))
	}

	@Test fun testAppendString() {
		val txt = Text("frodo baggins")
		assertTrue("frodo baggins".equals(txt.text))
		txt.append(" from the shire")
		assertTrue("frodo baggins from the shire".equals(txt.text))
		val app: String? = null
		txt.append(app)
		assertTrue("frodo baggins from the shire".equals(txt.text))
		txt.append("")
		assertTrue("frodo baggins from the shire".equals(txt.text))
		try {
			txt.append("New char data " + 0x05.toChar() + " with bad characters.")
		} catch (iae: IllegalDataException) {
			// good
		} catch (e: Exception) {
			fail ("Expected IllegalAddException, but got " + e::class.qualifiedName)
		}
	}

	@Test fun testAppendText() {
		val txt = Text("frodo baggins")
		assertTrue("frodo baggins".equals(txt.text))
		txt.append(Text(" from the shire"))
		assertTrue("frodo baggins from the shire".equals(txt.text))
		val app: Text? = null
		txt.append(app)
		assertTrue("frodo baggins from the shire".equals(txt.text))
	}

	@Test fun testToString() {
		val txt = Text("frodo baggins")
		assertTrue(txt.toString() != null)
	}

    @Test fun testCloneDetatchParentText() {
		val parent = Element("root")
		val content = Text("val")
		parent.addContent(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

    @Test fun testContentCType() {
    	assertTrue(Content.CType.Text === Text("").cType)
    }
}
