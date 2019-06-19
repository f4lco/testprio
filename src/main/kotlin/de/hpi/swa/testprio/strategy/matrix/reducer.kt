package de.hpi.swa.testprio.strategy.matrix

import kotlin.math.roundToInt

internal typealias Reducer = (Matrix, Matrix) -> Matrix

object CountingReducer : Reducer {
    override fun invoke(left: Matrix, right: Matrix) =
            Matrix((left.keys + right.keys).associateWith {
                (left[it] ?: 0) + (right[it] ?: 0)
            })
}

class DevaluationReducer(val alpha: Double) : Reducer {

    override fun invoke(left: Matrix, right: Matrix): Matrix {
        val m = mutableMapOf<Key, Double>()
        val newFiles = right.fileNames()

        for (entry in left) {
            m[entry.key] = if (entry.key.fileName in newFiles) {
                ((1 - alpha) * entry.value)
            } else entry.value.toDouble()
        }

        for (entry in right) {
            if (entry.key in left) {
                m.merge(entry.key, alpha * entry.value, Double::plus)
            } else {
                m[entry.key] = entry.value.toDouble()
            }
        }

        return Matrix(m.mapValues { it.value.roundToInt() })
    }
}