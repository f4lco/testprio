package de.hpi.swa.testprio.parser

import java.math.BigDecimal
import java.util.concurrent.TimeUnit

object BuckParser : Parser {

    /**
     * Example:
     *
     * PASS     25.2s  2 Passed   0 Skipped   0 Failed   com.facebook.buck.android.AndroidBuildConfigIntegrationTest
     * FAIL      4.0s  0 Passed   0 Skipped   1 Failed   com.facebook.buck.android.MultipleBuildConfigIntegrationTest
     */
    private val testPattern = "(\\w)+\\s+<?([.,\\d]+)(\\w+)\\s+(\\d+) Passed\\s+(\\d+) Skipped\\s+(\\d+) Failed\\s+(.*)\$".toRegex(RegexOption.MULTILINE)

    private val validStates = listOf("PASS", "FAIL")

    override fun parseFile(logFile: LogFile): Sequence<TestResult> {
        val text = logFile.source.readText()
        return testPattern.findAll(text).mapIndexed(::newTestResult)
    }

    private fun newTestResult(index: Int, m: MatchResult): TestResult {
        val (status, duration, unit, passed, skipped, failed, name) = m.destructured
        assert(status in validStates) { "Invalid status: $status" }

        val passCount = passed.toInt()
        val skipCount = skipped.toInt()
        val failCount = failed.toInt()

        return TestResult(
                name = name,
                index = index,
                duration = durationFrom(duration, unit),
                count = passCount + skipCount + failCount,
                failures = failCount,
                errors = 0,
                skipped = skipCount
        )
    }

    private fun durationFrom(duration: String, unit: String): BigDecimal {
        // Precision loss: ignoring split {millis, seconds, minutes} for now
        val value = duration.replace(",", "").split(".", limit = 2)[0].toLong()
        val timeUnit = when (unit) {
            "ms" -> TimeUnit.MILLISECONDS
            "s" -> TimeUnit.SECONDS
            "m" -> TimeUnit.MINUTES
            else -> throw IllegalArgumentException("Illegal time unit: $unit")
        }

        return TimeUnit.SECONDS.convert(value, timeUnit).toBigDecimal()
    }
}