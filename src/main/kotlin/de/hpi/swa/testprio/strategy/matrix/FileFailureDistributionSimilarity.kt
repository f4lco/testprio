package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy

/**
 * Prioritize TC of files whose failure distribution is similar to the files of the changeset.
 *
 * Deduce similarities for all rows (files) of the matrix by comparing the failure distribution
 * of a file to the most similar changed file. Then use the similarities as weights when
 * computing the final priorities.
 */
class FileFailureDistributionSimilarity(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)
        val priorities = priorities(p.changedFiles, p.testResults.map { it.name }, sumMatrix)
        return p.testResults.sortedByDescending { priorities[it.name] }
    }

    internal fun priorities(changedFiles: List<String>, tests: List<String>, sumMatrix: Matrix): Map<String, Double> {
        val fileToSimilarity = similarity(changedFiles, sumMatrix)

        return tests.associateWith { tc ->
            sumMatrix.matrix
                .filterKeys { it.testName == tc }
                .map { entry -> (fileToSimilarity[entry.key.fileName] ?: 0.0) * entry.value.toDouble() }
                .sum()
        }
    }

    private fun similarity(changedFiles: List<String>, m: Matrix): Map<String, Double> {
        val distances = m.fileNames().associateWith { distance(changedFiles, it, m) }
        val sum = distances.values.sum()
        return distances.mapValues { 1 - it.value / sum }
    }

    private fun distance(changedFiles: List<String>, f: String, m: Matrix): Double {
        return changedFiles.map { distance(f, it, m) }.min() ?: 1.0
    }

    private fun distance(f1: String, f2: String, m: Matrix): Double {
        return norm(f1, m).zip(norm(f2, m)).map { (a, b) -> squaredError(a, b) }.sum()
    }

    private fun norm(f: String, m: Matrix): List<Double> {
        val distribution = m.testDistribution(f)
        val sum = distribution.sum()
        return distribution.map { it / sum }
    }

    private fun squaredError(a: Double, b: Double) = Math.pow(a - b, 2.0)
}
