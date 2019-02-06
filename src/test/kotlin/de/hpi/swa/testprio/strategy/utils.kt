
import de.hpi.swa.testprio.parser.TestResult
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.map

fun newTestResult(name: String, index: Int = 0, failures: Int = 0) = TestResult(
        name = name,
        index = index,
        duration = 0.42.toBigDecimal(),
        count = 1,
        failures = failures,
        errors = 0,
        skipped = 0
)

fun <T : Iterable<TestResult>> Assertion.Builder<T>.hasTestOrder(vararg names: String) =
            compose("has test order ${names.joinToString(" ➟ ")}") {
                map(TestResult::name).containsExactly(names.toList())
            } then {
                if (allPassed) pass() else fail()
            }
