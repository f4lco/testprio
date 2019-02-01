package de.hpi.swa.testprio.parser

import mu.KotlinLogging
import java.io.File
import java.lang.IllegalStateException
import java.security.MessageDigest

private val logger = KotlinLogging.logger {}

object LogParser {

    fun parseLog(logPath: File) =
            if (logPath.isDirectory) parseLogDirectory(logPath)
            else sequenceOf(parseLogFile(LogFile.of(logPath)))

    private fun parseLogDirectory(logPath: File) = logFilesOf(logPath).map { parseLogFile(it) }

    private fun parseLogFile(logFile: LogFile): ParseResult {
        logger.info("Processing {}", logFile.source.name)
        val testResults = MavenLogParser.parseFile(logFile)
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
    private fun logFilesOf(path: File): Sequence<LogFile> = path.listFiles { pathName: File ->
        pathName.name.endsWith(".log")
    }.map { LogFile.of(it) }.distinctBy { it.travisJobId }.asSequence()
}