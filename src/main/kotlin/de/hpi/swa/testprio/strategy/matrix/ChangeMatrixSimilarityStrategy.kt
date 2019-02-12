package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class ChangeMatrixSimilarityStrategy(
    val repository: Repository,
    val cache: Cache,
    val reducer: Reducer
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
        val unitMatrices = selectJobs(p).map(::matrixFor)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val fileToSimilarity: Map<String, Double> = sumMatrix.fileNames.associate { fileName ->
            fileName to similarity(p, fileName, sumMatrix)
        }

        val order: Map<TestResult, Double> = p.testResults.associateWith { tc ->
            sumMatrix.matrix
                    .filterKeys { it.testName == tc.name }
                    .map { entry -> fileToSimilarity.getOrDefault(entry.key.fileName, 0.0) * entry.value.toDouble()
            }.sum()
        }

        return p.testResults.sortedByDescending { order[it] }
    }

    private fun selectJobs(p: Params): List<String> {
        val end = p.jobIds.indexOf(p.jobId)
        if (end == -1) throw IllegalArgumentException(p.jobId)
        return p.jobIds.subList(0, end)
    }

    private fun similarity(p: Params, file: String, matrix: Matrix): Double = p.changedFiles.map { changedFile ->
        similarity(matrix, file, changedFile)
    }.max() ?: 0.0

    private fun similarity(m: Matrix, f0: String, f1: String): Double {
        val testCases = m.matrix.keys.filter { it.fileName == f0 || it.fileName == f1 }.map { it.testName }
        val total = testCases.sumBy { testCase ->
            val count0: Int = m.matrix[Key(f0, testCase)] ?: 0
            val count1: Int = m.matrix[Key(f1, testCase)] ?: 0
            val diff = count1 - count0
            diff * diff
        }
        return if (total == 0) 1.0 else 1.0 / total
    }
}