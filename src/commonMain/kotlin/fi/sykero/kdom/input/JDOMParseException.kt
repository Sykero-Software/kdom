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

package fi.sykero.kdom.input

import fi.sykero.kdom.Document
import fi.sykero.kdom.JDOMException

/**
 * Thrown during parse errors, with information about where the parse error
 * occurred as well as access to the partially built document.
 *
 * @author  Laurent Bihanic
 */
open class JDOMParseException(
	message: String?,
	cause: Throwable?,
	/**
	 * Returns the partial document that was successfully built before
	 * the error occurred.
	 *
	 * The portion of the document that was successfully built before
	 * the parse error occurred; null if none.
	 */
	val partialDocument: Document?,
	/**
	 * Returns the public identifier of the entity where the
	 * parse error occurred, or `null` if the information is not available.
	 */
	val publicId: String?,
	/**
	 * Returns the system identifier of the entity where the
	 * parse error occurred, or `null` if the information is not available.
	 */
	val systemId: String?,
	/**
	 * Returns the line number of the end of the text where the
	 * parse error occurred, or -1 if the information is not available.
	 *
	 * The first line in the document is line 1.
	 */
	val lineNumber: Int,
	/**
	 * Returns the column number of the end of the text where the
	 * parse error occurred, or -1 if the information is not available.
	 *
	 * The first column in a line is position 1.
	 */
	val columnNumber: Int
) : JDOMException(message, cause) {

	/**
	 * This will create a parse `Exception` with the given message only.
	 * (kdom addition: location fields are -1 / null.)
	 *
	 * @param message `String` message indicating the problem that occurred.
	 */
	constructor(message: String?) : this(message, null)

	/**
	 * This will create a parse `Exception` with the given
	 * message and wrap the `Exception` that cause a document
	 * parse to fail.
	 *
	 * @param message `String` message indicating the problem that occurred.
	 * @param cause `Throwable` that caused this to be thrown.
	 */
	constructor(message: String?, cause: Throwable?) : this(message, cause, null)

	/**
	 * This will create a parse `Exception` with the given
	 * message and the partial document and wrap the
	 * `Exception` that cause a document parse to fail.
	 *
	 * @param message `String` message indicating the problem that occurred.
	 * @param cause `Throwable` that caused this to be thrown.
	 * @param partialDocument `Document` the portion of the input XML
	 *                        document that was successfully built.
	 */
	constructor(message: String?, cause: Throwable?, partialDocument: Document?) :
		this(message, cause, partialDocument, null, null, -1, -1)

	/**
	 * This will create a parse `Exception` with the given message,
	 * cause and the location where the parse error occurred.
	 * (kdom addition, replaces reading the location from a `SAXParseException` cause.)
	 *
	 * @param message `String` message indicating the problem that occurred.
	 * @param cause `Throwable` that caused this to be thrown.
	 * @param lineNumber the line number (first line is 1), or -1 if unknown.
	 * @param columnNumber the column number (first column is 1), or -1 if unknown.
	 */
	constructor(message: String?, cause: Throwable?, lineNumber: Int, columnNumber: Int) :
		this(message, cause, null, null, null, lineNumber, columnNumber)
}
