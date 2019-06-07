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
        val m = mapOf(Key("F1", "T1") to 1).toMatrix("A")
        val n = mapOf(Key("F2", "T2") to 2).toMatrix("B")

        val reduced = reducer(m, n)

        expectThat(reduced.matrix).isEqualTo(m.matrix + n.matrix)
    }

    @Test
    fun oneOverlap() {
        val m = mapOf(Key("F1", "T1") to 6).toMatrix("A")
        val n = mapOf(Key("F1", "T1") to 2).toMatrix("B")

        val reduced = reducer(m, n)

        expectThat(reduced.matrix).isEqualTo(mapOf(Key("F1", "T1") to 4))
    }

    @Test
    fun entireFileHistoryDevalued() {
        val m = mapOf(
                Key("F1", "T1") to 6,
                Key("F1", "T2") to 10
        ).toMatrix("A")

        val n = mapOf(Key("F1", "T1") to 1).toMatrix("B")

        val reduced = reducer(m, n)

        expectThat(reduced.matrix).isEqualTo(
                mapOf(
                        Key("F1", "T1") to 4,
                        Key("F1", "T2") to 5)
        )
    }

    private fun Map<Key, Int>.toMatrix(job: String) = Matrix(job, this)

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

        val reduced = DevaluationReducer(alpha = 0.8)(Matrix("M", m), Matrix("N", n))

        reduced.print()
    }
}