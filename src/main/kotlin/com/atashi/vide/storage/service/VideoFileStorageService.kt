package com.atashi.vide.storage.service

import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicLong

@Service
class VideoFileStorageService {

    companion object {
        private const val SNOWFLAKE_EPOCH = 1720000000000L
        private const val WORKER_ID = 1L
        private const val SEQUENCE_BITS = 12L
        private const val SEQUENCE_MASK = (1L shl SEQUENCE_BITS.toInt()) - 1L
        private val SEQUENCE = AtomicLong(0L)
    }

    fun buildTypeDirectory(rootDirectory: String, vType: String): Path {
        val safeRoot = rootDirectory.trim().ifEmpty { throw IllegalArgumentException("v_file root directory cannot be empty") }
        val safeType = vType.trim().ifEmpty { throw IllegalArgumentException("v_type cannot be empty") }
        return Paths.get(safeRoot).toAbsolutePath().normalize().resolve(safeType).normalize()
    }

    fun classifyFiles(
        rootDirectory: String,
        currentDirectory: String,
        vType: String,
        selectedFiles: List<String>,
        vSeries: String? = null,
        vSeason: String? = null,
        vNumber: String? = null
    ): List<String> {
        val targetDirectory = if (currentDirectory.isNotBlank()) {
            buildTypeDirectory(currentDirectory, vType)
        } else {
            buildTypeDirectory(rootDirectory, vType)
        }
        Files.createDirectories(targetDirectory)

        val sourceDirectory = Paths.get(currentDirectory).toAbsolutePath().normalize()
        val movedFiles = mutableListOf<String>()

        selectedFiles
            .filter { it.isNotBlank() }
            .forEach { fileName ->
                val sourceFile = sourceDirectory.resolve(fileName).normalize()
                val targetName = buildTargetFileName(fileName, vSeries, vSeason, vNumber)
                val targetFile = targetDirectory.resolve(targetName).normalize()

                if (Files.exists(sourceFile) && Files.isRegularFile(sourceFile)) {
                    Files.move(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
                    movedFiles.add(targetName)
                } else if (Files.exists(targetFile)) {
                    movedFiles.add(targetName)
                }
            }

        return movedFiles
    }

    private fun buildTargetFileName(
        originalFileName: String,
        vSeries: String?,
        vSeason: String?,
        vNumber: String?
    ): String {
        val extension = originalFileName.substringAfterLast('.', missingDelimiterValue = "")
        val suffix = if (extension.isBlank()) "" else ".${extension}"

        val metadataName = listOfNotNull(
            sanitizeFileToken(vSeries),
            sanitizeFileToken(vSeason),
            sanitizeFileToken(vNumber)
        ).filter { it.isNotBlank() }
            .joinToString("_")

        if (metadataName.isNotBlank()) {
            return "${metadataName}${suffix}"
        }

        return "${generateSnowflakeId()}${suffix}"
    }

    private fun sanitizeFileToken(value: String?): String {
        if (value.isNullOrBlank()) {
            return ""
        }

        return value.trim()
            .replace(Regex("[^a-zA-Z0-9]+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    private fun generateSnowflakeId(): String {
        val sequence = SEQUENCE.incrementAndGet() and SEQUENCE_MASK
        val timestamp = System.currentTimeMillis() - SNOWFLAKE_EPOCH
        val snowflake = (timestamp shl 22) or (WORKER_ID shl 12) or sequence
        return snowflake.toString()
    }
}
