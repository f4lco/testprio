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
import strikt.assertions.get
import java.io.File
import java.time.Duration
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class StrategyRunnerTest {

    companion object {
        const val PROJECT = "testproject"

        const val BUILD_A = 123
        const val BUILD_B = 456
        const val BUILD_C = 789
        const val BUILD_D = 790

        val t0 = Instant.now()
        val t5 = t0 + Duration.ofSeconds(5)
        val t10 = t0 + Duration.ofSeconds(10)
        val t15 = t0 + Duration.ofSeconds(15)
        val t20 = t0 + Duration.ofSeconds(20)

        /* A, B: two consecutive builds */
        val JOB_A1 = Job(buildNumber = 444, build = BUILD_A, jobNumber = 45, job = 1, begin = t0, end = t10)
        val JOB_A2 = Job(buildNumber = 444, build = BUILD_A, jobNumber = 46, job = 2, begin = t0, end = t10)

        val JOB_B1 = Job(buildNumber = 555, build = BUILD_B, jobNumber = 47, job = 3, begin = t10, end = t20)
        val JOB_B2 = Job(buildNumber = 555, build = BUILD_B, jobNumber = 48, job = 4, begin = t10, end = t20)

        /* C, D: two interleaved builds */
        val JOB_C1 = Job(buildNumber = 666, build = BUILD_C, jobNumber = 49, job = 5, begin = t5, end = t10)
        val JOB_C2 = Job(buildNumber = 666, build = BUILD_C, jobNumber = 50, job = 6, begin = t0, end = t15)

        val JOB_D1 = Job(buildNumber = 777, build = BUILD_D, jobNumber = 51, job = 7, begin = t10, end = t20)
        val JOB_D2 = Job(buildNumber = 777, build = BUILD_D, jobNumber = 52, job = 8, begin = t15, end = t20)
    }

    interface CloseableStrategy : PrioritizationStrategy, AutoCloseable

    private lateinit var runner: StrategyRunner

    private lateinit var output: File
    @Mock
    private lateinit var repository: Repository
    @Mock
    private lateinit var strategy: PrioritizationStrategy

    @BeforeEach
    fun setUp(@TempDir outputDirectory: File) {
        runner = StrategyRunner(repository)
        output = outputDirectory.resolve("output.csv")
    }

    @Test
    fun noJob() {
        runner.run(PROJECT, strategy, null, output)

        verifyZeroInteractions(strategy)
    }

    @Test
    fun twoBuildsOneJobEach() {
        given { repository.redJobs(PROJECT) }.willReturn(listOf(JOB_A1, JOB_B1))

        runner.run(PROJECT, strategy, null, output)

        inOrder(strategy) {

            argumentCaptor<Params> {
                verify(strategy).reorder(capture())
                expectThat(lastValue).has(job = 1, priorJobs = emptyList())

                verify(strategy).acceptFailedRun(capture())
                expectThat(lastValue).has(job = 1, priorJobs = emptyList())
            }

            argumentCaptor<Params> {
                verify(strategy).reorder(capture())
                expectThat(lastValue).has(job = 3, priorJobs = listOf(1))

                verify(strategy).acceptFailedRun(capture())
                expectThat(lastValue).has(job = 3, priorJobs = listOf(1))
            }
        }
    }

    @Test
    fun twoBuildsTwoJobsEach() {
        given { repository.redJobs(PROJECT) }.willReturn(listOf(JOB_A1, JOB_A2, JOB_B1, JOB_B2))

        runner.run(PROJECT, strategy, null, output)

        inOrder(strategy) {

            argumentCaptor<Params> {
                verify(strategy, times(2)).reorder(capture())
                expectThat(allValues) {
                    get(0).has(job = 1, priorJobs = emptyList())
                    get(1).has(job = 2, priorJobs = emptyList())
                }
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).acceptFailedRun(capture())
                expectThat(allValues) {
                    get(0).has(job = 1, priorJobs = emptyList())
                    get(1).has(job = 2, priorJobs = emptyList())
                }
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).reorder(capture())
                expectThat(firstValue).has(job = 3, priorJobs = listOf(1, 2))
                expectThat(secondValue).has(job = 4, priorJobs = listOf(1, 2))
            }

            argumentCaptor<Params> {
                verify(strategy, times(2)).acceptFailedRun(capture())
                expectThat(firstValue).has(job = 3, priorJobs = listOf(1, 2))
                expectThat(secondValue).has(job = 4, priorJobs = listOf(1, 2))
            }
        }
    }

    @Test
    fun deferredLearningOfBuild() {
        given { repository.redJobs(PROJECT) }.willReturn(listOf(JOB_C1, JOB_C2, JOB_D1, JOB_D2))

        runner.run(PROJECT, strategy, null, output)

        inOrder(strategy) {

            argumentCaptor<Params> {
                verify(strategy, times(4)).reorder(capture())
                expectThat(allValues) {
                    get(0).has(job = 5, priorJobs = emptyList())
                    get(1).has(job = 6, priorJobs = emptyList())
                    get(2).has(job = 7, priorJobs = emptyList())
                    get(3).has(job = 8, priorJobs = emptyList())
                }
            }

            argumentCaptor<Params> {
                verify(strategy, times(4)).acceptFailedRun(capture())
                expectThat(allValues) {
                    get(0).has(job = 5, priorJobs = emptyList())
                    get(1).has(job = 6, priorJobs = emptyList())
                    get(2).has(job = 7, priorJobs = emptyList())
                    get(3).has(job = 8, priorJobs = emptyList())
                }
            }
        }
    }

    @Test
    fun strategyIsClosed() {
        val strategy = mock<CloseableStrategy>()

        runner.run(PROJECT, strategy, null, output)

        verify(strategy).close()
    }

    fun Assertion.Builder<Params>.has(job: Int, priorJobs: List<Int>) {
        assert("has job ID", job) { actual ->
            if (actual.job.job == job) pass()
            else fail(actual = actual.job.job)
        }

        assert("has prior jobs") { actual ->
            if (actual.priorJobs.map { it.job } == priorJobs) pass()
            else fail(actual = actual.priorJobs)
        }
    }
}