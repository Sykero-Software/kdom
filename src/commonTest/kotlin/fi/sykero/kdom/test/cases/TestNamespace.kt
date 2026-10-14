/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases

import kotlin.test.*
//JAVA javax.xml.XMLConstants
import fi.sykero.kdom.*

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
 * @author unascribed
 * @version 0.1
 */





class TestNamespace {

	

	/**
	 * Test the object comparison method.
	 */
	@Test fun test_TCM__boolean_equals_Object() {
		var ns = Namespace.getNamespace("prefx", "http://some.other.place")
		var ob: Any = ns
		assertTrue(ns.equals(ob), "object not equal to attribute")

		ns = Namespace.NO_NAMESPACE
		ob = ns
		assertTrue(ns.equals(ob), "object not equal to attribute")

		//ns = Namespace.EMPTY_NAMESPACE;
		//ob = (Object)ns;
		//assertTrue("object not equal to attribute", ns.equals(ob));


	}

	/**
	 * Verify that a namespace will produce a hashcode.
	 */
	@Test fun test_TCM__int_hashCode() {
		val ns = Namespace.getNamespace("test", "value")
		//only an exception would be a problem
		var i: Int = -1
		try {
			i = ns.hashCode()
		}
		catch (e: Exception) {
			fail("bad hashCode")
		}

		//make sure a new one doesn't have the same value
		val ns2 = Namespace.getNamespace("test", "value2")
		val x: Int = ns2.hashCode()
		assertTrue(i!=x, "duplicate hashCode")

		//test hashcode for NO_NAMESPACE
		//only an exception would be a problem
		try {
			// Namespace.hashCode() is uri.hashCode().
			// NO_NAMESPACE has URI ""
			assertTrue(Namespace.NO_NAMESPACE.hashCode() == "".hashCode())
		}
		catch (e: Exception) {
			fail("bad hashCode")
		}

		//test hashcode for NO_NAMESPACE
		//y = Namespace.EMPTY_NAMESPACE.hashCode();
		//only an exception would be a problem
		//assertTrue("bad hashcode" , true);
	}

	/**
	 * Test the URI only Namespace.
	 */
	@Test fun test_TCM__OrgJdomNamespace_getNamespace_String() {
		val ns = Namespace.getNamespace("http://some.new.place")
		assertTrue(ns.toString().equals("[Namespace: prefix \"\" is mapped to URI \"http://some.new.place\"]"), "Incorrect namespace created")
		//the is really the default NO_NAMESPACE version
		val ns2 = Namespace.getNamespace("")
		assertTrue(ns2.toString().equals("[Namespace: prefix \"\" is mapped to URI \"\"]"), "Incorrect no namespace namespace created")

	}

	/**
	 * Test the prefix, uri version of getNamespace.
	 */
	@Test fun test_TCM__OrgJdomNamespace_getNamespace_String_String() {
		val ns = Namespace.getNamespace("prefx", "http://some.other.place")
		assertTrue(ns.toString().equals("[Namespace: prefix \"prefx\" is mapped to URI \"http://some.other.place\"]"), "Incorrect namespace created")

		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace(""))
		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace(null))
		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace(null, null))
		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace(null, ""))
		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace("", ""))
		assertTrue(Namespace.NO_NAMESPACE == Namespace.getNamespace("", null))
		
		assertTrue(Namespace.XML_NAMESPACE == Namespace.getNamespace(
				"xml", "http://www.w3.org/XML/1998/namespace"))
		
		assertFailsWith<IllegalNameException>("Should not be able to re-brand Namespace prefix 'xml'") {
			assertTrue(null != Namespace.getNamespace("xml", "myuri"))
		}
		
		assertFailsWith<IllegalNameException>("Should not be able to re-brand xml namespace URI to another prefix too.") {
			assertTrue(null != Namespace.getNamespace("pfx", "http://www.w3.org/XML/1998/namespace"))
		}
		
		assertFailsWith<IllegalNameException>("Should not be able to have a URI starting with '-'.") {
			assertTrue(null != Namespace.getNamespace("pfx", "-this is illegal..."))
		}
		
		assertFailsWith<IllegalNameException>("Should not be able to create Namespace with illegal prefix 'p:x'") {
			assertTrue(null != Namespace.getNamespace("p:x", "myuri"))
		}
		
		assertFailsWith<IllegalNameException>("Should not be able to create Namespace with no URI") {
			assertTrue(null != Namespace.getNamespace("pfx", " "))
		}
		
		assertFailsWith<IllegalNameException>("Should not be able to create Namespace with no URI") {
			assertTrue(null != Namespace.getNamespace("pfx", null))
		}
		
	}

	/**
	 * Test getPrefix()
	 */
	@Test fun test_TCM__String_getPrefix() {
		var ns = Namespace.getNamespace("prefx","http://foo")
		assertTrue(ns.prefix.equals("prefx"), "Incorrect namespace prefix")

		//ns = Namespace.EMPTY_NAMESPACE;
		//assertTrue("Incorrect empty namespace prefix", ns.getPrefix().equals(""));

		ns = Namespace.NO_NAMESPACE
		assertTrue(ns.prefix.equals(""), "Incorrect empty namespace prefix")

	}

	/**
	 * Test than a namespace returns the correct URI
	 */
	@Test fun test_TCM__String_getURI() {
		val ns = Namespace.getNamespace("prefx","http://foo")
		assertTrue(ns.uri.equals("http://foo"), "Incorrect namespace prefix")

	}

	/**
	 * Test that toString() operates according to JDOM specs
	 */
	@Test fun test_TCM__String_toString() {
		val ns = Namespace.getNamespace("http://some.new.place")
		assertTrue(ns.toString().equals("[Namespace: prefix \"\" is mapped to URI \"http://some.new.place\"]"), "Incorrect namespace created")
		//the is really the default NO_NAMESPACE version
		var ns2 = Namespace.getNamespace("")
		assertTrue(ns2.toString().equals("[Namespace: prefix \"\" is mapped to URI \"\"]"), "Incorrect no namespace namespace created")
		ns2 = Namespace.getNamespace("prefx","http://foo")
		assertTrue(ns2.toString().equals("[Namespace: prefix \"prefx\" is mapped to URI \"http://foo\"]"), "Incorrect namespace created")

	}
	
    @Test fun testXMLNamespaceGood() {
        val ns = Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace")
        assertTrue(ns === Namespace.XML_NAMESPACE)
    }
    
	@Test fun testXMLNamespacePrefix() {
		try {
			assertFailsWith<IllegalNameException>("Should not be able to redefine 'xml' prefix.") {
			Namespace.getNamespace("xml", "not right")
		}
		} catch (e: Exception) {
			e.printStackTrace()
			fail("We expect IllegalNameException not " + e::class)
		}
	}
	
    @Test fun testXMLNamespaceOnlyPrefix() {
        try {
        	assertFailsWith<IllegalNameException>("Should not be able to have XML Namespace URI mapped to other prefix.") {
            Namespace.getNamespace("other", JDOMConstants.NS_URI_XML)
        }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("We expect IllegalNameException not " + e::class)
        }
    }
    
    @Test fun testXMLNamespaceDefault() {
        try {
        	assertFailsWith<IllegalNameException>("Should not be able to have XML Namespace URI mapped the default namespace.") {
            Namespace.getNamespace(JDOMConstants.NS_URI_XML)
        }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("We expect IllegalNameException not " + e::class)
        }
    }
    
    
    @Test fun testXMLNSNamespaceGood() {
        val ns = Namespace.getNamespace("xmlns", "http://www.w3.org/2000/xmlns/")
        assertTrue("xmlns".equals(ns.prefix))
    }
    
    @Test fun testXMLNSNamespacePrefix() {
        try {
        	assertFailsWith<IllegalNameException>("Should not be able to redefine 'xmlns' prefix.") {
            Namespace.getNamespace("xmlns", "not right")
        }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("We expect IllegalNameException not " + e::class)
        }
    }
    
    @Test fun testXMLNSNamespaceOnlyPrefix() {
        try {
        	assertFailsWith<IllegalNameException>("Should not be able to have XMLNS Namespace URI mapped to other prefix.") {
            Namespace.getNamespace("other", JDOMConstants.NS_URI_XMLNS)
        }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("We expect IllegalNameException not " + e::class)
        }
    }
    
    @Test fun testXMLNSNamespaceDefault() {
        try {
        	assertFailsWith<IllegalNameException>("Should not be able to have XMLNS Namespace URI mapped the default namespace.") {
            Namespace.getNamespace(JDOMConstants.NS_URI_XMLNS)
        }
        } catch (e: Exception) {
            e.printStackTrace()
            fail("We expect IllegalNameException not " + e::class)
        }
    }
    
    
}
