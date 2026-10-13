package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AdminUser
import com.example.data.model.AudioQuality
import com.example.data.model.Category
import com.example.data.model.Zamil
import com.example.data.remote.SupabaseClient
import com.example.data.repository.ZawamilRepository
import com.example.player.AudioPlayerManager
import com.example.player.MediaNotificationHelper
import com.example.player.PlayerState
import com.example.util.ArabicSearchHelper
import com.example.util.ShareHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ZawamilUiState(
    val currentUser: AdminUser? = null,
    val isAdmin: Boolean = false,
    val searchQuery: String = "",
    val selectedCategoryId: String? = null,
    val isRefreshing: Boolean = false,
    val syncMessage: String? = null,
    val errorMessage: String? = null,
    val showGoogleAuthDialog: Boolean = false,
    val showFullPlayer: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val showAddCategoryDialog: Boolean = false,
    val editingZamil: Zamil? = null,
    val deletingZamil: Zamil? = null,
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val currentTab: NavigationTab = NavigationTab.HOME
)

enum class NavigationTab(val titleAr: String) {
    HOME("الرئيسية"),
    MOST_PLAYED("الأكثر استماعاً"),
    FAVORITES("المفضلة"),
    DOWNLOADS("المحملة"),
    HISTORY("السجل"),
    CATEGORIES("التصنيفات"),
    ADMIN("لوحة التحكم")
}

class ZawamilViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ZawamilRepository(application)
    val playerManager = AudioPlayerManager(application)
    private val prefs = application.getSharedPreferences("zawamil_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        ZawamilUiState(
            isDarkMode = prefs.getBoolean("is_dark_mode", true),
            notificationsEnabled = prefs.getBoolean("notifications_enabled", true)
        )
    )
    val uiState: StateFlow<ZawamilUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = playerManager.state
    val downloadProgress: StateFlow<Map<String, Float>> = repository.downloadManager.downloadingProgress

    // التدفقات الأساسية
    val categories: StateFlow<List<Category>> = repository.getCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allZawamil: StateFlow<List<Zamil>> = repository.getZawamilFlow(true)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayedZawamil: StateFlow<List<Zamil>> = repository.getMostPlayedZawamilFlow(15)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoritesZawamil: StateFlow<List<Zamil>> = repository.getFavoritesZawamilFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedZawamil: StateFlow<List<Zamil>> = repository.getDownloadedZawamilFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyZawamil: StateFlow<List<Zamil>> = repository.getHistoryZawamilFlow(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // الزوامل العامة المفلترة بالبحث الذكي
    val filteredZawamil: StateFlow<List<Zamil>> = combine(
        repository.getZawamilFlow(false),
        repository.getZawamilFlow(true),
        _uiState
    ) { publicList, adminList, state ->
        val source = if (state.isAdmin) adminList else publicList
        source.filter { zamil ->
            val matchesCategory = state.selectedCategoryId == null || zamil.categoryId == state.selectedCategoryId
            val matchesQuery = state.searchQuery.isBlank() ||
                    ArabicSearchHelper.matchesQuery(zamil.title, state.searchQuery) ||
                    ArabicSearchHelper.matchesQuery(zamil.performer, state.searchQuery) ||
                    ArabicSearchHelper.matchesQuery(zamil.description, state.searchQuery)
            matchesCategory && matchesQuery
        }.sortedBy { it.sortOrder }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        MediaNotificationHelper.createNotificationChannels(application)

        // ربط تسجيل مرات الاستماع الفعلي بعد الاستماع المستمر
        playerManager.onPlayCountEligible = { zamil ->
            viewModelScope.launch {
                repository.recordPlay(zamil)
            }
        }

        // حفظ موضع الاستماع في السجل
        playerManager.onHistoryPositionUpdate = { zamilId, positionMs ->
            viewModelScope.launch {
                repository.updateHistoryPosition(zamilId, positionMs)
            }
        }

        viewModelScope.launch {
            repository.initializeAndRefresh()
        }
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun toggleDarkMode() {
        val newMode = !_uiState.value.isDarkMode
        _uiState.value = _uiState.value.copy(isDarkMode = newMode)
        prefs.edit().putBoolean("is_dark_mode", newMode).apply()
    }

    fun toggleNotifications() {
        val newStatus = !_uiState.value.notificationsEnabled
        _uiState.value = _uiState.value.copy(notificationsEnabled = newStatus)
        prefs.edit().putBoolean("notifications_enabled", newStatus).apply()
        _uiState.value = _uiState.value.copy(
            syncMessage = if (newStatus) "تم تفعيل إشعارات الزوامل الجديدة" else "تم إيقاف الإشعارات"
        )
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onCategorySelected(categoryId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = if (_uiState.value.selectedCategoryId == categoryId) null else categoryId
        )
    }

    fun toggleFavorite(zamil: Zamil) {
        viewModelScope.launch {
            val result = repository.toggleFavorite(zamil, _uiState.value.currentUser?.email)
            if (result.isSuccess) {
                val isFav = result.getOrNull() == true
                _uiState.value = _uiState.value.copy(
                    syncMessage = if (isFav) "تمت إضافة «${zamil.title}» إلى المفضلة" else "تمت إزالة الزامل من المفضلة"
                )
            }
        }
    }

    fun downloadZamil(zamil: Zamil) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(syncMessage = "بدء تنزيل «${zamil.title}»...")
            val result = repository.downloadManager.downloadZamil(zamil)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(syncMessage = "اكتمل تنزيل «${zamil.title}» للاستماع دون إنترنت")
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "فشل تنزيل الزامل: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun deleteDownloadedZamil(zamilId: String) {
        viewModelScope.launch {
            repository.downloadManager.deleteDownloadedZamil(zamilId)
            _uiState.value = _uiState.value.copy(syncMessage = "تم حذف الملف الصوتي من الجهاز")
        }
    }

    fun removeHistoryItem(zamilId: String) {
        viewModelScope.launch {
            repository.removeHistoryItem(zamilId)
            _uiState.value = _uiState.value.copy(syncMessage = "تمت إزالة الزامل من السجل")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _uiState.value = _uiState.value.copy(syncMessage = "تم مسح سجل الاستماع بالكامل")
        }
    }

    fun shareZamil(context: Context, zamil: Zamil) {
        ShareHelper.shareZamil(context, zamil)
    }

    fun switchAudioQuality(quality: AudioQuality) {
        playerManager.switchQuality(quality)
        _uiState.value = _uiState.value.copy(syncMessage = "تم تغيير جودة الصوت إلى: ${quality.titleAr}")
    }

    fun playZamilWithPlaylist(zamil: Zamil, playlist: List<Zamil>, startPositionMs: Long = 0L) {
        playerManager.playZamil(zamil, playlist, startPositionMs)
    }

    fun openGoogleAuthDialog() {
        _uiState.value = _uiState.value.copy(showGoogleAuthDialog = true)
    }

    fun closeGoogleAuthDialog() {
        _uiState.value = _uiState.value.copy(showGoogleAuthDialog = false)
    }

    fun signInWithGoogle(email: String, displayName: String = "Google User") {
        val trimmedEmail = email.trim()
        val isOwner = AdminUser.isAuthorizedAdmin(trimmedEmail)

        val user = AdminUser(
            email = trimmedEmail,
            displayName = displayName,
            role = if (isOwner) "super_admin" else "regular_user"
        )

        _uiState.value = _uiState.value.copy(
            currentUser = user,
            isAdmin = isOwner,
            showGoogleAuthDialog = false,
            syncMessage = if (isOwner) "تم تسجيل الدخول وتفعيل لوحة التحكم بنجاح" else "تم تسجيل الدخول بنجاح",
            currentTab = if (isOwner) NavigationTab.ADMIN else NavigationTab.HOME
        )

        if (isOwner) {
            SupabaseClient.userBearerToken = "token_owner_${trimmedEmail}"
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            isAdmin = false,
            syncMessage = "تم تسجيل الخروج بنجاح",
            currentTab = NavigationTab.HOME
        )
        SupabaseClient.userBearerToken = null
    }

    fun openFullPlayer() {
        _uiState.value = _uiState.value.copy(showFullPlayer = true)
    }

    fun closeFullPlayer() {
        _uiState.value = _uiState.value.copy(showFullPlayer = false)
    }

    fun openAddZamilDialog() {
        if (!_uiState.value.isAdmin) return
        _uiState.value = _uiState.value.copy(showAddEditDialog = true, editingZamil = null)
    }

    fun openEditZamilDialog(zamil: Zamil) {
        if (!_uiState.value.isAdmin) return
        _uiState.value = _uiState.value.copy(showAddEditDialog = true, editingZamil = zamil)
    }

    fun closeAddEditDialog() {
        _uiState.value = _uiState.value.copy(showAddEditDialog = false, editingZamil = null)
    }

    fun openAddCategoryDialog() {
        if (!_uiState.value.isAdmin) return
        _uiState.value = _uiState.value.copy(showAddCategoryDialog = true)
    }

    fun closeAddCategoryDialog() {
        _uiState.value = _uiState.value.copy(showAddCategoryDialog = false)
    }

    fun addCategory(name: String, description: String?) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            val maxOrder = categories.value.maxOfOrNull { it.sortOrder } ?: 0
            val res = repository.addCategory(name, description, "folder", maxOrder + 1)
            if (res.isSuccess) {
                _uiState.value = _uiState.value.copy(
                    showAddCategoryDialog = false,
                    syncMessage = "تمت إضافة التصنيف «$name» بنجاح"
                )
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "فشل إضافة التصنيف")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            repository.deleteCategory(categoryId)
            _uiState.value = _uiState.value.copy(syncMessage = "تم حذف التصنيف")
        }
    }

    fun saveZamil(
        title: String,
        performer: String,
        description: String?,
        categoryId: String?,
        audioPath: String,
        coverPath: String?,
        durationSeconds: Int,
        isPublished: Boolean,
        audioPathLow: String?,
        audioPathHigh: String?
    ) {
        if (!_uiState.value.isAdmin) return

        viewModelScope.launch {
            val currentEditing = _uiState.value.editingZamil
            if (currentEditing != null) {
                val updated = currentEditing.copy(
                    title = title,
                    performer = performer,
                    description = description,
                    categoryId = categoryId,
                    audioPath = audioPath,
                    coverPath = coverPath,
                    durationSeconds = durationSeconds,
                    isPublished = isPublished,
                    audioPathLow = audioPathLow,
                    audioPathHigh = audioPathHigh
                )
                repository.updateZamil(updated)
                _uiState.value = _uiState.value.copy(
                    showAddEditDialog = false,
                    editingZamil = null,
                    syncMessage = "تم تحديث بيانات الزامل بنجاح"
                )
            } else {
                val result = repository.addZamil(
                    title = title,
                    performer = performer,
                    description = description,
                    categoryId = categoryId,
                    audioPath = audioPath,
                    coverPath = coverPath,
                    durationSeconds = durationSeconds,
                    isPublished = isPublished,
                    audioPathLow = audioPathLow,
                    audioPathHigh = audioPathHigh
                )
                if (result.isSuccess) {
                    val newZ = result.getOrNull()
                    if (newZ != null && isPublished && _uiState.value.notificationsEnabled) {
                        MediaNotificationHelper.showNewZamilNotification(getApplication(), newZ)
                    }
                    _uiState.value = _uiState.value.copy(
                        showAddEditDialog = false,
                        syncMessage = "تمت إضافة الزامل بنجاح"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "فشل إضافة الزامل: ${result.exceptionOrNull()?.message}"
                    )
                }
            }
        }
    }

    fun requestDeleteZamil(zamil: Zamil) {
        if (!_uiState.value.isAdmin) return
        _uiState.value = _uiState.value.copy(deletingZamil = zamil)
    }

    fun confirmDeleteZamil() {
        val target = _uiState.value.deletingZamil ?: return
        if (!_uiState.value.isAdmin) return

        viewModelScope.launch {
            repository.deleteZamil(target.id)
            if (playerState.value.currentZamil?.id == target.id) {
                playerManager.pause()
            }
            _uiState.value = _uiState.value.copy(
                deletingZamil = null,
                syncMessage = "تم حذف الزامل نهائياً"
            )
        }
    }

    fun cancelDeleteZamil() {
        _uiState.value = _uiState.value.copy(deletingZamil = null)
    }

    fun togglePublishStatus(zamil: Zamil) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            repository.togglePublishStatus(zamil.id, zamil.isPublished)
            _uiState.value = _uiState.value.copy(
                syncMessage = if (!zamil.isPublished) "تم نشر الزامل للعامة" else "تم إخفاء الزامل عن العامة"
            )
        }
    }

    fun reorderZamil(zamil: Zamil, direction: String) {
        if (!_uiState.value.isAdmin) return
        viewModelScope.launch {
            repository.reorderZamil(zamil.id, direction)
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            repository.initializeAndRefresh()
            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                syncMessage = "تم تحديث البيانات والمزامنة"
            )
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(syncMessage = null, errorMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.releasePlayer()
    }
}
