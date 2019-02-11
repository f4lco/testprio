package de.hpi.swa.testprio.strategy.matrix

import kotlin.math.roundToInt

internal typealias Reducer = (Matrix, Matrix) -> Matrix

object CountingReducer : Reducer {
    override fun invoke(left: Matrix, right: Matrix) =
            Matrix(right.jobId, (left.matrix.keys + right.matrix.keys).associateWith {
                (left.matrix[it] ?: 0) + (right.matrix[it] ?: 0)
            })
}

class DevaluationReducer(val alpha: Double) : Reducer {

    override fun invoke(left: Matrix, right: Matrix) =
            Matrix(right.jobId, (left.matrix.keys + right.matrix.keys).associateWith {
                (alpha * (left.matrix[it] ?: 0)).roundToInt() + (right.matrix[it] ?: 0)
            })
}