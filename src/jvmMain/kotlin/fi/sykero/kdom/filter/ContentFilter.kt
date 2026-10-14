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

package fi.sykero.kdom.filter

import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text

/**
 * A general purpose Filter able to represent all legal JDOM objects or a
 * specific subset. Filtering is accomplished by way of a filtering mask in
 * which each bit represents whether a JDOM object is visible or not.
 * For example to view all Text and CDATA nodes in the content of element x.
 * ```
 *      Filter filter = new ContentFilter(ContentFilter.TEXT |
 *                                        ContentFilter.CDATA);
 *      List content = x.getContent(filter);
 * ```
 *
 * For those who don't like bit-masking, set methods are provided as an
 * alternative.  For example to allow everything except Comment nodes.
 * ```
 *      Filter filter =  new ContentFilter();
 *      filter.setCommentVisible(false);
 *      List content = x.getContent(filter);
 * ```
 *
 * The default is to allow all valid JDOM objects.
 *
 * @author Bradley S. Huffman
 */
open class ContentFilter : AbstractFilter<Content> {

    /** The JDOM object mask  */
    private var mask: Int = 0

    /**
     * Default constructor that allows any legal JDOM objects.
     */
    constructor() : super() {
        setDefaultMask()
    }

    /**
     * Set whether all JDOM objects are visible or not.
     *
     * @param allVisible `true` all JDOM objects are visible,
     *                   `false` all JDOM objects are hidden.
     */
    constructor(allVisible: Boolean) : super() {
        if (allVisible) {
            setDefaultMask()
        } else {
            mask = mask and mask.inv()
        }
    }

    /**
     * Filter out JDOM objects according to a filtering mask.
     *
     * @param mask Mask of JDOM objects to allow.
     */
    constructor(mask: Int) : super() {
        filterMask = mask
    }

    /**
     * Return current filtering mask.
     * Set filtering mask: the new filtering mask is applied to the default mask.
     * (The mask is stored in the private field `mask`; jdom2 called it `filterMask`.)
     */
    var filterMask: Int
        get() = mask
        set(mask) {
            setDefaultMask()
            this.mask = this.mask and mask
        }

    /**
     * Set this filter to allow all legal JDOM objects.
     */
    fun setDefaultMask() {
        mask = ELEMENT or CDATA or TEXT or COMMENT or
                PI or ENTITYREF or DOCUMENT or DOCTYPE
    }

    /**
     * Set filter to match only JDOM objects that are legal
     * document content.
     */
    fun setDocumentContent() {
        mask = ELEMENT or COMMENT or PI or DOCTYPE
    }

    /**
     * Set filter to match only JDOM objects that are legal
     * element content.
     */
    fun setElementContent() {
        mask = ELEMENT or CDATA or TEXT or
                COMMENT or PI or ENTITYREF
    }

    /**
     * Set visibility of `Element` objects.
     *
     * @param visible whether Elements are visible, `true`
     *        if yes, `false` if not
     */
    fun setElementVisible(visible: Boolean) {
        if (visible) {
            mask = mask or ELEMENT
        } else {
            mask = mask and ELEMENT.inv()
        }
    }

    /**
     * Set visibility of `CDATA` objects.
     *
     * @param visible whether CDATA nodes are visible, `true`
     *        if yes, `false` if not
     */
    fun setCDATAVisible(visible: Boolean) {
        if (visible) {
            mask = mask or CDATA
        } else {
            mask = mask and CDATA.inv()
        }
    }

    /**
     * Set visibility of `Text` objects.
     *
     * @param visible whether Text nodes are visible, `true`
     *        if yes, `false` if not
     */
    fun setTextVisible(visible: Boolean) {
        if (visible) {
            mask = mask or TEXT
        } else {
            mask = mask and TEXT.inv()
        }
    }

    /**
     * Set visibility of `Comment` objects.
     *
     * @param visible whether Comments are visible, `true`
     *        if yes, `false` if not
     */
    fun setCommentVisible(visible: Boolean) {
        if (visible) {
            mask = mask or COMMENT
        } else {
            mask = mask and COMMENT.inv()
        }
    }

    /**
     * Set visibility of `ProcessingInstruction` objects.
     *
     * @param visible whether ProcessingInstructions are visible,
     *        `true` if yes, `false` if not
     */
    fun setPIVisible(visible: Boolean) {
        if (visible) {
            mask = mask or PI
        } else {
            mask = mask and PI.inv()
        }
    }

    /**
     * Set visibility of `EntityRef` objects.
     *
     * @param visible whether EntityRefs are visible, `true`
     *        if yes, `false` if not
     */
    fun setEntityRefVisible(visible: Boolean) {
        if (visible) {
            mask = mask or ENTITYREF
        } else {
            mask = mask and ENTITYREF.inv()
        }
    }

    /**
     * Set visibility of `DocType` objects.
     *
     * @param visible whether the DocType is visible, `true`
     *        if yes, `false` if not
     */
    fun setDocTypeVisible(visible: Boolean) {
        if (visible) {
            mask = mask or DOCTYPE
        } else {
            mask = mask and DOCTYPE.inv()
        }
    }

    /**
     * Check to see if the object matches according to the filter mask.
     *
     * @param content The object to verify.
     * @return `true` if the objected matched a predfined
     *           set of rules.
     */
    override fun filter(content: Any?): Content? {
        if (content == null || !Content::class.java.isInstance(content)) {
            return null
        }

        val c = content as Content

        if (c is Element) {
            return if ((mask and ELEMENT) != 0) c else null
        } else if (c is CDATA) { // must come before Text check
            return if ((mask and CDATA) != 0) c else null
        } else if (c is Text) {
            return if ((mask and TEXT) != 0) c else null
        } else if (c is Comment) {
            return if ((mask and COMMENT) != 0) c else null
        } else if (c is ProcessingInstruction) {
            return if ((mask and PI) != 0) c else null
        } else if (c is EntityRef) {
            return if ((mask and ENTITYREF) != 0) c else null
        }
        //        else if (obj instanceof Document) {
        //            return (filterMask & DOCUMENT) != 0 ? obj : null;
        //        }
        else if (c is DocType) {
            return if ((mask and DOCTYPE) != 0) c else null
        }

        return null
    }

    /**
     * Returns whether the two filters are equivalent (i&#46;e&#46; the
     * matching mask values are identical).
     *
     * @param  other                 the object to compare against
     * @return                     whether the two filters are equal
     */
    override fun equals(other: Any?): Boolean {
        // Generated by IntelliJ
        if (this === other) return true
        if (other !is ContentFilter) return false

        val filter: ContentFilter = other

        if (mask != filter.mask) return false

        return true
    }

    override fun hashCode(): Int {
        // Generated by IntelliJ
        return mask
    }

    companion object {
        /**
         * JDOM2 Serialization: Default mechanism
         */
        private const val serialVersionUID = 200L

        /** Mask for JDOM [Element] objects  */
        const val ELEMENT: Int = 1

        /** Mask for JDOM [CDATA] objects  */
        const val CDATA: Int = 2

        /** Mask for JDOM [Text] objects  */
        const val TEXT: Int = 4

        /** Mask for JDOM [Comment] objects  */
        const val COMMENT: Int = 8

        /** Mask for JDOM [ProcessingInstruction] objects  */
        const val PI: Int = 16

        /** Mask for JDOM [EntityRef] objects  */
        const val ENTITYREF: Int = 32

        /** Mask for JDOM [fi.sykero.kdom.Document] object  */
        const val DOCUMENT: Int = 64

        /** Mask for JDOM [DocType] object  */
        const val DOCTYPE: Int = 128
    }
}
