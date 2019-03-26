package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader

class TestRepository : Repository {

    val changedFiles = mutableMapOf<String, MutableList<String>>()

    val testResults = mutableMapOf<String, MutableList<TestResult>>()

    fun jobs() = (changedFiles.keys + testResults.keys).toList().sorted()

    override fun redJobs(projectName: String) = TODO()

    override fun changedFiles(jobId: String): MutableList<String> = changedFiles.computeIfAbsent(jobId) { mutableListOf() }

    fun loadChangedFiles(fileName: String) {
        val file = File(javaClass.classLoader.getResource("patches/$fileName").toURI())

        CSVParser(FileReader(file), CSVFormat.DEFAULT.withFirstRecordAsHeader()).use {
            for (record in it) {
                changedFiles(record["travisJobId"]).add(record["name"])
            }
        }
    }

    override fun testResults(jobId: String) = testResults.computeIfAbsent(jobId) { mutableListOf() }

    fun loadTestResult(fileName: String) {
        val file = File(javaClass.classLoader.getResource("test-results/$fileName").toURI())

        fun newTestResult(record: CSVRecord) = TestResult(
                name = record["testName"],
                index = record["index"].toInt(),
                duration = record["duration"].toBigDecimal(),
                count = record["count"].toInt(),
                failures = record["failures"].toInt(),
                errors = record["errors"].toInt(),
                skipped = record["skipped"].toInt()
        )

        CSVParser(FileReader(file), CSVFormat.DEFAULT.withFirstRecordAsHeader()).use {
            for (record in it) {
                testResults(record["travisJobId"]).add(newTestResult(record))
            }
        }
    }
}