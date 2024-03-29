package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritizationStrategy
import java.util.BitSet

/**
 * Take "hot" and "cold" files into account during matrix prioritization.
 *
 * Assign weights to file failure distributions, such that tests connected
 * to recently changed files are ranked higher.
 */
class RecentlyChanged(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val alpha: Double
) : PrioritizationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)
    private val histories = mutableMapOf<String, BitSet>()

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)
        val filePriorities = priorities(p.priorJobs, sumMatrix)

        val testPriorities = p.testResults.associateWith { tc ->
            sumMatrix.filterKeys { it.testName == tc.name }
                    .map { filePriorities.getOrDefault(it.key.fileName, 0.0) * it.value }
                    .sum()
        }

        p.changedFiles.forEach { histories.computeIfAbsent(it) { BitSet() }.set(p.job.jobNumber) }

        return p.testResults.sortedByDescending { testPriorities[it] }
    }

    internal fun priorities(priorJobs: List<Job>, m: Matrix): Map<String, Double> {
        return m.fileNames().associateWith { similarity(priorJobs, it) }
    }

    private fun similarity(priorJobs: List<Job>, fileName: String): Double {
        return getValue(histories.computeIfAbsent(fileName) { BitSet() }, priorJobs)
    }

    private fun getValue(history: BitSet, priorJobs: List<Job>): Double {
        var prob = 0.0

        for (job in priorJobs) {
            prob = alpha * historyAt(history, job) + (1 - alpha) * prob
        }

        return prob
    }

    private fun historyAt(history: BitSet, job: Job) = if (history[job.jobNumber]) 1.0 else 0.0
}