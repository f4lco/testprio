package de.hpi.swa.testprio.parser

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import org.apache.commons.csv.CSVRecord
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object CsvOutput {

    fun write(results: Sequence<ParseResult>, to: File) {
        CSVPrinter(FileWriter(to), CSVFormat.DEFAULT).use {
            printHeader(it)
            printRecords(results, it)
        }
    }

    fun writeSeq(results: Sequence<Pair<String, List<TestResult>>>, to: File) {
        CSVPrinter(FileWriter(to), CSVFormat.DEFAULT).use {
            printHeader(it)
            for (tr in results) {
                for (t in tr.second) {
                    it.print(tr.first)
                    t.printCsv(it)
                    it.println()
                }
            }
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

    private fun LogFile.Companion.printCsvHeader(to: CSVPrinter) = with(to) {
        print("travisJobId")
    }

    private fun LogFile.printCsv(to: CSVPrinter) = with(to) {
        print(travisJobId)
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
}
