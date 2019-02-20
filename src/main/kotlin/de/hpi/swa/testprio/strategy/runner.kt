package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.CsvOutput
import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import me.tongfei.progressbar.ProgressBar
import java.io.File

class Params(
    val jobId: String,
    val jobIds: List<String>,
    val repository: Repository
) {

    val changedFiles: List<String> by lazy { repository.changedFiles(jobId) }

    val testResults: List<TestResult> by lazy { repository.testResults(jobId) }

    val jobIndex by lazy { jobIds.indexOf(jobId) }
}

interface PrioritisationStrategy {

    fun apply(p: Params): List<TestResult>
}

enum class JobSpec {
    ALL,
    ONLY_TEST_FAILURES,
    ONLY_FIRST_TEST_FAILURES,
}

class StrategyRunner(val repository: Repository) {

    fun run(
        projectName: String,
        jobSpec: JobSpec,
        strategy: PrioritisationStrategy,
        output: File
    ) {

        val jobIds = getJobs(projectName, jobSpec)
        ProgressBar("Jobs", jobIds.size.toLong()).use {
            val results = jobIds.asSequence().map { jobId ->
                val results = processJob(jobId, jobIds, strategy)
                it.step()
                Pair(jobId, results)
            }
            CsvOutput.writeSeq(results, output)
        }
    }

    private fun getJobs(projectName: String, jobSpec: JobSpec): List<String> = when (jobSpec) {
        JobSpec.ALL -> repository.jobs(projectName)
        JobSpec.ONLY_TEST_FAILURES -> repository.redJobs(projectName)
        JobSpec.ONLY_FIRST_TEST_FAILURES -> repository.firstRedJobs(projectName)
    }

    private fun processJob(jobId: String, jobIds: List<String>, strategy: PrioritisationStrategy): List<TestResult> {
        val params = Params(jobId, jobIds, repository)
        return ensureIndexed(strategy.apply(params))
    }

    private fun ensureIndexed(tr: List<TestResult>) = tr.mapIndexed { index, t -> t.copy(index = index) }
}
