package de.hpi.swa.testprio.parser

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.filter
import strikt.assertions.isNotEmpty
import java.io.File

class MavenLogParserTest {

    @Test
    fun exampleLogFileNoModuleBanner() {
        val result = MavenLogParser.parseFile(find("6756_5b0897af57ca7765631a2e86da1fcf9d137d9adc_116837515.log"))

        expectThat(result.toList()) {
            isNotEmpty()
            filter { it.red > 0 }.isNotEmpty()
        }
    }

    private fun find(fileName: String): LogFile {
        val url = javaClass.classLoader.getResource("example-logs/$fileName") ?: throw IllegalArgumentException(fileName)
        return File(url.toURI()).asTravisLogFile()
    }
}