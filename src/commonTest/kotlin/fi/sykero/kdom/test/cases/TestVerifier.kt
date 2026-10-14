/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
import fi.sykero.kdom.test.util.nullT
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.Verifier
import fi.sykero.kdom.test.util.javaAsList

/*-- 

 Copyright (C) 2000 Brett McLaughlin and Jason Hunter.
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
 * @author unascribed
 * @version 0.1
 */














class TestVerifier {
	
	private val BADCHAR: Char = 0x0b.toChar().toChar()

    
    
	/**
	 * Test checkElementName such that a name is validated as an
	 * xml name with the following caveats.
	 * The name must not start with "-" or ":".
	 */
    @Test fun testCheckElementName() {
		//check out of range values
		assertNotNull(Verifier.checkElementName(null), "validated invalid null")
		assertNotNull(Verifier.checkElementName("test" + 0x0.toChar()), "validated invalid name with null char")
		assertNotNull(Verifier.checkElementName("test" + 0x0.toChar() + "ing"), "validated invalid name with null char")
		assertNotNull(Verifier.checkElementName(0x0.toChar() + "test"), "validated invalid name with null char")
		assertNotNull(Verifier.checkElementName(0x01.toChar() + "test"), "validated invalid name with 0x01")
		assertNotNull(Verifier.checkElementName("test" + 0xD800.toChar()), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkElementName("test" + 0xD800.toChar() + "ing"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkElementName(0xD800.toChar() + "test"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkElementName("test" + ':' + "local"), "validated invalid name with :")
		assertNotNull(Verifier.checkElementName("abcd:"), "validated invalid name with :")
		assertNotNull(Verifier.checkElementName("abc:d"), "validated invalid name with :")
		assertNotNull(Verifier.checkElementName("ab:cd"), "validated invalid name with :")
		assertNotNull(Verifier.checkElementName("a:bcd"), "validated invalid name with :")
		assertNotNull(Verifier.checkElementName(":abcd"), "validated invalid name with :")

		//invalid start characters
		assertNotNull(Verifier.checkElementName('-' + "test"), "validated invalid name with starting -")
		assertNotNull(Verifier.checkElementName(':' + "test"), "validated invalid name with starting :")

		//valid tests
		assertNull(Verifier.checkElementName('_' + "test"), "invalidated valid name with starting _")
		assertNull(Verifier.checkElementName("test" + '_'), "invalidated valid name with _")
		assertNull(Verifier.checkElementName("test" + '.' + "name"), "invalidated valid name with .")
		assertNull(Verifier.checkElementName("test" + 0x00B7.toChar()), "invalidated valid name with 0x00B7")
		assertNull(Verifier.checkElementName("test" + 0x4E01.toChar()), "invalidated valid name with 0x4E01")
		assertNull(Verifier.checkElementName("test" + 0x0301.toChar()), "invalidated valid name with 0x0301")

	}
    
	/**
	 * Test for a valid Attribute name.  A valid Attribute name is
	 * an xml name (xml start character + xml name characters) with
	 * some special considerations.  "xml:lang" and "xml:space" are
	 * allowed.  No ':' are allowed since prefixes are defined with
	 * Namespace objects.  The name must not be "xmlns"
	 */
    @Test fun testCheckAttributeName() {
		//check out of range values
		assertNotNull(Verifier.checkAttributeName(null), "validated invalid null")
		assertNotNull(Verifier.checkAttributeName("test" + 0x0.toChar()), "validated invalid name with null")
		assertNotNull(Verifier.checkAttributeName("test" + 0x0.toChar() + "ing"), "validated invalid name with null")
		assertNotNull(Verifier.checkAttributeName(0x0.toChar() + "test"), "validated invalid name with null")
		assertNotNull(Verifier.checkAttributeName(0x01.toChar() + "test"), "validated invalid name with 0x01")
		assertNotNull(Verifier.checkAttributeName("test" + 0xD800.toChar()), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkAttributeName("test" + 0xD800.toChar() + "ing"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkAttributeName(0xD800.toChar() + "test"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkAttributeName("test" + ':' + "local"), "validated invalid name with :")
		assertNotNull(Verifier.checkAttributeName("xml:lang"), "validated invalid name with xml:lang")
		assertNotNull(Verifier.checkAttributeName("xml:space"), "validated invalid name with xml:space")

		//invalid start characters
		assertNotNull(Verifier.checkAttributeName('-' + "test"), "validated invalid name with starting -")
		assertNotNull(Verifier.checkAttributeName("xmlns"), "validated invalid name with xmlns")
		assertNotNull(Verifier.checkAttributeName(':' + "test"), "validated invalid name with starting :")

		//valid tests
		assertNull(Verifier.checkAttributeName('_' + "test"), "invalidated valid name with starting _")
		assertNull(Verifier.checkAttributeName("test" + '_'), "invalidated valid name with _")
		assertNull(Verifier.checkAttributeName("test" + '.' + "name"), "invalidated valid name with .")
		assertNull(Verifier.checkAttributeName("test" + 0x00B7.toChar()), "invalidated valid name with 0x00B7")
		assertNull(Verifier.checkAttributeName("test" + 0x4E01.toChar()), "invalidated valid name with 0x4E01")
		assertNull(Verifier.checkAttributeName("test" + 0x0301.toChar()), "invalidated valid name with 0x0301")

    	assertNull(Verifier.checkAttributeName("hi"))
    	assertNull(Verifier.checkAttributeName("hi2you"))
    	assertNull(Verifier.checkAttributeName("hi_you"))
    	
    	assertNotNull(Verifier.checkAttributeName(null))
    	assertNotNull(Verifier.checkAttributeName(""))
    	assertNotNull(Verifier.checkAttributeName("   "))
    	assertNotNull(Verifier.checkAttributeName("  hi  "))
    	assertNotNull(Verifier.checkAttributeName("hi "))
    	assertNotNull(Verifier.checkAttributeName(" hi"))
    	assertNotNull(Verifier.checkAttributeName("2bad"))

    }
    
	/**
	 * Test that a String contains only xml characters.  The method under
	 * only checks for null values and then character by character scans
	 * the string so this test is not exhaustive
	 */
    @Test fun testCheckCharacterData() {
		//check out of range values
		assertNotNull(Verifier.checkCharacterData(null), "validated invalid null")
		assertNotNull(Verifier.checkCharacterData("test" + 0x0.toChar()), "validated invalid string with null")
		assertNotNull(Verifier.checkCharacterData("test" + 0x0.toChar() + "ing"), "validated invalid string with null")
		assertNotNull(Verifier.checkCharacterData(0x0.toChar() + "test"), "validated invalid string with null")
		assertNotNull(Verifier.checkCharacterData(0x01.toChar() + "test"), "validated invalid string with 0x01")
		assertNotNull(Verifier.checkCharacterData("test" + 0xD800.toChar()), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCharacterData("test" + 0xD800.toChar() + "ing"), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCharacterData(0xD800.toChar() + "test"), "validated invalid string with 0xD800")

		//various valid strings
		assertNull(Verifier.checkCharacterData("test" + '\n' + "ing"), "invalidated valid string with \n")
		assertNull(Verifier.checkCharacterData("test" +0x29.toChar()), "invalidated valid string with 0x29")
		//a few higher values
		assertNull(Verifier.checkCharacterData("test" + 0x0B08.toChar()), "invalidated valid string with 0x0B08")
		assertNull(Verifier.checkCharacterData("test" + '\t'), "invalidated valid string with \t")
		//xml letter
		assertNull(Verifier.checkCharacterData("test" + 0x42.toChar()), "invalidated valid string with 0x42")
		assertNull(Verifier.checkCharacterData("test" + 0x4E01.toChar()), "invalidated valid string with 0x4E01")

	}
    
	/**
	 * Test that checkCDATASection verifies CDATA excluding
	 * the closing delimiter.
	 */
    @Test fun testCheckCDATASection() {
		//check out of range values
		assertNotNull(Verifier.checkCDATASection(null), "validated invalid null")
		assertNotNull(Verifier.checkCDATASection("test" + 0x0.toChar()), "validated invalid string with null")
		assertNotNull(Verifier.checkCDATASection("test" + 0x0.toChar() + "ing"), "validated invalid string with null")
		assertNotNull(Verifier.checkCDATASection(0x0.toChar() + "test"), "validated invalid string with null")
		assertNotNull(Verifier.checkCDATASection(0x01.toChar() + "test"), "validated invalid string with 0x01")
		assertNotNull(Verifier.checkCDATASection("test" + 0xD800.toChar()), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCDATASection("test" + 0xD800.toChar() + "ing"), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCDATASection(0xD800.toChar() + "test"), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCDATASection("test]]>"), "validated invalid string with ]]>")

		//various valid strings
		assertNull(Verifier.checkCDATASection("test" + '\n' + "ing"), "invalidated valid string with \n")
		assertNull(Verifier.checkCDATASection("test" +0x29.toChar()), "invalidated valid string with 0x29")
		assertNull(Verifier.checkCDATASection("test]"), "invalidated valid string with ]")
		assertNull(Verifier.checkCDATASection("test["), "invalidated valid string with [")
		//a few higher values
		assertNull(Verifier.checkCDATASection("test" + 0x0B08.toChar()), "invalidated valid string with 0x0B08")
		assertNull(Verifier.checkCDATASection("test" + '\t'), "invalidated valid string with \t")
		//xml letter
		assertNull(Verifier.checkCDATASection("test" + 0x42.toChar()), "invalidated valid string with 0x42")
		assertNull(Verifier.checkCDATASection("test" + 0x4E01.toChar()), "invalidated valid string with 0x4E01")

	}
    
	/**
	 * Test that checkNamespacePrefix validates against xml names
	 * with the following exceptions.  Prefix names must not start
	 * with "-", "xmlns", digits, "$", or "." and must not contain
	 * ":"
	 */
    @Test fun testCheckNamespacePrefix() {
		//check out of range values
		assertNotNull(Verifier.checkNamespacePrefix("test" + 0x0.toChar()), "validated invalid name with null")
		assertNotNull(Verifier.checkNamespacePrefix("test" + 0x0.toChar() + "ing"), "validated invalid name with null")
		assertNotNull(Verifier.checkNamespacePrefix(0x0.toChar() + "test"), "validated invalid name with null")
		assertNotNull(Verifier.checkNamespacePrefix(0x01.toChar() + "test"), "validated invalid name with 0x01")
		assertNotNull(Verifier.checkNamespacePrefix("test" + 0xD800.toChar()), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkNamespacePrefix("test" + 0xD800.toChar() + "ing"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkNamespacePrefix(0xD800.toChar() + "test"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkNamespacePrefix("test" + ':' + "local"), "validated invalid name with :")

		//invalid start characters
		assertNotNull(Verifier.checkNamespacePrefix('-' + "test"), "validated invalid name with starting -")
		assertNotNull(Verifier.checkNamespacePrefix(':' + "test"), "validated invalid name with starting :")
		assertNotNull(Verifier.checkNamespacePrefix("9"), "validated invalid name with starting digit")
		assertNotNull(Verifier.checkNamespacePrefix("\$"), "validated invalid name with starting \$")
		assertNotNull(Verifier.checkNamespacePrefix("."), "validated invalid name with starting .")
		
		// cannot start with xml (case insensitive).
		// See issue 126: https://github.com/hunterhacker/jdom/issues/126
//      assertNotNull("validated invalid name with xmlns", Verifier.checkNamespacePrefix("xmlns"));
//		assertNotNull("validated invalid name beginning with xml", Verifier.checkNamespacePrefix("xmlabc"));
//		assertNotNull("validated invalid name beginning with xml", Verifier.checkNamespacePrefix("xmLabc"));
//		assertNotNull("validated invalid name beginning with xml", Verifier.checkNamespacePrefix("xMlabc"));
//		assertNotNull("validated invalid name beginning with xml", Verifier.checkNamespacePrefix("Xmlabc"));
		

		//valid tests
		assertNull(Verifier.checkNamespacePrefix(null), "invalidated valid null")
		assertNull(Verifier.checkNamespacePrefix(""), "invalidated valid empty String")
		assertNull(Verifier.checkNamespacePrefix('_' + "test"), "invalidated valid name with starting _")
		assertNull(Verifier.checkNamespacePrefix("test" + '_'), "invalidated valid name with _")
		assertNull(Verifier.checkNamespacePrefix("test" + '.' + "name"), "invalidated valid name with .")
		assertNull(Verifier.checkNamespacePrefix("test9"), "invalidated valid name with digit")
		assertNull(Verifier.checkNamespacePrefix("test" + 0x00B7.toChar()), "invalidated valid name with 0x00B7")
		assertNull(Verifier.checkNamespacePrefix("test" + 0x4E01.toChar()), "invalidated valid name with 0x4E01")
		assertNull(Verifier.checkNamespacePrefix("test" + 0x0301.toChar()), "invalidated valid name with 0x0301")
		assertNull(Verifier.checkNamespacePrefix("txml"), "invalidated valid name with xml embedded")
		assertNull(Verifier.checkNamespacePrefix("xmml"), "invalidated valid name with xml embedded")

		// These tests all used to be NotNull tests, but the Verifier as been changed to pass them
		// See issue 126: https://github.com/hunterhacker/jdom/issues/126
		assertNull(Verifier.checkNamespacePrefix("xmlns"), "invalidated invalid name with xmlns")
		assertNull(Verifier.checkNamespacePrefix("xmlabc"), "invalidated invalid name beginning with xml")
		assertNull(Verifier.checkNamespacePrefix("xmLabc"), "invalidated invalid name beginning with xml")
		assertNull(Verifier.checkNamespacePrefix("xMlabc"), "invalidated invalid name beginning with xml")
		assertNull(Verifier.checkNamespacePrefix("Xmlabc"), "invalidated invalid name beginning with xml")

		assertNull(Verifier.checkNamespacePrefix("XML"), "invalidated invalid name beginning with xml")
        assertNull(Verifier.checkNamespacePrefix("XMLNS"), "invalidated invalid name beginning with xml")
        assertNull(Verifier.checkNamespacePrefix("XmL"), "invalidated invalid name beginning with xml")
	}

    /**
	 * Tests that checkNamespaceURI validates xml uri's.
	 * A valid URI is alphanumeric characters and the reserved characters:
	 * ";" or "/" or "?" or ":" or "@" or "&" or "=" or "+" or  "$" or ","
	 *            
	 * The URI cannot begin with a digit, "-" or "$".  It must have at least
	 * one ":" separating the scheme from the scheme specific part
	 *
	 * XXX:TODO make this match the eventual specs for the Verifier class which is incomplete
	 */
    @Test fun testCheckNamespaceURI() {
		//invalid start characters
		assertNotNull(Verifier.checkNamespaceURI('-' + "test"), "validated invalid URI with starting -")
		assertNotNull(Verifier.checkNamespaceURI("9"), "validated invalid URI with starting digit")
		assertNotNull(Verifier.checkNamespaceURI("\$"), "validated invalid URI with starting \$")

		//valid tests
		assertNull(Verifier.checkNamespaceURI(null), "invalidated valid null")
		assertNull(Verifier.checkNamespaceURI(""), "invalidated valid null")
		assertNull(Verifier.checkNamespaceURI("test" + ':' + "local"), "invalidated valid URI with :")
		assertNull(Verifier.checkNamespaceURI("test" + '_'), "invalidated valid URI with _")
		assertNull(Verifier.checkNamespaceURI("test" + '.' + "URI"), "invalidated valid URI with .")
		assertNull(Verifier.checkNamespaceURI("test9"), "invalidated valid URI with digit")
		assertNull(Verifier.checkNamespaceURI("test" + 0x00B7.toChar()), "invalidated valid URI with 0x00B7")
		assertNull(Verifier.checkNamespaceURI("test" + 0x4E01.toChar()), "invalidated valid URI with 0x4E01")
		assertNull(Verifier.checkNamespaceURI("test" + 0x0301.toChar()), "invalidated valid URI with 0x0301")

		//check out of range values

		/** skip these tests until the time the checks are implemented
		assertNull("validated invalid URI with xmlns", Verifier.checkNamespaceURI("xmlns"));
		assertNull("validated invalid URI with starting :", Verifier.checkNamespaceURI(':' + "test"));
		assertNull("validated invalid URI with starting .", Verifier.checkNamespaceURI("."));
		
		assertNull("validated invalid URI with null", Verifier.checkNamespaceURI("test" + (char)0x0));
		assertNull("validated invalid URI with null", Verifier.checkNamespaceURI("test" + (char)0x0 + "ing"));
		assertNull("validated invalid URI with null", Verifier.checkNamespaceURI((char)0x0 + "test"));
		assertNull("validated invalid URI with 0x01", Verifier.checkNamespaceURI((char)0x01 + "test"));
		assertNull("validated invalid URI with 0xD800", Verifier.checkNamespaceURI("test" + (char)0xD800));
		assertNull("validated invalid URI with 0xD800", Verifier.checkNamespaceURI("test" + (char)0xD800 + "ing"));
		assertNull("validated invalid URI with 0xD800", Verifier.checkNamespaceURI((char)0xD800 + "test"));
		*/
	}

    /**
	 * Test that checkProcessintInstructionTarget validates the name
	 * of a processing instruction.  This name must be a normal xml
	 * and cannot have ":" or "xml" in the name.
	 */
    @Test fun testCheckProcessingInstructionTarget() {
		//check out of range values
		assertNotNull(Verifier.checkProcessingInstructionTarget(null), "validated invalid null")
		assertNotNull(Verifier.checkProcessingInstructionTarget("test" + 0x0.toChar()), "validated invalid name with null")
		assertNotNull(Verifier.checkProcessingInstructionTarget("test" + 0x0.toChar() + "ing"), "validated invalid name with null")
		assertNotNull(Verifier.checkProcessingInstructionTarget(0x0.toChar() + "test"), "validated invalid name with null")
		assertNotNull(Verifier.checkProcessingInstructionTarget(0x01.toChar() + "test"), "validated invalid name with 0x01")
		assertNotNull(Verifier.checkProcessingInstructionTarget("test" + 0xD800.toChar()), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkProcessingInstructionTarget("test" + 0xD800.toChar() + "ing"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkProcessingInstructionTarget(0xD800.toChar() + "test"), "validated invalid name with 0xD800")
		assertNotNull(Verifier.checkProcessingInstructionTarget("test" + ':' + "local"), "validated invalid name with :")
		assertNotNull(Verifier.checkProcessingInstructionTarget("xml:space"), "validated invalid name with xml:space")
		assertNotNull(Verifier.checkProcessingInstructionTarget("xml:lang"), "validated invalid name with xml:lang")
		assertNotNull(Verifier.checkProcessingInstructionTarget("xml"), "validated invalid name with xml")
		assertNotNull(Verifier.checkProcessingInstructionTarget("xMl"), "validated invalid name with xMl")

		//invalid start characters
		assertNotNull(Verifier.checkProcessingInstructionTarget('-' + "test"), "validated invalid name with starting -")
		assertNotNull(Verifier.checkProcessingInstructionTarget(':' + "test"), "validated invalid name with starting :")
		//valid tests
		assertNull(Verifier.checkProcessingInstructionTarget('_' + "test"), "invalidated valid name with starting _")
		assertNull(Verifier.checkProcessingInstructionTarget("test" + '_'), "invalidated valid name with _")
		assertNull(Verifier.checkProcessingInstructionTarget("test" + '.' + "name"), "invalidated valid name with .")
		assertNull(Verifier.checkProcessingInstructionTarget("test" + 0x00B7.toChar()), "invalidated valid name with 0x00B7")
		assertNull(Verifier.checkProcessingInstructionTarget("test" + 0x4E01.toChar()), "invalidated valid name with 0x4E01")
		assertNull(Verifier.checkProcessingInstructionTarget("test" + 0x0301.toChar()), "invalidated valid name with 0x0301")

	}
    
    @Test fun testCheckProcessingInstructionData() {
    	assertNull(Verifier.checkProcessingInstructionData(""))
    	assertNull(Verifier.checkProcessingInstructionData("  "))
    	assertNull(Verifier.checkProcessingInstructionData("hi"))
    	assertNull(Verifier.checkProcessingInstructionData(" h i "))
    	
    	assertNotNull(Verifier.checkProcessingInstructionData(null))
    	assertNotNull(Verifier.checkProcessingInstructionData("hi" + 0x0b.toChar() + " there"))
    	assertNotNull(Verifier.checkProcessingInstructionData("can't have '?>' in text."))
    }

	/**
	 * Test checkCommentData such that a comment is validated as an
	 * xml comment consisting of xml characters with the following caveats.
	 * The comment must not contain a double hyphen.
	 */
    @Test fun testCheckCommentData() {
		//check out of range values
		assertNotNull(Verifier.checkCommentData(null), "validated invalid null")
		assertNotNull(Verifier.checkCommentData("test" + 0x0.toChar()), "validated invalid string with null")
		assertNotNull(Verifier.checkCommentData("test" + 0x0.toChar() + "ing"), "validated invalid string with null")
		assertNotNull(Verifier.checkCommentData(0x0.toChar() + "test"), "validated invalid string with null")
		assertNotNull(Verifier.checkCommentData(0x01.toChar() + "test"), "validated invalid string with 0x01")
		assertNotNull(Verifier.checkCommentData("test" + 0xD800.toChar()), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCommentData("test" + 0xD800.toChar() + "ing"), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCommentData(0xD800.toChar() + "test"), "validated invalid string with 0xD800")
		assertNotNull(Verifier.checkCommentData("--test"), "validated invalid string with --")
		assertNotNull(Verifier.checkCommentData("test-"), "validated invalid string ending with -")

		//various valid strings
		assertNull(Verifier.checkCommentData("test" + '\n' + "ing"), "invalidated valid string with \n")
		assertNull(Verifier.checkCommentData("test" +0x29.toChar()), "invalidated valid string with 0x29")
		//a few higher values
		assertNull(Verifier.checkCommentData("test" + 0x0B08.toChar()), "invalidated valid string with 0x0B08")
		assertNull(Verifier.checkCommentData("test" + '\t'), "invalidated valid string with \t")
		//xml letter
		assertNull(Verifier.checkCommentData("test" + 0x42.toChar()), "invalidated valid string with 0x42")
		assertNull(Verifier.checkCommentData("test" + 0x4E01.toChar()), "invalidated valid string with 0x4E01")

	}
    
    @Test fun testCheckNamespaceCollision() {
    	try {
    		val ns1 = Namespace.getNamespace("aaa", "http://acme.com/aaa")
    		val e = Element("aaa", ns1)
    		e.setAttribute("att1", "att1")
    		val defns = Namespace.getNamespace("http://acme.com/default")
    		e.addNamespaceDeclaration(defns)
    		// pass
    	} catch (e: Exception) {
    		// also see http://markmail.org/message/hvzz73em7ztt5i5k
    		fail("Bug http://www.junlu.com/msg/166290.html")
    	}
    	
    	val nsnp = Namespace.getNamespace("rootns")
    	val nswp = Namespace.getNamespace("p", nsnp.uri)
    	val mscp = Namespace.getNamespace("p", "childns")
    	val ans = Namespace.getNamespace("a", "attns")
    	
    	// no collision between Namespace and itself
    	assertNull(Verifier.checkNamespaceCollision(nsnp, nsnp))
    	
    	// no collision between Namespace and other namespace with different prefix
    	assertNull(Verifier.checkNamespaceCollision(nsnp, nswp))
    	
    	// but collision between same prefix, but different URI
    	assertNotNull(Verifier.checkNamespaceCollision(nswp, mscp))
    	
    	val root = Element("root", nsnp)
    	root.addNamespaceDeclaration(nswp)
    	val att = Attribute("att", "val", nswp)
    	
    	// should be able to add 
    	assertNull(Verifier.checkNamespaceCollision(att, root))
    	root.setAttribute(att)
    	
    	root.setAttribute(Attribute("ans", "v", ans))
    	
    	// cnns is child no namespace
    	val cnns = Element ("cnns")
    	root.addContent(cnns)
    	// cwns is child with namespace
    	val cwns = Element ("cwns", nsnp)
    	root.addContent(cwns)
    	// cpns is child prefixed namespace
    	val cpns = Element ("cpns", nswp)
    	root.addContent(cpns)
    	// cpms is child prefixed different namespace
    	val cpms = Element ("cpms", mscp)
    	root.addContent(cpms)
    	
    	//printlement(root);
//    	XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
//    	try {
//    		CharArrayWriter  w = new CharArrayWriter();
//			out.output(root, w);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

    	
    	// Check Namespace against elements.
    	assertNull   (Verifier.checkNamespaceCollision(att, root))
    	assertNull   (Verifier.checkNamespaceCollision(att, cnns))
    	assertNull   (Verifier.checkNamespaceCollision(att, cwns))
    	assertNull   (Verifier.checkNamespaceCollision(att, cpns))
    	
    	// now, check it against the child that redefines the 'p' prefix.
    	assertNotNull(Verifier.checkNamespaceCollision(nswp, cpms))
    	// root has an attribute with the namspace a->ans, cannot then have a->dummy
    	assertNotNull(Verifier.checkNamespaceCollision(Namespace.getNamespace("a", "dummy"), root))
    	// root has an additional namespace p->rootns, can't then have a different 'p'
    	assertNotNull(Verifier.checkNamespaceCollision(Namespace.getNamespace("p", "dummy"), root))
    	
    	// Check Namespace against Attributes
    	// we do the same tests as above, only we wrap it in an Attribute
    	// first, one that passes.
    	assertNull(Verifier.checkNamespaceCollision(att, cwns))
    	// now, check it against the child that redefines the 'p' prefix.
    	assertNotNull(Verifier.checkNamespaceCollision(att, cpms))
    	// root has an attribute with the namspace a->ans, cannot then have a->dummy
    	assertNotNull(Verifier.checkNamespaceCollision(
    			Attribute("ax", "v", Namespace.getNamespace("a", "dummy")), root))
    	// root has an additional namespace p->rootns, can't then have a different 'p'
    	assertNotNull(Verifier.checkNamespaceCollision(
    			Attribute("ax", "v", Namespace.getNamespace("p", "dummy")), root))
    	
    	
    	// now we need to check the namespaces against Attributes not Elements
    	// first, one that works
    	assertNull(Verifier.checkNamespaceCollision(nswp, Attribute("foo", "bar")))
    	// now, check it against the child that redefines the 'p' prefix.
    	assertNotNull(Verifier.checkNamespaceCollision(nswp, Attribute("foo", "bar", mscp)))
    	
    	// Now test some List structures.
    	// first, one that passes.
    	assertNull(Verifier.checkNamespaceCollision(nswp, root.additionalNamespaces))
    	// then, a passing one... with junk...
    	var c: MutableList<Any?>? = null
    	assertNull(Verifier.checkNamespaceCollision(nswp, c))
    	c = ArrayList<Any?>()
    	c.addAll(root.additionalNamespaces)
    	c.add(1)
    	c.add(0, "dummy")
    	assertNull(Verifier.checkNamespaceCollision(nswp, c))
    	
    	// now, check something that will conflict... check against a conflicting attribute.
    	// which is already in there.
    	assertNotNull(Verifier.checkNamespaceCollision(mscp, c))
    	
    	// now check against an Element.
    	// replace prefixed namespace with prefixed Element
    	c.set(c.indexOf(nswp), cpns)
    	assertNotNull(Verifier.checkNamespaceCollision(mscp, c))
    	
    	// now replace prefixed Element with prefixed Attribute
    	c.set(c.indexOf(cpns), Attribute("a", "b", nswp))
    	assertNotNull(Verifier.checkNamespaceCollision(mscp, c))
    	
    	
    	// some basic namespace/attribute tests similar to the ones done for the bug test up top.
    	// attributes with no prefix can never collide... because attributes are always
    	// in the NO_NAMESPACE unless prefixed.
    	assertNull(Verifier.checkNamespaceCollision(Attribute("a", "b"), root))
    	// att is already on root, can't fail.
    	assertNull(Verifier.checkNamespaceCollision(att, root))
    	// but, we can fail an attribute on the child with changed p prefix
    	assertNotNull(Verifier.checkNamespaceCollision(Attribute("a", "b", mscp), root))
    	
    }
    
    @Test fun testCheckPublicID() {
    	assertNull(Verifier.checkPublicID(null), "invalidated valid publicid: null")
    	assertNull(Verifier.checkPublicID(""), "invalidated valid publicid: ''")
    	assertNull(Verifier.checkPublicID("  "), "invalidated valid publicid: '  '")
    	assertNull(Verifier.checkPublicID("shroedinger's cat was here"), "invalidated valid publicid: contains \"'\"")
    	
    	assertNotNull(Verifier.checkPublicID("cannot have " + BADCHAR + " characters here"))
    	
    }

    @Test fun testCheckXMLName() {
    	assertNull(Verifier.checkXMLName("hi"))
    	assertNull(Verifier.checkXMLName("hi2you"))
    	assertNull(Verifier.checkXMLName("hi_you"))
    	assertNull(Verifier.checkXMLName("hi:you"))
    	
    	assertNotNull(Verifier.checkXMLName(null))
    	assertNotNull(Verifier.checkXMLName(""))
    	assertNotNull(Verifier.checkXMLName("   "))
    	assertNotNull(Verifier.checkXMLName("  hi  "))
    	assertNotNull(Verifier.checkXMLName("hi "))
    	assertNotNull(Verifier.checkXMLName(" hi"))
    	assertNotNull(Verifier.checkXMLName("2bad"))
    }
    
    @Test fun testCheckSystemLiteral() {
    	assertNull(Verifier.checkSystemLiteral(null))
    	assertNull(Verifier.checkSystemLiteral(""))
    	assertNull(Verifier.checkSystemLiteral("  "))
    	assertNull(Verifier.checkSystemLiteral("frodo's theme "))
    	assertNull(Verifier.checkSystemLiteral("frodo has a \"theme\" "))
    	
    	assertNotNull(Verifier.checkSystemLiteral("frodo's \"theme\" "))
    	
    }


    @Test fun testCheckURI() {
    	assertNull(Verifier.checkURI(null))
    	assertNull(Verifier.checkURI(""))
    	assertNull(Verifier.checkURI("http://www.jdom.org/index.html"))
    	assertNull(Verifier.checkURI("http://www.jdom.org:321/index.html"))
    	assertNull(Verifier.checkURI("http://www.jdom.org%32%01/index.html?%ab"))
    	assertNull(Verifier.checkURI("http://www.jdom.org/index.html%31"))
    	
    	assertNotNull(Verifier.checkURI("http://www.jdom.org/ index.html"))
    	assertNotNull(Verifier.checkURI("  http://www.jdom.org/index.html  "))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org%3.21/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org%.21/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org%3g21/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org%3/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org" + BADCHAR + "/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org" + 0x05.toChar() + "/index.html"))
    	assertNotNull(Verifier.checkURI("http://www.jdom.org/index.html%3"))
    }
    
    @Test fun testIsXMLCharacter() {
    	// this test is not part of the automatic tests because it takes an int
    	// as an argument, instead of a char.
    	// cherry-pick some tests.
    	assertTrue(Verifier.isXMLCharacter('\n'.code))
    	assertTrue(Verifier.isXMLCharacter('\r'.code))
    	assertTrue(Verifier.isXMLCharacter('\t'.code))
    	assertTrue(Verifier.isXMLCharacter(' '.code))
    	assertTrue(Verifier.isXMLCharacter(0xd7ff))
    	assertTrue(Verifier.isXMLCharacter(0xe000))
    	assertTrue(Verifier.isXMLCharacter(0x10000))
    	
    	//cherry-pick values we know will fill out the coverage report.
    	assertFalse(Verifier.isXMLCharacter(0))
    	assertFalse(Verifier.isXMLCharacter(0x19))
    	assertFalse(Verifier.isXMLCharacter(0xd800))
    	assertFalse(Verifier.isXMLCharacter(0xffff))
    	assertFalse(Verifier.isXMLCharacter(0x110000))
    	
    }
    
    @Test fun testIsAllXMLWhitespace() {
    	assertTrue(Verifier.isAllXMLWhitespace(""))
    	assertTrue(Verifier.isAllXMLWhitespace(" "))
    	assertTrue(Verifier.isAllXMLWhitespace(" \r\n\t "))
    	assertFailsWith<NullPointerException>("Expected a NullPointerException, but it did not happen") {
    		Verifier.isAllXMLWhitespace(nullT())
    	}
    	assertFalse(Verifier.isAllXMLWhitespace(" a "))
    	assertFalse(Verifier.isAllXMLWhitespace(" &nbsp; "))
    	// \u00A0 is Non-Break space. 
    	assertFalse(Verifier.isAllXMLWhitespace("\u00A0"))
    }

}
