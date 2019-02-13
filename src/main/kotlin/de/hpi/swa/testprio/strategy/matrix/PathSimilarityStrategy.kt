package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import org.apache.commons.text.similarity.LongestCommonSubsequence
import org.apache.commons.text.similarity.SimilarityScore

class PathSimilarityStrategy(
    val repository: Repository,
    val cache: Cache,
    val reducer: Reducer,
    val similarity: SimilarityScore<Int> = LongestCommonSubsequence()
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

        val priorities = sumMatrix.fileNames().associateWith { similarity(p, it) }

        return p.testResults.sortedByDescending { tc ->

            sumMatrix.matrix
                    .filterKeys { it.testName == tc.name }
                    .map { (priorities[it.key.fileName] ?: 0.0) * it.value.toDouble() }
                    .sum()
        }
    }

    private fun selectJobs(p: Params): List<String> {
        val end = p.jobIds.indexOf(p.jobId)
        if (end == -1) throw IllegalArgumentException(p.jobId)
        return p.jobIds.subList(0, end)
    }

    private fun similarity(p: Params, fileName: String): Double {
        return p.changedFiles.map { similarity.apply(it, fileName).toDouble() }.max() ?: 1.0
    }
}