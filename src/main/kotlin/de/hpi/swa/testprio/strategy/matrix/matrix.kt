package de.hpi.swa.testprio.strategy.matrix

import kotlinx.serialization.Serializable

@Serializable
data class Matrix(private val matrix: Map<Key, Double>) : Map<Key, Double> by matrix {
    companion object
}

fun Matrix.Companion.empty() = Matrix(emptyMap())

fun Matrix.fileNames() = keys.map { it.fileName }.toSortedSet()

fun Matrix.fileDistribution(tc: String): DoubleArray {
    val files = fileNames()
    val distribution = DoubleArray(files.size)
    for ((index, file) in files.withIndex()) {
        distribution[index] = this[Key(file, tc)] ?: 0.0
    }
    return distribution
}

fun Matrix.testNames() = keys.map { it.testName }.toSortedSet()

fun Matrix.testDistribution(f: String): DoubleArray {
    val tests = testNames()
    val distribution = DoubleArray(tests.size)
    for ((index, test) in tests.withIndex()) {
        distribution[index] = this[Key(f, test)] ?: 0.0
    }
    return distribution
}

@Serializable
data class Key(val fileName: String, val testName: String)