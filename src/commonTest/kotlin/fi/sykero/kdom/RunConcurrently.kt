package fi.sykero.kdom

/**
 * Runs [block] on [workers] threads (JVM) or workers (Native) that all start together (a start gate), and returns
 * each worker's result in worker order. Rethrows the first worker failure.
 */
expect fun <T> runConcurrently(workers: Int, block: () -> T): List<T>
