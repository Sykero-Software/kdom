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
 * This simple class just tidies up any cloneable classes. This method deals
 * with any CloneNotSupported exceptions. THis class is package private only.
 *
 * kdom: there is no `Cloneable`/`Object.clone()` in common Kotlin. Each concrete subclass implements
 * [shallowCopy] (a new instance with every field copied, what `Object.clone()` did), and the `clone()`
 * overrides keep jdom2's deep-copy code on top of it.
 *
 * @author Rolf Lear
 */
// was package-private (Kotlin cannot expose an internal supertype from the public Content)
abstract class CloneBase protected constructor() {

    // jdom2: the protected no-arg constructor is needed to solve issue #88 (a no-arg constructor
    // accessible by all potential subclasses of CloneBase).

    /**
     * Returns a new instance of the runtime class's kdom type with every field copied by reference, the
     * equivalent of jdom2's `Object.clone()` call (`super.clone()`). A subclass outside kdom inherits its kdom
     * superclass's implementation, so its clone is an instance of that kdom class (see translation-notes.md).
     */
    protected abstract fun shallowCopy(): CloneBase

    /**
     * Return a deep clone of this instance. Even if this instance has a parent,
     * the returned clone will not.
     *
     * All JDOM core classes are Cloneable, and never throw
     * CloneNotSupportedException. Additionally all Cloneable JDOM classes
     * return the correct type of instance from this method and there is no
     * need to cast the result (co-variant return value).
     *
     * Subclasses of this should still call super.clone() in their clone method.
     */
    open fun clone(): CloneBase {
        return shallowCopy()
    }

}
