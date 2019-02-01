package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.CsvOutput
import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Patches
import de.hpi.swa.testprio.probe.Projects
import de.hpi.swa.testprio.probe.TestResults
import me.tongfei.progressbar.ProgressBar
import org.jooq.DSLContext
import java.io.File

interface Params {
    val jobId: String
    val jobIds: List<String>
    val jobIndex: Int
    val changedFiles: List<String>
    val testResults: List<TestResult>
}

class DatabaseParams(context: DSLContext,
                     override val jobId: String,
                     override val jobIds: List<String>) : Params {

    override val changedFiles: List<String> by lazy { Patches.selectPatches(context, jobId) }

    override val testResults: List<TestResult> by lazy { TestResults.ofJob(context, jobId) }

    override val jobIndex by lazy { jobIds.indexOf(jobId) }
}

interface PrioritisationStrategy {

    fun apply(p: Params): List<TestResult>
}

class StrategyRunner(val context: DSLContext) {

    fun run(projectName: String, strategy: PrioritisationStrategy, output: File) {
        val jobIds = Projects.redJobIdsOf(context, projectName)
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
        val params = DatabaseParams(context, jobId, jobIds)
        return ensureIndexed(strategy.apply(params))
    }

    private fun ensureIndexed(tr: List<TestResult>) = tr.mapIndexed { index, t -> t.copy(index = index) }

}