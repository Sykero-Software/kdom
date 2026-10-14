@file:JvmName("KdomBuilderJvm")

package fi.sykero.kdom.input

import fi.sykero.kdom.Document
import fi.sykero.kdom.JDOMException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.Reader

/**
 * JVM conveniences so that `SAXBuilder().build(StringReader(x))` call sites become `KdomBuilder().build(StringReader(x))`.
 * Each reads its input fully, then parses it with [KdomBuilder.build]; read errors propagate as [IOException] (as
 * from jdom2's `SAXBuilder`). The stream/reader is not closed (the caller owns it).
 */

/** Reads [reader] to the end and builds the document from the characters (no encoding detection, like a SAX `Reader` source). */
@Throws(JDOMException::class, IOException::class)
public fun KdomBuilder.build(reader: Reader): Document = build(reader.readText())

/** Reads [input] to the end and builds the document from the bytes (BOM / XML declaration encoding detection). */
@Throws(JDOMException::class, IOException::class)
public fun KdomBuilder.build(input: InputStream): Document = build(input.readBytes())

/** Reads [file] and builds the document from its bytes (BOM / XML declaration encoding detection). */
@Throws(JDOMException::class, IOException::class)
public fun KdomBuilder.build(file: File): Document = build(file.readBytes())
