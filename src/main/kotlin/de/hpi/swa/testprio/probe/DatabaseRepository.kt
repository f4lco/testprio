package de.hpi.swa.testprio.probe

import org.jooq.DSLContext

class DatabaseRepository(val context: DSLContext, val patchTable: String) : Repository {

    override fun redJobs(projectName: String) = Projects.redJobs(context, projectName)

    override fun changedFiles(jobId: String) = Patches.selectPatches(context, jobId, patchTable)

    override fun testResults(jobId: String) = TestResults.ofJob(context, jobId)
}