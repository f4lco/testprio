package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class TestCaseFailureDistributionSimilarityTest {

    private lateinit var strategy: TestCaseFailureDistributionSimilarity
    private lateinit var repository: TestRepository

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = TestCaseFailureDistributionSimilarity(repository, Cache(cache), 0.8, CountingReducer)
    }

    @Test
    fun `TC with similar failure distributions are promoted`() {
        with(repository) {
            loadTestResult("similar-tc.csv")
            loadChangedFiles("files-ascending.csv")
        }

        val result = strategy.reorder(Params("4", repository.jobs(), repository))

        expectThat(result).hasTestOrder("C", "B", "A")
    }
}