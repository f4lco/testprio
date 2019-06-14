package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table

object Patches {

    const val ALL_BUILT_COMMITS = "tr_all_built_commits"
    const val COMMITS_IN_PUSH = "tr_commits_in_push"

    fun selectPatches(context: DSLContext, job: Job, patchTable: String): List<String> =
        context.selectDistinct(field(name("tr_patches", "filename")))
                .from(table(name("tr_patches")),
                        table(name(patchTable)).`as`("commits"))
                .where("tr_patches.sha = commits.sha")
                .and(field(name("commits", "tr_job_id")).eq(job.job))
                .fetch(field(name("tr_patches", "filename")), String::class.java)
}