package de.hpi.swa.testprio.strategy

import hasTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class LeastRecentlyUsedStrategyTest {

    private lateinit var strategy: LeastRecentlyUsedStrategy
    private lateinit var repository: TestRepository

    @BeforeEach
    fun setUp() {
        strategy = LeastRecentlyUsedStrategy()
        repository = TestRepository()
    }

    @Test
    fun `first iteration leaves ordered unchanged`() {
        repository.testResults["A"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job1 = Params("A", listOf(), repository)

        val result = strategy.apply(job1)

        expectThat(result).hasTestOrder("tc0", "tc1")
    }

    @Test
    fun `second iteration demotes the first test of the prior run`() {
        repository.testResults["A"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job1 = Params("A", emptyList(), repository)

        repository.testResults["B"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job2 = Params("B", emptyList(), repository)

        strategy.apply(job1)
        val result = strategy.apply(job2)

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    @Test
    fun `added test cases are executed first`() {
        repository.testResults["A"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job1 = Params("A", emptyList(), repository)

        repository.testResults["B"] = listOf("tc0", "tc1", "addedTC").map { newTestResult(it) }
        val job2 = Params("B", emptyList(), repository)

        strategy.apply(job1)
        val result = strategy.apply(job2)

        expectThat(result).hasTestOrder("addedTC", "tc1", "tc0")
    }

    @Test
    fun `removed test cases are ignored`() {
        repository.testResults["A"] = listOf("tc0", "tc1", "removedTC").map { newTestResult(it) }
        val job1 = Params("A", emptyList(), repository)

        repository.testResults["B"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job2 = Params("B", emptyList(), repository)

        strategy.apply(job1)
        val result = strategy.apply(job2)

        expectThat(result).hasTestOrder("tc1", "tc0")
    }
}
