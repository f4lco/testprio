package de.hpi.swa.testprio.strategy.matrix

import com.google.common.hash.BloomFilter
import com.google.common.hash.Funnel
import com.google.common.hash.PrimitiveSink
import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import java.nio.charset.StandardCharsets
import java.util.SortedSet
import java.util.TreeSet

typealias FileSet = SortedSet<String>

/**
 * Per TC, keep one bloom filter, which remembers file sets that made the test red in the past.
 *
 * Conversely, to judge a new set of changes, prioritize TC whose bloom filter might contain the changed set of files.
 * The false positives of the bloom filter might to TC being prioritized, although the fileset did not cause failures
 * in the past. This can be seen as a sort of randomization.
 */
class Bloom(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer,
    val expectedInsertions: Int = 100
) : PrioritisationStrategy {

    private val unitMatrix = UnitMatrix(repository, cache)
    private val filters = mutableMapOf<String, BloomFilter<FileSet>>()

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = selectJobs(p).map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix(p.jobId, emptyMap()), reducer)
        val files: FileSet = TreeSet(p.changedFiles)

        val order: Map<TestResult, Int> = p.testResults.associateWith { tc ->
            if (test(tc, files)) {
                sumMatrix.matrix
                        .filterKeys { it.testName == tc.name }
                        .values.sum()
            } else 0
        }

        return p.testResults.sortedByDescending { order[it] }
    }

    override fun acceptFailedRun(p: Params) {
        update(p.testResults, p.changedFiles.toSortedSet())
    }

    private fun selectJobs(p: Params) = p.jobIds.subList(0, p.jobIndex)

    private fun test(tc: TestResult, files: FileSet): Boolean {
        val filter = filters[tc.name]
        return when (filter) {
            null -> true
            else -> filter.mightContain(files)
        }
    }

    private fun update(tc: List<TestResult>, files: FileSet) {
        tc.filter { it.red > 0 }.forEach {
            val filter = filters.computeIfAbsent(it.name) { BloomFilter.create(FileSetFunnel, expectedInsertions) }
            filter.put(files)
        }
    }
}

private object FileSetFunnel : Funnel<FileSet> {
    override fun funnel(from: FileSet, into: PrimitiveSink) {
        for (file in from) {
            into.putString(file, StandardCharsets.UTF_8)
        }
    }
}