package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.PrioritizationResultOutput
import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.probe.Repository
import me.tongfei.progressbar.ProgressBar
import mu.KotlinLogging
import java.io.File
import java.util.PriorityQueue

class Params(
    val jobId: String,
    val jobIds: List<String>,
    val repository: Repository
) {

    val changedFiles: List<String> by lazy { repository.changedFiles(jobId) }

    val testResults: List<TestResult> by lazy { repository.testResults(jobId) }

    // FIXME invent "job serial number" for indexing to bitsets
    // Look at runner tests: multiple jobs may conclude they are at the same position
    // Rename jobIds to priorJobs?
    // Perhaps even jobIndex >= len(priorJobs) ???
    // Consider "current" and "deferred" jobs / learning
    val jobIndex by lazy { jobIds.indexOf(jobId) }
}

interface PrioritisationStrategy {

    fun reorder(p: Params): List<TestResult>

    fun acceptFailedRun(p: Params) {
        // Do nothing by default
    }
}

class StrategyRunner(val repository: Repository) {

    companion object {
        val LOG = KotlinLogging.logger { }
    }

    fun run(
        projectName: String,
        strategy: PrioritisationStrategy,
        output: File
    ) {

        val builds = repository.redJobs(projectName).groupBy { it.build }

        ProgressBar("Builds", builds.size.toLong()).use { progress ->
            val results = processBuilds(builds, strategy).onEach { progress.step() }
            PrioritizationResultOutput.write(results, output)
        }

        if (strategy is AutoCloseable) {
            strategy.close()
        }
    }

    private fun processBuilds(builds: Map<Int, List<Job>>, strategy: PrioritisationStrategy) = sequence {
        val pending = PriorityQueue<PendingBuild>(compareBy { it.end })
        val priorJobs = mutableListOf<Job>()

        for ((build, jobGroup) in builds) {

            // Learn all completed builds
            val jobGroupBegin = jobGroup.map { it.begin }.min()
            while (pending.isNotEmpty() && pending.peek().end <= jobGroupBegin) {
                val priorBuild = pending.poll()
                LOG.debug { "Learning results of build ${priorBuild.id}" }
                for (job in priorBuild.jobs) {
                    advanceState(job, priorBuild.priorJobs, strategy)
                }
                priorJobs += priorBuild.jobs
            }

            // Treat current build
            val newBuild = PendingBuild(jobGroup, priorJobs.toList())
            LOG.debug { "Processing build $build (${jobGroup.size} jobs)" }
            yieldAll(processBuild(jobGroup, priorJobs, strategy))
            pending += newBuild
        }

        // Drain pending jobs
        while (pending.isNotEmpty()) {
            val priorBuild = pending.poll()
            LOG.debug { "Learning results of build ${priorBuild.id}" }
            for (job in priorBuild.jobs) {
                advanceState(job, priorBuild.priorJobs, strategy)
            }
            priorJobs += priorBuild.jobs
        }
    }

    data class PendingBuild(val jobs: List<Job>, val priorJobs: List<Job>) {
        val id = jobs.first().build
        val end = jobs.map { it.end }.max()!!
    }

    private fun processBuild(jobs: List<Job>, priorJobs: List<Job>, strategy: PrioritisationStrategy) = sequence {
        for (job in jobs) {
            LOG.debug { "Reorder job $job" }
            yield(job to reorderJob(job, priorJobs, strategy))
        }
    }

    private fun reorderJob(job: Job, priorJobs: List<Job>, strategy: PrioritisationStrategy): List<TestResult> {
        val params = newParams(job, priorJobs)
        return ensureIndexed(strategy.reorder(params))
    }

    private fun advanceState(job: Job, priorJobs: List<Job>, strategy: PrioritisationStrategy) {
        val params = newParams(job, priorJobs)
        strategy.acceptFailedRun(params)
    }

    private fun newParams(job: Job, priorJobs: List<Job>): Params {
        val jobId: String = job.job.toString()
        return Params(
            jobId,
            priorJobs.map { it.job.toString() } + jobId,
            repository
        )
    }

    private fun ensureIndexed(tr: List<TestResult>) = tr.mapIndexed { index, t -> t.copy(index = index) }
}
