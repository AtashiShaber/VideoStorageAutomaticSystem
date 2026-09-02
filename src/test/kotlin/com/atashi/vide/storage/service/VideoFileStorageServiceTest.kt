package com.atashi.vide.storage.service

import com.atashi.vide.storage.dao.VideoMapper
import com.atashi.vide.storage.entity.Video
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.nio.file.Files
import kotlin.io.path.name

class VideoFileStorageServiceTest {

    @Test
    fun `should create type folder and move files into it`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        val file1 = Files.createFile(currentDir.resolve("video-1.mp4"))
        val file2 = Files.createFile(currentDir.resolve("video-2.mkv"))

        val service = VideoFileStorageService()
        val movedFiles = service.classifyFiles(
            rootDirectory = rootDir.toString(),
            currentDirectory = currentDir.toString(),
            vType = "Anime",
            selectedFiles = listOf(file1.name, file2.name)
        )

        assertTrue(Files.isDirectory(rootDir.resolve("Anime")))
        assertTrue(movedFiles.size == 2)
        assertTrue(movedFiles.all { it.endsWith(".mp4") || it.endsWith(".mkv") })
        assertTrue(movedFiles.all { Files.exists(rootDir.resolve("Anime").resolve(it)) })
        assertTrue(Files.notExists(currentDir.resolve("video-1.mp4")))
        assertTrue(Files.notExists(currentDir.resolve("video-2.mkv")))
    }

    @Test
    fun `should store series files under author series season and use number as file name`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        val file = Files.createFile(currentDir.resolve("original-name.mp4"))

        val service = VideoFileStorageService()
        val movedFiles = service.classifyFiles(
            rootDirectory = rootDir.toString(),
            currentDirectory = currentDir.toString(),
            vType = "Anime",
            selectedFiles = listOf(file.name),
            vAuthor = "Studio A",
            vSeries = "One Piece",
            vSeason = "S1",
            vNumber = "05"
        )

        val targetDir = rootDir.resolve("Anime").resolve("Studio_A").resolve("One_Piece").resolve("S1")
        assertTrue(Files.isDirectory(targetDir))
        assertTrue(Files.exists(targetDir.resolve("05.mp4")))
        assertTrue(movedFiles.contains("05.mp4"))
        assertTrue(Files.notExists(targetDir.resolve("original-name.mp4")))
    }

    @Test
    fun `should put files directly under author when series is absent`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        val file = Files.createFile(currentDir.resolve("author-file.mp4"))

        val service = VideoFileStorageService()
        val movedFiles = service.classifyFiles(
            rootDirectory = rootDir.toString(),
            currentDirectory = currentDir.toString(),
            vType = "Anime",
            selectedFiles = listOf(file.name),
            vAuthor = "Studio A"
        )

        val targetDir = rootDir.resolve("Anime").resolve("Studio_A")
        assertTrue(Files.isDirectory(targetDir))
        assertTrue(Files.exists(targetDir.resolve(movedFiles.first())))
        assertTrue(movedFiles.first().endsWith(".mp4"))
    }

    @Test
    fun `should forward tag author and series keyword filters to the mapper`() {
        val mapper = object : VideoMapper {
            var lastAuthor: String? = null
            var lastTag: String? = null
            var lastSeries: String? = null

            override fun insert(video: Video): Int = 1
            override fun selectById(id: Long): Video? = null
            override fun selectAll(): List<Video> = emptyList()
            override fun update(video: Video): Int = 1
            override fun deleteById(id: Long): Int = 1
            override fun searchByCondition(
                keyword: String?,
                name: String?,
                type: String?,
                rank: String?,
                author: String?,
                tag: String?,
                series: String?,
                season: String?,
                number: String?,
                file: String?
            ): List<Video> {
                lastAuthor = author
                lastTag = tag
                lastSeries = series
                return emptyList()
            }
        }

        val service = VideoService(
            videoMapper = mapper,
            videoFileStorageService = mock(VideoFileStorageService::class.java)
        )

        service.searchVideos(tag = "adventure", author = "studio", series = "piece")

        assertEquals("studio", mapper.lastAuthor)
        assertEquals("adventure", mapper.lastTag)
        assertEquals("piece", mapper.lastSeries)
    }

    @Test
    fun `should use explicit vName when provided and no series metadata is set`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        val file = Files.createFile(currentDir.resolve("original-name.mp4"))

        val service = VideoFileStorageService()
        val movedFiles = service.classifyFiles(
            rootDirectory = rootDir.toString(),
            currentDirectory = currentDir.toString(),
            vType = "Anime",
            selectedFiles = listOf(file.name),
            vName = "custom-title",
            vAuthor = "Studio A"
        )

        val targetDir = rootDir.resolve("Anime").resolve("Studio_A")
        assertTrue(movedFiles.contains("custom-title.mp4"))
        assertTrue(Files.exists(targetDir.resolve("custom-title.mp4")))
    }

    @Test
    fun `should reject custom vName when series season or number is present`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        Files.createFile(currentDir.resolve("original-name.mp4"))

        val service = VideoFileStorageService()

        assertThrows(IllegalArgumentException::class.java) {
            service.classifyFiles(
                rootDirectory = rootDir.toString(),
                currentDirectory = currentDir.toString(),
                vType = "Anime",
                selectedFiles = listOf("original-name.mp4"),
                vName = "manual-name",
                vAuthor = "Studio A",
                vSeries = "One Piece",
                vSeason = "S1",
                vNumber = "03"
            )
        }
    }
}
