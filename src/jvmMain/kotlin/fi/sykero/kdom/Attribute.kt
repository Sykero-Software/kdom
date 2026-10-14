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

import fi.sykero.kdom.internal.unsafeNull

import java.io.Serializable
import java.util.ArrayList
import java.util.Collections
import java.util.TreeMap

/**
 * An XML attribute. Methods allow the user to obtain the value of the attribute
 * as well as namespace and type information.
 *
 * **JDOM 1.x Compatibility Note:**<br>
 * The Attribute class in JDOM 1.x had a number of int Constants declared to
 * represent different Attribute Types. JDOM2 has introduced an AttributeType
 * enumeration instead. To facilitate compatibility and to simplify JDOM 1.x
 * migrations, the replacement AttributeType enums are referenced still using
 * the JDOM 1.x constant names. In JDOM 1.x these names referenced constant
 * int values. In JDOM2 these names reference Enum constants.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Elliotte Rusty Harold
 * @author  Wesley Biggs
 * @author  Victor Toni
 * @author  Rolf Lear
 */
open class Attribute : CloneBase, NamespaceAware, Serializable, Cloneable {

    /**
     * This will retrieve the local name of the
     * `Attribute`. For any XML attribute
     * which appears as
     * `[namespacePrefix]:[attributeName]`,
     * the local name of the attribute would be
     * `[attributeName]`. When the attribute
     * has no namespace, the local name is simply the attribute
     * name.
     *
     * To obtain the namespace prefix for this
     * attribute, the
     * `[namespacePrefix]`
     * method should be used.
     *
     * (The local name of the `Attribute`; `null` for a raw instance made by the protected
     * no-args constructor. Was the protected field `name`.)
     *
     * @return `String` - name of this attribute,
     *                               without any namespace prefix.
     */
    var name: String
        get() = unsafeNull(nameField)
        @JvmSynthetic
        set(v) {
            setName(v)
        }

    private var nameField: String? = null

    /**
     * This will return this `Attribute`'s
     * `[Namespace]`.
     * (Was the protected field `namespace`; not initialised for a raw instance made by the
     * protected no-args constructor.)
     *
     * @return `Namespace` - Namespace object for this `Attribute`
     */
    var namespace: Namespace
        get() = unsafeNull(namespaceField)
        @JvmSynthetic
        set(v) {
            setNamespace(v)
        }

    private var namespaceField: Namespace? = null

    /**
     * This will return the actual textual value of this
     * `Attribute`.  This will include all text
     * within the quotation marks.
     * (Was the protected field `value`; `null` for a raw instance.)
     *
     * @return `String` - value for this attribute.
     */
    var value: String
        get() = unsafeNull(valueField)
        @JvmSynthetic
        set(v) {
            setValue(v)
        }

    private var valueField: String? = null

    /**
     * This will return the declared type of this `Attribute`.
     * (Was the protected field `type`.)
     *
     * @return `AttributeType` - type for this attribute.
     */
    var attributeType: AttributeType
        get() = type
        @JvmSynthetic
        set(v) {
            setAttributeType(v)
        }

    /** The type of the `Attribute` (kept as `type`, a serialized field in jdom2) */
    private var type: AttributeType = AttributeType.UNDECLARED

    /**
     * Specified attributes are part of the XML,
     * unspecified attributes are 'defaulted' from a DTD.
     *
     * Get the 'specified' flag. True values indicate this attribute
     * was part of an XML document, false indicates it was defaulted
     * from a DTD.
     * (Was the protected field `specified`, with `isSpecified()` and `setSpecified(boolean)`.)
     * @since JDOM2
     */
    var isSpecified: Boolean
        get() = specified
        set(specified) {
            this.specified = specified
        }

    /** The 'specified' flag (kept as `specified`, a serialized field in jdom2) */
    private var specified: Boolean = true

    /**
     * The parent to which this Attribute belongs. Change it with
     * [setParent]
     * (Was the protected field `parent`; renamed because `parent` is now the property.
     * `internal` + `@JvmField` so that `AttributeList` can set it, as package access did in Java.)
     */
    @JvmField
    @Transient
    internal var parentRef: Element? = null

    /**
     * Default, no-args constructor for implementations to use if needed.
     */
    protected constructor() : super()

    /**
     * This will create a new `Attribute` with the
     * specified (local) name and value, and in the provided
     * `[Namespace]`.
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @param namespace `Namespace` namespace for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name or if if the new namespace is the default
     *         namespace. Attributes cannot be in a default namespace.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]).
     */
    constructor(name: String?, value: String?, namespace: Namespace?) :
        this(name, value, AttributeType.UNDECLARED, namespace)

    /**
     * This will create a new `Attribute` with the
     * specified (local) name, value, and type, and in the provided
     * `[Namespace]`.
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @param type `int` type for new attribute.
     * @param namespace `Namespace` namespace for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name or if if the new namespace is the default
     *         namespace. Attributes cannot be in a default namespace.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]) or
     *         if the given attribute type is not one of the
     *         supported types.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use Attribute(String, String, AttributeType, Namespace).")
    constructor(name: String?, value: String?, type: Int, namespace: Namespace?) :
        this(name, value, AttributeType.byIndex(type), namespace)

    /**
     * This will create a new `Attribute` with the
     * specified (local) name, value, and type, and in the provided
     * `[Namespace]`.
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @param type `AttributeType` for new attribute.
     * @param namespace `Namespace` namespace for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name or if if the new namespace is the default
     *         namespace. Attributes cannot be in a default namespace.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]) or
     *         if the given attribute type is not one of the
     *         supported types.
     */
    constructor(name: String?, value: String?, type: AttributeType?, namespace: Namespace?) : super() {
        setName(name)
        setValue(value)
        setAttributeType(type)
        setNamespace(namespace)
    }

    /**
     * This will create a new `Attribute` with the
     * specified (local) name and value, and does not place
     * the attribute in a `[Namespace]`.
     *
     * **Note**: This actually explicitly puts the
     * `Attribute` in the "empty" `Namespace`
     * (`[Namespace.NO_NAMESPACE]`).
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]).
     */
    constructor(name: String?, value: String?) :
        this(name, value, AttributeType.UNDECLARED, Namespace.NO_NAMESPACE)

    /**
     * This will create a new `Attribute` with the
     * specified (local) name, value and type, and does not place
     * the attribute in a `[Namespace]`.
     *
     * **Note**: This actually explicitly puts the
     * `Attribute` in the "empty" `Namespace`
     * (`[Namespace.NO_NAMESPACE]`).
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @param type `AttributeType` for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]) or
     *         if the given attribute type is not one of the
     *         supported types.
     */
    constructor(name: String?, value: String?, type: AttributeType?) :
        this(name, value, type, Namespace.NO_NAMESPACE)

    /**
     * This will create a new `Attribute` with the
     * specified (local) name, value and type, and does not place
     * the attribute in a `[Namespace]`.
     *
     * **Note**: This actually explicitly puts the
     * `Attribute` in the "empty" `Namespace`
     * (`[Namespace.NO_NAMESPACE]`).
     *
     * @param name `String` name of `Attribute`.
     * @param value `String` value for new attribute.
     * @param type `int` type for new attribute.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]) or
     *         if the given attribute type is not one of the
     *         supported types.
     */
    @Suppress("DEPRECATION")
    @Deprecated("Use Attribute(String, String, AttributeType).")
    constructor(name: String?, value: String?, type: Int) :
        this(name, value, type, Namespace.NO_NAMESPACE)

    /**
     * This will return the parent of this `Attribute`.
     * If there is no parent, then this returns `null`.
     * Use return-type covariance to override Content's getParent() method
     * to return an Element, not just a Parent
     *
     * @return parent of this `Attribute`
     */
    val parent: Element?
        get() = parentRef

    /**
     * Get this Attribute's Document.
     * @return The document to which this Attribute is associated, may be null.
     */
    val document: Document?
        get() = parentRef?.document

    /**
     * This sets the local name of the `Attribute`.
     *
     * @param name the new local name to set
     * @return `Attribute` - the attribute modified.
     * @throws IllegalNameException if the given name is illegal as an
     *         attribute name.
     */
    open fun setName(name: String?): Attribute {
        if (name == null) {
            throw NullPointerException(
                "Can not set a null name for an Attribute."
            )
        }
        val reason = Verifier.checkAttributeName(name)
        if (reason != null) {
            throw IllegalNameException(name, "attribute", reason)
        }
        this.nameField = name
        specified = true
        return this
    }

    /**
     * This will retrieve the qualified name of the `Attribute`.
     * For any XML attribute whose name is
     * `[namespacePrefix]:[elementName]`,
     * the qualified name of the attribute would be
     * everything (both namespace prefix and
     * element name). When the attribute has no
     * namespace, the qualified name is simply the attribute's
     * local name.
     *
     * To obtain the local name of the attribute, the
     * `[name]` method should be used.
     *
     * To obtain the namespace prefix for this attribute,
     * the `[namespacePrefix]`
     * method should be used.
     *
     * @return `String` - full name for this element.
     */
    val qualifiedName: String
        get() {
            // Note: Any changes here should be reflected in
            // XMLOutputter.printQualifiedName()
            val prefix = namespace.prefix

            // no prefix found
            if ("" == prefix) {
                return name
            }
            return StringBuilder(prefix)
                .append(':')
                .append(name)
                .toString()
        }

    /**
     * This will retrieve the namespace prefix of the
     * `Attribute`. For any XML attribute
     * which appears as
     * `[namespacePrefix]:[attributeName]`,
     * the namespace prefix of the attribute would be
     * `[namespacePrefix]`. When the attribute
     * has no namespace, an empty `String` is returned.
     *
     * @return `String` - namespace prefix of this
     *                               attribute.
     */
    val namespacePrefix: String
        get() = namespace.prefix

    /**
     * This returns the URI mapped to this `Attribute`'s
     * prefix. If no mapping is found, an empty `String` is
     * returned.
     *
     * @return `String` - namespace URI for this `Attribute`.
     */
    val namespaceURI: String
        get() = namespace.uri

    /**
     * This sets this `Attribute`'s `[Namespace]`.
     * If the provided namespace is null, the attribute will have no namespace.
     * The namespace must have a prefix.
     *
     * @param namespace the new namespace
     * @return `Element` - the element modified.
     * @throws IllegalNameException if the new namespace is the default
     *         namespace. Attributes cannot be in a default namespace.
     */
    open fun setNamespace(namespace: Namespace?): Attribute {
        var namespace = namespace
        if (namespace == null) {
            namespace = Namespace.NO_NAMESPACE
        }

        // Verify the attribute isn't trying to be in a default namespace
        // Attributes can't be in a default namespace
        if (namespace !== Namespace.NO_NAMESPACE &&
            "" == namespace.prefix
        ) {
            throw IllegalNameException(
                "", "attribute namespace",
                "An attribute namespace without a prefix can only be the " +
                        "NO_NAMESPACE namespace"
            )
        }
        this.namespaceField = namespace
        specified = true
        return this
    }

    /**
     * This will set the value of the `Attribute`.
     *
     * @param value `String` value for the attribute.
     * @return `Attribute` - this Attribute modified.
     * @throws IllegalDataException if the given attribute value is
     *         illegal character data (as determined by
     *         [fi.sykero.kdom.Verifier.checkCharacterData]).
     */
    open fun setValue(value: String?): Attribute {
        if (value == null) {
            throw NullPointerException(
                "Can not set a null value for an Attribute"
            )
        }
        val reason = Verifier.checkCharacterData(value)
        if (reason != null) {
            throw IllegalDataException(value, "attribute", reason)
        }
        this.valueField = value
        specified = true
        return this
    }

    /**
     * This will set the type of the `Attribute`.
     *
     * @param type `int` type for the attribute.
     * @return `Attribute` - this Attribute modified.
     * @throws IllegalDataException if the given attribute type is
     *         not one of the supported types.
     */
    open fun setAttributeType(type: AttributeType?): Attribute {
        this.type = type ?: AttributeType.UNDECLARED
        specified = true
        return this
    }

    /**
     * This will set the type of the `Attribute`.
     *
     * @param type `int` type for the attribute.
     * @return `Attribute` - this Attribute modified.
     * @throws IllegalDataException if the given attribute type is
     *         not one of the supported types.
     */
    @Suppress("DEPRECATION")
    @Deprecated("use setAttributeType(AttributeType)")
    open fun setAttributeType(type: Int): Attribute {
        setAttributeType(AttributeType.byIndex(type))
        return this
    }

    /**
     * This returns a `String` representation of the
     * `Attribute`, suitable for debugging.
     *
     * @return `String` - information about the
     *         `Attribute`
     */
    override fun toString(): String {
        return StringBuilder()
            .append("[Attribute: ")
            .append(qualifiedName)
            .append("=\"")
            .append(value)
            .append("\"")
            .append("]")
            .toString()
    }

    public override fun clone(): Attribute {
        val clone = super<CloneBase>.clone() as Attribute
        clone.parentRef = null
        return clone
    }

    /**
     * Detach this Attribute from its parent.
     * @return this Attribute (detached).
     */
    open fun detach(): Attribute {
        parentRef?.removeAttribute(this)
        return this
    }

    /**
     * Set this Attribute's parent. This is not public!
     * (Was protected, i.e. package access in Java; public here because `AttributeList` calls it.)
     * @param parent The parent to set
     * @return this Attribute (state may be indeterminate depending on whether
     * this has been included in the Element's list yet).
     */
    open fun setParent(parent: Element?): Attribute {
        this.parentRef = parent
        return this
    }


    /////////////////////////////////////////////////////////////////
    // Convenience Methods below here
    /////////////////////////////////////////////////////////////////

    /**
     * This gets the value of the attribute, in
     * `int` form, and if no conversion
     * can occur, throws a
     * `[DataConversionException]`
     *
     * @return `int` value of attribute.
     * @throws DataConversionException when conversion fails.
     */
    @get:Throws(DataConversionException::class)
    val intValue: Int
        get() {
            try {
                return value.trim { it <= ' ' }.toInt()
            } catch (e: NumberFormatException) {
                throw DataConversionException(name, "int")
            }
        }

    /**
     * This gets the value of the attribute, in
     * `long` form, and if no conversion
     * can occur, throws a
     * `[DataConversionException]`
     *
     * @return `long` value of attribute.
     * @throws DataConversionException when conversion fails.
     */
    @get:Throws(DataConversionException::class)
    val longValue: Long
        get() {
            try {
                return value.trim { it <= ' ' }.toLong()
            } catch (e: NumberFormatException) {
                throw DataConversionException(name, "long")
            }
        }

    /**
     * This gets the value of the attribute, in
     * `float` form, and if no conversion
     * can occur, throws a
     * `[DataConversionException]`
     *
     * @return `float` value of attribute.
     * @throws DataConversionException when conversion fails.
     */
    @get:Throws(DataConversionException::class)
    val floatValue: Float
        get() {
            try {
                // Avoid Float.parseFloat() to support JDK 1.1
                return java.lang.Float.valueOf(value.trim { it <= ' ' }).toFloat()
            } catch (e: NumberFormatException) {
                throw DataConversionException(name, "float")
            }
        }

    /**
     * This gets the value of the attribute, in
     * `double` form, and if no conversion
     * can occur, throws a
     * `[DataConversionException]`
     *
     * @return `double` value of attribute.
     * @throws DataConversionException when conversion fails.
     */
    @get:Throws(DataConversionException::class)
    val doubleValue: Double
        get() {
            try {
                // Avoid Double.parseDouble() to support JDK 1.1
                return java.lang.Double.valueOf(value.trim { it <= ' ' }).toDouble()
            } catch (e: NumberFormatException) {
                // Specially handle INF and -INF that Double.valueOf doesn't do
                val v = value.trim { it <= ' ' }
                if ("INF" == v) {
                    return Double.POSITIVE_INFINITY
                }
                if ("-INF" == v) {
                    return Double.NEGATIVE_INFINITY
                }
                throw DataConversionException(name, "double")
            }
        }

    /**
     * This gets the effective boolean value of the attribute, or throws a
     * `[DataConversionException]` if a conversion can't be
     * performed.  True values are: "true", "on", "1", and "yes".  False
     * values are: "false", "off", "0", and "no".  Values are trimmed before
     * comparison.  Values other than those listed here throw the exception.
     *
     * @return `boolean` value of attribute.
     * @throws DataConversionException when conversion fails.
     */
    @get:Throws(DataConversionException::class)
    val booleanValue: Boolean
        get() {
            val valueTrim = value.trim { it <= ' ' }
            if (
                (valueTrim.equals("true", ignoreCase = true)) ||
                (valueTrim.equals("on", ignoreCase = true)) ||
                (valueTrim.equals("1", ignoreCase = true)) ||
                (valueTrim.equals("yes", ignoreCase = true))
            ) {
                return true
            } else if (
                (valueTrim.equals("false", ignoreCase = true)) ||
                (valueTrim.equals("off", ignoreCase = true)) ||
                (valueTrim.equals("0", ignoreCase = true)) ||
                (valueTrim.equals("no", ignoreCase = true))
            ) {
                return false
            } else {
                throw DataConversionException(name, "boolean")
            }
        }

    /**
     * Get the namespaces that are in-scope on this Attribute.
     *
     * Attribute has peculiarities that affect the in-scope Namespaces because
     * there are conditions in which the Attribute's scope is different to its
     * parent Element's scope. Specifically, if the parent Element is in a
     * 'default' Namespace that is not the empty Namespace (e.g.
     * xmlns="someurl") and this Attribute is also in the default Namespace (has
     * no prefix - but for Attributes that means the Namespace URL is ""), then
     * this Attribute has a different namespace scope from its parent Element
     * because it does not include the 'someurl' Namespace.
     *
     * In the above conditions (no-prefix Attribute in an Element with a
     * non-empty no-prefix Namespace) this Attribute effectively re-binds the ""
     * prefix to the "" URL, thus the Attribute 'introduces' the Namespace.
     * It follows then that the getNamespacesIntroduced() will return a list
     * with the single member [Namespace.NO_NAMESPACE].
     *
     * Note that the Attribute's Namespace will always be reported first.
     *
     * **Description copied from**
     * [NamespaceAware.namespacesInScope]:
     */
    override val namespacesInScope: List<Namespace>
        get() {
            val p = parent
            if (p == null) {
                val ret = ArrayList<Namespace>(3)
                ret.add(namespace)
                ret.add(Namespace.XML_NAMESPACE)
                return Collections.unmodifiableList(ret)
            }
            return orderFirst(namespace, p.namespacesInScope)
        }

    override val namespacesIntroduced: List<Namespace>
        get() {
            if (parent == null) {
                return Collections.singletonList(namespace)
            }
            return Collections.emptyList()
        }

    override val namespacesInherited: List<Namespace>
        get() {
            val p = parent
            if (p == null) {
                return Collections.singletonList(Namespace.XML_NAMESPACE)
            }
            return orderFirst(namespace, p.namespacesInScope)
        }

    companion object {
        /**
         * JDOM 2.0.0 Serialization version. Attribute is simple
         */
        private const val serialVersionUID = 200L

        // Keep the old constant names for one beta cycle to help migration

        /** JDOM 1.x compatible reference to [AttributeType.UNDECLARED]  */
        @JvmField
        val UNDECLARED_TYPE: AttributeType = AttributeType.UNDECLARED

        /** JDOM 1.x compatible reference to [AttributeType.CDATA]  */
        @JvmField
        val CDATA_TYPE: AttributeType = AttributeType.CDATA

        /** JDOM 1.x compatible reference to [AttributeType.ID]  */
        @JvmField
        val ID_TYPE: AttributeType = AttributeType.ID

        /** JDOM 1.x compatible reference to [AttributeType.IDREF]  */
        @JvmField
        val IDREF_TYPE: AttributeType = AttributeType.IDREF

        /** JDOM 1.x compatible reference to [AttributeType.IDREFS]  */
        @JvmField
        val IDREFS_TYPE: AttributeType = AttributeType.IDREFS

        /** JDOM 1.x compatible reference to [AttributeType.ENTITY]  */
        @JvmField
        val ENTITY_TYPE: AttributeType = AttributeType.ENTITY

        /** JDOM 1.x compatible reference to [AttributeType.ENTITIES]  */
        @JvmField
        val ENTITIES_TYPE: AttributeType = AttributeType.ENTITIES

        /** JDOM 1.x compatible reference to [AttributeType.NMTOKEN]  */
        @JvmField
        val NMTOKEN_TYPE: AttributeType = AttributeType.NMTOKEN

        /** JDOM 1.x compatible reference to [AttributeType.NMTOKENS]  */
        @JvmField
        val NMTOKENS_TYPE: AttributeType = AttributeType.NMTOKENS

        /** JDOM 1.x compatible reference to [AttributeType.NOTATION]  */
        @JvmField
        val NOTATION_TYPE: AttributeType = AttributeType.NOTATION

        /** JDOM 1.x compatible reference to [AttributeType.ENUMERATION]  */
        @JvmField
        val ENUMERATED_TYPE: AttributeType = AttributeType.ENUMERATION

        private fun orderFirst(nsa: Namespace, nsl: List<Namespace>): List<Namespace> {
            if (nsl[0] === nsa) {
                return nsl
            }
            // OK, we have our namespace list, but our's is not the first.
            // we need the Attribute's Namespace to be up front.
            val tm = TreeMap<String, Namespace>()
            for (ns in nsl) {
                if (ns !== nsa) {
                    tm.put(ns.prefix, ns)
                }
            }
            val ret = ArrayList<Namespace>(tm.size + 1)
            ret.add(nsa)
            ret.addAll(tm.values)
            return Collections.unmodifiableList(ret)
        }
    }

}
