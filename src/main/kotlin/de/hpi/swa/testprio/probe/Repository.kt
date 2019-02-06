package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult

interface Repository {

    fun changedFiles(jobId: String): List<String>

    fun testResults(jobId: String): List<TestResult>
}