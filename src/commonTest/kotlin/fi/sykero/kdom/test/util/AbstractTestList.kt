/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.util

import kotlin.test.*
import kotlin.reflect.KClass
import fi.sykero.kdom.test.util.UnitTestUtil.checkException
import fi.sykero.kdom.test.util.UnitTestUtil.failNoException
//JAVA java.lang.reflect.Array


/**
 * This base class can be used to test multiple implementations of List<?>
 * It is reusable for different list types, and does the 'normal' type testing.
 * 
 * 
 * 
 * @author Rolf Lear
 *
 */
abstract class AbstractTestList<T>(private val i_tclass: KClass<*>, private val i_nullok: Boolean) {
	
	/**
	 * The following is a list of somewhat binary-progressive prime numbers.
	 * sequence is +2, +2, +2, +2, +4, +4, +4, +4, +8, +8, +8, +8,
	 *            +16, +16, +16, +16, +32, ......
	 * given the above sequence, get the first prime number that is larger than
	 * the sequence value.
	 */
	private val BIGPRIMES: IntArray = intArrayOf(3, 5, 7, 11, 13, 17, 23, 
		29, 37, 41, 53, 59, 73, 89, 107, 127, 157, 191, 223, 251, 313, 379, 443, 
		509, 641, 761, 907, 1019, 1277, 1531, 1787, 2053, 2557, 3067, 3581, 
		4091, 5113, 6143, 7177, 8191, 10243, 12281, 14341, 16381, 20477, 24571, 
		28669, 32771, 40961, 49157, 57347, 65537, 81919, 98297, 114689, 131071, 
		163841, 196613, 229373, 262139, 327673, 393209, 458747, 524287, 655357, 
		786431, 917503, 1048571)
	
	private val SMALLSHUFFLE: Array<IntArray> = arrayOf(
		intArrayOf(),
		intArrayOf(0),
		intArrayOf(1,0),
		intArrayOf(2, 0, 1),
		intArrayOf(2, 0, 3, 1),
		intArrayOf(4, 1, 2, 0, 3),
		intArrayOf(3, 0, 2, 4, 5, 1)
	)
	


	/**
	 * Generalised constructor for testing a list.
	 * @param tclass all content should be an instance (or subtype) of this class 
	 * @param nullok are null values allowed on the list.
	 * @param samplecontent some sample data to load in to the list. The more, the better.
	 */


	/**
	 * Create a list of the type to test. It should be empty.
	 * @return the list to test.
	 */
	abstract fun buildEmptyList(): MutableList<T>
	
	/**
	 * This is sample data that should be legal in the list.
	 * There should be at least 10 or so elements.
	 * @return the sample data.
	 */
	abstract fun buildSampleContent(): Array<T>
	
	/**
	 * This should contain at least one additional valid entry
	 * that can be legally be added to the list after it is already populated
	 * with the sample data.
	 * @return the extra data.
	 */
	abstract fun buildAdditionalContent(): Array<T>
	
	/**
	 * This should contain data that is of the correct generic type of the data
	 * but has some property that makes it illegal. This can be empty.... 
	 * @return the IllegalArgument test data.
	 */
	abstract fun buildIllegalArgumentContent(): Array<T>
	/**
	 * This list should be of content that is of the wrong class (a class not
	 * compatible with <T>
	 * @return the ClassCast test data.
	 */
	abstract fun buildIllegalClassContent(): Array<Any?>

	
	/* *********************************
	 * Private support methods
	 * ********************************* */
	
	/**
	 * Create an array of the required type with the required length
	 * @param length The length of the array to create.
	 * @return the created array.
	 */
	protected fun buildArray(length: Int): Array<T> {
		return buildSampleContent().copyOf(length) as Array<T>
	}
	
	/**
	 * Remove an element from an array.
	 * @param content The original content.
	 * @param index The element to remove
	 * @return a new array with the specified element removed.
	 */
	protected fun arrayRemove(content: Array<T>, index: Int): Array<T> {
		if (index < 0 || index >= content.size) {
			throw IllegalArgumentException("Can not have index " + index 
					+ " when there are only " + content.size + " elements.")
		}
		val ret: Array<T> = arrCopy(content, content.size - 1)
		content.copyInto(ret, index, index + 1, index + 1 + ret.size - index)
		return ret
	}
	
	/**
	 * Insert an element in to the content at the specified index.
	 * @param content The base content to add to.
	 * @param index The position to insert (items from this position will be
	 * 				moved to the right). Using content.length will add to the end.
	 * @param insert The value to insert
	 * @return The amended content array
	 */
	protected fun arrayInsert(content: Array<T>, index: Int, vararg insert: T): Array<T> {
		if (index < 0 || index > content.size) {
			throw IllegalArgumentException("Can not use index " + index 
					+ " when there is only " + content.size + " content.")
		}
		if (insert == null) {
			throw NullPointerException("Can not have a null insert vararg array")
		}
		val ret: Array<T> = arrCopy(content, content.size + insert.size)
		ret.copyInto(ret, index+insert.size, index, index + content.size - index)
		insert.copyInto(ret, index, 0, 0 + insert.size)
		return ret
	}
	
	/**
	 * Reverse the contents of an array.
	 * @param content The array to reverse.
	 * @return a new array with the same content as the input, but in reverse
	 * 				order
	 */
	protected fun arrayReverse(content: Array<T>): Array<T> {
		val ret: Array<T> = arrCopy(content, content.size)
		for (i in (content.size - 1) / 2 downTo 0) {
			val tmp = ret[i]
			ret[i] = ret[ret.size - 1 - i]
			ret[ret.size - 1 - i] = tmp
		}
		return ret
	}
	
	@Suppress("UNCHECKED_CAST")
	private fun arrCopy(a: Array<T>, n: Int): Array<T> = a.copyOf(n) as Array<T>

	private fun arrCopy(a: IntArray, n: Int): IntArray = a.copyOf(n)

	private fun pickPrime(len: Int): Int {
		if (len < SMALLSHUFFLE.size) {
			throw IllegalStateException("Should have a prime set already")
		}
		var pindex: Int = 0
		while (pindex < BIGPRIMES.size && BIGPRIMES[pindex] <= (len / 2)) {
			pindex++
		}
		if (pindex >= BIGPRIMES.size) {
			throw IllegalStateException("Unable to create a shuffled order " +
					"for that many elements: " + len)
		}
		return BIGPRIMES[pindex]
	}
	
	/*
	 * helper method for shuffle().
	 */
	private fun shuffleCompute(offset: Int, len: Int, prime: Int): Int {
		// Integer.MAX_INT is a prime number... but too big
		// Using a Prime in this way guarantees that we loop through all the values
		// in the sequence without having duplicates.
		return (offset + prime) % len
	}
	
	/**
	 * Generate a (very) pseudo-random order of a given length.
	 * The shuffled order is always repeatable, predictable, but also
	 * appears 'random'. This is useful so that we can compare strange-ordered
	 * inserts/removes/sets.
	 * @param len How many elements to include.
	 * @return An array of int. Each value will be unique, from 0 to len-1
	 */
	protected fun shuffle(len: Int): IntArray {
		if (len < SMALLSHUFFLE.size) {
			return arrCopy(SMALLSHUFFLE[len], len)
		}
		val prime: Int = pickPrime(len)
		val ret = IntArray(len)
		var c: Int = shuffleCompute(0, len, prime)
		for (i in len - 1 downTo 0) {
			c = shuffleCompute(c, len, prime)
			if (ret[c] != 0) {
				// this should never happen, but this is a double-check
				// guarantee that we never miss values in the sequence.
				throw IllegalStateException("Oops")
			}
			ret[c] = i
		}
		return ret
	}
	
	/**
	 * Run a list though the basic paces of operation.
	 * @param list the List<T> to exercise.
	 * @param content the content (in the order) that we expect in the list.
	 */
	protected fun exercise(list: MutableList<T>, vararg content: T) {
		assertTrue(list != null, "List is null")
		assertTrue(content != null, "Content is null")
		assertTrue(content.size == list.size)
		assertTrue(list.toString() != null)
		if (content.size == 0) {
			assertTrue(list.size == 0)
			assertTrue(list.isEmpty())
		} else {
			assertTrue(list.size > 0)
			assertFalse(list.isEmpty())
		}
		
		for (i in 0 until content.size) {
			if (list.get(i) !== content[i]) {
				fail("We expect element in list at position $i to be ${content[i]}")
			}
			val pos: Int = list.indexOf(content[i])
			assertTrue(pos >= 0)
			if (pos != i) {
				// may be duplicates in the list....
				assertTrue(pos < i) // but pos must be first...
				assertEquals(content[pos], content[i])
			}
			assertTrue(list.contains(content[i]))
		}
		
		for (i in content.size - 1 downTo 0) {
			assertTrue(list.get(i) === content[i])
			val pos: Int = list.lastIndexOf(content[i])
			assertTrue(pos >= 0)
			if (pos != i) {
				// may be duplicates in the list....
				assertTrue(pos > i) // but pos must be later...
				assertEquals(content[pos], content[i])
			}
		}
		
		quickCheck(list, content)
		
		run {
			// Check the iteration code.
			// the iterator implementation can be different to the ListIterator
			val it = list.iterator()
			assertFailsWith<IllegalStateException> {
				it.remove()
			}
			var i: Int = 0
			while (i < content.size) {
				assertTrue(it.hasNext())
				assertTrue(it.next() === content[i])
				it.remove()
				assertFailsWith<IllegalStateException> {
					it.remove()
				}
				i++
			}
			assertFalse(it.hasNext())
			assertFailsWith<NoSuchElementException> {
				it.next()
			}
			
			assertFailsWith<IllegalStateException> {
				it.remove()
			}
			
			for (d in content) {
				list.add(d)
			}
			
		}
		
		quickCheck(list, content)
		
		run {
			//Read-Only List iteration through list contents.
			for (origin in 0 .. content.size) {
				// start at every place 
				val li = list.listIterator(origin)
				
				for (i in origin until content.size) {
					assertTrue(li.hasNext())
					assertTrue(li.nextIndex() == i)
					assertTrue(li.previousIndex() == i - 1)
					val emt = li.next()
					assertTrue(content[i] === emt)
				}
				
				assertTrue(li.nextIndex() == content.size)
				assertFalse(li.hasNext())
				assertFailsWith<NoSuchElementException> {
					li.next()
				}
				
				assertTrue(li.previousIndex() == (content.size - 1))
				for (i in content.size - 1 downTo 0) {
					assertTrue(li.previousIndex() == i)
					assertTrue(li.nextIndex() == i + 1)
					assertTrue(li.hasPrevious())
					val emt = li.previous()
					assertTrue(content[i] === emt)
				}
				assertTrue(li.previousIndex() == -1)
				assertFalse(li.hasPrevious())
				assertFailsWith<NoSuchElementException> {
					li.previous()
				}
				
				assertTrue(li.nextIndex() == 0)
				for (i in 0 until origin) {
					assertTrue(li.hasNext())
					assertTrue(li.nextIndex() == i)
					assertTrue(content[i] === li.next())
				}
				
				val fbli = list.listIterator(origin)
				assertTrue(fbli.nextIndex() == origin)
				assertTrue(fbli.previousIndex() == (origin - 1))
				
				
			}
		}
		run {
			//Read/write List iteration through list contents.
			for (origin in 0 .. content.size) {
				// start at every place 
				val li = list.listIterator(origin)
				
				for (i in origin until content.size) {
					assertTrue(li.hasNext())
					assertTrue(li.nextIndex() == i)
					assertTrue(li.previousIndex() == i - 1)
					val emt = li.next()
					assertTrue(content[i] === emt)
					val n: Int = li.nextIndex()
					val p: Int = li.previousIndex()
					li.remove()
					assertTrue(p - 1 == li.previousIndex())
					assertTrue(n - 1 == li.nextIndex())
					assertFailsWith<IllegalStateException> {
						li.remove()
					}
					li.add(emt)
					assertTrue(p == li.previousIndex())
					assertTrue(n == li.nextIndex())
				}
				
				assertTrue(li.nextIndex() == content.size)
				assertFalse(li.hasNext())
				assertFailsWith<NoSuchElementException> {
					li.next()
				}
				
				assertTrue(li.previousIndex() == (content.size - 1))
				for (i in content.size - 1 downTo 0) {
					assertTrue(li.previousIndex() == i)
					assertTrue(li.nextIndex() == i + 1)
					assertTrue(li.hasPrevious())
					val emt = li.previous()
					assertTrue(content[i] === emt)
					val p: Int = li.previousIndex()
					val n: Int = li.nextIndex()
					li.remove()
					assertTrue(p == li.previousIndex())
					assertTrue(n == li.nextIndex())
					assertFailsWith<IllegalStateException> {
						li.remove()
					}
					li.add(emt)
					assertTrue(content[i] === li.previous())
					assertTrue(p == li.previousIndex())
					assertTrue(n == li.nextIndex())
				}
				assertTrue(li.previousIndex() == -1)
				assertFalse(li.hasPrevious())
				assertFailsWith<NoSuchElementException> {
					li.previous()
				}
				
				assertTrue(li.nextIndex() == 0)
				for (i in 0 until origin) {
					assertTrue(li.hasNext())
					assertTrue(li.nextIndex() == i)
					assertTrue(content[i] === li.next())
				}
				
				val fbli = list.listIterator(origin)
				assertTrue(fbli.nextIndex() == origin)
				assertTrue(fbli.previousIndex() == (origin - 1))
				
				
			}
			
			quickCheck(list, content)
		}
		
		run {
			// remove all, and re-add them.
			var it = list.listIterator(0)
			for (i in 0 until content.size) {
				assertTrue(it.hasNext())
				assertTrue(it.next() === content[i])
				it.remove()
			}
			for (i in 0 until content.size) {
				assertFalse(it.hasNext())
				it.add(content[i])
				assertTrue(it.hasPrevious())
				assertFalse(it.hasNext())
				assertTrue(content[i] === it.previous())
				assertTrue(content[i] === it.next())
				assertFalse(it.hasNext())
			}
			assertFalse(it.hasNext())
			quickCheck(list, content)
			
			// then do it backwards.
			it = list.listIterator(content.size)
			for (i in content.size - 1 downTo 0) {
				assertTrue(it.hasPrevious())
				assertTrue(it.previous() === content[i])
				it.remove()
			}
			for (i in content.size - 1 downTo 0) {
				assertFalse(it.hasPrevious())
				it.add(content[i])
				assertTrue(it.hasPrevious())
				assertTrue(content[i] === it.previous())
				assertFalse(it.hasPrevious())
				assertTrue(it.hasNext())
			}
			assertFalse(it.hasPrevious())
			quickCheck(list, content)
							
		}
		
		
		if (content.size > 0) {
			// Check the iterator set() method.
			// first forward....
			
			val li = list.listIterator()
			
			assertFailsWith<IllegalStateException> {
				li.set(content[0])
			}
			
			
			var tmpa = li.next()
			li.remove()
			for (i in 1 until content.size) {
				val tmpb = li.next()
				assertTrue(content[i] === tmpb)
				li.set(tmpa)
				tmpa = tmpb
			}
			li.add(tmpa)
			quickCheck(list, content)
			
			// then backward ....
			assertFailsWith<IllegalStateException> {
				li.set(content[0])
			}
			
			
			tmpa = li.previous()
			li.remove()
			for (i in content.size - 2 downTo 0) {
				val tmpb = li.previous()
				assertTrue(content[i] === tmpb)
				li.set(tmpa)
				tmpa = tmpb
			}
			li.add(tmpa)
			quickCheck(list, content)
		}
		
	}
	
	private fun illegalExercise(list: MutableList<T>, content: Array<T>, d: T, eclass: KClass<out Exception>) {
		
		quickCheck(list, content)
		
		try {
			list.add(d)
			failNoException(eclass)
		} catch (e: Exception) {
			checkException(eclass, e)
		}
		
		try {
			list.addAll(setOf(d))
			failNoException(eclass)
		} catch (e: Exception) {
			checkException(eclass, e)
		}
		
		for (i in list.size - 1 downTo 0) {
			try {
				list.set(i, d)
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}
		}
		
		for (i in list.size downTo 0) {
			try {
				list.add(i, d)
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}

			try {
				list.addAll(i, listOf(d))
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}

			try {
				list.addAll(i, listOf(d))
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}
		}

		
		quickCheck(list, content)
		
		var li = list.listIterator()
		for (i in 0 until list.size) {
			assertTrue(li.hasNext())
			assertTrue(content[i] === li.next())
			try {
				li.add(d)
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}
			// The AbstractList class increments the cursor on the iterator
			// even if the add fails...
			// This poor accounting means that th FilterList (which does the right thing)
			// can't be tested as well... Create this artificail moveTo() to
			// fix the AbstractList failings.
			li = moveTo(list, i)
			
			val prev = li.previous()
			assertTrue(content[i] === prev)
			assertTrue(content[i] === li.next())
			
			try {
				li.set(d)
				failNoException(eclass)
			} catch (e: Exception) {
				checkException(eclass, e)
			}
			// The ContentList class modifies the modcount for set()
			// even if the set() fails...
			// This poor accounting means that the FilterList (which does the right thing)
			// can't be tested as well... Create this artificail moveTo() to
			// fix the AbstractList failings.
			li = moveTo(list, i)
			
		}

		quickCheck(list, content)
		
	}

	private fun moveTo(list: MutableList<T>, i: Int): MutableListIterator<T> {
		val li = list.listIterator()
		var idx = i
		while (idx-- >= 0) {
			assertTrue(li.hasNext())
			li.next()
		}
		return li
	}

	protected fun quickCheck(list: MutableList<T>, content: Array<out T>) {
		// Simple iteration through the list contents.
		val it = list.iterator()
		var i: Int = 0
		while (i < content.size) {
			assertTrue(it.hasNext())
			assertTrue(content[i] === it.next())
			i++
		}
		assertFalse(it.hasNext())
		assertFailsWith<NoSuchElementException> {
			val overflow = it.next()
			fail("There should be no element after hasNext() fails, but we got: " + overflow)
		}
	}
	
	

	/* *********************************
	 * Test the quality of the input data
	 * ********************************* */
	
	@Test fun testSamples() {
		for (s in buildSampleContent()) {
			if (s != null && !i_tclass.isInstance(s)) {
				fail("We expect all sample data to be an instance of " + i_tclass.qualifiedName)
			}
		}
	}
	
	@Test fun testIllegalClassData() {
		for (o in buildIllegalClassContent()) {
			if (o == null || i_tclass.isInstance(o)) {
				fail("We expect all IllegalClass data to be something other than " + i_tclass.qualifiedName)
			}
		}
	}
	
	@Test fun testIllegalArgumentData() {
		for (s in buildIllegalArgumentContent()) {
			if (s != null && !i_tclass.isInstance(s)) {
				fail("We expect all IllegalArgument data to be an instance of " + i_tclass.qualifiedName)
			}
		}
	}
	
	/* *********************************
	 * The actual tests to run.
	 * ********************************* */
	
	@Test fun testToString() {
		// basic run
		assertTrue(buildEmptyList().toString() != null)
	}
	
	@Test fun testEmpty() {
		val empty = buildEmptyList()
		assertTrue(empty.size == 0)
		assertTrue(empty.isEmpty())
		exercise(empty, *buildArray(0))
	}
	
	@Test fun testAdd() {
		for (s in buildSampleContent()) {
			val list = buildEmptyList()
			assertTrue(list.isEmpty())
			assertTrue(list.add(s))
			exercise(list, s)
		}
		val list = buildEmptyList()
		val content: Array<T> = buildSampleContent()
		for (s in content) {
			assertTrue(list.add(s))
		}
		exercise(list, *content)
	}
	
	@Test fun testAddAll() {
		for (s in buildSampleContent()) {
			val list = buildEmptyList()
			assertTrue(list.isEmpty())
			assertTrue(list.addAll(javaAsList(s)))
			exercise(list, s)
			list.clear()
		}
		val list = buildEmptyList()
		val content: Array<T> = buildSampleContent()
		assertTrue(list.addAll(javaAsList(content)))
		exercise(list, *content)
		val data: Array<T> = buildArray(0)
		assertFalse(list.addAll(javaAsList(data)))
	}
	
	@Test fun testAddAllInt() {
		for (s in buildSampleContent()) {
			val list = buildEmptyList()
			assertTrue(list.isEmpty())
			assertTrue(list.addAll(0, javaAsList(s)))
			exercise(list, s)
			list.clear()
		}
		val list = buildEmptyList()
		val content: Array<T> = buildSampleContent()
		assertTrue(list.addAll(0, javaAsList(content)))
		exercise(list, *content)
		val data: Array<T> = buildArray(0)
		assertFalse(list.addAll(0, javaAsList(data)))
	}
	
	@Test fun testIllegalAddAllInt() {
		val illegal: Array<T> = buildIllegalArgumentContent()
		if (illegal.size <= 0) {
			// for android.
			//Assume.assumeTrue(illegal.length > 0);
			return
		}
		val extra: Array<T> = buildAdditionalContent()
		if (extra.size <= 0) {
			// for android.
			//Assume.assumeTrue(extra.length > 0);
			return
		}
		val content: Array<T> = buildSampleContent()
		if (content.size <= 0) {
			// for android.
			//Assume.assumeTrue(content.length > 0);
			return
		}
		val toadd: Array<T> = arrCopy(extra, extra.size + illegal.size)
		illegal.copyInto(toadd, extra.size, 0, 0 + illegal.size)
		
		// right, we have legal content in 'content', and then in 'illegal' we
		// have some legal content, and then some illegal content.
		
		// populate the list.
		val list = buildEmptyList()
		assertTrue(list.addAll(0, javaAsList(content)))
		quickCheck(list, content)
		
		// check that the first to-add can be added.
		list.add(0, toadd[0])
		//then remove it again.
		assertTrue(toadd[0] === list.removeAt(0))
		
		for (i in 0 .. content.size) {
		
			quickCheck(list, content)
			
			// now, add the illegal, and then inspect the list...
			assertFailsWith<IllegalArgumentException> {
				list.addAll(i, javaAsList(toadd))
			}
			
			// make sure that the member that previously could be added can
			// still be added.
			list.add(0, toadd[0])
			//then remove it again.
			assertTrue(toadd[0] === list.removeAt(0))
			
			// make sure it's all OK.
			exercise(list, *content)
			
			if (content.size < 2) {
				// for android.
				// Assume.assumeTrue(content.length >= 2);
				return
			}
			
			// now check to make sure that concurrency is not affected....
			val it = list.iterator()
			// move it along at least once.....
			assertTrue(content[0] === it.next())
			// now do a failed addAll.
			assertFailsWith<IllegalArgumentException> {
				list.addAll(i, javaAsList(toadd))
			}
			// we should be able to move the iteratoe because the modcount should
			// not have been affected.....
			assertTrue(content[1] === it.next())
			
			
			
		}
		
	}
	
	@Test fun testClear() {
		val list = buildEmptyList()
		assertTrue(list.addAll(javaAsList(buildSampleContent())))
		assertFalse(list.isEmpty())
		list.clear()
		assertTrue(list.isEmpty())
	}
	
	@Test fun testInsert() {
		for (s in buildSampleContent()) {
			val list = buildEmptyList()
			assertTrue(list.isEmpty())
			list.add(0, s)
			exercise(list, s)
		}
		val list = buildEmptyList()
		val content: Array<T> = buildSampleContent()
		for (s in content) {
			list.add(0, s)
		}
		exercise(list, *arrayReverse(content))
		list.clear()
		
		val order: IntArray = shuffle(content.size)
		val mix: Array<T> = buildArray(order.size)
		for (i in 0 until order.size) {
			mix[i] = content[order[i]]
		}
		for (i in 0 until content.size) {
			var pos: Int = 0
			for (j in 0 until order.size) {
				if (order[j] < i) {
					pos++
				}
				if (order[j] == i) {
					break
				}
			}
			list.add(pos, content[i])
		}
		exercise(list, *mix)
		
	}
	
	@Test fun testNullAdd() {
		if (i_nullok) {
			val list = buildEmptyList()
			assertTrue(list.add(nullT<T>()))
			assertTrue(list.get(0) == null)
			list.add(1, nullT<T>())
			assertTrue(list.get(1) == null)
			assertTrue(list.addAll(javaAsList(buildArray(10))))
			assertTrue(list.size == 12)
		} else {
			assertFailsWith<NullPointerException> {
				val list = buildEmptyList()
				assertFalse(list.add(nullT<T>()))
			}
			assertFailsWith<NullPointerException> {
				val list = buildEmptyList()
				list.add(0, nullT<T>())
			}
			
			assertFailsWith<NullPointerException> {
				val list = buildEmptyList()
				val data: Array<T> = buildSampleContent()
				for (i in 0 until data.size step 2) {
					list.add(data[i])
					data[i] = nullT<T>()
				}
				if (data.size == 1) {
					data[0] = nullT<T>()
				}
				list.addAll(0, javaAsList(data))
			}
			
			assertFailsWith<NullPointerException> {
				val list = buildEmptyList()
				val data: Array<T> = buildSampleContent()
				for (i in 0 until data.size step 2) {
					list.add(data[i])
					data[i] = nullT<T>()
				}
				if (data.size == 1) {
					data[0] = nullT<T>()
				}
				list.addAll(javaAsList(data))
			}
			
			
		}
		
		assertFailsWith<NullPointerException> {
			val list = buildEmptyList()
			val data: MutableCollection<T> = nullT()
			list.addAll(0, data)
		}
		
		assertFailsWith<NullPointerException> {
			val list = buildEmptyList()
			val data: MutableCollection<T> = nullT()
			list.addAll(javaAsList(buildSampleContent()))
			list.addAll(data)
		}
	}
	
	@Test fun testIllegalIndex() {
		val data: Array<T> = buildSampleContent()
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.add(-10, data[0])
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.add(1, data[0])
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.addAll(-10, javaAsList(data))
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.addAll(1, javaAsList(data))
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.set(-1, data[0])
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.set(1, data[0])
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.get(-1)
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.get(1)
		}

		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.removeAt(-10)
		}
		assertFailsWith<IndexOutOfBoundsException> {
			val list = buildEmptyList()
			list.removeAt(1)
		}
	}
	
	@Test fun testRemove() {
		for (s in buildSampleContent()) {
			val list = buildEmptyList()
			assertTrue(list.isEmpty())
			assertTrue(list.add(s))
			exercise(list, s)
			assertTrue(list.removeAt(0) === s)
			exercise(list)
		}
		val samplecontent: Array<T> = buildSampleContent()
		for (i in 0 until samplecontent.size) {
			var content: Array<T> = samplecontent
			val list = buildEmptyList()
			for (s in content) {
				assertTrue(list.add(s))
			}
			exercise(list, *content)
			assertTrue(content[i] === list.removeAt(i))
			content = arrayRemove(content, i)
			exercise(list, *content)
			
			while (list.size > i) {
				assertTrue(content[i] === list.removeAt(i))
				content = arrayRemove(content, i)
			}
			while (!list.isEmpty()) {
				assertTrue(content[content.size -1] === list.removeAt(list.size - 1))
				content = arrayRemove(content, content.size - 1)
			}
			assertTrue(list.isEmpty())
		}
		var list = buildEmptyList()
		for (s in samplecontent) {
			assertTrue(list.add(s))
		}
		exercise(list, *samplecontent)
		if (i_nullok) {
			list = buildEmptyList()
			assertTrue(list.add(nullT<T>()))
			assertTrue(list.get(0) == null)
		}
	}
	
	
	@Test fun testIllegalArgumentContent() {
		for (d in buildIllegalArgumentContent()) {
			val list = buildEmptyList()
			val content: Array<T> = buildSampleContent()
			for (i in content) {
				list.add(i)
			}
			illegalExercise(list, content, d, IllegalArgumentException::class)
		}
	}

	@Test fun testIllegalClassContent() {
		if (!checksErasedCasts) {
			return // JVM only: Kotlin/Native does not check an erased cast at the typed call
		}
		for (d in buildIllegalClassContent()) {
			val list = buildEmptyList()
			val content: Array<T> = buildSampleContent()
			for (i in content) {
				list.add(i)
			}
			illegalExercise(list, content, (d as T), ClassCastException::class)
		}
	}

	@Test fun testNullContent() {
		if (i_nullok) {
			return
		}
		val list = buildEmptyList()
		val content: Array<T> = buildSampleContent()
		for (i in content) {
			list.add(i)
		}
		illegalExercise(list, content, nullT<T>(), NullPointerException::class)
	}


	@Test fun testConcurrentMod() {
		val list = buildEmptyList()
		
		val sample: Array<T> = buildSampleContent()
		assertTrue(sample.size > 2, "Not enough sample data " + sample.size)
		
		list.add(sample[0])
		
		var it = list.listIterator()
		val si = list.iterator()
		
		// It is important to add some content, and do a next()
		// before testing concurrency, because some iterator methods
		// check for the Iterator state before checking concurrency.
		// this puts the iterator in to a valid state for remove(), set(), etc.
		assertTrue(sample[0] === it.next())
		assertTrue(sample[0] === si.next())
		
		// create a concurrent issue.
		// we have checked for more than 2 elements so this should be fine.
		list.add(sample[1])
		list.add(sample[2])
		
		// hasNext() should never throw CME.
		assertTrue(it.hasNext())
		assertTrue(1 == it.nextIndex())
		
		// hasNext() should never throw CME.
		assertTrue(it.hasNext())
		assertFailsWith<ConcurrentModificationException> {
			it.next()
		}

		
		it = list.listIterator(3)
		assertTrue(sample[2] === it.previous())
		
		// create a concurrent issue.
		list.removeAt(2)
		
		// hasPrevious() should never throw CME.
		assertTrue(it.hasPrevious())
		
		assertTrue(1 == it.previousIndex())

		// hasPrevious() should never throw CME.
		assertTrue(it.hasPrevious())
		assertFailsWith<ConcurrentModificationException> {
			it.previous()
		}
		
		// hasPrevious() should never throw CME.
		assertTrue(it.hasPrevious())
		assertFailsWith<ConcurrentModificationException> {
			it.set(sample[sample.size - 1])
		}

		// hasPrevious() should never throw CME.
		assertTrue(it.hasPrevious())
		assertFailsWith<ConcurrentModificationException> {
			it.add(sample[sample.size - 1])
		}

		// hasPrevious() should never throw CME.
		assertTrue(it.hasPrevious())
		assertFailsWith<ConcurrentModificationException> {
			it.remove()
		}

		
		
		// Now test the Simple Iterator concurrency
		
		// hasNext() should never throw CME.
		assertTrue(si.hasNext())
		
		assertFailsWith<ConcurrentModificationException> {
			si.next()
		}

		assertFailsWith<ConcurrentModificationException> {
			si.remove()
		}
		
	}
	
	@Test fun testConcurrentSetMod() {
		val list = buildEmptyList()
		
		val sample: Array<T> = buildSampleContent()
		assertTrue(sample.size > 2, "Not enough sample data " + sample.size)
		
		list.addAll(javaAsList(sample))
		quickCheck(list, sample)
		
		val tmp = list.removeAt(0)
		
		val tmpsamp: Array<T> = sample.copyOfRange(1, sample.size) as Array<T>
		quickCheck(list, tmpsamp)

		// get a handle on our iterator....
		val it = list.listIterator()
		
		// make sure a set of the underlying does not effect the iterator.
		tmpsamp[0] = tmp
		list.set(0, tmp)
		quickCheck(list, tmpsamp)
		
		// next() should not throw CME because set() should not change modCount.
		assertTrue(tmp === it.next())
		
		// then, for kicks, restore the original list with
		it.add(sample[1])
		quickCheck(list, sample)
	}
	
	
}
