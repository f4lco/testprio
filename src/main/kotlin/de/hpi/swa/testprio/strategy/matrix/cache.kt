package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy.Matrix
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

class Cache(val directory: File) {

    private val logger = KotlinLogging.logger {}
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

    private fun loadFile(jobId: String): Matrix? {
        val file = cacheFileFor(jobId)
        if (!file.exists()) return null
        logger.debug { "Loading $file" }
        return Json.parse(Matrix.serializer(), file.readText())
    }

    private fun fromFactory(jobId: String, factory: (String) -> Matrix): Matrix {
        val matrix = factory(jobId)
        val file = cacheFileFor(jobId)
        logger.debug { "Writing $file" }
        file.writeText(Json.stringify(Matrix.serializer(), matrix))
        return matrix
    }
}
