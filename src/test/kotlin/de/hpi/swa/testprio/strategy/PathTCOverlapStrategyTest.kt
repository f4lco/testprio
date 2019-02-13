package de.hpi.swa.testprio.strategy

import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat

class PathTCOverlapStrategyTest {

    private lateinit var repository: TestRepository
    private lateinit var strategy: PathTCOverlapStrategy

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = PathTCOverlapStrategy()
    }

    @Test
    fun `TC names with overlap to changed files are promoted`() {
        with(repository) {
            loadTestResult("cars-results.csv")
            loadChangedFiles("cars-files.csv")
        }

        val result = strategy.apply(Params("1", repository.jobIds(), repository))

        expectThat(result).hasTestOrder("Car", "ca", "C", "z", "y", "x")
    }
}