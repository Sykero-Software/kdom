package fi.sykero.kdom.internal

internal actual fun flushIfPossible(out: Appendable) {
    (out as? java.io.Flushable)?.flush()
}

internal actual fun systemLineSeparator(): String = System.getProperty("line.separator")

actual typealias IOException = java.io.IOException
