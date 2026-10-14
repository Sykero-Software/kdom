package fi.sykero.kdom.input

/**
 * A DOCTYPE read by [SourceScanner] in permissive mode (`allowDocType = true`). The external subset is never read.
 * [start]/[end] are the offsets of `<!DOCTYPE` and just after its `>`.
 */
internal class ScannedDocType(
    val name: String,
    val publicId: String?,
    val systemId: String?,
    val start: Int,
    val end: Int,
    /** The internal subset's declarations, comments and processing instructions in document order (first declaration of a name only). */
    val items: List<DtdItem>,
    /** General entities by name (the first declaration wins, as in XML). */
    val entities: Map<String, DtdItem.Entity>,
)

/** One item of an internal subset, as SAX's `DeclHandler`/`LexicalHandler`/`ContentHandler` would report it. */
internal sealed class DtdItem {
    class ElementDecl(val name: String, val model: String) : DtdItem()

    /**
     * An entity declaration; [name] starts with `%` for a parameter entity (as in SAX). [value] is the replacement text
     * of an internal entity (character references expanded, general entity references kept), else null.
     */
    class Entity(val name: String, val value: String?, val publicId: String?, val systemId: String?, val notation: String?) : DtdItem()
    class Notation(val name: String, val publicId: String?, val systemId: String?) : DtdItem()
    class Comment(val text: String) : DtdItem()
    class Pi(val target: String, val data: String) : DtdItem()
}

/** How a non-predefined entity reference in content or an attribute value is reported. */
internal sealed class EntityUse {
    /** Replaced by [text] (an internal entity; or nothing, for an undeclared entity in an attribute value). */
    class Expand(val text: String) : EntityUse()

    /** Reported as `skippedEntity(name)` (external or undeclared entity with an unread external subset). */
    class Skip(val name: String) : EntityUse()
}
