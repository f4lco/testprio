package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult

interface Repository {

    fun jobs(projectName: String): List<String>

    fun redJobs(projectName: String): List<String>

    fun firstRedJobs(projectName: String): List<String>

    fun changedFiles(jobId: String): List<String>

    fun testResults(jobId: String): List<TestResult>
}