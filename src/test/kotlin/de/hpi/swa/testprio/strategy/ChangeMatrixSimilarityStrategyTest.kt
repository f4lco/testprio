package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.strategy.matrix.Cache
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixSimilarityStrategy
import de.hpi.swa.testprio.strategy.matrix.CountingReducer
import hasTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class ChangeMatrixSimilarityStrategyTest {

    private val FIRST_JOB = "1"
    private val SECOND_JOB = "2"
    private val JOBS = listOf(FIRST_JOB, SECOND_JOB)

    private val CAR = "src/Car.java"
    private val ACCELERATE = "drive"
    private val BREAK = "light"

    lateinit var strategy: ChangeMatrixSimilarityStrategy
    lateinit var repo: TestRepository
    @TempDir lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repo = TestRepository()
        strategy = ChangeMatrixSimilarityStrategy(repo, Cache(cache), CountingReducer)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repo.changedFiles[FIRST_JOB] = listOf(CAR)
        repo.testResults[FIRST_JOB] = listOf(newTestResult(ACCELERATE, failures = 3))

        val testResults = listOf(newTestResult(BREAK), newTestResult(ACCELERATE))
        val params = TestParams(SECOND_JOB,
                testResults = testResults,
                changedFiles = listOf(CAR),
                jobIds = JOBS)

        val result = strategy.apply(params)

        expectThat(result).hasTestOrder(ACCELERATE, BREAK)
    }
}