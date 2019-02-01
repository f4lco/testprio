package de.hpi.swa.testprio.parser

import mu.KotlinLogging
import java.math.BigDecimal
import kotlin.text.StringBuilder

private val logger = KotlinLogging.logger {}

/**
 * Parse Maven log files and check for the output format of the Surefire plugin.
 *
 * The output of a test class consists of two lines: "Running ..." (test announcement) and
 * "Tests run: 42, ..." (test completion, without a name / reference to the completed test).
 * These should always appear in pairs, and always end in newline.
 *
 * However, in the Guava logs, outputs of different test classes appear interleaved, e.g., multiple
 * test announcements or test results appear in a row. Also, the newline at the end of test events
 * might be missing.
 *
 * For this reasons, we apply the following best-effort parsing heuristic:
 * - Split log into sections containing Maven modules, s.t. tests of different modules do not interleave
 * - Find all test announcement and completion events for that section, and "zip". Assume that the
 *   test case which was first announced will be also the first to complete.
 */
object MavenLogParser {

    private val moduleBannerPattern = "^\\[INFO] Building (.*?)\$\\s+\\[INFO] -{72}\$".toRegex(RegexOption.MULTILINE)

    private val testNamePattern = "Running (\\w+(\\.\\w+)*)".toRegex(RegexOption.MULTILINE)

    private val testResultPattern = "Tests run: (\\d+), Failures: (\\d+), Errors: (\\d+), Skipped: (\\d+), Time elapsed: (.+?) sec".toRegex()

    fun parseFile(logFile: LogFile): Sequence<TestResult> {
        val content = logFile.source.readText()
        return splitSections(content).flatMap(::testResultFromSection).mapIndexed { index, item ->
            item.copy(index = index)
        }
    }

    private fun splitSections(log: String): Sequence<Section> {
        return moduleBannerPattern.findAll(log).windowed(2, partialWindows = true).map {
            val begin = it.component1()
            val name = begin.groupValues[1]

            val firstChar = begin.range.last + 1
            val lastChar = when (it.size) {
                1 -> log.length
                2 -> it.component2().range.first
                else -> throw IllegalArgumentException("illegal window")
            }

            Section(
                    name,
                    log.substring(firstChar, lastChar),
                    firstChar, lastChar)
        }
    }

    private fun testResultFromSection(section: Section): Sequence<TestResult> {
        logger.debug("Processing section: {} ({} - {})",
                section.name, section.firstChar, section.lastChar)

        val announced = findTestAnnounced(section).toList()
        val completed = findTestCompleted(section).toList()

        if (announced.size != completed.size) {
            logger.warn("Faulty section detected, {} announced tests, {} completed tests",
                    announced.size, completed.size)
            logger.trace { printableSectionString(section) }
        }

        return announced.zip(completed).mapIndexed { index, (name, completed) ->
            newTestResult(index, name, completed)
        }.asSequence()
    }

    private fun printableSectionString(section: Section): String {
        return StringBuilder().apply {
            val delimiter = "-".repeat(55)
            appendln().appendln(delimiter).appendln(section.content).appendln(delimiter)
        }.toString()
    }

    private fun findTestAnnounced(section: Section) =
            testNamePattern
                    .findAll(section.content)
                    .map { it.destructured.component1() }

    private fun findTestCompleted(section: Section) =
            testResultPattern
                    .findAll(section.content)
                    .map(::newTestCompleted)
                    .filter { event -> event.count > 0 }

    private fun newTestResult(index: Int, name: String, event: TestCompleted) = TestResult(
            index = index,
            name = name,
            duration = event.duration,
            count = event.count,
            failures = event.failures,
            errors = event.errors,
            skipped = event.skipped
    )

    private fun newTestCompleted(m: MatchResult): TestCompleted {
        val (count, failures, errors, skipped, duration) = m.destructured
        return TestCompleted(duration = duration.toBigDecimal(),
                count = count.toInt(),
                failures = failures.toInt(),
                errors = errors.toInt(),
                skipped = skipped.toInt()
        )
    }
}

data class TestCompleted(
    val duration: BigDecimal,
    val count: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int
)

data class Section(
    val name: String,
    val content: String,
    val firstChar: Int,
    val lastChar: Int
)
