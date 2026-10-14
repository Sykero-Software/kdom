/*-- 

 Copyright (C) 2000-2012 Jason Hunter & Brett McLaughlin.
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
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

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
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */

package fi.sykero.kdom

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kotlin callers keep jdom2's synthetic-property syntax (`e.name = "a"`) next to the fluent Java-shaped
 * `setX(..)` functions (R8: dual setter API), and raw instances keep jdom2's `null` getters.
 */
class PropertySyntaxTest {

    @Test
    fun propertyAssignmentCallsTheFluentSetter() {
        val e = Element("a")
        e.name = "b"
        assertEquals("b", e.name)
        val ns = Namespace.getNamespace("p", "urn:x")
        e.namespace = ns
        assertSame(ns, e.namespace)
        e.text = "hello"
        assertEquals("hello", e.text)
        val t = Text("x")
        t.text = "b"
        assertEquals("b", t.text)
        val att = Attribute("n", "v")
        att.name = "m"
        att.value = "w"
        assertEquals("m", att.name)
        assertEquals("w", att.value)
        val c = Comment("c")
        c.text = "d"
        assertEquals("d", c.text)
        val er = EntityRef("e")
        er.name = "f"
        er.systemID = "sys"
        assertEquals("f", er.name)
        assertEquals("sys", er.systemID)
        val pi = ProcessingInstruction("t")
        pi.target = "u"
        pi.data = "k=\"v\""
        assertEquals("u", pi.target)
        assertEquals("v", pi.getPseudoAttributeValue("k"))
        val doc = Document()
        doc.rootElement = Element("root")
        assertEquals("root", doc.rootElement.name)
        doc.docType = DocType("root")
        assertEquals("root", doc.docType!!.elementName)
    }

    @Test
    fun setterFunctionsAreFluentAndChain() {
        val e = Element("a")
        assertSame(e, e.setName("n"))
        assertEquals("c", e.setName("b").setName("c").name)
        assertSame(e, e.setText("t"))
        val t = Text("x")
        assertSame(t, t.setText("y"))
        assertSame(e, e.setAttribute("k", "v").setNamespace(null))
        assertEquals("v", e.getAttributeValue("k"))
        e.attributes = mutableListOf(Attribute("z", "1"))
        assertEquals(1, e.attributes.size)
    }

    @Test
    fun rawInstancesReturnNullFromNonNullTypedProperties() {
        val e = object : Element() {}
        val name: String? = e.name // typed String, holds jdom2's null
        assertNull(name)
        val t = object : Text() {}
        val text: String? = t.text
        assertNull(text)
    }

    @Test
    fun documentPropertyAcceptsANullKey() {
        val doc = Document()
        doc.setProperty(null, "v")
        assertEquals("v", doc.getProperty(null))
        doc.setProperty("k", null)
        assertTrue(doc.getProperty("k") == null)
    }
}
