package com.ax3l_mt.listropik

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ax3l_mt.listropik.ui.PantallaPrincipal
import com.ax3l_mt.listropik.ui.PantallaReproductor
import com.ax3l_mt.listropik.ui.theme.ListropikTheme
import com.ax3l_mt.listropik.utils.MediaPlayerManager
import java.io.File

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ListropikTheme {
                AppContent()
            }
        }
    }

    @Composable
    fun AppContent() {
        var cancionActual by remember { mutableStateOf<String?>(null) }
        var enReproductor by remember { mutableStateOf(false) }
        var canciones by remember { mutableStateOf(cargarCanciones()) }

        if (enReproductor && cancionActual != null) {
            PantallaReproductor(
                nombreArchivo = cancionActual!!,
                canciones = canciones,
                onCambiarCancion = { cancionActual = it },
                onVolver = { enReproductor = false }
            )
        } else {
            PantallaPrincipal(
                canciones = canciones,
                cancionReproduciendo = cancionActual,
                onCancionClick = { cancion ->
                    if (cancionActual != cancion) {
                        cancionActual = cancion
                    }
                    enReproductor = true
                },
                onAbrirReproductor = { enReproductor = true },
                onNuevaDescarga = { canciones = cargarCanciones() }
            )
        }
    }

    private fun cargarCanciones(): List<String> {
        val carpeta = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        return carpeta?.listFiles()?.filter { it.extension == "mp3" }?.map { it.name } ?: emptyList()
    }

    override fun onDestroy() {
        super.onDestroy()
        MediaPlayerManager.liberar()
    }
}