package com.atashi.vide.storage.controller

import com.atashi.vide.storage.dto.VideoBatchRequest
import com.atashi.vide.storage.dto.VideoBatchResponse
import com.atashi.vide.storage.dto.VideoClassifyRequest
import com.atashi.vide.storage.dto.VideoClassifyResponse
import com.atashi.vide.storage.entity.Video
import com.atashi.vide.storage.service.VideoFileStorageService
import com.atashi.vide.storage.service.VideoService
import org.springframework.http.ResponseEntity
import org.springframework.core.io.FileSystemResource
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import java.awt.GraphicsEnvironment
import javax.swing.JFileChooser

@RestController
@RequestMapping("/videos")
@CrossOrigin
class VideoController(
    private val videoFileStorageService: VideoFileStorageService,
    private val videoService: VideoService
) {

    @PostMapping("/classify")
    fun classifyFiles(@RequestBody request: VideoClassifyRequest): VideoClassifyResponse {
        val typeDirectory = videoFileStorageService.buildTypeDirectory(request.rootDirectory, request.vType)
        val movedFiles = videoFileStorageService.classifyFiles(
            rootDirectory = request.rootDirectory,
            vType = request.vType,
            selectedFiles = request.selectedFiles,
            vName = request.vName,
            vAuthor = request.vAuthor,
            vSeries = request.vSeries,
            vSeason = request.vSeason,
            vNumber = request.vNumber,
            sourceDirectory = request.sourceDirectory
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
        @RequestParam(name = "file", required = false) file: String?,
        @RequestParam(name = "excludeRank", required = false) excludeRank: String?
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
            file = file,
            excludeRank = excludeRank
        )
        return ResponseEntity.ok(results)
    }

    @GetMapping("/choose-directory")
    fun chooseDirectory(): ResponseEntity<Map<String, String>> {
        if (GraphicsEnvironment.isHeadless()) {
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body(mapOf("error" to "当前运行环境没有图形界面"))
        }
        val chooser = JFileChooser().apply {
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            dialogTitle = "选择 Video 文件夹"
            isAcceptAllFileFilterUsed = false
        }
        val result = chooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            ResponseEntity.ok(mapOf("path" to chooser.selectedFile.absolutePath))
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @GetMapping
    fun findAll(): ResponseEntity<List<Video>> = ResponseEntity.ok(videoService.findAll())

    @GetMapping("/{id}")
    fun findById(@PathVariable id: Long): ResponseEntity<Video> =
        videoService.findById(id)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody video: Video): ResponseEntity<Video> =
        videoService.update(id, video)?.let { ResponseEntity.ok(it) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long, @RequestParam(defaultValue = "true") deleteLocalFile: Boolean): ResponseEntity<Void> =
        if (videoService.delete(id, deleteLocalFile)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()

    @GetMapping("/{id}/file")
    fun file(@PathVariable id: Long): ResponseEntity<FileSystemResource> {
        val path = videoService.findById(id)?.vFile ?: return ResponseEntity.notFound().build()
        val resource = FileSystemResource(path)
        if (!resource.exists() || !resource.isReadable) return ResponseEntity.notFound().build()
        val mediaType = try { MediaType.parseMediaType(java.nio.file.Files.probeContentType(resource.file.toPath()) ?: "video/mp4") }
        catch (_: Exception) { MediaType.APPLICATION_OCTET_STREAM }
        return ResponseEntity.status(HttpStatus.OK).contentType(mediaType).body(resource)
    }

    @PostMapping("/{id}/open")
    fun openWithDefaultPlayer(@PathVariable id: Long): ResponseEntity<Void> =
        if (videoService.openWithDefaultPlayer(id)) ResponseEntity.noContent().build()
        else ResponseEntity.notFound().build()
}
