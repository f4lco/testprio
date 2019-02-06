package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult
import org.jooq.DSLContext

class DatabaseRepository(val context: DSLContext) : Repository {

    override fun changedFiles(jobId: String) = Patches.selectPatches(context, jobId)

    override fun testResults(jobId: String): List<TestResult> = TestResults.ofJob(context, jobId)
}