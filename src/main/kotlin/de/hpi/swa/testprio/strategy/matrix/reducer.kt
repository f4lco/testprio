package de.hpi.swa.testprio.strategy.matrix

internal typealias Reducer = (Matrix, Matrix) -> Matrix

object CountingReducer : Reducer {
    override fun invoke(left: Matrix, right: Matrix) =
            Matrix((left.keys + right.keys).associateWith {
                (left[it] ?: 0.0) + (right[it] ?: 0.0)
            })
}

class DevaluationReducer(val alpha: Double) : Reducer {

    override fun invoke(left: Matrix, right: Matrix): Matrix {
        val m = mutableMapOf<Key, Double>()
        val commonFiles = left.fileNames.intersect(right.fileNames)

        for (entry in left) {
            val factor = if (entry.key.fileName in commonFiles) (1 - alpha) else 1.0
            m[entry.key] = factor * entry.value
        }

        for (entry in right) {
            val factor = if (entry.key.fileName in commonFiles) alpha else 1.0
            m.merge(entry.key, factor * entry.value, Double::plus)
        }

        return Matrix(m)
    }
}