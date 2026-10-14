package fi.sykero.kdom.input

import fi.sykero.kdom.output.Format
import fi.sykero.kdom.output.XMLOutputter
import java.io.StringReader
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.jdom2.input.SAXBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 8 threads each build 500 distinct documents, once with their own [KdomBuilder]s and once through a shared
 * `ArrayBlockingQueue<KdomBuilder>(32)` pool used exactly like JSword's `OSISFilter.parse` (`poll() ?: KdomBuilder()`,
 * build, `offer`). Every tree must serialize (raw format) to what jdom2's `SAXBuilder` + `XMLOutputter` give for the
 * same input, computed single-threaded beforehand.
 */
class KdomBuilderConcurrencyTest {
    private val threads = 8
    private val docsPerThread = 500

    private fun xml(t: Int, i: Int) =
        "<root xmlns=\"urn:d$t\" xmlns:p=\"urn:p$t\" id=\"t$t-$i\"><p:a x=\"$i\" p:y=\"&lt;$t&gt;\">text $t/$i &amp; more&#233;</p:a>" +
            "<![CDATA[c$i]]><!--k$t.$i--><?pi $i?><b>${"w".repeat(i % 7)} </b></root>"

    private val expected: List<List<String>> = List(threads) { t ->
        List(docsPerThread) { i ->
            org.jdom2.output.XMLOutputter(org.jdom2.output.Format.getRawFormat())
                .outputString(SAXBuilder().build(StringReader(xml(t, i))).rootElement)
        }
    }

    private fun raw(doc: fi.sykero.kdom.Document) = XMLOutputter(Format.getRawFormat()).outputString(doc.rootElement)

    private fun runThreads(work: (Int) -> List<String>): List<List<String>> {
        val pool = Executors.newFixedThreadPool(threads)
        try {
            val gate = CountDownLatch(1)
            val futures = (0 until threads).map { t -> pool.submit<List<String>> { gate.await(); work(t) } }
            gate.countDown()
            return futures.map { it.get(5, TimeUnit.MINUTES) }
        } finally {
            pool.shutdownNow()
        }
    }

    @Test fun ownBuildersPerThread() {
        val results = runThreads { t -> List(docsPerThread) { i -> raw(KdomBuilder().build(xml(t, i))) } }
        assertEquals(expected, results)
    }

    @Test fun oneBuilderPerThreadReused() {
        val results = runThreads { t ->
            val builder = KdomBuilder()
            List(docsPerThread) { i -> raw(builder.build(xml(t, i))) }
        }
        assertEquals(expected, results)
    }

    @Test fun sharedPoolLikeOsisFilter() {
        val builders = ArrayBlockingQueue<KdomBuilder>(32)
        val results = runThreads { t ->
            List(docsPerThread) { i ->
                val builder = builders.poll() ?: KdomBuilder()
                val doc = builder.build(xml(t, i))
                builders.offer(builder)
                raw(doc)
            }
        }
        assertEquals(expected, results)
        assertTrue(builders.size in 1..32)
    }
}
