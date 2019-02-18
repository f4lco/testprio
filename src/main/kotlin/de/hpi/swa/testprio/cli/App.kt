package de.hpi.swa.testprio.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import de.hpi.swa.testprio.parser.ParseCommand
import de.hpi.swa.testprio.strategy.StrategyCommands

private class EntryPoint : CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) = EntryPoint()
        .subcommands(ParseCommand())
        .subcommands(StrategyCommands.get())
        .main(args)
