package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.strategy.matrix.Key
import de.hpi.swa.testprio.strategy.matrix.Matrix

const val DEFAULT_DURATION = 42.0

fun TestResult.Companion.successful(testName: String) = TestResult(testName,
            index = -1,
            duration = DEFAULT_DURATION.toBigDecimal(),
            count = 10,
            failures = 0,
            errors = 0,
            skipped = 0)

fun TestResult.Companion.failed(testName: String) = TestResult(testName,
        index = -1,
        duration = DEFAULT_DURATION.toBigDecimal(),
        count = 10,
        failures = 3,
        errors = 1,
        skipped = 0)

object Fixtures {

    fun matrixOne() = Matrix(
            mapOf(
                Key("F1", "T1") to 2.0,
                Key("F1", "T2") to 3.0,
                Key("F1", "T3") to 5.0,

                Key("F2", "T1") to 20.0,
                Key("F2", "T2") to 10.0))

    fun repeatedFailure() = revisions {

        job {
            changedFiles("Car.java")
            successful("T1")
            failed("T2")
        }

        job {
            changedFiles("Car.java")
            successful("T1")
            failed("T2")
        }
    }

    fun similarTests() = revisions {

        job {
            changedFiles("F1")
            failed("A", 3)
            failed("B", 2)
            failed("C", 1)
        }

        job {
            changedFiles("F2")
            failed("A", 4)
            failed("B", 3)
            failed("C", 2)
        }

        job {
            changedFiles("F3")
            successful("A", "B")
            failed("C", 3)
        }

        job {
            changedFiles("F3")
            successful("A", "B", "C")
        }
    }
}
