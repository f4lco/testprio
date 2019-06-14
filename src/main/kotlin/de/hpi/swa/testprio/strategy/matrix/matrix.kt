package de.hpi.swa.testprio.strategy.matrix

import kotlinx.serialization.Serializable

@Serializable
data class Matrix(val matrix: Map<Key, Int>) {
    companion object
}

fun Matrix.Companion.empty() = Matrix(emptyMap())

fun Matrix.fileNames() = matrix.keys.map { it.fileName }.toSortedSet()

fun Matrix.fileDistribution(tc: String): DoubleArray {
    val files = fileNames()
    val distribution = DoubleArray(files.size)
    for ((index, file) in files.withIndex()) {
        distribution[index] = matrix[Key(file, tc)]?.toDouble() ?: 0.0
    }
    return distribution
}

fun Matrix.testNames() = matrix.keys.map { it.testName }.toSortedSet()

fun Matrix.testDistribution(f: String): DoubleArray {
    val tests = testNames()
    val distribution = DoubleArray(tests.size)
    for ((index, test) in tests.withIndex()) {
        distribution[index] = matrix[Key(f, test)]?.toDouble() ?: 0.0
    }
    return distribution
}

@Serializable
data class Key(val fileName: String, val testName: String)