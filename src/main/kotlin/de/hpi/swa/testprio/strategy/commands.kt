package de.hpi.swa.testprio.strategy

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
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
            PrioritizePathTCOverlap(),
            PrioritizeGraph(),
            PrioritizeOptimalFailures(),
            PrioritizeOptimalFailuresPerDuration()
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

private class PrioritizeGraph : PrioritizeCommand(
        name = "graph",
        help = "Prioritize test cases using graph"
) {

    val graphHost by option("--graph").default("bolt://localhost:7687")
    val graphUser by option("--graph-user").default("neo4j")
    val graphPassword by option("--graph-pw", envvar = "NEO4J_PASS").required()

    override fun strategy(repository: Repository) = Graph(graphHost, graphUser, graphPassword)
}

private class PrioritizeOptimalFailures : PrioritizeCommand(
        name = "optimal-failure",
        help = "Prioritize TC revealing many faults"
) {
    override fun strategy(repository: Repository) = Optimal.byFailureCount()
}

private class PrioritizeOptimalFailuresPerDuration : PrioritizeCommand(
    name = "optimal-failure-duration",
    help = "Prioritize TC with optimal ratio of failures to duration"
) {
    override fun strategy(repository: Repository) = Optimal.byFailuresPerDuration()
}