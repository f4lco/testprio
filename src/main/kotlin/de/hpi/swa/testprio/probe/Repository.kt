package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult

interface Repository {

    fun redJobIdsOf(projectName: String): List<String>

    fun changedFiles(jobId: String): List<String>

    fun testResults(jobId: String): List<TestResult>
}