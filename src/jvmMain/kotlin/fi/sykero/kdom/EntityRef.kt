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
 * An XML entity reference. Methods allow the user to manage its name, public
 * id, and system id.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Philip Nelson
 * @author  Rolf Lear
 */
open class EntityRef : Content {

    /**
     * This returns the name of the `EntityRef`.
     * (The name of the `EntityRef`; `null` for a raw instance made by the protected
     * no-args constructor. Was the protected field `name`.)
     *
     * @return `String` - entity name.
     */
    var name: String? = null
        private set

    /**
     * This will return the publid ID of this `EntityRef`.
     * If there is no public ID, then this returns `null`.
     * (Was the protected field `publicID`.)
     *
     * @return public ID of this `EntityRef`
     */
    var publicID: String? = null
        private set

    /**
     * This will return the system ID of this `EntityRef`.
     * If there is no system ID, then this returns `null`.
     * (Was the protected field `systemID`.)
     *
     * @return system ID of this `EntityRef`
     */
    var systemID: String? = null
        private set

    /**
     * Default, no-args constructor for implementations to use if needed.
     */
    protected constructor() : super(CType.EntityRef)

    /**
     * This will create a new `EntityRef` with the supplied name.
     *
     * @param name `String` name of element.
     * @throws IllegalNameException if the given name is not a legal
     *         XML name.
     */
    constructor(name: String?) : this(name, null, null)

    /**
     * This will create a new `EntityRef`
     * with the supplied name and system id.
     *
     * @param name `String` name of element.
     * @param systemID system id of the entity reference being constructed
     * @throws IllegalNameException if the given name is not a legal
     *         XML name.
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal.
     */
    constructor(name: String?, systemID: String?) : this(name, null, systemID)

    /**
     * This will create a new `EntityRef`
     * with the supplied name, public id, and system id.
     *
     * @param name `String` name of element.
     * @param publicID public id of the entity reference being constructed
     * @param systemID system id of the entity reference being constructed
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal or the the given public ID is not a
     *         legal public ID
     * @throws IllegalNameException if the given name is not a legal
     *         XML name.
     */
    constructor(name: String?, publicID: String?, systemID: String?) : super(CType.EntityRef) {
        setName(name)
        setPublicID(publicID)
        setSystemID(systemID)
    }

    /**
     * Returns the empty string since entity references don't have an XPath
     * 1.0 string value.
     * @return the empty string
     */
    override val value: String
        get() = "" // entity references don't have XPath string values

    /**
     * This will set the name of this `EntityRef`.
     *
     * @param name new name of the entity
     * @return this `EntityRef` modified.
     * @throws IllegalNameException if the given name is not a legal
     *         XML name.
     */
    open fun setName(name: String?): EntityRef {
        // This can contain a colon so we use checkXMLName()
        // instead of checkElementName()
        val reason = Verifier.checkXMLName(name)
        if (reason != null) {
            throw IllegalNameException(name, "EntityRef", reason)
        }
        this.name = name
        return this
    }

    /**
     * This will set the public ID of this `EntityRef`.
     *
     * @param publicID new public id
     * @return this `EntityRef` modified.
     * @throws IllegalDataException if the given public ID is not a legal
     *         public ID.
     */
    open fun setPublicID(publicID: String?): EntityRef {
        val reason = Verifier.checkPublicID(publicID)
        if (reason != null) {
            throw IllegalDataException(publicID, "EntityRef", reason)
        }
        this.publicID = publicID
        return this
    }

    /**
     * This will set the system ID of this `EntityRef`.
     *
     * @param systemID new system id
     * @throws IllegalDataException if the given system ID is not a legal
     *         system literal.
     * @return this `EntityRef` modified.
     */
    open fun setSystemID(systemID: String?): EntityRef {
        val reason = Verifier.checkSystemLiteral(systemID)
        if (reason != null) {
            throw IllegalDataException(systemID, "EntityRef", reason)
        }
        this.systemID = systemID
        return this
    }

    /**
     * This returns a `String` representation of the
     * `EntityRef`, suitable for debugging.
     *
     * @return `String` - information about the
     *         `EntityRef`
     */
    override fun toString(): String {
        return StringBuilder()
            .append("[EntityRef: ")
            .append("&")
            .append(name)
            .append(";")
            .append("]")
            .toString()
    }

    override fun detach(): EntityRef {
        return super.detach() as EntityRef
    }

    override fun setParent(parent: Parent?): EntityRef {
        return super.setParent(parent) as EntityRef
    }

    override val parent: Element?
        get() =
            // because DocType can only be attached to a Document.
            super.parent as Element?

    override fun clone(): EntityRef {
        return super.clone() as EntityRef
    }

    private companion object {
        /**
         * JDOM2 Serialization. In this case, EntityRef is simple.
         */
        private const val serialVersionUID = 200L
    }
}
