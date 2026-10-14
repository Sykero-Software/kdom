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
 * Thrown when trying to add an illegal object to a JDOM construct.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 */
open class IllegalAddException : IllegalArgumentException {

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Attribute] to the [Element] is illegal.
     *
     * @param base `Element` that `Attribute` couldn't be added to
     * @param added `Attribute` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: Attribute, reason: String?) : super(
        StringBuilder()
            .append("The attribute \"")
            .append(added.qualifiedName)
            .append("\" could not be added to the element \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Element] to parent is illegal.
     *
     * @param base `Element` that the child couldn't be added to
     * @param added `Element` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: Element, reason: String?) : super(
        StringBuilder()
            .append("The element \"")
            .append(added.qualifiedName)
            .append("\" could not be added as a child of \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Element] to the [Document] is illegal.
     *
     * @param added `Element` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(added: Element, reason: String?) : super(
        StringBuilder()
            .append("The element \"")
            .append(added.qualifiedName)
            .append("\" could not be added as the root of the document: ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [ProcessingInstruction] to the [Element] is illegal.
     *
     * @param base `Element` that the `ProcessingInstruction` couldn't be added to
     * @param added `ProcessingInstruction` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: ProcessingInstruction, reason: String?) : super(
        StringBuilder()
            .append("The PI \"")
            .append(added.target)
            .append("\" could not be added as content to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [ProcessingInstruction] to the [Document] is illegal.
     *
     * @param added `ProcessingInstruction` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(added: ProcessingInstruction, reason: String?) : super(
        StringBuilder()
            .append("The PI \"")
            .append(added.target)
            .append("\" could not be added to the top level of the document: ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Comment] to the [Element] is illegal.
     *
     * @param base `Element` that the `Comment` couldn't be added to
     * @param added `Comment` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: Comment, reason: String?) : super(
        StringBuilder()
            .append("The comment \"")
            .append(added.text)
            .append("\" could not be added as content to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [CDATA]
     *
     * @param base `Element` that the `CDATA` couldn't be added to
     * @param added `CDATA` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: CDATA, reason: String?) : super(
        StringBuilder()
            .append("The CDATA \"")
            .append(added.text)
            .append("\" could not be added as content to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Text] to the [Element] is illegal.
     *
     * @param base `Element` that the `Comment` couldn't be added to
     * @param added `Text` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: Text, reason: String?) : super(
        StringBuilder()
            .append("The Text \"")
            .append(added.text)
            .append("\" could not be added as content to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Comment] to the [Document] is illegal.
     *
     * @param added `Comment` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(added: Comment, reason: String?) : super(
        StringBuilder()
            .append("The comment \"")
            .append(added.text)
            .append("\" could not be added to the top level of the document: ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [EntityRef] to the [Element] is illegal.
     *
     * @param base `Element` that the `EntityRef` couldn't be added to
     * @param added `EntityRef` reference that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: EntityRef, reason: String?) : super(
        StringBuilder()
            .append("The entity reference\"")
            .append(added.name)
            .append("\" could not be added as content to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [Namespace] to the [Element] is illegal.
     *
     * @param base `Element` that the `Namespace` couldn't be added to
     * @param added `Namespace` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(base: Element, added: Namespace, reason: String?) : super(
        StringBuilder()
            .append("The namespace xmlns")
            .append(if (added.prefix == "") "=" else ":" + added.prefix + "=")
            .append("\"")
            .append(added.uri)
            .append("\" could not be added as a namespace to \"")
            .append(base.qualifiedName)
            .append("\": ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` indicating
     * that the addition of the [DocType] to the [Document] is illegal.
     *
     * @param added `DocType` that could not be added
     * @param reason cause of the problem
     */
    internal constructor(added: DocType, reason: String?) : super(
        StringBuilder()
            .append("The DOCTYPE ")
            .append(added.toString())
            .append(" could not be added to the document: ")
            .append(reason)
            .toString()
    )

    /**
     * This will create an `Exception` with the specified
     * error message.
     *
     * @param reason cause of the problem
     */
    constructor(reason: String?) : super(reason)
}
