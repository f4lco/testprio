package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.probe.Job
import mu.KotlinLogging
import java.io.File

class Cache(val directory: File) {

    private val LOG = KotlinLogging.logger {}
    private val cache = mutableMapOf<Job, Matrix>()

    init {
        if (!directory.exists() && !directory.mkdirs()) {
            LOG.error { "Cannot create cache directory $directory" }
        }
    }

    fun get(job: Job, factory: (Job) -> Matrix): Matrix {
        return cache[job] ?: computeAndStore(job, factory)
    }

    private fun computeAndStore(job: Job, factory: (Job) -> Matrix): Matrix {
        val matrix = loadFile(job) ?: fromFactory(job, factory)
        cache[job] = matrix
        return matrix
    }

    private fun cacheFileFor(job: Job): File = File(directory, "${job.job}.json")

    private fun loadFile(job: Job): Matrix? = MatrixStore.read(cacheFileFor(job))

    private fun fromFactory(job: Job, factory: (Job) -> Matrix): Matrix {
        val matrix = factory(job)
        MatrixStore.write(matrix, cacheFileFor(job))
        return matrix
    }
}
