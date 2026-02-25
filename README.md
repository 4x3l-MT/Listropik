# Listropik

[Español](#español) | [English](#english)

---

## Español

### Descripción
Listropik es una aplicación Android para descargar música de YouTube y reproducirla sin conexión a internet. Utiliza una API propia basada en FastAPI y yt-dlp para procesar y descargar el audio.

### Requisitos
- Android 7.0 (API 24) o superior
- Conexión a internet para descargar canciones
- Servidor con la API de Listropik corriendo (ver carpeta `api/`)

**Para la API:**
- Docker
- Python 3.x
- FastAPI
- yt-dlp
- ffmpeg

### Instalación

#### App Android
1. Descarga el APK desde la carpeta `apk/`
2. Instálalo en tu dispositivo Android
3. Asegúrate de tener la API corriendo y configura la URL en `ApiService.kt`

#### API
1. Clona el repositorio
```bash
git clone https://github.com/4x3l-MT/Listropik.git
cd Listropik/api
```
2. Construye y corre el contenedor Docker
```bash
docker build -t listropik-api .
docker run -p 8000:8000 listropik-api
```

### Cómo usar la app
1. Abre Listropik en tu dispositivo
2. Pega el link de YouTube en el campo de texto
3. Toca el botón **Descargar** y espera a que termine
4. La canción aparecerá en **Mis canciones**
5. Toca una canción para reproducirla
6. Usa los controles del reproductor para pausar, adelantar, atrasar o cambiar de canción
7. El mini reproductor en la parte inferior te permite controlar la música sin salir de la pantalla principal

---

## English

### Description
Listropik is an Android app to download music from YouTube and play it offline. It uses a custom API built with FastAPI and yt-dlp to process and download audio.

### Requirements
- Android 7.0 (API 24) or higher
- Internet connection to download songs
- Server running the Listropik API (see `api/` folder)

**For the API:**
- Docker
- Python 3.x
- FastAPI
- yt-dlp
- ffmpeg

### Installation

#### Android App
1. Download the APK from the `apk/` folder
2. Install it on your Android device
3. Make sure the API is running and configure the URL in `ApiService.kt`

#### API
1. Clone the repository
```bash
git clone https://github.com/4x3l-MT/Listropik.git
cd Listropik/api
```
2. Build and run the Docker container
```bash
docker build -t listropik-api .
docker run -p 8000:8000 listropik-api
```

### How to use the app
1. Open Listropik on your device
2. Paste a YouTube link in the text field
3. Tap **Download** and wait for it to finish
4. The song will appear under **My songs**
5. Tap a song to play it
6. Use the player controls to pause, skip forward, skip back or change songs
7. The mini player at the bottom lets you control music without leaving the main screen
