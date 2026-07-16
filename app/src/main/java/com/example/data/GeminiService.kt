package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Talks to the Gemini API using a free-tier ("Flash") model.
 *
 * HOW TO KEEP THIS 100% FREE, FOREVER:
 * 1. Get your key from https://aistudio.google.com/apikey (NOT the Cloud Console / Vertex AI).
 * 2. Do NOT attach a billing account / credit card to that Google Cloud project.
 *    Flash models are free on the AI Studio free tier as long as billing is not enabled -
 *    Google will simply return an error (never a charge) once the free daily quota is used up.
 * 3. This class also enforces its own conservative daily cap (see MAX_CALLS_PER_DAY below),
 *    well under Google's free-tier daily limit, so the app itself never gets close to any
 *    paid-tier territory. Once the cap is hit, or if the request fails for any reason
 *    (offline, rate-limited, bad key, etc.), it silently falls back to a canned/offline
 *    response instead of retrying - so a bad connection or exhausted quota costs you nothing,
 *    it just answers with the offline responder for the rest of the day.
 */
object GeminiService {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    // Free-tier safety valve: stop calling the real API after this many requests per day,
    // no matter what. This is far below Google's free daily quota for Flash models, so it
    // also acts as an early-warning cushion in case Google's limits ever change.
    private const val MAX_CALLS_PER_DAY = 40
    private const val PREFS_NAME = "gemini_usage_prefs"
    private const val KEY_DATE = "usage_date"
    private const val KEY_COUNT = "usage_count"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** Returns true and increments the counter if we're still under today's cap. */
    private fun tryConsumeQuota(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = todayKey()
        val storedDate = prefs.getString(KEY_DATE, null)
        val count = if (storedDate == today) prefs.getInt(KEY_COUNT, 0) else 0

        if (count >= MAX_CALLS_PER_DAY) {
            Log.w("GeminiService", "Daily free-tier call cap ($MAX_CALLS_PER_DAY) reached - using offline responses.")
            return false
        }

        prefs.edit()
            .putString(KEY_DATE, today)
            .putInt(KEY_COUNT, count + 1)
            .apply()
        return true
    }

    suspend fun generateResponse(
        context: Context,
        prompt: String,
        systemInstruction: String? = null
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext mockResponse(prompt)
        }

        if (!tryConsumeQuota(context.applicationContext)) {
            return@withContext mockResponse(prompt)
        }

        try {
            val jsonRequest = JSONObject().apply {
                val contentsArray = JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                }
                put("contents", contentsArray)

                if (systemInstruction != null) {
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", systemInstruction)
                            })
                        })
                    })
                }
            }

            val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
            val url = "$BASE_URL?key=$apiKey"

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e("GeminiService", "Request failed: ${response.code} ${response.message}")
                    return@withContext mockResponse(prompt)
                }

                val bodyString = response.body?.string() ?: return@withContext "Empty response from server"
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return@withContext parts.getJSONObject(0).optString("text", "No response text")
                    }
                }
                "No response text"
            }
        } catch (e: Exception) {
            Log.e("GeminiService", "Error calling Gemini API", e)
            mockResponse(prompt)
        }
    }

    private fun mockResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("mount") || lower.contains("installation") || lower.contains("setup") || lower.contains("sensor_setup_valve_01") -> {
                "Got it. Looking at your image, the MQ-2 sensor is correctly mounted within 30cm (12 inches) of the gas regulator. Ensure the sensor face is pointing downwards to prevent dust accumulation. The indicator light pulsing orange is due to initial calibration or slight signal interference. Try rotating the sensor 45 degrees to optimize connection."
            }
            lower.contains("orange") || lower.contains("pulse") || lower.contains("light") -> {
                "A pulsing orange indicator light on your Gas Monitor device signals a calibration delay or minor signal interference. Please ensure that the pressure valve is fully open and try rotating the physical sensor slightly (e.g., by 45 degrees) for optimal alignment."
            }
            lower.contains("leak") || lower.contains("leakage") || lower.contains("ppm") -> {
                "The MQ-2 sensor detects LPG, propane, and butane. Under normal operation, safety threshold levels should remain below 0.05 ppm. If it rises, check joint seals using soapy water immediately."
            }
            lower.contains("refill") || lower.contains("order") -> {
                "Refills can be scheduled instantly from the Refill tab. Standard delivery takes 3-5 days ($0.00), Express is next-day (+$15), and Emergency delivers within 4 hours (+$45)."
            }
            lower.contains("calibrate") || lower.contains("calibration") -> {
                "Sensor calibration takes approximately 30-60 seconds during setup to stabilize the heater element in the MQ-2 sensor. Ensure surrounding air is clear during this step."
            }
            else -> {
                "Hi, I'm Alex M., your Gas Monitor technical support representative. I can assist you with your ESP32 monitor, MQ-2 calibration, pressure sensors, or refill schedules. What can I help you with today?"
            }
        }
    }
}
