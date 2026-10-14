/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom;

import static org.junit.Assert.*;

import fi.sykero.kdom.IllegalAddException;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestIllegalAddExceptn {
	
	@Test
	public void testIllegalAddExceptionElementAttributeString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new Attribute("att", "val"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementElementString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt1"), 
						new Element("emt2"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						"msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementProcessingInstructionString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new ProcessingInstruction("target", "data"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionProcessingInstructionString() {
		assertTrue (null != 
				new IllegalAddException(new ProcessingInstruction("target", "data"), 
						"msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementCommentString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new Comment("no comment!"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementCDATAString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new CDATA("cdata"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementTextString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new Text("text"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionCommentString() {
		assertTrue (null != 
				new IllegalAddException(new Comment("no comment!"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementEntityRefString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						new EntityRef("name"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionElementNamespaceString() {
		assertTrue (null != 
				new IllegalAddException(new Element("emt"), 
						Namespace.getNamespace("prefix", "ns"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionDocTypeString() {
		assertTrue (null != 
				new IllegalAddException( 
						new DocType("emt"), "msg").getMessage());
	}

	@Test
	public void testIllegalAddExceptionString() {
		assertTrue (null != 
				new IllegalAddException("msg").getMessage());
	}

}
