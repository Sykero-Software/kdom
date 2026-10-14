package fi.sykero.kdom

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** The common-collection replacements of Task 7 (read-only views, TreeMap order, the JVM sort override). */
class CommonCollectionsTest {
    /** Mutates through the Java `List` interface, as a Java caller would (a Kotlin `MutableList` cast is refused). */
    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun <T> mutate(list: List<T>, extra: T) = (list as java.util.List<T>).add(extra)

    @Test fun readOnlyViewsRejectMutation() {
        val e = Element("e", "p", "urn:p")
        e.addNamespaceDeclaration(Namespace.getNamespace("q", "urn:q"))
        Document(e)
        val ns = Namespace.getNamespace("z", "urn:z")
        assertFailsWith<UnsupportedOperationException> { mutate(e.additionalNamespaces, ns) }
        assertFailsWith<UnsupportedOperationException> { mutate(e.namespacesInScope, ns) }
        assertFailsWith<UnsupportedOperationException> { mutate(e.namespacesIntroduced, ns) }
        assertFailsWith<UnsupportedOperationException> { mutate(e.namespacesInherited, ns) }
        assertFailsWith<UnsupportedOperationException> { mutate(e.document!!.namespacesInScope, ns) }
        @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
        assertFailsWith<UnsupportedOperationException> { (e.namespacesInScope as java.util.List<*>).remove(0) }
    }

    /** jdom2 ordered the in-scope namespaces after the element's own one by prefix (a TreeMap). */
    @Test fun namespacesInScopeOrderMatchesJdom2() {
        val prefixes = listOf("m", "b", "z", "a", "k")
        val k = Element("root", "urn:default")
        val j = org.jdom2.Element("root", "urn:default")
        for (p in prefixes) {
            k.addNamespaceDeclaration(Namespace.getNamespace(p, "urn:$p"))
            j.addNamespaceDeclaration(org.jdom2.Namespace.getNamespace(p, "urn:$p"))
        }
        val kc = Element("c", "y", "urn:y").also { k.addContent(it) }
        val jc = org.jdom2.Element("c", "y", "urn:y").also { j.addContent(it) }
        kc.setAttribute("x", "1", Namespace.getNamespace("c", "urn:c"))
        jc.setAttribute("x", "1", org.jdom2.Namespace.getNamespace("c", "urn:c"))
        assertEquals(j.namespacesInScope.map { it.prefix }, k.namespacesInScope.map { it.prefix })
        assertEquals(jc.namespacesInScope.map { it.prefix }, kc.namespacesInScope.map { it.prefix })
        assertEquals(
            jc.getAttribute("x", org.jdom2.Namespace.getNamespace("c", "urn:c")).namespacesInScope.map { it.prefix },
            kc.getAttribute("x", Namespace.getNamespace("c", "urn:c"))!!.namespacesInScope.map { it.prefix },
        )
    }

    /** On the JVM the live lists keep jdom2's `List.sort(Comparator)` override (Kotlin's sortWith calls it). */
    @Test fun liveListSortOnJvm() {
        val e = Element("e")
        for (n in listOf("c", "a", "b")) e.addContent(Element(n))
        e.children.sortWith(compareBy { it.name })
        assertEquals(listOf("a", "b", "c"), e.children.map { it.name })
        e.setAttribute("y", "1")
        e.setAttribute("x", "2")
        e.attributes.sortWith(compareBy { it.name })
        assertEquals(listOf("x", "y"), e.attributes.map { it.name })
    }
}
