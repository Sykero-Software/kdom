/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.special;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;

import fi.sykero.kdom.Document;
import fi.sykero.kdom.Element;
import fi.sykero.kdom.JDOMException;
import fi.sykero.kdom.input.SAXBuilder;
import fi.sykero.kdom.input.sax.XMLReaders;
import fi.sykero.kdom.output.Format;
import fi.sykero.kdom.output.XMLOutputter;
import fi.sykero.kdom.test.util.FidoFetch;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestIssue173ContentCount {

	@Test
	public void testContentSize() throws Exception {
		// In Version 1.1 this was new SAXBuilder(false)
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><![CDATA[TEST]]></root>";
		Document doc = new SAXBuilder(XMLReaders.NONVALIDATING).build(new StringReader(input));
		Element rootElement = doc.getRootElement();
		System.out.println("Content Size is " + rootElement.getContentSize());
		assertEquals(1, rootElement.getContentSize());
	}

}
