package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ZamilDao {
    @Query("SELECT * FROM zawamil WHERE isPublished = 1 ORDER BY sortOrder ASC")
    fun getPublishedZawamilFlow(): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil ORDER BY sortOrder ASC")
    fun getAllZawamilFlow(): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil WHERE isPublished = 1 ORDER BY playsCount DESC LIMIT :limit")
    fun getMostPlayedZawamilFlow(limit: Int = 10): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil WHERE isFavorite = 1 AND isPublished = 1 ORDER BY sortOrder ASC")
    fun getFavoritesZawamilFlow(): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil WHERE isDownloaded = 1 ORDER BY sortOrder ASC")
    fun getDownloadedZawamilFlow(): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil WHERE lastPlayedAt IS NOT NULL ORDER BY lastPlayedAt DESC LIMIT :limit")
    fun getHistoryZawamilFlow(limit: Int = 30): Flow<List<ZamilEntity>>

    @Query("SELECT * FROM zawamil WHERE id = :id LIMIT 1")
    suspend fun getZamilById(id: String): ZamilEntity?

    @Query("SELECT * FROM zawamil ORDER BY sortOrder ASC")
    suspend fun getAllZawamil(): List<ZamilEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZawamil(zawamil: List<ZamilEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertZamil(zamil: ZamilEntity)

    @Query("DELETE FROM zawamil WHERE id = :id")
    suspend fun deleteZamil(id: String)

    @Query("UPDATE zawamil SET isPublished = :isPublished WHERE id = :id")
    suspend fun updatePublishStatus(id: String, isPublished: Boolean)

    @Query("UPDATE zawamil SET sortOrder = :order WHERE id = :id")
    suspend fun updateSortOrder(id: String, order: Int)

    @Query("SELECT MAX(sortOrder) FROM zawamil")
    suspend fun getMaxSortOrder(): Int?

    @Query("UPDATE zawamil SET playsCount = playsCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)

    @Query("UPDATE zawamil SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE zawamil SET isDownloaded = :isDownloaded, localAudioPath = :path WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean, path: String?)

    @Query("UPDATE zawamil SET lastPlayedAt = :timestamp, lastPositionMs = :position WHERE id = :id")
    suspend fun updateHistory(id: String, timestamp: Long, position: Long)

    @Query("UPDATE zawamil SET lastPlayedAt = NULL, lastPositionMs = 0 WHERE id = :id")
    suspend fun removeHistoryItem(id: String)

    @Query("UPDATE zawamil SET lastPlayedAt = NULL, lastPositionMs = 0")
    suspend fun clearAllHistory()
}
