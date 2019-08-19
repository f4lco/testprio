
import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Job
import de.hpi.swa.testprio.strategy.matrix.Key
import de.hpi.swa.testprio.strategy.matrix.Matrix
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.map
import java.time.LocalDate
import java.time.ZoneOffset

fun jobWithId(id: Int): Job {
    val begin = LocalDate.of(2019, 1, 1).atStartOfDay()
    val end = begin.plusSeconds(10)
    return Job(id, id, id, id, begin.toInstant(ZoneOffset.UTC), end.toInstant(ZoneOffset.UTC))
}

fun <T : Iterable<TestResult>> Assertion.Builder<T>.hasTestOrder(vararg names: String) =
            compose("has test order ${names.joinToString(" ➟ ")}") {
                map(TestResult::name).containsExactly(names.toList())
            } then {
                if (allPassed) pass() else fail()
            }

fun Matrix.print() {
    print(tableString())
}

fun Matrix.tableString(n: Int = 6): String = StringBuilder(128).apply {
    val tc = keys.map { it.testName }.toSortedSet()

    append(" ".repeat(n))
    for (name in tc) {
        append(name.padStart(n))
    }
    appendln()

    val files = keys.map { it.fileName }.toSortedSet()
    for (name in files) {
        append(name.padStart(n))
        for (t in tc) {
            val value = this@tableString[Key(name, t)] ?: 0.0
            append("%.2f".format(value).padStart(n))
        }
        appendln()
    }
}.toString()