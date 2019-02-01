package de.hpi.swa.testprio.strategy

import assertTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RecentlyFailedStrategyTest {

    lateinit var strategy: RecentlyFailedStrategy

    @BeforeEach
    fun setUp() {
        strategy = RecentlyFailedStrategy(alpha = 0.8)
    }

    @Test
    fun `first iteration leaves order unchanged`() {
        val job1 = TestParams("A")
        job1.testResults += newTestResult("tc0", failures = 0)
        job1.testResults += newTestResult("tc1", failures = 5)

        val reordered = strategy.apply(job1)

        assertTestOrder(reordered, "tc0", "tc1")
    }

    @Test
    fun `second iteration promotes failure from previous run`() {
        val job1 = TestParams("A")
        job1.testResults += newTestResult("tc0", failures = 0)
        job1.testResults += newTestResult("tc1", failures = 5)

        val job2 = TestParams("B", jobIndex = 1)
        job2.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        strategy.apply(job1)
        val reordered = strategy.apply(job2)

        assertTestOrder(reordered, "tc1", "tc0")
    }
}
