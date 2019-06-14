package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import de.hpi.swa.testprio.strategy.revisions
import hasTestOrder
import jobWithId
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

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = RecentlyChanged(repository, Cache(cache), CountingReducer, alpha = 0.8)
    }

    @Test
    fun testPriorities() {
        repository.load(changeFileOneThenTwo())
        strategy.acceptFailedRun(params(1))
        strategy.acceptFailedRun(params(2))

        val priorities = strategy.priorities(repository.jobs(), Fixtures.matrixOne())

        expectThat(priorities) {
            get("F1").isNotNull().isEqualTo(0.15, 0.01)
            get("F2").isEqualTo(0.8)
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
        strategy.acceptFailedRun(params(1))

        val result = strategy.reorder(params(2))

        expectThat(result).hasTestOrder("T2", "T1")
    }

    private fun params(id: Int) = Params(jobWithId(id), repository.jobs(), repository)
}