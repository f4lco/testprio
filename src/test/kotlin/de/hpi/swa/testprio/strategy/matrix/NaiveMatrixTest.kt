package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import jobWithId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.io.File

class NaiveMatrixTest {

    lateinit var strategy: NaiveMatrix
    lateinit var repository: TestRepository

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = NaiveMatrix(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun testMatrixOne() {
        val priority = strategy.priority(Fixtures.matrixOne(), listOf("F1", "F2"))

        expectThat(priority("T1")).isEqualTo(22.0)
        expectThat(priority("T2")).isEqualTo(13.0)
        expectThat(priority("T3")).isEqualTo(5.0)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repository.load(Fixtures.repeatedFailure())

        val result = strategy.reorder(params(2))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(id: Int) = Params(jobWithId(id), repository.jobs(), repository)
}