/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.test.util.UnitTestUtil.checkEquals
import fi.sykero.kdom.test.util.UnitTestUtil.cloneString
//JAVA java.util.Collections
//JAVA java.util.LinkedHashMap
//JAVA java.util.Map
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.IllegalNameException
import fi.sykero.kdom.IllegalTargetException
import fi.sykero.kdom.ProcessingInstruction

class TestProcessingInstruction {

	@Test fun testProcessingInstructionString() {
		val pi = ProcessingInstruction("test")
		checkEquals(pi.target, "test")
		checkEquals(pi.value, "")
	}

	@Test fun testProcessingInstructionStringString() {
		val pi = ProcessingInstruction("test", "key='value'")
		checkEquals(pi.target, "test")
		checkEquals(pi.value, "key='value'")
	}

	@Test fun testProcessingInstructionStringMap() {
		val kvs = buildMap(
				"key1", "val1",
				"key2", "val2"
				)
		val pi = ProcessingInstruction("test", kvs)
		checkEquals(pi.target, "test")
		assertTrue(pi.value != null)
		checkMapValues(pi, kvs)
	}
	
	@Test fun testIllegalTargetContentA() { assertFailsWith<IllegalTargetException> {
		ProcessingInstruction("1tgt", "data")
	} }
	
	@Test fun testIllegalTargetContentB() { assertFailsWith<IllegalTargetException> {
		ProcessingInstruction(" tgt", "data")
	} }
	
	@Test fun testIllegalTargetContentC() { assertFailsWith<IllegalTargetException> {
		ProcessingInstruction("tg?>t", "data")
	} }
	
	@Test fun testIllegalTargetContentD() { assertFailsWith<IllegalTargetException> {
		ProcessingInstruction("tgt ", "data")
	} }
	
	@Test fun testIllegalTargetContentE() { assertFailsWith<IllegalTargetException> {
		ProcessingInstruction("t gt", "data")
	} }
	
	@Test fun testIllegalTargetContentF() { assertFailsWith<IllegalTargetException> {
		val pi = ProcessingInstruction("tgt", "data")
		pi.setTarget("tg t")
	} }
	
	@Test fun testIllegalTargetContentG() { assertFailsWith<IllegalTargetException> {
		val pi = ProcessingInstruction("tgt", "data")
		pi.setTarget("tgt ")
	} }
	
	@Test fun testIllegalTargetContentH() { assertFailsWith<IllegalTargetException> {
		val pi = ProcessingInstruction("tgt", "data")
		pi.setTarget("tg?t")
	} }
	
	@Test fun testIllegalAttnameA() { assertFailsWith<IllegalDataException> {
		val pi = ProcessingInstruction("tgt", "data")
		pi.setPseudoAttribute("tg?>t", "val")
	} }
	
	@Test fun testIllegalAttnameB() { assertFailsWith<IllegalDataException> {
		val pi = ProcessingInstruction("tgt", buildMap("ok", "val"))
		pi.setPseudoAttribute("b?>a", "val")
	} }
	
	@Test @Ignore fun testIllegalAttnameC() { assertFailsWith<IllegalNameException> {
		ProcessingInstruction("tgt", buildMap("ok", "val", "b a", "val"))
	} }
	
	@Test @Ignore fun testIllegalAttValueA() { assertFailsWith<IllegalDataException> {
		ProcessingInstruction("tgt", buildMap("ok", "val", " bad", "va'''\"\"\"l"))
	} }
	
	@Test fun testIllegalAttValueB() { assertFailsWith<IllegalDataException> {
		ProcessingInstruction("tgt", buildMap("ok", "val", "b a", "v?>al"))
	} }
	
	@Test fun testIllegalAttValueC() { assertFailsWith<IllegalDataException> {
		val pi = ProcessingInstruction("tgt", buildMap("ok", "val"))
		pi.setPseudoAttribute("b a", "v?>al")
	} }
	
	@Test fun testIllegalData() { assertFailsWith<IllegalDataException> {
		val pi = ProcessingInstruction("tgt", buildMap("ok", "val"))
		pi.setData("b?>a=val")
	} }
	
	private fun checkMapValues(pi: ProcessingInstruction, vals: MutableMap<String, String>) {
		for (me in vals.entries) {
			checkEquals(pi.getPseudoAttributeValue(me.key), me.value)
		}
		for (s in pi.pseudoAttributeNames) {
			if (!vals.containsKey(s)) {
				fail("ProcessingInstruction has key " + s + " which is not expected.")
			}
		}
	}
	
	private fun buildMap(vararg kvpairs: String): MutableMap<String, String> {
		assertTrue(kvpairs != null)
		assertTrue(kvpairs.size % 2 == 0)
		val lhm = LinkedHashMap<String, String>()
		for (i in 0 until kvpairs.size step 2) {
			lhm.put(kvpairs[i], kvpairs[i+1])
		}
		return lhm
	}

	@Test fun testGetValue() {
		val pi = ProcessingInstruction("test", "value")
		checkEquals(cloneString("value"), pi.value)
	}

	@Test fun testCloneA() {
		val pi = ProcessingInstruction("test", "value")
		val copy = pi.clone()
		assertTrue(!pi.equals(copy))
		checkEquals(pi.target, copy.target)
		checkEquals(pi.value, copy.value)
	}

	@Test fun testCloneB() {
		val pi = ProcessingInstruction("test", "")
		val copy = pi.clone()
		assertTrue(!pi.equals(copy))
		checkEquals(pi.target, copy.target)
		checkEquals(pi.value, copy.value)
	}

	@Test fun testCloneC() {
		val pi = ProcessingInstruction("test", "")
		pi.setPseudoAttribute("hi", "val")
		pi.removePseudoAttribute("hi")
		val copy = pi.clone()
		assertTrue(!pi.equals(copy))
		checkEquals(pi.target, copy.target)
		checkEquals(pi.value, copy.value)
	}

	@Test fun testCloneD() {
		val empty = emptyMap<String, String>()
		val pi = ProcessingInstruction("test", empty)
		val copy = pi.clone()
		assertTrue(!pi.equals(copy))
		checkEquals(pi.target, copy.target)
		checkEquals(pi.value, copy.value)
	}


	@Test fun testSetTarget() {
		val pi = ProcessingInstruction("test", "value")
		checkEquals(pi.target, "test")
		checkEquals(pi.value, "value")
		assertTrue(pi === pi.setTarget("test2"))
		checkEquals(pi.target, "test2")
		assertTrue(pi === pi.setTarget("test"))
		checkEquals(pi.target, "test")
	}

	@Test fun testGetTarget() {
		val pi = ProcessingInstruction("test", "value")
		checkEquals(pi.target, "test")
		checkEquals(pi.value, "value")
		pi.setTarget("test2")
		checkEquals(pi.target, "test2")
		pi.setTarget("test")
		checkEquals(pi.target, "test")
	}

	@Test fun testGetData() {
		val pi = ProcessingInstruction("test", "value")
		checkEquals(pi.target, "test")
		checkEquals(pi.value, "value")
		checkEquals(pi.data, "value")
	}

	@Test fun testToString() {
		val pi = ProcessingInstruction("test", "value")
		assertTrue(pi.toString() != null)
	}
	
	@Test fun testExercise() {
		val data = LinkedHashMap<String, String>()
		for (i in 0 until 10) {
			data.put("key" + i.toString().padStart(2, '0'), " val " + i.toString().padStart(2) + " ")
			val td = LinkedHashMap<String, String>()
			td.putAll(data)
			exercise(td)
		}
		
	}
	

	private fun exercise(data: MutableMap<String, String>) {
		exerciseQuotes("'", "'", data)
		exerciseQuotes("\"", "\"", data)
		exerciseQuotes("	\"", "\"	", data)
		exerciseQuotes(" '", "' ", data)
		exerciseQuotes("	'", "'	", data)
		data.put("quote", "Foo'd up!")
		exerciseQuotes("\"", "\"", data)
		data.put("quote", "This is a \"quoted\" value.")
		exerciseQuotes(" '", "' ", data)
		data.remove("quote")
		val pi = ProcessingInstruction("tgt", data)
		for (me in data.entries) {
			assertEquals(me.value, pi.getPseudoAttributeValue(me.key))
			assertTrue(pi.removePseudoAttribute(me.key))
			assertFalse(pi.removePseudoAttribute(me.key))
		}
		for (me in data.entries) {
			assertTrue(null == pi.getPseudoAttributeValue(me.key))
			assertFalse(pi.removePseudoAttribute(me.key))
			assertTrue(pi === pi.setPseudoAttribute(me.key, me.value))
			assertEquals(me.value, pi.getPseudoAttributeValue(me.key))
		}
		checkEquals(mapValue(data), pi.data)
		
	}

	private fun exerciseQuotes(open: String, close: String, data: MutableMap<String, String>) {
		val sb = StringBuilder()
		var cnt: Int = 0
		for (me in data.entries) {
			if (cnt++ > 0) {
				sb.append(" ")
			}
			sb.append(me.key).append("=").append(open).append(me.value).append(close)
		}
		val pi = ProcessingInstruction("test", sb.toString())
		checkEquals(pi.data, sb.toString())
		val keys = pi.pseudoAttributeNames
		assertTrue(keys != null)
		assertTrue(keys.size == data.size)
		assertTrue(keys.containsAll(data.keys))
		assertTrue(data.keys.containsAll(keys))
		for (me in data.entries) {
			val val_ = pi.getPseudoAttributeValue(me.key)
			assertTrue(val_ != null)
			val x = me.value
			if (!x.equals(val_)) {
				fail("We expected value '" + x + "' but got '" + val_ + "'.")
			}
		}
		assertEquals(pi.data, pi.value)
		// ProcessingInstruction does not change the quoting unless you mess with
		// attribute values.
		assertEquals(pi.data, pi.value)
		pi.setPseudoAttribute("foo", "bar")
		pi.removePseudoAttribute("foo")
		checkEquals(pi.data, mapValue(data))
	}
	
	private fun mapValue(data: MutableMap<String, String>): String {
		val sb = StringBuilder()
		for (me in data.entries) {
			if (sb.length > 0) {
				sb.append(" ")
			}
			sb.append(me.key).append("=\"").append(me.value).append("\"")
		}
		return sb.toString()
	}

    @Test fun testCloneDetatchParentProcessingInstruction() {
		val parent = Element("root")
		val content = ProcessingInstruction("val", "")
		parent.addContent(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

}
