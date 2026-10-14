/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.Element
import fi.sykero.kdom.IllegalDataException

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
 * Please put a description of your test here.
 * 
 * @author Philip Nelson
 * @version 1.0
 */










class TestComment {

	

	/**
	 * Test the comment constructor with a valid and an invalid string.
	 */
	@Test fun test_TCC___String() {
		var theComment = Comment("this is a comment")

		assertEquals("[Comment: <!--this is a comment-->]", theComment.toString(), "incorrect Comment constructed") 
		assertFailsWith<IllegalDataException>("Comment constructor didn't catch invalid comment string") {
			theComment = Comment(null)
		}
	}
	/**
	 * Verify a simple object == object test
	 */
	@Test fun test_TCM__boolean_equals_Object() {
		val com = Comment("test")

		val ob: Any = com

		assertTrue(com.equals(ob), "object not equal to comment")
	}
	/**
	 * Test that a real hashcode is returned and that a different one is returned
	 * for a different comment.
	 */
	@Test fun test_TCM__int_hashCode() {
		//not sure what to test!

		val com = Comment("test")
		//only an exception would be a problem
		var i: Int = -1
		try {
			i = com.hashCode()
		}
		catch (e: Exception) {
			fail("bad hashCode")
		}
		val com2 = Comment("test")
		//different comments, same text
		val x: Int = com2.hashCode()
		assertTrue(x != i, "Different comments with same value have same hashcode")
		val com3 = Comment("test2")
		//only an exception would be a problem
		val y: Int = com3.hashCode()
		assertTrue(y != x, "Different comments have same hashcode")
	}
	
	/**
	 * Test setting and resetting the text value of this Comment.
	 */
	@Test fun test_TCM__OrgJdomComment_setText_String() {
		val theComment = Comment("this is a comment")

		assertEquals("[Comment: <!--this is a comment-->]", theComment.toString(), "incorrect Comment constructed")

		//set it to the empty string
		theComment.setText("")

		assertEquals("", theComment.text, "incorrect Comment text")
		assertEquals(theComment.text, theComment.value)
		//set it to a new string
		theComment.setText("12345qwerty")

		assertEquals("12345qwerty", theComment.text, "incorrect Comment text")
		assertEquals(theComment.text, theComment.value)

		//tests for invalid data but setText doesn't

		assertFailsWith<IllegalDataException>("Comment setText didn't catch invalid comment string") {
			theComment.setText(null)
		}
		assertFailsWith<IllegalDataException>("Comment setText didn't catch invalid comment string") {
			val c: Char = 0x11.toChar()
			val b = StringBuilder("hhhh")
			b.setCharAt(2, c)
			theComment.setText(b.toString())
		}

	}
	
	/**
	 * Match the XML fragment this comment produces.
	 */
	@Test fun test_TCM__String_getSerializedForm() {

		/** No op because the method is deprecated
		Comment theComment = new fi.sykero.kdom.Comment("this is a comment");

		assertEquals(
				"incorrect Comment constructed", 
				"<!--this is a comment-->", 
				theComment.getSerializedForm());
		 */

	}
	
	/**
	 * verify that the text of the Comment matches expected value.
	 */
	@Test fun test_TCM__String_getText() {
		val theComment = Comment("this is a comment")

		assertEquals("this is a comment", theComment.text, "incorrect Comment constructed") 


	}
	/**
	 * check for the expected toString text value of Comment.
	 */
	@Test fun test_TCM__String_toString() {
		var theComment = Comment("this is a comment")

		assertEquals("[Comment: <!--this is a comment-->]", theComment.toString(), "incorrect Comment constructed")
		assertFailsWith<IllegalDataException>("Comment constructor didn't catch invalid comment string") {
			theComment= Comment(null)
		}
	}
	
    @Test fun testCloneDetatchParentComment() {
		val parent = Element("root")
		val content = Comment("val")
		parent.addContent(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

    @Test fun testContentCType() {
    	assertTrue(Content.CType.Comment === Comment("").cType)
    }
}
