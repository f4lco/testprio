
import de.hpi.swa.testprio.parser.TestResult
import strikt.api.Assertion
import strikt.assertions.containsExactly
import strikt.assertions.map

fun <T : Iterable<TestResult>> Assertion.Builder<T>.hasTestOrder(vararg names: String) =
            compose("has test order ${names.joinToString(" ➟ ")}") {
                map(TestResult::name).containsExactly(names.toList())
            } then {
                if (allPassed) pass() else fail()
            }
