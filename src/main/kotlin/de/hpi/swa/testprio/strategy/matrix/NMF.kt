package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.parser.TestResult
import de.hpi.swa.testprio.probe.Repository
import de.hpi.swa.testprio.strategy.Params
import de.hpi.swa.testprio.strategy.PrioritisationStrategy
import org.dulab.javanmf.algorithms.MatrixFactorization
import org.dulab.javanmf.algorithms.SingularValueDecomposition
import org.dulab.javanmf.updaterules.MUpdateRule
import org.jblas.DoubleMatrix
import java.io.File

class NMF(
    repository: Repository,
    cache: Cache,
    val reducer: Reducer
) : PrioritisationStrategy {

    companion object {
        const val components = 1
        const val tolerance = 1e-4
        const val iterations = 10_000

        init {
            org.jblas.util.Logger.getLogger().setLevel(org.jblas.util.Logger.WARNING)
        }
    }

    private val unitMatrix = UnitMatrix(repository, cache)

    override fun reorder(p: Params): List<TestResult> {
        val unitMatrices = p.priorJobs.map(unitMatrix::get)
        val sumMatrix = unitMatrices.fold(Matrix.empty(), reducer)

        if (sumMatrix.isEmpty()) {
            return p.testResults
        }

        val files = sumMatrix.keys.map { it.fileName }.toSortedSet()
        val tests = sumMatrix.keys.map { it.testName }.toSortedSet()

        val matrixX = sumMatrix.toJblas()
        val matrixW = DoubleMatrix(files.size, components)
        val matrixH = DoubleMatrix(components, tests.size)
        SingularValueDecomposition(matrixX).decompose(matrixW, matrixH)

        val updateRuleW = MUpdateRule(1.0, 0.0)
        val updateRuleH = MUpdateRule(0.0, 1.0)
        MatrixFactorization(updateRuleW, updateRuleH, tolerance, iterations).execute(matrixX, matrixW, matrixH)

        val fileIndices = p.changedFiles.map { files.indexOf(it) }.filter { it >= 0 }.toIntArray()
        if (fileIndices.isEmpty()) {
            return p.testResults
        }

        val result = matrixW[fileIndices].mmul(matrixH)
        val testPriorities = p.testResults.associateWith {
            val index = tests.indexOf(it.name)
            if (index >= 0) result[index] else 0.0
        }

        return p.testResults.sortedBy { testPriorities[it] }
    }
}

private fun Matrix.toJblas(): DoubleMatrix {
    val files = keys.map { it.fileName }.toSortedSet()
    val tests = keys.map { it.testName }.toSortedSet()
    val m = DoubleMatrix(files.size, tests.size)
    for (entry in this) {
        val rowIndex = files.indexOf(entry.key.fileName)
        val columnIndex = tests.indexOf(entry.key.testName)
        m.put(rowIndex, columnIndex, entry.value)
    }
    return m
}

fun main(args: Array<String>) {
    val path = args[0]
    val m: Matrix = MatrixStore.read(File(path))!!
    val files = m.keys.map { it.fileName }.toSortedSet()
    val testCases = m.keys.map { it.testName }.toSortedSet()
    println("Loaded $path")
    println("${files.size} files and ${testCases.size} test cases")

    val rows = mutableListOf<DoubleArray>()
    for (fileName in files) {
        val row = testCases.map {
            testName -> m[Key(fileName, testName)] ?: 0.0
        }.toDoubleArray()

        rows.add(row)
    }

    val matrixX = DoubleMatrix(rows.toTypedArray())

    val components = 1

    val matrixW = DoubleMatrix(files.size, components)
    val matrixH = DoubleMatrix(components, testCases.size)
    SingularValueDecomposition(matrixX).decompose(matrixW, matrixH)

    val updateRuleW = MUpdateRule(1.0, 0.0)
    val updateRuleH = MUpdateRule(0.0, 1.0)
    MatrixFactorization(updateRuleW, updateRuleH, 1e-4, 10_000).execute(matrixX, matrixW, matrixH)

    for ((index, testName) in testCases.withIndex()) {
        print("$testName ${matrixH[index]} ")
        val relevant = m.filterKeys { it.testName == testName }.keys.map { it.fileName }.toSortedSet()
        println(relevant)
    }
    println()

    for ((index, fileName) in files.withIndex()) {
        print("$fileName ${matrixW[index]}")
        val relevant = m.keys.filter { it.fileName == fileName }.map { it.testName }.toSortedSet()
        println(relevant)
    }

    println("done")
}