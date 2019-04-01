package de.hpi.swa.testprio.parser

import de.hpi.swa.testprio.probe.Job
import mu.KotlinLogging
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.File
import java.io.FileWriter

private val LOG = KotlinLogging.logger {}

object ParseResultOutput {
    fun write(results: Sequence<ParseResult>, to: File) {
        ensureParentExists(to)
        CSVPrinter(FileWriter(to), CSVFormat.DEFAULT).use {
            printHeader(it)
            printRecords(results, it)
        }
    }

    private fun printHeader(to: CSVPrinter) {
        LogFile.printCsvHeader(to)
        TestResult.printCsvHeader(to)
        to.println()
    }

    private fun printRecords(results: Sequence<ParseResult>, to: CSVPrinter) {
        for (r in results) {
            for (t in r.testResults) {
                r.source.printCsv(to)
                t.printCsv(to)
                to.println()
            }
        }
    }
}

object PrioritizationResultOutput {

    fun write(results: Sequence<Pair<Job, List<TestResult>>>, to: File) {
        ensureParentExists(to)
        CSVPrinter(FileWriter(to), CSVFormat.DEFAULT).use {
            printHeader(it)
            printRecords(results, it)
        }
    }

    private fun printHeader(to: CSVPrinter) {
        Job.printCsvHeader(to)
        TestResult.printCsvHeader(to)
        to.println()
    }

    private fun printRecords(results: Sequence<Pair<Job, List<TestResult>>>, to: CSVPrinter) {
        for ((job, testResults) in results) {
            for (tc in testResults) {
                job.printCsv(to)
                tc.printCsv(to)
                to.println()
            }
        }
    }
}

private fun ensureParentExists(f: File) = f.parentFile?.let { parent ->
    if (!parent.exists() && !parent.mkdirs()) {
        LOG.error("Cannot create parent directory: {}", parent)
    }
}

private fun LogFile.Companion.printCsvHeader(to: CSVPrinter) = with(to) {
    print("travisJobId")
}

private fun LogFile.printCsv(to: CSVPrinter) = with(to) {
    print(travisJobId)
}

private fun Job.Companion.printCsvHeader(to: CSVPrinter) = with(to) {
    print("travisBuildNumber")
    print("travisBuildId")
    print("travisJobId")
}

private fun Job.printCsv(to: CSVPrinter) = with(to) {
    print(buildNumber)
    print(build)
    print(job)
}

private fun TestResult.Companion.printCsvHeader(to: CSVPrinter) = with(to) {
    print("testName")
    print("index")
    print("duration")
    print("count")
    print("failures")
    print("errors")
    print("skipped")
}

private fun TestResult.printCsv(to: CSVPrinter) = with(to) {
    print(name)
    print(index)
    print(duration)
    print(count)
    print(failures)
    print(errors)
    print(skipped)
}