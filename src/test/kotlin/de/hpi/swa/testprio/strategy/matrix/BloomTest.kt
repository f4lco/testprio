package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class BloomTest {
    lateinit var strategy: Bloom
    lateinit var repository: TestRepository
    @TempDir
    lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = Bloom(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repository.load(Fixtures.repeatedFailure())

        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}