package com.atashi.vide.storage.dto

data class VideoClassifyRequest(
    val rootDirectory: String = "",
    val vName: String? = null,
    val vType: String = "",
    val vAuthor: String? = null,
    val vTag: String? = null,
    val vSeries: String? = null,
    val vSeason: String? = null,
    val vNumber: String? = null,
    val selectedFiles: List<String> = emptyList()
)

data class VideoClassifyResponse(
    val rootDirectory: String,
    val vType: String,
    val typeDirectory: String,
    val movedFiles: List<String>
)

data class VideoBatchRequest(
    val rootDirectory: String = "",
    val vName: String? = null,
    val vType: String = "",
    val vRank: String = "",
    val vAuthor: String? = null,
    val vTag: String? = null,
    val vSeries: String? = null,
    val vSeason: String? = null,
    val vNumber: String? = null,
    val selectedFiles: List<String> = emptyList()
)

data class VideoBatchResponse(
    val rootDirectory: String,
    val vType: String,
    val typeDirectory: String,
    val savedVideos: List<String>,
    val movedFiles: List<String>
)
