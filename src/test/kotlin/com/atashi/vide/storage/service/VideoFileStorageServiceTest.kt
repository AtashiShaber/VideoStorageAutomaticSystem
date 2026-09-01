package com.atashi.vide.storage.service

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
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

        assertTrue(Files.isDirectory(currentDir.resolve("Anime")))
        assertTrue(movedFiles.size == 2)
        assertTrue(movedFiles.all { it.endsWith(".mp4") || it.endsWith(".mkv") })
        assertTrue(movedFiles.all { Files.exists(currentDir.resolve("Anime").resolve(it)) })
        assertTrue(Files.notExists(currentDir.resolve("video-1.mp4")))
        assertTrue(Files.notExists(currentDir.resolve("video-2.mkv")))
    }

    @Test
    fun `should rename file with series season and number before storing in type folder`() {
        val rootDir = Files.createTempDirectory("video-root")
        val currentDir = Files.createDirectory(rootDir.resolve("current"))
        val file = Files.createFile(currentDir.resolve("original-name.mp4"))

        val service = VideoFileStorageService()
        val movedFiles = service.classifyFiles(
            rootDirectory = rootDir.toString(),
            currentDirectory = currentDir.toString(),
            vType = "Anime",
            selectedFiles = listOf(file.name),
            vSeries = "One Piece",
            vSeason = "S1",
            vNumber = "05"
        )

        assertTrue(Files.isDirectory(currentDir.resolve("Anime")))
        assertTrue(Files.exists(currentDir.resolve("Anime").resolve("One_Piece_S1_05.mp4")))
        assertTrue(movedFiles.contains("One_Piece_S1_05.mp4"))
        assertTrue(Files.notExists(currentDir.resolve("Anime").resolve("original-name.mp4")))
    }
}
