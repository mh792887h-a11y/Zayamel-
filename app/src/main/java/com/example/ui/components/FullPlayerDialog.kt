package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AudioQuality
import com.example.player.PlayerState
import com.example.player.RepeatState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullPlayerDialog(
    playerState: PlayerState,
    onDismiss: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSeekForward10: () -> Unit,
    onSeekRewind10: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSwitchQuality: (AudioQuality) -> Unit,
    onFavoriteClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val zamil = playerState.currentZamil ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var qualityMenuExpanded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
        modifier = modifier.testTag("full_player_bottom_sheet")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // شريط الإغلاق وأدوات الرأس
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_full_player_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "تصغير المشغل",
                        modifier = Modifier.size(30.dp)
                    )
                }

                Text(
                    text = "مشغل الزوامل الأصيل",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // زر اختيار جودة الصوت
                Box {
                    FilledTonalButton(
                        onClick = { qualityMenuExpanded = true },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("quality_selector_button")
                    ) {
                        Icon(imageVector = Icons.Default.HighQuality, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = playerState.currentQuality.badge, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = qualityMenuExpanded,
                        onDismissRequest = { qualityMenuExpanded = false }
                    ) {
                        zamil.getAvailableQualities().forEach { quality ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(quality.titleAr, fontSize = 13.sp)
                                        if (playerState.currentQuality == quality) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("✓", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                },
                                onClick = {
                                    onSwitchQuality(quality)
                                    qualityMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // صورة الغلاف الكبيرة
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .aspectRatio(1f)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
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
                            contentDescription = "غلاف الزامل",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // اسم الزامل والمنشد ومرات الاستماع
            Text(
                text = zamil.title,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(0.9f)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = zamil.performer,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${zamil.playsCount} استماع",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // شريط التفاعل السريع (المفضلة، التنزيل، المشاركة)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (zamil.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "المفضلة",
                        tint = if (zamil.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "تنزيل",
                        tint = if (zamil.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                IconButton(onClick = onShareClick) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // الموجات الصوتية
            Spacer(modifier = Modifier.height(10.dp))
            WaveformVisualizer(isPlaying = playerState.isPlaying)

            // شريط التقديم والتأخير (Seek Slider)
            Spacer(modifier = Modifier.height(8.dp))
            val currentPos = playerState.currentPositionMs.toFloat()
            val totalDur = playerState.durationMs.coerceAtLeast(1L).toFloat()

            Slider(
                value = (currentPos / totalDur).coerceIn(0f, 1f),
                onValueChange = { frac ->
                    onSeekTo((frac * totalDur).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

            // عرض التوقيت الحالي والإجمالي
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp)
            ) {
                Text(text = formatMs(playerState.currentPositionMs), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = formatMs(playerState.durationMs), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // أزرار التحكم الرئيسية (عشوائي، تكرار، تشغيل/إيقاف، تقديم، تأخير)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                // التشغيل العشوائي (Shuffle)
                IconButton(onClick = onToggleShuffle) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "خلط عشوائي",
                        tint = if (playerState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // تأخير 10 ثوان
                IconButton(onClick = onSeekRewind10) {
                    Icon(imageVector = Icons.Default.Replay10, contentDescription = "تأخير 10 ثوان", modifier = Modifier.size(26.dp))
                }

                // السابق
                IconButton(onClick = onPlayPrevious) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "السابق", modifier = Modifier.size(30.dp))
                }

                // تشغيل / إيقاف
                if (playerState.isBuffering) {
                    CircularProgressIndicator(modifier = Modifier.size(56.dp).padding(6.dp), color = MaterialTheme.colorScheme.primary)
                } else {
                    FilledIconButton(
                        onClick = onTogglePlayPause,
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(62.dp)
                    ) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "إيقاف مؤقت" else "تشغيل",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // التالي
                IconButton(onClick = onPlayNext) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "التالي", modifier = Modifier.size(30.dp))
                }

                // تقديم 10 ثوان
                IconButton(onClick = onSeekForward10) {
                    Icon(imageVector = Icons.Default.Forward10, contentDescription = "تقديم 10 ثوان", modifier = Modifier.size(26.dp))
                }

                // التكرار (Repeat: OFF, ALL, ONE)
                IconButton(onClick = onCycleRepeat) {
                    Icon(
                        imageVector = if (playerState.repeatMode == RepeatState.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "تكرار",
                        tint = if (playerState.repeatMode != RepeatState.OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // محدد سرعة التشغيل (1.0x, 1.25x, 1.5x)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(1.0f, 1.25f, 1.5f).forEach { speed ->
                    FilledTonalButton(
                        onClick = { onSetSpeed(speed) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            fontSize = 12.sp,
                            fontWeight = if (playerState.playbackSpeed == speed) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WaveformVisualizer(isPlaying: Boolean, modifier: Modifier = Modifier) {
    val barCount = 18
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(28.dp).padding(horizontal = 20.dp)
    ) {
        repeat(barCount) { index ->
            val animHeight = remember { Animatable(8f) }

            LaunchedEffect(isPlaying) {
                if (isPlaying) {
                    val target = (10 + (index % 5) * 4).toFloat()
                    animHeight.animateTo(
                        targetValue = target,
                        animationSpec = infiniteRepeatable(
                            animation = tween(300 + (index * 40), easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                } else {
                    animHeight.snapTo(8f)
                }
            }

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animHeight.value.dp)
                    .clip(CircleShape)
                    .background(
                        if (isPlaying) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

private fun formatMs(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
