package de.hpi.swa.testprio.strategy.matrix

import de.hpi.swa.testprio.strategy.matrix.ChangeMatrixStrategy.Matrix
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File

object MatrixStore {

    private val logger = KotlinLogging.logger {}

    fun write(matrix: Matrix, file: File) {
        logger.debug { "Writing $file" }
        file.writeText(Json.stringify(ChangeMatrixStrategy.Matrix.serializer(), matrix))
    }

    fun read(file: File) = if (file.exists()) {
        logger.debug { "Loading $file" }
        Json.parse(ChangeMatrixStrategy.Matrix.serializer(), file.readText())
    } else null
}
