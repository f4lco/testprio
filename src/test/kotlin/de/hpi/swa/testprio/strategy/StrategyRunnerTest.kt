package de.hpi.swa.testprio.strategy

import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.argumentCaptor
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.probe.Repository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import strikt.api.Assertion
import strikt.api.expectThat
import java.io.File

@ExtendWith(MockitoExtension::class)
class StrategyRunnerTest {

    companion object {
        const val PROJECT = "testproject"

        const val BUILD_A = 123
        const val BUILD_B = 456

        val JOB_A1 = Job(buildNumber = 444, build = BUILD_A, job = 1)
        val JOB_A2 = Job(buildNumber = 444, build = BUILD_A, job = 2)
        val JOB_B1 = Job(buildNumber = 555, build = BUILD_B, job = 3)
        val JOB_B2 = Job(buildNumber = 555, build = BUILD_B, job = 4)
    }

    interface CloseableStrategy : PrioritisationStrategy, AutoCloseable

    private lateinit var runner: StrategyRunner

    private lateinit var output: File
    @Mock
    private lateinit var repository: Repository
    @Mock
    private lateinit var strategy: PrioritisationStrategy

    @BeforeEach
    fun setUp(@TempDir outputDirectory: File) {
        runner = StrategyRunner(repository)
        output = outputDirectory.resolve("output.csv")
    }

    @Test
    fun noJob() {
        runner.run(PROJECT, strategy, output)

        verifyZeroInteractions(strategy)
    }

    @Test
    fun twoBuildsOneJobEach() {
        given { repository.redJobs(PROJECT) }.willReturn(listOf(JOB_A1, JOB_B1))

        runner.run(PROJECT, strategy, output)

        inOrder(strategy) {

            argumentCaptor<Params> {
                verify(strategy).reorder(capture())
                expectThat(lastValue).has(job = 1, priorJobs = listOf(1))

                verify(strategy).acceptFailedRun(capture())
                expectThat(lastValue).has(job = 1, priorJobs = listOf(1))
            }

            argumentCaptor<Params> {
                verify(strategy).reorder(capture())
                expectThat(lastValue).has(job = 3, priorJobs = listOf(1, 3))

                verify(strategy).acceptFailedRun(capture())
                expectThat(lastValue).has(job = 3, priorJobs = listOf(1, 3))
            }
        }
    }

    @Test
    fun twoBuildsTwoJobsEach() {
        given { repository.redJobs(PROJECT) }.willReturn(listOf(JOB_A1, JOB_A2, JOB_B1, JOB_B2))

        runner.run(PROJECT, strategy, output)

        inOrder(strategy) {

            argumentCaptor<Params> {
                verify(strategy, times(2)).reorder(capture())
                expectThat(firstValue).has(job = 1, priorJobs = listOf(1))
                expectThat(secondValue).has(job = 2, priorJobs = listOf(2))
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).acceptFailedRun(capture())
                expectThat(firstValue).has(job = 1, priorJobs = listOf(1))
                expectThat(secondValue).has(job = 2, priorJobs = listOf(2))
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).reorder(capture())
                expectThat(firstValue).has(job = 3, priorJobs = listOf(1, 2, 3))
                expectThat(secondValue).has(job = 4, priorJobs = listOf(1, 2, 4))
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).acceptFailedRun(capture())
                expectThat(firstValue).has(job = 3, priorJobs = listOf(1, 2, 3))
                expectThat(secondValue).has(job = 4, priorJobs = listOf(1, 2, 4))
            }
        }
    }

    @Test
    fun strategyIsClosed() {
        val strategy = mock<CloseableStrategy>()

        runner.run(PROJECT, strategy, output)

        verify(strategy).close()
    }

    fun Assertion.Builder<Params>.has(job: Int, priorJobs: List<Int>) {
        assert("has job ID", job) { actual ->
            if (actual.jobId == job.toString()) pass()
            else fail(actual = actual.jobId)
        }

        assert("has prior jobs (including current)") { actual ->
            if (actual.jobIds == priorJobs.map { it.toString() }) pass()
            else fail(actual = actual.jobIds)
        }
    }
}