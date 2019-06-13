package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository

class TestRepository : Repository {

    val changedFiles = mutableMapOf<String, MutableList<String>>()

    val testResults = mutableMapOf<String, MutableList<TestResult>>()

    fun jobs() = (changedFiles.keys + testResults.keys).toList().sorted()

    fun load(revisions: List<Revision>) {
        for (revision in revisions) {
            changedFiles.computeIfAbsent(revision.job.job.toString()) { mutableListOf() }.addAll(revision.changedFiles)
            testResults.computeIfAbsent(revision.job.job.toString()) { mutableListOf() }.addAll(revision.testResults)
        }
    }

    override fun redJobs(projectName: String) = TODO()

    override fun changedFiles(jobId: String): MutableList<String> = changedFiles.computeIfAbsent(jobId) { mutableListOf() }

    override fun testResults(jobId: String) = testResults.computeIfAbsent(jobId) { mutableListOf() }
}