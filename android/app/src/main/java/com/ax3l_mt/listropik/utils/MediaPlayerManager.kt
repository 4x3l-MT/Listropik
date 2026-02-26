package com.ax3l_mt.listropik.utils

import android.media.MediaPlayer
import java.io.File

object MediaPlayerManager {

    private var mediaPlayer: MediaPlayer? = null

    fun reproducir(archivo: File) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer()
        mediaPlayer?.setDataSource(archivo.absolutePath)
        mediaPlayer?.prepare()
        mediaPlayer?.start()
    }

    fun pausar() = mediaPlayer?.pause()

    fun reanudar() = mediaPlayer?.start()

    fun adelantar(ms: Int = 10000) {
        mediaPlayer?.let {
            it.seekTo((it.currentPosition + ms).coerceAtMost(it.duration))
        }
    }

    fun atrasar(ms: Int = 10000) {
        mediaPlayer?.let {
            it.seekTo((it.currentPosition - ms).coerceAtLeast(0))
        }
    }

    fun seekTo(posicion: Int) = mediaPlayer?.seekTo(posicion)

    fun isPlaying() = mediaPlayer?.isPlaying ?: false

    fun getPosicion() = mediaPlayer?.currentPosition ?: 0

    fun getDuracion() = mediaPlayer?.duration ?: 0

    fun liberar() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}