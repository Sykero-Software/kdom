/* Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software. */
package fi.sykero.kdom.test.util

import fi.sykero.kdom.Attribute
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.NamespaceAware
import fi.sykero.kdom.ProcessingInstruction
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail

/** `Arrays.asList(array)`: a list of the array's elements. */
@kotlin.jvm.JvmName("javaAsListArray")
fun <T> javaAsList(a: Array<out T>): MutableList<T> = a.toMutableList()

/** `Arrays.asList(a, b, ...)`. */
fun <T> javaAsList(vararg a: T): MutableList<T> = a.toMutableList()

/**
 * Common test helpers translated from jdom2's `UnitTestUtil`. The JVM-only parts (Java serialization, the `Class`
 * based exception checks used by the remaining Java tests, the Android switch) live in `JvmTestUtil` (jvmTest).
 */
object UnitTestUtil {

    @JvmStatic
    fun testNamespaceIntro(content: NamespaceAware, vararg expect: Namespace) {
        val intro = content.namespacesIntroduced
        val exp = expect.toList()

        if (exp != intro) {
            fail("We expect Introduced equal the Expected Namespaces." +
                    "\n   Introduced: " + intro.toString() +
                    "\n   Expected:   " + exp.toString())
        }
    }

    @JvmStatic
    fun testNamespaceScope(content: NamespaceAware, vararg expect: Namespace) {
        val introduced = content.namespacesIntroduced
        val inscope = ArrayList<Namespace>(content.namespacesInScope)
        val inherited = content.namespacesInherited

        if (introduced.size + inherited.size != inscope.size) {
            fail("InScope Namespaces do not add up: InScope=${inscope.size} Inherited=${inherited.size}, " +
                    "Introduced=${introduced.size}")
        }
        var inher = 0
        var intro = 0
        for (ns in inscope) {
            if (inher < inherited.size && inherited[inher] === ns) {
                inher++
            }
            if (intro < introduced.size && introduced[intro] === ns) {
                intro++
            }
        }
        if (intro < introduced.size) {
            // we are missing content(or it is out of order) in the lists.
            fail("Introduced list contains out-of-order, or non-intersecting Namespaces." +
                    "\n   InScope:    " + inscope.toString() +
                    "\n   Introduced: " + introduced.toString())
        }
        if (inher < inherited.size) {
            // we are missing content(or it is out of order) in the lists.
            fail("Inherited list contains out-of-order, or non-intersecting Namespaces." +
                    "\n   InScope:   " + inscope.toString() +
                    "\n   Inherited: " + inherited.toString())
        }

        var i = 0
        for (ns in inscope) {
            if (i >= expect.size) {
                fail("We have additional Namespaces " + inscope.subList(i, inscope.size))
            }
            if (expect[i] !== ns) {
                assertEquals(expect[i], ns, "InScope differs from Expected at index $i")
            }
            i++
        }
        if (i < expect.size) {
            val missing = ArrayList<Namespace>(expect.size - i)
            while (i < expect.size) {
                missing.add(expect[i++])
            }
            fail("InScope is missing Namespaces $missing")
        }

        inscope.removeAll(introduced)
        if (inscope != inherited) {
            fail("We expect InScope - Introduced to equal Inherited." +
                    "\n   InScope:   " + inscope.toString() +
                    "\n   Inherited: " + inherited.toString())
        }
    }

    /**
     * Attribute order is not significant in XML documents.
     * This method re-arranges the order of the Attributes (in somewhat
     * alphabetical order, but that's not important) so that you can easily
     * compare the attribute content of two different elements.
     * @param emt The element who's Attributes we should rearrange.
     */
    @JvmStatic
    fun normalizeAttributes(emt: Element) {
        if (!emt.hasAttributes()) {
            return
        }
        emt.sortAttributes(object : Comparator<Attribute> {
            override fun compare(o1: Attribute, o2: Attribute): Int {
                return o1.qualifiedName.compareTo(o2.qualifiedName)
            }
        })
        for (o in emt.children) {
            normalizeAttributes(o)
        }
    }

    /**
     * Test whether two values are equals, in addition, check the hashCode() values
     * This method will pass if both values are null.
     * @param a The first value to check
     * @param b The other value
     */
    @JvmStatic
    fun checkEquals(a: Any?, b: Any?) {
        if (a == null) {
            if (b != null) {
                fail("Second value is not null like the first: $b")
            }
            return
        }
        if (b == null) {
            fail("First value is not null like the second: $a")
        }
        assertTrue(a.equals(a))
        assertTrue(b.equals(b))
        if (!a.equals(b)) {
            fail("First value '$a' does not equals() second value '$b'.")
        }
        if (!b.equals(a)) {
            fail("Second value '$b' does not equals() first value '$a'.")
        }
        if (a.hashCode() != b.hashCode()) {
            fail("Hashcodes of equals() values are different. " +
                    "First value '" + a + "' (hashcode=" + a.hashCode() + ") " +
                    "not same as " +
                    "second value '" + b + "' (hashcode=" + b.hashCode() + ").")
        }
    }

    @JvmStatic
    fun testReadIterator(it: Iterator<*>?, vararg values: Any?) {
        assertTrue(it != null)
        for (i in values.indices) {
            assertTrue(it.hasNext())
            assertTrue(values[i] === it.next())
        }
        assertFalse(it.hasNext(), "Not enough values in the test set.")
        try {
            it.next()
            fail("Should not be able to have next after values have run out.")
        } catch (nsee: NoSuchElementException) {
            // the way it should be
        } catch (e: Exception) {
            fail("next() should fail with NoSuchElementException, not " + e::class.qualifiedName)
        }
    }

    /**
     * Create a new String instance based on an input String.
     * The String class tends to have these fancy ways of re-using internal
     * data structures when creating one string from another. This method
     * circumvents those optimisations.
     * @param value The value to clone
     * @return The cloned version of the input value.
     */
    @JvmStatic
    fun cloneString(value: String): String {
        val chars = value.toCharArray()
        val ret = CharArray(chars.size)
        chars.copyInto(ret, 0, 0, chars.size)
        return ret.concatToString()
    }

    /**
     * Provide a standard way to fail when we expect an exception but did not
     * get one...
     */
    @JvmStatic
    fun failNoException(expect: KClass<out Throwable>) {
        fail("This code was expected to produce the exception " +
                expect.qualifiedName + " but it was not thrown.")
    }

    /**
     * Provide a standard test for checking for an exception.
     * @param expect
     * @param got
     */
    @JvmStatic
    fun checkException(expect: KClass<out Throwable>, got: Throwable?) {
        if (got == null) {
            fail("We expected an exception of type " + expect.qualifiedName +
                    " but got a null value instead")
        }
        if (!expect.isInstance(got)) {
            fail("We expected an exception of type " + expect.qualifiedName +
                    " but got a " + got::class.qualifiedName +
                    " instead with message " + got.message, got)
        }
    }

    /**
     * Call this if you have an exception you want to fail for!
     * @param message
     * @param got this exception
     */
    @JvmStatic
    fun failException(message: String, got: Throwable) {
        fail(message, got)
    }

    @JvmStatic
    fun compare(ap: NamespaceAware?, bp: NamespaceAware?) {
        assertNotNull(ap)
        assertNotNull(bp)
        assertEquals(ap::class, bp::class)
        assertTrue(ap !== bp)
        if (ap is Content) {
            val a = ap
            val b = bp as Content
            if (a.parent != null) {
                assertTrue(a.parent !== b.parent)
            }

            when (a.cType) {
                Content.CType.Text -> assertEquals(a.value, b.value)
                Content.CType.CDATA -> assertEquals(a.value, b.value)
                Content.CType.Comment -> assertEquals((a as Comment).text, (b as Comment).text)
                Content.CType.DocType -> {
                    val da = a as DocType
                    val db = b as DocType
                    assertEquals(da.elementName, db.elementName)
                    assertEquals(da.publicID, db.publicID)
                    assertEquals(da.systemID, db.systemID)
                    assertEquals(da.internalSubset, db.internalSubset)
                }
                Content.CType.Element -> {
                    val ea = a as Element
                    val eb = b as Element
                    assertEquals(ea.name, eb.name)
                    compare(ea.attributes, eb.attributes)
                    val ean = ea.namespacesInScope
                    val ebn = eb.namespacesInScope
                    assertEquals(ean, ebn)
                    val sz = ea.contentSize
                    if (sz != eb.contentSize) {
                        fail("Expected $sz members but got ${eb.contentSize} for Element $ea")
                    }
                    for (i in 0 until sz) {
                        compare(ea.getContent(i), eb.getContent(i))
                    }
                }
                Content.CType.EntityRef -> assertEquals((a as EntityRef).name, (b as EntityRef).name)
                Content.CType.ProcessingInstruction -> {
                    val pa = a as ProcessingInstruction
                    val pb = b as ProcessingInstruction
                    assertEquals(pa.target, pb.target)
                    assertEquals(pa.data, pb.data)
                }
            }
        } else if (ap is Attribute) {
            compare(ap, bp as Attribute)
        } else if (ap is Document) {
            val a = ap
            val b = bp as Document
            assertEquals(a.baseURI, b.baseURI)
            val sz = a.contentSize
            if (sz != b.contentSize) {
                fail("We expected $sz members in the Document content, but got ${b.contentSize}")
            }
            for (i in 0 until sz) {
                compare(a.getContent(i), b.getContent(i))
            }
        }
    }

    @JvmStatic
    fun compare(a: List<Attribute>, b: List<Attribute>) {
        assertTrue(a !== b)
        if (a.size != b.size) {
            fail("Expected same number of attributes (a has ${a.size} and b has ${b.size}): a=$a and b=$b")
        }
        val ait = a.iterator()
        val bit = b.iterator()
        while (ait.hasNext() && bit.hasNext()) {
            val aa = ait.next()
            val bb = bit.next()
            compare(aa, bb)
        }
        assertFalse(ait.hasNext())
        assertFalse(bit.hasNext())
    }

    @JvmStatic
    fun compare(aa: Attribute, bb: Attribute) {
        assertEquals(aa.name, bb.name)
        assertTrue(aa.namespace === bb.namespace)
        assertEquals(aa.value, bb.value)
    }
}
