package de.hpi.swa.testprio.probe

import java.time.Instant
import de.hpi.swa.testprio.parser.TestResult

data class Job(
    val buildNumber: Int,
    val build: Int,
    val job: Int,
    val begin: Instant,
    val end: Instant
) {
    companion object
}

interface Repository {

    fun redJobs(projectName: String): List<Job>

    fun changedFiles(jobId: String): List<String>

    fun testResults(jobId: String): List<TestResult>
}