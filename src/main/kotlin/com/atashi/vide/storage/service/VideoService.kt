package com.atashi.vide.storage.service

import com.atashi.vide.storage.dao.VideoMapper
import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.entity.Video
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Paths

@Service
class VideoService(
    private val videoMapper: VideoMapper,
    private val videoFileStorageService: VideoFileStorageService
) {

    @Transactional
    fun classifyAndSave(request: VideoBatchRequest): VideoBatchResponse {
        val rootDirectory = request.rootDirectory.trim().ifEmpty { throw IllegalArgumentException("rootDirectory cannot be empty") }
        val safeType = request.vType.trim().ifEmpty { throw IllegalArgumentException("vType cannot be empty") }
        val resolvedName = request.vName?.trim()
        validateNameOverride(resolvedName, request.vSeries, request.vSeason, request.vNumber)

        val typeDirectory = videoFileStorageService.buildTypeDirectory(
            rootDirectory = rootDirectory,
            vType = safeType,
            vAuthor = request.vAuthor,
            vSeries = request.vSeries,
            vSeason = request.vSeason
        )
        val movedFiles = videoFileStorageService.classifyFiles(
            rootDirectory = request.rootDirectory,
            currentDirectory = request.currentDirectory,
            vType = request.vType,
            selectedFiles = request.selectedFiles,
            vName = resolvedName,
            vAuthor = request.vAuthor,
            vSeries = request.vSeries,
            vSeason = request.vSeason,
            vNumber = request.vNumber,
            sourceDirectory = request.sourceDirectory
        )

        val savedNames = mutableListOf<String>()
        request.selectedFiles
            .filter { it.isNotBlank() }
            .forEach { fileName ->
                val targetFileName = movedFiles.find { it.contains(fileName.substringBeforeLast('.', "")) || it == fileName }
                    ?: fileName

                val vFilePath = if (targetFileName.isBlank()) typeDirectory.toString() else typeDirectory.resolve(targetFileName).toString()

                val saved = Video(
                    vName = resolvedName ?: targetFileName,
                    vType = safeType,
                    vRank = request.vRank,
                    vAuthor = request.vAuthor,
                    vTag = request.vTag,
                    vSeries = request.vSeries,
                    vSeason = request.vSeason,
                    vNumber = request.vNumber,
                    vFile = vFilePath.takeIf { it.isNotBlank() }
                )

                videoMapper.insert(saved)
                savedNames.add(targetFileName)
            }

        return VideoBatchResponse(
            rootDirectory = rootDirectory,
            vType = safeType,
            typeDirectory = typeDirectory.toString(),
            savedVideos = savedNames,
            movedFiles = movedFiles
        )
    }

    @Transactional
    fun classifyUploadedAndSave(
        rootDirectory: String,
        vType: String,
        file: MultipartFile,
        vName: String?,
        vRank: String?,
        vAuthor: String?,
        vTag: String?,
        vSeries: String?,
        vSeason: String?,
        vNumber: String?
    ): VideoBatchResponse {
        val safeRoot = rootDirectory.trim().ifEmpty { throw IllegalArgumentException("rootDirectory cannot be empty") }
        val safeType = vType.trim().ifEmpty { throw IllegalArgumentException("vType cannot be empty") }
        val (targetName, targetPath) = videoFileStorageService.storeUploadedFile(
            safeRoot, safeType, file, vName, vAuthor, vSeries, vSeason, vNumber
        )
        videoMapper.insert(Video(
            vName = vName?.trim()?.takeIf { it.isNotEmpty() } ?: targetName,
            vType = safeType,
            vRank = vRank,
            vAuthor = vAuthor,
            vTag = vTag,
            vSeries = vSeries,
            vSeason = vSeason,
            vNumber = vNumber,
            vFile = targetPath.toString()
        ))
        return VideoBatchResponse(safeRoot, safeType, targetPath.parent.toString(), listOf(targetName), listOf(targetName))
    }

    private fun validateNameOverride(vName: String?, vSeries: String?, vSeason: String?, vNumber: String?) {
        val hasSeriesAssignment = !vSeries.isNullOrBlank() || !vSeason.isNullOrBlank() || !vNumber.isNullOrBlank()
        if (!vName.isNullOrBlank() && hasSeriesAssignment) {
            throw IllegalArgumentException("vName cannot be set when vSeries, vSeason or vNumber is provided")
        }
    }

    fun searchVideos(
        keyword: String? = null,
        name: String? = null,
        type: String? = null,
        rank: String? = null,
        author: String? = null,
        tag: String? = null,
        series: String? = null,
        season: String? = null,
        number: String? = null,
        file: String? = null,
        excludeRank: String? = null
    ): List<Video> {
        return videoMapper.searchByCondition(
            keyword = keyword,
            name = name,
            type = type,
            rank = rank,
            author = author,
            tag = tag,
            series = series,
            season = season,
            number = number,
            file = file,
            excludeRank = excludeRank
        )
    }

    fun findAll(excludeRank: String? = null): List<Video> = if (excludeRank.isNullOrBlank()) {
        videoMapper.selectAll()
    } else {
        videoMapper.searchByCondition(null, null, null, null, null, null, null, null, null, null, excludeRank)
    }

    fun findById(id: Long): Video? = videoMapper.selectById(id)

    @Transactional
    fun update(id: Long, video: Video): Video? {
        val existing = videoMapper.selectById(id) ?: return null
        val updated = video.copy(id = id, vFile = existing.vFile)
        videoMapper.update(updated)
        return updated
    }

    @Transactional
    fun delete(id: Long, deleteLocalFile: Boolean = true): Boolean {
        val video = videoMapper.selectById(id) ?: return false
        if (deleteLocalFile && !video.vFile.isNullOrBlank()) {
            val file = Paths.get(video.vFile!!).toAbsolutePath().normalize()
            if (Files.exists(file) && Files.isRegularFile(file)) Files.delete(file)
        }
        return videoMapper.deleteById(id) > 0
    }

    fun openWithDefaultPlayer(id: Long): Boolean {
        val path = videoMapper.selectById(id)?.vFile ?: return false
        val file = Paths.get(path).toFile()
        if (!file.isFile || !Desktop.isDesktopSupported()) return false
        Desktop.getDesktop().open(file)
        return true
    }

    @Transactional
    fun renameVideo(videoId: Long, newName: String) {
        val video = videoMapper.selectById(videoId) ?: throw IllegalArgumentException("Video not found")
        video.vName = newName.trim()
        videoMapper.update(video)
    }
}
