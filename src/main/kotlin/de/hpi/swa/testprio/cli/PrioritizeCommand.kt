package de.hpi.swa.testprio.cli

import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file
import de.hpi.swa.testprio.probe.Patches

open class PrioritizeCommand(name: String?, help: String = "") : DatabaseCommand(name = name, help = help) {
    val projectName by option("--project").required()
    val patchTable by option("--patches").choice(Patches.ALL_BUILT_COMMITS, Patches.COMMITS_IN_PUSH).default(Patches.ALL_BUILT_COMMITS)
    val output by option("--output").file(exists = false, folderOkay = false).required()
}