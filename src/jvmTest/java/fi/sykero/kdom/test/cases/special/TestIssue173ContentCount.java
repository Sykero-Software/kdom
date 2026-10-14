/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.special;

import static org.junit.Assert.*;

import java.io.StringReader;

import fi.sykero.kdom.Document;
import fi.sykero.kdom.Element;

import org.junit.Ignore;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestIssue173ContentCount {

	/** K5: placeholder for the SAXBuilder parse; Task 9 switches the body to KdomBuilder (non-validating). */
	private static Document parseWithKdomBuilder(String xml) {
		throw new UnsupportedOperationException("needs KdomBuilder, Task 9");
	}

	// K5: needs KdomBuilder (Task 9)
	@Ignore("needs KdomBuilder, Task 9")
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
