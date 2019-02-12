package de.hpi.swa.testprio.strategy

import hasTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class RecentlyFailedStrategyTest {

    private val JOBS = listOf("A", "B")

    lateinit var strategy: RecentlyFailedStrategy
    lateinit var repository: TestRepository

    @BeforeEach
    fun setUp() {
        strategy = RecentlyFailedStrategy(alpha = 0.8)
        repository = TestRepository()
    }

    @Test
    fun `first iteration leaves order unchanged`() {
        repository.testResults["A"] = listOf(newTestResult("tc0", failures = 0),
                newTestResult("tc1", failures = 5))

        val job1 = Params("A", JOBS, repository)

        val result = strategy.apply(job1)

        expectThat(result).hasTestOrder("tc0", "tc1")
    }

    @Test
    fun `second iteration promotes failure from previous run`() {
        repository.testResults["A"] = listOf(newTestResult("tc0", failures = 0), newTestResult("tc1", failures = 5))
        val job1 = Params("A", JOBS, repository)

        repository.testResults["B"] = listOf("tc0", "tc1").map { newTestResult(it) }
        val job2 = Params("B", JOBS, repository)

        strategy.apply(job1)
        val result = strategy.apply(job2)

        expectThat(result).hasTestOrder("tc1", "tc0")
    }
}
