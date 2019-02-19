package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

/**
 * Prioritize test cases whose failure distribution is similar.
 *
 * Given a changeset, pick a set of relevant TC as follows: collect all red TC for all
 * of the elements of the changeset (= TC which are potentially relevant given this
 * set of files).
 *
 * Prioritize the remaining test cases by the minimum distance to any of the relevant
 * test cases.
 */
class TestCaseFailureDistributionSimilarity(
    repository: Repository,
    cache: Cache,
    val prior: Double,
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val relevantTC: Set<String> = collectRelevantTC(p, sumMatrix)
        val similarities: Map<TestResult, Double> = p.testResults.associateWith { tc ->
            similarity(sumMatrix, relevantTC, tc.name)
        }

        return p.testResults.sortedByDescending { similarities[it] }
    }

    private fun selectJobs(p: Params): List<String> = p.jobIds.subList(0, p.jobIndex)

    private fun collectRelevantTC(p: Params, m: Matrix): Set<String> {
        return m.matrix.keys
                .filter { it.fileName in p.changedFiles }
                .map { it.testName }
                .toSet()
    }

    private fun similarity(m: Matrix, relevantTC: Set<String>, tc: String): Double {
        return relevantTC.map { similarity(m, it, tc) }.min() ?: 0.0
    }

    private fun similarity(m: Matrix, tc1: String, tc2: String): Double {
        val distance = norm(tc1, m).toMutableMap()

        for (entry in norm(tc2, m)) {
            distance.merge(entry.key, entry.value, ::squaredError)
        }

        return 1 - (distance.values.min() ?: 1.0)
    }

    private fun norm(tc: String, m: Matrix): Map<String, Double> {
        val allFiles: Set<String> = m.matrix.keys.map { it.fileName }.toSet()

        val counts = allFiles.associateWith { fileName ->
            val failureCount = m.matrix[Key(fileName, tc)] ?: 0
            prior + failureCount
        }

        val normalizer = counts.values.sum()
        return counts.mapValues { entry -> entry.value / normalizer }
    }

    private fun squaredError(a: Double, b: Double) = Math.pow(a - b, 2.0)
}