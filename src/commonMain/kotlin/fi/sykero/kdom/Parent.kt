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

import fi.sykero.kdom.filter.Filter
import fi.sykero.kdom.filter.Filters
import fi.sykero.kdom.util.IteratorIterable

/**
 * Interface for JDOM objects which are allowed to contain
 * [Content] content - [Element] and [Document].
 *
 * @see fi.sykero.kdom.Content
 * @see fi.sykero.kdom.Document
 * @see fi.sykero.kdom.Element
 *
 * @author Bradley S. Huffman
 * @author Jason Hunter
 * @author Rolf Lear
 */
interface Parent : NamespaceAware {

    /**
     * Returns the number of children in this parent's content list.
     * Children may be any [Content] type.
     *
     * @return number of children
     */
    val contentSize: Int

    /**
     * Returns the index of the supplied child in the content list,
     * or -1 if not a child of this parent.
     *
     * @param child  child to search for
     * @return       index of child, or -1 if not found
     */
    fun indexOf(child: Content): Int

    //    /**
    //     * Starting at the given index (inclusive), returns the index of
    //     * the first child matching the supplied filter, or -1
    //     * if none is found.
    //     *
    //     * @return index of child, or -1 if none found
    //     */
    //    int indexOf(int index, Filter filter);

    /**
     * Returns a list containing detached clones of this parent's content list.
     *
     * @return list of cloned child content
     */
    fun cloneContent(): MutableList<Content>

    /**
     * Returns the child at the given index.
     *
     * @param index location of desired child
     * @return child at the given index
     * @throws IndexOutOfBoundsException if index is negative or beyond
     *         the current number of children
     * @throws IllegalStateException if parent is a Document
     *         and the root element is not set
     */
    fun getContent(index: Int): Content

    /**
     * Returns the full content of this parent as a [java.util.List]
     * which contains objects of type [Content]. The returned list is
     * **"live"** and in document order. Any modifications
     * to it affect the element's actual contents. Modifications are checked
     * for conformance to XML 1.0 rules.
     *
     * Sequential traversal through the List is best done with an Iterator
     * since the underlying implement of [java.util.List.size] may
     * require walking the entire list and indexed lookups may require
     * starting at the beginning each time.
     *
     * @return a list of the content of the parent
     * @throws IllegalStateException if parent is a Document
     *         and the root element is not set
     */
    val content: MutableList<Content>

    /**
     * Returns as a [java.util.List] the content of
     * this parent that matches the supplied filter. The returned list is
     * **"live"** and in document order. Any modifications to it affect
     * the element's actual contents. Modifications are checked for
     * conformance to XML 1.0 rules.
     *
     * Sequential traversal through the List is best done with an Iterator
     * since the underlying implement of [java.util.List.size] may
     * require walking the entire list and indexed lookups may require
     * starting at the beginning each time.
     * @param <E> The Generic type of the returned content (the Filter's type)
     *
     * @param  filter filter to apply.
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return a list of the content of the parent matching the filter
     * @throws IllegalStateException if parent is a Document
     *         and the root element is not set
     */
    fun <E : Content> getContent(filter: Filter<E>): MutableList<E>

    /**
     * Removes all content from this parent and returns the detached
     * children.
     *
     * @return list of the old content detached from this parent
     */
    fun removeContent(): MutableList<Content>

    /**
     * Removes from this parent all child content matching the given filter
     * and returns a list of the detached children.
     * @param <E> The Generic type of the content to remove.
     *
     * @param  filter filter to apply
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return list of the detached children matching the filter
     */
    fun <E : Content> removeContent(filter: Filter<E>): MutableList<E>

    /**
     * Removes a single child node from the content list.
     *
     * @param  child  child to remove
     * @return whether the removal occurred
     */
    fun removeContent(child: Content): Boolean

    /**
     * Removes and returns the child at the given
     * index, or returns null if there's no such child.
     *
     * @param index index of child to remove
     * @return detached child at given index or null if no
     * @throws IndexOutOfBoundsException if index is negative or beyond
     *             the current number of children
     */
    fun removeContent(index: Int): Content?

    /**
     * Obtain a deep, unattached copy of this parent and its children.
     *
     * @return a deep copy of this parent and its children.
     */
    fun clone(): Any

    /**
     * Returns an [java.util.Iterator] that walks over all descendants
     * in document order.
     *
     * Note that this method returns an IteratorIterable instance, which means
     * that you can use it either as an Iterator, or an Iterable, allowing both:
     *
     * ```
     *   for (Iterator<Content> it = parent.getDescendants();
     *           it.hasNext();) {
     *       Content c = it.next();
     *       ....
     *   }
     * ```
     * and
     * ```
     *   for (Content c : parent.getDescendants()) {
     *       ....
     *   }
     * ```
     * The Iterator version is most useful if you need to do list modification
     * on the iterator (using remove()), and for compatibility with JDOM 1.x
     *
     * @return an iterator to walk descendants
     */
    val descendants: IteratorIterable<Content>

    /**
     * Returns an [java.util.Iterator] that walks over all descendants
     * in document order applying the Filter to return only content that
     * match the filter rule.  With filters you can match only Elements,
     * only Comments, Elements or Comments, only Elements with a given name
     * and/or prefix, and so on.
     *
     * Note that this method returns an IteratorIterable instance, which means
     * that you can use it either as an Iterator, or an Iterable, allowing both:
     *
     * ```
     *   for (Iterator<Element> it = parent.getDescendants(Filters.element());
     *           it.hasNext();) {
     *       Element e = it.next();
     *       ....
     *   }
     * ```
     * and
     * ```
     *   for (Element e : parent.getDescendants(Filters.element())) {
     *       ....
     *   }
     * ```
     * The Iterator version is most useful if you need to do list modification
     * on the iterator (using remove()), and for compatibility with JDOM 1.x
     *
     * @param <E> The generic type of the returned descendant data
     * @param filter filter to select which descendants to see
     *        Note that the [Filters] class has a number of predefined, useful
     *        filters.
     * @return an iterator to walk descendants that match a filter
     */
    fun <E : Content> getDescendants(filter: Filter<E>): IteratorIterable<E>

    /**
     * Return this parent's parent, or null if this parent is currently
     * not attached to another parent. This is the same method as in Content but
     * also added to Parent to allow more easy up-the-tree walking.
     *
     * @return this parent's parent or null if none
     */
    val parent: Parent?

    /**
     * Return this parent's owning document or null if the branch containing
     * this parent is currently not attached to a document.
     *
     * @return this child's owning document or null if none
     */
    val document: Document?

    /**
     * Test whether this Parent instance can contain the specified content
     * at the specified position.
     * @param content The content to be checked
     * @param index The location where the content would be put.
     * @param replace true if the intention is to replace the content already at
     * 				the index.
     * @throws IllegalAddException if there is a problem with the content
     */
    @Throws(IllegalAddException::class)
    fun canContainContent(content: Content, index: Int, replace: Boolean)

    /**
     * Appends the child to the end of the content list.
     *
     * @param child   child to append to end of content list
     * @return        the Parent instance on which the method was called
     * @throws IllegalAddException if the given child already has a parent.
     */
    fun addContent(child: Content): Parent

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
    fun addContent(c: Collection<Content>): Parent

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
    fun addContent(index: Int, child: Content): Parent

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
    fun addContent(index: Int, c: Collection<Content>): Parent

}
