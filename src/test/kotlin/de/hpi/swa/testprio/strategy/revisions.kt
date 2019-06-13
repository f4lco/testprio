package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import java.util.concurrent.atomic.AtomicInteger

data class Revision(
    val job: Job,
    val changedFiles: List<String>,
    val testResults: List<TestResult>
)

fun revisions(body: RevisionBuilder.() -> Unit): List<Revision> {
    val builder = RevisionBuilder()
    body(builder)
    return builder.revisions
}

class RevisionBuilder {

    private val counter = AtomicInteger()
    val revisions = mutableListOf<Revision>()

    operator fun invoke(body: RevisionBuilder.() -> Unit) {
        body()
    }

    fun job(builder: JobBuilder.() -> Unit) {
        val jobBuilder = JobBuilder()
        builder(jobBuilder)
        revisions.add(Revision(nextJob(), jobBuilder.changedFiles, jobBuilder.testResults))
    }

    private fun nextJob(): Job {
        val id = counter.incrementAndGet()
        return Job(id, id, id)
    }
}

class JobBuilder(
    val changedFiles: MutableList<String> = mutableListOf(),
    private val tests: MutableList<TestResult> = mutableListOf()
) {

    val testResults: List<TestResult> get() = tests.mapIndexed { index, tc -> tc.copy(index = index) }

    fun changedFiles(vararg names: String) = changedFiles.addAll(names)

    fun successful(vararg names: String) = tests.addAll(names.map(TestResult.Companion::successful))

    fun failed(vararg names: String) = tests.addAll(names.map(TestResult.Companion::failed))

    fun failed(name: String, failures: Int, duration: Double = DEFAULT_DURATION) {
        tests.add(TestResult.failed(name).copy(
            failures = failures,
            errors = 0,
            duration = duration.toBigDecimal()))
    }
}
