package de.hpi.swa.testprio.probe

import org.jooq.DSLContext
import org.jooq.impl.DSL.rowNumber
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import org.jooq.impl.DSL.table
import org.jooq.impl.DSL.selectOne
import java.time.Instant

object Projects {

    private val TRAVIS_TORRENT = table(name("travistorrent_8_2_2017"))

    fun redJobs(context: DSLContext, projectName: String): List<Job> {
        return context.select(
            field(name("tt", "tr_build_number")),
            field(name("tt", "tr_build_id")),
            rowNumber().over().`as`(name("tr_job_number")).minus(1),
            field(name("tt", "tr_job_id")),
            field(name("tt", "gh_build_started_at")),
            field(name("tt", "tr_duration"))
        )
        .from(TRAVIS_TORRENT.asTable("tt"))
        .where(field(name("tt", "gh_project_name")).eq(projectName))
        .andExists(
                selectOne().from(table(name("tr_test_result")).asTable("tr"))
                .where("tt.tr_job_id = tr.tr_job_id")
                .and(field(name("tr", "failures")).gt(0).or(field(name("tr", "errors")).gt(0)))
        )
        .orderBy(field(name("tt", "tr_build_number")))
        .fetch { record ->
            val begin = record["gh_build_started_at", Instant::class.java]
            Job(
                buildNumber = record["tr_build_number", Int::class.java],
                build = record["tr_build_id", Int::class.java],
                jobNumber = record["tr_job_number", Int::class.java],
                job = record["tr_job_id", Int::class.java],
                begin = begin,
                end = begin.plusSeconds(record["tr_duration", Long::class.java])
            )
        }
    }
}