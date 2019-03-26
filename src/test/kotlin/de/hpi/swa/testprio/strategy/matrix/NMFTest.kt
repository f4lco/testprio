package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class NMFTest {

    private lateinit var strategy: NMF
    private lateinit var repository: TestRepository

    @BeforeEach
    fun setUp(@TempDir cache: File) {
        repository = TestRepository()
        strategy = NMF(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun nmf() {
        with(repository) {
            loadTestResult("similar-tc.csv")
            loadChangedFiles("files-ascending.csv")
        }

        val result = strategy.apply(Params("4", repository.jobs(), repository))

        expectThat(result).hasTestOrder("C", "B", "A")
    }
}