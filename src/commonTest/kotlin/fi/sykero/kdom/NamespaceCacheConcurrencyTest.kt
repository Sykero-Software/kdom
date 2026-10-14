package fi.sykero.kdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Pins the canonical-instance contract of the global [Namespace] cache (jdom2: a `ConcurrentHashMap`):
 * the same prefix and URI always give the same instance, also when many threads ask at once.
 */
class NamespaceCacheConcurrencyTest {
    @Test fun sameInstanceForSamePrefixAndUri() {
        val a = Namespace.getNamespace("p", "urn:x")
        val b = Namespace.getNamespace("p", "urn:x")
        assertSame(a, b)
    }

    @Test fun constantsAreCanonical() {
        assertSame(Namespace.NO_NAMESPACE, Namespace.getNamespace("", ""))
        assertSame(Namespace.XML_NAMESPACE, Namespace.getNamespace("xml", "http://www.w3.org/XML/1998/namespace"))
    }

    /** Many workers ask for the same 50 namespaces at once; every worker must see the same instances. */
    @Test fun concurrentLookupsStayCanonical() {
        val results = runConcurrently(workers = 8) { (0 until 50).map { Namespace.getNamespace("p$it", "urn:$it") } }
        val first = results.first()
        for (r in results) for (i in r.indices) assertSame(first[i], r[i])
        assertEquals(50, first.toSet().size)
    }
}
