/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import fi.sykero.kdom.Attribute;
import fi.sykero.kdom.CDATA;
import fi.sykero.kdom.Comment;
import fi.sykero.kdom.DocType;
import fi.sykero.kdom.Document;
import fi.sykero.kdom.Element;
import fi.sykero.kdom.EntityRef;
import fi.sykero.kdom.Namespace;
import fi.sykero.kdom.ProcessingInstruction;
import fi.sykero.kdom.Text;
import fi.sykero.kdom.filter.ContentFilter;
import fi.sykero.kdom.filter.Filter;
import fi.sykero.kdom.filter.Filters;
import fi.sykero.kdom.test.util.UnitTestUtil;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestFilters extends AbstractTestFilter {
	
	
	private <F> void checkFilter(Filter<F> filter, F match, Object not) {
		assertNotNull(filter);
		assertNotNull(filter.toString());
		// K5: the deserialized-copy check is gone (no serialization)
		assertFilterNotEquals(filter, filter.negate());
		assertFalse(filter.equals(null));
		assertTrue(filter.matches(match));
		assertFalse(filter.matches(not));
		ArrayList<Object> al = null;
		assertTrue(filter.filter(al).isEmpty());
		al = new ArrayList<Object>(3);
		al.add(not);
		al.add(match);
		al.add(null);
		List<F> mat = filter.filter(al);
		
		assertTrue(mat.size() == 1);
		assertTrue(mat.get(0) == match);
	}

	@Test
	public void testContent() {
		checkFilter(Filters.content(), new Element("tag"), new Object());
		checkFilter(Filters.content(), new Element("tag"), null);
	}

	@Test
	public void testDocument() {
		checkFilter(Filters.document(), new Document(), new Object());
	}

	@Test
	public void testAttribute() {
		checkFilter(Filters.attribute(), new Attribute("tag", "val"), new Object());
	}

	@Test
	public void testAttributeString() {
		checkFilter(Filters.attribute("mat"), new Attribute("mat", "val"), new Attribute("not", "val"));
	}

	@Test
	public void testAttributeStringNamespace() {
		Namespace nsa = Namespace.getNamespace("pfa", "uria");
		Namespace nsb = Namespace.getNamespace("pfb", "urib");
		checkFilter(Filters.attribute("mat", nsa), 
				new Attribute("mat", "val", nsa), new Object());
		checkFilter(Filters.attribute("mat", nsa), 
				new Attribute("mat", "val", nsa), new Attribute("mat", "val", nsb));
		checkFilter(Filters.attribute("mat", nsa), 
				new Attribute("mat", "val", nsa), new Attribute("not", "val", nsa));
		checkFilter(Filters.attribute("mat", nsa), 
				new Attribute("mat", "val", nsa), new Attribute("mat", "val"));
		checkFilter(Filters.attribute("mat", null), 
				new Attribute("mat", "val", nsa), new Attribute("not", "val", nsa));
		checkFilter(Filters.attribute(null, nsa), 
				new Attribute("mat", "val", nsa), new Attribute("mat", "val", nsb));
		checkFilter(Filters.attribute(null, null), 
				new Attribute("mat", "val", nsa), new Element("mat", nsb));
	}

	@Test
	public void testAttributeNamespace() {
		Namespace nsa = Namespace.getNamespace("pfa", "uria");
		Namespace nsb = Namespace.getNamespace("pfb", "urib");
		checkFilter(Filters.attribute(nsa), 
				new Attribute("mat", "val", nsa), new Object());
		checkFilter(Filters.attribute(nsa), 
				new Attribute("mat", "val", nsa), new Attribute("mat", "val", nsb));
	}

	@Test
	public void testComment() {
		checkFilter(Filters.comment(), new Comment("comment"), new Object());
	}

	@Test
	public void testCdata() {
		checkFilter(Filters.cdata(), new CDATA("comment"), new Object());
	}

	@Test
	public void testDoctype() {
		checkFilter(Filters.doctype(), new DocType("root"), new Object());
	}

	@Test
	public void testEntityref() {
		checkFilter(Filters.entityref(), new EntityRef("er"), new Object());
	}

	@Test
	public void testElement() {
		checkFilter(Filters.element(), new Element("emt"), new Object());
	}

	@Test
	public void testElementString() {
		Namespace nsa = Namespace.getNamespace("pfa", "uria");
		checkFilter(Filters.element("mat"), 
				new Element("mat"), new Object());
		checkFilter(Filters.element("mat"), 
				new Element("mat"), new Element("mat", nsa));
		checkFilter(Filters.element("mat"), 
				new Element("mat"), new Element("not", nsa));
	}

	@Test
	public void testElementStringNamespace() {
		Namespace nsa = Namespace.getNamespace("pfa", "uria");
		Namespace nsb = Namespace.getNamespace("pfb", "urib");
		checkFilter(Filters.element("mat", nsa), 
				new Element("mat", nsa), new Object());
		checkFilter(Filters.element("mat", nsa), 
				new Element("mat", nsa), new Element("mat", nsb));
		checkFilter(Filters.element("mat", nsa), 
				new Element("mat", nsa), new Element("not", nsa));
		checkFilter(Filters.element("mat", nsa), 
				new Element("mat", nsa), new Element("mat"));
	}

	@Test
	public void testElementNamespace() {
		Namespace nsa = Namespace.getNamespace("pfa", "uria");
		Namespace nsb = Namespace.getNamespace("pfb", "urib");
		checkFilter(Filters.element(nsa), 
				new Element("mat", nsa), new Object());
		checkFilter(Filters.element(nsa), 
				new Element("mat", nsa), new Element("mat", nsb));
		checkFilter(Filters.element(nsa), 
				new Element("mat", nsa), new Element("mat"));
	}

	@Test
	public void testProcessinginstruction() {
		checkFilter(Filters.processinginstruction(), 
				new ProcessingInstruction("jdomtest", ""), new Object());
	}

	@Test
	public void testText() {
		checkFilter(Filters.text(), new Text("txt"), new Object());
		checkFilter(Filters.text(), new CDATA("txt"), new Object());
	}

	@Test
	public void testTextOnly() {
		checkFilter(Filters.textOnly(), new Text("txt"), new CDATA("txt"));
	}

	@Test
	public void testFBoolean() {
		checkFilter(Filters.fboolean(), Boolean.TRUE, new Object());
	}

	@Test
	public void testFString() {
		checkFilter(Filters.fstring(), new String("txt"), new Object());
	}

	@Test
	public void testFDouble() {
		checkFilter(Filters.fdouble(), Double.valueOf(123.45), new Object());
	}
	
	@Test
	public void testFClass() {
		checkFilter(Filters.fclass(kotlin.jvm.JvmClassMappingKt.getKotlinClass(Integer.class)), // K5: fclass takes a KClass
				Integer.valueOf(123), new Object());
	}
	
	@Test
	public void testRefine() {
		try {
			new ContentFilter().refine(null);
			fail("Should not be able to set null refine filter");
		} catch (NullPointerException npe) {
			//good
		} catch (Exception e) {
			e.printStackTrace();
			fail("Expected NullPointerException but got " + e.getClass());
		}
	}

}
