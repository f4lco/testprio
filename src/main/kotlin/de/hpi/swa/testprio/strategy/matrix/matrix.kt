package de.hpi.swa.testprio.strategy.matrix

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Matrix(private val matrix: Map<Key, Double>) : Map<Key, Double> by matrix {

    @Transient
    val fileNames by lazy {
        keys.map { it.fileName }.toSortedSet()
    }

    @Transient
    val testNames by lazy {
        keys.map { it.testName }.toSortedSet()
    }

    companion object
}

fun Matrix.Companion.empty() = Matrix(emptyMap())

fun Matrix.fileNames() = fileNames

fun Matrix.fileDistribution(tc: String): DoubleArray {
    val files = fileNames()
    val distribution = DoubleArray(files.size)
    for ((index, file) in files.withIndex()) {
        distribution[index] = this[Key(file, tc)] ?: 0.0
    }
    return distribution
}

fun Matrix.testNames() = testNames

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