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

import fi.sykero.kdom.JDOMConstants.Companion.NS_PREFIX_DEFAULT
import fi.sykero.kdom.JDOMConstants.Companion.NS_PREFIX_XML
import fi.sykero.kdom.JDOMConstants.Companion.NS_PREFIX_XMLNS
import fi.sykero.kdom.JDOMConstants.Companion.NS_URI_DEFAULT
import fi.sykero.kdom.JDOMConstants.Companion.NS_URI_XML
import fi.sykero.kdom.JDOMConstants.Companion.NS_URI_XMLNS
import java.io.InvalidObjectException
import java.io.Serializable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * An XML namespace representation, as well as a factory for creating XML
 * namespace objects. All methods on Namespace (including
 * [getNamespace] (String) and [getNamespace] (String, String))
 * are thread-safe.
 *
 * See [NamespaceAware] for additional notes on how Namespaces are
 * 'in-scope' in JDOM content, and how those in-scope Namespaces are accessed.
 *
 * @see NamespaceAware
 * @author  Brett McLaughlin
 * @author  Elliotte Rusty Harold
 * @author  Jason Hunter
 * @author  Wesley Biggs
 * @author  Rolf Lear
 */
class Namespace private constructor(
    /** The prefix mapped to this namespace. */
    @Transient val prefix: String,
    /** The URI for this namespace. */
    @get:JvmName("getURI") @Transient val uri: String
) : Serializable {

    /**
     * This tests for equality - Two `Namespaces`
     * are equal if and only if their URIs are byte-for-byte equals.
     *
     * @param other `Object` to compare to this `Namespace`.
     * @return `boolean` - whether the supplied object is equal to this `Namespace`.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other is Namespace) {  // instanceof returns false if null
            return uri == other.uri
        }
        return false
    }

    /**
     * This returns a `String` representation of this
     * `Namespace`, suitable for use in debugging.
     *
     * @return `String` - information about this instance.
     */
    override fun toString(): String {
        return "[Namespace: prefix \"" + prefix + "\" is mapped to URI \"" +
                uri + "\"]"
    }

    /**
     * This returns the hash code for the `Namespace` that conforms
     * to the 'equals()' contract.
     *
     * If two namespaces have the same URI, they are equal and have the same
     * hash code, even if they have different prefixes.
     *
     * @return `int` - hash code for this `Namespace`.
     */
    override fun hashCode(): Int {
        return uri.hashCode()
    }

    /* *****************************************
     * Serialization
     * ***************************************** */

    private class NamespaceSerializationProxy(
        private val pprefix: String?,
        private val puri: String?
    ) : Serializable {

        private fun readResolve(): Any {
            return getNamespace(pprefix, puri)
        }

        private companion object {
            private const val serialVersionUID = 200L
        }
    }

    /**
     * Serializes Namespace by using a proxy serialization instance.
     * @serialData The proxy deals with the protocol.
     * @return the Namespace proxy instance.
     */
    private fun writeReplace(): Any {
        return NamespaceSerializationProxy(prefix, uri)
    }

    /**
     * Because Namespace is serialized by proxy, the reading of direct Namespace
     * instances is illegal and prohibited.
     * @return nothing.
     * @throws InvalidObjectException always
     */
    @Throws(InvalidObjectException::class)
    private fun readResolve(): Any {
        throw InvalidObjectException(
            "Namespace is serialized through a proxy"
        )
    }

    companion object {
        /**
         * Factory list of namespaces.
         * Keys are *URI*&*prefix*.
         * Values are Namespace objects
         */
        private val namespacemap: ConcurrentMap<String, ConcurrentMap<String, Namespace>> =
            ConcurrentHashMap<String, ConcurrentMap<String, Namespace>>(512, 0.75f, 64)

        /** Define a `Namespace` for when *not* in a namespace */
        @JvmField
        val NO_NAMESPACE: Namespace = Namespace(
            NS_PREFIX_DEFAULT,
            NS_URI_DEFAULT
        )

        /** Define a `Namespace` for the standard xml prefix. */
        @JvmField
        val XML_NAMESPACE: Namespace = Namespace(
            NS_PREFIX_XML,
            NS_URI_XML
        )

        private val XMLNS_NAMESPACE: Namespace = Namespace(
            NS_PREFIX_XMLNS,
            NS_URI_XMLNS
        )

        init {
            // pre-populate the map with the constant namespaces that would
            // otherwise fail validation
            val nmap: ConcurrentMap<String, Namespace> = ConcurrentHashMap<String, Namespace>()
            nmap.put(NO_NAMESPACE.prefix, NO_NAMESPACE)
            namespacemap.put(NO_NAMESPACE.uri, nmap)

            val xmap: ConcurrentMap<String, Namespace> = ConcurrentHashMap<String, Namespace>()
            xmap.put(XML_NAMESPACE.prefix, XML_NAMESPACE)
            namespacemap.put(XML_NAMESPACE.uri, xmap)

            val xnsmap: ConcurrentMap<String, Namespace> = ConcurrentHashMap<String, Namespace>()
            xnsmap.put(XMLNS_NAMESPACE.prefix, XMLNS_NAMESPACE)
            namespacemap.put(XMLNS_NAMESPACE.uri, xnsmap)
        }

        /**
         * JDOM 2.0.0 Serialization version
         */
        private const val serialVersionUID = 200L

        /**
         * This will retrieve (if in existence) or create (if not) a
         * `Namespace` for the supplied *prefix* and *uri*.
         * This method is thread-safe.
         *
         * @param prefix `String` prefix to map to `Namespace`.
         * @param uri `String` URI of new `Namespace`.
         * @return `Namespace` - ready to use namespace.
         * @throws IllegalNameException if the given prefix and uri make up
         *         an illegal namespace name.
         * @see Verifier.checkNamespacePrefix
         * @see Verifier.checkNamespaceURI
         */
        @JvmStatic
        fun getNamespace(prefix: String?, uri: String?): Namespace {

            // This is a rewrite of the JDOM 1 getNamespace() to use
            // java.util.concurrent. The motivation is:
            // 1. avoid having to create a new NamespaceKey for each query.
            // 2. avoid a 'big' synchronisation bottleneck in the Namespace class.
            // 3. no-memory-lookup for pre-existing Namespaces... (avoid 'new' and
            //    most String methods that allocate memory (like trim())

            if (uri == null) {
                if (prefix == null || NS_PREFIX_DEFAULT == prefix) {
                    return NO_NAMESPACE
                }
                // we have an attempt for some prefix
                // (not "" or it would have found NO_NAMESPACE) on the null URI
                throw IllegalNameException(
                    "", "namespace",
                    "Namespace URIs must be non-null and non-empty Strings"
                )
            }

            // must have checked for 'null' uri else namespacemap throws NPE
            // do not 'trim' uri's any more see issue #50
            var urimap: ConcurrentMap<String, Namespace>? = namespacemap.get(uri)
            if (urimap == null) {
                // no Namespaces yet with that URI.
                // Ensure proper naming
                val reason: String? = Verifier.checkNamespaceURI(uri)
                if (reason != null) {
                    throw IllegalNameException(uri, "Namespace URI", reason)
                }

                urimap = ConcurrentHashMap<String, Namespace>()
                val xmap: ConcurrentMap<String, Namespace>? =
                    namespacemap.putIfAbsent(uri, urimap)

                if (xmap != null) {
                    // some other thread registered this URI between when we
                    // first checked, and when we got a new map created.
                    // we must use the already-registered value.
                    urimap = xmap
                }
            }

            // OK, we have a container for the URI, let's search on the prefix.

            var ns: Namespace? = urimap!!.get(if (prefix == null) NS_PREFIX_DEFAULT else prefix)
            if (ns != null) {
                // got one.
                return ns
            }

            // OK, no namespace yet for that uri/prefix
            // validate the prefix (the uri is already validated).

            if (NS_URI_DEFAULT == uri) {
                // we have an attempt for some prefix
                // (not "" or it would have found NO_NAMESPACE) on the "" URI
                // note, we have already done this check for 'null' uri above.
                throw IllegalNameException(
                    "", "namespace",
                    "Namespace URIs must be non-null and non-empty Strings"
                )
            }

            // http://www.w3.org/TR/REC-xml-names/#xmlReserved
            // The erratum to Namespaces in XML 1.0 that suggests this
            // next check is controversial. Not everyone accepts it.
            if (NS_URI_XML == uri) {
                throw IllegalNameException(
                    uri, "Namespace URI",
                    "The " + NS_URI_XML + " must be bound to " +
                            "only the '" + NS_PREFIX_XML + "' prefix."
                )
            }

            // http://www.w3.org/TR/REC-xml-names/#xmlReserved
            if (NS_URI_XMLNS == uri) {
                throw IllegalNameException(
                    uri, "Namespace URI",
                    "The " + NS_URI_XMLNS + " must be bound to " +
                            "only the '" + NS_PREFIX_XMLNS + "' prefix."
                )
            }

            // no namespace found, we validate the prefix
            val pfx: String = if (prefix == null) NS_PREFIX_DEFAULT else prefix

            // http://www.w3.org/TR/REC-xml-names/#xmlReserved
            // checkNamespacePrefix no longer checks for xml prefix
            if (NS_PREFIX_XML == pfx) {
                // The xml namespace prefix was in the map. attempts to rebind it are illegal
                throw IllegalNameException(
                    uri, "Namespace prefix",
                    "The prefix " + NS_PREFIX_XML + " (any case) can only be bound to " +
                            "only the '" + NS_URI_XML + "' uri."
                )
            }

            // http://www.w3.org/TR/REC-xml-names/#xmlReserved
            // checkNamespacePrefix no longer checks for xmlns prefix
            if (NS_PREFIX_XMLNS == pfx) {
                // The xml namespace prefix was in the map. attempts to rebind it are illegal
                throw IllegalNameException(
                    uri, "Namespace prefix",
                    "The prefix " + NS_PREFIX_XMLNS + " (any case) can only be bound to " +
                            "only the '" + NS_URI_XMLNS + "' uri."
                )
            }

            val reason: String? = Verifier.checkNamespacePrefix(pfx)
            if (reason != null) {
                throw IllegalNameException(pfx, "Namespace prefix", reason)
            }

            // OK, good bet that we have a new Namespace.
            ns = Namespace(pfx, uri)
            val prev: Namespace? = urimap.putIfAbsent(pfx, ns)
            if (prev != null) {
                // someone registered the same namespace as us while we were busy
                // validating. Use their registered copy.
                ns = prev
            }
            return ns
        }

        /**
         * This will retrieve (if in existence) or create (if not) a
         * `Namespace` for the supplied URI, and make it usable
         * as a default namespace, as no prefix is supplied.
         * This method is thread-safe.
         *
         * @param uri `String` URI of new `Namespace`.
         * @return `Namespace` - ready to use namespace.
         */
        @JvmStatic
        fun getNamespace(uri: String?): Namespace {
            return getNamespace(NS_PREFIX_DEFAULT, uri)
        }
    }
}
