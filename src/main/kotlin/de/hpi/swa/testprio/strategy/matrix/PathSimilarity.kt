package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritizationStrategy
import org.apache.commons.text.similarity.LongestCommonSubsequence
import org.apache.commons.text.similarity.SimilarityScore

/**
 * Assign matrix row weights by how similar the path names are w.r.t. to any of the changed files.
 *
 * This strategy improves the naive approach by assigning positive weights to files similar to the
 * changed files, measured by longest common subsequence.
 * Especially on Java projects with a nested package structure this strategy may prioritize TC which
 * treat the same or related components / features / topics, as the changeset at hand.
 */
class PathSimilarity(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val similarity: SimilarityScore<Int> = LongestCommonSubsequence()
) : PrioritizationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)
        val priorities = priorities(p.changedFiles, sumMatrix)

        return p.testResults.sortedByDescending { tc ->

            sumMatrix.filterKeys { it.testName == tc.name }
                    .map { (priorities[it.key.fileName] ?: 0.0) * it.value }
                    .sum()
        }
    }

    internal fun priorities(changedFiles: List<String>, m: Matrix): Map<String, Double> {
        return m.fileNames().associateWith { similarity(changedFiles, it) }
    }

    private fun similarity(changedFiles: List<String>, fileName: String): Double {
        return changedFiles.map { similarity.apply(it, fileName).toDouble() }.max() ?: 1.0
    }
}