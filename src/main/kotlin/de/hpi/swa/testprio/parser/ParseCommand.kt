package de.hpi.swa.testprio.parser

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.file

class ParseCommand : CliktCommand(name = "parse", help = "Parse test results from build log files") {
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