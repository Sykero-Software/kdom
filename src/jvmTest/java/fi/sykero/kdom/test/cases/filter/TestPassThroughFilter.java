/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.cases.filter;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

import fi.sykero.kdom.filter.Filter;
import fi.sykero.kdom.filter.Filters;
import fi.sykero.kdom.test.util.JvmTestUtil;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class TestPassThroughFilter  {
	
	@Test
	public void testPassThroughFilterRandom() {
		Filter<Object> ef = Filters.fpassthrough();
		
		List<String> lst = new ArrayList<String>(3);
		lst.add("hello");
		lst.add("there");
		lst.add("world");
		List<Object> filtered = ef.filter(lst);
		assertTrue(filtered instanceof RandomAccess);
		assertTrue(filtered.size() == 3);
		assertEquals("hello", filtered.get(0));
		assertEquals("there", filtered.get(1));
		assertEquals("world", filtered.get(2));
		
		try {
			filtered.add("boo");
			JvmTestUtil.failNoException(UnsupportedOperationException.class);
		} catch (Exception e) {
			JvmTestUtil.checkException(UnsupportedOperationException.class, e);
		}
		
	}


	@Test
	public void testPassThroughFilterLinked() {
		Filter<Object> ef = Filters.fpassthrough();
		
		List<String> lst = new LinkedList<String>();
		lst.add("hello");
		lst.add("there");
		lst.add("world");
		List<Object> filtered = ef.filter(lst);
		assertTrue(filtered instanceof RandomAccess);
		assertTrue(filtered.size() == 3);
		assertEquals("hello", filtered.get(0));
		assertEquals("there", filtered.get(1));
		assertEquals("world", filtered.get(2));
		
		try {
			filtered.add("boo");
			JvmTestUtil.failNoException(UnsupportedOperationException.class);
		} catch (Exception e) {
			JvmTestUtil.checkException(UnsupportedOperationException.class, e);
		}
		
	}


	@Test
	public void testPassThroughFilterNull() {
		Filter<Object> ef = Filters.fpassthrough();
		
		List<String> lst = null;
		List<Object> filtered = ef.filter(lst);
		assertTrue(filtered instanceof RandomAccess);
		assertTrue(filtered.size() == 0);
		
		try {
			filtered.add("boo");
			JvmTestUtil.failNoException(UnsupportedOperationException.class);
		} catch (Exception e) {
			JvmTestUtil.checkException(UnsupportedOperationException.class, e);
		}
		
	}

	@Test
	public void testPassThroughFilterEmpty() {
		Filter<Object> ef = Filters.fpassthrough();
		
		List<String> lst = new LinkedList<String>();
		List<Object> filtered = ef.filter(lst);
		assertTrue(filtered instanceof RandomAccess);
		assertTrue(filtered.size() == 0);
		
		try {
			filtered.add("boo");
			JvmTestUtil.failNoException(UnsupportedOperationException.class);
		} catch (Exception e) {
			JvmTestUtil.checkException(UnsupportedOperationException.class, e);
		}
		
	}
}
