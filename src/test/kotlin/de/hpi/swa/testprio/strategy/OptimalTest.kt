package de.hpi.swa.testprio.strategy

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.get
import strikt.assertions.isGreaterThan
import strikt.assertions.isEqualTo

class OptimalTest {

    private lateinit var repository: TestRepository

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "2"])
    fun failures(job: String) {
        repository.load(Fixtures.repeatedFailure())

        val result = Optimal.byFailureCount().reorder(params(job))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isGreaterThan(0)
            get(1).get { red }.isEqualTo(0)
        }
    }

    @Test
    fun failuresPerDurationUnchanged() {
        repository.load(failuresWithDuration())

        val result = Optimal.byFailuresPerDuration().reorder(params("1"))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isEqualTo(10)
            get(1).get { red }.isEqualTo(5)
        }
    }

    @Test
    fun failuresPerDurationChanged() {
        repository.load(failuresWithDuration())

        val result = Optimal.byFailuresPerDuration().reorder(params("2"))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isEqualTo(5)
            get(1).get { red }.isEqualTo(10)
        }
    }

    private fun failuresWithDuration() = revisions {
        job {
            failed("tc0", failures = 5, duration = 1.0)
            failed("tc1", failures = 10, duration = 1.0)
        }

        job {
            failed("tc1", failures = 10, duration = 10.0)
            failed("tc0", failures = 5, duration = 2.5)
        }
    }

    private fun params(job: String) = Params(job, repository.jobs(), repository)
}