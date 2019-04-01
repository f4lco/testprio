package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult

/**
 * Cycle tests in round-robin fashion.
 *
 * Newly inserted test cases are executed first.
 */
class LeastRecentlyUsedStrategy : PrioritisationStrategy {

    private val lastOrder = mutableMapOf<String, Int>()

    override fun reorder(p: Params): List<TestResult> {
        val (kept, added) = p.testResults.partition { it.name in lastOrder.keys }

        val newOrder = mutableMapOf<String, Int>()

        for ((index, tc) in added.withIndex()) {
            newOrder[tc.name] = index
        }

        if (kept.isNotEmpty()) {
            val shifted = kept.sortedBy { lastOrder[it.name] }.toMutableList()
            shifted.add(shifted.removeAt(0))

            for ((index, tc) in shifted.withIndex()) {
                newOrder[tc.name] = added.size + index
            }
        }

        lastOrder.clear()
        lastOrder.putAll(newOrder)

        return p.testResults.sortedBy { lastOrder[it.name] }
    }
}