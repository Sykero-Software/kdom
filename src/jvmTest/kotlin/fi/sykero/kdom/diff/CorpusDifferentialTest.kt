package fi.sykero.kdom.diff

import fi.sykero.kdom.input.KdomBuilder
import fi.sykero.kdom.input.XmlEventParser
import java.io.File
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory
import org.jdom2.input.SAXBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.xml.sax.InputSource

/**
 * Corpus differential: kdom against jdom2 / the JDK SAX parser on the strings JSword's filters really parse
 * (`jsword`'s `CorpusDumpTest`, `-Dkdom.corpus=<dir>`; skipped when the property is unset, a failure when it is set but
 * the directory or its `manifest.tsv` is missing or empty).
 *
 * How each input is compared, by manifest `stage`:
 *  - `osis-*` (OSISFilter, jdom2 `SAXBuilder`, namespace-aware, external-general-entities off): accept/reject must
 *    match between `SAXBuilder` and [KdomBuilder]; when both accept, `XMLOutputter` raw, pretty and compact output
 *    must be byte-identical.
 *  - `thml-*` (THMLFilter, a default JAXP `SAXParserFactory`: namespace-UNAWARE, no tree builder): [KdomBuilder] has
 *    no namespace-unaware mode, so the comparison is at the SAX event level: the JDK parser with a recording handler
 *    against `XmlEventParser(namespaceAware = false)` with the same recording (element qName, every attribute
 *    qName/type/value, merged character data, processing instructions, skipped entities); accept/reject must match.
 *  - `gbf-*` are not XML and are not compared.
 *
 * Counts printed as `KDOMCORPUS ...`; the gate is ref-only-reject = kdom-only-reject = different = 0, and the seven
 * real rejects of the spike must be both-reject (by module + key + stage).
 */
class CorpusDifferentialTest {

    private enum class Outcome { EQUAL, BOTH_REJECT, REF_ONLY_REJECT, KDOM_ONLY_REJECT, DIFFERENT }

    @Test
    fun corpusMatchesJdom2() {
        val prop = System.getProperty("kdom.corpus").orEmpty()
        assumeTrue("kdom.corpus not set", prop.isNotEmpty() && prop != "false")
        val dir = File(prop)
        val manifest = File(dir, "manifest.tsv")
        assertTrue("kdom.corpus is set but $manifest is missing", manifest.isFile)
        val rows = manifest.readLines(Charsets.UTF_8).filter { it.isNotBlank() }.map { it.split('\t') }
        assertTrue("kdom.corpus manifest is empty: $manifest", rows.isNotEmpty())

        val counts = sortedMapOf<Outcome, Int>()
        val byStage = sortedMapOf<String, Int>()
        val outcomes = HashMap<String, Outcome>()
        val problems = ArrayList<String>()
        var skipped = 0
        for (r in rows) {
            val module = r[0]; val key = r[1]; val stage = r[2]; val file = r[3]
            val label = "$module $key $stage ($file)"
            val input = File(dir, file).readText(Charsets.UTF_8)
            val outcome = when {
                stage.startsWith("osis-") -> compareTree(input)
                stage.startsWith("thml-") -> compareSaxEvents(input)
                else -> { skipped++; null }
            } ?: continue
            counts.merge(outcome.first, 1, Int::plus)
            byStage.merge(stage.substringBefore('-') + "/" + outcome.first, 1, Int::plus)
            outcomes["$module\t$key\t$stage"] = outcome.first
            if (outcome.first != Outcome.EQUAL && outcome.first != Outcome.BOTH_REJECT && problems.size < 25) {
                problems += "${outcome.first} $label: ${outcome.second}"
            }
        }
        val line = Outcome.values().joinToString(" ") { "${it.name.lowercase().replace('_', '-')}=${counts[it] ?: 0}" }
        println("KDOMCORPUS inputs=${rows.size - skipped} skipped-nonxml=$skipped $line")
        byStage.forEach { (k, v) -> println("KDOMCORPUS $k $v") }

        val bad = Outcome.REF_ONLY_REJECT.let { (counts[it] ?: 0) } + (counts[Outcome.KDOM_ONLY_REJECT] ?: 0) + (counts[Outcome.DIFFERENT] ?: 0)
        if (bad != 0) fail("$bad corpus inputs differ ($line):\n" + problems.joinToString("\n"))

        // The spike's seven real rejects: NASB through the raw stage, ISV through three chain stages.
        val present = rows.map { it[0] }.toSet()
        val mustReject = buildList {
            for (k in listOf("Mark.10.34", "Luke.4.43", "Luke.18.42", "John.13.26")) add("NASB\t$k\tosis-raw")
            for (s in listOf("thml-entities", "thml-chars", "thml-close")) add("ISV\t1Cor.14.33\t$s")
        }
        for (id in mustReject) {
            if (id.substringBefore('\t') !in present) continue // that module is not part of this corpus
            assertEquals("real reject must be both-reject: $id", Outcome.BOTH_REJECT, outcomes[id])
        }
    }

    // ---- OSIS: tree level ----

    private fun compareTree(input: String): Pair<Outcome, String>? {
        val jdom = try {
            SAXBuilder().apply { setFeature("http://xml.org/sax/features/external-general-entities", false) }
                .build(StringReader(input))
        } catch (e: Exception) { e }
        val kdom = try { KdomBuilder().build(input) } catch (e: Exception) { e }
        val jOk = jdom is org.jdom2.Document
        val kOk = kdom is fi.sykero.kdom.Document
        if (!jOk && !kOk) {
            return if (kdom is fi.sykero.kdom.input.JDOMParseException) Outcome.BOTH_REJECT to ""
            else Outcome.DIFFERENT to "kdom rejected with ${kdom::class.simpleName}, not JDOMParseException: $kdom"
        }
        if (!jOk) return Outcome.REF_ONLY_REJECT to "jdom2 rejected: ${(jdom as Exception).message}"
        if (!kOk) return Outcome.KDOM_ONLY_REJECT to "kdom rejected: ${(kdom as Exception).message}"
        val jd = jdom as org.jdom2.Document
        val kd = kdom as fi.sykero.kdom.Document
        val pairs = listOf(
            Triple("raw", org.jdom2.output.Format.getRawFormat(), fi.sykero.kdom.output.Format.getRawFormat()),
            Triple("pretty", org.jdom2.output.Format.getPrettyFormat(), fi.sykero.kdom.output.Format.getPrettyFormat()),
            Triple("compact", org.jdom2.output.Format.getCompactFormat(), fi.sykero.kdom.output.Format.getCompactFormat()),
        )
        for ((name, jf, kf) in pairs) {
            val j = org.jdom2.output.XMLOutputter(jf).outputString(jd)
            val k = fi.sykero.kdom.output.XMLOutputter(kf).outputString(kd)
            if (j != k) return Outcome.DIFFERENT to "$name output differs at ${firstDiff(j, k)}"
        }
        return Outcome.EQUAL to ""
    }

    private fun firstDiff(a: String, b: String): String {
        val i = (0 until minOf(a.length, b.length)).firstOrNull { a[it] != b[it] } ?: minOf(a.length, b.length)
        fun ctx(s: String) = s.substring(maxOf(0, i - 30), minOf(s.length, i + 30)).replace("\n", "\\n")
        return "$i jdom2=[${ctx(a)}] kdom=[${ctx(b)}]"
    }

    // ---- ThML: SAX event level, namespace-unaware ----

    private class Rec {
        val events = ArrayList<String>()
        private val text = StringBuilder()
        fun chars(ch: CharArray, s: Int, l: Int) { text.append(ch, s, l) }
        fun add(e: String) {
            if (text.isNotEmpty()) { events += "chars(${text})"; text.setLength(0) }
            events += e
        }
        fun finish(): List<String> { add("end"); return events }
    }

    private fun compareSaxEvents(input: String): Pair<Outcome, String> {
        val jdom = try { jdkEvents(input) } catch (e: Exception) { e }
        val kdom = try { kdomEvents(input) } catch (e: Exception) { e }
        val jOk = jdom is List<*>
        val kOk = kdom is List<*>
        if (!jOk && !kOk) {
            return if (kdom is fi.sykero.kdom.sax.SAXParseException) Outcome.BOTH_REJECT to ""
            else Outcome.DIFFERENT to "kdom rejected with ${kdom::class.simpleName}: $kdom"
        }
        if (!jOk) return Outcome.REF_ONLY_REJECT to "JDK SAX rejected: ${(jdom as Exception).message}"
        if (!kOk) return Outcome.KDOM_ONLY_REJECT to "kdom rejected: ${(kdom as Exception).message}"
        if (jdom != kdom) {
            @Suppress("UNCHECKED_CAST") val j = jdom as List<String>
            @Suppress("UNCHECKED_CAST") val k = kdom as List<String>
            val i = (0 until minOf(j.size, k.size)).firstOrNull { j[it] != k[it] } ?: minOf(j.size, k.size)
            return Outcome.DIFFERENT to "event $i: JDK=${j.getOrNull(i)} kdom=${k.getOrNull(i)}"
        }
        return Outcome.EQUAL to ""
    }

    private fun jdkEvents(xml: String): List<String> {
        val rec = Rec()
        val h = object : org.xml.sax.helpers.DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String?, a: org.xml.sax.Attributes) {
                rec.add("start($qName" + (0 until a.length).joinToString("") { " ${a.getQName(it)}/${a.getType(it)}=${a.getValue(it)}" } + ")")
            }
            override fun endElement(uri: String?, localName: String?, qName: String?) { rec.add("end($qName)") }
            override fun characters(ch: CharArray, start: Int, length: Int) { rec.chars(ch, start, length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { rec.chars(ch, start, length) }
            override fun processingInstruction(target: String?, data: String?) { rec.add("pi($target,$data)") }
            override fun skippedEntity(name: String?) { rec.add("skipped($name)") }
        }
        SAXParserFactory.newInstance().newSAXParser().parse(InputSource(StringReader(xml)), h)
        return rec.finish()
    }

    private fun kdomEvents(xml: String): List<String> {
        val rec = Rec()
        val h = object : fi.sykero.kdom.sax.DefaultHandler() {
            override fun startElement(uri: String, localName: String, qName: String, atts: fi.sykero.kdom.sax.Attributes) {
                rec.add("start($qName" + (0 until atts.length).joinToString("") { " ${atts.getQName(it)}/${atts.getType(it)}=${atts.getValue(it)}" } + ")")
            }
            override fun endElement(uri: String, localName: String, qName: String) { rec.add("end($qName)") }
            override fun characters(ch: CharArray, start: Int, length: Int) { rec.chars(ch, start, length) }
            override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) { rec.chars(ch, start, length) }
            override fun processingInstruction(target: String, data: String?) { rec.add("pi($target,$data)") }
            override fun skippedEntity(name: String) { rec.add("skipped($name)") }
        }
        XmlEventParser(false).parse(xml, h)
        return rec.finish()
    }
}
