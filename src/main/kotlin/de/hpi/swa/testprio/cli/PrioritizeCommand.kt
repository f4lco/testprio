package de.hpi.swa.testprio.cli

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import de.hpi.swa.testprio.probe.DatabaseRepository
import de.hpi.swa.testprio.probe.Patches
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import de.hpi.swa.testprio.strategy.StrategyRunner

abstract class PrioritizeCommand(name: String?, help: String = "") : DatabaseCommand(name = name, help = help) {
    val projectName by option("--project").required()
    val patchTable by option("--patches").choice(Patches.ALL_BUILT_COMMITS, Patches.COMMITS_IN_PUSH).default(Patches.ALL_BUILT_COMMITS)
    val windowSize by option("--window").int()
    val output by option("--output").file(exists = false, folderOkay = false).required()

    abstract fun strategy(repository: Repository): PrioritisationStrategy

    override fun run() {
        makeContext().use {
            val repository = DatabaseRepository(it, patchTable)
            val strategy = strategy(repository)
            StrategyRunner(repository).run(projectName, strategy, windowSize, output)
        }
    }
}