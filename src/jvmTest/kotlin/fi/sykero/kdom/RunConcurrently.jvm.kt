package fi.sykero.kdom

import java.util.concurrent.CountDownLatch

/** JVM: one thread per worker, released together by a [CountDownLatch]. */
actual fun <T> runConcurrently(workers: Int, block: () -> T): List<T> {
    val gate = CountDownLatch(1)
    val results = arrayOfNulls<Any?>(workers)
    val failures = arrayOfNulls<Throwable>(workers)
    val threads = (0 until workers).map { i ->
        Thread {
            gate.await()
            try {
                results[i] = block()
            } catch (t: Throwable) {
                failures[i] = t
            }
        }.also { it.start() }
    }
    gate.countDown()
    threads.forEach { it.join() }
    failures.firstOrNull { it != null }?.let { throw it }
    @Suppress("UNCHECKED_CAST")
    return results.toList() as List<T>
}
