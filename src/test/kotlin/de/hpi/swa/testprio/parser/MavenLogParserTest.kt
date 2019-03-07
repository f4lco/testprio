package de.hpi.swa.testprio.parser

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.filter
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import java.io.File

class MavenLogParserTest {

    @Test
    fun exampleLogFileNoModuleBanner() {
        val result = MavenLogParser.parseFile(find("6756_5b0897af57ca7765631a2e86da1fcf9d137d9adc_116837515.log"))

        expectThat(result.toList()) {
            hasSize(139)
            filter { it.red > 0 }.hasSize(1)
        }
    }

    @Test
    fun testResultAndNameInOneLine() {
        val result = MavenLogParser.parseFile(find("3710_6b6f5f917f6caf170136d5fdf9d022e1ecc37470_78417927.log"))

        expectThat(result.toList()) {
            hasSize(288)
            filter { it.red > 0 }.isEmpty()
        }
    }

    @Test
    fun testDurationParsingOnInterleavedTestResults() {
        val result = MavenLogParser.parseFile(find("5176_eee39ea7a547cfc11707e0c62ff558fb97e53c22_108786409.log"))

        expectThat(result.toList()) {
            hasSize(305)
            filter { it.red > 0 }.isEmpty()
        }
    }

    private fun find(fileName: String): LogFile {
        val url = javaClass.classLoader.getResource("example-logs/$fileName") ?: throw IllegalArgumentException(fileName)
        return File(url.toURI()).asTravisLogFile()
    }
}