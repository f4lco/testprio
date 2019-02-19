package de.hpi.swa.testprio.strategy.matrix

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.cli.PrioritizeCommand
import de.hpi.swa.testprio.probe.DatabaseRepository
import de.hpi.swa.testprio.strategy.StrategyRunner
import java.io.File

object MatrixCommands {

    private val commands = listOf<CliktCommand>(
                PrioritizeNaiveMatrix(),
                PrioritizePathSimilarityMatrix(),
                PrioritizeFileSimilarityMatrix(),
                PrioritizeTestCaseSimilarityMatrix(),
                PrioritizeBloom()
    )

    fun get() = commands
}

private class PrioritizeNaiveMatrix : PrioritizeCommand(
        name = "matrix-naive",
        help = "Prioritize using naive matrix approach") {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val windowSize by option("--window").int().default(100)
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = NaiveMatrix(
                    repository,
                    cache,
                    DevaluationReducer(alpha),
                    windowSize)

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeFileSimilarityMatrix : PrioritizeCommand(
        name = "matrix-file-similarity",
        help = "Prioritize using similarity matrix") {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val prior by option("--prior").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = FileFailureDistributionSimilarity(
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

private class PrioritizeTestCaseSimilarityMatrix : PrioritizeCommand(
        name = "matrix-tc-similarity",
        help = "Prioritize TC similar to those connected to the change"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = TestCaseFailureDistributionSimilarity(
                    repository,
                    cache,
                    DevaluationReducer(alpha))

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}

private class PrioritizeBloom : PrioritizeCommand(
        name = "bloom",
        help = "Prioritize with Bloom"
) {

    val cacheDirectory by option("--cache").file(fileOkay = false, exists = true).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val expectedInsertions by option("--insertions").int().default(100)

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val cache = Cache(cacheDirectory)
            val strategy = Bloom(
                    repository,
                    cache,
                    DevaluationReducer(alpha),
                    expectedInsertions)

            StrategyRunner(repository).run(projectName, strategy, output)
        }
    }
}