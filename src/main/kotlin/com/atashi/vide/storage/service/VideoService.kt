package com.atashi.vide.storage.service

import com.atashi.vide.storage.dao.VideoMapper
import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.entity.Video
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
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

        val typeDirectory = videoFileStorageService.buildTypeDirectory(rootDirectory, safeType)
        val movedFiles = videoFileStorageService.classifyFiles(
            rootDirectory = rootDirectory,
            currentDirectory = request.currentDirectory,
            vType = safeType,
            selectedFiles = request.selectedFiles,
            vSeries = request.vSeries,
            vSeason = request.vSeason,
            vNumber = request.vNumber
        )

        val savedNames = mutableListOf<String>()
        request.selectedFiles
            .filter { it.isNotBlank() }
            .forEach { fileName ->
                val targetFileName = movedFiles.find { it.contains(fileName.substringBeforeLast('.', "")) || it == fileName }
                    ?: fileName

                val saved = Video(
                    vName = targetFileName,
                    vType = safeType,
                    vRank = request.vRank,
                    vAuthor = request.vAuthor,
                    vTag = request.vTag,
                    vSeries = request.vSeries,
                    vSeason = request.vSeason,
                    vNumber = request.vNumber,
                    vFile = typeDirectory.toString()
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
}
