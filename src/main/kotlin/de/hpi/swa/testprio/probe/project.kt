package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.*

object Projects {

    fun jobIdsOf(context: DSLContext, projectName: String) =
            context.select(field(name("tr_job_id")))
                    .from(table(name("travistorrent_8_2_2017")))
                    .where(field(name("gh_project_name")).eq(projectName))
                    .orderBy(field(name("tr_build_number")))
                    .fetch(0, String::class.java)

    fun redJobIdsOf(context: DSLContext, projectName: String) =
            context.select(field(name("tr_job_id")))
                    .from(table(name("travistorrent_8_2_2017")))
                    .where(field(name("gh_project_name")).eq(projectName))
                    .and(field(name("tr_log_bool_tests_ran")).isTrue)
                    .and(field(name("tr_log_bool_tests_failed")).isTrue)
                    .fetch(0, String::class.java)
}