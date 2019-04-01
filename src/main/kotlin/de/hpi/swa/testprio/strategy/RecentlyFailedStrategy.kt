package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import java.util.BitSet

/**
 * Exercise test cases based on recently demonstrated fault effectiveness.
 */
class RecentlyFailedStrategy(val alpha: Double) : PrioritisationStrategy {

    private val histories = mutableMapOf<String, BitSet>()

    override fun reorder(p: Params): List<TestResult> {

        val priorities = mutableMapOf<String, Double>()

        for (tc in p.testResults) {
            val history = historyFor(tc)
            priorities[tc.name] = getValue(history, p)
        }

        return p.testResults.sortedByDescending { priorities[it.name] }
    }

    override fun acceptFailedRun(p: Params) {
        for (tc in p.testResults) {
            if (tc.red > 0) {
                historyFor(tc).set(p.jobIndex)
            }
        }
    }

    private fun historyFor(tc: TestResult) = histories.computeIfAbsent(tc.name) { BitSet() }

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