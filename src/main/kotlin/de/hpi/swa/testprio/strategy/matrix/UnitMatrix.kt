package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository

class UnitMatrix(
    val repository: Repository,
    val cache: Cache
) {

    fun get(jobId: String): Matrix {
        return cache.get(jobId, ::computeMatrix)
    }

    private fun computeMatrix(jobId: String): Matrix {
        val changedFiles = repository.changedFiles(jobId)
        val testResults = repository.testResults(jobId)
        return createUnitMatrix(jobId, changedFiles, testResults)
    }

    private fun createUnitMatrix(jobId: String, changedFiles: List<String>, testResults: List<TestResult>): Matrix {
        val matrix = mutableMapOf<Key, Int>()
        for (test in testResults.filter { it.red > 0 }) {
            for (file in changedFiles) {
                matrix.merge(Key(file, test.name), test.red, Int::plus)
            }
        }
        return if (matrix.isEmpty()) Matrix(jobId, emptyMap()) else Matrix(jobId, matrix)
    }
}