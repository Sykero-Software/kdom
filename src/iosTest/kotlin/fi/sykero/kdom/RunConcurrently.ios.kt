package fi.sykero.kdom

import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.native.concurrent.ObsoleteWorkersApi
import kotlin.native.concurrent.TransferMode
import kotlin.native.concurrent.Worker

/** Native: one [Worker] per worker; each spins on a shared start gate so they run [block] together. */
@OptIn(ObsoleteWorkersApi::class, ExperimentalAtomicApi::class)
actual fun <T> runConcurrently(workers: Int, block: () -> T): List<T> {
    val gate = AtomicInt(0)
    val started = List(workers) { Worker.start() }
    val futures = started.map { worker ->
        worker.execute(TransferMode.SAFE, { gate to block }) { (g, b) ->
            while (g.load() == 0) {
                // spin until every worker has its job
            }
            runCatching { b() }
        }
    }
    gate.store(1)
    val results = futures.map { it.result }
    started.forEach { it.requestTermination().result }
    return results.map { it.getOrThrow() }
}
