package fi.sykero.kdom.internal

internal actual fun flushIfPossible(out: Appendable) {
    (out as? java.io.Flushable)?.flush()
}

internal actual fun systemLineSeparator(): String = System.getProperty("line.separator")

actual typealias IOException = java.io.IOException

internal actual abstract class KdomMutableList<E> actual constructor() : AbstractMutableList<E>() {
    protected actual abstract fun sortLive(comp: Comparator<in E>?)

    /** jdom2's `List.sort(Comparator)` override (the JDK default would re-set attached content). */
    override fun sort(c: Comparator<in E>?) {
        sortLive(c)
    }
}

internal actual fun className(obj: Any): String = obj.javaClass.name

internal actual fun className(kclass: kotlin.reflect.KClass<*>): String = kclass.javaObjectType.name
