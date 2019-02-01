package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Patches
import de.hpi.swa.testprio.probe.TestResults
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import kotlinx.serialization.Serializable
import org.jooq.DSLContext

/**
 * FIXME
 * Add test
 * Implement devaluation of prior results in reducer
 * Implement similarity search
 * have debug counter for common error situations (not resolved git commits etc)
 */
class ChangeMatrixStrategy(val context: DSLContext,
                           val windowSize: Int = 100,
                           val cache: Cache) : PrioritisationStrategy {

    private fun matrixFor(jobId: String): Matrix {
        return cache.get(jobId, ::computeMatrix)
    }

    private fun computeMatrix(jobId: String): Matrix {
        val changedFiles = Patches.selectPatches(context, jobId)
        val testResults = TestResults.ofJob(context, jobId)
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

    @Serializable
    data class Matrix(val jobId: String, val matrix: Map<Key, Int>)


    @Serializable
    data class Key(val fileName: String, val testName: String)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobsByWindowSize(p).map(::matrixFor)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), ::combine)

        return p.testResults.sortedByDescending { test ->

            sumMatrix.matrix.filterKeys { key ->
                key.testName == test.name && key.fileName in p.changedFiles
            }.values.sum()

        }
    }

    private fun selectJobsByWindowSize(p: Params): Sequence<String> {
        val end = p.jobIds.indexOfFirst { p.jobId == it }
        if (end == -1) throw IllegalArgumentException(p.jobId)
        val begin = Math.max(end - windowSize, 0)
        return p.jobIds.subList(begin, end).asSequence()
    }

    private fun combine(left: Matrix, right: Matrix) =
            Matrix(right.jobId, (left.matrix.keys + right.matrix.keys).associateWith {
                (left.matrix[it] ?: 0) + (right.matrix[it] ?: 0)
            })
}