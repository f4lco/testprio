package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy.Matrix
import java.io.File

class Cache(val directory: File) {

    private val cache = mutableMapOf<String, Matrix>()

    fun get(jobId: String, factory: (String) -> Matrix): Matrix {
        return cache[jobId] ?: computeAndStore(jobId, factory)
    }

    private fun computeAndStore(jobId: String, factory: (String) -> Matrix): Matrix {
        val matrix = loadFile(jobId) ?: fromFactory(jobId, factory)
        cache[jobId] = matrix
        return matrix
    }

    private fun cacheFileFor(jobId: String): File = File(directory, "$jobId.json")

    private fun loadFile(jobId: String): Matrix? = MatrixStore.read(cacheFileFor(jobId))

    private fun fromFactory(jobId: String, factory: (String) -> Matrix): Matrix {
        val matrix = factory(jobId)
        MatrixStore.write(matrix, cacheFileFor(jobId))
        return matrix
    }
}
