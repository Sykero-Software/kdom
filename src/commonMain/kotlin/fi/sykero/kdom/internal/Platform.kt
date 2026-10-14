package fi.sykero.kdom.internal

/**
 * Platform seams of kdom. Everything else in kdom is common Kotlin.
 */

/**
 * Flushes [out] when the platform can (JVM: a `java.io.Flushable` such as a `Writer`); a no-op otherwise.
 * jdom2's `XMLOutputter.output(.., Writer)` flushed the writer after writing.
 */
internal expect fun flushIfPossible(out: Appendable)

/** The platform line separator (JVM: the `line.separator` system property; iOS: `"\n"`), for `LineSeparator.SYSTEM`. */
internal expect fun systemLineSeparator(): String

/**
 * jdom2's checked `java.io.IOException` (JVM: a typealias, so `@Throws(IOException::class)` keeps the JVM throws
 * clauses and Java callers can still catch it). Common kdom code never throws it itself; on the JVM an
 * `Appendable` such as a `Writer` may.
 */
expect open class IOException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
}
