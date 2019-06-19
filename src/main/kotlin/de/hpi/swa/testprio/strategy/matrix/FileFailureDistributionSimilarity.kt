package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import kotlin.math.pow
import kotlin.math.sqrt

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
        val priorities = priorities(p.changedFiles, sumMatrix)
        return p.testResults.sortedByDescending { priorities[it.name] }
    }

    internal fun priorities(changedFiles: List<String>, matrix: Matrix): Map<String, Double> {
        val fileToSimilarity = similarity(changedFiles, matrix)

        return matrix.testNames().associateWith { tc ->
            matrix.filterKeys { it.testName == tc }
                .map { entry -> (fileToSimilarity[entry.key.fileName] ?: 0.0) * entry.value }
                .sum()
        }
    }

    private fun similarity(changedFiles: List<String>, m: Matrix): Map<String, Double> = m.fileNames().associateWith { file ->
            changedFiles.parallelStream().mapToDouble { changedFile ->

                val a = m.testDistribution(changedFile)
                val b = m.testDistribution(file)
                var dot = 0.0
                var normA = 0.0
                var normB = 0.0

                for ((va, vb) in a.zip(b)) {
                    dot += va * vb
                    normA += va.pow(2.0)
                    normB += vb.pow(2.0)
                }

                dot / (sqrt(normA) * sqrt(normB))
            }.max().orElse(0.0)
        }
}
