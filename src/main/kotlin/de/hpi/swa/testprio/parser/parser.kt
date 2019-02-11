package de.hpi.swa.testprio.parser

import me.tongfei.progressbar.ProgressBar
import mu.KotlinLogging
import java.io.File

private val logger = KotlinLogging.logger {}

interface Parser {

    fun parseFile(logFile: LogFile): Sequence<TestResult>
}

class LogParser(private val parser: Parser) {

    fun parseLog(logPath: File, to: File) {
        val files = if (logPath.isDirectory) logFilesOf(logPath)
        else listOf(LogFile.of(logPath))

        ProgressBar("Files", files.size.toLong()).use { bar ->
            val results = files.asSequence().map {
                val result = parseLogFile(it)
                bar.step()
                result
            }

            CsvOutput.write(results, to)
        }
    }

    private fun parseLogFile(logFile: LogFile): ParseResult {
        logger.info("Processing {}", logFile.source.name)
        val testResults = parser.parseFile(logFile)
        return ParseResult(logFile, testResults.toList())
    }

    /**
     * Get unique logfiles.
     *
     * When merging the two set of TravisTorrent logfiles ("20-12-2016", "rubyjava"), duplicates
     * may not be detected when copying into a common folder, because the two sets have different
     * naming conventions.
     *
     */
    private fun logFilesOf(path: File): List<LogFile> = path.listFiles { pathName: File ->
        pathName.name.endsWith(".log")
    }.map { LogFile.of(it) }.distinctBy { it.travisJobId }
}
