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


/**
 * Superclass for JDOM objects which can be legal child content
 * of [fi.sykero.kdom.Parent] nodes.
 *
 * @see fi.sykero.kdom.Comment
 * @see fi.sykero.kdom.DocType
 * @see fi.sykero.kdom.Element
 * @see fi.sykero.kdom.EntityRef
 * @see fi.sykero.kdom.Parent
 * @see fi.sykero.kdom.ProcessingInstruction
 * @see fi.sykero.kdom.Text
 *
 * @author Bradley S. Huffman
 * @author Jason Hunter
 * @author Rolf Lear
 */
abstract class Content protected constructor(type: CType) : CloneBase(), NamespaceAware {

    /**
     * An enumeration useful for identifying content types without
     * having to do `instanceof` type conditionals.
     *
     * `instanceof` is not a particularly safe way to test content
     * in JDOM because CDATA extends Text ( a CDATA instance is an
     * `instanceof` Text).
     *
     * This CType enumeration provides a direct and sure mechanism for
     * identifying JDOM content.
     *
     * These can be used in switch-type statements too (which is much neater
     * than chained if (instanceof) else if (instanceof) else .... expressions):
     *
     * ```
     *    switch(content.getCType()) {
     *        case Text :
     *             .....
     *             break;
     *        case CDATA :
     *             .....
     *             break;
     *        ....
     *    }
     * ```
     *
     * @author Rolf Lear
     * @since JDOM2
     */
    enum class CType {
        /** Represents [Comment] content  */
        Comment,

        /** Represents [Element] content  */
        Element,

        /** Represents [ProcessingInstruction] content  */
        ProcessingInstruction,

        /** Represents [EntityRef] content  */
        EntityRef,

        /** Represents [Text] content  */
        Text,

        /** Represents [CDATA] content  */
        CDATA,

        /** Represents [DocType] content  */
        DocType
    }

    /**
     * The parent [Parent] of this Content.
     * Note that the field is not serialized, thus deserialized Content
     * instances are 'detached'
     *
     * (Was the protected field `parent`; renamed because `parent` is now the property. `internal` +
     * `@JvmField` so that `ContentList` and the other tree classes can set it, as package access did in Java.)
     */
    @JvmField
    internal var parentRef: Parent? = null

    /**
     * All content has an enumerated type expressing the type of content.
     * This makes it possible to use switch-type statements on the content.
     * (The content type enumerate value for this Content; was the protected final field `ctype`.)
     *
     * @return A CType enumerated value representing this content.
     */
    val cType: CType
        get() = ctype

    /**
     * The content type enumerate value for this Content
     * (Kept as the protected final field `ctype` so the default Java serialization form is unchanged.)
     * @serialField This is an Enum, and cannot be null.
     */
    @JvmField
    protected val ctype: CType = type

    /**
     * Detaches this child from its parent or does nothing if the child
     * has no parent.
     *
     * This method can be overridden by particular Content subclasses to return
     * a specific type of Content (co-variant return type). All overriding
     * subclasses **must** call `super.detach()`;
     *
     * @return this child detached
     */
    open fun detach(): Content {
        parentRef?.removeContent(this)
        return this
    }

    /**
     * Return this child's parent, or null if this child is currently
     * not attached. The parent can be either an [Element]
     * or a [Document].
     *
     * This method can be overridden by particular Content subclasses to return
     * a specific type of Parent (co-variant return type). All overriding
     * subclasses **must** call `super.getParent()`;
     *
     * @return this child's parent or null if none
     */
    open val parent: Parent?
        get() = parentRef

    /**
     * A convenience method that returns any parent element for this element,
     * or null if the element is unattached or is a root element.  This was the
     * original behavior of getParent() in JDOM Beta 9 which began returning
     * Parent in Beta 10.  This method provides a convenient upgrade path for
     * JDOM Beta 10 and 1.0 users.
     *
     * @return the containing Element or null if unattached or a root element
     */
    val parentElement: Element?
        get() {
            val pnt = parent
            return if (pnt is Element) pnt else null
        }

    /**
     * Sets the parent of this Content. The caller is responsible for removing
     * any pre-existing parentage.
     *
     * This method can be overridden by particular Content subclasses to return
     * a specific type of Content (co-variant return type). All overriding
     * subclasses **must** call `super.setParent(Parent)`;
     *
     * (Was protected, i.e. package access in Java; public here because `ContentList` and `Element`
     * call it on `Content` receivers, which Kotlin's `protected` does not allow.)
     *
     * @param  parent              new parent element
     * @return                     the target element
     */
    internal open fun setParent(parent: Parent?): Content {
        this.parentRef = parent
        return this
    }

    /**
     * Return this child's owning document or null if the branch containing
     * this child is currently not attached to a document.
     *
     * @return this child's owning document or null if none
     */
    open val document: Document?
        get() {
            val p = parentRef ?: return null
            return p.document
        }

    /**
     * Returns the XPath 1.0 string value of this child.
     *
     * @return xpath string value of this child.
     */
    abstract val value: String

    public override fun clone(): Content {
        val c = super.clone() as Content
        c.parentRef = null
        return c
    }

    /**
     * This tests for equality of this Content object to the supplied object.
     * Content items are considered equal only if they are referentially equal
     * (i&#46;e&#46; the same object).  User code may choose to compare objects
     * based on their properties instead.
     *
     * @param other `Object` to compare to.
     * @return `boolean` - whether the `Content` is
     *         equal to the supplied `Object`.
     */
    final override fun equals(other: Any?): Boolean {
        return other === this
    }

    /**
     * This returns the hash code for this `Content` item.
     *
     * @return `int` - hash code.
     */
    final override fun hashCode(): Int {
        return super.hashCode()
    }

    override val namespacesInScope: List<Namespace>
        get() {
            // Element class will override this method to do it differently.
            val emt = parentElement ?: return listOf(Namespace.XML_NAMESPACE)
            return emt.namespacesInScope
        }

    override val namespacesIntroduced: List<Namespace>
        get() = emptyList()

    override val namespacesInherited: List<Namespace>
        get() = namespacesInScope // Element class will override
}
