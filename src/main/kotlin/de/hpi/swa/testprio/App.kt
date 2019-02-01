package de.hpi.swa.testprio

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.parser.*
import de.hpi.swa.testprio.strategy.*
import de.hpi.swa.testprio.strategy.RecentlyFailedStrategy
import de.hpi.swa.testprio.strategy.matrix.Cache
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy
import de.hpi.swa.testprio.strategy.UntreatedStrategy
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

private open class DatabaseCommand(name: String? = null) : CliktCommand(name = name) {

    val host by option("--host").default("localhost")
    val port by option("--port").int().default(5432)
    val db by option("--db").default("github")
    val user by option("--user")
    val password by option("--password")

    override fun run() = Unit

    protected fun makeContext(readOnly: Boolean = true): DSLContext {
        val source = PGSimpleDataSource()
        source.setUrl("jdbc:postgresql://$host:$port/$db")
        source.user = user
        source.password = password
        source.readOnly = readOnly
        return DSL.using(source, SQLDialect.POSTGRES)
    }
}

private class ParseToCsv : CliktCommand() {
    val logs by option("-l", "--logs").file(exists = true, readable = true).required()
    val output: File by option("-o", "--output").file(exists = false).required()

    override fun run() {
        val parseResult = LogParser.parseLog(logs)
        CsvOutput.write(parseResult, output)
    }
}

private class ParseToDb : DatabaseCommand() {

    val logs by option("-l", "--logs").file(exists = true, readable = true).required()

    override fun run() {
        parse()
    }

    private fun parse() {
        val parsed = LogParser.parseLog(logs)

        makeContext(readOnly = false).use { db ->

            db.createTableIfNotExists(table(name("tr_test_result")))
                    .column("tr_job_id", SQLDataType.BIGINT).apply {
                        column("name", SQLDataType.VARCHAR)
                        column("index", SQLDataType.INTEGER)
                        column("duration", SQLDataType.DECIMAL)
                        column("count", SQLDataType.INTEGER)
                        column("failures", SQLDataType.INTEGER)
                        column("errors", SQLDataType.INTEGER)
                        column("skipped", SQLDataType.INTEGER)
                    }.execute()

            parsed.forEach { result ->
                val statements = result.testResults.map { r ->

                    db.insertInto(table(name("tr_test_result")))
                            .set(field(name("tr_job_id")), result.source.travisJobId)
                            .set(field(name("name")), r.name)
                            .set(field(name("index")), r.index)
                            .set(field(name("duration")), r.duration)
                            .set(field(name("count")), r.count)
                            .set(field(name("failures")), r.failures)
                            .set(field(name("errors")), r.errors)
                            .set(field(name("skipped")), r.skipped)
                }

                db.batch(statements).execute()
            }
        }

    }
}

private open class PrioritizeCommand(name: String?) : DatabaseCommand(name = name) {
    val projectName by option("--project").required()
    val output by option("--output").file(exists = false, folderOkay = false).required()
}

private class PrioritizeMatrix : PrioritizeCommand("matrix") {
    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))

    override fun run() {
        makeContext(readOnly = true).use {
            val cache = Cache(cacheDirectory)
            val strategy = ChangeMatrixStrategy(it, cache = cache)
            StrategyRunner(it).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeUntreated: PrioritizeCommand("untreated") {

    override fun run() {
        makeContext(readOnly = true).use {
            StrategyRunner(it).run(projectName, UntreatedStrategy(), output)
        }
    }
}

private class PrioritizeRecentlyFailed : PrioritizeCommand("recently-failed") {
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext(readOnly = true).use {
            StrategyRunner(it).run(projectName, RecentlyFailedStrategy(alpha), output)
        }
    }
}

private class PrioritizeLRU : PrioritizeCommand("lru") {

    override fun run() {
        makeContext(readOnly = true).use {
            StrategyRunner(it).run(projectName, LeastRecentlyUsedStrategy(), output)
        }
    }
}

private class PrioritizeRandom : PrioritizeCommand("random") {
    val seed by option("--seed").int().default(42)

    override fun run() {
        makeContext(readOnly = true).use {
            StrategyRunner(it).run(projectName, RandomStrategy(seed), output)
        }
    }
}

private class EntryPoint : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) = EntryPoint().subcommands(
        ParseToCsv(),
        ParseToDb(),
        PrioritizeRandom(),
        PrioritizeLRU(),
        PrioritizeRecentlyFailed(),
        PrioritizeMatrix(),
        PrioritizeUntreated()).main(args)
