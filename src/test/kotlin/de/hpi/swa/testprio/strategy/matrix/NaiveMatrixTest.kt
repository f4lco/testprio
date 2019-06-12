package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
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
    fun testMatrixOne() {
        val priority = strategy.priority(Fixtures.matrixOne(), listOf("F1", "F2"))

        expectThat(priority("T1")).isEqualTo(22)
        expectThat(priority("T2")).isEqualTo(13)
        expectThat(priority("T3")).isEqualTo(5)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        with(repository) {
            loadTestResult("repeated-failure.csv")
            loadChangedFiles("repeated-change.csv")
        }

        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}