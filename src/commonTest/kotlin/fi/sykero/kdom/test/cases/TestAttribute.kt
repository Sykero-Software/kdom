/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.AttributeType
import fi.sykero.kdom.DataConversionException
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.IllegalNameException
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.test.util.UnitTestUtil

/*--

 Copyright (C) 2000 Brett McLaughlin & Jason Hunter.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
	notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
	notice, this list of conditions, and the disclaimer that follows
	these conditions in the documentation and/or other materials
	provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
	derived from this software without prior written permission.  For
	written permission, please contact license@jdom.org.

 4. Products derived from this software may not be called "JDOM", nor
	may "JDOM" appear in their name, without prior written permission
	from the JDOM Project Management (pm@jdom.org).

 In addition, we request (but do not require) that you include in the
 end-user documentation provided with the redistribution and/or in the
 software itself an acknowledgement equivalent to the following:
	 "This product includes software developed by the
	  JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many
 individuals on behalf of the JDOM Project and was originally
 created by Brett McLaughlin <brett@jdom.org> and
 Jason Hunter <jhunter@jdom.org>.  For more information on the
 JDOM Project, please see <http://www.jdom.org/>.

 */



















/**
 * Test the expected behaviour of the Attribute class.
 *
 * @author unascribed
 * @version 0.1
 */
class TestAttribute {

    

    /**
	 * Test the simple case of constructing an attribute without
	 * namespace or prefix
	 */
    @Test fun test_TCC___String_String() {
		val attribute = Attribute("test", "value")
        assertTrue(attribute.isSpecified)
		assertTrue(attribute.name.equals("test"), "incorrect attribute name")
        assertTrue(attribute.value.equals("value"), "incoorect attribute value")
        assertEquals(attribute.attributeType, Attribute.UNDECLARED_TYPE, "incorrect attribute type")

        //should have been put in the NO_NAMESPACE namespace
		assertTrue(attribute.namespace.equals(Namespace.NO_NAMESPACE), "incorrect namespace")


		assertFailsWith<NullPointerException>("didn't catch null attribute name") {
			Attribute(null, "value")
		}

		assertFailsWith<NullPointerException>("didn't catch null attribute value") {
            Attribute("test", null)
		}

		assertFailsWith<IllegalArgumentException>("didn't catch invalid attribute name") {
            Attribute("test" + 0x01.toChar(), "value")
		}

		assertFailsWith<IllegalArgumentException>("didn't catch invalid attribute value") {
            Attribute("test", "test" + 0x01.toChar())
		}

	}

    /**
	 * Test the constructor with name, value and namespace
	 */
    @Test fun test_TCC___String_String_OrgJdomNamespace() {
        run {
    		val namespace = Namespace.getNamespace("prefx", "http://some.other.place")

    		val attribute = Attribute("test", "value", namespace)
    		assertTrue(attribute.name.equals("test"), "incorrect attribute name")
    		assertTrue(attribute.value.equals("value"), "incoorect attribute value")
    		assertTrue(attribute.namespace.equals(namespace), "incorrect Namespace")

            assertEquals(attribute.attributeType, Attribute.UNDECLARED_TYPE, "incoorect attribute type")
        }

		//now test that the attribute cannot be created with a namespace
		//without a prefix
        val defaultNamespace = Namespace.getNamespace("http://some.other.place")
		assertFailsWith<IllegalNameException>("allowed creation of attribute with a default namespace") {
            Attribute("test", "value", defaultNamespace)
		}

		try {
            Attribute("test", "value", (null as Namespace?))
		} catch (e: Exception) {
        		fail("didn't handle null attribute namespace")
		}
	}


    /**
	 * Test the constructor with name, value and namespace
	 */
    @Test fun test_TCC___String_String_int() {
        run {

    		
			val attribute = Attribute("test", "value", AttributeType.ID.ordinal)
    		assertTrue(attribute.name.equals("test"), "incorrect attribute name")
    		assertTrue(attribute.value.equals("value"), "incoorect attribute value")
    		assertTrue(attribute.namespace.equals(Namespace.NO_NAMESPACE), "incorrect Namespace")

            assertEquals(attribute.attributeType, Attribute.ID_TYPE, "incoorect attribute type")
        }

	}


    /**
	 * Test the constructor with name, value and namespace
	 */
    @Test fun test_TCC___String_String_int_OrgJdomNamespace() {
        run {
    		val namespace = Namespace.getNamespace("prefx", "http://some.other.place")

    		
			val attribute = Attribute("test", "value", Attribute.ID_TYPE.ordinal, namespace)
    		assertTrue(attribute.name.equals("test"), "incorrect attribute name")
    		assertTrue(attribute.value.equals("value"), "incoorect attribute value")
    		assertTrue(attribute.namespace.equals(namespace), "incorrect Namespace")

            assertEquals(attribute.attributeType, Attribute.ID_TYPE, "incoorect attribute type")
        }

		//now test that the attribute cannot be created with a namespace
		//without a prefix
        val defaultNamespace = Namespace.getNamespace("http://some.other.place")
		assertFailsWith<IllegalNameException>("allowed creation of attribute with a default namespace") {
            Attribute("test", "value", defaultNamespace)
		}

		try {
            Attribute("test", "value", (null as Namespace?))
		} catch (e: Exception) {
        		fail("didn't handle null attribute namespace")
		}
	}


    /**
     * Test possible attribute values
     */
    @Test fun test_TCM__Attribute_setAttributeType_int() {
        val attribute = Attribute("test", "value")

        for (attributeType in -10 until 15) {
            if (
                    Attribute.UNDECLARED_TYPE.ordinal <= attributeType &&
                    attributeType <= Attribute.ENUMERATED_TYPE.ordinal
            ) {
            	attribute.setAttributeType(attributeType)
            	assertTrue(attribute.attributeType.ordinal == attributeType)
                continue
            }
            try {
            	assertFailsWith<IllegalDataException>("set invalid attribute type: "+ attributeType) {
                attribute.setAttributeType(attributeType)
            }
            }
            catch (exception: Exception) {
            	exception.printStackTrace()
                fail("unknown exception throws: "+ exception)
            }
        }
    }

    /**
	 * Test a simple object comparison
	 */
    @Test fun test_TCM__boolean_equals_Object() {
		val attribute = Attribute("test", "value")

        assertFalse(attribute.equals(null), "attribute equal to null")

        val object_: Any = attribute
        assertTrue(attribute.equals(object_), "object not equal to attribute")
        assertTrue(object_.equals(attribute), "attribute not equal to object")

        // current implementation checks only for identity
//        final Attribute clonedAttribute = (Attribute) attribute.clone();
//        assertTrue("attribute not equal to its clone", attribute.equals(clonedAttribute));
//        assertTrue("clone not equal to attribute", clonedAttribute.equals(attribute));
	}

    /**
	 * test the convenience method getBooleanValue();
	 */
    @Test fun test_TCM__boolean_getBooleanValue() {
		val attribute = Attribute("test", "true")
		try {
			assertTrue(attribute.booleanValue, "incorrect boolean true value")

            attribute.setValue("false")
			assertTrue(!attribute.booleanValue, "incorrect boolean false value")

            attribute.setValue("TRUE")
			assertTrue(attribute.booleanValue, "incorrect boolean TRUE value")

            attribute.setValue("FALSE")
			assertTrue(!attribute.booleanValue, "incorrect boolean FALSE value")

            attribute.setValue("On")
			assertTrue(attribute.booleanValue, "incorrect boolean TRUE value")

            attribute.setValue("Yes")
			assertTrue(attribute.booleanValue, "incorrect boolean TRUE value")

            attribute.setValue("1")
			assertTrue(attribute.booleanValue, "incorrect boolean TRUE value")

            attribute.setValue("OfF")
			assertTrue(!attribute.booleanValue, "incorrect boolean FALSE value")

            attribute.setValue("0")
			assertTrue(!attribute.booleanValue, "incorrect boolean FALSE value")

            attribute.setValue("No")
			assertTrue(!attribute.booleanValue, "incorrect boolean FALSE value")

		} catch (e: DataConversionException) {
			fail("couldn't convert boolean value")
		}

		try {
            attribute.setValue("foo")
			assertTrue(attribute.booleanValue, "incorrectly returned boolean from non boolean value")

		} catch (e: DataConversionException) {
			// good
		} catch (e: Exception) {
			fail("Expected DataConversionException, but got " + e::class.qualifiedName)
		}


	}
	/**
	 * Test convenience method for getting doubles from an Attribute
	 */
    @Test fun test_TCM__double_getDoubleValue() {
		val attr = Attribute("test", "11111111111111")
		try {
			assertTrue(attr.doubleValue == 11111111111111.0, "incorrect double value")

			attr.setValue("0")
			assertTrue(attr.doubleValue == 0.0, "incorrect double value")

			attr.setValue((Double.MAX_VALUE).toString())
			assertTrue(attr.doubleValue == Double.MAX_VALUE, "incorrect double value")

			attr.setValue((Double.MIN_VALUE).toString())
			assertTrue(attr.doubleValue == Double.MIN_VALUE, "incorrect double value")

		} catch (e: DataConversionException) {
			fail("couldn't convert boolean value")
		}

		assertFailsWith<DataConversionException> {
			attr.setValue("foo")
			fail("incorrectly returned double from non double value" + attr.doubleValue)
		}

	}

    /**
     * Test floats returned from Attribute values. Checks that both correctly
     * formatted (java style) and incorrectly formatted float strings are
     * returned or raise a DataConversionException
     */
    @Test fun test_TCM__float_getFloatValue() {
        val attr = Attribute("test", "1.00000009999e+10f")
        val flt: Float = 1.00000009999e+10f
        try {
            assertTrue(attr.floatValue == flt, "incorrect float conversion")
        } catch (e: DataConversionException) {
            fail("couldn't convert to float")
        }

        // test an invalid float

        attr.setValue("1.00000009999e")
        assertFailsWith<DataConversionException>("incorrect float conversion from non float") {
            attr.floatValue
        }

    }

    /**
 	 * Tests that Attribute can convert value strings to ints and
 	 * that is raises DataConversionException if it is not an int.
	 */
    @Test fun test_TCM__int_getIntValue() {
        val attribute = Attribute("test", "")
        var summand: Int = 3
        for (i in 0 until 28) {
            summand = summand shl 1
            val value: Int = Int.MIN_VALUE + summand

            attribute.setValue("" + value)
            try {
                assertEquals(attribute.intValue, value, "incorrect int conversion")
            } catch (e: DataConversionException) {
                fail("couldn't convert to int")
            }
        }

		//test an invalid int
        TCM__int_getIntValue_invalidInt("" + Long.MIN_VALUE)
        TCM__int_getIntValue_invalidInt("" + Long.MAX_VALUE)
        TCM__int_getIntValue_invalidInt("" + Float.MIN_VALUE)
        TCM__int_getIntValue_invalidInt("" + Float.MAX_VALUE)
        TCM__int_getIntValue_invalidInt("" + Double.MIN_VALUE)
        TCM__int_getIntValue_invalidInt("" + Double.MAX_VALUE)
	}

    private fun TCM__int_getIntValue_invalidInt(value: String) {
        val attribute = Attribute("test", value)
        assertFailsWith<DataConversionException>("incorrect int conversion from non int") {
            attribute.intValue
        }
    }
    /**
	 * Test that Attribute returns a valid hashcode.
	 */
    @Test fun test_TCM__int_hashCode() {
	    val attr = Attribute("test", "value")
		//only an exception would be a problem
		var i: Int = -1
		try {
		    i = attr.hashCode()
		}
		catch (e: Exception) {
		    fail("bad hashCode")
		}
		val attr2 = Attribute("test", "value")

        //different Attributes, same text
		val x: Int = attr2.hashCode()
		assertFalse(x == i, "Different Attributes with same value have same hashcode")

        val attr3 = Attribute("test2", "value")
		//only an exception would be a problem
		val y: Int = attr3.hashCode()
		assertFalse(y == x, "Different Attributes have same hashcode")
	}

    /**
	 * Test the convenience method for returning a long from an Attribute
	 */
    @Test fun test_TCM__long_getLongValue() {
        val attribute = Attribute("test", "")
        var summand: Long = 3
	    for (i in 0 until 60) {
            summand = summand shl 1
            val value: Long = Long.MIN_VALUE + summand

            attribute.setValue("" + value)
    		try {
    			assertEquals(attribute.longValue, value, "incorrect long conversion")
    		} catch (e: DataConversionException) {
    			fail("couldn't convert to long")
    		}
        }

		//test an invalid long
        attribute.setValue("100000000000000000000000000")
		assertFailsWith<DataConversionException>("incorrect long conversion from non long") {
            attribute.longValue
		}
	}

    /**
 	 * Test that an Attribute can clone itself correctly.  The test
 	 * covers the simple case and with the attribute using a namespace
 	 * and prefix.
	 */
    @Test fun test_TCM__Object_clone() {
        TCM__Object_clone__default()

        TCM__Object_clone__attributeType(Attribute.UNDECLARED_TYPE)
        TCM__Object_clone__attributeType(Attribute.CDATA_TYPE)
        TCM__Object_clone__attributeType(Attribute.ID_TYPE)
        TCM__Object_clone__attributeType(Attribute.IDREF_TYPE)
        TCM__Object_clone__attributeType(Attribute.IDREFS_TYPE)
        TCM__Object_clone__attributeType(Attribute.ENTITY_TYPE)
        TCM__Object_clone__attributeType(Attribute.ENTITIES_TYPE)
        TCM__Object_clone__attributeType(Attribute.NMTOKEN_TYPE)
        TCM__Object_clone__attributeType(Attribute.NMTOKENS_TYPE)
        TCM__Object_clone__attributeType(Attribute.NOTATION_TYPE)
        TCM__Object_clone__attributeType(Attribute.ENUMERATED_TYPE)


        TCM__Object_clone__Namespace_default()

        TCM__Object_clone__Namespace_attributeType(Attribute.UNDECLARED_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.CDATA_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.ID_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.IDREF_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.IDREFS_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.ENTITY_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.ENTITIES_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.NMTOKEN_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.NMTOKENS_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.NOTATION_TYPE)
        TCM__Object_clone__Namespace_attributeType(Attribute.ENUMERATED_TYPE)
	}

    /**
     * Test that an Attribute can clone itself correctly. The test covers the
     * simple case with only name and value.
     */
    private fun TCM__Object_clone__default() {
        val attributeName = "test"
        val attributeValue = "value"

        val attribute = Attribute(attributeName, attributeValue)
        val clonedAttribute = attribute.clone()

        assertTrue(clonedAttribute.name.equals(attributeName), "incorrect name in clone")
        assertTrue(clonedAttribute.value.equals(attributeValue), "incorrect value in clone")
        assertEquals(clonedAttribute.attributeType, Attribute.UNDECLARED_TYPE, "incoorect attribute type in clone")
    }

    /**
     * Test that an Attribute can clone itself correctly. The test covers the
     * simple case with only name, value and a given attribute type.
     */
    private fun TCM__Object_clone__attributeType(attributeType: AttributeType) {
        val attributeName = "test"
        val attributeValue = "value"

        val attribute = Attribute(attributeName, attributeValue, attributeType)
        val clonedAttribute = attribute.clone()

        assertTrue(clonedAttribute.name.equals(attributeName), "incorrect name in clone")
        assertTrue(clonedAttribute.value.equals(attributeValue), "incorrect value in clone")
        assertEquals(clonedAttribute.attributeType, attributeType, "incoorect attribute type in clone")
    }

    /**
     * Test that an Attribute can clone itself correctly. The test covers the
     * case with name, value, prefix, namespace and a given attribute type.
     */
    private fun TCM__Object_clone__Namespace_default() {
        val prefix = "prefx"
        val uri = "http://some.other.place"

        val namespace = Namespace.getNamespace(prefix, uri)

        val attributeName = "test"
        val attributeValue = "value"

        val attribute = Attribute(attributeName, attributeValue, namespace)
        val clonedAttribute = attribute.clone()

        assertTrue(clonedAttribute.name.equals(attributeName), "incorrect name in clone")
        assertTrue(clonedAttribute.value.equals(attributeValue), "incorrect value in clone")
        assertEquals(clonedAttribute.attributeType, Attribute.UNDECLARED_TYPE, "incoorect attribute type in clone")

        assertTrue(clonedAttribute.namespacePrefix.equals(prefix), "incorrect prefix in clone")
        assertTrue(clonedAttribute.qualifiedName.equals(prefix + ':' + attributeName), "incorrect qualified name in clone")
        assertTrue(clonedAttribute.namespaceURI.equals(uri), "incorrect Namespace URI in clone")
    }

    /**
     * Test that an Attribute can clone itself correctly. The test covers the
     * case with name, value, prefix, namespace and a given attribute type.
     */
    private fun TCM__Object_clone__Namespace_attributeType(attributeType: AttributeType) {
        val prefix = "prefx"
        val uri = "http://some.other.place"

        val namespace = Namespace.getNamespace(prefix, uri)

        val attributeName = "test"
        val attributeValue = "value"

        val attribute = Attribute(attributeName, attributeValue, attributeType, namespace)
        val clonedAttribute = attribute.clone()

        assertTrue(clonedAttribute.name.equals(attributeName), "incorrect name in clone")
        assertTrue(clonedAttribute.value.equals(attributeValue), "incorrect value in clone")
        assertEquals(clonedAttribute.attributeType, attributeType, "incoorect attribute type in clone")

        assertTrue(clonedAttribute.namespacePrefix.equals(prefix), "incorrect prefix in clone")
        assertTrue(clonedAttribute.qualifiedName.equals(prefix + ':' + attributeName), "incorrect qualified name in clone")
        assertTrue(clonedAttribute.namespaceURI.equals(uri), "incorrect Namespace URI in clone")
    }

    /**
     * Test that setting an Attribute's value works correctly.
     */
    @Test fun test_TCM__OrgJdomAttribute_setValue_String() {
    	val namespace = Namespace.getNamespace("prefx", "http://some.other.place")

    	val attribute = Attribute("test", "value", namespace)

    	assertTrue(attribute.value.equals("value"), "incorrect value before set")

        attribute.setValue("foo")
    	assertTrue(attribute.value.equals("foo"), "incorrect value after set")

    	//test that the verifier is called
    	assertFailsWith<NullPointerException>("Attribute setValue didn't catch null  string") {
            attribute.setValue(null)
    	}

    	assertFailsWith<IllegalDataException>("Attribute setValue didn't catch invalid comment string") {
    		val c: Char = 0x11.toChar()
    		val buffer = StringBuilder("hhhh")
            buffer.setCharAt(2, c)
    		attribute.setValue(buffer.toString())
    	}

    }

    /**
	 * check that the attribute can return the correct parent element
	 */
    @Test fun test_TCM__OrgJdomElement_getParent() {
		val attribute = Attribute("test", "value")
		assertNull(attribute.parent, "attribute returned parent when there was none")

		val element = Element("element")
		element.setAttribute(attribute)

        assertNotNull(attribute.parent, "no parent element found")

        assertEquals(attribute.parent, element, "invalid parent element")
	}

    /**
     * check that the attribute can return the correct document
     */
    @Test fun test_TCM__OrgJdomDocument_getDocument() {
        val attribute = Attribute("test", "value")
        assertNull(attribute.document, "attribute returned document when there was none")

        val element = Element("element")
        element.setAttribute(attribute)
        assertNull(attribute.document, "attribute returned document when there was none")

        val document = Document(element)

        assertEquals(attribute.document, document, "invalid document")
    }

	/**
 	 * Test that an independently created Namespace and one
  	 * retrieved from an Attribute create with the same namespace
 	 * parameters are the same namespace.
	 */
    @Test fun test_TCM__OrgJdomNamespace_getNamespace() {
		val ns = Namespace.getNamespace("prefx", "http://some.other.place")

		val attr = Attribute("test", "value", ns)
		val ns2 = Namespace.getNamespace("prefx", "http://some.other.place")

		assertTrue(attr.namespace.equals(ns2), "incorrect Namespace")

	}

    /**
 	 * Test that an Attribute returns its correct name.
	 */
    @Test fun test_TCM__String_getName() {
		val attr = Attribute("test", "value")
		assertTrue(attr.name.equals("test"), "incorrect attribute name")

	}

    /**
 	 * Test that an Attribute returns the correct Namespace prefix.
	 */
    @Test fun test_TCM__String_getNamespacePrefix() {
		val namespace = Namespace.getNamespace("prefx", "http://some.other.place")

		val attribute = Attribute("test", "value", namespace)
		assertTrue(attribute.namespacePrefix.equals("prefx"), "incorrect prefix")
	}

    /**
	 * Test that an Attribute returns the correct Namespace URI.
	 */
    @Test fun test_TCM__String_getNamespaceURI() {
		val namespace = Namespace.getNamespace("prefx", "http://some.other.place")

		val attribute = Attribute("test", "value", namespace)
		assertTrue(attribute.namespaceURI.equals("http://some.other.place"), "incorrect URI")

	}

    /**
	 * Tests that an Attribute returns the correct Qualified Name.
	 */
    @Test fun test_TCM__String_getQualifiedName() {
        val prefix = "prefx"
        val uri = "http://some.other.place"

        val namespace = Namespace.getNamespace(prefix, uri)

        val attributeName = "test"
        val attributeQName = prefix + ':' + attributeName
        val attributeValue = "value"

        val qulifiedAttribute = Attribute(attributeName, attributeValue, namespace)
        assertEquals(qulifiedAttribute.qualifiedName, attributeQName, "incorrect qualified name")

        val attribute = Attribute(attributeName, attributeValue)
        assertEquals(attribute.qualifiedName, attributeName, "incorrect qualified name")

	}
	/**
	 * Test that Attribute returns the correct serialized form.
	 */
    @Test fun test_TCM__String_getSerializedForm() {
		/* noop because the method is deprecated

		Namespace ns = Namespace.getNamespace("prefx", "http://some.other.place");
		Attribute attr = new Attribute("test", "value", ns);
		String serialized = attr.getSerializedForm();
		assertTrue("incorrect serialized form", serialized.equals("prefx:test=\"value\""));
		*/

	}

    /**
	 * Test that an Attribute returns the correct value.
	 */
    @Test fun test_TCM__String_getValue() {
		val ns = Namespace.getNamespace("prefx", "http://some.other.place")
		val attr = Attribute("test", "value", ns)
		assertTrue(attr.value.equals("value"), "incorrect value")

	}

    /**
 	 * Test that the toString function works according to the
 	 * JDOM spec.
	 */
    @Test fun test_TCM__String_toString() {
		//expected value
		val ns = Namespace.getNamespace("prefx", "http://some.other.place")
		val attr = Attribute("test", "value", ns)
		val str = attr.toString()
		assertTrue(str.equals("[Attribute: prefx:test=\"value\"]"), "incorrect toString form")

	}

    /**
     * Test that the detach() function works according to the JDOM spec.
     *
     * @see Attribute#detach()
     */
    @Test fun test_TCM___detach() {
        val element = Element("element")
        element.setAttribute("name", "value")

        val attribute = element.getAttribute("name")

        // should never occur, just to be sure
        assertNotNull(attribute, "no attribute found")


        assertNotNull(attribute.parent, "no parent for attribute found")

        attribute.detach()

        assertNull(attribute.parent, "attribute has still a parent")
    }



    @Test fun testInfinity() {
        val infinity = Attribute("name", "Infinity")
        val neginfinity = Attribute("name", "-Infinity")
        val inf = Attribute("name", "INF")
        val neginf = Attribute("name", "-INF")
        assertEquals(infinity.doubleValue, Double.POSITIVE_INFINITY, 0.0)
        assertEquals(neginfinity.doubleValue, Double.NEGATIVE_INFINITY, 0.0)
        assertEquals(inf.doubleValue, Double.POSITIVE_INFINITY, 0.0)
        assertEquals(neginf.doubleValue, Double.NEGATIVE_INFINITY, 0.0)
    }

    @Test fun testDetatchCloneParentAttribute() {
		val parent = Element("root")
		val content = Attribute("att", "val")
		parent.setAttribute(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

    @Test fun testCloneDetatchParentAttribute() {
		val parent = Element("root")
		val content = Attribute("att", "val")
		parent.setAttribute(content)
		val clone = content.clone()
		assertNull(clone.parent)
		assertEquals(parent, content.parent)
		assertEquals(content.value, clone.value)
		content.detach()
		assertNull(content.parent)
		assertNull(clone.parent)
	}
    
    @Test fun testNullAttributeType() {
    	val att = Attribute("att", "value", AttributeType.CDATA)
    	assertTrue(att.attributeType === AttributeType.CDATA)
    	att.setAttributeType(null)
    	assertTrue(att.attributeType === AttributeType.UNDECLARED)
    	att.setAttributeType(AttributeType.ID)
    	assertTrue(att.attributeType === AttributeType.ID)
    }
    
    @Test fun testSpecified() {
    	val att = Attribute("att", "value")
    	assertTrue(att.isSpecified)
    	att.isSpecified = false
    	assertFalse(att.isSpecified)
    	
    	att.setValue("val")
    	assertTrue(att.isSpecified)
    	att.isSpecified = false
    	assertFalse(att.isSpecified)
    	
    	att.setName("attb")
    	assertTrue(att.isSpecified)
    	att.isSpecified = false
    	assertFalse(att.isSpecified)
    	
    	att.setNamespace(Namespace.getNamespace("pfx", "nothing"))
    	assertTrue(att.isSpecified)
    	att.isSpecified = false
    	assertFalse(att.isSpecified)
    	
    	att.setAttributeType(AttributeType.ID)
    	assertTrue(att.isSpecified)
    	att.isSpecified = false
    	assertFalse(att.isSpecified)
    	
    }

}
