package fi.sykero.kdom.output;

import static org.junit.Assert.assertThrows;

import org.junit.Test;

/**
 * Behaviour difference (translation-notes.md): kdom's non-null setters reject {@code null} at the call with a
 * NullPointerException; jdom2 accepted it and failed with a NullPointerException later, at output.
 * (A Java test: Kotlin code cannot pass null to these parameters.)
 */
@SuppressWarnings("javadoc")
public class NullSetterTest {

	@Test
	public void kdomRejectsNullAtTheSetter() {
		Format f = Format.getRawFormat();
		assertThrows(NullPointerException.class, () -> f.setTextMode(null));
		assertThrows(NullPointerException.class, () -> f.setEscapeStrategy(null));
		XMLOutputter out = new XMLOutputter();
		assertThrows(NullPointerException.class, () -> out.setFormat(null));
		assertThrows(NullPointerException.class, () -> out.setXMLOutputProcessor(null));
	}

	@Test
	public void jdom2FailedLaterAtOutput() {
		org.jdom2.Document jdoc = new org.jdom2.Document(new org.jdom2.Element("root").setText("t"));
		org.jdom2.output.Format f1 = org.jdom2.output.Format.getPrettyFormat().setTextMode(null);
		org.jdom2.output.XMLOutputter o1 = new org.jdom2.output.XMLOutputter(f1);
		assertThrows(NullPointerException.class, () -> o1.outputString(jdoc));

		org.jdom2.output.Format f2 = org.jdom2.output.Format.getRawFormat().setEncoding("US-ASCII");
		f2.setEscapeStrategy(null);
		org.jdom2.Document jdoc2 = new org.jdom2.Document(new org.jdom2.Element("root").setText("é"));
		org.jdom2.output.XMLOutputter o2 = new org.jdom2.output.XMLOutputter(f2);
		assertThrows(NullPointerException.class, () -> o2.outputString(jdoc2));

		org.jdom2.output.XMLOutputter o3 = new org.jdom2.output.XMLOutputter();
		assertThrows(NullPointerException.class, () -> o3.setFormat(null));

		org.jdom2.output.XMLOutputter o4 = new org.jdom2.output.XMLOutputter();
		o4.setXMLOutputProcessor(null);
		assertThrows(NullPointerException.class, () -> o4.outputString(jdoc));
	}
}
