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

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val tc: Set<String> = collectTC(p, sumMatrix)
        return p.testResults.sortedBy {
            distance(sumMatrix, tc, it.name)
        }
    }

    private fun selectJobs(p: Params): List<String> = p.jobIds.subList(0, p.jobIndex)

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