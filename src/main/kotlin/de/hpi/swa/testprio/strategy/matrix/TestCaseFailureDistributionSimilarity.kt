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
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val relevantTests: Set<String> = collectRelevantTests(p, sumMatrix)
        val priorities = priorities(sumMatrix, relevantTests)

        return p.testResults.sortedByDescending { priorities[it.name] }
    }

    private fun selectJobs(p: Params): List<String> = p.jobIds.subList(0, p.jobIndex)

    private fun collectRelevantTests(p: Params, m: Matrix): Set<String> {
        return m.matrix.keys
                .filter { it.fileName in p.changedFiles }
                .map { it.testName }
                .toSet()
    }

    internal fun priorities(m: Matrix, relevantTests: Set<String>): Map<String, Double> {
        val testToSimilarity = similarities(m, relevantTests)

        return m.testNames().associateWith { tc ->
            m.matrix.filterKeys { it.testName == tc }.values.sum() * (testToSimilarity[tc] ?: 0.0)
        }
    }

    private fun similarities(m: Matrix, relevantTests: Set<String>): Map<String, Double> {
        val testToDistance = m.testNames().associateWith { testName ->
            distance(m, relevantTests, testName)
        }

        val distances = testToDistance.values.sum()
        return testToDistance.mapValues { 1 - it.value / distances }
    }

    private fun distance(m: Matrix, relevantTests: Set<String>, tc: String): Double {
        return relevantTests.map { distance(m, it, tc) }.min() ?: 0.0
    }

    private fun distance(m: Matrix, t1: String, t2: String): Double {
        return norm(t1, m).zip(norm(t2, m)).map { (a, b) -> squaredError(a, b) }.sum()
    }

    private fun norm(t: String, m: Matrix): List<Double> {
        val distribution = m.fileDistribution(t)
        val sum = distribution.sum()
        return distribution.map { it / sum }
    }

    private fun squaredError(a: Double, b: Double) = Math.pow(a - b, 2.0)
}