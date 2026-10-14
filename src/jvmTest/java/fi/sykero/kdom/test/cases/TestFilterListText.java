/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases;

import java.util.List;

import fi.sykero.kdom.Element;
import fi.sykero.kdom.Text;
import fi.sykero.kdom.filter.Filters;
import fi.sykero.kdom.test.util.AbstractTestList;

@SuppressWarnings("javadoc")
public class TestFilterListText extends AbstractTestList<Text> {
	
	public TestFilterListText() {
		super(Text.class, false);
	}

	@Override
	public List<Text> buildEmptyList() {
		Element root = new Element("root");
		return root.getContent(Filters.text());
	}

	@Override
	public Text[] buildSampleContent() {
		return new Text[] {
				new Text("kida"),
				new Text("kidb"),
				new Text("kidc"),
				new Text("kidd"),
				new Text("kide"),
				new Text("kidf"),
				new Text("kidg"),
				new Text("kidh"),
				new Text("kidi"),
				new Text("kidj"),
		};
	}
	
	@Override
	public Text[] buildAdditionalContent() {
		return new Text[] { };
	}

	@Override
	public Text[] buildIllegalArgumentContent() {
		return new Text[] { };
	}

	@Override
	public Object[] buildIllegalClassContent() {
		return new Object[] {};
	}


}
