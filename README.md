# Listropik

Listropik es una aplicación móvil de reproducción de audio local para Android que incluye la posibilidad de descargar canciones directamente desde YouTube conectándose a su servidor backend local ([API_Listropik](https://github.com/4x3l-MT/API_Listropik)).

---

##Características

* **Reproductor Local:** Organización y reproducción de archivos de audio guardados en el dispositivo.
* **Descarga vía Red Local:** Conexión con un servidor Python hospedado en tu red local (laptop/PC) para procesar y descargar audio desde YouTube.
* **Interfaz Moderna:** Diseñada con Jetpack Compose y la arquitectura MVVM.

---

##Tecnologías Utilizadas

* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose
* **Arquitectura:** MVVM (Model-View-ViewModel)
* **Red:** Retrofit / OkHttp (para la comunicación con la API)

---

##Requisitos y Configuración

Para utilizar la función de descarga de música:

1. Debes tener ejecutando el backend [API_Listropik](https://github.com/4x3l-MT/API_Listropik) en una computadora conectada a la misma red Wi-Fi que tu dispositivo Android.
2. Ingresa la dirección IP de tu computadora dentro de la configuración de la aplicación móvil.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
