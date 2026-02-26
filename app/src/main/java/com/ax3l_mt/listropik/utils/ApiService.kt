package com.ax3l_mt.listropik.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

object ApiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val clientLocal = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .hostnameVerifier { _, _ -> true }
        .sslSocketFactory(
            javax.net.ssl.SSLContext.getInstance("TLS").also {
                it.init(null, arrayOf(object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                }), java.security.SecureRandom()
                )
            }.socketFactory,
            object : javax.net.ssl.X509TrustManager {
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
            }
        )
        .build()

    private const val URL_PUBLICA = "https://listropik.duckdns.org/descargar"
    private const val URL_LOCAL = "https://192.168.1.84/descargar"

    private fun estaEnRedLocal(): Boolean {
        return try {
            val request = Request.Builder()
                .url("http://192.168.1.84")
                .build()
            val response = clientLocal.newCall(request).execute()
            response.code in 200..499
        } catch (e: Exception) {
            false
        }
    }

    fun descargarCancion(url: String, carpeta: File): String {
        return try {
            val json = """{"url": "$url"}"""
            val body = json.toRequestBody("application/json".toMediaType())

            val urlFinal = if (estaEnRedLocal()) URL_LOCAL else URL_PUBLICA

            val request = Request.Builder()
                .url(urlFinal)
                .post(body)
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val bytes = response.body?.bytes()

                if (bytes == null || bytes.isEmpty()) {
                    return "❌ El servidor no devolvió datos"
                }

                val contentDisposition = response.header("Content-Disposition") ?: ""
                val nombreArchivo = when {
                    contentDisposition.contains("filename=\"") ->
                        contentDisposition.substringAfter("filename=\"").substringBefore("\"").trim()
                    contentDisposition.contains("filename=") ->
                        contentDisposition.substringAfter("filename=").substringBefore(";").trim()
                    else -> "cancion_${System.currentTimeMillis()}.mp3"
                }

                val archivo = File(carpeta, nombreArchivo)
                FileOutputStream(archivo).use { it.write(bytes) }

                if (archivo.exists()) {
                    "✅ Descargado: $nombreArchivo"
                } else {
                    "❌ El archivo no se guardó correctamente"
                }
            } else {
                "❌ Error del servidor: ${response.code}"
            }
        } catch (e: Exception) {
            "❌ Error: ${e.message}"
        }
    }
}