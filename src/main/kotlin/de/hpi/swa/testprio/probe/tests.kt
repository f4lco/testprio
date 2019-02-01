package de.hpi.swa.testprio.probe

import de.hpi.swa.testprio.parser.TestResult
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.field
import org.jooq.impl.DSL.name
import java.math.BigDecimal

object TestResults {

    fun ofJob(context: DSLContext, jobId: String) =
            context.selectFrom<Record>("tr_test_result")
                    .where(field(name("tr_job_id")).eq(jobId.toInt()))
                    .orderBy(field(name("index")))
                    .fetch {
                        TestResult(name = it["name"] as String,
                                index = it["index"] as Int,
                                duration = it["duration"] as BigDecimal,
                                count = it["count"] as Int,
                                failures = it["failures"] as Int,
                                errors = it["errors"] as Int,
                                skipped = it["skipped"] as Int
                        )

                    }
}
