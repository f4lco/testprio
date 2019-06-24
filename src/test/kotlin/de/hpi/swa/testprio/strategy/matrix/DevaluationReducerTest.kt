package de.hpi.swa.testprio.strategy.matrix

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import print
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class DevaluationReducerTest {

    private val alpha = 0.5
    private lateinit var reducer: Reducer

    @BeforeEach
    fun setUp() {
        reducer = DevaluationReducer(alpha)
    }

    @Test
    fun noOverlap() {
        val m = mapOf(Key("F1", "T1") to 1).toMatrix()
        val n = mapOf(Key("F2", "T2") to 2).toMatrix()

        val reduced = reducer(m, n)

        expectThat(reduced).isEqualTo(Matrix(m + n))
    }

    @Test
    fun oneOverlap() {
        val m = mapOf(Key("F1", "T1") to 6).toMatrix()
        val n = mapOf(Key("F1", "T1") to 2).toMatrix()

        val reduced = reducer(m, n)

        expectThat(reduced).isEqualTo(mapOf(Key("F1", "T1") to 4).toMatrix())
    }

    @Test
    fun entireFileHistoryDevalued() {
        val m = mapOf(
                Key("F1", "T1") to 6,
                Key("F1", "T2") to 10
        ).toMatrix()

        val n = mapOf(Key("F1", "T1") to 1).toMatrix()

        val reduced = reducer(m, n)

        expectThat(reduced).isEqualTo(
            Matrix(mapOf(Key("F1", "T1") to 3.5,
                    Key("F1", "T2") to 5.0))
        )
    }

    private fun Map<Key, Int>.toMatrix() = Matrix(this.mapValues { it.value.toDouble() })

    @Test
    fun testExample() {
        val m = mapOf(
            Key("F0", "T0") to 3,
            Key("F0", "T1") to 2,

            Key("F1", "T0") to 1,
            Key("F1", "T1") to 4
        )

        val n = mapOf(
            Key("F0", "T1") to 6,
            Key("F0", "T2") to 24,

            Key("F1", "T1") to 5,
            Key("F1", "T2") to 6
        )

        val reduced = DevaluationReducer(alpha = 0.8)(m.toMatrix(), n.toMatrix())

        reduced.print()
    }
}