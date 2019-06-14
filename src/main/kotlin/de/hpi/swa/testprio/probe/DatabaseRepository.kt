package de.hpi.swa.testprio.probe

import org.jooq.DSLContext

class DatabaseRepository(val context: DSLContext, private val patchTable: String) : Repository {

    override fun redJobs(projectName: String) = Projects.redJobs(context, projectName)

    override fun changedFiles(job: Job) = Patches.selectPatches(context, job, patchTable)

    override fun testResults(job: Job) = TestResults.ofJob(context, job)
}