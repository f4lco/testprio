package de.hpi.swa.testprio.strategy

import hasTestOrder
import jobWithId
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
        repository.load(fixture())

        val result = strategy.reorder(params(1))

        expectThat(result).hasTestOrder("Car", "ca", "C", "z", "y", "x")
    }

    private fun fixture() = revisions {
        job {
            changedFiles("Car.java")
            successful("C", "ca", "Car")
            successful("z", "y", "x")
        }
    }

    private fun params(id: Int) = Params(jobWithId(id), repository.jobs(), repository)
}