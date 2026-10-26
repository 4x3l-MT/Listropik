package com.ax3lmt.listropik.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import com.ax3lmt.listropik.model.MusicFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PlaybackState(
    val currentFile: MusicFile? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playlist: List<MusicFile> = emptyList()
)

class MusicPlayerManager {
    private var mediaPlayer: MediaPlayer? = null
    private var updateProgressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    fun updatePlaylist(files: List<MusicFile>) {
        _playbackState.update { it.copy(playlist = files) }
    }

    fun playFile(context: Context, file: MusicFile) {
        val applicationContext = context.applicationContext
        try {
            stopProgressTracker()
            mediaPlayer?.release()

            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(applicationContext, file.uri)
                prepare()
                start()
            }

            val fileDuration = if ((mediaPlayer?.duration != null) && (mediaPlayer!!.duration > 0)) {
                mediaPlayer!!.duration.toLong()
            } else {
                file.durationMs
            }

            _playbackState.update {
                it.copy(
                    currentFile = file,
                    isPlaying = true,
                    currentPositionMs = 0L,
                    durationMs = fileDuration
                )
            }

            mediaPlayer?.setOnCompletionListener {
                playNext(applicationContext)
            }

            startProgressTracker()
        } catch (e: Exception) {
            e.printStackTrace()
            _playbackState.update {
                it.copy(isPlaying = false)
            }
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return

        if (player.isPlaying) {
            player.pause()
            stopProgressTracker()
            _playbackState.update { it.copy(isPlaying = false) }
        } else {
            player.start()
            startProgressTracker()
            _playbackState.update { it.copy(isPlaying = true) }
        }
    }

    fun playNext(context: Context) {
        val currentState = _playbackState.value
        val playlist = currentState.playlist
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == currentState.currentFile?.id }
        val nextIndex = if (currentIndex != -1 && currentIndex + 1 < playlist.size) {
            currentIndex + 1
        } else {
            0 // Loop back to start
        }

        playFile(context, playlist[nextIndex])
    }

    fun playPrevious(context: Context) {
        val currentState = _playbackState.value
        val playlist = currentState.playlist
        if (playlist.isEmpty()) return

        val currentIndex = playlist.indexOfFirst { it.id == currentState.currentFile?.id }
        val prevIndex = if (currentIndex > 0) {
            currentIndex - 1
        } else {
            playlist.size - 1 // Go to last item
        }

        playFile(context, playlist[prevIndex])
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
                _playbackState.update { it.copy(currentPositionMs = positionMs) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun startProgressTracker() {
        stopProgressTracker()
        updateProgressJob = scope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        val current = player.currentPosition.toLong()
                        val dur = if (player.duration > 0) player.duration.toLong() else _playbackState.value.durationMs
                        _playbackState.update {
                            it.copy(
                                currentPositionMs = current,
                                durationMs = dur
                            )
                        }
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracker() {
        updateProgressJob?.cancel()
        updateProgressJob = null
    }

    fun release() {
        stopProgressTracker()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
