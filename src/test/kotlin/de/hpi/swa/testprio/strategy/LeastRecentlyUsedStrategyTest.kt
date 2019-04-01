package de.hpi.swa.testprio.strategy

import hasTestOrder
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
        repository.loadTestResult("repeated-failure.csv")
        val result = strategy.reorder(params("1"))

        expectThat(result).hasTestOrder("tc0", "tc1")
    }

    @Test
    fun `second iteration demotes the first test of the prior run`() {
        repository.loadTestResult("repeated-failure.csv")

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    @Test
    fun `added test cases are executed first`() {
        repository.loadTestResult("added-tc.csv")

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("addedTC", "tc1", "tc0")
    }

    @Test
    fun `removed test cases are ignored`() {
        repository.loadTestResult("removed-tc.csv")

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}
