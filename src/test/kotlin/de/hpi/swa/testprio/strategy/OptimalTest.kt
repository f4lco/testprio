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
        repository.loadTestResult("repeated-failure.csv")

        val result = Optimal.byFailureCount().apply(params(job))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isGreaterThan(0)
            get(1).get { red }.isEqualTo(0)
        }
    }

    @Test
    fun failuresPerDurationUnchanged() {
        repository.loadTestResult("failures-duration.csv")

        val result = Optimal.byFailuresPerDuration().apply(params("1"))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isEqualTo(10)
            get(1).get { red }.isEqualTo(5)
        }
    }

    @Test
    fun failuresPerDurationChanged() {
        repository.loadTestResult("failures-duration.csv")

        val result = Optimal.byFailuresPerDuration().apply(params("2"))

        expectThat(result) {
            hasSize(2)
            get(0).get { red }.isEqualTo(5)
            get(1).get { red }.isEqualTo(10)
        }
    }

    private fun params(job: String) = Params(job, repository.jobs(), repository)
}