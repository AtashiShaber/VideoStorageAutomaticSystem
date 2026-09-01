package com.atashi.vide.storage.dao

import com.atashi.vide.storage.entity.Video
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param

@Mapper
interface VideoMapper {
    fun insert(video: Video): Int

    fun selectById(id: Long): Video?

    fun selectAll(): List<Video>

    fun update(video: Video): Int

    fun deleteById(id: Long): Int

    fun searchByCondition(
        @Param("name") name: String?,
        @Param("type") type: String?,
        @Param("rank") rank: String?,
        @Param("author") author: String?,
        @Param("tag") tag: String?
    ): List<Video>
}
