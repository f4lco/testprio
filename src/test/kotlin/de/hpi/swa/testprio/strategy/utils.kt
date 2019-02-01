
import de.hpi.swa.testprio.parser.TestResult
import com.winterbe.expekt.should

fun newTestResult(name: String, index: Int = 0, failures: Int = 0) = TestResult(
        name = name,
        index = index,
        duration = 0.42.toBigDecimal(),
        count = 1,
        failures = failures,
        errors = 0,
        skipped = 0
)

fun assertTestOrder(actual: List<TestResult>, vararg names: String) {
    val actualNames = actual.map { it.name }
    actualNames.should.equal(names.toList())
}