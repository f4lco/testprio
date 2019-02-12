package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult
import org.jooq.DSLContext

class DatabaseRepository(val context: DSLContext, val patchTable: String) : Repository {

    override fun redJobIdsOf(projectName: String): List<String> = Projects.redJobIdsOf(context, projectName)

    override fun changedFiles(jobId: String) = Patches.selectPatches(context, jobId, patchTable)

    override fun testResults(jobId: String): List<TestResult> = TestResults.ofJob(context, jobId)
}