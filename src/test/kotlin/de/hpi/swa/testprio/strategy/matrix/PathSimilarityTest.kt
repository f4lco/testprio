package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.Fixtures
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.TestRepository
import de.hpi.swa.testprio.strategy.revisions
import hasTestOrder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import strikt.assertions.get
import strikt.assertions.isEqualTo
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
    fun testPriorities() {
        val priorities = strategy.priorities(listOf("F1"), Fixtures.matrixOne())

        expectThat(priorities) {
            get("F1").isEqualTo(2.0)
            get("F2").isEqualTo(1.0)
        }
    }

    @Test
    fun similarPathGetsPromoted() {
        repository.load(twoFailing())

        val result = strategy.reorder(Params("3", repository.jobs(), repository))

        expectThat(result).hasTestOrder("T1", "T2")
    }

    private fun twoFailing() = revisions {

        job {
            changedFiles("/my/Car.java")
            failed("T1", "T2")
        }

        job {
            changedFiles("/path/to/my/Car.java")
            failed("T1", "T2")
        }

        job {
            changedFiles("/my/Car.java", "/path/to/my/Car.java")
            failed("T1", "T2")
        }
    }
}