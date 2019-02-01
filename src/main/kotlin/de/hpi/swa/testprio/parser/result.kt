package de.hpi.swa.testprio.parser

import java.io.File
import java.math.BigDecimal

data class LogFile(
    val source: File,
    val travisBuildNumber: Long,
    val travisJobId: Long,
    val travisBuildId: Long?,
    val gitCommitId: String
) {

    companion object
}

data class TestResult(
    val name: String,
    val index: Int,
    val duration: BigDecimal,
    val count: Int,
    val failures: Int,
    val errors: Int,
    val skipped: Int
) {

    companion object

    val red get() = failures + errors
}

data class ParseResult(
    val source: LogFile,
    val testResults: List<TestResult>
)
