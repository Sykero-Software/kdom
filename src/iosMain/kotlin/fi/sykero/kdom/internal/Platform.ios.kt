package fi.sykero.kdom.internal

internal actual fun flushIfPossible(out: Appendable) {
    // Native Appendables (StringBuilder, ...) have nothing to flush.
}

internal actual fun systemLineSeparator(): String = "\n"

actual open class IOException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
}

internal actual abstract class KdomMutableList<E> actual constructor() : AbstractMutableList<E>() {
    protected actual abstract fun sortLive(comp: Comparator<in E>?)
}
