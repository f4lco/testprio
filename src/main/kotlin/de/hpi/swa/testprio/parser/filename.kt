package de.hpi.swa.testprio.parser

import java.io.File
import java.lang.IllegalArgumentException

fun File.asTravisLogFile(): LogFile {
    return fromFile(this)
}

fun LogFile.Companion.of(f: File) = fromFile(f)

private fun fromFile(f: File): LogFile {
    val parts = f.name.removeSuffix(".log").split("_")
    return when (parts.size) {

        3 -> {
            val (buildNumber, commit, jobId) = parts
            LogFile(source = f,
                    travisBuildNumber = buildNumber.toLong(),
                    travisJobId = jobId.toLong(),
                    travisBuildId = null,
                    gitCommitId = commit)
        }

        4 -> {
            val (buildNumber, buildId, commit, jobId) = parts
            LogFile(source = f,
                    travisBuildNumber = buildNumber.toLong(),
                    travisJobId = jobId.toLong(),
                    travisBuildId = buildId.toLong(),
                    gitCommitId = commit)
        }

        else -> throw IllegalArgumentException(f.name)
    }
}