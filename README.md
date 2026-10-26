# Listropik 

Listropik is a local audio player for Android designed with a modern user interface and local network integration to process and download music directly from YouTube using its companion backend server ([API_Listropik](https://github.com/4x3l-MT/API_Listropik)).

---

##  Features

- **Local Audio Player:** Organize and play audio files stored directly on your Android device.

- **Network-Based Downloads:** Connect to a Python server hosted on your local Wi-Fi network (laptop/PC) to download audio tracks from YouTube.

- **Modern UI:** Built from scratch using Jetpack Compose and following the MVVM architecture pattern.


---

##  Tech Stack

- **Language:** Kotlin

- **UI Framework:** Jetpack Compose

- **Architecture:** MVVM (Model-View-ViewModel)

- **Networking:** Retrofit / OkHttp (for local API communication)


---

##  Requirements & Setup

To use the YouTube download feature:

1. Ensure the backend server ([API_Listropik](https://github.com/4x3l-MT/API_Listropik)) is running on a computer connected to the same Wi-Fi network as your Android device.

2. Enter your computer's local IP address in the app settings to link the client with the backend.

---

##  License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/4x3l-MT/Listropik?tab=MIT-1-ov-file) file for details.
