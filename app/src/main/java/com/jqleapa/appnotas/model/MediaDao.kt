package com.jqlqapa.appnotas.data.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert

@Dao
interface MediaDao {
    @Insert
    suspend fun insertMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    // Útil para limpiar todos los medios de una nota al eliminarla
    @Delete
    suspend fun deleteMediaList(mediaList: List<MediaEntity>)
}