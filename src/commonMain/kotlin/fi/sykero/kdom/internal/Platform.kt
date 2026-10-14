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

/**
 * Base of kdom's live lists (`AttributeList`, `ContentList`, `ContentList.FilterList`). On the JVM it overrides
 * `java.util.List.sort(Comparator)` to call [sortLive], as jdom2's lists did: the JDK default `sort` re-sets every
 * element through a list iterator, which a live list rejects for already-attached content. Other platforms have no
 * such member (Kotlin's `sortWith` extension on a live list is not supported; use `Element.sortContent` etc.).
 */
internal expect abstract class KdomMutableList<E>() : AbstractMutableList<E> {
    /** Sorts the list in place, keeping the relative order of equal entries (jdom2's `sort(Comparator)` body). */
    protected abstract fun sortLive(comp: Comparator<in E>?)
}

/** The class name used in jdom2's messages (JVM: `obj.getClass().getName()`; elsewhere the qualified Kotlin name). */
internal expect fun className(obj: Any): String

/** The name `ClassFilter.toString()` shows (JVM: the boxed Java class name, as jdom2's `Class.getName()`). */
internal expect fun className(kclass: kotlin.reflect.KClass<*>): String

/**
 * `@Throws` for a property getter. Common `kotlin.Throws` targets only functions and constructors; on the JVM this is
 * `kotlin.jvm.Throws`, so the getters keep jdom2's checked `throws DataConversionException` clause (Java callers
 * catch it). Other platforms drop it (`@OptionalExpectation`).
 */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
@Target(AnnotationTarget.PROPERTY_GETTER)
internal expect annotation class GetterThrows(vararg val exceptionClasses: kotlin.reflect.KClass<out Throwable>)
