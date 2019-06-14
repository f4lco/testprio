package de.hpi.swa.testprio.strategy

import hasTestOrder
import jobWithId
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
        repository.load(Fixtures.repeatedFailure())

        val result = strategy.reorder(params(1))

        expectThat(result).hasTestOrder("T1", "T2")
    }

    @Test
    fun `second iteration promotes failure from previous run`() {
        repository.load(Fixtures.repeatedFailure())

        strategy.acceptFailedRun(params(1))
        val result = strategy.reorder(params(2))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(id: Int) = Params(jobWithId(id), repository.jobs(), repository)
}
