package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritizationStrategy

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
 */
class NaiveMatrix(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer
) : PrioritizationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)
        val priorities = priority(sumMatrix, p.changedFiles)
        return p.testResults.sortedByDescending { priorities(it.name) }
    }

    internal fun priority(m: Matrix, changedFiles: List<String>): (String) -> Double {
        return { testName ->
            m.filterKeys { key ->
                key.testName == testName && key.fileName in changedFiles
            }.values.sum()
        }
    }
}