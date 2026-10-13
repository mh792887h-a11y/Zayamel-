package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Zamil

@Entity(tableName = "zawamil")
data class ZamilEntity(
    @PrimaryKey val id: String,
    val title: String,
    val performer: String,
    val description: String?,
    val categoryId: String?,
    val audioPath: String,
    val coverPath: String?,
    val durationSeconds: Int,
    val sortOrder: Int,
    val isPublished: Boolean,
    val createdAt: String?,
    val playsCount: Int = 0,
    val audioPathLow: String? = null,
    val audioPathHigh: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val lastPlayedAt: Long? = null,
    val lastPositionMs: Long = 0L
) {
    fun toDomain(): Zamil = Zamil(
        id = id,
        title = title,
        performer = performer,
        description = description,
        categoryId = categoryId,
        audioPath = audioPath,
        coverPath = coverPath,
        durationSeconds = durationSeconds,
        sortOrder = sortOrder,
        isPublished = isPublished,
        createdAt = createdAt,
        playsCount = playsCount,
        audioPathLow = audioPathLow,
        audioPathHigh = audioPathHigh,
        isFavorite = isFavorite,
        isDownloaded = isDownloaded,
        localAudioPath = localAudioPath,
        lastPlayedAt = lastPlayedAt,
        lastPositionMs = lastPositionMs
    )

    companion object {
        fun fromDomain(z: Zamil): ZamilEntity = ZamilEntity(
            id = z.id,
            title = z.title,
            performer = z.performer,
            description = z.description,
            categoryId = z.categoryId,
            audioPath = z.audioPath,
            coverPath = z.coverPath,
            durationSeconds = z.durationSeconds,
            sortOrder = z.sortOrder,
            isPublished = z.isPublished,
            createdAt = z.createdAt,
            playsCount = z.playsCount,
            audioPathLow = z.audioPathLow,
            audioPathHigh = z.audioPathHigh,
            isFavorite = z.isFavorite,
            isDownloaded = z.isDownloaded,
            localAudioPath = z.localAudioPath,
            lastPlayedAt = z.lastPlayedAt,
            lastPositionMs = z.lastPositionMs
        )
    }
}
