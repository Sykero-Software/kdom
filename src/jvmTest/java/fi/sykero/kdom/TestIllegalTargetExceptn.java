/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom;

import static org.junit.Assert.*;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestIllegalTargetExceptn {

	@Test
	public void testIllegalTargetExceptionStringString() {
		assertTrue (null != 
				new IllegalTargetException("target", "reason").getMessage());

	}

	@Test
	public void testIllegalTargetExceptionString() {
		assertTrue (null != 
				new IllegalTargetException("reason").getMessage());
	}

}
