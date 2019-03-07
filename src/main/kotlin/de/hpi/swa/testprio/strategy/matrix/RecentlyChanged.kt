package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import java.util.BitSet

/**
 * Take "hot" and "cold" files into account during matrix prioritisation.
 *
 * Assign weights to file failure distributions, such that tests connected
 * to recently changed files are ranked higher.
 */
class RecentlyChanged(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val alpha: Double
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)
    private val history = mutableMapOf<String, BitSet>()

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val filePriorities = sumMatrix.fileNames().associateWith { similarity(p, it) }

        val testPriorities = p.testResults.associateWith { tc ->
            sumMatrix.matrix
                .filterKeys { it.testName == tc.name }
                .map { filePriorities.getOrDefault(it.key.fileName, 0.0) * it.value.toDouble() }
                .sum()
        }

        p.changedFiles.forEach { history.computeIfAbsent(it) { BitSet() }.set(p.jobIndex) }
        return p.testResults.sortedByDescending { testPriorities[it] }
    }

    private fun selectJobs(p: Params): List<String> = p.jobIds.subList(0, p.jobIndex)

    private fun similarity(p: Params, fileName: String): Double {
        return getValue(history.computeIfAbsent(fileName) { BitSet() }, p)
    }

    private fun getValue(history: BitSet, p: Params): Double {
        var prob: Double = historyAt(history, 0)
        var currentIndex = 1

        while (currentIndex < p.jobIndex) {
            prob = alpha * historyAt(history, currentIndex) + (1 - alpha) * prob
            currentIndex += 1
        }

        return prob
    }

    private fun historyAt(history: BitSet, at: Int) = if (history[at]) 1.0 else 0.0
}