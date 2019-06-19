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
import strikt.assertions.get
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.File

class TestCaseFailureDistributionSimilarityTest {

    private lateinit var strategy: TestCaseFailureDistributionSimilarity
    private lateinit var repository: TestRepository

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = TestCaseFailureDistributionSimilarity(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun testPriorities() {
        val priorities = strategy.priorities(Fixtures.matrixOne(), setOf("T1"))

        expectThat(priorities) {
            get("T1").isEqualTo(22.0)
            get("T2").isNotNull().isEqualTo(12.76, 0.01)
            get("T3").isNotNull().isEqualTo(0.49, 0.01)
        }
    }

    @Test
    fun `TC with similar failure distributions are promoted`() {
        repository.load(Fixtures.similarTests())

        val result = strategy.reorder(Params(jobWithId(4), repository.jobs(), repository))

        expectThat(result).hasTestOrder("C", "A", "B")
    }
}