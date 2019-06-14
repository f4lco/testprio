package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.probe.Repository

class UnitMatrix(
    val repository: Repository,
    val cache: Cache
) {

    fun get(job: Job): Matrix {
        return cache.get(job, ::computeMatrix)
    }

    private fun computeMatrix(job: Job): Matrix {
        val changedFiles = repository.changedFiles(job)
        val testResults = repository.testResults(job)
        return createUnitMatrix(changedFiles, testResults)
    }

    private fun createUnitMatrix(changedFiles: List<String>, testResults: List<TestResult>): Matrix {
        val matrix = mutableMapOf<Key, Int>()
        for (test in testResults.filter { it.red > 0 }) {
            for (file in changedFiles) {
                matrix.merge(Key(file, test.name), test.red, Int::plus)
            }
        }
        return if (matrix.isEmpty()) Matrix.empty() else Matrix(matrix)
    }
}