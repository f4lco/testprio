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

class ConditionalProbabilityTest {

    companion object {
        val TESTS = listOf("T1", "T2", "T3")
    }

    lateinit var strategy: ConditionalProbability
    lateinit var repository: TestRepository
    @TempDir
    lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = ConditionalProbability(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun matrixProbabilitiesF1() {
        val probabilities = strategy.probabilities(listOf("F1"), TESTS, Fixtures.matrixOne())

        expectThat(probabilities).isEqualTo(
            mapOf("T1" to 0.05, "T2" to 0.075, "T3" to 0.125)
        )
    }

    @Test
    fun matrixProbabilitiesF2() {
        val probabilities = strategy.probabilities(listOf("F2"), TESTS, Fixtures.matrixOne())

        expectThat(probabilities).isEqualTo(
            mapOf("T1" to 0.5, "T2" to 0.25, "T3" to 0.0)
        )
    }

    @Test
    fun matrixProbabilitiesF1F2() {
        val probabilities = strategy.probabilities(listOf("F1", "F2"), TESTS, Fixtures.matrixOne())

        expectThat(probabilities).isEqualTo(
            mapOf("T1" to 0.55, "T2" to 0.325, "T3" to 0.125)
        )
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repository.load(Fixtures.repeatedFailure())

        val result = strategy.reorder(params("2"))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(jobId: String) = Params(jobId, repository.jobs(), repository)
}