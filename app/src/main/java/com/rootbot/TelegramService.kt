package com.rootbot

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

class TelegramService(private val token: String, private val chatId: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val baseUrl = "https://api.telegram.org/bot$token"

    fun sendMessage(text: String): Boolean {
        if (token.isEmpty() || chatId.isEmpty()) return false
        return try {
            val json = """{"chat_id":"$chatId","text":"$text","parse_mode":"HTML"}"""
            val body = json.toRequestBody("application/json".toMediaType())
            val req = Request.Builder().url("$baseUrl/sendMessage").post(body).build()
            client.newCall(req).execute().isSuccessful
        } catch (e: Exception) { false }
    }

    fun sendPhoto(file: File, caption: String = ""): Boolean {
        if (token.isEmpty() || chatId.isEmpty() || !file.exists()) return false
        return try {
            val body = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("chat_id", chatId)
                .addFormDataPart("caption", caption)
                .addFormDataPart("photo", file.name,
                    file.asRequestBody("image/png".toMediaType()))
                .build()
            val req = Request.Builder().url("$baseUrl/sendPhoto").post(body).build()
            client.newCall(req).execute().isSuccessful
        } catch (e: Exception) { false }
    }
}
