package com.ax3l_mt.listropik.ui

import android.os.Environment
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ax3l_mt.listropik.utils.ApiService
import com.ax3l_mt.listropik.utils.MediaPlayerManager
import kotlinx.coroutines.*
import java.io.File

@Composable
fun PantallaPrincipal(
    canciones: List<String>,
    cancionReproduciendo: String?,
    onCancionClick: (String) -> Unit,
    onAbrirReproductor: () -> Unit,
    onNuevaDescarga: () -> Unit
) {
    val context = LocalContext.current
    var link by remember { mutableStateOf("") }
    var descargando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var busqueda by remember { mutableStateOf("") }
    var reproduciendo by remember { mutableStateOf(MediaPlayerManager.isPlaying()) }
    var cancionAEliminar by remember { mutableStateOf<String?>(null) }

    val cancionesFiltradas = remember(busqueda, canciones) {
        if (busqueda.isBlank()) canciones
        else canciones.filter { it.contains(busqueda, ignoreCase = true) }
    }

    // Dialogo de confirmación para eliminar
    if (cancionAEliminar != null) {
        AlertDialog(
            onDismissRequest = { cancionAEliminar = null },
            title = { Text("Eliminar canción", color = Color.White) },
            text = { Text("¿Eliminar \"${cancionAEliminar!!.removeSuffix(".mp3")}\"?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    val carpeta = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
                    val archivo = File(carpeta, cancionAEliminar!!)
                    if (archivo.exists()) archivo.delete()
                    cancionAEliminar = null
                    onNuevaDescarga()
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { cancionAEliminar = null }) {
                    Text("Cancelar", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF1E1E1E)
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF121212)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎵 Listropik",
                    color = Color.White,
                    fontSize = 24.sp,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it },
                    label = { Text("Pega el link de YouTube aquí", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (link.isNotBlank()) {
                            descargando = true
                            mensaje = ""
                            CoroutineScope(Dispatchers.IO).launch {
                                val carpeta = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
                                val resultado = ApiService.descargarCancion(link, carpeta)
                                withContext(Dispatchers.Main) {
                                    mensaje = resultado
                                    descargando = false
                                    link = ""
                                    onNuevaDescarga()
                                }
                            }
                        }
                    },
                    enabled = !descargando,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text(if (descargando) "Descargando..." else "Descargar")
                }

                if (descargando) {
                    CircularProgressIndicator(
                        color = Color(0xFF1DB954),
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)
                    )
                }

                if (mensaje.isNotBlank()) {
                    Text(
                        text = mensaje,
                        color = if (mensaje.startsWith("✅")) Color(0xFF1DB954) else Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    label = { Text("Buscar canción", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF1DB954),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text("Mis canciones", color = Color.Gray, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyColumn(modifier = Modifier.weight(1f)) {
                items(cancionesFiltradas) { cancion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCancionClick(cancion) }
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎵 ${cancion.removeSuffix(".mp3")}",
                            color = if (cancion == cancionReproduciendo) Color(0xFF1DB954) else Color.White,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { cancionAEliminar = cancion }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Gray)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF333333))
                }
            }

            // Mini reproductor
            if (cancionReproduciendo != null) {
                Surface(color = Color(0xFF1E1E1E)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAbrirReproductor() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎵 ${cancionReproduciendo.removeSuffix(".mp3")}",
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            if (MediaPlayerManager.isPlaying()) {
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
                                tint = Color(0xFF1DB954)
                            )
                        }
                    }
                }
            }
        }
    }
}