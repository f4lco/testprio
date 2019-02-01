package de.hpi.swa.testprio.strategy

import assertTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LeastRecentlyUsedStrategyTest {

    private lateinit var strategy: LeastRecentlyUsedStrategy

    @BeforeEach
    fun setUp() {
        strategy = LeastRecentlyUsedStrategy()
    }

    @Test
    fun `first iteration leaves ordered unchanged`() {
        val job1 = TestParams("A")
        job1.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        val reordered = strategy.apply(job1)

        assertTestOrder(reordered, "tc0", "tc1")
    }

    @Test
    fun `second iteration demotes the first test of the prior run`() {
        val job1 = TestParams("A")
        job1.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        val job2 = TestParams("B")
        job2.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        strategy.apply(job1)
        val reordered = strategy.apply(job2)

        assertTestOrder(reordered, "tc1", "tc0")
    }

    @Test
    fun `added test cases are executed first`() {
        val job1 = TestParams("A")
        job1.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        val job2 = TestParams("B")
        job2.testResults = listOf("tc0", "tc1", "addedTC").map { newTestResult(it) }

        strategy.apply(job1)
        val reordered = strategy.apply(job2)

        assertTestOrder(reordered, "addedTC", "tc1", "tc0")
    }

    @Test
    fun `removed test cases are ignored`() {
        val job1 = TestParams("A")
        job1.testResults = listOf("tc0", "tc1", "removedTC").map { newTestResult(it) }

        val job2 = TestParams("B")
        job2.testResults = listOf("tc0", "tc1").map { newTestResult(it) }

        strategy.apply(job1)
        val reordered = strategy.apply(job2)

        assertTestOrder(reordered, "tc1", "tc0")
    }
}
