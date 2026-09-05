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

    fun buildTypeDirectory(
        rootDirectory: String,
        vType: String,
        vAuthor: String? = null,
        vSeries: String? = null,
        vSeason: String? = null
    ): Path {
        val safeRoot = rootDirectory.trim().ifEmpty { throw IllegalArgumentException("v_file root directory cannot be empty") }
        val safeType = vType.trim().ifEmpty { throw IllegalArgumentException("v_type cannot be empty") }
        var directory = Paths.get(safeRoot).toAbsolutePath().normalize().resolve(safeType).normalize()

        if (!vAuthor.isNullOrBlank()) {
            directory = directory.resolve(sanitizeFileToken(vAuthor)).normalize()
        }

        if (!vSeries.isNullOrBlank()) {
            directory = directory.resolve(sanitizeFileToken(vSeries)).normalize()
            if (!vSeason.isNullOrBlank()) {
                directory = directory.resolve(sanitizeFileToken(vSeason)).normalize()
            }
        }

        return directory
    }

    fun classifyFiles(
        rootDirectory: String,
        vType: String,
        selectedFiles: List<String>,
        vName: String? = null,
        vAuthor: String? = null,
        vSeries: String? = null,
        vSeason: String? = null,
        vNumber: String? = null
    ): List<String> {
        val resolvedName = normalizeCustomName(vName)
        validateNameOverride(resolvedName, vSeries, vSeason, vNumber)

        val targetDirectory = buildTypeDirectory(rootDirectory, vType, vAuthor, vSeries, vSeason)
        Files.createDirectories(targetDirectory)

        // 注意：由于前端不再提供 currentDirectory，selectedFiles 仅包含文件名
        // 文件已通过浏览器 File API 读取，这里只需要处理目标路径的创建
        // 实际文件移动/复制操作需要后端能够访问源文件，但这在 Web 应用中不可行
        // 因此，这个函数现在只返回目标文件名列表，真正的文件传输需要其他方式（如上传）
        val movedFiles = mutableListOf<String>()

        selectedFiles
            .filter { it.isNotBlank() }
            .forEach { fileName ->
                val targetName = buildTargetFileName(fileName, resolvedName, vSeries, vSeason, vNumber)
                movedFiles.add(targetName)
            }

        return movedFiles
    }

    private fun buildTargetFileName(
        originalFileName: String,
        vName: String?,
        vSeries: String?,
        vSeason: String?,
        vNumber: String?
    ): String {
        val extension = originalFileName.substringAfterLast('.', missingDelimiterValue = "")
        val suffix = if (extension.isBlank()) "" else ".${extension}"

        if (!vName.isNullOrBlank()) {
            return "${normalizeUserFileName(vName)}${suffix}"
        }

        if (!vSeries.isNullOrBlank() && !vSeason.isNullOrBlank()) {
            val normalizedNumber = normalizeNumber(vNumber)
            if (normalizedNumber.isNotBlank()) {
                return "${normalizedNumber}${suffix}"
            }
            return "${generateSnowflakeId()}${suffix}"
        }

        return "${generateSnowflakeId()}${suffix}"
    }

    private fun normalizeCustomName(value: String?): String? {
        if (value.isNullOrBlank()) {
            return null
        }
        return value.trim()
    }

    private fun validateNameOverride(vName: String?, vSeries: String?, vSeason: String?, vNumber: String?) {
        val hasSeriesOverride = !vSeries.isNullOrBlank() || !vSeason.isNullOrBlank() || !vNumber.isNullOrBlank()
        if (!vName.isNullOrBlank() && hasSeriesOverride) {
            throw IllegalArgumentException("vName cannot be set when vSeries, vSeason or vNumber is provided")
        }
    }

    private fun normalizeUserFileName(value: String): String {
        val trimmed = value.trim()
        val cleaned = trimmed
            .replace(Regex("[\\\\/:*?\"<>|]+"), "_")
            .replace(Regex("\\s+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
        return cleaned.ifEmpty { generateSnowflakeId() }
    }

    private fun sanitizeFileToken(value: String?): String {
        if (value.isNullOrBlank()) {
            return ""
        }

        return value.trim()
            .replace(Regex("[^a-zA-Z0-9._-]+"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
    }

    private fun normalizeNumber(value: String?): String {
        if (value.isNullOrBlank()) {
            return ""
        }

        val trimmed = value.trim()
        val digitsOnly = trimmed.filter { it.isDigit() }
        if (digitsOnly.isNotEmpty() && trimmed.all { it.isDigit() }) {
            return digitsOnly
        }

        return sanitizeFileToken(trimmed)
    }

    private fun generateSnowflakeId(): String {
        val sequence = SEQUENCE.incrementAndGet() and SEQUENCE_MASK
        val timestamp = System.currentTimeMillis() - SNOWFLAKE_EPOCH
        val snowflake = (timestamp shl 22) or (WORKER_ID shl 12) or sequence
        return snowflake.toString()
    }
}
