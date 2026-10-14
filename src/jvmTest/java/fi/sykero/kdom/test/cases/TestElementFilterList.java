/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases;

import kotlin.jvm.JvmClassMappingKt;
import java.util.List;

import fi.sykero.kdom.Content;
import fi.sykero.kdom.Element;
import fi.sykero.kdom.filter.ElementFilter;
import fi.sykero.kdom.test.util.AbstractTestList;
import org.junit.Before;

@SuppressWarnings("javadoc")
public class TestElementFilterList extends AbstractTestList<Element> {
	
	private static final Element base = new Element("dummy");
	private static final Element parent = new Element("parent").addContent(base);


	public TestElementFilterList() {
		super(JvmClassMappingKt.getKotlinClass(Element.class), false);
	}
	
	@Override
	public List<Element> buildEmptyList() {
		base.getContent().clear();
		return base.getContent(new ElementFilter());
	}

	@Override
	public Element[] buildSampleContent() {
		return new Element[]{ new Element("zero"), 
				new Element("one"), new Element("two"), 
				new Element("three"), new Element("four"),
				new Element("five"), new Element("six")};
	}

	@Override
	public Element[] buildAdditionalContent() {
		return new Element[]{ new Element("seven"), 
				new Element("eight")};
	}

	@Override
	public Object[] buildIllegalClassContent() {
		Object[] ret = new Object[] {};
		return ret;
	}
	
	@Override
	public Element[] buildIllegalArgumentContent() {
		return new Element[]{base, parent};
	}
	
	@Before
	public void detatchAll () {
		// make sure all content is detached before each test.
		for (Content c : buildSampleContent()) {
			c.detach();
		}
	}
	
}
