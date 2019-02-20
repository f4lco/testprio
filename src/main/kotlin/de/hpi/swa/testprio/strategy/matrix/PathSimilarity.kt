package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
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
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun apply(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)

        val priorities = sumMatrix.fileNames().associateWith { similarity(p, it) }

        return p.testResults.sortedByDescending { tc ->

            sumMatrix.matrix
                    .filterKeys { it.testName == tc.name }
                    .map { (priorities[it.key.fileName] ?: 0.0) * it.value.toDouble() }
                    .sum()
        }
    }

    private fun selectJobs(p: Params): List<String> = p.jobIds.subList(0, p.jobIndex)

    private fun similarity(p: Params, fileName: String): Double {
        return p.changedFiles.map { similarity.apply(it, fileName).toDouble() }.max() ?: 1.0
    }
}