package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult

data class Job(
    val buildNumber: Int,
    val build: Int,
    val job: Int
) {
    companion object
}

interface Repository {

    fun redJobs(projectName: String): List<Job>

    fun changedFiles(jobId: String): List<String>

    fun testResults(jobId: String): List<TestResult>
}