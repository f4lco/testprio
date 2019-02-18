package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class NaiveMatrixTest {

    lateinit var strategy: NaiveMatrix
    lateinit var repository: TestRepository
    @TempDir lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = NaiveMatrix(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        with(repository) {
            loadTestResult("repeated-failure.csv")
            loadChangedFiles("repeated-change.csv")
        }

        val result = strategy.apply(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobIds(), repository)
}