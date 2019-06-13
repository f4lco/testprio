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
        repository.load(Fixtures.repeatedFailure())
        val result = strategy.reorder(params("1"))

        expectThat(result).hasTestOrder("T1", "T2")
    }

    @Test
    fun `second iteration demotes the first test of the prior run`() {
        repository.load(Fixtures.repeatedFailure())

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    @Test
    fun `added test cases are executed first`() {
        repository.load(addedTestCase())

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("addedTC", "tc1", "tc0")
    }

    private fun addedTestCase() = revisions {
        job {
            successful("tc0")
            failed("tc1")
        }

        job {
            successful("tc0")
            failed("tc1")
            successful("addedTC")
        }
    }

    @Test
    fun `removed test cases are ignored`() {
        repository.load(removedTestCase())

        strategy.reorder(params("1"))
        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    private fun removedTestCase() = revisions {
        job {
            successful("tc0")
            failed("tc1")
            successful("removedTC")
        }

        job {
            successful("tc0")
            failed("tc1")
        }
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}
