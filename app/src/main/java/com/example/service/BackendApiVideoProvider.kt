package com.example.service

import com.example.model.GenerationJob
import com.example.model.GenerationSettings
import com.example.model.JobStatus
import com.example.model.SceneItem
import com.example.model.VideoProject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class BackendApiVideoProvider(
    private var customBaseUrl: String = "https://ai-video-studio-api.example.com"
) : VideoGenerationProvider {

    override val providerName: String = "Custom Secure Backend"
    override val isSimulation: Boolean = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun updateBaseUrl(newUrl: String) {
        customBaseUrl = newUrl.trim().removeSuffix("/")
    }

    fun getBaseUrl(): String = customBaseUrl

    override suspend fun generateFromText(
        project: VideoProject,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("prompt", project.prompt)
                put("enhancedPrompt", project.enhancedPrompt ?: "")
                put("mode", project.mode.name)
                put("durationSeconds", project.durationSeconds)
                put("aspectRatio", project.aspectRatio)
                put("style", project.styleName)
                put("quality", project.quality)
                put("cameraMotion", project.cameraMotion)
                put("motionStrength", project.motionStrength)
                put("negativePrompt", project.negativePrompt)
                put("keepCharacterConsistent", project.keepCharacterConsistent)
                if (settings.voiceConfig.enabled) {
                    put("voice", JSONObject().apply {
                        put("language", settings.voiceConfig.language)
                        put("gender", settings.voiceConfig.voiceGender)
                        put("dialogue", settings.voiceConfig.dialogueScript)
                        put("speakerName", settings.voiceConfig.speakerName)
                        put("lipSync", settings.voiceConfig.enableLipSync)
                        put("backgroundMusic", settings.voiceConfig.backgroundMusic)
                        put("soundEffects", settings.voiceConfig.soundEffects)
                    })
                }
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$customBaseUrl/api/video/generate")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(
                        IOException("Backend responded with HTTP ${response.code}: $bodyStr")
                    )
                }

                val json = JSONObject(bodyStr)
                val jobId = json.optString("jobId", "job_${System.currentTimeMillis()}")
                val statusStr = json.optString("status", "QUEUED")
                val status = try { JobStatus.valueOf(statusStr) } catch (_: Exception) { JobStatus.QUEUED }
                val message = json.optString("message", "Job queued on secure backend")

                Result.success(
                    GenerationJob(
                        jobId = jobId,
                        projectId = project.id,
                        status = status,
                        progressPercentage = json.optInt("progress", 5),
                        statusMessage = message,
                        isMock = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Cannot reach secure backend ($customBaseUrl): ${e.localizedMessage}. " +
                    "Check server configuration in Settings or test in Development Mode."
                )
            )
        }
    }

    override suspend fun generateFromImage(
        project: VideoProject,
        imageUri: String,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("prompt", project.prompt)
                put("imageUri", imageUri)
                put("mode", "IMAGE_TO_VIDEO")
                put("durationSeconds", project.durationSeconds)
                put("aspectRatio", project.aspectRatio)
                put("style", project.styleName)
                put("cameraMotion", project.cameraMotion)
                put("motionStrength", project.motionStrength)
                put("negativePrompt", project.negativePrompt)
            }

            val request = Request.Builder()
                .url("$customBaseUrl/api/video/generate-from-image")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP ${response.code}: $body"))
                }
                val json = JSONObject(body)
                Result.success(
                    GenerationJob(
                        jobId = json.optString("jobId", "job_${System.currentTimeMillis()}"),
                        projectId = project.id,
                        status = JobStatus.QUEUED,
                        progressPercentage = 5,
                        statusMessage = json.optString("message", "Image-to-video queued on backend"),
                        isMock = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Image-to-video backend call failed: ${e.localizedMessage}"))
        }
    }

    override suspend fun generateFromScenes(
        project: VideoProject,
        scenes: List<SceneItem>,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        try {
            val jsonBody = JSONObject().apply {
                put("projectId", project.id)
                put("prompt", project.prompt)
                put("totalScenes", scenes.size)
                put("scenes", org.json.JSONArray().apply {
                    scenes.forEach { s ->
                        put(JSONObject().apply {
                            put("sceneNumber", s.sceneNumber)
                            put("duration", s.durationSeconds)
                            put("prompt", s.prompt)
                            put("character", s.characterName)
                            put("dialogue", s.dialogueText)
                            put("voice", s.voiceType)
                        })
                    }
                })
            }

            val request = Request.Builder()
                .url("$customBaseUrl/api/video/generate-story")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("HTTP ${response.code}: $body"))
                }
                val json = JSONObject(body)
                Result.success(
                    GenerationJob(
                        jobId = json.optString("jobId", "job_${System.currentTimeMillis()}"),
                        projectId = project.id,
                        status = JobStatus.QUEUED,
                        progressPercentage = 5,
                        statusMessage = "Multi-scene story rendering initiated",
                        isMock = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(Exception("Scene Builder backend error: ${e.localizedMessage}"))
        }
    }

    override suspend fun getJobStatus(jobId: String): Result<GenerationJob> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$customBaseUrl/api/video/status/$jobId")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Status check failed HTTP ${response.code}"))
                }
                val json = JSONObject(body)
                val statusStr = json.optString("status", "PROCESSING")
                val status = try { JobStatus.valueOf(statusStr) } catch (_: Exception) { JobStatus.PROCESSING }

                Result.success(
                    GenerationJob(
                        jobId = jobId,
                        projectId = json.optString("projectId", ""),
                        status = status,
                        progressPercentage = json.optInt("progress", 50),
                        statusMessage = json.optString("message", "Processing video on server..."),
                        videoUrl = json.optString("videoUrl", null),
                        thumbnailUrl = json.optString("thumbnailUrl", null),
                        errorMessage = json.optString("error", null),
                        isMock = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$customBaseUrl/api/video/cancel/$jobId")
                .post("{}".toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
