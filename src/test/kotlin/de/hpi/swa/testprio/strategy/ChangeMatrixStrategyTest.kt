package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.strategy.matrix.Cache
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy
import de.hpi.swa.testprio.strategy.matrix.CountingReducer
import hasTestOrder
import newTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expectThat
import java.io.File

class ChangeMatrixStrategyTest {

    private val FIRST_JOB = "1"
    private val SECOND_JOB = "2"
    private val JOBS = listOf(FIRST_JOB, SECOND_JOB)

    private val CAR = "src/Car.java"
    private val ACCELERATE = "drive"
    private val BREAK = "light"

    lateinit var strategy: ChangeMatrixStrategy
    lateinit var repository: TestRepository
    @TempDir lateinit var cache: File

    @BeforeEach
    fun setUp() {
        repository = TestRepository()
        strategy = ChangeMatrixStrategy(repository, Cache(cache), CountingReducer)
    }

    @Test
    fun `TC gets promoted due to previous failure with similar file changes`() {
        repository.changedFiles[FIRST_JOB] = listOf(CAR)
        repository.testResults[FIRST_JOB] = listOf(newTestResult(ACCELERATE, failures = 3))

        repository.changedFiles[SECOND_JOB] = listOf(CAR)
        repository.testResults[SECOND_JOB] = listOf(newTestResult(BREAK), newTestResult(ACCELERATE))

        val params = Params(SECOND_JOB, JOBS, repository)

        val result = strategy.apply(params)

        expectThat(result).hasTestOrder(ACCELERATE, BREAK)
    }
}