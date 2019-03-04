package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.DSL.selectOne

object Projects {

    fun jobIdsOf(context: DSLContext, projectName: String): List<String> =
            context.select(field(name("tr_job_id")))
                    .from(table(name("travistorrent_8_2_2017")))
                    .where(field(name("gh_project_name")).eq(projectName))
                    .orderBy(field(name("tr_build_number")))
                    .fetch(0, String::class.java)

    fun redJobIdsOf(context: DSLContext, projectName: String): List<String> =
        context.select(field(name("tt", "tr_job_id")))
                .from(table(name("travistorrent_8_2_2017")).`as`("tt"))
                .where(field(name("tt", "gh_project_name")).eq(projectName))
                .andExists(
                        selectOne().from(table(name("tr_test_result")).`as`("tr"))
                        .where("tr.tr_job_id = tt.tr_job_id")
                        .and(field(name("tr", "failures")).gt(0).or(field(name("tr", "errors")).gt(0)))
                ).fetch(0, String::class.java)

    fun firstFailuresOf(context: DSLContext, projectName: String): List<String> =
        context.select(field(name("tt1", "tr_job_id")))
            .from(table(name("travistorrent_8_2_2017")).`as`("tt1"))
            .where(
                field(name("tt1", "gh_project_name")).eq(projectName)
                .andExists(
                        selectOne().from(table(name("tr_test_result")).`as`("tr1"))
                            .where("tr1.tr_job_id = tt1.tr_job_id")
                            .and(field(name("tr1", "failures")).gt(0).or(field(name("tr1", "errors")).gt(0)))
                )
                .andNotExists(
                    selectOne().from(table(name("travistorrent_8_2_2017")).`as`("tt2"))
                        .where("tt2.gh_project_name = tt1.gh_project_name").and("tt1.tr_build_number = tt2.tr_build_number + 1")
                        .andNotExists(
                            selectOne().from(table(name("tr_test_result")).`as`("tr2"))
                                .where("tr2.tr_job_id = tt2.tr_job_id")
                                .and(field(name("tr2", "failures")).gt(0).or(field(name("tr2", "errors")).gt(0)))
                        )
                )
            ).fetch(0, String::class.java)
}