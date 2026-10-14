/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.special;

import static org.junit.Assert.*;

import fi.sykero.kdom.Document;
import fi.sykero.kdom.Element;
import fi.sykero.kdom.input.KdomBuilder;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestIssue173ContentCount {

	/** K5: the SAXBuilder parse, through KdomBuilder (non-validating). */
	private static Document parseWithKdomBuilder(String xml) throws Exception {
		return new KdomBuilder().build(xml);
	}

	@Test
	public void testContentSize() throws Exception {
		// In Version 1.1 this was new SAXBuilder(false)
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><![CDATA[TEST]]></root>";
		Document doc = parseWithKdomBuilder(input);
		Element rootElement = doc.getRootElement();
		System.out.println("Content Size is " + rootElement.getContentSize());
		assertEquals(1, rootElement.getContentSize());
	}

}
