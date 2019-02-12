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

class StrategyRunner(val repository: Repository) {

    fun run(projectName: String, strategy: PrioritisationStrategy, output: File) {
        val jobIds = repository.redJobIdsOf(projectName)
        ProgressBar("Jobs", jobIds.size.toLong()).use {
            val results = jobIds.asSequence().map { jobId ->
                val results = processJob(jobId, jobIds, strategy)
                it.step()
                Pair(jobId, results)
            }
            CsvOutput.writeSeq(results, output)
        }
    }

    private fun processJob(jobId: String, jobIds: List<String>, strategy: PrioritisationStrategy): List<TestResult> {
        val params = Params(jobId, jobIds, repository)
        return ensureIndexed(strategy.apply(params))
    }

    private fun ensureIndexed(tr: List<TestResult>) = tr.mapIndexed { index, t -> t.copy(index = index) }
}
