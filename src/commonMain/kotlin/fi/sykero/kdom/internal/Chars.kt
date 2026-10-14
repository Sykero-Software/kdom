package fi.sykero.kdom.internal

/**
 * `java.lang.Character.isWhitespace(ch)`: a Unicode space, line or paragraph separator that is not a no-break space
 * (U+00A0, U+2007, U+202F), or one of U+0009-U+000D, U+001C-U+001F. Kotlin's `Char.isWhitespace()` also accepts the
 * no-break spaces, so jdom2 code that called `Character.isWhitespace` uses this.
 */
internal fun isJavaWhitespace(ch: Char): Boolean {
    return when (ch) {
        ' ', ' ', ' ' -> false
        '\t', '\n', '\u000B', '\u000C', '\r', '\u001C', '\u001D', '\u001E', '\u001F' -> true
        else -> when (ch.category) {
            CharCategory.SPACE_SEPARATOR, CharCategory.LINE_SEPARATOR, CharCategory.PARAGRAPH_SEPARATOR -> true
            else -> false
        }
    }
}

/** `String.format("%0<width>x", value)` for a non-negative [value]: lower-case hex, zero-padded to [width]. */
internal fun hex(value: Int, width: Int): String = value.toString(16).padStart(width, '0')
