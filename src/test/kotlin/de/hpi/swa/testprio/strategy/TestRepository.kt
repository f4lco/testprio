package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.probe.Repository

class TestRepository : Repository {

    private val changedFiles = mutableMapOf<Job, MutableList<String>>()

    private val testResults = mutableMapOf<Job, MutableList<TestResult>>()

    fun jobs() = (changedFiles.keys + testResults.keys).toList().sortedBy { it.job }

    fun load(revisions: List<Revision>) {
        for (revision in revisions) {
            changedFiles(revision.job).addAll(revision.changedFiles)
            testResults(revision.job).addAll(revision.testResults)
        }
    }

    override fun redJobs(projectName: String) = TODO()

    override fun changedFiles(job: Job): MutableList<String> = changedFiles.computeIfAbsent(job) { mutableListOf() }

    override fun testResults(job: Job) = testResults.computeIfAbsent(job) { mutableListOf() }
}