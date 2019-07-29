package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import java.util.BitSet

/**
 * Exercise test cases based on recently demonstrated fault effectiveness.
 */
class RecentlyFailedStrategy(val alpha: Double) : PrioritizationStrategy {

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
                historyFor(tc).set(p.job.jobNumber)
            }
        }
    }

    private fun historyFor(tc: TestResult) = histories.computeIfAbsent(tc.name) { BitSet() }

    private fun getValue(history: BitSet, p: Params): Double {
        var prob = 0.0

        for (job in p.priorJobs) {
            prob = alpha * historyAt(history, job) + (1 - alpha) * prob
        }

        return prob
    }

    private fun historyAt(history: BitSet, job: Job) = if (history[job.jobNumber]) 1.0 else 0.0
}