package com.ax3lmt.listropik.ui

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ax3lmt.listropik.model.MusicFile
import com.ax3lmt.listropik.player.MusicPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel : ViewModel() {

    val playerManager = MusicPlayerManager()

    private val _laptopPort = MutableStateFlow("192.168.1.100:8000")
    val laptopPort: StateFlow<String> = _laptopPort.asStateFlow()

    private val _youtubeUrl = MutableStateFlow("")
    val youtubeUrl: StateFlow<String> = _youtubeUrl.asStateFlow()

    private val _musicFiles = MutableStateFlow<List<MusicFile>>(emptyList())
    val musicFiles: StateFlow<List<MusicFile>> = _musicFiles.asStateFlow()

    private val _isFolderFound = MutableStateFlow(false)
    val isFolderFound: StateFlow<Boolean> = _isFolderFound.asStateFlow()

    private val _folderPathInfo = MutableStateFlow("")
    val folderPathInfo: StateFlow<String> = _folderPathInfo.asStateFlow()

    private val _isPlayerExpanded = MutableStateFlow(false)
    val isPlayerExpanded: StateFlow<Boolean> = _isPlayerExpanded.asStateFlow()

    private val _connectionStatus = MutableStateFlow("")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()

    private val _downloadStatus = MutableStateFlow("")
    val downloadStatus: StateFlow<String> = _downloadStatus.asStateFlow()

    fun onPortChanged(newPort: String) {
        _laptopPort.value = newPort
        _connectionStatus.value = ""
    }

    fun onYoutubeUrlChanged(newUrl: String) {
        _youtubeUrl.value = newUrl
        _downloadStatus.value = ""
    }

    fun setPlayerExpanded(expanded: Boolean) {
        _isPlayerExpanded.value = expanded
    }

    private fun formatBaseUrl(input: String): String {
        var url = input.trim()
        if (url.isEmpty()) return ""
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://$url"
        }
        val schemeEnd = url.indexOf("://") + 3
        val hostAndPort = url.substring(schemeEnd)
        if (!hostAndPort.contains(":")) {
            url = "$url:8000"
        }
        return url.removeSuffix("/")
    }

    fun testConnection() {
        viewModelScope.launch {
            val baseUrl = formatBaseUrl(_laptopPort.value)
            if (baseUrl.isEmpty()) {
                _connectionStatus.value = "Ingresa la IP de tu laptop (ej: 192.168.1.15:8000)"
                return@launch
            }
            _connectionStatus.value = "Conectando a $baseUrl..."

            val result = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$baseUrl/ping")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.requestMethod = "GET"
                    val code = conn.responseCode
                    if (code == 200) {
                        "¡Conexión exitosa con la laptop! ($baseUrl)"
                    } else {
                        "Respuesta del servidor HTTP $code"
                    }
                } catch (e: Exception) {
                    "No se pudo conectar a $baseUrl. Revisa la IP y que la laptop ejecute el servidor."
                }
            }
            _connectionStatus.value = result
        }
    }

    fun downloadYoutubeAudio(context: Context) {
        val urlToDownload = _youtubeUrl.value.trim()
        if (urlToDownload.isEmpty()) {
            _downloadStatus.value = "Por favor ingresa o pega un enlace de YouTube"
            return
        }
        val baseUrl = formatBaseUrl(_laptopPort.value)
        if (baseUrl.isEmpty()) {
            _downloadStatus.value = "Por favor ingresa la IP de la laptop primero"
            return
        }

        viewModelScope.launch {
            _isDownloading.value = true
            _downloadStatus.value = "Procesando y descargando audio en la laptop..."

            val success = withContext(Dispatchers.IO) {
                try {
                    val url = URL("$baseUrl/download")
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 120000 // downloading audio may take 10-30 seconds
                    conn.requestMethod = "POST"
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.doOutput = true

                    val jsonBody = "{\"url\":\"$urlToDownload\"}"
                    conn.outputStream.use { os ->
                        os.write(jsonBody.toByteArray(Charsets.UTF_8))
                    }

                    val code = conn.responseCode
                    if (code == 200) {
                        var filename = "audio_${System.currentTimeMillis()}.mp3"
                        val cd = conn.getHeaderField("Content-Disposition")
                        if (!cd.isNullOrEmpty() && cd.contains("filename=")) {
                            val extracted = cd.substringAfter("filename=").replace("\"", "").trim()
                            if (extracted.isNotEmpty()) filename = extracted
                        }

                        val targetDir = getTargetMusicDirectory(context)
                        if (!targetDir.exists()) targetDir.mkdirs()

                        val outputFile = File(targetDir, filename)
                        conn.inputStream.use { input ->
                            outputFile.outputStream().use { output ->
                                input.copyTo(output)
                            }
                        }
                        _downloadStatus.value = "¡Audio guardado exitosamente en 'music'!: $filename"
                        true
                    } else {
                        _downloadStatus.value = "Error $code al procesar enlace en la laptop"
                        false
                    }
                } catch (e: Exception) {
                    _downloadStatus.value = "Error de conexión: ${e.localizedMessage}"
                    false
                }
            }

            _isDownloading.value = false
            if (success) {
                _youtubeUrl.value = ""
                scanMusicFolder(context)
            }
        }
    }

    fun deleteMusicFile(context: Context, fileToDelete: MusicFile) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val playbackState = playerManager.playbackState.value
                    if (playbackState.currentFile?.id == fileToDelete.id) {
                        withContext(Dispatchers.Main) {
                            playerManager.playNext(context)
                            if (playerManager.playbackState.value.currentFile?.id == fileToDelete.id) {
                                playerManager.release()
                            }
                        }
                    }

                    var deleted = false
                    val physicalFile = File(fileToDelete.path)
                    if (physicalFile.exists()) {
                        deleted = physicalFile.delete()
                    }

                    if (!deleted && fileToDelete.uri.scheme == "content") {
                        try {
                            val count = context.contentResolver.delete(fileToDelete.uri, null, null)
                            deleted = count > 0
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            scanMusicFolder(context)
        }
    }

    private fun getTargetMusicDirectory(context: Context): File {
        val rootExt = Environment.getExternalStorageDirectory()
        val primaryFolder = File(rootExt, "music")
        if (primaryFolder.exists() || rootExt.canWrite()) {
            return primaryFolder
        }
        val musicPublic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val publicFolder = File(musicPublic, "music")
        if (publicFolder.exists()) return publicFolder

        return File(context.getExternalFilesDir(null), "music")
    }

    fun scanMusicFolder(context: Context) {
        viewModelScope.launch {
            val (files, folderFound, pathInfo) = withContext(Dispatchers.IO) {
                findAndLoadMusicFiles(context.applicationContext)
            }
            _musicFiles.value = files
            _isFolderFound.value = folderFound
            _folderPathInfo.value = pathInfo
            playerManager.updatePlaylist(files)
        }
    }

    private fun findAndLoadMusicFiles(context: Context): Triple<List<MusicFile>, Boolean, String> {
        val audioExtensions = setOf("mp3", "wav", "m4a", "aac", "flac", "ogg", "opus", "wma")
        val foundFiles = mutableListOf<MusicFile>()
        val searchedPaths = mutableListOf<String>()

        val rootExt = Environment.getExternalStorageDirectory()
        val musicPublic = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        val appExtDir = context.getExternalFilesDir(null)

        val candidateDirs = listOfNotNull(
            File(rootExt, "music"),
            File(rootExt, "Music/music"),
            File(musicPublic, "music"),
            File(appExtDir, "music"),
            File(rootExt, "Download/music")
        )

        var folderFound = false
        var primaryPathInfo = ""

        for (dir in candidateDirs) {
            searchedPaths.add(dir.absolutePath)
            if (dir.exists() && dir.isDirectory) {
                folderFound = true
                primaryPathInfo = dir.absolutePath
                val files = dir.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isFile && (file.extension.lowercase() in audioExtensions)) {
                            foundFiles.add(
                                MusicFile(
                                    id = file.absolutePath,
                                    title = file.nameWithoutExtension,
                                    artist = "Local File",
                                    path = file.absolutePath,
                                    uri = Uri.fromFile(file),
                                    durationMs = 0L,
                                    sizeBytes = file.length()
                                )
                            )
                        }
                    }
                }
            }
        }

        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.SIZE
            )

            val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
            val selectionArgs = arrayOf("%/music/%")

            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )

            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val dataCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val sizeCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val dataPath = c.getString(dataCol) ?: ""
                    val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    if (foundFiles.none { it.path == dataPath || it.id == contentUri.toString() }) {
                        folderFound = true
                        val title = c.getString(titleCol) ?: File(dataPath).nameWithoutExtension
                        val artist = c.getString(artistCol) ?: "Unknown Artist"
                        val duration = c.getLong(durationCol)
                        val size = c.getLong(sizeCol)

                        foundFiles.add(
                            MusicFile(
                                id = contentUri.toString(),
                                title = title,
                                artist = artist,
                                path = dataPath,
                                uri = contentUri,
                                durationMs = duration,
                                sizeBytes = size
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!folderFound) {
            val appMusicFolder = File(appExtDir, "music")
            if (!appMusicFolder.exists()) {
                appMusicFolder.mkdirs()
            }
            primaryPathInfo = appMusicFolder.absolutePath
        }

        val infoMsg = if (folderFound) {
            "Carpeta 'music': $primaryPathInfo (${foundFiles.size} canciones)"
        } else {
            "Crea la carpeta 'music' en la memoria del teléfono y guarda tus archivos ahí."
        }

        return Triple(foundFiles.distinctBy { it.path }, folderFound, infoMsg)
    }

    override fun onCleared() {
        super.onCleared()
        playerManager.release()
    }
}
