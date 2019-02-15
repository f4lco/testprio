package de.hpi.swa.testprio

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.parser.BuckParser
import de.hpi.swa.testprio.parser.LogParser
import de.hpi.swa.testprio.parser.MavenLogParser
import de.hpi.swa.testprio.probe.DatabaseRepository
import de.hpi.swa.testprio.probe.Patches
import de.hpi.swa.testprio.strategy.RecentlyFailedStrategy
import de.hpi.swa.testprio.strategy.LeastRecentlyUsedStrategy
import de.hpi.swa.testprio.strategy.RandomStrategy
import de.hpi.swa.testprio.strategy.PathTCOverlapStrategy
import de.hpi.swa.testprio.strategy.StrategyRunner
import de.hpi.swa.testprio.strategy.matrix.Cache
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy
import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixSimilarityStrategy
import de.hpi.swa.testprio.strategy.matrix.PathSimilarityStrategy
import de.hpi.swa.testprio.strategy.matrix.TCSimilarityStrategy
import de.hpi.swa.testprio.strategy.matrix.DevaluationReducer
import de.hpi.swa.testprio.strategy.matrix.NormalizedMatrix
import de.hpi.swa.testprio.strategy.UntreatedStrategy
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.postgresql.ds.PGSimpleDataSource
import java.io.File

private open class DatabaseCommand(
    name: String? = null,
    help: String = ""
) : CliktCommand(name = name, help = help) {

    val host by option("--host").default("localhost")
    val port by option("--port").int().default(5432)
    val db by option("--db").default("github")
    val user by option("--user")
    val password by option("--password", envvar = "PRIO_PW").prompt("Password", hideInput = true)

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

private class Parse : CliktCommand(help = "Parse test results from build log files") {
    val logs by option("-l", "--logs").file(exists = true, readable = true).required()
    val output by option("-o", "--output").file(exists = false).required()
    val type by option("-t", "--type").choice("maven", "buck").default("maven")

    override fun run() {
        val parser = when (type) {
            "maven" -> MavenLogParser
            "buck" -> BuckParser
            else -> throw IllegalArgumentException(type)
        }

        LogParser(parser).parseLog(logs, output)
    }
}

private open class PrioritizeCommand(name: String?, help: String = "") : DatabaseCommand(name = name, help = help) {
    val projectName by option("--project").required()
    val patchTable by option("--patches").choice(Patches.ALL_BUILT_COMMITS, Patches.COMMITS_IN_PUSH).default(Patches.ALL_BUILT_COMMITS)
    val output by option("--output").file(exists = false, folderOkay = false).required()
}

private class PrioritizeMatrix : PrioritizeCommand(
        name = "matrix",
        help = "Prioritize using counting matrix") {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val windowSize by option("--window").int().default(100)
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = ChangeMatrixStrategy(
                    repository,
                    cache,
                    DevaluationReducer(alpha),
                    windowSize)

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeSimilarityMatrix : PrioritizeCommand(
        name = "matrix-similarity",
        help = "Prioritize using similarity matrix") {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = ChangeMatrixSimilarityStrategy(
                    repository,
                    cache,
                    DevaluationReducer(alpha))

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeNormalizedMatrix : PrioritizeCommand(
        name = "matrix-normalized",
        help = "Prioritize using normalized matrix") {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val prior by option("--prior").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = NormalizedMatrix(
                    repository,
                    cache,
                    prior,
                    DevaluationReducer(alpha))

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizePathSimilarityMatrix : PrioritizeCommand(
        name = "matrix-path-similarity",
        help = "Prioritize using weighted path similarity"
) {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = PathSimilarityStrategy(
                    repository,
                    cache,
                    DevaluationReducer(alpha))

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeTCSimilarityMatrix : PrioritizeCommand(
        name = "matrix-tc-similarity",
        help = "Prioritize TC similar to those connected to the change"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = TCSimilarityStrategy(
                    repository,
                    cache,
                    DevaluationReducer(alpha))

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizePathTCOverlap : PrioritizeCommand(
        name = "path-tc-overlap",
        help = "Prioritize test case with names similar to path parts") {

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            StrategyRunner(repository).run(projectName, PathTCOverlapStrategy(), output)
        }
    }
}

private class PrioritizeUntreated : PrioritizeCommand(
        name = "untreated",
        help = "Output the untreated test order") {

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            StrategyRunner(repository).run(projectName, UntreatedStrategy(), output)
        }
    }
}

private class PrioritizeRecentlyFailed : PrioritizeCommand(
        name = "recently-failed",
        help = "Prioritize recently failed test cases") {

    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            StrategyRunner(repository).run(projectName, RecentlyFailedStrategy(alpha), output)
        }
    }
}

private class PrioritizeLRU : PrioritizeCommand(
        name = "lru",
        help = "Prioritize tests which run late in the build process") {

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            StrategyRunner(repository).run(projectName, LeastRecentlyUsedStrategy(), output)
        }
    }
}

private class PrioritizeRandom : PrioritizeCommand(name = "random", help = "Randomize test order") {
    val seed by option("--seed").int().default(42)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            StrategyRunner(repository).run(projectName, RandomStrategy(seed), output)
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
        PrioritizePathTCOverlap(),
        PrioritizeMatrix(),
        PrioritizeSimilarityMatrix(),
        PrioritizePathSimilarityMatrix(),
        PrioritizeTCSimilarityMatrix(),
        PrioritizeNormalizedMatrix(),
        PrioritizeUntreated()).main(args)
