package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class ConditionalProbability(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val fileCounts = fileCounts(sumMatrix)

        val probabilities = p.testResults.associateWith { tc ->
            prob(p, sumMatrix, fileCounts, tc)
        }

        return p.testResults.sortedByDescending { probabilities[it] }
    }

    private fun selectJobs(p: Params) = p.jobIds.subList(0, p.jobIndex)

    private fun fileCounts(m: Matrix): Map<String, Pair<Double, Double>> {
        val counts = mutableMapOf<String, Double>()
        for (entry in m.matrix) {
            counts.merge(entry.key.fileName, entry.value.toDouble(), Double::plus)
        }

        val sum = counts.values.sum()
        return counts.mapValues { entry ->
            entry.value to (entry.value / sum)
        }
    }

    private fun prob(
        p: Params,
        m: Matrix,
        fileCounts: Map<String, Pair<Double, Double>>,
        tc: TestResult
    ): Double {

        return p.changedFiles.map { file ->
            val (fileCount, fileProbability) = fileCounts[file] ?: Pair(0.0, 0.0)
            val count = m.matrix[Key(file, tc.name)] ?: 0
            fileProbability * (count / fileCount)
        }.sum()
    }
}