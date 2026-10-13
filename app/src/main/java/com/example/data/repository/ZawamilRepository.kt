package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.local.CategoryDao
import com.example.data.local.CategoryEntity
import com.example.data.local.ZamilDao
import com.example.data.local.ZamilEntity
import com.example.data.local.ZawamilDatabase
import com.example.data.model.Category
import com.example.data.model.Zamil
import com.example.data.remote.SupabaseClient
import com.example.player.DownloadManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID

class ZawamilRepository(context: Context) {
    private val database = ZawamilDatabase.getDatabase(context)
    private val zamilDao: ZamilDao = database.zamilDao()
    private val categoryDao: CategoryDao = database.categoryDao()
    val downloadManager = DownloadManager(context)

    companion object {
        private const val TAG = "ZawamilRepository"

        // التصنيفات الستة المطلوبة في الميزة 7
        val DEFAULT_CATEGORIES = listOf(
            Category("cat-1", "زوامل حماسية", "زوامل الشجاعة والإقدام والشهامة اليمانية", "bolt", 1),
            Category("cat-2", "زوامل قبلية", "زوامل القبائل العريقة والأصالة اليمانية", "shield", 2),
            Category("cat-3", "زوامل وطنية", "قصائد المجد وصمود وشموخ جبال اليمن", "flag", 3),
            Category("cat-4", "زوامل مناسبات", "زوامل الأعراس والأعياد واستقبال الضيوف", "celebration", 4),
            Category("cat-5", "زوامل قديمة", "التراث والباليه الفلكلوري العريق", "history_edu", 5),
            Category("cat-6", "زوامل حديثة", "أحدث الإصدارات والتسجيلات الفنية الجديدة", "whatshot", 6)
        )

        val DEFAULT_ZAWAMIL = listOf(
            Zamil(
                id = "zamil-1",
                title = "زامل عهد الأحرار",
                performer = "عيسى الليث",
                description = "زامل حماسي أصيل يعبر عن الصمود والمجد وعزة النفس اليمنية",
                categoryId = "cat-1",
                audioPath = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                coverPath = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=600",
                durationSeconds = 245,
                sortOrder = 1,
                isPublished = true,
                createdAt = "2026-03-01T10:00:00Z",
                playsCount = 4280,
                audioPathLow = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                audioPathHigh = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg"
            ),
            Zamil(
                id = "zamil-2",
                title = "زامل صدى الجبال السامقة",
                performer = "قناف المقبلي",
                description = "لحن تراثي أصيل يحاكي شموخ جبال اليمن الشماء وطيبة أهلها",
                categoryId = "cat-2",
                audioPath = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                coverPath = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=600",
                durationSeconds = 198,
                sortOrder = 2,
                isPublished = true,
                createdAt = "2026-03-02T11:00:00Z",
                playsCount = 3120,
                audioPathLow = null,
                audioPathHigh = null
            ),
            Zamil(
                id = "zamil-3",
                title = "زامل أهلاً بصناديد الرجال",
                performer = "عبد الخالق النبهان",
                description = "زامل ترحيبي قبلي للمناسبات الكبرى ووفود القبائل الكريمة",
                categoryId = "cat-4",
                audioPath = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                coverPath = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600",
                durationSeconds = 215,
                sortOrder = 3,
                isPublished = true,
                createdAt = "2026-03-03T12:00:00Z",
                playsCount = 1890,
                audioPathLow = null,
                audioPathHigh = null
            ),
            Zamil(
                id = "zamil-4",
                title = "زامل فخر اليماني",
                performer = "صقر الوعيل",
                description = "كلمات معبرة في الكرم والنجدة وعادات الآباء والأجداد الأصيلة",
                categoryId = "cat-3",
                audioPath = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                coverPath = "https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=600",
                durationSeconds = 260,
                sortOrder = 4,
                isPublished = true,
                createdAt = "2026-03-04T14:00:00Z",
                playsCount = 2540,
                audioPathLow = null,
                audioPathHigh = null
            ),
            Zamil(
                id = "zamil-5",
                title = "مسودة زامل قيد المراجعة",
                performer = "أبو مجلي",
                description = "تسجيل تجريبي أولي محفوظ كمسودة - خاص بالإدارة فقط",
                categoryId = "cat-6",
                audioPath = "https://commondatastorage.googleapis.com/codeskulptor-assets/Epoq-Lepidoptera.ogg",
                coverPath = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600",
                durationSeconds = 180,
                sortOrder = 5,
                isPublished = false,
                createdAt = "2026-03-05T15:00:00Z",
                playsCount = 120,
                audioPathLow = null,
                audioPathHigh = null
            )
        )
    }

    fun getZawamilFlow(isAdmin: Boolean): Flow<List<Zamil>> {
        val flow = if (isAdmin) zamilDao.getAllZawamilFlow() else zamilDao.getPublishedZawamilFlow()
        return flow.map { list -> list.map { it.toDomain() } }
    }

    fun getMostPlayedZawamilFlow(limit: Int = 10): Flow<List<Zamil>> {
        return zamilDao.getMostPlayedZawamilFlow(limit).map { list -> list.map { it.toDomain() } }
    }

    fun getFavoritesZawamilFlow(): Flow<List<Zamil>> {
        return zamilDao.getFavoritesZawamilFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getDownloadedZawamilFlow(): Flow<List<Zamil>> {
        return zamilDao.getDownloadedZawamilFlow().map { list -> list.map { it.toDomain() } }
    }

    fun getHistoryZawamilFlow(limit: Int = 30): Flow<List<Zamil>> {
        return zamilDao.getHistoryZawamilFlow(limit).map { list -> list.map { it.toDomain() } }
    }

    fun getCategoriesFlow(): Flow<List<Category>> {
        return categoryDao.getAllCategoriesFlow().map { list -> list.map { it.toDomain() } }
    }

    suspend fun initializeAndRefresh() = withContext(Dispatchers.IO) {
        val existingZawamil = zamilDao.getAllZawamil()
        if (existingZawamil.isEmpty()) {
            categoryDao.insertCategories(DEFAULT_CATEGORIES.map { CategoryEntity.fromDomain(it) })
            zamilDao.insertZawamil(DEFAULT_ZAWAMIL.map { ZamilEntity.fromDomain(it) })
        } else {
            // تحديث التصنيفات لضمان وجود التصنيفات الستة المطلوبة
            DEFAULT_CATEGORIES.forEach { defCat ->
                categoryDao.insertCategory(CategoryEntity.fromDomain(defCat))
            }
        }

        if (SupabaseClient.isConfigured()) {
            try {
                val api = SupabaseClient.getApi()
                val catResponse = api.getCategories()
                if (catResponse.isSuccessful && catResponse.body() != null) {
                    val cats = catResponse.body()!!
                    if (cats.isNotEmpty()) {
                        categoryDao.insertCategories(cats.map { CategoryEntity.fromDomain(it) })
                    }
                }

                val zawamilResponse = api.getZawamil()
                if (zawamilResponse.isSuccessful && zawamilResponse.body() != null) {
                    val items = zawamilResponse.body()!!
                    if (items.isNotEmpty()) {
                        // دمج مع الاحتفاظ بحالة المفضلة والتنزيل المحلية
                        val localMap = zamilDao.getAllZawamil().associateBy { it.id }
                        val merged = items.map { remote ->
                            val local = localMap[remote.id]
                            ZamilEntity.fromDomain(
                                remote.copy(
                                    isFavorite = local?.isFavorite ?: false,
                                    isDownloaded = local?.isDownloaded ?: false,
                                    localAudioPath = local?.localAudioPath,
                                    lastPlayedAt = local?.lastPlayedAt,
                                    lastPositionMs = local?.lastPositionMs ?: 0L
                                )
                            )
                        }
                        zamilDao.insertZawamil(merged)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not fetch from Supabase: ${e.message}")
            }
        }
    }

    suspend fun toggleFavorite(zamil: Zamil, userId: String?): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val newStatus = !zamil.isFavorite
            zamilDao.updateFavorite(zamil.id, newStatus)

            if (SupabaseClient.isConfigured() && !userId.isNullOrBlank()) {
                try {
                    val api = SupabaseClient.getApi()
                    if (newStatus) {
                        api.addFavorite(mapOf("user_id" to userId, "zamil_id" to zamil.id))
                    } else {
                        api.removeFavorite("eq.${zamil.id}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Sync favorite to Supabase failed: ${e.message}")
                }
            }
            Result.success(newStatus)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun recordPlay(zamil: Zamil) = withContext(Dispatchers.IO) {
        try {
            zamilDao.incrementPlayCount(zamil.id)
            zamilDao.updateHistory(zamil.id, System.currentTimeMillis(), 0L)

            if (SupabaseClient.isConfigured()) {
                try {
                    SupabaseClient.getApi().recordZamilPlay(mapOf("p_zamil_id" to zamil.id))
                } catch (e: Exception) {
                    Log.w(TAG, "RPC record play error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "recordPlay error: ${e.message}")
        }
    }

    suspend fun updateHistoryPosition(zamilId: String, positionMs: Long) = withContext(Dispatchers.IO) {
        try {
            zamilDao.updateHistory(zamilId, System.currentTimeMillis(), positionMs)
        } catch (_: Exception) { }
    }

    suspend fun removeHistoryItem(zamilId: String) = withContext(Dispatchers.IO) {
        try {
            zamilDao.removeHistoryItem(zamilId)
        } catch (_: Exception) { }
    }

    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        try {
            zamilDao.clearAllHistory()
        } catch (_: Exception) { }
    }

    suspend fun addCategory(name: String, description: String?, icon: String?, sortOrder: Int): Result<Category> = withContext(Dispatchers.IO) {
        try {
            val newCat = Category(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description?.trim(),
                icon = icon ?: "folder",
                sortOrder = sortOrder
            )
            categoryDao.insertCategory(CategoryEntity.fromDomain(newCat))

            if (SupabaseClient.isConfigured()) {
                try {
                    SupabaseClient.getApi().insertCategory(
                        category = mapOf(
                            "id" to newCat.id,
                            "name" to newCat.name,
                            "description" to newCat.description,
                            "icon" to newCat.icon,
                            "sort_order" to newCat.sortOrder
                        )
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing category to Supabase: ${e.message}")
                }
            }
            Result.success(newCat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            categoryDao.deleteCategory(categoryId)
            if (SupabaseClient.isConfigured()) {
                try {
                    SupabaseClient.getApi().deleteCategory("eq.$categoryId")
                } catch (_: Exception) { }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addZamil(
        title: String,
        performer: String,
        description: String?,
        categoryId: String?,
        audioPath: String,
        coverPath: String?,
        durationSeconds: Int,
        isPublished: Boolean,
        audioPathLow: String? = null,
        audioPathHigh: String? = null
    ): Result<Zamil> = withContext(Dispatchers.IO) {
        try {
            val maxOrder = zamilDao.getMaxSortOrder() ?: 0
            val newZamil = Zamil(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                performer = performer.trim(),
                description = description?.trim(),
                categoryId = categoryId,
                audioPath = audioPath.trim(),
                coverPath = coverPath?.trim(),
                durationSeconds = durationSeconds,
                sortOrder = maxOrder + 1,
                isPublished = isPublished,
                createdAt = "2026-03-10T12:00:00Z",
                playsCount = 0,
                audioPathLow = audioPathLow?.trim(),
                audioPathHigh = audioPathHigh?.trim()
            )

            zamilDao.insertZamil(ZamilEntity.fromDomain(newZamil))

            if (SupabaseClient.isConfigured()) {
                try {
                    val payload = mutableMapOf<String, Any?>(
                        "id" to newZamil.id,
                        "title" to newZamil.title,
                        "performer" to newZamil.performer,
                        "description" to newZamil.description,
                        "category_id" to newZamil.categoryId,
                        "audio_path" to newZamil.audioPath,
                        "cover_path" to newZamil.coverPath,
                        "duration_seconds" to newZamil.durationSeconds,
                        "sort_order" to newZamil.sortOrder,
                        "is_published" to newZamil.isPublished,
                        "plays_count" to 0
                    )
                    if (!audioPathLow.isNullOrBlank()) payload["audio_path_low"] = audioPathLow
                    if (!audioPathHigh.isNullOrBlank()) payload["audio_path_high"] = audioPathHigh

                    SupabaseClient.getApi().insertZamil(zamil = payload)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing to Supabase: ${e.message}")
                }
            }

            Result.success(newZamil)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateZamil(zamil: Zamil): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            zamilDao.insertZamil(ZamilEntity.fromDomain(zamil))

            if (SupabaseClient.isConfigured()) {
                try {
                    val payload = mapOf(
                        "title" to zamil.title,
                        "performer" to zamil.performer,
                        "description" to zamil.description,
                        "category_id" to zamil.categoryId,
                        "audio_path" to zamil.audioPath,
                        "cover_path" to zamil.coverPath,
                        "duration_seconds" to zamil.durationSeconds,
                        "sort_order" to zamil.sortOrder,
                        "is_published" to zamil.isPublished,
                        "audio_path_low" to zamil.audioPathLow,
                        "audio_path_high" to zamil.audioPathHigh
                    )
                    SupabaseClient.getApi().updateZamil("eq.${zamil.id}", updates = payload)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing update to Supabase: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun togglePublishStatus(id: String, currentStatus: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val newStatus = !currentStatus
            zamilDao.updatePublishStatus(id, newStatus)

            if (SupabaseClient.isConfigured()) {
                try {
                    SupabaseClient.getApi().updateZamil(
                        "eq.$id",
                        updates = mapOf("is_published" to newStatus)
                    )
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing publish status: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteZamil(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            zamilDao.deleteZamil(id)
            downloadManager.deleteDownloadedZamil(id)

            if (SupabaseClient.isConfigured()) {
                try {
                    SupabaseClient.getApi().deleteZamil("eq.$id")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed syncing delete to Supabase: ${e.message}")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reorderZamil(zamilId: String, direction: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val all = zamilDao.getAllZawamil().sortedBy { it.sortOrder }
            val currentIndex = all.indexOfFirst { it.id == zamilId }
            if (currentIndex == -1) return@withContext Result.failure(Exception("الزامل غير موجود"))

            val targetIndex = if (direction == "up") currentIndex - 1 else currentIndex + 1
            if (targetIndex in all.indices) {
                val currentItem = all[currentIndex]
                val targetItem = all[targetIndex]

                val orderA = currentItem.sortOrder
                val orderB = targetItem.sortOrder

                zamilDao.updateSortOrder(currentItem.id, orderB)
                zamilDao.updateSortOrder(targetItem.id, orderA)

                if (SupabaseClient.isConfigured()) {
                    try {
                        SupabaseClient.getApi().reorderZamil(
                            mapOf("p_zamil_id" to zamilId, "p_direction" to direction)
                        )
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed calling rpc reorder: ${e.message}")
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
