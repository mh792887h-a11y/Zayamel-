package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Zamil(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "performer") val performer: String,
    @Json(name = "description") val description: String? = null,
    @Json(name = "category_id") val categoryId: String? = null,
    @Json(name = "audio_path") val audioPath: String,
    @Json(name = "cover_path") val coverPath: String? = null,
    @Json(name = "duration_seconds") val durationSeconds: Int = 0,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    @Json(name = "is_published") val isPublished: Boolean = true,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "plays_count") val playsCount: Int = 0,
    @Json(name = "audio_path_low") val audioPathLow: String? = null,
    @Json(name = "audio_path_high") val audioPathHigh: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val localAudioPath: String? = null,
    val lastPlayedAt: Long? = null,
    val lastPositionMs: Long = 0L
) {
    fun formatDuration(): String {
        if (durationSeconds <= 0) return "--:--"
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun getAudioUrlForQuality(quality: AudioQuality): String {
        return when (quality) {
            AudioQuality.LOW -> audioPathLow?.takeIf { it.isNotBlank() } ?: audioPath
            AudioQuality.HIGH -> audioPathHigh?.takeIf { it.isNotBlank() } ?: audioPath
            AudioQuality.STANDARD -> audioPath
        }
    }

    fun getAvailableQualities(): List<AudioQuality> {
        val list = mutableListOf(AudioQuality.STANDARD)
        if (!audioPathLow.isNullOrBlank()) list.add(AudioQuality.LOW)
        if (!audioPathHigh.isNullOrBlank()) list.add(AudioQuality.HIGH)
        return list
    }
}

enum class AudioQuality(val titleAr: String, val badge: String) {
    LOW("منخفضة (توفير بيانات)", "64kbps"),
    STANDARD("متوسطة (قياسية)", "128kbps"),
    HIGH("عالية (أصوات نقية)", "320kbps")
}
