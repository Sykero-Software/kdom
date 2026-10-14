package fi.sykero.kdom.test.cases

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
 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */

import kotlin.test.*
import fi.sykero.kdom.test.util.UnitTestUtil.checkException
import fi.sykero.kdom.test.util.UnitTestUtil.failNoException
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.AttributeType
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.IllegalAddException
import fi.sykero.kdom.IllegalNameException
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text
import fi.sykero.kdom.Content.CType
import fi.sykero.kdom.filter.ContentFilter
import fi.sykero.kdom.filter.ElementFilter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.XMLOutputter
import fi.sykero.kdom.test.util.UnitTestUtil
import fi.sykero.kdom.test.util.nullT

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









































class TestElement {

    

    /**
     * Test the constructor for an empty element
     */
    @Test fun test_TCC___String() {

        //create a new empty element
        var el = Element("theElement")
        assertTrue(el.name.equals("theElement"), "wrong element name after constructor")
        assertTrue(el.namespace.equals(Namespace.NO_NAMESPACE), "expected NO_NAMESPACE")
        assertTrue(el.children.equals(emptyList<Any?>()), "expected no child elements")
        assertTrue(el.attributes.equals(emptyList<Any?>()), "expected no attributes")

        //must have a name
        assertFailsWith<IllegalNameException>("allowed creation of an element with no name") {
            el = Element("")
        }

        //name can't be null
        assertFailsWith<IllegalNameException>("allowed creation of an element with null name") {
            el = Element(null)
        }

        //we can assume the Verifier has been called by now so we are done
    }

    /**
     * Test the Element constructor with an name and namespace
     */
    @Test fun test_TCC___String_OrgJdomNamespace() {
        //create a new empty element with a namespace

        val ns = Namespace.getNamespace("urn:foo")
        var el = Element("theElement", ns)
        assertTrue(el.name.equals("theElement"), "wrong element name after constructor")
        assertTrue(el.namespace.equals(Namespace.getNamespace("urn:foo")), "expected urn:foo namespace")
        assertTrue(el.children.equals(emptyList<Any?>()), "expected no child elements")
        assertTrue(el.attributes.equals(emptyList<Any?>()), "expected no attributes")

        //must have a name
        assertFailsWith<IllegalNameException>("allowed creation of an element with no name") {
            el = Element("", ns)
        }

        //name can't be null
        assertFailsWith<IllegalNameException>("allowed creation of an element with null name") {
            el = Element(null, ns)
        }

        //we can assume the Verifier has been called by now so we are done
    }

    /**
     * Test the Element constructor with a string default namespace
     */
    @Test fun test_TCC___String_String() {
        //create a new empty element with a namespace

        var el = Element("theElement", "urn:foo")
        assertTrue(el.name.equals("theElement"), "wrong element name after constructor")
        assertTrue(el.namespace.equals(Namespace.getNamespace("urn:foo")), "expected urn:foo namespace")
        assertTrue(el.children.equals(emptyList<Any?>()), "expected no child elements")
        assertTrue(el.attributes.equals(emptyList<Any?>()), "expected no attributes")

        //must have a name
        assertFailsWith<IllegalNameException>("allowed creation of an element with no name") {
            el = Element("", "urn:foo")
        }

        //name can't be null
        assertFailsWith<IllegalNameException>("allowed creation of an element with null name") {
            el = Element(null, "urn:foo")
        }

        //we can assume the Verifier has been called by now so we are done
    }

    /**
     * Test the Element constructor with a namespace uri and prefix
     */
    @Test fun test_TCC___String_String_String() {
        //create a new empty element with a namespace


        var el = Element("theElement", "x", "urn:foo")
        assertTrue(el.name.equals("theElement"), "wrong element name after constructor")
        assertTrue(el.namespace.equals(Namespace.getNamespace("x", "urn:foo")), "expected urn:foo namespace")
        assertTrue(el.children.equals(emptyList<Any?>()), "expected no child elements")
        assertTrue(el.attributes.equals(emptyList<Any?>()), "expected no attributes")

        //must have a name
        assertFailsWith<IllegalNameException>("allowed creation of an element with no name") {
            el = Element("", "x", "urn:foo")
        }

        //name can't be null
        assertFailsWith<IllegalNameException>("allowed creation of an element with null name") {
            el = Element(null, "x", "urn:foo")
        }

        //we can assume the Verifier has been called by now so we are done

    }

    /**
     * Test the equals compares only object instances
     */
    @Test fun test_TCM__boolean_equals_Object() {
        val el = Element("theElement", "x", "urn:foo")
        val el2 = Element("theElement", "x", "urn:foo")

        assertTrue(((el as Any?))!!.equals(el), "incorrect equals evaluation")
        assertTrue(!((el2 as Any?))!!.equals(el), "incorrect equals evaluation")
    }

    /**
     * Test that hasChildren only reports true for actual child elements.
     */
/*
    public void test_TCM__boolean_hasChildren() {
        //set up an element to test with
        Element element = new Element("element", Namespace.getNamespace("http://foo"));
        assertTrue("reported children when there are none", !element.hasChildren());

        Attribute att1 = new Attribute("anAttribute", "boo");
        element.setAttribute(att1);
        assertTrue("reported children when there are none", !element.hasChildren());

        //add some text
        element.addContent("the text");
        assertTrue("reported children when there are none", !element.hasChildren());

        //add some CDATA
        element.addContent(new CDATA("the text"));
        assertTrue("reported children when there are none", !element.hasChildren());

        //add a PI
        element.addContent(new ProcessingInstruction("pi", "the text"));
        assertTrue("reported children when there are none", !element.hasChildren());

        //add Comment
        element.addContent(new Comment("the text"));
        assertTrue("reported children when there are none", !element.hasChildren());

        //finally a child element
        Element child1 = new Element("child1");
        element.addContent(child1);
        assertTrue("reported no children when there is a child element", element.hasChildren());
    }
*/

    /**
     * Test that hasMixedContent works for varying types of possible
     * child and other content
     */
    @Test fun test_TCM__boolean_hasMixedContent() {
        /** No op because the method is deprecated

         //set up an element to test with
         Element element= new Element("element", Namespace.getNamespace("http://foo"));
         assertTrue("reported mixed content when there is none", ! element.hasMixedContent());


         Attribute att1 = new Attribute("anAttribute", "boo");
         element.setAttribute(att1);
         assertTrue("reported mixed content when there is none", ! element.hasMixedContent());

         //add some text
         element.addContent("the text");
         assertTrue("reported mixed content when there is none", ! element.hasMixedContent());

         //add some CDATA
         element.addContent(new CDATA("the text"));
         assertTrue("reported no mixed content when there is", element.hasMixedContent());

         element= new Element("element");
         //add a PI
         element.addContent(new ProcessingInstruction("pi", "the text"));
         assertTrue("reported mixed content when there is none", ! element.hasMixedContent());

         //add Comment
         element.addContent(new Comment("the text"));
         assertTrue("reported no mixed content when there is", element.hasMixedContent());

         element= new Element("element");
         //finally a child element
         Element child1= new Element("child1");
         element.addContent(child1);
         assertTrue("reported mixed content when there is none", ! element.hasMixedContent());

         element.addContent("some text");
         assertTrue("reported no mixed content when there is",  element.hasMixedContent());

         */
    }

    /**
     * Test that an Element can determine if it is a root element
     */
    @Test fun test_TCM__boolean_isRootElement() {
        val element = Element("element")
        assertTrue(!element.isRootElement, "incorrectly identified element as root")

        Document(element)
        assertTrue(element.isRootElement, "incorrectly identified element as non root")
    }

    /**
     * Test than an Element can remove an Attribute by name
     */
    @Test fun test_TCM__boolean_removeAttribute_String() {
        val element = Element("test")
        assertFalse(element.removeAttribute("att"))
        val att = Attribute("anAttribute", "test")
        element.setAttribute(att)

        //make sure it's there
        assertNotNull(element.getAttribute("anAttribute"), "attribute not found after add")

        //and remove it
        assertTrue(element.removeAttribute("anAttribute"), "attribute not removed")
        //make sure it's not there
        assertNull(element.getAttribute("anAttribute"), "attribute found after remove")
        assertFalse(element.removeAttribute("anAttribute"), "non-existing attribute remove returned true")
    }

    /**
     * Test than an Element can remove an Attribute by name
     */
    @Test fun test_TCM__boolean_removeAttribute_Attribute() {
        val element = Element("test")
        val att = Attribute("anAttribute", "test")
        assertFalse(element.removeAttribute(att))
        
        element.setAttribute(att)

        //make sure it's there
        assertNotNull(element.getAttribute("anAttribute"), "attribute not found after add")

        //and remove it
        assertTrue(element.removeAttribute(att), "attribute not removed")
        //make sure it's not there
        assertNull(element.getAttribute("anAttribute"), "attribute found after remove")
        assertFalse(element.removeAttribute(att), "non-existing attribute remove returned true")
    }

    /**
     * Test removeAttribute with a namespace
     */
    @Test fun test_TCM__boolean_removeAttribute_String_OrgJdomNamespace() {
        val element = Element("test")
        val ns = Namespace.getNamespace("x", "urn:test")
        assertFalse(element.removeAttribute("anAttribute", ns))
        
        val att = Attribute("anAttribute", "test", ns)
        element.setAttribute(att)

        //make sure it's there
        assertNotNull(element.getAttribute("anAttribute", ns), "attribute not found after add")
        //make sure we check namespaces - a different namespace
        assertFalse(element.removeAttribute("anAttribute", Namespace.NO_NAMESPACE), "wrong attribute removed")
        //and remove it
        assertTrue(element.removeAttribute("anAttribute", ns), "attribute not removed")
        //make sure it's not there
        assertNull(element.getAttribute("anAttribute", ns), "attribute found after remove")
    }

    /**
     * Test removeAtttribute with a namespace uri.
     */
    @Test fun test_TCM__boolean_removeAttribute_String_String() {
        /** No op because the method is deprecated

         Element element = new Element("test");
         Namespace ns = Namespace.getNamespace("x", "urn:test");
         Attribute att = new Attribute("anAttribute", "test", ns);
         element.setAttribute(att);

         //make sure it's there
         assertNotNull("attribute not found after add", element.getAttribute("anAttribute", ns));
         //and remove it
         assertTrue("attribute not removed", element.removeAttribute("anAttribute", "urn:test"));
         //make sure it's not there
         assertNull("attribute found after remove", element.getAttribute("anAttribute", ns));
         */
    }

    /**
     * Test removeChild by name
     */
    @Test fun test_TCM__boolean_removeChild_String() {
        val element = Element("element")
        val child = Element("child")
        element.addContent(child)

        assertTrue(element.removeChild("child"), "couldn't remove child content")
        assertNull(element.getChild("child"), "child not removed")
    }

    /**
     * Test removeChild by name and namespace
     */
    @Test fun test_TCM__boolean_removeChild_String_OrgJdomNamespace() {
        val ns = Namespace.getNamespace("x", "urn:fudge")
        val element = Element("element")
        val child = Element("child", ns)
        val child2 = Element("child", ns)
        element.addContent(child)

        assertTrue(element.removeChild("child", ns), "couldn't remove child content")
        assertNull(element.getChild("child", ns), "child not removed")
        //now test that only the first child is removed
        element.addContent(child)
        element.addContent(child2)
        assertTrue(element.removeChild("child", ns), "couldn't remove child content")
        assertNotNull(element.getChild("child", ns), "child not removed")

    }

    /**
     * Test removeChildren which removes all child elements
     */
/*
    public void test_TCM__boolean_removeChildren() {
        Namespace ns = Namespace.getNamespace("x", "urn:fudge");
        Element element = new Element("element");

        assertTrue("incorrectly returned true when deleting no content", element.removeChildren() == false);

        Element child = new Element("child", ns);
        Element child2 = new Element("child", ns);
        element.addContent(child);
        element.addContent(child2);

        assertTrue("couldn't remove child content", element.removeChildren());
        assertNull("child not removed", element.getContent("child", ns));
    }
*/

    /**
     * Test removeChildren by name.
     */
/*
    public void test_TCM__boolean_removeChildren_String() {
        Element element = new Element("element");

        assertTrue("incorrectly returned true when deleting no content", element.removeChildren() == false);

        Element child = new Element("child");
        Element child2 = new Element("child");
        element.addContent(child);
        element.addContent(child2);

        assertTrue("incorrect return on bogus child", !element.removeChildren("test"));
        assertNotNull("child incorrectly removed", element.getContent("child"));
        assertTrue("couldn't remove child content", element.removeChildren("child"));
        assertNull("children not removed", element.getContent("child"));
    }
*/

    /**
     * Test removeChildren with a name and namespace
     */
/*
    public void test_TCM__boolean_removeChildren_String_OrgJdomNamespace() {
        Namespace ns = Namespace.getNamespace("x", "urn:fudge");
        Element element = new Element("element");

        assertTrue("incorrectly returned true when deleting no content", element.removeChildren() == false);

        Element child = new Element("child", ns);
        Element child2 = new Element("child", ns);
        element.addContent(child);
        element.addContent(child2);

        assertTrue("incorrect return on bogus child", !element.removeChildren("child"));
        assertNotNull("child incorrectly removed", element.getContent("child", ns));
        assertTrue("couldn't remove child content", element.removeChildren("child", ns));
        assertNull("children not removed", element.getContent("child", ns));
    }
*/

    /**
     * Test removeContent for a Comment
     */
    @Test fun test_TCM__boolean_removeContent_OrgJdomComment() {
        val element = Element("element")
        val comm = Comment("a comment")
        element.addContent(comm)

        assertTrue(element.removeContent(comm), "couldn't remove comment content")
        assertTrue(element.content.equals(emptyList<Any?>()), "didn't remove comment content")
    }

    /**
     * Test removeContent for an Element.
     */
    @Test fun test_TCM__boolean_removeContent_OrgJdomElement() {
        val element = Element("element")
        val child = Element("child")
        element.addContent(child)

        assertTrue(element.removeContent(child), "couldn't remove element content")
        assertTrue(element.content.equals(emptyList<Any?>()), "didn't remove element content")
    }

    /**
     * Test removeContent for entities.
     */
    @Test fun test_TCM__boolean_removeContent_OrgJdomEntity() {
        val element = Element("element")
        val ent = EntityRef("anEntity")
        element.addContent(ent)

        assertTrue(element.removeContent(ent), "couldn't remove entity content")
        assertTrue(element.content.equals(emptyList<Any?>()), "didn't remove entity content")
    }

    /**
     * Test removeContent for processing instructions.
     */
    @Test fun test_TCM__boolean_removeContent_OrgJdomProcessingInstruction() {
        val element = Element("element")
        val pi = ProcessingInstruction("aPi", "something")
        element.addContent(pi)

        assertTrue(element.removeContent(pi), "couldn't remove entity content")
        assertTrue(element.content.equals(emptyList<Any?>()), "didn't remove entity content")
    }

    /**
     * Test hashcode functions.
     */
    @Test fun test_TCM__int_hashCode() {
        val element = Element("test")
        //only an exception would be a problem
        var i: Int = -1
        try {
            i = element.hashCode()
        }
        catch (e: Exception) {
            fail("bad hashCode")
        }


        val element2 = Element("test")
        //different Elements, same text
        val x: Int = element2.hashCode()
        assertTrue(x != i, "Different Elements with same value have same hashcode")
        val element3 = Element("test2")
        //only an exception would be a problem
        val y: Int = element3.hashCode()
        assertTrue(y != x, "Different Elements have same hashcode")

    }

    /**
     * Test that additionalNamespaces are returned.
     */
    @Test fun test_TCM__List_getAdditionalNamespaces() {
        val element = Element("element")
        element.addNamespaceDeclaration(Namespace.getNamespace("x", "urn:foo"))
        element.addNamespaceDeclaration(Namespace.getNamespace("y", "urn:bar"))
        element.addNamespaceDeclaration(Namespace.getNamespace("z", "urn:baz"))

        val list = element.additionalNamespaces

        var ns = list.get(0)
        assertTrue(ns.uri.equals("urn:foo"), "didn't return added namespace")
        ns = list.get(1)
        assertTrue(ns.uri.equals("urn:bar"), "didn't return added namespace")
        ns = list.get(2)
        assertTrue(ns.uri.equals("urn:baz"), "didn't return added namespace")
    }

    /**
     * Test that getAttribute returns all attributes of this element.
     */
    @Test fun test_TCM__List_getAttributes() {
        val element = Element("test")
        val att = Attribute("anAttribute", "test")
        val att2 = Attribute("anotherAttribute", "test")
        val att3 = Attribute("anotherAttribute", "test", Namespace.getNamespace("x", "urn:JDOM"))
        element.setAttribute(att)
        element.setAttribute(att2)
        element.setAttribute(att3)

        val list = element.attributes

        assertEquals(list.size, 3, "incorrect size returned")
        assertEquals(list.get(0), att, "incorrect attribute returned")
        assertEquals(list.get(1), att2, "incorrect attribute returned")
    }

    /**
     * Test getChildren to return all children from all namespaces.
     */
    @Test fun test_TCM__List_getChildren() {
        val element = Element("el")
        assertEquals(emptyList<Any?>(), element.getChildren("child"), "did not return Collections.EMPTY_LIST on empty element")
        val child1 = Element("child")
        child1.setAttribute(Attribute("name", "first"))

        val child2 = Element("child")
        child2.setAttribute(Attribute("name", "second"))
        val child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))

        element.addContent(child1)
        element.addContent(child2)
        element.addContent(child3)

        //should only get the child elements in NO_NAMESPACE
        val list = element.children
        assertEquals(list.size, 3, "incorrect number of children returned")
        assertEquals(list.get(0), child1, "incorrect child returned")
        assertEquals(list.get(1), child2, "incorrect child returned")
        assertEquals(list.get(2), child3, "incorrect child returned")
    }

    /**
     * Test that Element returns a List of children by name
     */
    @Test fun test_TCM__List_getChildren_String() {
        val element = Element("el")
        assertEquals(emptyList<Any?>(), element.getChildren("child"), "did not return Collections.EMPTY_LIST on empty element")
        val child1 = Element("child")
        child1.setAttribute(Attribute("name", "first"))

        val child2 = Element("child")
        child2.setAttribute(Attribute("name", "second"))
        val child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))

        element.addContent(child1)
        element.addContent(child2)
        element.addContent(child3)

        //should only get the child elements in NO_NAMESPACE
        val list = element.getChildren("child")
        assertEquals(list.size, 2, "incorrect number of children returned")
        assertEquals(list.get(0).getAttribute("name")!!.value, "first", "incorrect child returned")
        assertEquals(list.get(1).getAttribute("name")!!.value, "second", "incorrect child returned")
    }

    /**
     * Test that Element returns a List of children by name and namespace
     */
    @Test fun test_TCM__List_getChildren_String_OrgJdomNamespace() {
        val element = Element("el")
        assertEquals(emptyList<Any?>(), element.getChildren("child"), "did not return Collections.EMPTY_LIST on empty element")
        val ns = Namespace.getNamespace("urn:hogwarts")
        val child1 = Element("child", ns)
        child1.setAttribute(Attribute("name", "first"))

        val child2 = Element("child", ns)
        child2.setAttribute(Attribute("name", "second"))
        val child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))

        element.addContent(child1)
        element.addContent(child2)
        element.addContent(child3)

        //should only get the child elements in the hogwarts namespace
        val list = element.getChildren("child", ns)
        assertEquals(list.size, 2, "incorrect number of children returned")
        assertEquals(list.get(0).getAttribute("name")!!.value, "first", "incorrect child returned")
        assertEquals(list.get(1).getAttribute("name")!!.value, "second", "incorrect child returned")
    }

    /**
     * Test that getContent returns all the content for the element
     */
    @Test fun test_TCM__List_getContent() {
        val element = Element("el")
        assertEquals(emptyList<Any?>(), element.content, "did not return Collections.EMPTY_LIST on empty element")
        val ns = Namespace.getNamespace("urn:hogwarts")
        val child1 = Element("child", ns)
        child1.setAttribute(Attribute("name", "first"))

        val child2 = Element("child", ns)
        child2.setAttribute(Attribute("name", "second"))
        val child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))

        element.addContent(child1)
        element.addContent(child2)
        element.addContent(child3)

        val comment = Comment("hi")
        element.addContent(comment)
        val cdata = CDATA("gotcha")
        element.addContent(cdata)
        val pi = ProcessingInstruction("tester", "do=something")
        element.addContent(pi)
        val entity = EntityRef("wizards")
        element.addContent(entity)
        val text = Text("finally a new wand!")
        element.addContent(text)

        val list = element.content

        assertEquals(8, list.size, "incorrect number of content items")
        assertEquals(child1, list.get(0), "wrong child element")
        assertEquals(child2, list.get(1), "wrong child element")
        assertEquals(child3, list.get(2), "wrong child element")
        assertEquals(comment, list.get(3), "wrong comment")
        assertEquals(cdata, list.get(4), "wrong CDATA")
        assertEquals(pi, list.get(5), "wrong ProcessingInstruction")
        assertEquals(entity, list.get(6), "wrong EntityRef")
        assertEquals(text, list.get(7), "wrong text")
    }

    /**
     * Test that clone returns a disconnected copy of the original element.
     * Since clone is a deep copy, that must be tested also
     */
    @Test fun test_TCM__Object_clone() {

        //first the simple case
        var element = Element("simple")
        var clone = element.clone()

        assertTrue(element !== clone, "clone should not be the same object")
        assertNull(clone.parent, "clone should not have a parent")
        assertEquals(element.name, clone.name, "names do not match")

        //now do the content tests to 2 levels deep to verify recursion
        element = Element("el")
        val ns = Namespace.getNamespace("urn:hogwarts")
        element.setAttribute(Attribute("name", "anElement"))
        var child1 = Element("child", ns)
        child1.setAttribute(Attribute("name", "first"))

        var child2 = Element("firstChild", ns)
        child2.setAttribute(Attribute("name", "second"))
        var child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))
        child1.addContent(child2)
        element.addContent(child1)
        element.addContent(child3)

        element.addNamespaceDeclaration(Namespace.getNamespace("foo", "http://test1"))
        element.addNamespaceDeclaration(Namespace.getNamespace("bar", "http://test2"))

        //add mixed content to the nested child2 element
        var comment = Comment("hi")
        child2.addContent(comment)
        var cdata = CDATA("gotcha")
        child2.addContent(cdata)
        var pi = ProcessingInstruction("tester", "do=something")
        child2.addContent(pi)
        var entity = EntityRef("wizards")
        child2.addContent(entity)
        child2.addContent("finally a new wand!")

        //a little more for the element
        element.addContent("top level element text")

        clone = element.clone()
        element = nullT()
        child3 = nullT()
        child2 = nullT()
        child1 = nullT()

        // additional namespaces
        val additional = clone.additionalNamespaces
        assertEquals(additional.get(0), Namespace.getNamespace("foo", "http://test1"), "incorrect deep clone additional namespace")
        assertEquals(additional.get(1), Namespace.getNamespace("bar", "http://test2"), "incorrect deep clone additional namespace")

        val list = clone.content

        //finally the test

        assertEquals(((list.get(0) as Element)).name, "child", "wrong child element")
        assertEquals(((list.get(1) as Element)).name, "child", "wrong child element")
        val deepClone = ((list.get(0) as Element)).getChild("firstChild", Namespace.getNamespace("urn:hogwarts"))

        assertEquals("firstChild", deepClone!!.name, "wrong nested element")
        //comment
        assertTrue(deepClone!!.content.get(0) !== comment, "deep clone comment not a clone")
        comment = nullT()
        assertEquals("hi", ((deepClone!!.content.get(0) as Comment)).text, "incorrect deep clone comment")
        //CDATA
        assertTrue(((deepClone!!.content.get(1) as CDATA)).text.equals(cdata.text), "deep clone CDATA not a clone")
        cdata = nullT()
        assertEquals("gotcha", ((deepClone!!.content.get(1) as CDATA)).text, "incorrect deep clone CDATA")
        //PI
        assertTrue(deepClone!!.content.get(2) !== pi, "deep clone PI not a clone")
        pi = nullT()
        assertEquals("do=something", ((deepClone!!.content.get(2) as ProcessingInstruction)).data, "incorrect deep clone PI")
        //entity
        assertTrue(deepClone!!.content.get(3) !== entity, "deep clone Entity not a clone")
        entity = nullT()
        assertEquals("wizards", ((deepClone!!.content.get(3) as EntityRef)).name, "incorrect deep clone EntityRef")
        //text
        assertEquals("finally a new wand!", ((deepClone!!.content.get(4) as Text)).text, "incorrect deep clone test")
    }

    /**
     * Test getAttribute by name
     */
    @Test fun test_TCM__OrgJdomAttribute_getAttribute_String() {
        val element = Element("el")
        val att = Attribute("name", "first")
        element.setAttribute(att)
        assertEquals(element.getAttribute("name"), att, "incorrect Attribute returned")
    }

    /**
     * Test getAttribute by name and namespace
     */
    @Test fun test_TCM__OrgJdomAttribute_getAttribute_String_OrgJdomNamespace() {
        val element = Element("el")
        val ns = Namespace.getNamespace("x", "urn:foo")
        val att = Attribute("name", "first", ns)
        element.setAttribute(att)
        element.setAttribute(Attribute("name", "first"))
        assertEquals(element.getAttribute("name", ns), att, "incorrect Attribute returned")

    }

    /**
     * Test that an element returns the reference to its enclosing document
     */
    @Test fun test_TCM__OrgJdomDocument_getDocument() {
        val element = Element("element")

        assertNull(element.document, "incorrectly returned document before there is one")

        val child = Element("child")
        val child2 = Element("child")
        element.addContent(child)
        element.addContent(child2)

        val doc = Document(element)

        assertEquals(element.document, doc, "didn't return correct Document")
        assertEquals(child.document, doc, "didn't return correct Document")
    }

    /**
     * Test addContent for CDATA.
     */
    @Test fun test_TCM__OrgJdomElement_addContent_OrgJdomCDATA() {
        //positive test is covered in test__List_getContent()

        //test with null
        val element = Element("element")
        val cdata: CDATA? = null
        assertFailsWith<NullPointerException>("didn't catch null CDATA element") {
            element.addContent(nullT<Content>())
        }
    }

    /**
     * Test adding comment content.
     */
    @Test fun test_TCM__OrgJdomElement_addContent_OrgJdomComment() {
        val element = Element("element")
        var comm = Comment("a comment")
        element.addContent(comm)

        assertEquals(element.content.get(0), comm, "didn't add comment content")
        assertFailsWith<NullPointerException>("didn't catch null Comment") {
            comm = nullT()
            element.addContent(comm)
        }
    }

    /**
     * Test addContent for Element.
     */
    @Test fun test_TCM__OrgJdomElement_addContent_OrgJdomElement() {
        //positive test is covered in test__List_getContent()

        //test with null
        val element = Element("element")
        assertFailsWith<NullPointerException>("didn't catch null Element") {
            val el: Element? = null
            element.addContent(nullT<Content>())
        }
    }

    /**
     * Test addContent for Entity
     */
    @Test fun test_TCM__OrgJdomElement_addContent_OrgJdomEntityRef() {
        //positive test is covered in test__List_getContent()

        //try with null
        val element = Element("element")
        assertFailsWith<NullPointerException>("didn't catch null EntityRef") {
            val entity: EntityRef? = null
            element.addContent(nullT<Content>())
        }
    }

    /**
     * Test addContent for ProcessingInstruction.
     */
    @Test fun test_TCM__OrgJdomElement_addContent_OrgJdomProcessingInstruction() {
        //positive test is covered in test__List_getContent()

        //try with null data
        val element = Element("element")
        assertFailsWith<NullPointerException>("didn't catch null ProcessingInstruction") {
            val pi: ProcessingInstruction? = null
            element.addContent(nullT<Content>())
        }
    }

    /**
     * Test that string based content is added correctly to an Element.
     */
    @Test fun test_TCM__OrgJdomElement_addContent_String() {
        val element = Element("element")
        element.addContent("the first part")
        assertEquals("the first part", element.text, "incorrect string value")
        element.addContent("the second part")
        assertEquals("the first partthe second part", element.text, "incorrect string value")
        //make sure it still works with mixed content
        element.addContent(Element("child"))
        element.addContent("the third part")
        assertEquals("the first partthe second partthe third part", element.text, "incorrect string value")

        // RolfL: I'v tried not to mess with the 'original' JUnit tests, but the
        // following comment does not match the code, or the tests.
        // vvvvvvvvvv
        //test that add content null clears the content
        // ^^^^^^^^^^
        // Adding a null String appears to have always had the effect of adding
        // a new 'empty' Text content...: new Text("")
        // I am updating this test to match the current reality.
        try {
        	// we expect addContent(null) to add a new Empty Text element.
        	// thus 'last' will be accurate after the add.
            val last: Int = element.contentSize
            
            val data: String? = null
            element.addContent(data)
            
            // ensure there is a new content at 'last', and that it is an empty string.
            assertEquals("", element.content.get(last).value)
        }
        catch (e: IllegalAddException) {
        	fail("didn't handle null String content")
        }
        catch (e: NullPointerException) {
            fail("didn't handle null String content")
        }

    }

    /**
     * Test getContent by child name.
     */
    @Test fun test_TCM__OrgJdomElement_getChild_String() {
        val element = Element("element")
        val child = Element("child")
        val childNS = Element("child", Namespace.getNamespace("urn:foo"))
        element.addContent(child)
        element.addContent(childNS)
        assertEquals(element.getChild("child"), child, "incorrect child returned")
    }

    /**
     * Test getContent by child name and namespace.
     */
    @Test fun test_TCM__OrgJdomElement_getChild_String_OrgJdomNamespace() {
        val element = Element("element")
        val child = Element("child")
        val ns = Namespace.getNamespace("urn:foo")
        val childNS = Element("child", ns)
        element.addContent(child)
        element.addContent(childNS)
        assertEquals(element.getChild("child", ns), childNS, "incorrect child returned")
    }

    /**
     * Test getCopy with only the name argument. Since the copy is just a clone,
     * the clone test covers most of the testing.
     */
    @Test fun test_TCM__OrgJdomElement_getCopy_String() {
        /** No op because the method is deprecated

         Element element = new Element("element", Namespace.getNamespace("urn:foo"));
         element.addContent("text");

         Element newElement = element.getCopy("new");
         assertEquals("incorrect name", "new", newElement.getName());
         assertEquals("incorrect namespace", Namespace.NO_NAMESPACE, newElement.getNamespace());
         assertEquals("incorrect content", "text", newElement.getText());
         */
    }

    /**
     * Test getCopy with the name and namespace arguments.
     * Since the copy is just a clone, the clone test
     * covers most of the testing.
     */
    @Test fun test_TCM__OrgJdomElement_getCopy_String_OrgJdomNamespace() {
        /** No op because the method is deprecated

         Element element = new Element("element", Namespace.getNamespace("urn:foo"));
         element.addContent("text");
         Namespace ns = Namespace.getNamespace("urn:test:new");
         Element newElement = element.getCopy("new", ns);
         assertEquals("incorrect name", "new", newElement.getName());
         assertEquals("incorrect namespace", ns, newElement.getNamespace());
         assertEquals("incorrect content", "text", newElement.getText());
         */
    }

    /**
     * Test test that a child element can return its parent.
     */
    @Test fun test_TCM__OrgJdomElement_getParent() {
        val element = Element("el")
        val child = Element("child")
        element.addContent(child)

        assertEquals(element, child.parent, "parent not found")
    }

    /**
     * Test addAttribute with an Attribute
     */
    @Test fun test_TCM__OrgJdomElement_setAttribute_OrgJdomAttribute() {
        val element = Element("el")
        val att = Attribute("name", "first")
        element.setAttribute(att)
        assertEquals(element.getAttribute("name"), att, "incorrect Attribute returned")

        //update the value
        val att2 = Attribute("name", "replacefirst")
        element.setAttribute(att2)
        assertEquals("replacefirst", element.getAttribute("name")!!.value, "incorrect Attribute value returned")

        //test with bad data
        assertFailsWith<NullPointerException>("didn't catch null attribute") {
            element.setAttribute(nullT())
        }
        
        assertFailsWith<IllegalAddException>("Should not be able to add already added Attribute.") {
        	val emt = Element("dummy")
        	val ma = Attribute("hi", "there")
        	emt.setAttribute(ma)
        	element.setAttribute(ma)
        }
    }

    /**
     * Test addAttribute with a supplied name and value
     */
    @Test fun test_TCM__OrgJdomElement_setAttribute_String_String__invalidCharacters() {
        val element = Element("el")

		//try with null name
        assertFailsWith<NullPointerException>("didn't catch null attribute name") { 
            element.setAttribute(null, "value")
        }

		//try with null value
        assertFailsWith<NullPointerException>("didn't catch null attribute value") {
            element.setAttribute("name2", null)
        }

        //try with bad characters
        assertFailsWith<IllegalArgumentException>("didn't catch bad characters in attribute name") {
            element.setAttribute("\n", "value")
        }

        assertFailsWith<IllegalArgumentException>("didn't catch bad characters  in attribute value") {
            element.setAttribute("name2", "" + 0x01.toChar())
        }
    }

    /**
     * Test addAttribute with a supplied name and value
     */
    @Test fun test_setAttribute_String_String__attributeTypes() {
    	helper_setAttribute_String_String__attributeType(Attribute.UNDECLARED_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.CDATA_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.ID_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.IDREF_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.IDREFS_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.ENTITY_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.ENTITIES_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.NMTOKEN_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.NMTOKENS_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.NOTATION_TYPE)    
    	helper_setAttribute_String_String__attributeType(Attribute.ENUMERATED_TYPE)    
    }

    /**
     * Test setAttribute with a supplied name and value
     */
    private fun helper_setAttribute_String_String__attributeType(attributeType: AttributeType) {
        val element = Element("el")

        val attributeName = "name"
		val attributeValue = "value"
        val attributeNewValue = "newValue"

		val attribute = Attribute(attributeName, attributeValue, attributeType)

		// add attribute to element
		element.setAttribute(attribute)

		val foundAttribute = element.getAttribute(attributeName)
        assertNotNull(foundAttribute, "no Attribute found")
        
        assertEquals(attributeName, foundAttribute.name, "incorrect Attribute name returned")
        assertEquals(attributeValue, foundAttribute.value, "incorrect Attribute value returned")
        assertEquals(attributeType, foundAttribute.attributeType, "incorrect Attribute type returned")

		//update the value
        element.setAttribute(attributeName, attributeNewValue)

		val changedAttribute = element.getAttribute(attributeName)
        assertNotNull(changedAttribute, "no Attribute found")
        
        assertEquals(attributeName, changedAttribute.name, "incorrect Attribute name returned")
        assertEquals(attributeNewValue, changedAttribute.value, "incorrect Attribute value returned")
        assertEquals(attributeType, changedAttribute.attributeType, "incorrect Attribute type returned")
    }

    /**
     * Test addAttribute with a supplied name and value
     */
    @Test fun test_setAttribute_String_String_String__attributeTypes() {
        helper_setAttribute_String_String_String__attributeType(Attribute.UNDECLARED_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.CDATA_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.ID_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.IDREF_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.IDREFS_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.ENTITY_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.ENTITIES_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.NMTOKEN_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.NMTOKENS_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.NOTATION_TYPE)    
        helper_setAttribute_String_String_String__attributeType(Attribute.ENUMERATED_TYPE)    
    }

    /**
     * Test setAttribute with a supplied name and value
     * 
     * @author Victor Toni
     */
    private fun helper_setAttribute_String_String_String__attributeType(attributeType: AttributeType) {
        assertFailsWith<IllegalNameException>("didn't catch empty prefix for attribute ") {
            val defaultNamespace = Namespace.getNamespace(null, "http://test.org/default")
            helper_setAttribute_String_String_String__attributeType(defaultNamespace, attributeType)
        }

        val testNamespace = Namespace.getNamespace("test", "http://test.org/test")
        helper_setAttribute_String_String_String__attributeType(testNamespace, attributeType)
    }

    /**
     * Test setAttribute with a supplied name, namespace and value
     * 
     * @author Victor Toni
     */
    private fun helper_setAttribute_String_String_String__attributeType(namespace: Namespace, attributeType: AttributeType) {
        val element = Element("el")

        val attributeName = "name"
        val attributeValue = "value"
        val attributeNewValue = "newValue"

        val attribute = Attribute(attributeName, attributeValue, attributeType, namespace)

        // add attribute to element
        element.setAttribute(attribute)

        val foundAttribute = element.getAttribute(attributeName, namespace)
        assertNotNull(foundAttribute, "no Attribute found")
        
        assertEquals(attributeName, foundAttribute.name, "incorrect Attribute name returned")
        assertEquals(attributeValue, foundAttribute.value, "incorrect Attribute value returned")
        assertEquals(attributeType, foundAttribute.attributeType, "incorrect Attribute type returned")

        //update the value
        element.setAttribute(attributeName, attributeNewValue, namespace)

        val changedAttribute = element.getAttribute(attributeName, namespace)
        assertNotNull(changedAttribute, "no Attribute found")
        
        assertEquals(attributeName, changedAttribute.name, "incorrect Attribute name returned")
        assertEquals(attributeNewValue, changedAttribute.value, "incorrect Attribute value returned")
        assertEquals(attributeType, changedAttribute.attributeType, "incorrect Attribute type returned")
    }

    /**
     * Test that attributes are added correctly as a list
     */
    @Test fun test_TCM__OrgJdomElement_setAttributes_List() {
        val element = Element("element")
        val one = Attribute("one", "value")
        val two = Attribute("two", "value")
        val three = Attribute("three", "value")
        val list = ArrayList<Attribute>()
        list.add(one)
        list.add(two)
        list.add(three)

        //put in an attribute that will get blown away later
        element.setAttribute(Attribute("type", "test"))
        element.setAttributes(list)
        assertEquals(one, element.getAttribute("one"), "attribute not found")
        assertEquals(two, element.getAttribute("two"), "attribute not found")
        assertEquals(three, element.getAttribute("three"), "attribute not found")
        assertNull(element.getAttribute("type"), "attribute should not have been found")


        //now try to add something that isn't an attribute which should still add those
        //attributes added before the mistake.
//        Element bogus = new Element("bogus");
//        Attribute four = new Attribute("four", "value");
//
//        ArrayList<Object> newList = new ArrayList<Object>();
//        newList.add(four);
//        newList.add(bogus);
//        try {
//            element.setAttributes(newList);
//            fail("didn't catch bad data in list");
//        }
//        catch (ClassCastException e) {
//        }
        //should be an atomic operation so the original state should be preserved
        assertEquals(3, element.attributes.size, "wrong number of attributes after failed add")
        assertEquals(one, element.getAttribute("one"), "attribute not found")
        assertEquals(two, element.getAttribute("two"), "attribute not found")
        assertEquals(three, element.getAttribute("three"), "attribute not found")

        try {
            element.setAttributes(null)
            val attList = element.attributes
            assertTrue(attList.size == 0, "null didn't clear attributes")
        }
        catch (e: IllegalArgumentException) {
            fail("didn't handle null String content")
        }
        catch (e: NullPointerException) {
            fail("didn't handle null String content")
        }
    }

    /**
     * Test setting child elements in a list.
     */
/*
    public void test_TCM__OrgJdomElement_setChildren_List() {
        Element element = new Element("el");
        assertEquals("did not return emptyList<Any?>() on empty element", emptyList<Any?>(), element.getChildren("child"));
        Element child1 = new Element("child");
        child1.setAttribute(new Attribute("name", "first"));

        Element child2 = new Element("child");
        child2.setAttribute(new Attribute("name", "second"));
        Element child3 = new Element("child", Namespace.getNamespace("ftp://wombat.stew"));

        ArrayList list = new ArrayList();
        list.add(child1);
        list.add(child2);
        list.add(child3);

        element.setChildren(list);

        //should only get the child elements in all namespaces
        List contentList = element.getChildren();
        assertEquals("incorrect number of children returned", 3, contentList.size());
        assertEquals("incorrect child returned", contentList.get(0), child1);
        assertEquals("incorrect child returned", contentList.get(1), child2);
        assertEquals("incorrect child returned", contentList.get(2), child3);

        ArrayList newList = new ArrayList();
        newList.add(child1);
        newList.add(new Attribute("name", "bogus"));

        try {
            element.setChildren(newList);
            fail("didn't catch a bad object in list");
        }
        catch (IllegalArgumentException e) {
        }
        //should be an atomic operation
        contentList = element.getChildren();
        assertEquals("wrong number of children after failed add", 3, contentList.size());
        assertEquals("incorrect child returned after failed add", contentList.get(0), child1);
        assertEquals("incorrect child returned after failed add", contentList.get(1), child2);
        assertEquals("incorrect child returned after failed add", contentList.get(2), child3);


        //nulls should reset the list
        element.setContent(null);
        assertTrue("didn't reset children List", element.getContent().isEmpty());
    }
*/

    /**
     * Test adding mixed content in a List
     */
    @Test fun test_TCM__OrgJdomElement_setContent_List() {
        val element = Element("el")
        assertEquals(emptyList<Any?>(), element.content, "did not return Collections.EMPTY_LIST on empty element")
        val ns = Namespace.getNamespace("urn:hogwarts")
        val child1 = Element("child", ns)

        val child2 = Element("child", ns)
        val child3 = Element("child", Namespace.getNamespace("ftp://wombat.stew"))

        val list = ArrayList<Content>()
        list.add(child1)
        list.add(child2)
        list.add(child3)

        val comment = Comment("hi")
        list.add(comment)
        val cdata = CDATA("gotcha")
        list.add(cdata)
        val pi = ProcessingInstruction("tester", "do=something")
        list.add(pi)
        val entity = EntityRef("wizards")
        list.add(entity)
        val text = Text("finally a new wand!")
        list.add(text)

        element.setContent(list)
        var contentList = element.content

        assertEquals(8, contentList.size, "incorrect number of content items")
        assertEquals(child1, contentList.get(0), "wrong child element")
        assertEquals(child2, contentList.get(1), "wrong child element")
        assertEquals(child3, contentList.get(2), "wrong child element")
        assertEquals(comment, contentList.get(3), "wrong comment")
        assertEquals(cdata, contentList.get(4), "wrong CDATA")
        assertEquals(pi, contentList.get(5), "wrong ProcessingInstruction")
        assertEquals(entity, contentList.get(6), "wrong EntityRef")
        assertEquals(text, contentList.get(7), "wrong text")

        var newList = ArrayList<Content>()
        //test adding a bad object type in the list
        newList.add(child1)
        addBrokenContent(newList, 7)

        assertFailsWith<ClassCastException> {
        	// this is a sensitive test.
        	// The first member in the newList is already in the element, so,
        	// normally adding it would cause anissue with the child already
        	// having a parent, and thus an IllegalAddException, so, we could
        	// expect that, but, setContent first detaches the previous content
        	// so, in this case, the child element first gets detached, and then
        	// re-added... so, the effect is that we get a ClassCastException
        	// by adding the Integer...
            element.setContent(newList)
        }

        //should add no content... if setContent fails it should restore previous
        // content.
        contentList = element.content
        assertEquals(child1, contentList.get(0), "wrong child element after failed add")
        assertEquals(child2, contentList.get(1), "wrong child element after failed add")
        assertEquals(child3, contentList.get(2), "wrong child element after failed add")
        assertEquals(comment, contentList.get(3), "wrong comment after failed add")
        assertEquals(cdata, contentList.get(4), "wrong CDATA after failed add")
        assertEquals(pi, contentList.get(5), "wrong ProcessingInstruction after failed add")
        assertEquals(entity, contentList.get(6), "wrong EntityRef after failed add")
        assertEquals(text, contentList.get(7), "wrong text after failed add")


        //nulls should reset the list
        element.removeContent()
        assertTrue(element.content.isEmpty(), "didn't reset mixed content List")
        assertNull(child1.parent)
        assertNull(child2.parent)
        assertNull(child3.parent)
        assertNull(comment.parent)
        assertNull(cdata.parent)
        assertNull(pi.parent)
        assertNull(entity.parent)
        assertNull(text.parent)
        
        // test an empty list clears....
        newList.clear()
        assertTrue(element === element.setContent(newList))
        assertTrue(element.content.isEmpty())
        assertTrue(element === element.setContent(list))
        assertTrue(element.contentSize == list.size)
        // test a null list clears.
        newList = nullT()
        assertTrue(element === element.setContent(newList))
        assertTrue(element.content.isEmpty())
        assertTrue(element === element.setContent(list))
        assertTrue(element.contentSize == list.size)
        
        // get a 1-sized list.
        for (i in list.size - 1 downTo 1) {
        	list.removeAt(i)
        }
        assertTrue(element === element.setContent(list))
        assertTrue(element.contentSize == 1)
    }

    /**
     * Test setting the content of the Element with just a string
     * This should wipe out all other content the element has
     */
    @Test fun test_TCM__OrgJdomElement_setText_String() {
        val element = Element("el")
        val child = Element("child")
        element.addContent(child)

        element.setText("it's all gone")
        assertEquals("it's all gone", element.text, "incorrect text returned")
        assertEquals(1, element.content.size, "incorrect number of content items found")


        element.setText(null)
        assertTrue(element.content.isEmpty(), "didn't clear content with null text")

        //bad string test

        /** skip this test unless we determine that it is required
         to check textual content for validity.  We have the means to do
         it in the validator but don't have it turned on right now for
         performance reasons where the parser has already checked this
         in the majority of cases.


         try {
         element.setText("test" + (char)0x01);
         fail("didn't catch text with invalid character");
         } catch (IllegalArgumentException e) {
         } catch (NullPointerException e) {
         fail("NullPointerException");
         }

         */
    }

    /**
     * Test that the Element returns its primary namespace
     */
    @Test fun test_TCM__OrgJdomNamespace_getNamespace() {
        val ns = Namespace.getNamespace("urn:test:foo")
        val element = Element("element", ns)

        assertEquals(ns, element.namespace, "wrong namespace returned")
    }

    /**
     * Test that Element can return a namespace given a uri
     */
    @Test fun test_TCM__OrgJdomNamespace_getNamespace_String() {
        val ns = Namespace.getNamespace("x", "urn:test:foo")
        val element = Element("element", ns)

        assertEquals(ns, element.getNamespace("x"), "wrong namespace returned")
        assertNull(element.getNamespace("bogus"), "no namespace should have been found")

        //now make sure it can return the namespace from the additional namespaces
        val newNs = Namespace.getNamespace("y", "urn:test:new")
        element.addNamespaceDeclaration(newNs)
        assertEquals(newNs, element.getNamespace("y"), "wrong namespace returned")

        //now make sure the same thing works from a child
        val child = Element("child")
        element.addContent(child)

        assertEquals(ns, child.getNamespace("x"), "wrong namespace returned")
        assertNull(child.getNamespace("bogus"), "no namespace should have been found")
        assertEquals(newNs, child.getNamespace("y"), "wrong namespace returned")
        
        //Now make sure we can pick up namespaces on attributes too that are not
        //explicitly added with addAdditionalNamespace()
        assertNull(child.getNamespace("attns"))
        child.setAttribute("att", "value", Namespace.getNamespace("attns", "atturi"))
        assertEquals("atturi", child.getNamespace("attns")!!.uri)
        child.setAttribute("att", "value", Namespace.getNamespace("attnt", "atturt"))
        assertEquals("atturt", child.getNamespace("attnt")!!.uri)

        assertNull(element.getNamespace("attns"))
}

    /**
     * Test getAttributeValue by attribute name.
     */
    @Test fun test_TCM__String_getAttributeValue_String() {
        val element = Element("el")
        assertEquals(element.getAttributeValue("name"), null, "incorrect value returned")
        element.setAttribute(Attribute("name", "first"))
        assertEquals(element.getAttributeValue("name"), "first", "incorrect value returned")
    }

    /**
     * Test getAttributeValue by attribute name.
     */
    @Test fun test_TCM__String_getAttributeValue_String_String() {
        val element = Element("el")
        assertEquals(element.getAttributeValue("name", ""), "", "incorrect value returned")
        element.setAttribute(Attribute("name", "first"))
        assertEquals(element.getAttributeValue("name", ""), "first", "incorrect value returned")
        assertEquals(element.getAttributeValue("namex", ""), "", "incorrect value returned")
    }

    /**
     * Test getAttributeValue with name and namespace
     */
    @Test fun test_TCM__String_getAttributeValue_String_OrgJdomNamespace() {
        val element = Element("el")
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("x", "urn:WombatsRUS")), null, "incorrect value returned")
        element.setAttribute(Attribute("name", "first", Namespace.getNamespace("x", "urn:WombatsRUS")))
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("x", "urn:WombatsRUS")), "first", "incorrect value returned")
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("y", "urn:WombatsRUS2")), null, "incorrect value returned")
    }

    /**
     * Test getAttributeValue with name and namespace
     */
    @Test fun test_TCM__String_getAttributeValue_String_OrgJdomNamespace_String() {
        val element = Element("el")
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("x", "urn:WombatsRUS"), ""), "", "incorrect value returned")
        element.setAttribute(Attribute("name", "first", Namespace.getNamespace("x", "urn:WombatsRUS")))
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("x", "urn:WombatsRUS"), ""), "first", "incorrect value returned")
        assertEquals(element.getAttributeValue("name", Namespace.getNamespace("y", "urn:WombatsRUS2"), "x"), "x", "incorrect value returned")
    }

    /**
     * Test the convenience method for retrieving child text.
     */
    @Test fun test_TCM__String_getChildText_String() {
        val element = Element("element")
        val child = Element("child")
        child.addContent("  some text \nboo  ")
        element.addContent(child)

        assertEquals("  some text \nboo  ", element.getChildText("child"), "incorrect text returned")
    }

    /**
     * Test the convenience method for retrieving child text for a child
     * retrieved by name and namespace
     */
    @Test fun test_TCM__String_getChildText_String_OrgJdomNamespace() {
        val element = Element("element")
        val ns = Namespace.getNamespace("urn:test:foo")
        val child = Element("child", ns)
        child.addContent("  some text \nboo  ")
        element.addContent(child)

        assertEquals("  some text \nboo  ", element.getChildText("child", ns), "incorrect text returned")

    }

    /**
     * Test the convenience method for retrieving trimmed child text.
     */
    @Test fun test_TCM__String_getChildTextTrim_String() {
        val element = Element("element")
        val child = Element("child")
        child.addContent("  some text  \n ")
        element.addContent(child)

        assertEquals("some text", element.getChildTextTrim("child"), "incorrect text returned")
    }

    /**
     * Test the convenience method for retrieving trimmed child text for the
     * child in the given namespace
     */
    @Test fun test_TCM__String_getChildTextTrim_String_OrgJdomNamespace() {
        val element = Element("element")
        val ns = Namespace.getNamespace("urn:test:foo")
        val child = Element("child", ns)
        child.addContent("  some text  \n ")
        element.addContent(child)

        assertEquals("some text", element.getChildTextTrim("child", ns), "incorrect text returned")
    }

    /**
     * Test getName.
     */
    @Test fun test_TCM__String_getName() {
        val element = Element("element", Namespace.getNamespace("x", "ftp://wombat.stew"))
        assertEquals(element.name, "element", "incorrect name")
    }

    /**
     * Test getNamespacePrefix.
     */
    @Test fun test_TCM__String_getNamespacePrefix() {
        val element = Element("element", Namespace.getNamespace("x", "ftp://wombat.stew"))
        assertEquals(element.namespacePrefix, "x", "incorrect namespace prefix")
    }

    /**
     * Test code goes here. Replace this comment.
     */
    @Test fun test_TCM__String_getNamespaceURI() {
        val element = Element("element", Namespace.getNamespace("x", "ftp://wombat.stew"))
        assertEquals(element.namespaceURI, "ftp://wombat.stew", "incorrect uri")
    }

    /**
     * Test that Element returns the correct qualified name.
     */
    @Test fun test_TCM__String_getQualifiedName() {
        val element = Element("element", Namespace.getNamespace("x", "ftp://wombat.stew"))
        assertEquals(element.qualifiedName, "x:element", "incorrect qualified name")
    }

    /**
     * Test getSerializedForm
     */
    @Test fun test_TCM__String_getSerializedForm() {
//        fail("method not implemented");
    }

    /**
     * Test getText returns that full text of the element
     */
    @Test fun test_TCM__String_getText() {
        val element = Element("element")
        element.addContent("  some text \nboo  ")

        assertEquals("  some text \nboo  ", element.text, "incorrect text returned")
    }

    /**
     * Test getTextTrim.
     */
    @Test fun test_TCM__String_getTextTrim() {
        val element = Element("element")
        element.addContent("  some text  \n ")

        assertEquals("some text", element.textTrim, "incorrect text returned")
    }

    /**
     * Test the toString method which should return a useful string
     * for debugging purposes only
     */
    @Test fun test_TCM__String_toString() {
        val element = Element("element", Namespace.getNamespace("urn:foo"))
        element.setAttribute(Attribute("name", "aName"))

        assertEquals("[Element: <element [Namespace: urn:foo]/>]", element.toString(), "wrong toString text found")
    }

    /**
     * Test addNamespaceDeclaration for prefix and uri.
     */
    @Test fun test_TCM__void_addNamespaceDeclaration_OrgJdomNamespace() {
        val element = Element("element")
        element.addNamespaceDeclaration(Namespace.getNamespace("x", "urn:foo"))
        val list = element.additionalNamespaces

        val ns = list.get(0)
        assertTrue(ns.uri.equals("urn:foo"), "didn't return added namespace")
        assertTrue(ns.prefix.equals("x"), "didn't return added namespace prefix")
    }

    /**
     * Test that attributes will be added and retrieved according
     * to specs. with namespaces and prefixes intact
     *
     */
    @Test fun test_TCU__testAttributeNamespaces() {

        //set up an element and namespaces to test with

        var el = Element("test")
        val one = Namespace.getNamespace("test", "http://foo")
        val two = Namespace.getNamespace("test2", "http://foo")

        var att = Attribute("first", "first", one)
        var att2 = Attribute("first", "second", two)

        //should overwrite attributes with same names even if prefixes are different
        try {
            el.setAttribute(att)
            el.setAttribute(att2)
            assertTrue(el.getAttribute("first", one)!!.value.equals("second"), "didn't catch duplicate setAttribute with different prefixes")
            assertTrue(el.getAttribute("first", two)!!.namespace.equals(two), "didn't catch duplicate setAttribute with different prefixes")
        }
        catch (e: IllegalAddException) {
			// Do nothing
		} catch (e: Exception) {
			fail("Unexpected exception " + e::class)
        }

        //set up some unique namespaces for later tests
        val testDefault = Namespace.getNamespace("http://bar")
        val testNS = Namespace.getNamespace("test", "http://foo")
        val testNS2 = Namespace.getNamespace("test2", "http://foo2")

        //this should cause an error since the prefix will be discarded
        var testNS3: Namespace? = null
        assertFailsWith<IllegalNameException>("didn't catch bad \"\" uri name") {
            testNS3 = Namespace.getNamespace("test", "")
        }

        //show how you can have an empty namespace with the current scheme
        testNS3 = Namespace.getNamespace("")

        el = Element("test", testDefault)
        el.addNamespaceDeclaration(testNS2)
        try {

            el.addNamespaceDeclaration(testNS3)
        }
        catch (e: IllegalAddException) {
			// Do nothing
		} catch (e: Exception) {
			fail("Unexpected exception " + e::class)
        }

        att = Attribute("prefixNS", "test")

        //Test for default namespace check for attribute
        att2 = Attribute("prefixNS", "empty namespace")


        //test prefixes with same name but different namespaces
        val att3 = Attribute("prefixNS", "test", testNS)
        val att4 = Attribute("prefixNS", "test2", testNS2)
        el.setAttribute(att2)
        el.setAttribute(att3)
        el.setAttribute(att4)

        var attback = el.getAttribute("prefixNS")
        assertTrue(attback!!.namespaceURI.equals(""), "failed to get attribute from empty default namespace")
        attback = el.getAttribute("prefixNS", testNS3)
        assertTrue(attback!!.namespaceURI.equals(""), "failed to get attribute from http://bar namespace")
        attback = el.getAttribute("prefixNS", testNS)
        assertTrue(attback!!.namespaceURI.equals("http://foo"), "failed to get attribute from http://foo namespace")
        attback = el.getAttribute("prefixNS", testNS2)
        assertTrue(attback!!.namespaceURI.equals("http://foo2"), "failed to get attribute from http://foo2 namespace")
    }

    /**
     * Test that an Element properly handles default namespaces
     *
     */
    @Test fun test_TCU__testDefaultNamespaces() {

        //set up an element to test with
        var element = Element("element", Namespace.getNamespace("http://foo"))
        var child1 = Element("child1", Namespace.getNamespace("http://foo"))
        var child2 = Element("child2", Namespace.getNamespace("http://foo"))

        element.addContent(child1)
        element.addContent(child2)

        //here is what we expect in these two scenarios
        val bufWithNoNS = "<element xmlns=\"http://foo\"><child1 /><child2 /></element>"

        val bufWithEmptyNS = "<element xmlns=\"http://foo\"><child1 xmlns=\"\" /><child2 xmlns=\"\" /></element>"

        var sw = StringBuilder()
        var op = XMLOutputter()
        op.output(element, sw)
        assertEquals(bufWithNoNS, sw.toString(), "Incorrect output for NO_NAMESPACE in a default namespace")

        //new try setting a new empty default namespace for children
        element = Element("element", Namespace.getNamespace("http://foo"))
        child1 = Element("child1")
        child2 = Element("child2")

        element.addContent(child1)
        element.addContent(child2)
        sw = StringBuilder()
        op = XMLOutputter()
        op.output(element, sw)
        assertTrue(sw.toString().equals(bufWithEmptyNS), "Incorrect output for empty default namespace")


        //this code tests where multiple default namespaces disallowed
        assertFailsWith<IllegalAddException>("didn't catch multiple default namespaces added to an element") {
            val el = Element("test")
            el.addNamespaceDeclaration(Namespace.getNamespace("", "foo:bar"))
            el.addNamespaceDeclaration(Namespace.getNamespace("", "foo2:bar"))
            el.addNamespaceDeclaration(Namespace.NO_NAMESPACE)
        }
    }


//    @Test
//    public void test_AddingString() {
//    	Vector<Object> v = new Vector<Object>();
//    	v.add("one");
//    	Element e = new Element("e");
//    	try {
//    		e.setContent(v);
//    		fail("Should not be avle to add String content");
//    	} catch (ClassCastException cce) {
//    		// good.
//    	} catch (Exception ex) {
//    		fail("Expected ClassCastException, not " + ex.getClass());
//    	}
//    }
    
	@Test fun testElementAddDocType() {
		try {
			val list = (Element("tag").content)
			list.add(DocType("elementname"))
			fail ("Should not be able to add DocType to an Element")
		} catch (iae: IllegalAddException) {
			// good!
		} catch (e: Exception) {
			fail ("We expect an IllegalAddException, but got " + e::class.qualifiedName)
		}
	}
	
//	@Test
//	public void testElementAddAttribute() {
//		try {
//			List<Content> list = (new Element("tag").getContent());
//			list.add(new Attribute("att", "value"));
//			fail ("Should not be able to add Attribute to an Element's ContentList");
//		} catch (IllegalAddException iae) {
//			// good!
//		} catch (Exception e) {
//			fail ("We expect an IllegalAddException, but got " + e.getClass().getName());
//		}
//	}
	
	@Test fun testGetNamespace() {
		val emt = Element("mine")
		val nsa = Namespace.getNamespace("tstada", "uri")
		emt.setAttribute("name", "value", nsa)
		assertTrue(Namespace.NO_NAMESPACE == emt.namespace)
		assertTrue(emt.getNamespace(null) == null)
		assertTrue(emt.getNamespace("none") == null)
		assertTrue(emt.getNamespace("xml") === Namespace.XML_NAMESPACE)
		assertTrue(emt.getNamespace("tstada") === nsa)
	}
	
	@Test fun testSetNamespaceAdditional() {
		val nsa = Namespace.getNamespace("pfx","URIA")
		val nsb = Namespace.getNamespace("pfx","URIB")
		val nsc = Namespace.getNamespace("pfx","URIC")

		val emt = Element("emt", nsa)
		try {
			assertFailsWith<IllegalAddException>("Expected Namespace Collision Exception.") {
			// cannot add a second namespace with the same prefix as the Element.
			emt.addNamespaceDeclaration(nsb)
		}
		} catch (e: Exception) {
			e.printStackTrace()
			fail("Expected IllegalAddException, but got " + e::class)
		}

		// and we can change the Element's Namespace to nsc
		emt.setNamespace(nsc)
		// and back again.
		emt.setNamespace(nsa)
		
		// further we can add nsa as an additional namespace because it is the
		// same as the Element's namespace.
		assertTrue(emt.addNamespaceDeclaration(nsa))
		
		try {
			assertFailsWith<IllegalAddException>("Expected Namespace Collision Exception.") {
			// but, now we can't change the Element's Namespace.
			emt.setNamespace(nsb)
		}
		} catch (e: Exception) {
			e.printStackTrace()
			fail("Expected IllegalAddException, but got " + e::class)
		}

		// false because it's already added.
		assertFalse(emt.addNamespaceDeclaration(nsa))
	}
	
	@Test fun testSetNamespaceAttribute() {
		val nsa = Namespace.getNamespace("pfx","URIA")
		val nsb = Namespace.getNamespace("pfx","URIB")
		val nsc = Namespace.getNamespace("pfx","URIC")
		val nsd = Namespace.getNamespace("pfy","URID")

		val emt = Element("emt", nsa)
		try {
			assertFailsWith<IllegalAddException>("Expected Namespace Collision Exception.") {
			// cannot add a second namespace with the same prefix as the Element.
			emt.setAttribute("att", "val", nsb)
		}
		} catch (e: Exception) {
			e.printStackTrace()
			fail("Expected IllegalAddException, but got " + e::class)
		}

		// and we can change the Element's Namespace to nsc
		emt.setNamespace(nsc)
		assertTrue(nsc === emt.namespace)
		// and back again.
		emt.setNamespace(nsa)
		assertTrue(nsa === emt.namespace)
		
		// further we can add nsa as an additional namespace because it is the
		// same as the Element's namespace.
		assertTrue(emt === emt.setAttribute("att", "value", nsa))
		
		try {
			assertFailsWith<IllegalAddException>("Expected Namespace Collision Exception.") {
			// but, now we can't change the Element's Namespace.
			emt.setNamespace(nsb)
		}
		} catch (e: Exception) {
			e.printStackTrace()
			fail("Expected IllegalAddException, but got " + e::class)
		}
		
		// but we can change it to something with a different prefix:
		emt.setNamespace(nsd)
		assertTrue(nsd === emt.namespace)
		
		assertTrue(emt.setAttribute("tst", "val1", null) === emt)
		assertEquals("val1", emt.getAttributeValue("tst"))
		assertEquals("val1", emt.getAttributeValue("tst", (null as Namespace?)))
		assertEquals("val1", emt.getAttributeValue("tst", Namespace.NO_NAMESPACE))
		assertTrue(emt.setAttribute("tst", "val2", Namespace.NO_NAMESPACE) === emt)
		assertEquals("val2", emt.getAttributeValue("tst"))
		assertEquals("val2", emt.getAttributeValue("tst", (null as Namespace?)))
		assertEquals("val2", emt.getAttributeValue("tst", Namespace.NO_NAMESPACE))
		

	}
	
	@Test fun testRemoveNamespace() {
		val emt = Element("mine")
		val myuri = Namespace.getNamespace("pfx", "myuri")
		assertTrue(emt.getNamespace("pfx") == null)
		emt.removeNamespaceDeclaration(myuri)
		emt.addNamespaceDeclaration(myuri)
		assertTrue(emt.getNamespace("pfx") === myuri)
		emt.removeNamespaceDeclaration(myuri)
		assertTrue(emt.getNamespace("pfx") == null)
	}

	@Test fun testGetValue() {
		val emt = Element("root")
		emt.addContent("See ")
		assertTrue("See ".equals(emt.value))
		emt.addContent(Element("k1").addContent("Spot "))
		assertTrue("See Spot ".equals(emt.value))
		emt.addContent(Comment("skip "))
		assertTrue("See Spot ".equals(emt.value))
		emt.addContent(CDATA("run!"))
		assertTrue("See Spot run!".equals(emt.value))
	}

	@Test fun testGetText() {
		val emt = Element("root")
		assertTrue("".equals(emt.text))
		emt.addContent(Comment("skip "))
		assertTrue("".equals(emt.text))
		emt.addContent(ProcessingInstruction("dummy", "nodata"))
		assertTrue("".equals(emt.text))
		emt.removeContent()
		assertTrue("".equals(emt.text))
		emt.addContent(" See   ")
		assertTrue(" See   ".equals(emt.text))
		emt.addContent(Element("k1").addContent(" Spot  the \n dog  "))
		assertTrue(" See   ".equals(emt.text))
		emt.addContent(CDATA(" run!   "))
		assertTrue(" See    run!   ".equals(emt.text))
		assertTrue("See    run!".equals(emt.textTrim))
		assertTrue("See run!".equals(emt.textNormalize))
		
		assertTrue(" Spot  the \n dog  ".equals(emt.getChildText("k1")))
		assertTrue("Spot  the \n dog".equals(emt.getChildTextTrim("k1")))
		assertTrue("Spot the dog".equals(emt.getChildTextNormalize("k1")))
		
		assertTrue(" Spot  the \n dog  ".equals(emt.getChildText("k1", Namespace.NO_NAMESPACE)))
		assertTrue("Spot  the \n dog".equals(emt.getChildTextTrim("k1", Namespace.NO_NAMESPACE)))
		assertTrue("Spot the dog".equals(emt.getChildTextNormalize("k1", Namespace.NO_NAMESPACE)))

		assertTrue(null == emt.getChildText("x1"))
		assertTrue(null == emt.getChildTextTrim("x1"))
		assertTrue(null == emt.getChildTextNormalize("x1"))
		
		assertTrue(null == emt.getChildText("x1", Namespace.NO_NAMESPACE))
		assertTrue(null == emt.getChildTextTrim("x1", Namespace.NO_NAMESPACE))
		assertTrue(null == emt.getChildTextNormalize("x1", Namespace.NO_NAMESPACE))

		val xx = Namespace.getNamespace("nouri")
		assertTrue(null == emt.getChildText("k1", xx))
		assertTrue(null == emt.getChildTextTrim("k1", xx))
		assertTrue(null == emt.getChildTextNormalize("k1", xx))
	}

	@Test fun testElementContent() {
		val doc = Document()
		val root = Element("root")
		assertTrue(root.contentSize == 0)
		assertFalse(root.isRootElement)
		doc.addContent(root)
		assertTrue(root.isRootElement)
		
		assertTrue(root.cloneContent().size == 0)
		
		val text = Text("text")
		val comment1 = Comment("comment1")
		val comment2 = Comment("comment2")
		val child = Element("child")
		
		root.addContent(text)
		assertTrue(root.contentSize == 1)
		assertTrue(root.text.equals(text.text))
		assertTrue(root.cloneContent().size == 1)
		assertTrue(root.cloneContent().get(0) is Text)
		assertTrue(root.indexOf(text) == 0)
		
		assertTrue(text.parent === root)
		assertTrue(root.removeContent().get(0) === text)
		
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 0)
		assertTrue(root.cloneContent().size == 0)
		
		root.addContent(comment1)
		root.addContent(comment2)
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 2)
		root.setContent(1, text)
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent == null)
		assertTrue(text.parent === root)
		assertTrue(root.contentSize == 2)
		
		root.setContent(comment2)
		assertTrue(comment1.parent == null)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 1)
		assertTrue(comment2 === root.removeContent(0))
		
		root.addContent(setOf(comment2))
		assertTrue(comment1.parent == null)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 1)
		
		root.addContent(0, setOf(comment1))
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 2)
		
		root.addContent(2, setOf(text))
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent === root)
		assertTrue(root.contentSize == 3)
		assertTrue(root.indexOf(comment1) == 0)
		assertTrue(root.indexOf(comment2) == 1)
		assertTrue(root.indexOf(text) == 2)
		
		root.setContent(2, setOf(text))
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent === root)
		assertTrue(root.contentSize == 3)
		assertTrue(root.indexOf(comment1) == 0)
		assertTrue(root.indexOf(comment2) == 1)
		assertTrue(root.indexOf(text) == 2)
		
		val empty = emptySet<Content>()
		root.setContent(2, empty)
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent == null)
		assertTrue(root.contentSize == 2)
		assertTrue(root.indexOf(comment1) == 0)
		assertTrue(root.indexOf(comment2) == 1)
		assertTrue(root.indexOf(text) == -1)
		
		root.addContent(2, text)
		assertTrue(comment1.parent === root)
		assertTrue(comment2.parent === root)
		assertTrue(text.parent === root)
		assertTrue(root.contentSize == 3)
		assertTrue(root.indexOf(comment1) == 0)
		assertTrue(root.indexOf(comment2) == 1)
		assertTrue(root.indexOf(text) == 2)
		
		root.addContent(child)
		assertTrue(root.indexOf(child) == 3)
		assertTrue(root.contentSize == 4)
		assertTrue(child.parent === root)
		assertTrue(child.parentElement === root)
		assertTrue(root.content.size == 4)
		assertTrue(root.getContent(ElementFilter()).size == 1)
		assertTrue(root.getContent(ContentFilter(ContentFilter.COMMENT)).size == 2)
		assertFalse(root.removeChild("child", Namespace.XML_NAMESPACE))
		assertTrue(root.removeChild("child", Namespace.NO_NAMESPACE))

		assertTrue(root.contentSize == 3)
		assertTrue(root.getContent(ElementFilter()).isEmpty())
		
		assertTrue(root.removeContent(ContentFilter(ContentFilter.COMMENT)).size == 2)
		
		assertTrue(root.contentSize == 1)
		assertTrue(root === root.addContent(child))
		assertTrue(root.contentSize == 2)
		assertFalse(root.removeChildren("nothing"))
		assertFalse(root.removeChildren("child", Namespace.getNamespace("nothing")))
		assertTrue(root.removeChildren("child"))
		assertTrue(root.contentSize == 1)
		
		// some negative cases not yet covered....
		assertFailsWith<NullPointerException>("Should not be able to add null Element content.") {
			val n: Element? = null
			root.addContent(0, nullT<Content>())
		}
		
		assertFailsWith<IllegalAddException>("Should not be able to add ourself as Element content.") {
			// try to add ourself.
			child.addContent(0, child)
		}
		
		root.detach()
		root.addContent(child)
		
		assertFailsWith<IllegalAddException>("Should not be able to add circular Element content.") {
			// try to add ourself.
			child.addContent(0, root)
		}
	}
	
	@Test fun testAttributes() {
		val emt = Element("element")
		val myns = Namespace.getNamespace("pfxa", "attns")
		val mynsz = Namespace.getNamespace("pfxz", "attns")
		assertTrue(emt.attributes.isEmpty())
		assertTrue(emt.getAttribute("att") == null)
		assertTrue(emt.getAttributeValue("att") == null)
		assertTrue(emt.getAttributeValue("att", Namespace.NO_NAMESPACE) == null)
		assertTrue(emt.getAttributeValue("att", (null as Namespace?)) == null)
		assertTrue(emt.getAttributeValue("att", myns) == null)
		assertTrue("def".equals(emt.getAttributeValue("att", "def")))
		assertTrue("def".equals(emt.getAttributeValue("att", myns, "def")))
		assertTrue(emt.additionalNamespaces.isEmpty())
		
		emt.setAttribute(Attribute("att", "val"))
		assertFalse(emt.attributes.isEmpty())
		assertTrue(emt.getAttribute("att") != null)
		assertTrue("val".equals(emt.getAttributeValue("att")))
		assertTrue("val".equals(emt.getAttributeValue("att", Namespace.NO_NAMESPACE)))
		assertTrue("val".equals(emt.getAttributeValue("att", (null as Namespace?))))
		assertTrue(emt.getAttributeValue("att", myns) == null)
		assertTrue("val".equals(emt.getAttributeValue("att", "def")))
		assertTrue("val".equals(emt.getAttributeValue("att", Namespace.NO_NAMESPACE, "def")))
		assertTrue("val".equals(emt.getAttributeValue("att", null, "def")))
		assertTrue("def".equals(emt.getAttributeValue("att", myns, "def")))
		assertTrue(emt.additionalNamespaces.isEmpty())
		
		emt.setAttribute(Attribute("att", "nsval", myns))
		assertFalse(emt.attributes.isEmpty())
		assertTrue(emt.getAttribute("att") != null)
		assertTrue(emt.getAttribute("att", myns) != null)
		assertTrue(emt.getAttribute("att") !== emt.getAttribute("att", myns))
		assertTrue("val".equals(emt.getAttributeValue("att")))
		assertTrue("val".equals(emt.getAttributeValue("att", Namespace.NO_NAMESPACE)))
		assertTrue("val".equals(emt.getAttributeValue("att", (null as Namespace?))))
		assertTrue("nsval".equals(emt.getAttributeValue("att", myns)))
		assertTrue("nsval".equals(emt.getAttributeValue("att", mynsz)))
		assertTrue("val".equals(emt.getAttributeValue("att", "def")))
		assertTrue("nsval".equals(emt.getAttributeValue("att", myns, "def")))
		assertTrue("def".equals(emt.getAttributeValue("att", Namespace.XML_NAMESPACE, "def")))

		assertTrue(emt.additionalNamespaces.isEmpty())
		
		var att = emt.getAttribute("att")
		var na = Attribute("xx", "xval")
		assertFalse(emt.removeAttribute(na))
		assertTrue(emt.removeAttribute(att!!))
		assertTrue(att!!.parent == null)
		emt.setAttribute(att!!)
		assertTrue(att!!.parent === emt)
		assertTrue(emt.setAttribute("att", "nval") === emt)
		assertTrue(att!!.parent === emt)
		assertTrue("nval".equals(att!!.value))
		assertTrue("nval".equals(emt.getAttributeValue("att")))
		assertTrue(emt.setAttribute(Attribute("att", "zval")) === emt)
		assertTrue(att!!.parent == null)
		assertTrue("nval".equals(att!!.value))
		assertTrue("zval".equals(emt.getAttributeValue("att")))
		assertTrue(emt.setAttribute(att!!) === emt)
		assertTrue(att!!.parent === emt)
		

		att = emt.getAttribute("att", myns)
		na = Attribute("xx", "xval", myns)
		assertFalse(emt.removeAttribute(na))
		assertTrue(emt.removeAttribute(att!!))
		assertTrue(emt.setAttribute("att", "aval", myns) === emt)
		assertTrue("nsval".equals(att!!.value))
		assertTrue("aval".equals(emt.getAttributeValue("att", myns)))
		assertTrue(att!!.parent == null)
		emt.setAttribute(att!!)
		assertTrue(att!!.parent === emt)
		assertTrue(emt.setAttribute("att", "nval", myns) === emt)
		assertTrue(att!!.parent === emt)
		assertTrue("nval".equals(att!!.value))
		assertTrue("nval".equals(emt.getAttributeValue("att", myns)))
		assertTrue(emt.setAttribute(Attribute("att", "zval", myns)) === emt)
		assertTrue(att!!.parent == null)
		assertTrue("nval".equals(att!!.value))
		assertTrue("zval".equals(emt.getAttributeValue("att", myns)))
		assertTrue(emt.setAttribute(att!!) === emt)
		assertTrue(att!!.parent === emt)
		assertTrue("nval".equals(emt.getAttributeValue("att", myns)))
	
	}
	
	private fun addBrokenContent(cn: MutableCollection<*>, val_: Any?) {
		// this method is intentionally broken.
		val ocn = (cn as MutableCollection<Any?>)
		ocn.add(val_)
	}
	
    @Test fun testCloneDetatchParentElement() {
		val parent = Element("root")
		val content = Element("val")
		parent.addContent(content)
		val clone = content.detach().clone()
		assertEquals(content.value, clone.value)
		assertNull(content.parent)
		assertNull(clone.parent)
	}

    @Test fun testContentCType() {
    	assertTrue(Content.CType.Element === Element("root").cType)
    }

	private val alphaText: Comparator<Text> = object : Comparator<Text> {
		override fun compare(o1: Text, o2: Text): Int {
			return o1.text.compareTo(o2.text)
		}
	}

	private val alphaContent: Comparator<Content> = object : Comparator<Content> {
		override fun compare(o1: Content, o2: Content): Int {
			val ctd: Int = o1.cType.ordinal - o2.cType.ordinal
			if (ctd != 0) {
				return ctd
			}
			val ct = o1.cType
			when (ct) {
				Content.CType.Comment ->
					return ((o1 as Comment)).text.compareTo(((o2 as Comment)).text)
				Content.CType.CDATA ->
					return ((o1 as CDATA)).text.compareTo(((o2 as CDATA)).text)
				Content.CType.DocType ->
					return ((o1 as DocType)).elementName
							.compareTo(((o2 as DocType)).elementName)
				Content.CType.Element ->
					return ((o1 as Element)).name
							.compareTo(((o2 as Element)).name)
				Content.CType.ProcessingInstruction ->
					return ((o1 as ProcessingInstruction)).target
							.compareTo(((o2 as ProcessingInstruction)).target)
				Content.CType.EntityRef ->
					return ((o1 as EntityRef)).name
							.compareTo(((o2 as EntityRef)).name)
				Content.CType.Text ->
					return ((o1 as Text)).text.compareTo(((o2 as Text)).text)
			}
			return 0
		}
	}

	@Test fun testSort() {
		val emt = Element("root")
		emt.addContent(Text("d"))
		emt.addContent(Text("c"))
		emt.addContent(Text("b"))
		emt.addContent(Text("a"))
		assertEquals("dcba", emt.text)
		
		emt.sortContent(Filters.text(), alphaText)
		
		assertEquals("abcd", emt.text)
	}

	@Test fun testSortOnlyContent() {
		val emt = Element("root")
		emt.addContent(Text("a"))
		assertEquals("a", emt.text)
		emt.sortContent(alphaContent)
		assertEquals("a", emt.text)
	}
	
	@Test fun testSortOnlyFiltered() {
		val emt = Element("root")
		emt.addContent(Text("a"))
		assertEquals("a", emt.text)
		emt.sortContent(Filters.text(), alphaText)
		assertEquals("a", emt.text)
	}
	
	@Test fun testSortAllSameContent() {
		val emt = Element("root")
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		assertEquals("aaaa", emt.text)
		emt.sortContent(alphaContent)
		assertEquals("aaaa", emt.text)
	}
	
	@Test fun testSortAllSameFiltered() {
		val emt = Element("root")
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		emt.addContent(Text("a"))
		assertEquals("aaaa", emt.text)
		emt.sortContent(Filters.text(), alphaText)
		assertEquals("aaaa", emt.text)
	}
	
	@Test fun testSortContent() {
		val emt = Element("root")
		val cdata = CDATA("XXX")
		emt.addContent(Text("d"))
		emt.addContent(cdata)
		emt.addContent(Text("c"))
		emt.addContent(Text("b"))
		emt.addContent(Text("a"))
		assertEquals("dXXXcba", emt.text)
		
		emt.sortContent(alphaContent)
		
		assertEquals("abcdXXX", emt.text)
		assertEquals(cdata, emt.getContent(4))
	}
	
    @Test fun testSortContentNullComp() {
        val emt = Element("root")
        val cdata = CDATA("XXX")
        emt.addContent(Text("d"))
        emt.addContent(cdata)
        emt.addContent(Text("c"))
        emt.addContent(Text("b"))
        emt.addContent(Text("a"))
        assertEquals("dXXXcba", emt.text)
        
        emt.sortContent(null)
        
        assertEquals("dXXXcba", emt.text)
        assertEquals(cdata, emt.getContent(1))
    }
    
	@Test fun testSortElementContent() {
		val emt = Element("root")
		val cdata = CDATA("XXX")
		val a = Element("a")
		val b = Element("b")
		val c = Element("c")
		val d = Element("d")
		
		emt.addContent(d)
		emt.addContent(cdata)
		emt.addContent(c)
		emt.addContent(b)
		emt.addContent(a)
		
		assertTrue(emt.getContent(0) === d)
		assertTrue(emt.getContent(1) === cdata)
		assertTrue(emt.getContent(2) === c)
		assertTrue(emt.getContent(3) === b)
		assertTrue(emt.getContent(4) === a)
		
		emt.sortChildren(alphaContent)
		assertTrue(emt.getContent(0) === a)
		assertTrue(emt.getContent(1) === cdata)
		assertTrue(emt.getContent(2) === b)
		assertTrue(emt.getContent(3) === c)
		assertTrue(emt.getContent(4) === d)
		
		emt.sortContent(alphaContent)
		
		assertTrue(emt.getContent(0) === a)
		assertTrue(emt.getContent(1) === b)
		assertTrue(emt.getContent(2) === c)
		assertTrue(emt.getContent(3) === d)
		assertTrue(emt.getContent(4) === cdata)
	}
	
	@Test fun testSortEqualsContent() {
		val emt = Element("root")
		val cdata = CDATA("XXX")
		val t1 = Text("a")
		val t2 = Text("a")
		val t3 = Text("a")
		val t4 = Text("a")
		emt.addContent(t1)
		emt.addContent(cdata)
		emt.addContent(t2)
		emt.addContent(t3)
		emt.addContent(t4)
		assertEquals("aXXXaaa", emt.text)
		
		emt.sortContent(alphaContent)
		
		assertEquals("aaaaXXX", emt.text)
		assertEquals(t1, emt.getContent(0))
		assertEquals(t2, emt.getContent(1))
		assertEquals(t3, emt.getContent(2))
		assertEquals(t4, emt.getContent(3))
		assertEquals(cdata, emt.getContent(4))
	}
	
	@Test fun testSortInterleavedContent() {
		val emt = Element("root")
		emt.addContent(Text("d"))
		emt.addContent(CDATA("ZZZ"))
		emt.addContent(Text("c"))
		emt.addContent(CDATA("YYY"))
		emt.addContent(Text("b"))
		emt.addContent(CDATA("XXX"))
		emt.addContent(Text("a"))
		assertEquals("dZZZcYYYbXXXa", emt.text)
		
		// we can use Text comparator for CDATA too.
		emt.sortContent(Filters.cdata(), alphaText)
		assertEquals("dXXXcYYYbZZZa", emt.text)
		
		// we can use Text comparator for CDATA too.
		emt.sortContent(ContentFilter(ContentFilter.TEXT), alphaContent)
		assertEquals("aXXXbYYYcZZZd", emt.text)

		// we can use Text comparator for CDATA too.... and Filters.text() does Text and CDATA
		emt.sortContent(Filters.text(), alphaText)
		assertEquals("XXXYYYZZZabcd", emt.text)
	}
	
	@Test fun testSortInterleavedEqualContent() {
		val emt = Element("root")
		val cd1 = CDATA("ZZZ")
		val cd2 = CDATA("ZZZ")
		val cd3 = CDATA("ZZZ")
		emt.addContent(Text("d"))
		emt.addContent(cd1)
		emt.addContent(Text("c"))
		emt.addContent(cd2)
		emt.addContent(Text("b"))
		emt.addContent(cd3)
		emt.addContent(Text("a"))
		assertEquals("dZZZcZZZbZZZa", emt.text)
		
		assertTrue(emt.getContent(1) === cd1)
		assertTrue(emt.getContent(3) === cd2)
		assertTrue(emt.getContent(5) === cd3)
		
		// we can use Text comparator for CDATA too.
		// check sort does not reorder comp==0 content
		emt.sortContent(Filters.cdata(), alphaText)
		assertEquals("dZZZcZZZbZZZa", emt.text)
		assertTrue(emt.getContent(1) === cd1)
		assertTrue(emt.getContent(3) === cd2)
		assertTrue(emt.getContent(5) === cd3)
		
		
		// we can use Text comparator for CDATA too.
		emt.sortContent(ContentFilter(ContentFilter.TEXT), alphaContent)
		assertEquals("aZZZbZZZcZZZd", emt.text)
		assertTrue(emt.getContent(1) === cd1)
		assertTrue(emt.getContent(3) === cd2)
		assertTrue(emt.getContent(5) === cd3)

		// we can use Text comparator for CDATA too.... and Filters.text() does Text and CDATA
		emt.sortContent(Filters.text(), alphaText)
		assertEquals("ZZZZZZZZZabcd", emt.text)
		assertTrue(emt.getContent(0) === cd1)
		assertTrue(emt.getContent(1) === cd2)
		assertTrue(emt.getContent(2) === cd3)
	}

	@Test fun testSortAttributes() {
		val emt = Element("root")
		val att1 = Attribute("one",   "001", Namespace.getNamespace("z", "uri1"))
		val att2 = Attribute("two",   "002", Namespace.getNamespace("y", "uri1"))
		val att3 = Attribute("three", "003", Namespace.getNamespace("x", "uri1"))
		val att4 = Attribute("four",  "004", Namespace.getNamespace("w", "uri2"))
		val att5 = Attribute("five",  "005", Namespace.getNamespace("v", "uri2"))
		
		val att6 = Attribute("six",   "006", Namespace.getNamespace("x", "uri1"))
        
		emt.setAttribute(att5)
		emt.setAttribute(att4)
		emt.setAttribute(att3)
		emt.setAttribute(att2)
		emt.setAttribute(att1)
		
		checkAttOrder(emt.attributes, att5, att4, att3, att2, att1)
		
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return o1.name.compareTo(o2.name);
			}
		})
		
		// alphabetic by string name.
		checkAttOrder(emt.attributes, att5, att4, att1, att3, att2)
		
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return o1.namespacePrefix.compareTo(o2.namespacePrefix);
			}
		})
		
		// Namespace Prefixes's are reverse order
		checkAttOrder(emt.attributes, att5, att4, att3, att2, att1)
		
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return o1.value.compareTo(o2.value);
			}
		})
		
		// Values are in order
		checkAttOrder(emt.attributes, att1, att2, att3, att4, att5)
		
		// Namespace URI's have some common items.... and are in same order
		// as a result, we should have no change at all.
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return o1.namespaceURI.compareTo(o2.namespaceURI);
			}
		})
		
		// Values are in order
		checkAttOrder(emt.attributes, att1, att2, att3, att4, att5)
		
		// Namespace URI's have some common items.... and are in same order
		// as a result, we should have no change at all.... except, this time
		// we do the inverse of the result... so, this moves 4&5 to the front
		// but relative order is maintained for equal values....
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return - o1.namespaceURI.compareTo(o2.namespaceURI);
			}
		})
		
		// Values are in order
		checkAttOrder(emt.attributes, att4, att5, att1, att2, att3)
		
        // use null sort on attributes... which sorts alphabetically by pfx:name
        emt.sortAttributes(null)
        
        // Values are in order
        checkAttOrder(emt.attributes, att5, att4, att3, att2, att1)

        emt.setAttribute(att6)
        
        checkAttOrder(emt.attributes, att5, att4, att3, att2, att1, att6)

        // alpha should sort att6 to before att3 (same prefix, six comes before three)
        emt.sortAttributes(null)
        
        checkAttOrder(emt.attributes, att5, att4, att6, att3, att2, att1)
        
	}
	
	@Test fun testSortAttributesNone() {
		val emt = Element("root")
		emt.sortAttributes(object : Comparator<Attribute> {
			override fun compare(o1: Attribute, o2: Attribute): Int {
				return - o1.namespaceURI.compareTo(o2.namespaceURI);
			}
		})
		// this ends up creating the Attribute array inside the Element.
		checkAttOrder(emt.attributes)
	}

	private fun checkAttOrder(attributes: MutableList<Attribute>, vararg atts: Attribute) {
		assertTrue(atts.size == attributes.size)
		for (i in atts.size - 1 downTo 0) {
			assertTrue(atts[i] === attributes.get(i))
		}
		
	}

	private fun getTestString(): String {
		return "  this  has  space "
	}
	
	private fun getComp(): String {
		return Format.compact(getTestString())
	}
	
	private fun getTrim(): String {
		return Format.trimBoth(getTestString())
	}
	
	private fun getPlain(): String {
		return getTestString() 
	}
	
	private fun getNamespace(): Namespace {
		return Namespace.getNamespace("jdomtest")
	}

	fun getTextHelperRoot(): Element {
		val ns = getNamespace()
		
		val root = Element("root")
		val childa = Element("child")
		val childb = Element("child", ns)
		val childc = Element("child")
		val childd = Element("child", ns)
		val childe = Element("kid")
		val childf = Element("kid", ns)
		
		childa.setText(getTestString())
		childb.setText(getTestString())
		childc.setText(getTestString())
		childd.setText(getTestString())
		root.addContent(childa)
		root.addContent(childb)
		root.addContent(childc)
		root.addContent(childd)
		root.addContent(childe)
		root.addContent(childf)
		
		return root
	}

	@Test fun testGetChildTextElementString() {
		val root = getTextHelperRoot()
		assertEquals(getPlain(), root.getChildText("child"))
		assertNull(root.getChildText("dummy"))
		assertEquals("", root.getChildText("kid"))
	}

	@Test fun testGetChildTextElementStringNamespace() {
		val root = getTextHelperRoot()
		val ns = getNamespace()
		assertEquals(getPlain(), root.getChildText("child", ns))
		assertNull(root.getChildText("dummy", ns))
		assertEquals("", root.getChildText("kid", ns))
	}

	@Test fun testGetChildTextTrimElementString() {
		val root = getTextHelperRoot()
		assertEquals(getTrim(), root.getChildTextTrim("child"))
		assertNull(root.getChildTextTrim("dummy"))
		assertEquals("", root.getChildTextTrim("kid"))
	}

	@Test fun testGetChildTextTrimElementStringNamespace() {
		val root = getTextHelperRoot()
		val ns = getNamespace()
		assertEquals(getTrim(), root.getChildTextTrim("child", ns))
		assertNull(root.getChildTextTrim("dummy", ns))
		assertEquals("", root.getChildTextTrim("kid", ns))
	}

	@Test fun testGetChildTextNormalizeElementString() {
		val root = getTextHelperRoot()
		assertEquals(getComp(), root.getChildTextNormalize("child"))
		assertNull(root.getChildTextNormalize("dummy"))
		assertEquals("", root.getChildTextNormalize("kid"))
	}

	@Test fun testGetChildTextNormalizeElementStringNamespace() {
		val root = getTextHelperRoot()
		val ns = getNamespace()
		assertEquals(getComp(), root.getChildTextNormalize("child", ns))
		assertNull(root.getChildTextNormalize("dummy", ns))
		assertEquals("", root.getChildTextNormalize("kid", ns))
	}


	@Test fun testCoalesceTextSimple() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		root.addContent("two")
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(false))
		assertTrue(1 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(false))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextCDATA() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		root.addContent(CDATA("two"))
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(false))
		assertTrue(3 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(false))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextNested() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		val kid = Element("kid")
		root.addContent(kid)
		kid.addContent("two")
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one  three", root.text)
		
		assertTrue(root.coalesceText(false))
		assertTrue(3 == root.contentSize)
		assertEquals("one  three", root.text)
		assertFalse(root.coalesceText(false))
		assertEquals("one  three", root.text)
	}
	
	@Test fun testCoalesceTextEmpty() {
		val root = Element("root")
		root.addContent("")
		root.addContent("one")
		root.addContent("")
		root.addContent(" ")
		root.addContent("")
		
		root.addContent("two")
		
		root.addContent("")
		root.addContent(" ")
		root.addContent("")
		root.addContent("three")
		root.addContent("")
		assertTrue(11 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(false))
		assertTrue(1 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(false))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextSingle() {
		val root = Element("root")
		root.addContent("")
		assertEquals("", root.text)
		
		assertTrue(root.coalesceText(false))
		assertTrue(0 == root.contentSize)
		assertEquals("", root.text)
		assertFalse(root.coalesceText(false))
		assertEquals("", root.text)
	}


	@Test fun testCoalesceTextSimpleRec() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		root.addContent("two")
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(true))
		assertTrue(1 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(true))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextCDATARec() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		root.addContent(CDATA("two"))
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(true))
		assertTrue(3 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(true))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextNestedRec() {
		val root = Element("root")
		root.addContent("one")
		root.addContent(" ")
		val kid = Element("kid")
		root.addContent(kid)
		kid.addContent("two")
		root.addContent(" ")
		root.addContent("three")
		assertTrue(5 == root.contentSize)
		assertEquals("one  three", root.text)
		
		assertTrue(root.coalesceText(true))
		assertTrue(3 == root.contentSize)
		assertEquals("one  three", root.text)
		assertFalse(root.coalesceText(true))
		assertEquals("one  three", root.text)
	}
	
	@Test fun testCoalesceTextEmptyRec() {
		val root = Element("root")
		root.addContent("")
		root.addContent("one")
		root.addContent("")
		root.addContent(" ")
		root.addContent("")
		
		root.addContent("two")
		
		root.addContent("")
		root.addContent(" ")
		root.addContent("")
		root.addContent("three")
		root.addContent("")
		assertTrue(11 == root.contentSize)
		assertEquals("one two three", root.text)
		
		assertTrue(root.coalesceText(true))
		assertTrue(1 == root.contentSize)
		assertEquals("one two three", root.text)
		assertFalse(root.coalesceText(true))
		assertEquals("one two three", root.text)
	}
	
	@Test fun testCoalesceTextSingleRec() {
		val root = Element("root")
		root.addContent("")
		assertEquals("", root.text)
		
		assertTrue(root.coalesceText(true))
		assertTrue(0 == root.contentSize)
		assertEquals("", root.text)
		assertFalse(root.coalesceText(true))
		assertEquals("", root.text)
	}


}
