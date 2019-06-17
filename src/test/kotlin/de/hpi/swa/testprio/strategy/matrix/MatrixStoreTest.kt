package de.hpi.swa.testprio.strategy.matrix

import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import java.io.File
import java.lang.IllegalArgumentException

class MatrixStoreTest {

    @Test
    fun canReadExistingFormat() {
        val url = MatrixStoreTest::class.java.classLoader.getResource("cache/100274507.json") ?: throw IllegalArgumentException()

        val read = MatrixStore.read(File(url.toURI()))

        expectThat(read) {
            isNotNull().and {
                get { matrix }.hasSize(80)
            }
        }
    }
}