package de.hpi.swa.testprio.strategy.matrix

import kotlinx.serialization.Serializable

@Serializable
data class Matrix(val jobId: String, val matrix: Map<Key, Int>)

fun Matrix.fileNames() = matrix.keys.map { it.fileName }.toSet()

@Serializable
data class Key(val fileName: String, val testName: String)