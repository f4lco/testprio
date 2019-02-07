package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.DSL.selectOne

object Projects {

    fun jobIdsOf(context: DSLContext, projectName: String) =
            context.select(field(name("tr_job_id")))
                    .from(table(name("travistorrent_8_2_2017")))
                    .where(field(name("gh_project_name")).eq(projectName))
                    .orderBy(field(name("tr_build_number")))
                    .fetch(0, String::class.java)

    fun redJobIdsOf(context: DSLContext, projectName: String) =
        context.select(field(name("tt", "tr_job_id")))
                .from(table(name("travistorrent_8_2_2017")).`as`("tt"))
                .where(field(name("tt", "gh_project_name")).eq(projectName))
                .andExists(
                        selectOne().from(table(name("tr_test_result")).`as`("tr"))
                        .where("tr.tr_job_id = tt.tr_job_id")
                        .and(field(name("tr", "failures")).gt(0).or(field(name("tr", "errors")).gt(0)))
                ).fetch(0, String::class.java)
}