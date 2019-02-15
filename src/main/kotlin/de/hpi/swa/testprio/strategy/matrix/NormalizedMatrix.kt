package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

class NormalizedMatrix(
    repository: Repository,
    cache: Cache,
    val prior: Double,
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(cache, repository)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val fileToSimilarity: Map<String, Double> = sumMatrix.fileNames().associateWith { fileName ->
            similarity(p, fileName, sumMatrix)
        }

        return p.testResults.sortedByDescending { tc ->
            sumMatrix.matrix
                    .filterKeys { it.testName == tc.name }
                    .map { entry -> (fileToSimilarity[entry.key.fileName] ?: 0.0) * entry.value.toDouble() }
                    .sum()
        }
    }

    private fun selectJobs(p: Params) = p.jobIds.subList(0, p.jobIndex)

    private fun similarity(p: Params, fileName: String, m: Matrix): Double = p.changedFiles.map { similarity(fileName, it, m) }.max() ?: 1.0

    private fun similarity(f1: String, f2: String, m: Matrix): Double {
        val distance = norm(f1, m).toMutableMap()

        for (entry in norm(f2, m)) {
            distance.merge(entry.key, entry.value, ::squaredError)
        }

        val sum = distance.values.sum()
        return when (sum) {
            0.0 -> 1.0
            else -> 1.0 / sum
        }
    }

    private fun norm(f: String, m: Matrix): Map<String, Double> {
        val allTC: Set<String> = m.matrix.keys.map { it.testName }.toSet()

        val norms = allTC.associateWith { tc ->
            val failureCount = (m.matrix[Key(f, tc)] ?: 0).toDouble()
            prior + failureCount
        }

        val normalizer = norms.values.sum()
        return norms.mapValues { entry -> entry.value / normalizer }
    }

    private fun squaredError(a: Double, b: Double) = Math.pow(a - b, 2.0)
}