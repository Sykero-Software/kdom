/*-- 

 Copyright (C) 2006 Brett McLaughlin & Jason Hunter.
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
 
 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Content
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalDataException
import fi.sykero.kdom.Text

/**
 * Test for {@link CDATA}.
 * 
 * @author Victor Toni
 * @version 1.0.0
 */
class TestCDATA {

    

    /**
     * Test the protected CDATA constructor.
     */
	@Test fun test_TCC() {
        object : CDATA() {
            // check protected constructor via anonymous class
        }
    }

    /**
	 * Test the CDATA constructor with a valid and an invalid string.
	 */
	@Test fun test_TCC___String() {
        val text = "this is a CDATA section"        

        val cdata = CDATA(text)

		assertEquals("[CDATA: " + text + ']', cdata.toString(), "incorrect CDATA constructed") 

        try {
            CDATA("")
        } catch (e: IllegalDataException) {
            fail("CDATA constructor did throw exception on empty string")
        }

        try {
            CDATA(null)
        } catch (e: IllegalDataException) {
            fail("CDATA constructor did throw exception on null")
        }

        assertFailsWith<IllegalDataException>("CDATA constructor didn't catch invalid comment string") {
            CDATA("some valid <text> with a CDATA section ending ]]>")
        }
	}

    /**
	 * Verify a simple object == object test
	 */
	@Test fun test_TCM__boolean_equals_Object() {
        val text = "this is a CDATA section"
	    val cdata = CDATA(text)

	    val object_: Any = cdata

        assertTrue(cdata.equals(object_), "object not equal to CDATA")
        assertTrue(object_.equals(cdata), "CDATA not equal to object")
	}

    /**
	 * Test that a real hashcode is returned and that a different one is returned
	 * for a different CDATA.
	 */
	@Test fun test_TCM__int_hashCode() {
        val text = "this is a CDATA section"
        val cdata = CDATA(text)

		//only an exception would be a problem
        var hash: Int = -1
        try {
           hash = cdata.hashCode()
        }
        catch (exception: Exception) {
            fail("bad hashCode")
        }

        // same text but created out of parts to avoid object re-usage
		val similarCDATA = CDATA("this"+ " is" +" a" + " CDATA" + " section")

        //different CDATA sections, same text
		val similarHash: Int = similarCDATA.hashCode()
		assertTrue(hash != similarHash, "Different comments with same value have same hashcode")
        
		val otherCDATA = CDATA("this is another CDATA section")

        //only an exception would be a problem
		val otherHash: Int = otherCDATA.hashCode()
        assertTrue(otherHash != hash, "Different comments have same hashcode")
        assertTrue(otherHash != similarHash, "Different comments have same hashcode")
	}

    /**
     * Test setting and resetting the text value of this CDATA.
     */
	@Test fun test_TCM__orgJdomText_setText_String() {
        // simple text in CDATA section
        val text = "this is a CDATA section"        
        val cdata = CDATA(text)
        assertEquals(text, cdata.text, "incorrect CDATA text")

    	// set it to the empty string
        val emptyString = ""
    	cdata.setText(emptyString)
        assertEquals(emptyString, cdata.text, "incorrect CDATA text")

        // set it to a another string
        val otherString = "12345qwerty" 
        cdata.setText(otherString)
        assertEquals(otherString, cdata.text, "incorrect CDATA text")

        // set it to the null (after it was set to another string so that we 
        // are sure something has changed)
        cdata.setText(null)
        assertEquals(emptyString, cdata.text, "incorrect CDATA text")

    	// the following test check for invalid data and transactional behavior
        // means the content must not be change on exceptions so we set a default

        // set text with some special characters
        val specialText = "this is CDATA section with special characters as < > & &amp; &lt; &gt; [[ ]] > <![!["
        cdata.setText(specialText)    
        assertEquals(specialText, cdata.text, "incorrect CDATA text")
        
        
        cdata.setText(otherString)    
        try {
            val c: Char = 0x11.toChar()
            val buffer = StringBuilder("hhhh")
            buffer.setCharAt(2, c)

            cdata.setText(buffer.toString())
            fail("Comment setText didn't catch invalid CDATA string")
        } catch (exception: IllegalDataException) {
            assertEquals(otherString, cdata.text, "incorrect CDATA text after exception")
        }
    
        cdata.setText(text)        
        try {
            val invalidCDATAString = "some valid <text> with an invalid CDATA section ending ]]>"

            cdata.setText(invalidCDATAString)
            fail("Comment setText didn't catch invalid CDATA string")
        } catch (exception: IllegalDataException) {
            assertEquals(text, cdata.text, "incorrect CDATA text after exception")
        }    
    }

    /**
     * Test appending text values to this CDATA.
     */
	@Test fun test_TCM___append_String() {
        val emptyString = ""
        val nullString: String? = null
        val text = "this is a CDATA section"        
        val otherString = "12345qwerty" 
        val specialCharactersText = "this is CDATA section with special characters as < > & &amp; &lt; &gt; [[ ]] > <![!["
        val specialText = "> this is a CDATA section with special characters as ]]"
        val cdataEndText = "this is aCDATA section with a CDATA end marke ]]> somewhere inside"

        run {
            // simple text in CDATA section
            val cdata = CDATA(text)
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(text)
            assertEquals(text+text, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }

        run {
            // set it to the empty string
            val cdata = CDATA(emptyString)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(emptyString)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(text)
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(specialCharactersText)
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(emptyString)
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(otherString)
            assertEquals(text + specialCharactersText + otherString, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }

        run {
            // set it to a another string
            val cdata = CDATA(otherString)
            assertEquals(otherString, cdata.text, "incorrect CDATA text")
            cdata.append(text)
            assertEquals(otherString + text, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(otherString + text, cdata.text, "incorrect CDATA text")
            cdata.append(specialCharactersText)
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(emptyString)
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }
        
        run {
            // set it to the null (after it was set to another string so that we 
            // are sure something has changed)
            val cdata = CDATA(nullString)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(specialCharactersText)
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(text)
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(nullString)
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(emptyString)
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(otherString)
            assertEquals(specialCharactersText + text + otherString, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }

        run {
            // set it to the null (after it was set to another string so that we 
            // are sure something has changed)
            val cdata = CDATA(null)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append((null as String?))
            assertEquals("", cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }

        run {
            // set it to the null (after it was set to another string so that we 
            // are sure something has changed)
            val cdata = CDATA(null)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append((null as Text?))
            assertEquals("", cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }

        run {
            // set text with some special characters
            val cdata = CDATA(specialCharactersText)
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(specialCharactersText)
            assertEquals(specialCharactersText + specialCharactersText, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(cdataEndText)
            }
        }
        
        
        assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
            // set text with some special characters which should result into an exception
            val cdata = CDATA(specialText)
            assertEquals(specialText, cdata.text, "incorrect CDATA text")
            cdata.append(specialText)
        }
    }

    /**
     * Test appending text values to this CDATA.
     */
	@Test fun test_TCM___append_Text() {
        val emptyString = ""
        val nullString: String? = null
        val text = "this is a CDATA section"        
        val otherString = "12345qwerty" 
        val specialCharactersText = "this is CDATA section with special characters as < > & &amp; &lt; &gt; [[ ]] > <![!["
        val specialText = "> this is a CDATA section with special characters as ]]"
        val cdataEndText = "this is aCDATA section with a CDATA end marke ]]> somewhere inside"

        run {
            // simple text in CDATA section
            val cdata = CDATA(text)
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(text))
            assertEquals(text+text, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }

        run {
            // set it to the empty string
            val cdata = CDATA(emptyString)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(Text(emptyString))
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(Text(text))
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(specialCharactersText))
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(emptyString))
            assertEquals(text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(otherString))
            assertEquals(text + specialCharactersText + otherString, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }

        run {
            // set it to a another string
            val cdata = CDATA(otherString)
            assertEquals(otherString, cdata.text, "incorrect CDATA text")
            cdata.append(Text(text))
            assertEquals(otherString + text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(otherString + text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(specialCharactersText))
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(emptyString))
            assertEquals(otherString + text + specialCharactersText, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }
        
        run {
            // set it to the null (after it was set to another string so that we 
            // are sure something has changed)
            val cdata = CDATA(nullString)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append(Text(specialCharactersText))
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(text))
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(nullString))
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(emptyString))
            assertEquals(specialCharactersText + text, cdata.text, "incorrect CDATA text")
            cdata.append(Text(otherString))
            assertEquals(specialCharactersText + text + otherString, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }

        run {
            // set it to the null (after it was set to another string so that we 
            // are sure comething has changed)
            val cdata = CDATA(null)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append((null as String?))
            assertEquals("", cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }

        run {
            // set it to the null (after it was set to another string so that we 
            // are sure something has changed)
            val cdata = CDATA(null)
            assertEquals("", cdata.text, "incorrect CDATA text")
            cdata.append((null as Text?))
            assertEquals("", cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }

        run {
            // set text with some special characters
            val cdata = CDATA(specialCharactersText)
            assertEquals(specialCharactersText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(specialCharactersText))
            assertEquals(specialCharactersText + specialCharactersText, cdata.text, "incorrect CDATA text")
            assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
                cdata.append(Text(cdataEndText))
            }
        }
        
        
        assertFailsWith<IllegalDataException>("failed to detect CDATA end marker") {
            // set text with some special characters which should cause an exception
            val cdata = CDATA(specialText)
            assertEquals(specialText, cdata.text, "incorrect CDATA text")
            cdata.append(Text(specialText))
        }
    }

	/**
	 * Verify that the text of the CDATA matches expected value.
     * It assumes that the constructor is working correctly
	 */
	@Test fun test_TCM__String_getText() {
        run {
            val text = "this is a CDATA section"        
    
            val cdata = CDATA(text)
            assertEquals(text, cdata.text, "incorrect CDATA text")
        }

        run {
            //set it to the empty string
            val emptyString = ""
            val cdata = CDATA(emptyString)
            assertEquals(emptyString, cdata.text, "incorrect CDATA text")
        }

        run {
            // set it to a another string
            val otherString = "12345qwerty" 
            val cdata = CDATA(otherString)
            assertEquals(otherString, cdata.text, "incorrect CDATA text")
        }

        run {
            // set it to the null
            val cdata = CDATA(null)
            assertEquals("", cdata.text, "incorrect CDATA text")
        }
	}

    /**
     * check for the expected toString text value of Comment.
     */
	@Test fun test_TCM__String_toString() {
        run {
            val text = "this is a simple CDATA section"        
            val cdata = CDATA(text)

            assertEquals("[CDATA: " + text + ']', cdata.toString(), "incorrect CDATA constructed") 
        }
    
        run {
            val text = "this is CDATA section with special characters as < > & &amp; &lt; &gt; [[ ]] > <![!["        
            val cdata = CDATA(text)

            assertEquals("[CDATA: " + text + ']', cdata.toString(), "incorrect CDATA constructed") 
        }
    
    }
	
    @Test fun testCloneDetatchParentCDATA() {
		val parent = Element("root")
		val content = CDATA("val")
		parent.addContent(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

    @Test fun testContentCType() {
    	assertTrue(Content.CType.CDATA == CDATA("").cType)
    }
}
