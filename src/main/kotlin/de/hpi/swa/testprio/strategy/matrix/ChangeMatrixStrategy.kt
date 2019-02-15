package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class ChangeMatrixStrategy(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val windowSize: Int = 100
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(cache, repository)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobsByWindowSize(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        return p.testResults.sortedByDescending { test ->

            sumMatrix.matrix.filterKeys { key ->
                key.testName == test.name && key.fileName in p.changedFiles
            }.values.sum()
        }
    }

    private fun selectJobsByWindowSize(p: Params): Sequence<String> {
        val end = p.jobIds.indexOf(p.jobId)
        if (end == -1) throw IllegalArgumentException(p.jobId)
        val begin = when (windowSize) {
            -1 -> 0
            else -> Math.max(end - windowSize, 0)
        }
        return p.jobIds.subList(begin, end).asSequence()
    }
}