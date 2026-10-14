package fi.sykero.kdom.test.cases;

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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fi.sykero.kdom.Attribute;
import fi.sykero.kdom.Comment;
import fi.sykero.kdom.Element;
import fi.sykero.kdom.ProcessingInstruction;
import fi.sykero.kdom.Text;

/**
 * Raw instances: a subclass created through the protected no-argument constructor has {@code null} for properties
 * that kdom types as non-null (see translation-notes, "Raw instances"). These five tests were cut from
 * {@code TestText}, {@code TestComment}, {@code TestProcessingInstruction}, {@code TestAttribute} and
 * {@code TestElement} when those moved to commonTest (Task 8); the {@code == null} on a non-null type is JVM-only
 * behaviour (undefined on Kotlin/Native). The test bodies are unchanged.
 */
@SuppressWarnings("javadoc")
public class RawInstanceTest {

	@Test
	public void textRaw() {
		Text txt = new Text() {
			// nothing
    		private static final long serialVersionUID = 200L;
		};
		assertTrue(txt.getValue() == null);
		assertTrue(txt.getText() == null);
	}
	@Test
	public void commentRaw() {
		// test creating a subclass with an anonymous instance
		final Comment theComment = new Comment() {
			// no modifications.
    		private static final long serialVersionUID = 200L;
		};
		assertTrue(null == theComment.getText());
	}
	@Test
	public void processingInstructionRaw() {
		ProcessingInstruction pi = new ProcessingInstruction() {
			// nothing
    		private static final long serialVersionUID = 200L;
		};
		assertTrue(null == pi.getTarget());
		assertTrue(null == pi.getValue());
	}
    /**
     * Test the simple case of constructing an attribute without name, value,
     * namespace or prefix
     */
    @Test
    public void attributeRaw() {
        final Attribute attribute = new Attribute(){
    		private static final long serialVersionUID = 200L;
        };
        assertTrue(null == attribute.getName());
        assertTrue(attribute.isSpecified());
    }
    /**
     * Test the constructor for a subclass element
     */
    @Test
    public void elementRaw() {
    	Element emt = new Element() {
    		// change nothing
    		private static final long serialVersionUID = 200L;
    	};
    	assertNull(emt.getName());
    }
}
