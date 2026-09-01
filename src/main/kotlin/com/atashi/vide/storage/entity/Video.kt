package com.atashi.vide.storage.entity

data class Video(
    var id: Long? = null,
    var vName: String = "",
    var vType: String = "",
    var vRank: String = "",
    var vAuthor: String? = null,
    var vTag: String? = null,
    var vSeries: String? = null,
    var vSeason: String? = null,
    var vNumber: String? = null,
    var vFile: String? = null
)
