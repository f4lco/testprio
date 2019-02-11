package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class ChangeMatrixStrategy(
    val repository: Repository,
    val cache: Cache,
    val reducer: Reducer,
    val windowSize: Int = 100
) : PrioritisationStrategy {

    private fun matrixFor(jobId: String): Matrix {
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

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobsByWindowSize(p).map(::matrixFor)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        return p.testResults.sortedByDescending { test ->

            sumMatrix.matrix.filterKeys { key ->
                key.testName == test.name && key.fileName in p.changedFiles
            }.values.sum()
        }
    }

    private fun selectJobsByWindowSize(p: Params): Sequence<String> {
        return when (windowSize) {

            -1 -> p.jobIds

            else -> {
                val end = p.jobIds.indexOfFirst { p.jobId == it }
                if (end == -1) throw IllegalArgumentException(p.jobId)
                val begin = Math.max(end - windowSize, 0)
                p.jobIds.subList(begin, end)
            }
        }.asSequence()
    }
}