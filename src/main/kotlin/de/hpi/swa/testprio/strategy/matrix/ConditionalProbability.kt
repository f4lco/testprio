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
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)
        val probabilities = probabilities(p.changedFiles, p.testResults.map { it.name }, sumMatrix)
        return p.testResults.sortedByDescending { probabilities[it.name] }
    }

    internal fun probabilities(changedFiles: List<String>, tests: List<String>, sumMatrix: Matrix): Map<String, Double> {
        val fileCounts = fileCounts(sumMatrix)
        return tests.associateWith { tc ->
            prob(changedFiles, sumMatrix, fileCounts, tc)
        }
    }

    private fun fileCounts(m: Matrix): Map<String, Pair<Double, Double>> {
        val counts = mutableMapOf<String, Double>()
        for (entry in m) {
            counts.merge(entry.key.fileName, entry.value.toDouble(), Double::plus)
        }

        val sum = counts.values.sum()
        return counts.mapValues { entry ->
            entry.value to (entry.value / sum)
        }
    }

    private fun prob(
        changedFiles: List<String>,
        m: Matrix,
        fileCounts: Map<String, Pair<Double, Double>>,
        tc: String
    ): Double {

        return changedFiles.map { file ->
            val (fileCount, fileProbability) = fileCounts[file] ?: Pair(0.0, 0.0)
            val count = m[Key(file, tc)] ?: 0
            fileProbability * (count / fileCount)
        }.sum()
    }
}