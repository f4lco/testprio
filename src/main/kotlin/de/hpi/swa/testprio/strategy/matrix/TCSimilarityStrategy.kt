package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class TCSimilarityStrategy(
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

        val tc: Set<String> = collectTC(p, sumMatrix)
        return p.testResults.sortedBy {
            distance(sumMatrix, tc, it.name)
        }
    }

    private fun selectJobs(p: Params): List<String> {
        val end = p.jobIds.indexOf(p.jobId)
        if (end == -1) throw IllegalArgumentException(p.jobId)
        return p.jobIds.subList(0, end)
    }

    private fun collectTC(p: Params, m: Matrix): Set<String> {
        return m.matrix.keys
                .filter { it.fileName in p.changedFiles }
                .map { it.testName }
                .toSet()
    }

    private fun distance(m: Matrix, relevantTC: Set<String>, tc: String): Int {
        return relevantTC.map { distance(m, it, tc) }.min() ?: 0
    }

    private fun distance(m: Matrix, tc1: String, tc2: String): Int {
        val files = m.matrix.keys.filter { it.testName == tc1 || it.testName == tc2 }.map { it.fileName }.toSet()
        return files.sumBy { file ->
            val v1 = m.matrix[Key(file, tc1)] ?: 0
            val v2 = m.matrix[Key(file, tc2)] ?: 0
            val distance = v2 - v1
            distance * distance
        }
    }
}