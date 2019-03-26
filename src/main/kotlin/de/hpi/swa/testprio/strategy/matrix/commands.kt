package de.hpi.swa.testprio.strategy.matrix

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.cli.PrioritizeCommand
import de.hpi.swa.testprio.probe.Repository
import java.io.File

object MatrixCommands {

    private val commands = listOf<CliktCommand>(
        PrioritizeNaive(),
        PrioritizePathSimilarity(),
        PrioritizeFileSimilarity(),
        PrioritizeTestCaseSimilarity(),
        PrioritizeConditionalProbability(),
        PrioritizeBloom(),
        PrioritizeRecentlyChanged(),
        PrioritizeNMF()
    )

    fun get() = commands
}

private class PrioritizeNaive : PrioritizeCommand(
        name = "matrix-naive",
        help = "Prioritize using naive matrix approach"
) {

    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val windowSize by option("--window").int().default(100)
    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = NaiveMatrix(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha),
        windowSize)
}

private class PrioritizeFileSimilarity : PrioritizeCommand(
        name = "matrix-file-similarity",
        help = "Prioritize using similarity matrix"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val prior by option("--prior").double().default(0.8)

    override fun strategy(repository: Repository) = FileFailureDistributionSimilarity(
        repository,
        Cache(cacheDirectory),
        prior,
        DevaluationReducer(alpha))
}

private class PrioritizePathSimilarity : PrioritizeCommand(
        name = "matrix-path-similarity",
        help = "Prioritize using weighted path similarity"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = PathSimilarity(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha))
}

private class PrioritizeTestCaseSimilarity : PrioritizeCommand(
        name = "matrix-tc-similarity",
        help = "Prioritize TC similar to those connected to the change"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val prior by option("--prior").double().default(0.8)

    override fun strategy(repository: Repository) = TestCaseFailureDistributionSimilarity(
        repository,
        Cache(cacheDirectory),
        prior,
        DevaluationReducer(alpha))
}

private class PrioritizeConditionalProbability : PrioritizeCommand(
        name = "matrix-conditional-prob",
        help = "Prioritize with conditional probabilities"
) {

    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = ConditionalProbability(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha))
}

private class PrioritizeBloom : PrioritizeCommand(
        name = "bloom",
        help = "Prioritize with Bloom"
) {

    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)
    val expectedInsertions by option("--insertions").int().default(100)

    override fun strategy(repository: Repository) = Bloom(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha),
        expectedInsertions)
}

private class PrioritizeRecentlyChanged : PrioritizeCommand(
        name = "matrix-recently-changed",
        help = "Prioritize recently changed files"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = RecentlyChanged(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha),
        alpha
    )
}

private class PrioritizeNMF : PrioritizeCommand(
        name = "matrix-nmf",
        help = "Use NMF"
) {
    val cacheDirectory by option("--cache").file(fileOkay = false).default(File("cache"))
    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = NMF(
        repository,
        Cache(cacheDirectory),
        DevaluationReducer(alpha)
    )
}