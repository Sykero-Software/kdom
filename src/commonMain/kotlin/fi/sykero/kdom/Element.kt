/*-- 

 Copyright (C) 2000-2012 Jason Hunter & Brett McLaughlin.
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions, and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions, and the disclaimer that follows 
    these conditions in the documentation and/or other materials 
    provided with the distribution.

 3. The name "JDOM" must not be used to endorse or promote products
    derived from this software without prior written permission.  For
    written permission, please contact <request_AT_jdom_DOT_org>.

 4. Products derived from this software may not be called "JDOM", nor
    may "JDOM" appear in their name, without prior written permission
    from the JDOM Project Management <request_AT_jdom_DOT_org>.

 In addition, we request (but do not require) that you include in the 
 end-user documentation provided with the redistribution and/or in the 
 software itself an acknowledgement equivalent to the following:
     "This product includes software developed by the
      JDOM Project (http://www.jdom.org/)."
 Alternatively, the acknowledgment may be graphical using the logos 
 available at http://www.jdom.org/images/logos.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED.  IN NO EVENT SHALL THE JDOM AUTHORS OR THE PROJECT
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 This software consists of voluntary contributions made by many 
 individuals on behalf of the JDOM Project and was originally 
 created by Jason Hunter <jhunter_AT_jdom_DOT_org> and
 Brett McLaughlin <brett_AT_jdom_DOT_org>.  For more information
 on the JDOM Project, please see <http://www.jdom.org/>.

 Kotlin Multiplatform translation for kdom (fi.sykero.kdom), 2026, Sykerö Software.

 */

package fi.sykero.kdom

import kotlin.jvm.JvmSynthetic
import fi.sykero.kdom.internal.unmodifiableList
import fi.sykero.kdom.internal.unsafeNull

import fi.sykero.kdom.ContentList.FilterList
import fi.sykero.kdom.JDOMConstants.Companion.NS_PREFIX_DEFAULT
import fi.sykero.kdom.JDOMConstants.Companion.NS_PREFIX_XML
import fi.sykero.kdom.filter.ElementFilter
import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.util.IteratorIterable

/**
 * An XML element. Methods allow the user to get and manipulate its child
 * elements and content, directly access the element's textual content,
 * manipulate its attributes, and manage namespaces.
 *
 * See [NamespaceAware] and [namespacesInScope] for more
 * details on what the Namespace scope is and how it is managed in JDOM and
 * specifically by this Element class.
 *
 * @see NamespaceAware
 * @see Content
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Lucas Gonze
 * @author  Kevin Regan
 * @author  Dan Schaffer
 * @author  Yusuf Goolamabbas
 * @author  Kent C. Johnson
 * @author  Jools Enticknap
 * @author  Alex Rosen
 * @author  Bradley S. Huffman
 * @author  Victor Toni
 * @author  Rolf Lear
 *
 */
open class Element : Content, Parent {

    /**
     * Returns the (local) name of the element (without any namespace prefix).
     * (The local name of the element; `null` for a raw instance made by the protected no-args
     * constructor. Was the protected field `name`.)
     *
     * @return                     local element name
     */
    var name: String
        get() = unsafeNull(nameField)
        @JvmSynthetic
        set(v) {
            setName(v)
        }

    private var nameField: String? = null

    /**
     * Returns the element's [Namespace].
     * (The namespace of the element. Was the protected field `namespace`; not initialised for a raw
     * instance made by the protected no-args constructor.)
     *
     * @return                     the element's namespace
     */
    var namespace: Namespace
        get() = unsafeNull(namespaceField)
        @JvmSynthetic
        set(v) {
            setNamespace(v)
        }

    private var namespaceField: Namespace? = null

    /** Additional namespace declarations to store on this element; useful
     * during output
     * (Was the package-private field `additionalNamespaces`; renamed because that is now the property.)
     */
    private var additionalNamespaceList: MutableList<Namespace>? = null

    /**
     *  The attributes of the element.  Subclassers have to
     * track attributes using their own mechanism.
     */
    private var attributeListField: AttributeList? = null // = new AttributeList(this);

    /**
     * The content of the element.  Subclassers have to
     * track content using their own mechanism.
     * (Was the package-private field `content`; renamed because that is now the property.)
     */
    private var contentList: ContentList = ContentList(this)

    /**
     * This protected constructor is provided in order to support an Element
     * subclass that wants full control over variable initialization. It
     * intentionally leaves all instance variables null, allowing a lightweight
     * subclass implementation. The subclass is responsible for ensuring all the
     * get and set methods on Element behave as documented.
     *
     * When implementing an Element subclass which doesn't require full control
     * over variable initialization, be aware that simply calling super() (or
     * letting the compiler add the implicit super() call) will not initialize
     * the instance variables which will cause many of the methods to throw a
     * NullPointerException. Therefore, the constructor for these subclasses
     * should call one of the public constructors so variable initialization is
     * handled automatically.
     */
    protected constructor() : super(CType.Element)

    /**
     * Creates a new element with the supplied (local) name and namespace. If
     * the provided namespace is null, the element will have no namespace.
     *
     * @param  name                 local name of the element
     * @param  namespace            namespace for the element
     * @throws IllegalNameException if the given name is illegal as an element
     *                              name
     */
    constructor(name: String?, namespace: Namespace?) : super(CType.Element) {
        setName(name)
        setNamespace(namespace)
    }

    /**
     * Create a new element with the supplied (local) name and no namespace.
     *
     * @param  name                 local name of the element
     * @throws IllegalNameException if the given name is illegal as an element
     *                              name.
     */
    constructor(name: String?) : this(name, null as Namespace?)

    /**
     * Creates a new element with the supplied (local) name and a namespace
     * given by a URI. The element will be put into the unprefixed (default)
     * namespace.
     *
     * @param  name                 name of the element
     * @param  uri                  namespace URI for the element
     * @throws IllegalNameException if the given name is illegal as an element
     *                              name or the given URI is illegal as a
     *                              namespace URI
     */
    constructor(name: String?, uri: String?) : this(name, Namespace.getNamespace(NS_PREFIX_DEFAULT, uri))

    /**
     * Creates a new element with the supplied (local) name and a namespace
     * given by the supplied prefix and URI combination.
     *
     * @param  name                 local name of the element
     * @param  prefix               namespace prefix
     * @param  uri                  namespace URI for the element
     * @throws IllegalNameException if the given name is illegal as an element
     *                              name, the given prefix is illegal as a
     *                              namespace prefix, or the given URI is
     *                              illegal as a namespace URI
     */
    constructor(name: String?, prefix: String?, uri: String?) : this(name, Namespace.getNamespace(prefix, uri))

    /**
     * Sets the (local) name of the element.
     *
     * @param  name                 the new (local) name of the element
     * @return                      the target element
     * @throws IllegalNameException if the given name is illegal as an Element
     *                              name
     */
    open fun setName(name: String?): Element {
        val reason = Verifier.checkElementName(name)
        if (reason != null) {
            throw IllegalNameException(name, "element", reason)
        }
        this.nameField = name
        return this
    }

    /**
     * Sets the element's [Namespace]. If the provided namespace is null,
     * the element will have no namespace.
     *
     * @param  namespace           the new namespace. A null implies Namespace.NO_NAMESPACE.
     * @return                     the target element
     * @throws IllegalAddException if there is a Namespace conflict
     */
    open fun setNamespace(namespace: Namespace?): Element {
        var namespace = namespace
        if (namespace == null) {
            namespace = Namespace.NO_NAMESPACE
        }

        if (additionalNamespaceList != null) {
            val reason = Verifier.checkNamespaceCollision(
                namespace,
                additionalNamespaces
            )
            if (reason != null) {
                throw IllegalAddException(this, namespace, reason)
            }
        }
        if (hasAttributes()) {
            for (a in attributes) {
                val reason =
                    Verifier.checkNamespaceCollision(namespace, a)
                if (reason != null) {
                    throw IllegalAddException(this, namespace, reason)
                }
            }
        }

        this.namespaceField = namespace
        return this
    }

    /**
     * Returns the namespace prefix of the element or an empty string if none
     * exists.
     *
     * @return                     the namespace prefix
     */
    val namespacePrefix: String
        get() = namespace.prefix

    /**
     * Returns the namespace URI mapped to this element's prefix (or the
     * in-scope default namespace URI if no prefix). If no mapping is found, an
     * empty string is returned.
     *
     * @return                     the namespace URI for this element
     */
    val namespaceURI: String
        get() = namespace.uri

    /**
     * Returns the [Namespace] corresponding to the given prefix in scope
     * for this element. This involves searching up the tree, so the results
     * depend on the current location of the element. Returns null if there is
     * no namespace in scope with the given prefix at this point in the
     * document.
     *
     * @param  prefix              namespace prefix to look up
     * @return                     the Namespace for this prefix at this
     *                             location, or null if none
     */
    fun getNamespace(prefix: String?): Namespace? {
        if (prefix == null) {
            return null
        }

        if (NS_PREFIX_XML == prefix) {
            // Namespace "xml" is always bound.
            return Namespace.XML_NAMESPACE
        }

        // Check if the prefix is the prefix for this element
        if (prefix == namespacePrefix) {
            return namespace
        }

        // Scan the additional namespaces
        if (additionalNamespaceList != null) {
            for (i in 0 until additionalNamespaceList!!.size) {
                val ns = additionalNamespaceList!![i]
                if (prefix == ns.prefix) {
                    return ns
                }
            }
        }

        if (attributeListField != null) {
            for (a in attributeListField!!) {
                if (prefix == a.namespacePrefix) {
                    return a.namespace
                }
            }
        }

        // If we still don't have a match, ask the parent
        if (parentRef is Element) {
            return (parentRef as Element).getNamespace(prefix)
        }

        return null
    }

    /**
     * Returns the full name of the element, in the form
     * [namespacePrefix]:[localName]. If the element does not have a namespace
     * prefix, then the local name is returned.
     *
     * @return                     qualified name of the element (including
     *                             namespace prefix)
     */
    val qualifiedName: String
        get() {
            // Note: Any changes here should be reflected in
            // XMLOutputter.printQualifiedName()
            if ("" == namespace.prefix) {
                return name
            }

            return StringBuilder(namespace.prefix)
                .append(':')
                .append(name)
                .toString()
        }

    /**
     * Adds a namespace declarations to this element. This should *not* be
     * used to add the declaration for this element itself; that should be
     * assigned in the construction of the element. Instead, this is for adding
     * namespace declarations on the element not relating directly to itself.
     * It's used during output to for stylistic reasons move namespace
     * declarations higher in the tree than they would have to be.
     *
     * @param  additionalNamespace namespace to add
     * @return true if the namespace is added (false if it was previously added)
     * @throws IllegalAddException if the namespace prefix collides with another
     *                             namespace prefix on the element
     */
    fun addNamespaceDeclaration(additionalNamespace: Namespace): Boolean {

        if (additionalNamespaceList == null) {
            additionalNamespaceList = ArrayList<Namespace>(INITIAL_ARRAY_SIZE)
        }

        for (ns in additionalNamespaceList!!) {
            if (ns === additionalNamespace) {
                return false
            }
        }

        // Verify the new namespace prefix doesn't collide with another
        // declared namespace, an attribute prefix, or this element's prefix
        val reason = Verifier.checkNamespaceCollision(additionalNamespace, this)
        if (reason != null) {
            throw IllegalAddException(this, additionalNamespace, reason)
        }

        return additionalNamespaceList!!.add(additionalNamespace)
    }

    /**
     * Removes an additional namespace declarations from this element. This
     * should *not* be used to remove the declaration for this element
     * itself; that should be handled in the construction of the element.
     * Instead, this is for removing namespace declarations on the element not
     * relating directly to itself. If the declaration is not present, this
     * method does nothing.
     *
     * @param additionalNamespace namespace to remove. A null Namespace does nothing.
     */
    fun removeNamespaceDeclaration(additionalNamespace: Namespace?) {
        if (additionalNamespaceList == null) {
            return
        }
        @Suppress("UNCHECKED_CAST")
        (additionalNamespaceList as MutableCollection<Namespace?>).remove(additionalNamespace)
    }

    /**
     * Returns a list of the additional namespace declarations on this element.
     * This includes only additional namespace, not the namespace of the element
     * itself, which can be obtained through [namespace]. If there
     * are no additional declarations, this returns an empty list. Note, the
     * returned list is unmodifiable.
     *
     * @return                     a List of the additional namespace
     *                             declarations
     */
    val additionalNamespaces: List<Namespace>
        get() {
            // Not having the returned list be live allows us to avoid creating a
            // new list object when XMLOutputter calls this method on an element
            // with an empty list.
            if (additionalNamespaceList == null) {
                return emptyList()
            }
            return unmodifiableList(additionalNamespaceList!!)
        }

    /**
     * Returns the XPath 1.0 string value of this element, which is the
     * complete, ordered content of all text node descendants of this element
     * (i&#46;e&#46; the text that's left after all references are resolved
     * and all other markup is stripped out.)
     *
     * @return a concatenation of all text node descendants
     */
    override val value: String
        get() {
            val buffer = StringBuilder()

            for (child in content) {
                if (child is Element || child is Text) {
                    buffer.append(child.value)
                }
            }
            return buffer.toString()
        }

    /**
     * Returns whether this element is a root element. This can be used in
     * tandem with [parent] to determine if an element has any
     * "attachments" to a parent element or document.
     *
     * An element is a root element when it has a parent and that parent is a
     * Document. In particular, this means that detached Elements are **not**
     * root elements.
     *
     * @return                     whether this is a root element
     */
    val isRootElement: Boolean
        get() = parentRef is Document

    override val contentSize: Int
        get() = contentList.size

    override fun indexOf(child: Content): Int {
        return contentList.indexOf(child)
    }

    //    private int indexOf(int start, Filter filter) {
    //        int size = getContentSize();
    //        for (int i = start; i < size; i++) {
    //            if (filter.matches(getContent(i))) {
    //                return i;
    //            }
    //        }
    //        return -1;
    //    }


    /**
     * Returns the textual content directly held under this element as a string.
     * This includes all text within this single element, including whitespace
     * and CDATA sections if they exist. It's essentially the concatenation of
     * all [Text] and [CDATA] nodes returned by [content].
     * The call does not recurse into child elements. If no textual value exists
     * for the element, an empty string is returned.
     *
     * @return                     text content for this element, or empty
     *                             string if none
     */
    var text: String
        @JvmSynthetic
        set(v) {
            setText(v)
        }
        get() {
            if (contentList.size == 0) {
                return ""
            }

            // If we hold only a Text or CDATA, return it directly
            if (contentList.size == 1) {
                val obj: Any = contentList[0]
                if (obj is Text) {
                    return obj.text
                }
                return ""
            }

            // Else build String up
            val textContent = StringBuilder()
            var hasText = false

            for (i in 0 until contentList.size) {
                val obj: Any = contentList[i]
                if (obj is Text) {
                    textContent.append(obj.text)
                    hasText = true
                }
            }

            if (!hasText) {
                return ""
            }
            return textContent.toString()
        }

    /**
     * Returns the textual content of this element with all surrounding
     * whitespace removed. If no textual value exists for the element, or if
     * only whitespace exists, the empty string is returned.
     *
     * @return                     trimmed text content for this element, or
     *                             empty string if none
     */
    val textTrim: String
        get() = text.trim { it <= ' ' }

    /**
     * Returns the textual content of this element with all surrounding
     * whitespace removed and internal whitespace normalized to a single space.
     * If no textual value exists for the element, or if only whitespace exists,
     * the empty string is returned.
     *
     * @return                     normalized text content for this element, or
     *                             empty string if none
     */
    val textNormalize: String
        get() = Text.normalizeString(text)

    /**
     * Returns the textual content of the named child element, or null if
     * there's no such child. This method is a convenience because calling
     * `getChild().getText()` can throw a NullPointerException.
     *
     * @param  cname                the name of the child
     * @return                     text content for the named child, or null if
     *                             no such child
     */
    fun getChildText(cname: String?): String? {
        val child = getChild(cname) ?: return null
        return child.text
    }

    /**
     * Returns the trimmed textual content of the named child element, or null
     * if there's no such child. See `[textTrim]` for
     * details of text trimming.
     *
     * @param  cname                the name of the child
     * @return                     trimmed text content for the named child, or
     *                             null if no such child
     */
    fun getChildTextTrim(cname: String?): String? {
        val child = getChild(cname) ?: return null
        return child.textTrim
    }

    /**
     * Returns the normalized textual content of the named child element, or
     * null if there's no such child. See `[textNormalize]` for details of text normalizing.
     *
     * @param  cname                the name of the child
     * @return                     normalized text content for the named child,
     *                             or null if no such child
     */
    fun getChildTextNormalize(cname: String?): String? {
        val child = getChild(cname) ?: return null
        return child.textNormalize
    }

    /**
     * Returns the textual content of the named child element, or null if
     * there's no such child.
     *
     * @param cname
     *        the name of the child
     * @param ns
     *        the namespace of the child. A null implies Namespace.NO_NAMESPACE.
     * @return text content for the named child, or null if no such child
     */
    fun getChildText(cname: String?, ns: Namespace?): String? {
        val child = getChild(cname, ns) ?: return null
        return child.text
    }

    /**
     * Returns the trimmed textual content of the named child element, or null
     * if there's no such child.
     *
     * @param cname
     *        the name of the child
     * @param ns
     *        the namespace of the child. A null implies Namespace.NO_NAMESPACE.
     * @return trimmed text content for the named child, or null if no such
     *         child
     */
    fun getChildTextTrim(cname: String?, ns: Namespace?): String? {
        val child = getChild(cname, ns) ?: return null
        return child.textTrim
    }

    /**
     * Returns the normalized textual content of the named child element, or
     * null if there's no such child.
     *
     * @param cname
     *        the name of the child
     * @param ns
     *        the namespace of the child. A null implies Namespace.NO_NAMESPACE.
     * @return normalized text content for the named child, or null if no such
     *         child
     */
    fun getChildTextNormalize(cname: String?, ns: Namespace?): String? {
        val child = getChild(cname, ns) ?: return null
        return child.textNormalize
    }

    /**
     * Sets the content of the element to be the text given. All existing text
     * content and non-text context is removed. If this element should have both
     * textual content and nested elements, use `[setContent]`
     * instead. Setting a null text value is equivalent to setting an empty
     * string value.
     *
     * @param  text                 new text content for the element
     * @return                      the target element
     * @throws IllegalDataException if the assigned text contains an illegal
     *                              character such as a vertical tab (as
     *                              determined by [fi.sykero.kdom.Verifier.checkCharacterData])
     */
    open fun setText(text: String?): Element {
        contentList.clear()

        if (text != null) {
            addContent(Text(text))
        }

        return this
    }

    /**
     * Adjacent Text content is merged into the first Text in document order,
     * and the redundant Text items are removed (including any empty Text).
     *
     * @param recursively
     *        true if you want the text of child elements coalesced too. False
     *        if you only want to coalesce this Element's Text.
     * @return true if any content was changed by this operation.
     */
    fun coalesceText(recursively: Boolean): Boolean {
        val it: MutableIterator<Content> = if (recursively) descendants
        else contentList.iterator()
        var tfirst: Text? = null
        var changed = false
        while (it.hasNext()) {
            val c = it.next()
            if (c.cType == CType.Text) {
                // Text, and no CDATA!
                val text = c as Text
                if ("" == text.value) {
                    it.remove()
                    changed = true
                } else if (tfirst == null ||
                    tfirst.parent !== text.parent
                ) {
                    // previous item in the iterator was not text, or
                    // we are the next Text item after coming up the tree.
                    tfirst = text
                } else {
                    // add our text to the first in the sequence
                    tfirst.append(text.value)
                    // remove us from the sequence
                    it.remove()
                    changed = true
                }
            } else {
                // the end of the sequence
                tfirst = null
            }
        }
        return changed
    }

    /**
     * This returns the full content of the element as a List which
     * may contain objects of type `Text`, `Element`,
     * `Comment`, `ProcessingInstruction`,
     * `CDATA`, and `EntityRef`.
     * The List returned is "live" in document order and modifications
     * to it affect the element's actual contents.  Whitespace content is
     * returned in its entirety.
     *
     *
     * Sequential traversal through the List is best done with an Iterator
     * since the underlying implement of List.size() may require walking the
     * entire list.
     *
     *
     * @return a `List` containing the mixed content of the
     *         element: may contain `Text`,
     *         `[Element]`, `[Comment]`,
     *         `[ProcessingInstruction]`,
     *         `[CDATA]`, and
     *         `[EntityRef]` objects.
     */
    override var content: MutableList<Content>
        get() = contentList
        @JvmSynthetic
        set(v) {
            setContent(v)
        }

    /**
     * Return a filter view of this `Element`'s content.
     *
     *
     * Sequential traversal through the List is best done with a Iterator
     * since the underlying implement of List.size() may require walking the
     * entire list.
     *
     *
     * @param filter `Filter` to apply
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return `List` - filtered Element content
     */
    override fun <E : Content> getContent(filter: Filter<E>): MutableList<E> {
        return contentList.getView(filter)
    }

    /**
     * Removes all child content from this parent.
     *
     * @return list of the old children detached from this parent
     */
    override fun removeContent(): MutableList<Content> {
        val old: MutableList<Content> = ArrayList<Content>(contentList)
        contentList.clear()
        return old
    }

    /**
     * Remove all child content from this parent matching the supplied filter.
     *
     * @param filter filter to select which content to remove
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return list of the old children detached from this parent
     */
    override fun <F : Content> removeContent(filter: Filter<F>): MutableList<F> {
        val old: MutableList<F> = ArrayList<F>()
        val iter = contentList.getView(filter).iterator()
        while (iter.hasNext()) {
            val child = iter.next()
            old.add(child)
            iter.remove()
        }
        return old
    }

    /**
     * This sets the content of the element.  The supplied List should
     * contain only objects of type `Element`, `Text`,
     * `CDATA`, `Comment`,
     * `ProcessingInstruction`, and `EntityRef`.
     *
     *
     * When all objects in the supplied List are legal and before the new
     * content is added, all objects in the old content will have their
     * parentage set to null (no parent) and the old content list will be
     * cleared. This has the effect that any active list (previously obtained
     * with a call to [content] or [children]) will also
     * change to reflect the new content.  In addition, all objects in the
     * supplied List will have their parentage set to this element, but the
     * List itself will not be "live" and further removals and additions will
     * have no effect on this elements content. If the user wants to continue
     * working with a "live" list, then a call to setContent should be
     * followed by a call to [content] or [children] to
     * obtain a "live" version of the content.
     *
     *
     *
     * Passing a null or empty List clears the existing content.
     *
     *
     *
     * In event of an exception the original content will be unchanged and
     * the objects in the supplied content will be unaltered.
     *
     *
     * @param newContent `Collection` of content to set
     * @return this element modified
     * @throws IllegalAddException if the List contains objects of
     *         illegal types or with existing parentage.
     */
    fun setContent(newContent: Collection<Content>?): Element {
        contentList.clearAndSet(newContent)
        return this
    }

    /**
     * Replace the current child the given index with the supplied child.
     *
     * In event of an exception the original content will be unchanged and
     * the supplied child will be unaltered.
     *
     *
     * @param index - index of child to replace.
     * @param child - child to add.
     * @return element on which this method was invoked
     * @throws IllegalAddException if the supplied child is already attached
     *                             or not legal content for this parent.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than the current number of children.
     */
    fun setContent(index: Int, child: Content): Element {
        contentList.set(index, child)
        return this
    }

    /**
     * Replace the child at the given index with the supplied
     * collection.
     *
     * In event of an exception the original content will be unchanged and
     * the content in the supplied collection will be unaltered.
     *
     *
     * @param index - index of child to replace.
     * @param newContent - `Collection` of content to replace child.
     * @return object on which this method was invoked
     * @throws IllegalAddException if the collection contains objects of
     *         illegal types.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than the current number of children.
     */
    fun setContent(index: Int, newContent: Collection<Content>): Parent {
        contentList.removeAt(index)
        contentList.addAll(index, newContent)
        return this
    }

    /**
     * This adds text content to this element.  It does not replace the
     * existing content as does `setText()`.
     *
     * @param str `String` to add
     * @return this element modified
     * @throws IllegalDataException if `str` contains an
     *         illegal character such as a vertical tab (as determined
     *         by [fi.sykero.kdom.Verifier.checkCharacterData])
     */
    fun addContent(str: String?): Element {
        return addContent(Text(str))
    }

    /**
     * Appends the child to the end of the element's content list.
     *
     * @param child   child to append to end of content list
     * @return        the element on which the method was called
     * @throws IllegalAddException if the given child already has a parent.     */
    override fun addContent(child: Content): Element {
        contentList.add(child)
        return this
    }

    /**
     * Appends all children in the given collection to the end of
     * the content list.  In event of an exception during add the
     * original content will be unchanged and the objects in the supplied
     * collection will be unaltered.
     *
     * @param c `Collection` of content to append
     * @return           the element on which the method was called
     * @throws IllegalAddException if any item in the collection
     *         already has a parent or is of an inappropriate type.
     */
    override fun addContent(c: Collection<Content>): Element {
        contentList.addAll(c)
        return this
    }

    /**
     * Inserts the child into the content list at the given index.
     *
     * @param index location for adding the collection
     * @param child      child to insert
     * @return           the parent on which the method was called
     * @throws IndexOutOfBoundsException if index is negative or beyond
     *         the current number of children
     * @throws IllegalAddException if the given child already has a parent.
     */
    override fun addContent(index: Int, child: Content): Element {
        contentList.add(index, child)
        return this
    }

    /**
     * Inserts the content in a collection into the content list
     * at the given index.  In event of an exception the original content
     * will be unchanged and the objects in the supplied collection will be
     * unaltered.
     *
     * @param index location for adding the collection
     * @param c  `Collection` of content to insert
     * @return            the parent on which the method was called
     * @throws IndexOutOfBoundsException if index is negative or beyond
     *         the current number of children
     * @throws IllegalAddException if any item in the collection
     *         already has a parent or is of an inappropriate type.
     */
    override fun addContent(index: Int, c: Collection<Content>): Element {
        contentList.addAll(index, c)
        return this
    }

    override fun cloneContent(): MutableList<Content> {
        val size = contentSize
        val list: MutableList<Content> = ArrayList<Content>(size)
        for (i in 0 until size) {
            val child = getContent(i)
            list.add(child.clone())
        }
        return list
    }

    override fun getContent(index: Int): Content {
        return contentList[index]
    }

    //    public Content getChild(Filter filter) {
    //        int i = indexOf(0, filter);
    //        return (i < 0) ? null : getContent(i);
    //    }

    override fun removeContent(child: Content): Boolean {
        return contentList.remove(child)
    }

    override fun removeContent(index: Int): Content? {
        return contentList.removeAt(index)
    }

    /**
     * Set this element's content to be the supplied child.
     *
     * If the supplied child is legal content for this parent and before
     * it is added, all content in the current content list will
     * be cleared and all current children will have their parentage set to
     * null.
     *
     * This has the effect that any active list (previously obtained with
     * a call to one of the [content] methods will also change
     * to reflect the new content.  In addition, all content in the supplied
     * collection will have their parentage set to this parent.  If the user
     * wants to continue working with a **"live"** list of this parent's
     * child, then a call to setContent should be followed by a call to one
     * of the [content] methods to obtain a **"live"**
     * version of the children.
     *
     * Passing a null child clears the existing content.
     *
     * In event of an exception the original content will be unchanged and
     * the supplied child will be unaltered.
     *
     * @param child new content to replace existing content
     * @return           the parent on which the method was called
     * @throws IllegalAddException if the supplied child is already attached
     *                             or not legal content for an Element
     */
    fun setContent(child: Content): Element {
        contentList.clear()
        contentList.add(child)
        return this
    }


    /**
     * Determines if this element is the ancestor of another element.
     *
     * @param element `Element` to check against
     * @return `true` if this element is the ancestor of the
     *         supplied element
     */
    fun isAncestor(element: Element): Boolean {
        var p = element.parent
        while (p is Element) {
            if (p === this) {
                return true
            }
            p = p.parent
        }
        return false
    }

    /**
     * Indicate whether this Element has any attributes.
     * Where possible you should call this method before calling attributes
     * because calling attributes will create the necessary Attribute List
     * memory structures, even if there are no Attributes attached to the
     * Element. Calling hasAttributes() first can save memory.
     * @return true if this Element has attributes.
     */
    fun hasAttributes(): Boolean {
        return attributeListField != null && !attributeListField!!.isEmpty()
    }

    /**
     * Indicate whether this Element has any additional Namespace declarations.
     * Where possible you should call this method before calling
     * [additionalNamespaces] because calling attributes will
     * create an unnecessary List even if there are no Additional Namespaces
     * attached to the Element. Calling this method first can save memory and
     * time.
     * @return true if this Element has additional Namespaces.
     */
    fun hasAdditionalNamespaces(): Boolean {
        return additionalNamespaceList != null && !additionalNamespaceList!!.isEmpty()
    }

    /**
     * Lazy initialiser for the Attribute list.
     * @return this Element's Attribute List (creating it if necessary).
     */
    private fun getAttributeList(): AttributeList {
        if (attributeListField == null) {
            attributeListField = AttributeList(this)
        }
        return attributeListField!!
    }

    /**
     *
     * This returns the complete set of attributes for this element, as a
     * `List` of `Attribute` objects in no particular
     * order, or an empty list if there are none.
     *
     * The returned list is "live" and changes to it affect the
     * element's actual attributes.
     *
     * Use the methods [hasAttributes] or [attributesSize]
     * if you just want to see whether there are attributes. Calling this method
     * may be inefficient if there are no Attributes.
     *
     * @return attributes for the element
     */
    var attributes: MutableList<Attribute>
        get() = getAttributeList()
        @JvmSynthetic
        set(v) {
            setAttributes(v)
        }


    /**
     * Get the number of Attributes currently attached to this Element.
     * @return the number of Attributes attached.
     */
    val attributesSize: Int
        get() = if (attributeListField == null) 0 else attributeListField!!.size

    /**
     *
     * This returns the attribute for this element with the given name
     * and within no namespace, or null if no such attribute exists.
     *
     *
     * @param attname name of the attribute to return
     * @return attribute for the element
     */
    fun getAttribute(attname: String?): Attribute? {
        return getAttribute(attname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This returns the attribute for this element with the given name
     * and within the given Namespace, or null if no such attribute exists.
     *
     *
     * @param attname name of the attribute to return
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @return attribute for the element
     */
    fun getAttribute(attname: String?, ns: Namespace?): Attribute? {
        if (attributeListField == null) {
            return null
        }
        return getAttributeList().get(attname, ns)
    }

    /**
     *
     * This returns the attribute value for the attribute with the given name
     * and within no namespace, null if there is no such attribute, and the
     * empty string if the attribute value is empty.
     *
     *
     * @param attname name of the attribute whose value to be returned
     * @return the named attribute's value, or null if no such attribute
     */
    fun getAttributeValue(attname: String?): String? {
        if (attributeListField == null) {
            return null
        }
        return getAttributeValue(attname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This returns the attribute value for the attribute with the given name
     * and within no namespace, or the passed-in default if there is no
     * such attribute.
     *
     *
     * @param attname name of the attribute whose value to be returned
     * @param def a default value to return if the attribute does not exist
     * @return the named attribute's value, or the default if no such attribute
     */
    fun getAttributeValue(attname: String?, def: String?): String? {
        if (attributeListField == null) {
            return def
        }
        return getAttributeValue(attname, Namespace.NO_NAMESPACE, def)
    }

    /**
     *
     * This returns the attribute value for the attribute with the given name
     * and within the given Namespace, null if there is no such attribute, and
     * the empty string if the attribute value is empty.
     *
     *
     * @param attname name of the attribute whose valud is to be returned
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @return the named attribute's value, or null if no such attribute
     */
    fun getAttributeValue(attname: String?, ns: Namespace?): String? {
        if (attributeListField == null) {
            return null
        }
        return getAttributeValue(attname, ns, null)
    }

    /**
     *
     * This returns the attribute value for the attribute with the given name
     * and within the given Namespace, or the passed-in default if there is no
     * such attribute.
     *
     *
     * @param attname name of the attribute whose valud is to be returned
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @param def a default value to return if the attribute does not exist
     * @return the named attribute's value, or the default if no such attribute
     */
    fun getAttributeValue(attname: String?, ns: Namespace?, def: String?): String? {
        if (attributeListField == null) {
            return def
        }
        val attribute = getAttributeList().get(attname, ns)
        if (attribute == null) {
            return def
        }

        return attribute.value
    }

    /**
     *
     * This sets the attributes of the element.  The supplied Collection should
     * contain only objects of type `Attribute`.
     *
     *
     *
     * When all objects in the supplied List are legal and before the new
     * attributes are added, all old attributes will have their
     * parentage set to null (no parent) and the old attribute list will be
     * cleared. This has the effect that any active attribute list (previously
     * obtained with a call to [attributes]) will also change to
     * reflect the new attributes.  In addition, all attributes in the supplied
     * List will have their parentage set to this element, but the List itself
     * will not be "live" and further removals and additions will have no
     * effect on this elements attributes. If the user wants to continue
     * working with a "live" attribute list, then a call to setAttributes
     * should be followed by a call to [attributes] to obtain a
     * "live" version of the attributes.
     *
     *
     *
     *
     * Passing a null or empty List clears the existing attributes.
     *
     *
     *
     *
     * In cases where the List contains duplicate attributes, only the last
     * one will be retained.  This has the same effect as calling
     * [setAttribute] sequentially.
     *
     *
     *
     *
     * In event of an exception the original attributes will be unchanged and
     * the attributes in the supplied attributes will be unaltered.
     *
     *
     * @param newAttributes `Collection` of attributes to set
     * @return this element modified
     * @throws IllegalAddException if the List contains objects
     *         that are not instances of `Attribute`,
     *         or if any of the `Attribute` objects have
     *         conflicting namespace prefixes.
     */
    fun setAttributes(newAttributes: Collection<Attribute>?): Element {
        getAttributeList().clearAndSet(newAttributes)
        return this
    }

    /**
     *
     * This sets an attribute value for this element.  Any existing attribute
     * with the same name and namespace URI is removed.
     *
     *
     * @param name name of the attribute to set
     * @param value value of the attribute to set
     * @return this element modified
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]).
     */
    fun setAttribute(name: String?, value: String?): Element {
        val attribute = getAttribute(name)
        if (attribute == null) {
            val newAttribute = Attribute(name, value)
            setAttribute(newAttribute)
        } else {
            attribute.setValue(value)
        }

        return this
    }

    /**
     *
     * This sets an attribute value for this element.  Any existing attribute
     * with the same name and namespace URI is removed.
     *
     *
     * @param name name of the attribute to set
     * @param value value of the attribute to set
     * @param ns namespace of the attribute to set. A null implies Namespace.NO_NAMESPACE.
     * @return this element modified
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name, or if the namespace is an unprefixed default
     *         namespace
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]).
     * @throws IllegalAddException if the attribute namespace prefix
     *         collides with another namespace prefix on the element.
     */
    fun setAttribute(name: String?, value: String?, ns: Namespace?): Element {
        val attribute = getAttribute(name, ns)
        if (attribute == null) {
            val newAttribute = Attribute(name, value, ns)
            setAttribute(newAttribute)
        } else {
            attribute.setValue(value)
        }

        return this
    }

    /**
     *
     * This sets an attribute value for this element.  Any existing attribute
     * with the same name and namespace URI is removed.
     *
     *
     * @param attribute `Attribute` to set
     * @return this element modified
     * @throws IllegalAddException if the attribute being added already has a
     *   parent or if the attribute namespace prefix collides with another
     *   namespace prefix on the element.
     */
    fun setAttribute(attribute: Attribute): Element {
        getAttributeList().add(attribute)
        return this
    }

    /**
     *
     * This removes the attribute with the given name and within no
     * namespace. If no such attribute exists, this method does nothing.
     *
     *
     * @param attname name of attribute to remove
     * @return whether the attribute was removed
     */
    fun removeAttribute(attname: String?): Boolean {
        return removeAttribute(attname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This removes the attribute with the given name and within the
     * given Namespace.  If no such attribute exists, this method does
     * nothing.
     *
     *
     * @param attname name of attribute to remove
     * @param ns namespace URI of attribute to remove. A null implies Namespace.NO_NAMESPACE.
     * @return whether the attribute was removed
     */
    fun removeAttribute(attname: String?, ns: Namespace?): Boolean {
        if (attributeListField == null) {
            return false
        }
        return getAttributeList().remove(attname, ns)
    }

    /**
     *
     * This removes the supplied Attribute should it exist.
     *
     *
     * @param attribute Reference to the attribute to be removed.
     * @return whether the attribute was removed
     */
    fun removeAttribute(attribute: Attribute): Boolean {
        if (attributeListField == null) {
            return false
        }
        return getAttributeList().remove(attribute)
    }

    /**
     *
     * This returns a `String` representation of the
     * `Element`, suitable for debugging. If the XML
     * representation of the `Element` is desired,
     * [fi.sykero.kdom.output.XMLOutputter.outputString]
     * should be used.
     *
     *
     * @return `String` - information about the
     *         `Element`
     */
    override fun toString(): String {
        val stringForm = StringBuilder(64)
            .append("[Element: <")
            .append(qualifiedName)

        val nsuri = namespaceURI
        if ("" != nsuri) {
            stringForm
                .append(" [Namespace: ")
                .append(nsuri)
                .append("]")
        }
        stringForm.append("/>]")

        return stringForm.toString()
    }

    /**
     *
     *  This returns a deep clone of this element.
     *  The new element is detached from its parent, and getParent()
     *  on the clone will return null.
     *
     *
     * @return the clone of this element
     */
    protected override fun shallowCopy(): Element {
        val copy = Element()
        copy.nameField = nameField
        copy.namespaceField = namespaceField
        copy.additionalNamespaceList = additionalNamespaceList
        copy.attributeListField = attributeListField
        copy.contentList = contentList
        return copy
    }

    public override fun clone(): Element {

        // Ken Rune Helland <kenh@csc.no> is our local clone() guru

        val element = super.clone() as Element

        // name and namespace are references to immutable objects
        // so super.clone() handles them ok

        // Reference to parent is copied by super.clone()
        // (Object.clone()) so we have to remove it
        // Actually, super is a Content, which has already detached in the
        // clone().
        // element.parent = null;

        // Reference to content list and attribute lists are copied by
        // super.clone() so we set it new lists if the original had lists
        element.contentList = ContentList(element)
        element.attributeListField = if (attributeListField == null) null else AttributeList(element)

        // Cloning attributes
        if (attributeListField != null) {
            for (i in 0 until attributeListField!!.size) {
                val attribute = attributeListField!![i]
                element.attributeListField!!.add(attribute.clone())
            }
        }

        // Cloning additional namespaces
        if (additionalNamespaceList != null) {
            element.additionalNamespaceList = ArrayList<Namespace>(additionalNamespaceList!!)
        }

        // Cloning content
        for (i in 0 until contentList.size) {
            val c = contentList[i]
            element.contentList.add(c.clone())
        }

        return element
    }


    /**
     * Returns an iterator that walks over all descendants in document order.
     *
     * @return an iterator to walk descendants
     */
    override val descendants: IteratorIterable<Content>
        get() = DescendantIterator(this)

    /**
     * Returns an iterator that walks over all descendants in document order
     * applying the Filter to return only content that match the filter rule.
     * With filters you can match only Elements, only Comments, Elements or
     * Comments, only Elements with a given name and/or prefix, and so on.
     *
     * @param filter filter to select which descendants to see
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return an iterator to walk descendants within a filter
     */
    override fun <F : Content> getDescendants(filter: Filter<F>): IteratorIterable<F> {
        return FilterIterator<F>(DescendantIterator(this), filter)
    }


    /**
     * This returns a `List` of all the child elements
     * nested directly (one level deep) within this element, as
     * `Element` objects.  If this target element has no nested
     * elements, an empty List is returned.  The returned list is "live"
     * in document order and changes to it affect the element's actual
     * contents.
     *
     *
     * Sequential traversal through the List is best done with a Iterator
     * since the underlying implement of List.size() may not be the most
     * efficient.
     *
     *
     *
     * No recursion is performed, so elements nested two levels deep
     * would have to be obtained with:
     * ```
     *   for(Element oneLevelDeep : topElement.getChildren()) {
     *     List<Element> twoLevelsDeep = oneLevelDeep.getChildren();
     *     // Do something with these children
     *   }
     * ```
     *
     * @return list of child `Element` objects for this element
     */
    val children: MutableList<Element>
        get() = contentList.getView(ElementFilter())

    /**
     * This returns a `List` of all the child elements
     * nested directly (one level deep) within this element with the given
     * local name and belonging to no namespace, returned as
     * `Element` objects.  If this target element has no nested
     * elements with the given name outside a namespace, an empty List
     * is returned.  The returned list is "live" in document order
     * and changes to it affect the element's actual contents.
     *
     * Please see the notes for `[children]`
     * for a code example.
     *
     *
     * @param cname local name for the children to match
     * @return all matching child elements
     */
    fun getChildren(cname: String?): MutableList<Element> {
        return getChildren(cname, Namespace.NO_NAMESPACE)
    }

    /**
     * This returns a `List` of all the child elements
     * nested directly (one level deep) within this element with the given
     * local name and belonging to the given Namespace, returned as
     * `Element` objects.  If this target element has no nested
     * elements with the given name in the given Namespace, an empty List
     * is returned.  The returned list is "live" in document order
     * and changes to it affect the element's actual contents.
     *
     * Please see the notes for `[children]`
     * for a code example.
     *
     *
     * @param cname local name for the children to match
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @return all matching child elements
     */
    fun getChildren(cname: String?, ns: Namespace?): MutableList<Element> {
        return contentList.getView(ElementFilter(cname, ns))
    }

    /**
     * This returns the first child element within this element with the
     * given local name and belonging to the given namespace.
     * If no elements exist for the specified name and namespace, null is
     * returned.
     *
     * @param cname local name of child element to match. A null implies any name
     * @param ns `Namespace` to search within. A null implies any namespace.
     * @return the first matching child element, or null if not found
     */
    fun getChild(cname: String?, ns: Namespace?): Element? {
        val elements = contentList.getView(ElementFilter(cname, ns))
        val iter = elements.iterator()
        if (iter.hasNext()) {
            return iter.next()
        }
        return null
    }

    /**
     * This returns the first child element within this element with the
     * given local name and belonging to no namespace.
     * If no elements exist for the specified name and namespace, null is
     * returned.
     *
     * @param cname local name of child element to match
     * @return the first matching child element, or null if not found
     */
    fun getChild(cname: String?): Element? {
        return getChild(cname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This removes the first child element (one level deep) with the
     * given local name and belonging to no namespace.
     * Returns true if a child was removed.
     *
     *
     * @param cname the name of child elements to remove
     * @return whether deletion occurred
     */
    fun removeChild(cname: String?): Boolean {
        return removeChild(cname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This removes the first child element (one level deep) with the
     * given local name and belonging to the given namespace.
     * Returns true if a child was removed.
     *
     *
     * @param cname the name of child element to remove
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @return whether deletion occurred
     */
    fun removeChild(cname: String?, ns: Namespace?): Boolean {
        val filter = ElementFilter(cname, ns)
        val old = contentList.getView(filter)
        val iter = old.iterator()
        if (iter.hasNext()) {
            iter.next()
            iter.remove()
            return true
        }

        return false
    }

    /**
     *
     * This removes all child elements (one level deep) with the
     * given local name and belonging to no namespace.
     * Returns true if any were removed.
     *
     *
     * @param cname the name of child elements to remove
     * @return whether deletion occurred
     */
    fun removeChildren(cname: String?): Boolean {
        return removeChildren(cname, Namespace.NO_NAMESPACE)
    }

    /**
     *
     * This removes all child elements (one level deep) with the
     * given local name and belonging to the given namespace.
     * Returns true if any were removed.
     *
     *
     * @param cname the name of child elements to remove
     * @param ns `Namespace` to search within. A null implies Namespace.NO_NAMESPACE.
     * @return whether deletion occurred
     */
    fun removeChildren(cname: String?, ns: Namespace?): Boolean {
        var deletedSome = false

        val filter = ElementFilter(cname, ns)
        val old = contentList.getView(filter)
        val iter = old.iterator()
        while (iter.hasNext()) {
            iter.next()
            iter.remove()
            deletedSome = true
        }

        return deletedSome
    }

    /**
     * Get the Namespaces that are in-scope on this Element. Element has the
     * most complex rules for the namespaces-in-scope.
     *
     * The scope is built up from a number of sources following the rules of
     * XML namespace inheritance as follows:
     *
     * - The [Namespace.XML_NAMESPACE] is added
     * - The element's namespace is added (commonly [Namespace.NO_NAMESPACE])
     * - All the attributes are inspected and for those that have a namespace
     *   prefix then their Namespaces are included (the "default" namespace for
     *   attributes is not the same as the "default" namespace for the element that
     *   attribute is on).
     * - All Namespaces declared on this Element using
     *   [addNamespaceDeclaration] are included.
     * - If the element has a parent then the parent's Namespace scope is
     *   inspected, and any prefixes in the parent scope that are not yet bound
     *   in this Element's scope are included.
     * - If the default Namespace (the no-prefix namespace) has not been
     *   encountered for this Element then [Namespace.NO_NAMESPACE] is
     *   included.
     *
     * The Element's Namespace scope consist of its inherited Namespaces and
     * any modifications to that scope derived from the Element itself.
     *
     * Note that the Element's Namespace will always be reported first.
     *
     * **Description copied from**
     * [NamespaceAware.namespacesInScope]:
     *
     * @see NamespaceAware
     */
    override val namespacesInScope: List<Namespace>
        get() {
            // The assumption here is that all namespaces are valid,
            // that there are no namespace collisions on this element

            // This method is also the 'anchor' of the three getNamespaces*() methods
            // It does not make reference to this Element instance's other
            // getNamespace*() methods

            // kdom: jdom2 used a TreeMap (no TreeMap in common code); the values are sorted by prefix at the read
            // below, which gives the same order.
            val namespaces = LinkedHashMap<String, Namespace>()
            namespaces.put(Namespace.XML_NAMESPACE.prefix, Namespace.XML_NAMESPACE)
            namespaces.put(namespacePrefix, namespace)
            if (additionalNamespaceList != null) {
                for (ns in additionalNamespaces) {
                    if (!namespaces.containsKey(ns.prefix)) {
                        namespaces.put(ns.prefix, ns)
                    }
                }
            }
            if (attributeListField != null) {
                for (att in attributes) {
                    val ns = att.namespace
                    if (Namespace.NO_NAMESPACE != ns && !namespaces.containsKey(ns.prefix)) {
                        namespaces.put(ns.prefix, ns)
                    }
                }
            }
            // Right, we now have all the namespaces that are current on this ELement.
            // Include any other namespaces that are inherited.
            val pnt = parentElement
            if (pnt != null) {
                for (ns in pnt.namespacesInScope) {
                    if (!namespaces.containsKey(ns.prefix)) {
                        namespaces.put(ns.prefix, ns)
                    }
                }
            }

            if (pnt == null && !namespaces.containsKey("")) {
                // we are the root element, and there is no 'default' namespace.
                namespaces.put(Namespace.NO_NAMESPACE.prefix, Namespace.NO_NAMESPACE)
            }

            val al = ArrayList<Namespace>(namespaces.size)
            al.add(namespace)
            namespaces.remove(namespacePrefix)
            namespaces.keys.sorted().mapTo(al) { namespaces.getValue(it) }

            return unmodifiableList(al)
        }

    override val namespacesInherited: List<Namespace>
        get() {
            if (parentElement == null) {
                val ret = ArrayList<Namespace>(namespacesInScope)
                val it = ret.iterator()
                while (it.hasNext()) {
                    val ns = it.next()
                    if (ns === Namespace.NO_NAMESPACE || ns === Namespace.XML_NAMESPACE) {
                        continue
                    }
                    it.remove()
                }
                return unmodifiableList(ret)
            }

            // OK, the things we inherit are the prefixes we have in scope that
            // are also in our parent's scope.
            val parents = HashMap<String, Namespace>()
            for (ns in parentElement!!.namespacesInScope) {
                parents.put(ns.prefix, ns)
            }

            val al = ArrayList<Namespace>()
            for (ns in namespacesInScope) {
                if (ns === parents.get(ns.prefix)) {
                    // inherited
                    al.add(ns)
                }
            }

            return unmodifiableList(al)
        }

    override val namespacesIntroduced: List<Namespace>
        get() {
            if (parentElement == null) {
                // we introduce everything... except Namespace.XML_NAMESPACE
                val ret: MutableList<Namespace> = ArrayList<Namespace>(namespacesInScope)
                val it = ret.iterator()
                while (it.hasNext()) {
                    val ns = it.next()
                    if (ns === Namespace.XML_NAMESPACE || ns === Namespace.NO_NAMESPACE) {
                        it.remove()
                    }
                }
                return unmodifiableList(ret)
            }

            // OK, the things we introduce are the prefixes we have in scope that
            // are *not* in our parent's scope.
            val parents = HashMap<String, Namespace>()
            for (ns in parentElement!!.namespacesInScope) {
                parents.put(ns.prefix, ns)
            }

            val al = ArrayList<Namespace>()
            for (ns in namespacesInScope) {
                if (!parents.containsKey(ns.prefix) || ns !== parents.get(ns.prefix)) {
                    // introduced
                    al.add(ns)
                }
            }

            return unmodifiableList(al)
        }

    override fun detach(): Element {
        return super.detach() as Element
    }

    @Throws(IllegalAddException::class)
    override fun canContainContent(content: Content, index: Int, replace: Boolean) {
        if (content is DocType) {
            throw IllegalAddException(
                "A DocType is not allowed except at the document level"
            )
        }
    }

    /**
     * Sort the contents of this Element using a mechanism that is safe for JDOM
     * content. See the notes on [sortContent] for
     * how the algorithm works.
     *
     * [Collections.sort] is not appropriate for sorting
     * the Lists returned from [content] because those are
     * 'live' lists, and the Collections.sort() method uses an algorithm that
     * adds the content in the new location before removing it from the old.
     * That creates validation issues with content attempting to attach to a
     * parent before detaching first.
     *
     * This method provides a safe means to conveniently sort the content.
     *
     * @param comparator The Comparator to use for the sorting.
     */
    fun sortContent(comparator: Comparator<in Content>?) {
        contentList.sortContent(comparator)
    }

    /**
     * Sort the child Elements of this Element using a mechanism that is safe
     * for JDOM content. Other child content will be unaffected. See the notes
     * on [sortContent] for how the algorithm works.
     *
     * [Collections.sort] is not appropriate for sorting
     * the Lists returned from [content] because those are
     * 'live' lists, and the Collections.sort() method uses an algorithm that
     * adds the content in the new location before removing it from the old.
     * This creates validation issues with content attempting to attach to a
     * parent before detaching first.
     *
     * This method provides a safe means to conveniently sort the content.
     *
     * @param comparator The Comparator to use for the sorting.
     */
    fun sortChildren(comparator: Comparator<in Element>?) {
        (children as FilterList<Element>).sortContent(comparator)
    }

    /**
     * Sort the Attributes of this Element using a mechanism that is safe
     * for JDOM. Other child content will be unaffected. See the notes
     * on [sortContent] for how the algorithm works.
     *
     * [Collections.sort] is not appropriate for sorting
     * the Lists returned from [content] because those are
     * 'live' lists, and the Collections.sort() method uses an algorithm that
     * adds the content in the new location before removing it from the old.
     * This creates validation issues with content attempting to attach to a
     * parent before detaching first.
     *
     * This method provides a safe means to conveniently sort the content.
     *
     * A null comparator will sort the Attributes alphabetically first by prefix,
     * then by name
     *
     * @param comparator The Comparator to use for the sorting.
     */
    fun sortAttributes(comparator: Comparator<in Attribute>?) {
        if (attributeListField != null) {
            attributeListField!!.sortAttributes(comparator)
        }
    }

    /**
     * Sort the child content of this Element that matches the Filter, using a
     * mechanism that is safe for JDOM content. Other child content (that does
     * not match the filter) will be unaffected.
     *
     * The algorithm used for sorting affects the child content in the following
     * ways:
     *
     * 1. Items not matching the Filter will be unchanged. This includes the
     *    absolute position of that content in this Element. i.e. if child content
     *    `cc` does not match the Filter, then `indexOf(cc)`
     *    will not be changed by this sort.
     * 1. Items matching the Filter will be reordered according to the
     *    comparator. Only the relative order of the Filtered data will change.
     * 1. Items that compare as 'equals' using the comparator will keep the
     *    the same relative order as before the sort.
     *
     * [Collections.sort] is not appropriate for sorting
     * the Lists returned from [content] because those are
     * 'live' lists, and the Collections.sort() method uses an algorithm that
     * adds the content in the new location before removing it from the old.
     * This creates validation issues with content attempting to attach to a
     * parent before detaching first.
     *
     * This method provides a safe means to conveniently sort the content.
     * @param <E> The generic type of the Filter used to select the content to
     * sort.
     * @param filter The Filter used to select which child content to sort.
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @param comparator The Comparator to use for the sorting.
     */
    fun <E : Content> sortContent(filter: Filter<E>, comparator: Comparator<in E>?) {
        @Suppress("UNCHECKED_CAST")
        val list = getContent(filter) as FilterList<E>
        list.sortContent(comparator)

    }


    private companion object {
        private const val INITIAL_ARRAY_SIZE = 5

    }

}
