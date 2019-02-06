package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository

class TestRepository : Repository {

    val changedFiles = mutableMapOf<String, List<String>>()

    val testResults = mutableMapOf<String, List<TestResult>>()

    override fun changedFiles(jobId: String) = changedFiles[jobId] ?: emptyList()

    override fun testResults(jobId: String) = testResults[jobId] ?: emptyList()
}