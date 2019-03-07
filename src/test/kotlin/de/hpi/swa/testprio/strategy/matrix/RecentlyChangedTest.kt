package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
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
    @DisplayName("Promote test due to recently changed files")
    fun recentlyChanged() {
        with(repository) {
            loadChangedFiles("repeated-change.csv")
            loadTestResult("repeated-failure.csv")
        }

        strategy.apply(Params("1", repository.jobs(), repository))

        val result = strategy.apply(Params("2", repository.jobs(), repository))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }
}