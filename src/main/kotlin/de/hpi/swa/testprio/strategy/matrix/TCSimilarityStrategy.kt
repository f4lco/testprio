package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class TCSimilarityStrategy(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val tc: Set<String> = collectTC(p, sumMatrix)
        return p.testResults.sortedBy {
            distance(sumMatrix, tc, it.name)
        }
    }

    private fun selectJobs(p: Params): List<String> {
        val end = p.jobIds.indexOf(p.jobId)
        if (end == -1) throw IllegalArgumentException(p.jobId)
        return p.jobIds.subList(0, end)
    }

    private fun collectTC(p: Params, m: Matrix): Set<String> {
        return m.matrix.keys
                .filter { it.fileName in p.changedFiles }
                .map { it.testName }
                .toSet()
    }

    private fun distance(m: Matrix, relevantTC: Set<String>, tc: String): Int {
        return relevantTC.map { distance(m, it, tc) }.min() ?: 0
    }

    private fun distance(m: Matrix, tc1: String, tc2: String): Int {
        val files = m.matrix.keys.filter { it.testName == tc1 || it.testName == tc2 }.map { it.fileName }.toSet()
        return files.sumBy { file ->
            val v1 = m.matrix[Key(file, tc1)] ?: 0
            val v2 = m.matrix[Key(file, tc2)] ?: 0
            val distance = v2 - v1
            distance * distance
        }
    }
}