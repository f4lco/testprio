package de.hpi.swa.testprio.strategy

import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class PathTestCaseOverlapTest {

    private lateinit var repository: TestRepository
    private lateinit var strategy: PathTestCaseOverlap

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = PathTestCaseOverlap()
    }

    @Test
    fun `TC names with overlap to changed files are promoted`() {
        with(repository) {
            loadTestResult("cars-results.csv")
            loadChangedFiles("cars-files.csv")
        }

        val result = strategy.reorder(Params("1", repository.jobs(), repository))

        expectThat(result).hasTestOrder("Car", "ca", "C", "z", "y", "x")
    }
}