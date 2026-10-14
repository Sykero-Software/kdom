package fi.sykero.kdom.query

import fi.sykero.kdom.Content
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.Parent
import fi.sykero.kdom.Text

/*
 * XPath-replacing axis helpers (new kdom code, no jdom2 counterpart). They reproduce what jaxen returned for the
 * expressions `:app` evaluated through jdom2's XPathFactory: results are in document order unless the function says
 * "nearest first" (XPath's reverse axes), name tests compare the namespace URI (never the prefix), and the double-slash descendant step is
 * absolute: [xpathRoot] is the node it searches from, whatever the context node.
 *
 * `Sequence` results are lazy; do not consume one after mutating the tree it walks.
 */

/**
 * XPath's root node for the double-slash step from this node: a Document is its own root; an attached Element's root is
 * its Document. A detached Element, or any node of a detached subtree, has NO root (null): jaxen on jdom2 returns an
 * empty result for an absolute path evaluated from a node of a detached tree, and so does `findAll`. Callers that need
 * the descendants of a detached element use [descendantElements] (the relative axes work on detached trees).
 */
public fun Parent.xpathRoot(): Document? = this as? Document ?: document

/**
 * Every element of the document, as the XPath `//` + `*` step: the root element and all its descendants in document
 * order; empty if the document has no root element. Call on [xpathRoot].
 */
public fun Document.allElements(): Sequence<Element> = descendantElements()

/** `descendant::*` (excluding self). */
public fun Parent.descendantElements(): Sequence<Element> = descendantContent().filterIsInstance<Element>()

/** `descendant::text()`: Text and CDATA nodes. */
public fun Parent.descendantTexts(): Sequence<Text> = descendantContent().filterIsInstance<Text>()

/** `ancestor::*`, nearest first. */
public fun Element.ancestorElements(): Sequence<Element> = generateSequence(parentElement) { it.parentElement }

/** `*` (child elements). */
public fun Element.childElements(): Sequence<Element> = contentSequence().filterIsInstance<Element>()

/** `following-sibling::*`. */
public fun Element.followingSiblingElements(): Sequence<Element> = sequence {
    val p = parent ?: return@sequence
    var i = p.indexOf(this@followingSiblingElements) + 1
    while (i < p.contentSize) {
        val c = p.getContent(i++)
        if (c is Element) yield(c)
    }
}

/** `preceding-sibling::*`, nearest first, like XPath's reverse axis. */
public fun Element.precedingSiblingElements(): Sequence<Element> = sequence {
    val p = parent ?: return@sequence
    var i = p.indexOf(this@precedingSiblingElements) - 1
    while (i >= 0) {
        val c = p.getContent(i--)
        if (c is Element) yield(c)
    }
}

/**
 * Name test `ns:local` with the namespace matched by URI (XPath semantics, the prefix is irrelevant), or no-namespace
 * when [ns] is [Namespace.NO_NAMESPACE].
 */
public fun Element.isNamed(localName: String, ns: Namespace): Boolean =
    name == localName && namespaceURI == ns.uri

/** `@name`: has a no-namespace attribute called [name]. */
public fun Element.hasAttribute(name: String): Boolean = getAttribute(name) != null

/** `@name='value'`: a no-namespace attribute called [name] has exactly [value]. */
public fun Element.attributeIs(name: String, value: String): Boolean = getAttribute(name)?.value == value

/** Convenience for the common `//ns:name` from any context (absolute, like XPath). */
public fun Parent.findAll(localName: String, ns: Namespace): List<Element> =
    xpathRoot()?.allElements()?.filter { it.isNamed(localName, ns) }?.toList() ?: emptyList()

private fun Parent.contentSequence(): Sequence<Content> = sequence {
    var i = 0
    while (i < contentSize) yield(getContent(i++))
}

/** Pre-order (document order) walk of everything below this node, with an explicit stack (no recursion depth limit). */
private fun Parent.descendantContent(): Sequence<Content> = sequence {
    val parents = ArrayList<Parent>()
    val indexes = ArrayList<Int>()
    parents.add(this@descendantContent)
    indexes.add(0)
    while (parents.isNotEmpty()) {
        val top = parents.size - 1
        val p = parents[top]
        val i = indexes[top]
        if (i >= p.contentSize) {
            parents.removeAt(top)
            indexes.removeAt(top)
            continue
        }
        indexes[top] = i + 1
        val c = p.getContent(i)
        yield(c)
        if (c is Element) {
            parents.add(c)
            indexes.add(0)
        }
    }
}
