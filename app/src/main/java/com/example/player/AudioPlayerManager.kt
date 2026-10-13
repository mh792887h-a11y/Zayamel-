package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import com.example.data.model.AudioQuality
import com.example.data.model.Zamil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

enum class RepeatState {
    OFF, ALL, ONE
}

data class PlayerState(
    val currentZamil: Zamil? = null,
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatState = RepeatState.OFF,
    val isShuffle: Boolean = false,
    val currentQuality: AudioQuality = AudioQuality.STANDARD,
    val errorMessage: String? = null
)

class AudioPlayerManager(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var tickerJob: Job? = null
    private var playCountRecordJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var originalPlaylist: List<Zamil> = emptyList()
    private var activePlaylist: List<Zamil> = emptyList()

    var onPlayCountEligible: ((Zamil) -> Unit)? = null
    var onHistoryPositionUpdate: ((zamilId: String, positionMs: Long) -> Unit)? = null

    companion object {
        private const val TAG = "AudioPlayerManager"
    }

    fun playZamil(
        zamil: Zamil,
        playlist: List<Zamil> = emptyList(),
        startPositionMs: Long = 0L
    ) {
        if (playlist.isNotEmpty()) {
            originalPlaylist = playlist
            activePlaylist = if (_state.value.isShuffle) playlist.shuffled() else playlist
        } else if (!activePlaylist.any { it.id == zamil.id }) {
            originalPlaylist = listOf(zamil)
            activePlaylist = listOf(zamil)
        }

        // إذا كان نفس الزامل قيد التشغيل بالفعل ومطلوب استئنافه فقط
        if (_state.value.currentZamil?.id == zamil.id && mediaPlayer != null && startPositionMs == 0L) {
            if (!_state.value.isPlaying) {
                resume()
            }
            return
        }

        releasePlayer()

        _state.value = _state.value.copy(
            currentZamil = zamil,
            isBuffering = true,
            isPlaying = false,
            errorMessage = null,
            currentPositionMs = startPositionMs,
            durationMs = (zamil.durationSeconds * 1000L).coerceAtLeast(0L)
        )

        // تحديد مسار التشغيل: محلي إذا كان منزلاً، أو الرابط السحابي بحسب الجودة
        val dataSourceUrl = if (zamil.isDownloaded && !zamil.localAudioPath.isNullOrBlank() && File(zamil.localAudioPath).exists()) {
            zamil.localAudioPath
        } else {
            zamil.getAudioUrlForQuality(_state.value.currentQuality)
        }

        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )

                setDataSource(dataSourceUrl)

                setOnPreparedListener { mp ->
                    _state.value = _state.value.copy(
                        isBuffering = false,
                        isPlaying = true,
                        durationMs = mp.duration.toLong().coerceAtLeast(0L)
                    )
                    applyPlaybackSpeed()
                    if (startPositionMs > 0 && startPositionMs < mp.duration) {
                        mp.seekTo(startPositionMs.toInt())
                    }
                    mp.start()
                    startProgressTicker()
                    schedulePlayCountRecording(zamil)
                }

                setOnCompletionListener {
                    handlePlaybackComplete()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error what=$what extra=$extra")
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        isBuffering = false,
                        errorMessage = "تعذر تشغيل المقطع الصوتي، يرجى التحقق من الرابط والاتصال"
                    )
                    true
                }

                prepareAsync()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize player: ${e.message}")
            _state.value = _state.value.copy(
                isPlaying = false,
                isBuffering = false,
                errorMessage = "خطأ في تهيئة المشغل: ${e.message}"
            )
        }
    }

    // احتساب الاستماع الفعلي بعد مرور 7 ثوانٍ من التشغيل الفعلي (منع التكرار الوهمي)
    private fun schedulePlayCountRecording(zamil: Zamil) {
        playCountRecordJob?.cancel()
        playCountRecordJob = scope.launch {
            delay(7000)
            if (_state.value.isPlaying && _state.value.currentZamil?.id == zamil.id) {
                onPlayCountEligible?.invoke(zamil)
            }
        }
    }

    fun switchQuality(quality: AudioQuality) {
        val current = _state.value.currentZamil ?: return
        if (_state.value.currentQuality == quality) return

        val currentPos = _state.value.currentPositionMs
        _state.value = _state.value.copy(currentQuality = quality)
        // إعادة التشغيل مع الحفاظ التام على موضع التوقف
        playZamil(current, activePlaylist, startPositionMs = currentPos)
    }

    fun toggleShuffle() {
        val newShuffle = !_state.value.isShuffle
        _state.value = _state.value.copy(isShuffle = newShuffle)
        activePlaylist = if (newShuffle) originalPlaylist.shuffled() else originalPlaylist
    }

    fun cycleRepeatMode() {
        val nextMode = when (_state.value.repeatMode) {
            RepeatState.OFF -> RepeatState.ALL
            RepeatState.ALL -> RepeatState.ONE
            RepeatState.ONE -> RepeatState.OFF
        }
        _state.value = _state.value.copy(repeatMode = nextMode)
    }

    fun togglePlayPause() {
        if (_state.value.isPlaying) {
            pause()
        } else {
            resume()
        }
    }

    fun pause() {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                }
            }
            _state.value = _state.value.copy(isPlaying = false)
            stopProgressTicker()
            saveCurrentHistory()
        } catch (e: Exception) {
            Log.e(TAG, "Pause error: ${e.message}")
        }
    }

    fun resume() {
        try {
            mediaPlayer?.let {
                it.start()
                _state.value = _state.value.copy(isPlaying = true)
                startProgressTicker()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resume error: ${e.message}")
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _state.value = _state.value.copy(currentPositionMs = positionMs)
            saveCurrentHistory()
        } catch (e: Exception) {
            Log.e(TAG, "Seek error: ${e.message}")
        }
    }

    fun seekForward10() {
        val target = (_state.value.currentPositionMs + 10_000L).coerceAtMost(_state.value.durationMs)
        seekTo(target)
    }

    fun seekRewind10() {
        val target = (_state.value.currentPositionMs - 10_000L).coerceAtLeast(0L)
        seekTo(target)
    }

    fun playNext() {
        val current = _state.value.currentZamil ?: return
        if (activePlaylist.isEmpty()) return

        val currentIndex = activePlaylist.indexOfFirst { it.id == current.id }
        if (currentIndex != -1 && currentIndex + 1 < activePlaylist.size) {
            playZamil(activePlaylist[currentIndex + 1], activePlaylist)
        } else if (_state.value.repeatMode == RepeatState.ALL && activePlaylist.isNotEmpty()) {
            playZamil(activePlaylist.first(), activePlaylist)
        }
    }

    fun playPrevious() {
        val current = _state.value.currentZamil ?: return
        if (activePlaylist.isEmpty()) return

        val currentIndex = activePlaylist.indexOfFirst { it.id == current.id }
        if (currentIndex > 0) {
            playZamil(activePlaylist[currentIndex - 1], activePlaylist)
        } else {
            seekTo(0)
        }
    }

    fun setSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        applyPlaybackSpeed()
    }

    private fun applyPlaybackSpeed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.let {
                    if (it.isPlaying) {
                        it.playbackParams = it.playbackParams.setSpeed(_state.value.playbackSpeed)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Could not set playback speed: ${e.message}")
            }
        }
    }

    private fun handlePlaybackComplete() {
        when (_state.value.repeatMode) {
            RepeatState.ONE -> {
                seekTo(0)
                resume()
            }
            RepeatState.ALL -> {
                playNext()
            }
            RepeatState.OFF -> {
                val current = _state.value.currentZamil
                val currentIndex = activePlaylist.indexOfFirst { it.id == current?.id }
                if (currentIndex != -1 && currentIndex + 1 < activePlaylist.size) {
                    playNext()
                } else {
                    _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0L)
                    stopProgressTicker()
                }
            }
        }
    }

    private fun saveCurrentHistory() {
        val zamil = _state.value.currentZamil ?: return
        val pos = _state.value.currentPositionMs
        if (pos > 1000) {
            onHistoryPositionUpdate?.invoke(zamil.id, pos)
        }
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        tickerJob = scope.launch {
            while (isActive) {
                try {
                    mediaPlayer?.let {
                        if (it.isPlaying) {
                            val pos = it.currentPosition.toLong()
                            val dur = it.duration.toLong().coerceAtLeast(0L)
                            _state.value = _state.value.copy(
                                currentPositionMs = pos,
                                durationMs = dur
                            )
                        }
                    }
                } catch (_: Exception) { }
                delay(250)
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    fun releasePlayer() {
        stopProgressTicker()
        playCountRecordJob?.cancel()
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) { }
        mediaPlayer = null
    }
}
