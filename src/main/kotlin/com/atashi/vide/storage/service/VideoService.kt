package com.atashi.vide.storage.service

import com.atashi.vide.storage.dao.VideoMapper
import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.entity.Video
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VideoService(
    private val videoMapper: VideoMapper,
    private val videoFileStorageService: VideoFileStorageService
) {

    @Transactional
    fun classifyAndSave(request: VideoBatchRequest): VideoBatchResponse {
        val rootDirectory = request.rootDirectory.trim().ifEmpty { throw IllegalArgumentException("rootDirectory cannot be empty") }
        val safeType = request.vType.trim().ifEmpty { throw IllegalArgumentException("vType cannot be empty") }

        val typeDirectory = videoFileStorageService.buildTypeDirectory(
            rootDirectory = rootDirectory,
            vType = safeType,
            vAuthor = request.vAuthor,
            vSeries = request.vSeries,
            vSeason = request.vSeason
        )
        val movedFiles = videoFileStorageService.classifyFiles(
            rootDirectory = rootDirectory,
            currentDirectory = request.currentDirectory,
            vType = safeType,
            selectedFiles = request.selectedFiles,
            vAuthor = request.vAuthor,
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
        file: String? = null
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
            file = file
        )
    }
}
