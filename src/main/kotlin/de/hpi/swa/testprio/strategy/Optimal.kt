package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import java.math.BigDecimal

/**
 * Optimal strategies according to some criterion.
 */
class Optimal private constructor(private val comparator: Comparator<TestResult>) : PrioritisationStrategy {

    companion object {

        private val TINY_DURATION = BigDecimal.valueOf(0.25)

        /**
         * Reorder by best fault detection per test executed ratio.
         */
        fun byFailureCount() = Optimal(compareByDescending { it.red / it.count.toDouble() })

        /**
         * Reorder by best fault detection per time ratio.
         *
         * If the test runner reported "0 sec" as duration, assume that rounding took place, and the test took
         * in between (0, 0.5) seconds (both interval bounds excluded). Further assume that the test durations
         * are evenly distributed, such that 0.25 sec is a reasonable approximation of duration of those tests.
         */
        fun byFailuresPerDuration() = Optimal(compareByDescending {
            val red = it.red.toBigDecimal()
            val duration = if (it.duration.signum() == 0) TINY_DURATION else it.duration
            red / duration
        })
    }

    override fun apply(p: Params): List<TestResult> {
        return p.testResults.sortedWith(comparator)
    }
}