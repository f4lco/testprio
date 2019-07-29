package de.hpi.swa.testprio.strategy

import de.hpi.swa.testprio.parser.TestResult
import org.apache.commons.text.similarity.LongestCommonSubsequence
import org.apache.commons.text.similarity.SimilarityScore

/**
 * Prioritize TC with names which appear in the paths of the changed files.
 *
 * This may help identify naming patterns such as: subject-under-test _Foo_, covered by _FooTest_.
 * Given that _Foo_ changed, it might be good to prioritize _FooTest_ and _FooBarTest_, but not
 * _BarTest_.
 */
class PathTestCaseOverlap(val similarity: SimilarityScore<Int> = LongestCommonSubsequence()) : PrioritizationStrategy {

    private val nonWordChars = "\\W".toRegex()

    override fun reorder(p: Params): List<TestResult> {
        return p.testResults.sortedByDescending {
            overlap(p.changedFiles, it.name)
        }
    }

    private fun overlap(fileNames: List<String>, tc: String): Int {
        return fileNames.map { overlap(it, tc) }.max() ?: 0
    }

    private fun overlap(fileName: String, tc: String): Int {
        val parts: List<String> = fileName.split('/')
        val normalizedTestName = normalize(tc)
        return parts.sumBy { similarity.apply(normalize(it), normalizedTestName) }
    }

    private fun normalize(s: String) = s.replace(nonWordChars, "").toLowerCase()
}