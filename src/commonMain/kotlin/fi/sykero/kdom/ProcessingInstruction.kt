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

import kotlin.jvm.JvmField
import kotlin.jvm.JvmSynthetic
import fi.sykero.kdom.internal.isJavaWhitespace
import fi.sykero.kdom.internal.EmptyUnmodifiableMap
import fi.sykero.kdom.internal.unsafeNull


/**
 * An XML processing instruction. Methods allow the user to obtain the target of
 * the PI as well as its data. The data can always be accessed as a String or,
 * if the data appears akin to an attribute list, can be retrieved as name/value
 * pairs.
 *
 * @author  Brett McLaughlin
 * @author  Jason Hunter
 * @author  Steven Gould
 */
open class ProcessingInstruction : Content {

    /**
     * This will retrieve the target of the PI.
     * (The target of the PI; `null` for a raw instance made by the protected no-args constructor.
     * Was the protected field `target`.)
     *
     * @return `String` - target of PI.
     */
    var target: String
        get() = unsafeNull(targetField)
        @JvmSynthetic
        set(v) {
            setTarget(v)
        }

    private var targetField: String? = null

    /**
     * The data for the PI as a String
     * (`null` for a raw instance made by the protected no-args constructor. Was the protected
     * field `rawData`.)
     */
    private var rawData: String? = null

    /** The data for the PI in name/value pairs  */
    @JvmField
    protected var mapData: MutableMap<String, String>? = null

    /**
     * Default, no-args constructor for implementations
     * to use if needed.
     */
    protected constructor() : super(CType.ProcessingInstruction)

    /**
     * This will create a new `ProcessingInstruction`
     * with the specified target.
     *
     * @param target `String` target of PI.
     * @throws IllegalTargetException if the given target is illegal
     *         as a processing instruction name.
     */
    constructor(target: String?) : this(target, "")

    /**
     * This will create a new `ProcessingInstruction`
     * with the specified target and data.
     *
     * @param target `String` target of PI.
     * @param data `Map` data for PI, in
     *             name/value pairs
     * @throws IllegalTargetException if the given target is illegal
     *         as a processing instruction name.
     */
    constructor(target: String?, data: Map<String, String>) : super(CType.ProcessingInstruction) {
        setTarget(target)
        setData(data)
    }

    /**
     * This will create a new `ProcessingInstruction`
     * with the specified target and data.
     *
     * @param target `String` target of PI.
     * @param data `String` data for PI.
     * @throws IllegalTargetException if the given target is illegal
     *         as a processing instruction name.
     */
    constructor(target: String?, data: String?) : super(CType.ProcessingInstruction) {
        setTarget(target)
        setData(data)
    }

    /**
     * This will set the target for the PI.
     *
     * @param newTarget `String` new target of PI.
     * @return `ProcessingInstruction` - this PI modified.
     */
    open fun setTarget(newTarget: String?): ProcessingInstruction {
        val reason: String? = Verifier.checkProcessingInstructionTarget(newTarget)
        if (reason != null) {
            throw IllegalTargetException(newTarget, reason)
        }

        targetField = newTarget
        return this
    }

    /**
     * Returns the XPath 1.0 string value of this element, which is the
     * data of this PI.
     *
     * @return the data of this PI
     */
    override val value: String
        get() = unsafeNull(rawData)

    /**
     * This will return the raw data from all instructions.
     *
     * @return `String` - data of PI.
     */
    var data: String
        get() = unsafeNull(rawData)
        @JvmSynthetic
        set(v) {
            setData(v)
        }

    /**
     * This will return a `List` containing the names of the
     * "attribute" style pieces of name/value pairs in this PI's data.
     *
     * @return `List` - the `List` containing the
     *         "attribute" names.
     */
    val pseudoAttributeNames: List<String>
        get() = ArrayList<String>(mapData!!.keys)

    /**
     * This will set the raw data for the PI.
     *
     * @param data `String` data of PI.
     * @return `ProcessingInstruction` - this PI modified.
     */
    open fun setData(data: String?): ProcessingInstruction {
        val reason: String? = Verifier.checkProcessingInstructionData(data)
        if (reason != null) {
            throw IllegalDataException(data, reason)
        }

        this.rawData = data
        this.mapData = parseData(data!!)
        return this
    }

    /**
     * This will set the name/value pairs within the passed
     * `Map` as the pairs for the data of
     * this PI.  The keys should be the pair name
     * and the values should be the pair values.
     *
     * @param data new map data to use
     * @return `ProcessingInstruction` - modified PI.
     */
    open fun setData(data: Map<String, String>): ProcessingInstruction {
        val temp = toString(data)

        val reason: String? = Verifier.checkProcessingInstructionData(temp)
        if (reason != null) {
            throw IllegalDataException(temp, reason)
        }

        this.rawData = temp
        this.mapData = LinkedHashMap<String, String>(data)
        return this
    }

    /**
     * This will return the value for a specific
     * name/value pair on the PI.  If no such pair is
     * found for this PI, null is returned.
     *
     * @param name `String` name of name/value pair
     *             to lookup value for.
     * @return `String` - value of name/value pair.
     */
    fun getPseudoAttributeValue(name: String?): String? {
        @Suppress("UNCHECKED_CAST")
        return (mapData!! as Map<String?, String>)[name]
    }

    /**
     * This will set a pseudo attribute with the given name and value.
     * If the PI data is not already in a pseudo-attribute format, this will
     * replace the existing data.
     *
     * @param name `String` name of pair.
     * @param value `String` value for pair.
     * @return `ProcessingInstruction` this PI modified.
     */
    open fun setPseudoAttribute(name: String?, value: String?): ProcessingInstruction {
        var reason: String? = Verifier.checkProcessingInstructionData(name)
        if (reason != null) {
            throw IllegalDataException(name, reason)
        }

        reason = Verifier.checkProcessingInstructionData(value)
        if (reason != null) {
            throw IllegalDataException(value, reason)
        }

        this.mapData!!.put(name!!, value!!)
        this.rawData = toString(mapData!!)
        return this
    }

    /**
     * This will remove the pseudo attribute with the specified name.
     *
     * @param name name of pseudo attribute to remove
     * @return `boolean` - whether the requested
     *         instruction was removed.
     */
    fun removePseudoAttribute(name: String?): Boolean {
        @Suppress("UNCHECKED_CAST")
        if (((mapData!! as MutableMap<String?, String>).remove(name)) != null) {
            rawData = toString(mapData!!)
            return true
        }

        return false
    }

    /**
     * This will parse and load the instructions for the PI.
     * This is separated to allow it to occur once and then be reused.
     */
    private fun parseData(prawData: String): MutableMap<String, String> {
        // The parsing here is done largely "by hand" which means the code
        // gets a little tricky/messy.  The following conditions should
        // now be handled correctly:
        //   <?pi href="http://hi/a=b"?>        Reads OK
        //   <?pi href = 'http://hi/a=b' ?>     Reads OK
        //   <?pi href\t = \t'http://hi/a=b'?>  Reads OK
        //   <?pi href  =  "http://hi/a=b"?>    Reads OK
        //   <?pi?>                             Empty Map
        //   <?pi id=22?>                       Empty Map
        //   <?pi id='22?>                      Empty Map

        val data = LinkedHashMap<String, String>()

        // System.out.println("rawData: " + rawData);

        // The inputData variable holds the part of rawData left to parse
        var inputData: String = prawData.trim { it <= ' ' }

        // Iterate through the remaining inputData string
        while (inputData.trim { it <= ' ' } != "") {
            //System.out.println("parseData() looking at: " + inputData);

            // Search for "name =", "name=" or "name1 name2..."
            var name = ""
            var value = ""
            var startName = 0
            var previousChar = inputData[startName]
            var pos = 1
            while (pos < inputData.length) {
                val currentChar = inputData[pos]
                if (currentChar == '=') {
                    name = inputData.substring(startName, pos).trim { it <= ' ' }
                    // Get the boundaries on the quoted string
                    // We use boundaries so we know where to start next
                    val bounds = extractQuotedString(
                        inputData.substring(pos + 1)
                    )
                    // A null value means a parse error and we return empty!
                    if (bounds == null) {
                        return EmptyUnmodifiableMap() // jdom2: Collections.emptyMap()
                    }
                    value = inputData.substring(
                        bounds[0] + pos + 1,
                        bounds[1] + pos + 1
                    )
                    pos += bounds[1] + 1 // skip past value
                    break
                } else if (isJavaWhitespace(previousChar)
                    && !isJavaWhitespace(currentChar)
                ) {
                    startName = pos
                }

                previousChar = currentChar
                pos++
            }

            // Remove the first pos characters; they have been processed
            inputData = inputData.substring(pos)

            // System.out.println("Extracted (name, value) pair: ("
            //                          + name + ", '" + value+"')");

            // If both a name and a value have been found, then add
            // them to the data Map - actually, we add an empty value if there
            // is a valid name.
            if (name.length > 0) {
                //if (data.containsKey(name)) {
                // A repeat, that's a parse error, so return a null map
                //return new HashMap();
                //}
                //else {
                data.put(name, value)
                //}
            }
        }

        return data
    }

    /**
     * This returns a `String` representation of the
     * `ProcessingInstruction`, suitable for debugging. If the XML
     * representation of the `ProcessingInstruction` is desired,
     * [fi.sykero.kdom.output.XMLOutputter.outputString]
     * should be used.
     *
     * @return `String` - information about the
     *         `ProcessingInstruction`
     */
    override fun toString(): String {
        return StringBuilder()
            .append("[ProcessingInstruction: ")
            .append(fi.sykero.kdom.output.XMLOutputter().outputString(this))
            .append("]")
            .toString()
    }

    protected override fun shallowCopy(): ProcessingInstruction {
        val copy = ProcessingInstruction()
        copy.targetField = targetField
        copy.rawData = rawData
        copy.mapData = mapData
        return copy
    }

    public override fun clone(): ProcessingInstruction {
        val pi = super.clone() as ProcessingInstruction

        // target and rawdata are immutable and references copied by
        // Object.clone()

        // Create a new Map object for the clone (since Map isn't Cloneable)
        pi.mapData = parseData(rawData!!)
        return pi
    }

    override fun detach(): ProcessingInstruction {
        return super.detach() as ProcessingInstruction
    }

    internal override fun setParent(parent: Parent?): ProcessingInstruction {
        return super.setParent(parent) as ProcessingInstruction
    }

    private companion object {
        /**
         * This will convert the Map to a string representation.
         *
         * @param pmapData `Map` PI data to convert
         * @return a string representation of the Map as appropriate for a PI
         */
        private fun toString(pmapData: Map<String, String>): String {
            val stringData = StringBuilder()

            for (me in pmapData.entries) {
                stringData.append(me.key)
                    .append("=\"")
                    .append(me.value)
                    .append("\" ")
            }
            // Remove last space, if we did any appending
            if (stringData.length > 0) {
                stringData.deleteAt(stringData.length - 1)
            }

            return stringData.toString()
        }

        /**
         * This is a helper routine, only used by parseData, to extract a
         * quoted String from the input parameter, rawData. A quoted string
         * can use either single or double quotes, but they must match up.
         * A singly quoted string can contain an unbalanced amount of double
         * quotes, or vice versa. For example, the String "JDOM's the best"
         * is legal as is 'JDOM"s the best'.
         *
         * @param rawData the input string from which a quoted string is to
         *                be extracted.
         * @return the first quoted string encountered in the input data. If
         *         no quoted string is found, then the empty string, "", is
         *         returned.
         * @see parseData
         */
        private fun extractQuotedString(rawData: String): IntArray? {
            // Remembers whether we're actually in a quoted string yet
            var inQuotes = false

            // Remembers which type of quoted string we're in
            var quoteChar = '"'

            // Stores the position of the first character inside
            //  the quoted string (i.e. the start of the return string)
            var start = 0

            // Iterate through the input string looking for the start
            // and end of the quoted string
            for (pos in 0 until rawData.length) {
                val currentChar = rawData[pos]
                if (currentChar == '"' || currentChar == '\'') {
                    if (!inQuotes) {
                        // We're entering a quoted string
                        quoteChar = currentChar
                        inQuotes = true
                        start = pos + 1
                    } else if (quoteChar == currentChar) {
                        // We're leaving a quoted string
                        inQuotes = false
                        return intArrayOf(start, pos)
                    }
                    // Otherwise we've encountered a quote
                    // inside a quote, so just continue
                }
            }

            return null
        }
    }

}
