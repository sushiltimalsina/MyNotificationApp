package com.example.esewaspeaker

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {
    // Paste your key here for testing. Never share or upload this file with the key in it.
    private const val API_KEY = "AIzaSyB4O8bJQQxe6T2yyqNbmJiJDpxJc3aeu-k"
    private const val MODEL = "gemini-2.5-flash-lite" // check AI Studio for a model your free tier allows

    /** Returns a short Nepali sentence, or null if anything fails. Call from a background thread. */
    fun summarizeToNepali(cleanText: String): String? {
        if (API_KEY.startsWith("PASTE")) return null
        return try {
            val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", API_KEY)
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true

            val prompt = "Summarize this payment app notification as ONE short Nepali sentence " +
                    "(Devanagari script). Mention what happened and the amount if present. " +
                    "Reply with only the sentence.\n\nNotification: $cleanText"

            val body = JSONObject().put(
                "contents",
                JSONArray().put(
                    JSONObject().put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                )
            )
            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            if (conn.responseCode != 200) return null // includes 429 (limit reached)
            val resp = conn.inputStream.bufferedReader().readText()
            JSONObject(resp)
                .getJSONArray("candidates").getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts").getJSONObject(0)
                .getString("text").trim()
        } catch (e: Exception) {
            null
        }
    }
}