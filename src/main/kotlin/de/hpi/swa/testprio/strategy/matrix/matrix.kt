package de.hpi.swa.testprio.strategy.matrix

import kotlinx.serialization.Serializable

@Serializable
data class Matrix(val jobId: String, val matrix: Map<Key, Int>)

@Serializable
data class Key(val fileName: String, val testName: String)