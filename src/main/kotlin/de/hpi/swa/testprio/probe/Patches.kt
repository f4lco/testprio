package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table

object Patches {

    fun selectPatches(context: DSLContext, travisJobId: String): List<String> =
        context.selectDistinct(field(name("tr_patches", "filename")))
                .from(table(name("tr_patches")),
                        table(name("tr_all_built_commits")))
                .where("tr_patches.sha = tr_all_built_commits.git_commit_id")
                .and(field(name("tr_all_built_commits", "tr_job_id")).eq(travisJobId.toLong()))
                .fetch(field(name("tr_patches", "filename")), String::class.java)
}