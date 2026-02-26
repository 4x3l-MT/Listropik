package com.ax3l_mt.listropik.ui

import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax3l_mt.listropik.utils.MediaPlayerManager
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun PantallaReproductor(
    nombreArchivo: String,
    canciones: List<String>,
    onCambiarCancion: (String) -> Unit,
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var reproduciendo by remember { mutableStateOf(false) }
    var progreso by remember { mutableStateOf(0f) }
    var posicion by remember { mutableStateOf(0) }
    var duracion by remember { mutableStateOf(0) }

    // Solo reproduce si no está sonando ya esta canción
    var ultimaCancionReproducida by remember { mutableStateOf("") }

    LaunchedEffect(nombreArchivo) {
        val carpeta = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        val archivo = File(carpeta, nombreArchivo)
        if (nombreArchivo != ultimaCancionReproducida) {
            MediaPlayerManager.reproducir(archivo)
            ultimaCancionReproducida = nombreArchivo
        }
        reproduciendo = MediaPlayerManager.isPlaying()
        duracion = MediaPlayerManager.getDuracion()
    }

    // Actualizar progreso
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            posicion = MediaPlayerManager.getPosicion()
            duracion = MediaPlayerManager.getDuracion()
            progreso = if (duracion > 0) posicion.toFloat() / duracion else 0f
            reproduciendo = MediaPlayerManager.isPlaying()
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onVolver) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = Color.White
                    )
                }
            }

            Text(
                text = nombreArchivo.removeSuffix(".mp3"),
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Slider(
                    value = progreso,
                    onValueChange = { valor ->
                        progreso = valor
                        MediaPlayerManager.seekTo((valor * duracion).toInt())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFF1DB954),
                        activeTrackColor = Color(0xFF1DB954),
                        inactiveTrackColor = Color(0xFF333333)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatearTiempo(posicion), color = Color.Gray, fontSize = 12.sp)
                    Text(formatearTiempo(duracion), color = Color.Gray, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = {
                        val index = canciones.indexOf(nombreArchivo)
                        if (index > 0) onCambiarCancion(canciones[index - 1])
                    }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Anterior", tint = Color.White, modifier = Modifier.size(48.dp))
                    }

                    IconButton(onClick = { MediaPlayerManager.atrasar() }) {
                        Icon(Icons.Default.Replay10, contentDescription = "Atrasar 10s", tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                    IconButton(onClick = {
                        if (reproduciendo) {
                            MediaPlayerManager.pausar()
                            reproduciendo = false
                        } else {
                            MediaPlayerManager.reanudar()
                            reproduciendo = true
                        }
                    }) {
                        Icon(
                            if (reproduciendo) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFF1DB954),
                            modifier = Modifier.size(72.dp)
                        )
                    }

                    IconButton(onClick = { MediaPlayerManager.adelantar() }) {
                        Icon(Icons.Default.Forward10, contentDescription = "Adelantar 10s", tint = Color.White, modifier = Modifier.size(40.dp))
                    }

                    IconButton(onClick = {
                        val index = canciones.indexOf(nombreArchivo)
                        if (index < canciones.size - 1) onCambiarCancion(canciones[index + 1])
                    }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Siguiente", tint = Color.White, modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
    }
}

fun formatearTiempo(ms: Int): String {
    val segundos = (ms / 1000) % 60
    val minutos = (ms / 1000) / 60
    return "%d:%02d".format(minutos, segundos)
}