/*-- 

 Copyright (C) 2011 Jason Hunter & Brett McLaughlin.
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

package fi.sykero.kdom.output

import fi.sykero.kdom.JDOMConstants
import fi.sykero.kdom.internal.systemLineSeparator

/**
 * An enumeration of common separators that are used for JDOM output.
 * <p>
 * These enumerated values can be used as input to the
 * {@link Format#setLineSeparator(LineSeparator)} method.
 * 
 * <p>
 * JDOM has historically used the CR/NL sequence '\r\n' as a line-terminator.
 * This sequence has the advantage that the output is easily opened in the 
 * 'Notepad' editor on Windows. Other editors on other platforms are typically
 * smart enough to automatically adjust to whatever termination sequence is
 * used in the document. The XML specification requires that the CR/NL sequence
 * should be 'normalized' to a single newline '\n' when the document is parsed
 * (<a href="http://www.w3.org/TR/xml11/#sec-line-ends">XML 1.1 End-Of-Line
 * Handling</a>). As a result there is no XML issue with the JDOM default CR/NL
 * end-of-line sequence.
 * <p>
 * It should be noted that because JDOM internally stores just a '\n' as a line
 * separator that any other output separator requires additional processing to
 * output. There is a distinct performance benefit for using the UNIX, or NL
 * LineSeparator for output.
 * <p>
 * JDOM has always allowed the line-terminating sequence to be customised (or
 * even disabled) for each {@link XMLOutputter} operation by using this Format
 * class.
 * <p>
 * JDOM2 introduces two new features in relation to the end-of-line sequence.
 * Firstly, it introduces this new {@link LineSeparator} enumeration which
 * formalises the common line separators that can be used. In addition to the
 * simple String-based {@link Format#setLineSeparator(String)} method you can
 * now also call {@link Format#setLineSeparator(LineSeparator)} with one of the
 * common enumerations.
 * <p>
 * kdom: jdom2's second new feature, a global default end-of-line sequence read from the system property
 * {@link JDOMConstants#JDOM2_PROPERTY_LINE_SEPARATOR}, is not supported (no system properties in common
 * code): {@link #DEFAULT} is always CRNL, jdom2's default when the property is not set.
 * 
 * @since JDOM2
 * @author Rolf Lear
 *
 */
enum class LineSeparator(private val value: String?) {
	/**
	 * The Separator sequence CRNL which is '\r\n'. 
	 * This is the default sequence.
	 */
	CRNL("\r\n"),

	/**
	 * The Separator sequence NL which is '\n'. 
	 */
	NL("\n"),
	/**
	 * The Separator sequence CR which is '\r'. 
	 */
	CR("\r"),
	
	/** The 'DOS' Separator sequence CRLF (CRNL) which is '\r\n'. */
	DOS("\r\n"),
	
	/** The 'UNIX' Separator sequence NL which is '\n'. */
	UNIX("\n"),
	
	
	/**
	 * The system-dependent Separator sequence NL which is obtained from
	 * <code>System.getProperty("line.separator")</code> on the JVM ("\n" on iOS). This should be
	 * the equivalent of {@link #DOS} on windows platforms, and
	 * of {@link #UNIX} on UNIX and Apple systems (after Mac OSX).
	 */
	SYSTEM(systemLineSeparator()),
	
	/** Perform no end-of-line processing. */
	NONE(null),
	
	/**
	 * Use the sequence '\r\n'. (jdom2 let the System property
	 * {@link JDOMConstants#JDOM2_PROPERTY_LINE_SEPARATOR} override it; kdom does not, see the class
	 * documentation.)
	 */
	DEFAULT("\r\n");
	
	
	
	/**
	 * The String sequence used for this Separator
	 * @return an End-Of-Line String
	 */
	fun value(): String? {
		return value
	}

}
