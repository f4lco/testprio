package de.hpi.swa.testprio.strategy

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.cli.PrioritizeCommand
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.matrix.MatrixCommands

object StrategyCommands {

    private val commands = listOf(
            PrioritizeUntreated(),
            PrioritizeRandom(),
            PrioritizeLRU(),
            PrioritizeRecentlyFailed(),
            PrioritizePathTCOverlap()
    ) + MatrixCommands.get()

    fun get() = commands
}

private class PrioritizeUntreated : PrioritizeCommand(
        name = "untreated",
        help = "Output the untreated test order") {

    override fun strategy(repository: Repository) = UntreatedStrategy()
}

private class PrioritizeRandom : PrioritizeCommand(name = "random", help = "Randomize test order") {
    val seed by option("--seed").int().default(42)

    override fun strategy(repository: Repository) = RandomStrategy(seed)
}

private class PrioritizeLRU : PrioritizeCommand(
        name = "lru",
        help = "Prioritize tests which run late in the build process") {

    override fun strategy(repository: Repository) = LeastRecentlyUsedStrategy()
}

private class PrioritizeRecentlyFailed : PrioritizeCommand(
        name = "recently-failed",
        help = "Prioritize recently failed test cases") {

    val alpha by option("--alpha").double().default(0.8)

    override fun strategy(repository: Repository) = RecentlyFailedStrategy(alpha)
}

private class PrioritizePathTCOverlap : PrioritizeCommand(
        name = "path-tc-overlap",
        help = "Prioritize test case with names similar to path parts") {

    override fun strategy(repository: Repository) = PathTestCaseOverlap()
}