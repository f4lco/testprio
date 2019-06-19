package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import kotlin.math.pow
import kotlin.math.sqrt

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
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)

        val relevantTests: Set<String> = collectRelevantTests(p, sumMatrix)
        val priorities = priorities(sumMatrix, relevantTests)

        return p.testResults.sortedByDescending { priorities[it.name] }
    }

    private fun collectRelevantTests(p: Params, m: Matrix): Set<String> {
        return m.keys.filter { it.fileName in p.changedFiles }
                .map { it.testName }
                .toSet()
    }

    internal fun priorities(m: Matrix, relevantTests: Set<String>): Map<String, Double> {
        val testToSimilarity = similarities(m, relevantTests)

        return m.testNames().associateWith { tc ->
            m.filterKeys { it.testName == tc }.values.sum() * (testToSimilarity[tc] ?: 0.0)
        }
    }

    private fun similarities(m: Matrix, relevantTests: Set<String>): Map<String, Double> = m.testNames().associateWith { tc ->
        relevantTests.parallelStream().mapToDouble { relevantTC ->

            val a = m.fileDistribution(relevantTC)
            val b = m.fileDistribution(tc)
            var dot = 0.0
            var normA = 0.0
            var normB = 0.0

            for ((va, vb) in a.zip(b)) {
                dot += va * vb
                normA += va.pow(2.0)
                normB += vb.pow(2.0)
            }

            dot / (sqrt(normA) * sqrt(normB))
        }.max().orElse(0.0)
    }
}