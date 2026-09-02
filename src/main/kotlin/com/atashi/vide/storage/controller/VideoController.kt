package com.atashi.vide.storage.controller

import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.dto.VideoClassifyRequest
import com.atashi.vide.storage.dto.VideoClassifyResponse
import com.atashi.vide.storage.entity.Video
import com.atashi.vide.storage.service.VideoFileStorageService
import com.atashi.vide.storage.service.VideoService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
            vAuthor = request.vAuthor,
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

    @GetMapping("/search")
    fun searchVideos(
        @RequestParam(name = "keyword", required = false) keyword: String?,
        @RequestParam(name = "q", required = false) q: String?,
        @RequestParam(name = "name", required = false) name: String?,
        @RequestParam(name = "type", required = false) type: String?,
        @RequestParam(name = "rank", required = false) rank: String?,
        @RequestParam(name = "author", required = false) author: String?,
        @RequestParam(name = "tag", required = false) tag: String?,
        @RequestParam(name = "series", required = false) series: String?,
        @RequestParam(name = "season", required = false) season: String?,
        @RequestParam(name = "number", required = false) number: String?,
        @RequestParam(name = "file", required = false) file: String?
    ): ResponseEntity<List<Video>> {
        val finalKeyword = keyword ?: q
        val results = videoService.searchVideos(
            keyword = finalKeyword,
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
        return ResponseEntity.ok(results)
    }
}
