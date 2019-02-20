package de.hpi.swa.testprio.strategy

import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class RecentlyFailedStrategyTest {

    lateinit var strategy: RecentlyFailedStrategy
    lateinit var repository: TestRepository

    @BeforeEach
    fun setUp() {
        strategy = RecentlyFailedStrategy(alpha = 0.8)
        repository = TestRepository()
    }

    @Test
    fun `first iteration leaves order unchanged`() {
        repository.loadTestResult("repeated-failure.csv")

        val result = strategy.apply(params("1"))

        expectThat(result).hasTestOrder("tc0", "tc1")
    }

    @Test
    fun `second iteration promotes failure from previous run`() {
        repository.loadTestResult("repeated-failure.csv")

        strategy.apply(params("1"))
        val result = strategy.apply(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}
