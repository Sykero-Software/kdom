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

import kotlin.reflect.KClass
import fi.sykero.kdom.Attribute
import fi.sykero.kdom.CDATA
import fi.sykero.kdom.Comment
import fi.sykero.kdom.Content
import fi.sykero.kdom.DocType
import fi.sykero.kdom.Document
import fi.sykero.kdom.Element
import fi.sykero.kdom.EntityRef
import fi.sykero.kdom.Namespace
import fi.sykero.kdom.ProcessingInstruction
import fi.sykero.kdom.Text

/**
 * Factory class of convenience methods to create Filter instances of common
 * types. Methods that return Filters that act on core JDOM classes (Element,
 * Text, etc.) are simply named after the content they return.
 *
 * Filters that
 * match non-core classes (Boolean, Object, etc.) are all prefixed with the
 * letter 'f' (for **f**ilter).
 *
 * The Filter returned by [fpassthrough] is not really a filter in the
 * sense that it will never filter anything out - everything matches. This can
 * be useful to accomplish some tasks, for example the JDOM XPath API uses it
 * extensively.
 *
 * @author Rolf Lear
 *
 */
class Filters private constructor() {

    companion object {
        private val fcontent: Filter<Content> =
            ClassFilter<Content>(Content::class)

        private val fattribute: Filter<Attribute> =
            AttributeFilter()

        private val fcomment: Filter<Comment> =
            ClassFilter<Comment>(Comment::class)

        private val fcdata: Filter<CDATA> =
            ClassFilter<CDATA>(CDATA::class)

        private val fdoctype: Filter<DocType> =
            ClassFilter<DocType>(DocType::class)

        private val fentityref: Filter<EntityRef> =
            ClassFilter<EntityRef>(EntityRef::class)

        private val fpi: Filter<ProcessingInstruction> =
            ClassFilter<ProcessingInstruction>(ProcessingInstruction::class)

        private val ftext: Filter<Text> =
            ClassFilter<Text>(Text::class)

        private val ftextonly: Filter<Text> = TextOnlyFilter()

        private val felement: Filter<Element> =
            ClassFilter<Element>(Element::class)

        private val fdocument: Filter<Document> =
            ClassFilter<Document>(Document::class)

        // KClass.isInstance matches the boxed values (jdom2 needed java.lang.Double.class / java.lang.Boolean.class).
        private val fdouble: Filter<Double> =
            ClassFilter<Double>(Double::class)

        private val fboolean: Filter<Boolean> =
            ClassFilter<Boolean>(Boolean::class)

        private val fstring: Filter<String> =
            ClassFilter<String>(String::class)

        private val fpassthrough: Filter<Any> =
            PassThroughFilter()

        /**
         * Return a Filter that matches any [Content] data.
         *
         * @return a Filter that matches any [Content] data.
         */
        @JvmStatic
        fun content(): Filter<Content> {
            return fcontent
        }

        /**
         * Return a Filter that matches any [Attribute] data.
         *
         * @return a Filter that matches any [Attribute] data.
         */
        @JvmStatic
        fun attribute(): Filter<Attribute> {
            return fattribute
        }

        /**
         * Return a Filter that matches any [Attribute] data with the
         * specified name.
         *
         * @param name The name for all the Attributes to have (these can be in any
         * Namespace).
         * @return a Filter that matches any [Attribute] data with the
         * specified name.
         */
        @JvmStatic
        fun attribute(name: String?): Filter<Attribute> {
            return AttributeFilter(name)
        }

        /**
         * Return a Filter that matches any [Attribute] data with the
         * specified name and namespace.
         *
         * @param name The name for all the Attributes to have.
         * @param ns The Namespace for all the Attributes to have.
         * @return a Filter that matches any [Attribute] data with the
         * specified name and namespace.
         */
        @JvmStatic
        fun attribute(name: String?, ns: Namespace?): Filter<Attribute> {
            return AttributeFilter(name, ns)
        }

        /**
         * Return a Filter that matches any [Attribute] data with the
         * specified namespace.
         *
         * @param ns The Namespace for all the Attributes to have.
         * @return a Filter that matches any [Attribute] data with the
         * specified namespace.
         */
        @JvmStatic
        fun attribute(ns: Namespace?): Filter<Attribute> {
            return AttributeFilter(ns)
        }

        /**
         * Return a Filter that matches any [Comment] data.
         *
         * @return a Filter that matches any [Comment] data.
         */
        @JvmStatic
        fun comment(): Filter<Comment> {
            return fcomment
        }

        /**
         * Return a Filter that matches any [CDATA] data.
         *
         * @return a Filter that matches any [CDATA] data.
         */
        @JvmStatic
        fun cdata(): Filter<CDATA> {
            return fcdata
        }

        /**
         * Return a Filter that matches any [DocType] data.
         *
         * @return a Filter that matches any [DocType] data.
         */
        @JvmStatic
        fun doctype(): Filter<DocType> {
            return fdoctype
        }

        /**
         * Return a Filter that matches any [EntityRef] data.
         *
         * @return a Filter that matches any [EntityRef] data.
         */
        @JvmStatic
        fun entityref(): Filter<EntityRef> {
            return fentityref
        }

        /**
         * Return a Filter that matches any [Element] data.
         *
         * @return a Filter that matches any [Element] data.
         */
        @JvmStatic
        fun element(): Filter<Element> {
            return felement
        }

        /**
         * Return a Filter that matches any [Document] data.
         *
         * @return a Filter that matches any [Document] data.
         */
        @JvmStatic
        fun document(): Filter<Document> {
            return fdocument
        }

        /**
         * Return a Filter that matches any [Element] data with the specified
         * name.
         *
         * @param name The name of Elements to match.
         * @return a Filter that matches any [Element] data with the specified
         * name.
         */
        @JvmStatic
        fun element(name: String?): Filter<Element> {
            return ElementFilter(name, Namespace.NO_NAMESPACE)
        }

        /**
         * Return a Filter that matches any [Element] data with the specified
         * name and Namespace.
         *
         * @param name The name of Elements to match.
         * @param ns The Namespace to match
         * @return a Filter that matches any [Element] data with the specified
         * name and Namespace.
         */
        @JvmStatic
        fun element(name: String?, ns: Namespace?): Filter<Element> {
            return ElementFilter(name, ns)
        }

        /**
         * Return a Filter that matches any [Element] data with the specified
         * Namespace.
         *
         * @param ns The Namespace to match
         * @return a Filter that matches any [Element] data with the specified
         * Namespace.
         */
        @JvmStatic
        fun element(ns: Namespace?): Filter<Element> {
            return ElementFilter(null, ns)
        }

        /**
         * Return a Filter that matches any [ProcessingInstruction] data.
         *
         * @return a Filter that matches any [ProcessingInstruction] data.
         */
        @JvmStatic
        fun processinginstruction(): Filter<ProcessingInstruction> {
            return fpi
        }

        /**
         * Return a Filter that matches any [Text] data (which includes
         * [CDATA] since that is a subclass of Text).
         *
         * @return a Filter that matches any [Text] data (which includes
         * [CDATA] since that is a subclass of Text).
         */
        @JvmStatic
        fun text(): Filter<Text> {
            return ftext
        }

        /**
         * Return a Filter that matches any [Text] data (excludes
         * [CDATA] instances).
         *
         * @return a Filter that matches any [Text] data (which excludes
         * [CDATA] instances).
         */
        @JvmStatic
        fun textOnly(): Filter<Text> {
            return ftextonly
        }

        /**
         * Return a Filter that matches any Boolean data.
         *
         * @return a Filter that matches any Boolean data.
         */
        @JvmStatic
        fun fboolean(): Filter<Boolean> {
            return fboolean
        }

        /**
         * Return a Filter that matches any String data.
         *
         * @return a Filter that matches any String data.
         */
        @JvmStatic
        fun fstring(): Filter<String> {
            return fstring
        }

        /**
         * Return a Filter that matches any Double data.
         *
         * @return a Filter that matches any Double data.
         */
        @JvmStatic
        fun fdouble(): Filter<Double> {
            return fdouble
        }

        /**
         * Return a Filter that matches any data of the specified Class.
         *
         * @param <F> The generic type of the content returned by this Filter
         * @param clazz the Class type to match in the filter (kdom: a `KClass`; Java callers pass
         *        `JvmClassMappingKt.getKotlinClass(X.class)`)
         * @return a Filter that matches any data of the specified Class.
         */
        @JvmStatic
        fun <F : Any> fclass(clazz: KClass<F>): Filter<F> {
            return ClassFilter<F>(clazz)
        }

        /**
         * Return a filter that does no filtering at all - everything matches.
         * @return A Pass-Through Filter.
         */
        @JvmStatic
        fun fpassthrough(): Filter<Any> {
            return fpassthrough
        }
    }

}
