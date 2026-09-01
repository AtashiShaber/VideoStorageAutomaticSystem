package com.atashi.vide.storage.entity

data class Video(
    var id: Long? = null,
    var vName: String = "",
    var vType: String = "",
    var vRank: String = "",
    var vAuthor: String? = null,
    var vTag: String? = null
)
