package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import de.hpi.swa.testprio.strategy.revisions
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.io.File

class RecentlyChangedTest {

    lateinit var strategy: RecentlyChanged
    lateinit var repository: TestRepository
    @TempDir
    lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = RecentlyChanged(repository, Cache(cache), CountingReducer, alpha = 0.8)
    }

    @Test
    fun testPriorities() {
        repository.load(changeFileOneThenTwo())
        strategy.acceptFailedRun(Params("1", repository.jobs(), repository))
        strategy.acceptFailedRun(Params("2", repository.jobs(), repository))

        val priorities = strategy.priorities(3, Fixtures.matrixOne())

        expectThat(priorities) {
            get("F1").isNotNull().isEqualTo(0.04, 0.01)
            get("F2").isNotNull().isEqualTo(0.16, 0.01)
        }
    }

    private fun changeFileOneThenTwo() = revisions {
        job { changedFiles("F1") }

        job { changedFiles("F2") }
    }

    @Test
    @DisplayName("Promote test due to recently changed files")
    fun recentlyChanged() {
        repository.load(Fixtures.repeatedFailure())
        strategy.acceptFailedRun(Params("1", repository.jobs(), repository))

        val result = strategy.reorder(Params("2", repository.jobs(), repository))

        expectThat(result).hasTestOrder("T2", "T1")
    }
}