package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import org.apache.commons.text.similarity.LongestCommonSubsequence
import org.apache.commons.text.similarity.SimilarityScore

class PathTCOverlapStrategy(val similarity: SimilarityScore<Int> = LongestCommonSubsequence()) : PrioritisationStrategy {

    override fun apply(p: Params): List<TestResult> {
        return p.testResults.sortedByDescending {
            overlap(p.changedFiles, it.name)
        }
    }

    private fun overlap(fileNames: List<String>, tc: String): Int {
        return fileNames.map { overlap(it, tc) }.max() ?: 0
    }

    private fun overlap(fileName: String, tc: String): Int {
        val parts: List<String> = fileName.split('/')
        return parts.sumBy { similarity.apply(it.toLowerCase(), tc.toLowerCase()) }
    }
}