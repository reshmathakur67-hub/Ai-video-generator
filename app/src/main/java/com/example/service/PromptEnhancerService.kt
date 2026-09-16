package com.example.service

import com.example.BuildConfig
import com.example.model.CameraMotionOption
import com.example.model.VideoStyleOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class PromptEnhancerService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun enhancePrompt(
        rawPrompt: String,
        style: VideoStyleOption,
        cameraMotion: CameraMotionOption
    ): String = withContext(Dispatchers.IO) {
        val trimmed = rawPrompt.trim()
        if (trimmed.isEmpty()) return@withContext ""

        val apiKey = try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotEmpty()) {
            try {
                val enhanced = callGeminiFlashEnhancer(trimmed, style.title, cameraMotion.label, apiKey)
                if (enhanced.isNotBlank()) return@withContext enhanced
            } catch (_: Exception) {
                // Fallback to local deterministic enhancer
            }
        }

        // High quality local cinematic prompt synthesizer
        return@withContext synthesizeLocalCinematicPrompt(trimmed, style, cameraMotion)
    }

    private fun callGeminiFlashEnhancer(
        userPrompt: String,
        styleName: String,
        camera: String,
        apiKey: String
    ): String {
        val systemPrompt = "You are an elite Hollywood cinematographer and AI video prompt engineer. " +
                "Transform the user's short prompt into an ultra-detailed, vivid, cinematic video generation prompt. " +
                "Include: [Subject], [Environment & atmosphere], [Action & micro-movement], [Camera motion: $camera], " +
                "[Lighting: volumetric, rim lights, shadows], [Visual Style: $styleName], [Mood & emotional tone], " +
                "[Composition & lens framing], and [Temporal motion physics]. " +
                "Keep the final output concise (2 to 4 sentences), highly vivid, without markdown formatting or bullet points."

        val jsonBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "$systemPrompt\n\nOriginal prompt: \"$userPrompt\"")
                        })
                    })
                })
            })
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            val body = response.body?.string() ?: return ""
            val json = JSONObject(body)
            val candidates = json.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text", "") ?: ""
            return text.trim().removePrefix("\"").removeSuffix("\"")
        }
    }

    private fun synthesizeLocalCinematicPrompt(
        rawPrompt: String,
        style: VideoStyleOption,
        camera: CameraMotionOption
    ): String {
        val cameraDesc = when (camera) {
            CameraMotionOption.AUTO -> "dynamic floating camera with subtle parallax"
            CameraMotionOption.STATIC -> "locked-off tripod shot with crisp focal stability"
            CameraMotionOption.ZOOM_IN -> "slow cinematic push-in zooming onto the focal subject"
            CameraMotionOption.ZOOM_OUT -> "gradual wide zoom-out revealing the grand surroundings"
            CameraMotionOption.PAN_LEFT -> "smooth horizontal pan to the left tracking environmental depth"
            CameraMotionOption.PAN_RIGHT -> "sweeping horizontal pan to the right with fluid motion blur"
            CameraMotionOption.DOLLY_IN -> "smooth dolly forward through the atmosphere"
            CameraMotionOption.DOLLY_OUT -> "dramatic dolly pull-back revealing scale"
            CameraMotionOption.ORBIT -> "360-degree sweeping orbit camera wrapping around the subject"
            CameraMotionOption.TRACKING_SHOT -> "low-angle steadycam tracking shot moving in sync with action"
            CameraMotionOption.CRANE_SHOT -> "majestic high-altitude crane jib descending smoothly"
            CameraMotionOption.HANDHELD -> "tactile documentary handheld motion with natural organic drift"
        }

        val lightingDesc = "illuminated by dramatic golden-hour volumetric light beams, soft ambient fill, and specular highlights"
        val compositionDesc = "shot on 35mm anamorphic lens, centered golden ratio framing with shallow depth of field"
        val motionDesc = "photorealistic micro-movements, wind rustling textures, and hyper-coherent fluid physics"
        val styleDesc = if (style.promptAddon.isNotBlank()) style.promptAddon else "cinematic masterpiece rendering"

        return "$rawPrompt. $cameraDesc, $lightingDesc, $compositionDesc. $motionDesc. Stylized in $styleDesc, evocative emotional mood."
    }
}
