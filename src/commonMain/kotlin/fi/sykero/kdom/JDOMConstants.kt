/*-- 

 Copyright (C) 2011-2012 Jason Hunter & Brett McLaughlin.
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

import fi.sykero.kdom.output.LineSeparator

/**
 * A collection of constants that may be useful to JDOM users.
 *
 * JDOM attempts to make knowing these 'magic' constants unnecessary, but it is
 * not always possible. In an attempt to make it easier though, common constants
 * are defined here. This is not a comprehensive list of all the constants that
 * may be useful when processing XML, but it should cover most of those occasions
 * where JDOM does not automatically do the 'right thing'.
 *
 * Many of these constants are already referenced inside the JDOM code.
 *
 * @author Rolf Lear
 */
class JDOMConstants private constructor() {
    companion object {
        /*
         * XML Namespace constants
         */

        /** Defined as `""` */
        const val NS_PREFIX_DEFAULT: String = ""
        /** Defined as `""` */
        const val NS_URI_DEFAULT: String = ""

        /** Defined as `xml` */
        const val NS_PREFIX_XML: String = "xml"
        /** Defined as `http://www.w3.org/XML/1998/namespace` */
        const val NS_URI_XML: String = "http://www.w3.org/XML/1998/namespace"

        /** Defined as `xmlns` */
        const val NS_PREFIX_XMLNS: String = "xmlns"
        /** Defined as `http://www.w3.org/2000/xmlns/` */
        const val NS_URI_XMLNS: String = "http://www.w3.org/2000/xmlns/"

        /** Defined as `http://xml.org/sax/properties/declaration-handler` */
        const val SAX_PROPERTY_DECLARATION_HANDLER: String =
            "http://xml.org/sax/properties/declaration-handler"

        /** Defined as `http://xml.org/sax/handlers/DeclHandler` */
        const val SAX_PROPERTY_DECLARATION_HANDLER_ALT: String =
            "http://xml.org/sax/handlers/DeclHandler"

        /** Defined as `http://xml.org/sax/properties/lexical-handler` */
        const val SAX_PROPERTY_LEXICAL_HANDLER: String =
            "http://xml.org/sax/properties/lexical-handler"

        /** Defined as `http://xml.org/sax/handlers/LexicalHandler` */
        const val SAX_PROPERTY_LEXICAL_HANDLER_ALT: String =
            "http://xml.org/sax/handlers/LexicalHandler"

        /** Defined as `http://xml.org/sax/features/external-general-entities` */
        const val SAX_FEATURE_EXTERNAL_ENT: String =
            "http://xml.org/sax/features/external-general-entities"

        /** Defined as `http://xml.org/sax/features/validation` */
        const val SAX_FEATURE_VALIDATION: String =
            "http://xml.org/sax/features/validation"

        /** Defined as `http://xml.org/sax/features/namespaces` */
        const val SAX_FEATURE_NAMESPACES: String =
            "http://xml.org/sax/features/namespaces"

        /** Defined as `http://xml.org/sax/features/namespace-prefixes` */
        const val SAX_FEATURE_NAMESPACE_PREFIXES: String =
            "http://xml.org/sax/features/namespace-prefixes"

        /**
         * Constant used to define the JDOMSource with JAXP.
         * Defined as `http://jdom.org/jdom2/transform/JDOMSource/feature`
         */
        const val JDOM2_FEATURE_JDOMSOURCE: String =
            "http://jdom.org/jdom2/transform/JDOMSource/feature"

        /**
         * Constant used to define the JDOMResult with TransformerFactory.
         * Defined as `http://jdom.org/jdom2/transform/JDOMResult/feature`
         */
        const val JDOM2_FEATURE_JDOMRESULT: String =
            "http://jdom.org/jdom2/transform/JDOMResult/feature"

        /**
         * System Property queried to obtain an alternate default XPathFactory.
         * Defined as `fi.sykero.kdom.xpath.XPathFactory`
         */
        const val JDOM2_PROPERTY_XPATH_FACTORY: String =
            "fi.sykero.kdom.xpath.XPathFactory"

        /**
         * System Property queried by jdom2 to obtain an alternate default Line Separator
         * (kdom never reads it: [LineSeparator.DEFAULT] is always CRNL).
         *
         * Defined as `fi.sykero.kdom.output.LineSeparator`
         * @see LineSeparator
         */
        const val JDOM2_PROPERTY_LINE_SEPARATOR: String =
            "fi.sykero.kdom.output.LineSeparator"
    }
}
