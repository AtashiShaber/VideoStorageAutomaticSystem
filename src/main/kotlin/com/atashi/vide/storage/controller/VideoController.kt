package com.atashi.vide.storage.controller

import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.dto.VideoClassifyRequest
import com.atashi.vide.storage.dto.VideoClassifyResponse
import com.atashi.vide.storage.service.VideoFileStorageService
import com.atashi.vide.storage.service.VideoService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/videos")
class VideoController(
    private val videoFileStorageService: VideoFileStorageService,
    private val videoService: VideoService
) {

    @PostMapping("/classify")
    fun classifyFiles(@RequestBody request: VideoClassifyRequest): VideoClassifyResponse {
        val typeDirectory = videoFileStorageService.buildTypeDirectory(request.rootDirectory, request.vType)
        val movedFiles = videoFileStorageService.classifyFiles(
            rootDirectory = request.rootDirectory,
            currentDirectory = request.currentDirectory,
            vType = request.vType,
            selectedFiles = request.selectedFiles,
            vSeries = request.vSeries,
            vSeason = request.vSeason,
            vNumber = request.vNumber
        )

        return VideoClassifyResponse(
            rootDirectory = request.rootDirectory,
            vType = request.vType,
            typeDirectory = typeDirectory.toString(),
            movedFiles = movedFiles
        )
    }

    @PostMapping("/batch")
    fun batchCreateAndClassify(@RequestBody request: VideoBatchRequest): VideoBatchResponse {
        return videoService.classifyAndSave(request)
    }
}
