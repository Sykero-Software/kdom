/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases;

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
