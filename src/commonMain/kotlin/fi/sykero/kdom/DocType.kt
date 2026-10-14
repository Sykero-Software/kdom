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
import fi.sykero.kdom.internal.unsafeNull

/**
 * An XML DOCTYPE declaration.  Method allow the user to get and set the
 * root element name, public id, system id and internal subset.
 *
 * @author Brett McLaughlin
 * @author Jason Hunter
 */
open class DocType : Content {

    /**
     * The element being constrained
     * This will retrieve the element name being constrained.
     * (`null` for a raw instance made by the protected no-args constructor. Was the protected
     * field `elementName`.)
     *
     * @return `String` - element name for DOCTYPE
     */
    var elementName: String
        get() = unsafeNull(elementNameField)
        @JvmSynthetic
        set(v) {
            setElementName(v)
        }

    private var elementNameField: String? = null

    /**
     * The public ID of the DOCTYPE
     * This will retrieve the public ID of an externally
     * referenced DTD, or an empty `String` if
     * none is referenced.
     * (May be null. Was the protected field `publicID`.)
     *
     * @return `String` - public ID of referenced DTD.
     */
    var publicID: String?
        get() = publicIDField
        @JvmSynthetic
        set(v) {
            setPublicID(v)
        }

    private var publicIDField: String? = null

    /**
     * The system ID of the DOCTYPE
     * This will retrieve the system ID of an externally
     * referenced DTD, or an empty `String` if
     * none is referenced.
     * (May be null. Was the protected field `systemID`.)
     *
     * @return `String` - system ID of referenced DTD.
     */
    var systemID: String?
        get() = systemIDField
        @JvmSynthetic
        set(v) {
            setSystemID(v)
        }

    private var systemIDField: String? = null

    /**
     * The internal subset of the DOCTYPE.
     * This sets the data for the internal subset, and returns it.
     * (May be null. Was the protected field `internalSubset`, with `setInternalSubset(String)`
     * and `getInternalSubset()`.)
     */
    var internalSubset: String? = null

    /**
     * Default, no-args constructor for implementations to use if needed.
     */
    protected constructor() : super(CType.DocType)

    /*
     * XXX:
     *   We need to take care of entities and notations here.
     */

    /**
     * This will create the `DocType` with
     * the specified element name and a reference to an
     * external DTD.
     *
     * @param elementName `String` name of
     *        element being constrained.
     * @param publicID `String` public ID of
     *        referenced DTD
     * @param systemID `String` system ID of
     *        referenced DTD
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal or the public ID is not a legal public ID.
     * @throws IllegalNameException if the given root element name is not a
     *         legal XML element name.
     */
    constructor(elementName: String?, publicID: String?, systemID: String?) : super(CType.DocType) {
        setElementName(elementName)
        setPublicID(publicID)
        setSystemID(systemID)
    }

    /**
     * This will create the `DocType` with
     * the specified element name and reference to an
     * external DTD.
     *
     * @param elementName `String` name of
     *        element being constrained.
     * @param systemID `String` system ID of
     *        referenced DTD
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal.
     * @throws IllegalNameException if the given root element name is not a
     *         legal XML element name.
     */
    constructor(elementName: String?, systemID: String?) : this(elementName, null, systemID)

    /**
     * This will create the `DocType` with
     * the specified element name
     *
     * @param elementName `String` name of
     *        element being constrained.
     * @throws IllegalNameException if the given root element name is not a
     *         legal XML element name.
     */
    constructor(elementName: String?) : this(elementName, null, null)

    /**
     * This will set the root element name declared by this
     * DOCTYPE declaration.
     *
     * @return this `DocType` instance
     * @param elementName `String` name of
     *        root element being constrained.
     * @throws IllegalNameException if the given root element name is not a
     *         legal XML element name.
     */
    open fun setElementName(elementName: String?): DocType {
        // This can contain a colon so we use checkXMLName()
        // instead of checkElementName()
        val reason = Verifier.checkXMLName(elementName)
        if (reason != null) {
            throw IllegalNameException(elementName, "DocType", reason)
        }
        this.elementNameField = elementName
        return this
    }

    /**
     * This will set the public ID of an externally
     * referenced DTD.
     *
     * @param publicID id to set
     * @return DocType `DocType` this DocType object
     * @throws IllegalDataException if the given public ID is not a legal
     *         public ID.
     */
    open fun setPublicID(publicID: String?): DocType {
        val reason = Verifier.checkPublicID(publicID)
        if (reason != null) {
            throw IllegalDataException(publicID, "DocType", reason)
        }
        this.publicIDField = publicID

        return this
    }

    /**
     * This will set the system ID of an externally
     * referenced DTD.
     *
     * @param systemID id to set
     * @return systemID `String` system ID of
     *                  referenced DTD.
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal.
     */
    open fun setSystemID(systemID: String?): DocType {
        val reason = Verifier.checkSystemLiteral(systemID)
        if (reason != null) {
            throw IllegalDataException(systemID, "DocType", reason)
        }
        this.systemIDField = systemID

        return this
    }

    /**
     * Returns the empty string since doctypes don't have an XPath
     * 1.0 string value.
     * @return the empty string
     */
    override val value: String
        get() = "" // doctypes don't have an XPath string value

    /**
     * This returns a `String` representation of the
     * `DocType`, suitable for debugging.
     *
     * @return `String` - information about the
     *         `DocType`
     */
    override fun toString(): String {
        return StringBuilder()
            .append("[DocType: ")
            .append(fi.sykero.kdom.output.XMLOutputter().outputString(this))
            .append("]")
            .toString()
    }

    protected override fun shallowCopy(): DocType {
        val copy = DocType()
        copy.elementNameField = elementNameField
        copy.publicIDField = publicIDField
        copy.systemIDField = systemIDField
        copy.internalSubset = internalSubset
        return copy
    }

    public override fun clone(): DocType {
        return super.clone() as DocType
    }

    override fun detach(): DocType {
        return super.detach() as DocType
    }

    internal override fun setParent(parent: Parent?): DocType {
        return super.setParent(parent) as DocType
    }

    override val parent: Document?
        get() =
            // because DocType can only be attached to a Document.
            super.parent as Document?

}
