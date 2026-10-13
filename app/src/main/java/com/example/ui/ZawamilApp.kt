package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddEditZamilDialog
import com.example.ui.components.AdminAuthDialog
import com.example.ui.components.FullPlayerDialog
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.ZawamilSearchBar
import com.example.ui.components.ZawamilTopBar
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme

@Composable
fun ZawamilApp(
    viewModel: ZawamilViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val playerState by viewModel.playerState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val filteredZawamil by viewModel.filteredZawamil.collectAsState()
    val allZawamil by viewModel.allZawamil.collectAsState()
    val mostPlayedZawamil by viewModel.mostPlayedZawamil.collectAsState()
    val favoritesZawamil by viewModel.favoritesZawamil.collectAsState()
    val downloadedZawamil by viewModel.downloadedZawamil.collectAsState()
    val historyZawamil by viewModel.historyZawamil.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    val context = LocalContext.current
    var isSearchVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // تطبيق الوضع الداكن أو الفاتح بناءً على اختيار المستخدم (الميزة 10)
    MyApplicationTheme(darkTheme = uiState.isDarkMode) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            LaunchedEffect(uiState.syncMessage, uiState.errorMessage) {
                val msg = uiState.syncMessage ?: uiState.errorMessage
                if (msg != null) {
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearMessages()
                }
            }

            BackHandler(enabled = uiState.currentTab != NavigationTab.HOME) {
                viewModel.selectTab(NavigationTab.HOME)
            }

            Scaffold(
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    if (uiState.currentTab != NavigationTab.ADMIN) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ZawamilTopBar(
                                currentUser = uiState.currentUser,
                                isAdmin = uiState.isAdmin,
                                isDarkMode = uiState.isDarkMode,
                                notificationsEnabled = uiState.notificationsEnabled,
                                onToggleDarkMode = { viewModel.toggleDarkMode() },
                                onToggleNotifications = { viewModel.toggleNotifications() },
                                onSearchToggle = { isSearchVisible = !isSearchVisible },
                                onAuthClick = { viewModel.openGoogleAuthDialog() },
                                onSignOutClick = { viewModel.signOut() },
                                onRefreshClick = { viewModel.refreshData() }
                            )

                            AnimatedVisibility(
                                visible = isSearchVisible,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                ZawamilSearchBar(
                                    query = uiState.searchQuery,
                                    onQueryChanged = { viewModel.onSearchQueryChanged(it) }
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if (playerState.currentZamil != null) {
                        MiniPlayerBar(
                            playerState = playerState,
                            onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                            onPlayNext = { viewModel.playerManager.playNext() },
                            onPlayPrevious = { viewModel.playerManager.playPrevious() },
                            onBarClick = { viewModel.openFullPlayer() }
                        )
                    }
                },
                modifier = modifier.fillMaxSize()
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (uiState.currentTab) {
                        NavigationTab.ADMIN -> {
                            AdminDashboardScreen(
                                currentUser = uiState.currentUser,
                                isAdmin = uiState.isAdmin,
                                zawamil = allZawamil,
                                categories = categories,
                                playerState = playerState,
                                onBackClick = { viewModel.selectTab(NavigationTab.HOME) },
                                onAddZamilClick = { viewModel.openAddZamilDialog() },
                                onAddCategoryClick = { viewModel.openAddCategoryDialog() },
                                onDeleteCategoryClick = { viewModel.deleteCategory(it) },
                                onEditZamilClick = { viewModel.openEditZamilDialog(it) },
                                onDeleteZamilClick = { viewModel.requestDeleteZamil(it) },
                                onTogglePublish = { viewModel.togglePublishStatus(it) },
                                onMoveUp = { viewModel.reorderZamil(it, "up") },
                                onMoveDown = { viewModel.reorderZamil(it, "down") },
                                onRefresh = { viewModel.refreshData() },
                                onPlayZamil = { zamil ->
                                    viewModel.playZamilWithPlaylist(zamil, allZawamil)
                                },
                                deletingZamil = uiState.deletingZamil,
                                onConfirmDelete = { viewModel.confirmDeleteZamil() },
                                onCancelDelete = { viewModel.cancelDeleteZamil() }
                            )
                        }

                        else -> {
                            HomeScreen(
                                currentTab = uiState.currentTab,
                                zawamil = filteredZawamil,
                                mostPlayedZawamil = mostPlayedZawamil,
                                favoritesZawamil = favoritesZawamil,
                                downloadedZawamil = downloadedZawamil,
                                historyZawamil = historyZawamil,
                                categories = categories,
                                playerState = playerState,
                                downloadProgress = downloadProgress,
                                selectedCategoryId = uiState.selectedCategoryId,
                                searchQuery = uiState.searchQuery,
                                isAdmin = uiState.isAdmin,
                                onTabSelected = { viewModel.selectTab(it) },
                                onCategorySelected = { viewModel.onCategorySelected(it) },
                                onPlayZamil = { zamil, playlist ->
                                    viewModel.playZamilWithPlaylist(zamil, playlist)
                                },
                                onFavoriteClick = { viewModel.toggleFavorite(it) },
                                onDownloadClick = { viewModel.downloadZamil(it) },
                                onShareClick = { viewModel.shareZamil(context, it) },
                                onClearHistory = { viewModel.clearAllHistory() },
                                onNavigateToAdmin = { viewModel.selectTab(NavigationTab.ADMIN) }
                            )
                        }
                    }
                }
            }

            // نافذة مشغل الصوت الموسع بجميع ميزاته
            if (uiState.showFullPlayer && playerState.currentZamil != null) {
                FullPlayerDialog(
                    playerState = playerState,
                    onDismiss = { viewModel.closeFullPlayer() },
                    onTogglePlayPause = { viewModel.playerManager.togglePlayPause() },
                    onSeekTo = { viewModel.playerManager.seekTo(it) },
                    onSeekForward10 = { viewModel.playerManager.seekForward10() },
                    onSeekRewind10 = { viewModel.playerManager.seekRewind10() },
                    onPlayNext = { viewModel.playerManager.playNext() },
                    onPlayPrevious = { viewModel.playerManager.playPrevious() },
                    onCycleRepeat = { viewModel.playerManager.cycleRepeatMode() },
                    onToggleShuffle = { viewModel.playerManager.toggleShuffle() },
                    onSetSpeed = { viewModel.playerManager.setSpeed(it) },
                    onSwitchQuality = { viewModel.switchAudioQuality(it) },
                    onFavoriteClick = { playerState.currentZamil?.let { viewModel.toggleFavorite(it) } },
                    onDownloadClick = { playerState.currentZamil?.let { viewModel.downloadZamil(it) } },
                    onShareClick = { playerState.currentZamil?.let { viewModel.shareZamil(context, it) } }
                )
            }

            // حوار تسجيل الدخول بحساب Google المعتمد
            if (uiState.showGoogleAuthDialog) {
                AdminAuthDialog(
                    onDismiss = { viewModel.closeGoogleAuthDialog() },
                    onGoogleSignIn = { email, name ->
                        viewModel.signInWithGoogle(email, name)
                    }
                )
            }

            // حوار إضافة أو تعديل زامل (مع دعم الجودات الصوتية المتعددة)
            if (uiState.showAddEditDialog) {
                AddEditZamilDialog(
                    zamilToEdit = uiState.editingZamil,
                    categories = categories,
                    onDismiss = { viewModel.closeAddEditDialog() },
                    onSave = { title, performer, desc, catId, audio, cover, dur, isPub, audioLow, audioHigh ->
                        viewModel.saveZamil(title, performer, desc, catId, audio, cover, dur, isPub, audioLow, audioHigh)
                    }
                )
            }

            // حوار إضافة تصنيف جديد
            if (uiState.showAddCategoryDialog) {
                AddCategoryDialog(
                    onDismiss = { viewModel.closeAddCategoryDialog() },
                    onSave = { name, desc ->
                        viewModel.addCategory(name, desc)
                    }
                )
            }
        }
    }
}
