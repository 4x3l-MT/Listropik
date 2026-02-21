import yt_dlp
import os
def descargar_musica(url,carpeta="Mi_Musica"):
    try:
        if not os.path.exists(carpeta):
            os.makedirs(carpeta)
        opciones = {
            'format' : 'bestaudio/best',
            'outtmpl' : f'{carpeta}/%(title)s.%(ext)s',
            'postprocessors': [{
                'key': 'FFmpegExtractAudio',
                'preferredcodec': 'mp3',
                'preferredquality': '192',
            }],
        }
        print("Descargando musica...")
        with yt_dlp.YoutubeDL(opciones) as ydl:
            ydl.download([url])
            print("Descarga completada.")
    except Exception as e:
        print(f"Error al descargar la musica: {e}")
if __name__ == "__main__":
    url = input("Ingrese la URL del video de YouTube: ")
    descargar_musica(url)
    