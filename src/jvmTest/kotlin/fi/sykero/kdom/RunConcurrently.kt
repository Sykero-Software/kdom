package fi.sykero.kdom

import java.util.concurrent.CountDownLatch

/**
 * Runs [block] on [workers] threads that all start together (a start gate), and returns each worker's result
 * in worker order. Rethrows the first worker failure.
 */
fun <T> runConcurrently(workers: Int, block: () -> T): List<T> {
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
