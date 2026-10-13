package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Category
import com.example.data.model.Zamil
import com.example.player.PlayerState
import com.example.ui.NavigationTab
import com.example.ui.components.CategoryChips
import com.example.ui.components.ZamilCard

@Composable
fun HomeScreen(
    currentTab: NavigationTab,
    zawamil: List<Zamil>,
    mostPlayedZawamil: List<Zamil>,
    favoritesZawamil: List<Zamil>,
    downloadedZawamil: List<Zamil>,
    historyZawamil: List<Zamil>,
    categories: List<Category>,
    playerState: PlayerState,
    downloadProgress: Map<String, Float>,
    selectedCategoryId: String?,
    searchQuery: String,
    isAdmin: Boolean,
    onTabSelected: (NavigationTab) -> Unit,
    onCategorySelected: (String?) -> Unit,
    onPlayZamil: (Zamil, List<Zamil>) -> Unit,
    onFavoriteClick: (Zamil) -> Unit,
    onDownloadClick: (Zamil) -> Unit,
    onShareClick: (Zamil) -> Unit,
    onClearHistory: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeList = when (currentTab) {
        NavigationTab.HOME -> zawamil
        NavigationTab.MOST_PLAYED -> mostPlayedZawamil
        NavigationTab.FAVORITES -> favoritesZawamil
        NavigationTab.DOWNLOADS -> downloadedZawamil
        NavigationTab.HISTORY -> historyZawamil
        else -> zawamil
    }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column")
    ) {
        // شريط تنقل التبويبات الرئيسية (الرئيسية، الأكثر استماعاً، المفضلة، المحملة، السجل)
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                listOf(
                    NavigationTab.HOME to Icons.Default.Home,
                    NavigationTab.MOST_PLAYED to Icons.AutoMirrored.Filled.TrendingUp,
                    NavigationTab.FAVORITES to Icons.Default.Favorite,
                    NavigationTab.DOWNLOADS to Icons.Default.DownloadDone,
                    NavigationTab.HISTORY to Icons.Default.History
                ).forEach { (tab, icon) ->
                    item {
                        FilterChip(
                            selected = currentTab == tab,
                            onClick = { onTabSelected(tab) },
                            leadingIcon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = { Text(tab.titleAr, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // شريط الوصول السريع للوحة التحكم إذا كان مسجلاً كمدير
        if (isAdmin) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onNavigateToAdmin() }
                        .testTag("admin_banner_quick_access")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "لوحة التحكم",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "إضافة وتعديل وترتيب الزوامل والتصنيفات",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Button(
                            onClick = onNavigateToAdmin,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("فتح اللوحة", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // في تبويب الرئيسية فقط: نعرض الهيرو التراثي وقسم الأكثر استماعاً المميز
        if (currentTab == NavigationTab.HOME && searchQuery.isBlank() && selectedCategoryId == null) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.zawamil_hero),
                            contentDescription = "جبال وتراث اليمن",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0x9912100E),
                                            Color(0xEE12100E)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Whatshot,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "أصالة التراث اليماني",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "صوت القبائل وصدى الشجاعة والمجد",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استمع لأشهر الزوامل الحماسية والتراثية بأعلى جودة",
                                color = Color(0xFFD4CDC5),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // قسم الأكثر استماعاً (الميزة 1)
            if (mostPlayedZawamil.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "الأكثر استماعاً",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "عرض الكل",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onTabSelected(NavigationTab.MOST_PLAYED) }
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(mostPlayedZawamil.take(6), key = { "top_${it.id}" }) { item ->
                                FeaturedZamilCard(
                                    zamil = item,
                                    isPlaying = playerState.isPlaying && playerState.currentZamil?.id == item.id,
                                    onPlayClick = { onPlayZamil(item, mostPlayedZawamil) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // فلترة التصنيفات (في تبويب الرئيسية)
        if (currentTab == NavigationTab.HOME) {
            item {
                Column {
                    Text(
                        text = "التصنيفات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    CategoryChips(
                        categories = categories,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected
                    )
                }
            }
        }

        // رأس القسم الحالي مع الإحصائيات
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val headerIcon = when (currentTab) {
                        NavigationTab.HOME -> Icons.Default.LibraryMusic
                        NavigationTab.MOST_PLAYED -> Icons.AutoMirrored.Filled.TrendingUp
                        NavigationTab.FAVORITES -> Icons.Default.Favorite
                        NavigationTab.DOWNLOADS -> Icons.Default.DownloadDone
                        NavigationTab.HISTORY -> Icons.Default.History
                        else -> Icons.Default.LibraryMusic
                    }
                    Icon(
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentTab) {
                            NavigationTab.HOME -> if (selectedCategoryId != null) {
                                val cat = categories.find { it.id == selectedCategoryId }?.name ?: "التصنيف"
                                "زوامل $cat"
                            } else "جميع الزوامل"
                            NavigationTab.MOST_PLAYED -> "الزوامل الأكثر استماعاً"
                            NavigationTab.FAVORITES -> "قائمة المفضلة"
                            NavigationTab.DOWNLOADS -> "المحملة دون إنترنت"
                            NavigationTab.HISTORY -> "سجل الاستماع"
                            else -> currentTab.titleAr
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (currentTab == NavigationTab.HISTORY && historyZawamil.isNotEmpty()) {
                    OutlinedButton(
                        onClick = onClearHistory,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مسح السجل", fontSize = 11.sp)
                    }
                } else {
                    Text(
                        text = "${activeList.size} زامل",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // قائمة العناصر
        if (activeList.isEmpty()) {
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp)
                ) {
                    Icon(
                        imageVector = when (currentTab) {
                            NavigationTab.FAVORITES -> Icons.Default.Favorite
                            NavigationTab.DOWNLOADS -> Icons.Default.DownloadDone
                            NavigationTab.HISTORY -> Icons.Default.History
                            else -> Icons.Default.SearchOff
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = when (currentTab) {
                            NavigationTab.FAVORITES -> "لا توجد زوامل في المفضلة حالياً. اضغط على أيقونة القلب لإضافتها"
                            NavigationTab.DOWNLOADS -> "لم تقم بتنزيل أي زوامل بعد. اضغط على زر التنزيل للاستماع دون إنترنت"
                            NavigationTab.HISTORY -> "سجل الاستماع فارغ. ابدأ بتشغيل أي زامل وسيظهر هنا"
                            else -> if (searchQuery.isNotBlank()) "لم يتم العثور على زوامل مطابقة للبحث" else "لا توجد زوامل في هذا التصنيف حالياً"
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            items(activeList, key = { "${currentTab.name}_${it.id}" }) { item ->
                ZamilCard(
                    zamil = item,
                    categories = categories,
                    isPlaying = playerState.isPlaying,
                    isCurrentTrack = playerState.currentZamil?.id == item.id,
                    isAdmin = false,
                    downloadProgress = downloadProgress[item.id],
                    onPlayClick = { onPlayZamil(item, activeList) },
                    onFavoriteClick = { onFavoriteClick(item) },
                    onDownloadClick = { onDownloadClick(item) },
                    onShareClick = { onShareClick(item) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun FeaturedZamilCard(
    zamil: Zamil,
    isPlaying: Boolean,
    onPlayClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .width(180.dp)
            .clickable { onPlayClick() }
            .testTag("featured_zamil_${zamil.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                if (!zamil.coverPath.isNullOrBlank()) {
                    AsyncImage(
                        model = zamil.coverPath,
                        contentDescription = zamil.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }

                FilledIconButton(
                    onClick = onPlayClick,
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = zamil.title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = zamil.performer,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${zamil.playsCount} استماع",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
