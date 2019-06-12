package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

/**
 * Naive matrix strategy.
 *
 * Considering *N* observations from the past, build a sum matrix (with optional devaluation).
 * Limit the rows of the sum matrix to the current set of changed files, and compute column
 * sum (= cumulative failure counts) for all TC. The failure counts represent priorities,
 * such that TC of higher failure probability are promoted. The remaining TC - TC which never
 * failed given any of the changed files of this revision - have priority zero.
 *
 * @param reducer the reduction function to use when summing matrices
 * @param windowSize the maximum number of observations to consider; `-1` means all observations
 */
class NaiveMatrix(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val windowSize: Int = 100
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = selectJobsByWindowSize(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)
        val priorities = priority(sumMatrix, p.changedFiles)
        return p.testResults.sortedByDescending { priorities(it.name) }
    }

    internal fun priority(m: Matrix, changedFiles: List<String>): (String) -> Int {
        return { testName ->
            m.matrix.filterKeys { key ->
                key.testName == testName && key.fileName in changedFiles
            }.values.sum()
        }
    }

    private fun selectJobsByWindowSize(p: Params): Sequence<String> {
        val end = p.jobIndex
        val begin = when (windowSize) {
            -1 -> 0
            else -> Math.max(end - windowSize, 0)
        }
        return p.jobIds.subList(begin, end).asSequence()
    }
}