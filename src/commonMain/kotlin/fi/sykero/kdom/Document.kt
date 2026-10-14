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
import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.util.IteratorIterable

/**
 * An XML document. Methods allow access to the root element as well as the
 * [DocType] and other document-level information.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Jools Enticknap
 * @author  Bradley S. Huffman
 * @author  Rolf Lear
 */
open class Document : CloneBase, Parent {

    /**
     * This document's content including comments, PIs, a possible
     * DocType, and a root element.
     * Subclassers have to track content using their own
     * mechanism.
     * (Was the package-private field `content`; renamed because that is now the property.)
     */
    private var contentList: ContentList = ContentList(this)

    /**
     *  See http://www.w3.org/TR/2003/WD-DOM-Level-3-Core-20030226/core.html#baseURIs-Considerations
     *
     * Sets the effective URI from which this document was loaded,
     * and against which relative URLs in this document will be resolved.
     * (The URI is not checked.)
     *
     * Returns the URI from which this document was loaded,
     * or null if this is not known.
     */
    var baseURI: String? = null

    // Supports the setProperty/getProperty calls
    private var propertyMap: HashMap<String?, Any?>? = null

    /**
     * Creates a new empty document.  A document must have a root element,
     * so this document will not be well-formed and accessor methods will
     * throw an IllegalStateException if this document is accessed before a
     * root element is added.  This method is most useful for build tools.
     */
    constructor() : super()

    /**
     * This will create a new `Document`,
     * with the supplied `[Element]`
     * as the root element, the supplied
     * `[DocType]` declaration, and the specified
     * base URI.
     *
     * @param rootElement `Element` for document root.
     * @param docType `DocType` declaration.
     * @param baseURI the URI from which this document was loaded.
     * @throws IllegalAddException if the given docType object
     *         is already attached to a document or the given
     *         rootElement already has a parent
     */
    constructor(rootElement: Element?, docType: DocType?, baseURI: String?) : super() {
        if (rootElement != null) {
            setRootElement(rootElement)
        }
        if (docType != null) {
            setDocType(docType)
        }
        if (baseURI != null) {
            this.baseURI = baseURI
        }
    }

    /**
     * This will create a new `Document`,
     * with the supplied `[Element]`
     * as the root element and the supplied
     * `[DocType]` declaration.
     *
     * @param rootElement `Element` for document root.
     * @param docType `DocType` declaration.
     * @throws IllegalAddException if the given DocType object
     *         is already attached to a document or the given
     *         rootElement already has a parent
     */
    constructor(rootElement: Element?, docType: DocType?) : this(rootElement, docType, null)

    /**
     * This will create a new `Document`,
     * with the supplied `[Element]`
     * as the root element, and no `[DocType]`
     * declaration.
     *
     * @param rootElement `Element` for document root
     * @throws IllegalAddException if the given rootElement already has
     *         a parent.
     */
    constructor(rootElement: Element?) : this(rootElement, null, null)

    /**
     * This will create a new `Document`,
     * with the supplied list of content, and a
     * `[DocType]` declaration only if the content
     * contains a DocType instance.  A null list is treated the
     * same as the no-arg constructor.
     *
     * @param content `List` of starter content
     * @throws IllegalAddException if the List contains more than
     *         one Element or objects of illegal types.
     */
    constructor(content: List<Content>?) : super() {
        setContent(content)
    }

    override val contentSize: Int
        get() = contentList.size

    override fun indexOf(child: Content): Int {
        return contentList.indexOf(child)
    }

    //    /**
    //     * Starting at the given index (inclusive), return the index of
    //     * the first child matching the supplied filter, or -1
    //     * if none is found.
    //     *
    //     * @return index of child, or -1 if none found.
    //     */
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
     * This will return `true` if this document has a
     * root element, `false` otherwise.
     *
     * @return `true` if this document has a root element,
     *         `false` otherwise.
     */
    fun hasRootElement(): Boolean {
        return if ((contentList.indexOfFirstElement() < 0)) false else true
    }

    /**
     * This will return the root `Element`
     * for this `Document`
     *
     * @return `Element` - the document's root element
     * @throws IllegalStateException if the root element hasn't been set
     */
    var rootElement: Element
        @JvmSynthetic
        set(v) {
            setRootElement(v)
        }
        get() {
            val index = contentList.indexOfFirstElement()
            if (index < 0) {
                throw IllegalStateException("Root element not set")
            }
            return contentList[index] as Element
        }

    /**
     * This sets the root `[Element]` for the
     * `Document`. If the document already has a root
     * element, it is replaced.
     *
     * @param rootElement `Element` to be new root.
     * @return `Document` - modified Document.
     * @throws IllegalAddException if the given rootElement already has
     *         a parent.
     */
    open fun setRootElement(rootElement: Element?): Document {
        val index = contentList.indexOfFirstElement()
        if (index < 0) {
            contentList.add(rootElement!!)
        } else {
            contentList.set(index, rootElement!!)
        }
        return this
    }

    /**
     * Detach the root `[Element]` from this document.
     *
     * @return removed root `Element`
     */
    fun detachRootElement(): Element? {
        val index = contentList.indexOfFirstElement()
        if (index < 0)
            return null
        return removeContent(index) as Element?
    }

    /**
     * This will return the `[DocType]`
     * declaration for this `Document`, or
     * `null` if none exists.
     *
     * @return `DocType` - the DOCTYPE declaration.
     */
    var docType: DocType?
        @JvmSynthetic
        set(v) {
            setDocType(v)
        }
        get() {
            val index = contentList.indexOfDocType()
            if (index < 0) {
                return null
            }
            return contentList[index] as DocType?
        }

    /**
     * This will set the `[DocType]`
     * declaration for this `Document`. Note
     * that a DocType can only be attached to one Document.
     * Attempting to set the DocType to a DocType object
     * that already belongs to a Document will result in an
     * IllegalAddException being thrown.
     *
     * @param docType `DocType` declaration.
     * @return object on which the method was invoked
     * @throws IllegalAddException if the given docType is
     *   already attached to a Document.
     */
    open fun setDocType(docType: DocType?): Document {
        if (docType == null) {
            // Remove any existing doctype
            val docTypeIndex = contentList.indexOfDocType()
            if (docTypeIndex >= 0) contentList.removeAt(docTypeIndex)
            return this
        }

        if (docType.parent != null) {
            throw IllegalAddException(
                docType,
                "The DocType already is attached to a document"
            )
        }

        // Add DocType to head if new, replace old otherwise
        val docTypeIndex = contentList.indexOfDocType()
        if (docTypeIndex < 0) {
            contentList.add(0, docType)
        } else {
            contentList.set(docTypeIndex, docType)
        }

        return this
    }

    /**
     * Appends the child to the end of the content list.
     *
     * @param child   child to append to end of content list
     * @return        the document on which the method was called
     * @throws IllegalAddException if the given child already has a parent.
     */
    override fun addContent(child: Content): Document {
        contentList.add(child)
        return this
    }

    /**
     * Appends all children in the given collection to the end of
     * the content list.  In event of an exception during add the
     * original content will be unchanged and the objects in the supplied
     * collection will be unaltered.
     *
     * @param c   collection to append
     * @return    the document on which the method was called
     * @throws IllegalAddException if any item in the collection
     *         already has a parent or is of an illegal type.
     */
    override fun addContent(c: Collection<Content>): Document {
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
    override fun addContent(index: Int, child: Content): Document {
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
     * @param c  collection to insert
     * @return            the parent on which the method was called
     * @throws IndexOutOfBoundsException if index is negative or beyond
     *         the current number of children
     * @throws IllegalAddException if any item in the collection
     *         already has a parent or is of an illegal type.
     */
    override fun addContent(index: Int, c: Collection<Content>): Document {
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

    /**
     * This will return all content for the `Document`.
     * The returned list is "live" in document order and changes to it
     * affect the document's actual content.
     *
     *
     * Sequential traversal through the List is best done with a Iterator
     * since the underlying implement of List.size() may require walking the
     * entire list.
     *
     *
     * @return `List` - all Document content
     * @throws IllegalStateException if the root element hasn't been set
     */
    override var content: MutableList<Content>
        @JvmSynthetic
        set(v) {
            setContent(v)
        }
        get() {
            if (!hasRootElement())
                throw IllegalStateException("Root element not set")
            return contentList
        }

    /**
     * Return a filtered view of this `Document`'s content.
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
     * @return `List` - filtered Document content
     * @throws IllegalStateException if the root element hasn't been set
     */
    override fun <F : Content> getContent(filter: Filter<F>): MutableList<F> {
        if (!hasRootElement())
            throw IllegalStateException("Root element not set")
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
        val itr = contentList.getView(filter).iterator()
        while (itr.hasNext()) {
            val child = itr.next()
            old.add(child)
            itr.remove()
        }
        return old
    }

    /**
     * This sets the content of the `Document`.  The supplied
     * List should contain only objects of type `Element`,
     * `Comment`, and `ProcessingInstruction`.
     *
     *
     * When all objects in the supplied List are legal and before the new
     * content is added, all objects in the old content will have their
     * parentage set to null (no parent) and the old content list will be
     * cleared. This has the effect that any active list (previously obtained
     * with a call to [content]) will also
     * change to reflect the new content.  In addition, all objects in the
     * supplied List will have their parentage set to this document, but the
     * List itself will not be "live" and further removals and additions will
     * have no effect on this document content. If the user wants to continue
     * working with a "live" list, then a call to setContent should be
     * followed by a call to [content] to
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
     * @param newContent `List` of content to set
     * @return this document modified
     * @throws IllegalAddException if the List contains objects of
     *         illegal types or with existing parentage.
     */
    open fun setContent(newContent: Collection<Content>?): Document {
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
     * @return this document instance
     * @throws IllegalAddException if the supplied child is already attached
     *                             or not legal content for this parent.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than the current number of children.
     */
    open fun setContent(index: Int, child: Content): Document {
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
     * @param collection - collection of content to add.
     * @return object on which the method was invoked
     * @throws IllegalAddException if the collection contains objects of
     *         illegal types.
     * @throws IndexOutOfBoundsException if index is negative or greater
     *         than the current number of children.
     */
    open fun setContent(index: Int, collection: Collection<Content>): Document {
        contentList.removeAt(index)
        contentList.addAll(index, collection)
        return this
    }

    override fun removeContent(child: Content): Boolean {
        return contentList.remove(child)
    }

    override fun removeContent(index: Int): Content? {
        return contentList.removeAt(index)
    }

    /**
     * Set this document's content to be the supplied child.
     *
     * If the supplied child is legal content for a Document and before
     * it is added, all content in the current content list will
     * be cleared and all current children will have their parentage set to
     * null.
     *
     * This has the effect that any active list (previously obtained with
     * a call to one of the [content] methods will also change
     * to reflect the new content.  In addition, all content in the supplied
     * collection will have their parentage set to this Document.  If the user
     * wants to continue working with a **"live"** list of this Document's
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
     *                             or not legal content for this parent
     */
    open fun setContent(child: Content): Document {
        contentList.clear()
        contentList.add(child)
        return this
    }

    /**
     * This returns a `String` representation of the
     * `Document`, suitable for debugging. If the XML
     * representation of the `Document` is desired,
     * [fi.sykero.kdom.output.XMLOutputter.outputString]
     * should be used.
     *
     * @return `String` - information about the
     *         `Document`
     */
    override fun toString(): String {
        val stringForm = StringBuilder()
            .append("[Document: ")

        val docType = docType
        if (docType != null) {
            stringForm.append(docType.toString())
                .append(", ")
        } else {
            stringForm.append(" No DOCTYPE declaration, ")
        }

        val rootElement = if (hasRootElement()) rootElement else null
        if (rootElement != null) {
            stringForm.append("Root is ")
                .append(rootElement.toString())
        } else {
            stringForm.append(" No root element") // shouldn't happen
        }

        stringForm.append("]")

        return stringForm.toString()
    }

    /**
     * This tests for equality of this `Document` to the supplied
     * `Object`.
     *
     * @param other `Object` to compare to
     * @return `boolean` whether the `Document` is
     *         equal to the supplied `Object`
     */
    final override fun equals(other: Any?): Boolean {
        return (other === this)
    }

    /**
     * This returns the hash code for this `Document`.
     *
     * @return `int` hash code
     */
    final override fun hashCode(): Int {
        return super.hashCode()
    }

    /**
     * This will return a deep clone of this `Document`.
     *
     * @return `Object` clone of this `Document`
     */
    protected override fun shallowCopy(): Document {
        val copy = Document()
        copy.contentList = contentList
        copy.baseURI = baseURI
        copy.propertyMap = propertyMap
        return copy
    }

    public override fun clone(): Document {
        val doc = super.clone() as Document

        // The clone has a reference to this object's content list, so
        // overwrite with a empty list
        doc.contentList = ContentList(doc)

        // Add the cloned content to clone

        for (i in 0 until contentList.size) {
            val obj: Any = contentList[i]
            if (obj is Element) {
                val element = obj.clone()
                doc.contentList.add(element)
            } else if (obj is Comment) {
                val comment = obj.clone()
                doc.contentList.add(comment)
            } else if (obj is ProcessingInstruction) {
                val pi = obj.clone()
                doc.contentList.add(pi)
            } else if (obj is DocType) {
                val dt = obj.clone()
                doc.contentList.add(dt)
            }
        }

        return doc
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
     * applying the Filter to return only elements that match the filter rule.
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
     * Always returns null, Document cannot have a parent.
     * @return null
     */
    override val parent: Parent?
        get() = null // documents never have parents


    /**
     * Always returns this Document Instance
     * @return 'this' because this Document is its own Document
     */
    override val document: Document
        get() = this

    /**
     * Assigns an arbitrary object to be associated with this document under
     * the given "id" string.  Null values are permitted.  'id' Strings beginning
     * with "http://www.jdom.org/ are reserved for JDOM use. Properties set with
     * this method will not be serialized with the rest of this Document, should
     * serialization need to be done.
     *
     * @param id     the id of the stored `Object`
     * @param value  the `Object` to store
     */
    fun setProperty(id: String?, value: Any?) {
        if (propertyMap == null) {
            propertyMap = HashMap<String?, Any?>()
        }
        propertyMap!!.put(id, value)
    }

    /**
     * Returns the object associated with this document under the given "id"
     * string, or null if there is no binding or if the binding explicitly
     * stored a null value.
     *
     * @param id   the id of the stored `Object` to return
     * @return     the `Object` associated with the given id
     */
    fun getProperty(id: String?): Any? {
        if (propertyMap == null) {
            return null
        }
        return propertyMap!![id]
    }

    @Throws(IllegalAddException::class)
    override fun canContainContent(content: Content, index: Int, replace: Boolean) {
        if (content is Element) {
            val cre = contentList.indexOfFirstElement()
            if (replace && cre == index) {
                return
            }
            if (cre >= 0) {
                throw IllegalAddException(
                    "Cannot add a second root element, only one is allowed"
                )
            }
            if (contentList.indexOfDocType() >= index) {
                throw IllegalAddException(
                    "A root element cannot be added before the DocType"
                )
            }
        }
        if (content is DocType) {
            val cdt = contentList.indexOfDocType()
            if (replace && cdt == index) {
                // It's OK to replace an existing DocType
                return
            }
            if (cdt >= 0) {
                throw IllegalAddException(
                    "Cannot add a second doctype, only one is allowed"
                )
            }
            val firstElt = contentList.indexOfFirstElement()
            if (firstElt != -1 && firstElt < index) {
                throw IllegalAddException(
                    "A DocType cannot be added after the root element"
                )
            }
        }

        if (content is CDATA) {
            throw IllegalAddException("A CDATA is not allowed at the document root")
        }

        if (content is Text) {
            if (Verifier.isAllXMLWhitespace(content.text)) {
                // only whitespace, not a problem.
                return
            }
            throw IllegalAddException("A Text is not allowed at the document root")
        }

        if (content is EntityRef) {
            throw IllegalAddException("An EntityRef is not allowed at the document root")
        }

    }

    /**
     * Get the Namespaces that are in-scope on this Document.
     *
     * Document always has exactly two Namespaces in-scope:
     * [Namespace.NO_NAMESPACE] and [Namespace.XML_NAMESPACE].
     *
     * These namespaces are always introduced by the Document, and thus they are
     * both returned by [namespacesIntroduced], and additionally
     * [namespacesInherited] will always be empty.
     *
     * **Description copied from**
     * [NamespaceAware.namespacesInScope]:
     */
    override val namespacesInScope: List<Namespace>
        get() = unmodifiableList(
            listOf(Namespace.NO_NAMESPACE, Namespace.XML_NAMESPACE)
        )

    override val namespacesIntroduced: List<Namespace>
        get() = unmodifiableList(
            listOf(Namespace.NO_NAMESPACE, Namespace.XML_NAMESPACE)
        )

    override val namespacesInherited: List<Namespace>
        get() = emptyList()


}
