package fi.sykero.kdom.test.util

import fi.sykero.kdom.internal.unsafeNull

/**
 * True when the platform checks the runtime type of an erased generic cast at the call into a typed member (the JVM does
 * through its bridge methods, Kotlin/Native does not). Tests that hand an object of the wrong class to a typed list through
 * an erased cast only run where this is true.
 */
expect val checksErasedCasts: Boolean

/** A `null` typed as [T], passed through an erased cast so that a Kotlin callee's own null check is what gets exercised. */
fun <T> nullT(): T = unsafeNull(null)

/**
 * Outputs [content] with the `OutputStream` overload of [outputter] that matches its type and returns the bytes decoded as
 * a string, or `null` on a platform that has no `OutputStream` form of the outputter (iOS). [methodprefix] is `"output"`, or
 * `"outputElementContent"` for the content of an `Element`.
 */
expect fun outputViaStream(outputter: fi.sykero.kdom.output.XMLOutputter, methodprefix: String, content: Any): String?
