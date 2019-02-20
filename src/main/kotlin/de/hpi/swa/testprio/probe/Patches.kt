package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table

object Patches {

    val ALL_BUILT_COMMITS = "tr_all_built_commits"
    val COMMITS_IN_PUSH = "tr_commits_in_push"

    fun selectPatches(context: DSLContext, travisJobId: String, patchTable: String): List<String> =
        context.selectDistinct(field(name("tr_patches", "filename")))
                .from(table(name("tr_patches")),
                        table(name(patchTable)).`as`("commits"))
                .where("tr_patches.sha = commits.sha")
                .and(field(name("commits", "tr_job_id")).eq(travisJobId.toLong()))
                .fetch(field(name("tr_patches", "filename")), String::class.java)
}