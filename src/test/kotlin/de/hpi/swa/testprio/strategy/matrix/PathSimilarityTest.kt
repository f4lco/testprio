package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class PathSimilarityTest {

    private lateinit var repository: TestRepository
    private lateinit var strategy: PathSimilarity
    @TempDir lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = PathSimilarity(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun similarPathGetsPromoted() {
        with(repository) {
            loadTestResult("two-failing.csv")
            loadChangedFiles("similar-filenames.csv")
        }

        val result = strategy.apply(Params("3", repository.jobs(), repository))

        expectThat(result).hasTestOrder("tc1", "tc0")
    }
}