package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.TestRepository
import de.hpi.swa.testprio.strategy.Params
import hasTestOrder
import jobWithId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.isEqualTo
import java.io.File

class FileFailureDistributionSimilarityTest {

    lateinit var strategy: FileFailureDistributionSimilarity
    lateinit var repository: TestRepository

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = FileFailureDistributionSimilarity(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun testPriorities() {
        val priorities = strategy.priorities(listOf("F1"), listOf("T1", "T2", "T3"), Fixtures.matrixOne())

        expectThat(priorities) {
            get("T1").isEqualTo(2.0)
            get("T2").isEqualTo(3.0)
            get("T3").isEqualTo(5.0)
        }
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repository.load(Fixtures.repeatedFailure())

        val result = strategy.reorder(params(2))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(id: Int) = Params(jobWithId(id), repository.jobs(), repository)
}