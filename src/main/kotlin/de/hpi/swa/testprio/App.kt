package de.hpi.swa.testprio

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.parser.CsvOutput
import de.hpi.swa.testprio.parser.LogParser
import de.hpi.swa.testprio.probe.DatabaseRepository
import de.hpi.swa.testprio.strategy.RecentlyFailedStrategy
import de.hpi.swa.testprio.strategy.LeastRecentlyUsedStrategy
import de.hpi.swa.testprio.strategy.RandomStrategy
import de.hpi.swa.testprio.strategy.StrategyRunner
import de.hpi.swa.testprio.strategy.matrix.Cache
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy
import de.hpi.swa.testprio.strategy.UntreatedStrategy
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

private open class DatabaseCommand(name: String? = null) : CliktCommand(name = name) {

    val host by option("--host").default("localhost")
    val port by option("--port").int().default(5432)
    val db by option("--db").default("github")
    val user by option("--user")
    val password by option("--password").prompt("Password", hideInput = true)

    override fun run() = Unit

    protected fun makeContext(): DSLContext {
        val source = PGSimpleDataSource()
        source.setUrl("jdbc:postgresql://$host:$port/$db")
        source.user = user
        source.password = password
        source.readOnly = true
        return DSL.using(source, SQLDialect.POSTGRES)
    }
}

private class Parse : CliktCommand() {
    val logs by option("-l", "--logs").file(exists = true, readable = true).required()
    val output: File by option("-o", "--output").file(exists = false).required()

    override fun run() {
        val parseResult = LogParser.parseLog(logs)
        CsvOutput.write(parseResult, output)
    }
}

private open class PrioritizeCommand(name: String?) : DatabaseCommand(name = name) {
    val projectName by option("--project").required()
    val output by option("--output").file(exists = false, folderOkay = false).required()
}

private class PrioritizeMatrix : PrioritizeCommand("matrix") {
    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val windowSize by option("--window").int().default(100)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it)
            val cache = Cache(cacheDirectory)
            val strategy = ChangeMatrixStrategy(repository, cache, windowSize = windowSize)
            StrategyRunner(it).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeUntreated : PrioritizeCommand("untreated") {

    override fun run() {
        makeContext().use {
            StrategyRunner(it).run(projectName, UntreatedStrategy(), output)
        }
    }
}

private class PrioritizeRecentlyFailed : PrioritizeCommand("recently-failed") {
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            StrategyRunner(it).run(projectName, RecentlyFailedStrategy(alpha), output)
        }
    }
}

private class PrioritizeLRU : PrioritizeCommand("lru") {

    override fun run() {
        makeContext().use {
            StrategyRunner(it).run(projectName, LeastRecentlyUsedStrategy(), output)
        }
    }
}

private class PrioritizeRandom : PrioritizeCommand("random") {
    val seed by option("--seed").int().default(42)

    override fun run() {
        makeContext().use {
            StrategyRunner(it).run(projectName, RandomStrategy(seed), output)
        }
    }
}

private class EntryPoint : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) = EntryPoint().subcommands(
        Parse(),
        PrioritizeRandom(),
        PrioritizeLRU(),
        PrioritizeRecentlyFailed(),
        PrioritizeMatrix(),
        PrioritizeUntreated()).main(args)
