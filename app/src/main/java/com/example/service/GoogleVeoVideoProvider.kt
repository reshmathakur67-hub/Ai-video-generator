package com.example.service

import com.example.BuildConfig
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

class GoogleVeoVideoProvider : VideoGenerationProvider {

    override val providerName: String = "Google Veo 3.1"
    override val isSimulation: Boolean = false

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun getApiKey(): String {
        return try {
            val key = BuildConfig.GEMINI_API_KEY
            if (key.isBlank() || key == "MY_GEMINI_API_KEY") "" else key
        } catch (_: Exception) {
            ""
        }
    }

    override suspend fun generateFromText(
        project: VideoProject,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException(
                    "Google Veo requires a valid GEMINI_API_KEY. Please configure your key in AI Studio Secrets panel, or switch to Development Mode in Settings."
                )
            )
        }

        try {
            val resolution = if (project.quality.contains("High", ignoreCase = true)) "1080p" else "720p"
            val aspectRatio = when {
                project.aspectRatio.contains("9:16") -> "9:16"
                project.aspectRatio.contains("1:1") -> "1:1"
                else -> "16:9"
            }

            // Build payload for Veo models
            val finalPrompt = buildString {
                append(project.prompt)
                if (project.styleName.isNotBlank() && !project.styleName.contains("Custom")) {
                    append(", ").append(project.styleName).append(" style")
                }
                if (project.cameraMotion.isNotBlank() && !project.cameraMotion.contains("Auto")) {
                    append(", ").append(project.cameraMotion).append(" camera movement")
                }
            }

            val requestJson = JSONObject().apply {
                put("prompt", finalPrompt)
                put("config", JSONObject().apply {
                    put("numberOfVideos", 1)
                    put("resolution", resolution)
                    put("aspectRatio", aspectRatio)
                    put("durationSeconds", project.durationSeconds)
                })
            }

            val url = "https://generativelanguage.googleapis.com/v1beta/models/veo-3.1-fast-generate-preview:generateVideos?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    val errorDetail = try {
                        JSONObject(bodyStr).getJSONObject("error").getString("message")
                    } catch (_: Exception) {
                        bodyStr
                    }
                    return@withContext Result.failure(
                        IOException("Veo API error (HTTP ${response.code}): $errorDetail")
                    )
                }

                val json = JSONObject(bodyStr)
                // Veo returns operation name, e.g., "operations/generate-videos-xyz"
                val operationName = json.optString("name", "op_${System.currentTimeMillis()}")

                Result.success(
                    GenerationJob(
                        jobId = operationName,
                        projectId = project.id,
                        status = JobStatus.QUEUED,
                        progressPercentage = 10,
                        statusMessage = "Veo video generation operation registered",
                        isMock = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateFromImage(
        project: VideoProject,
        imageUri: String,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(
                IllegalStateException("Veo API key missing. Configure GEMINI_API_KEY in Secrets or use Development Mode.")
            )
        }

        // Forward to text endpoint with reference image notes
        val updatedProject = project.copy(
            prompt = "Animate this reference scene smoothly: ${project.prompt}"
        )
        generateFromText(updatedProject, settings)
    }

    override suspend fun generateFromScenes(
        project: VideoProject,
        scenes: List<SceneItem>,
        settings: GenerationSettings
    ): Result<GenerationJob> = withContext(Dispatchers.IO) {
        val compositePrompt = scenes.joinToString(" -> ") { "Scene ${it.sceneNumber}: ${it.prompt}" }
        val updatedProject = project.copy(prompt = compositePrompt)
        generateFromText(updatedProject, settings)
    }

    override suspend fun getJobStatus(jobId: String): Result<GenerationJob> = withContext(Dispatchers.IO) {
        val apiKey = getApiKey()
        if (apiKey.isEmpty()) {
            return@withContext Result.failure(IllegalStateException("Veo API key missing"))
        }

        try {
            val opPath = if (jobId.startsWith("operations/")) jobId else "operations/$jobId"
            val url = "https://generativelanguage.googleapis.com/v1beta/$opPath?key=$apiKey"

            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (!response.isSuccessful) {
                    return@withContext Result.failure(IOException("Operation query HTTP ${response.code}: $bodyStr"))
                }

                val json = JSONObject(bodyStr)
                val isDone = json.optBoolean("done", false)

                if (!isDone) {
                    Result.success(
                        GenerationJob(
                            jobId = jobId,
                            projectId = "",
                            status = JobStatus.GENERATING,
                            progressPercentage = 50,
                            statusMessage = "Google Veo 3.1 is synthesizing neural frames...",
                            isMock = false
                        )
                    )
                } else {
                    // Check if error
                    if (json.has("error")) {
                        val err = json.getJSONObject("error").optString("message", "Veo generation failed")
                        return@withContext Result.success(
                            GenerationJob(
                                jobId = jobId,
                                projectId = "",
                                status = JobStatus.FAILED,
                                progressPercentage = 0,
                                statusMessage = "Generation failed: $err",
                                errorMessage = err,
                                isMock = false
                            )
                        )
                    }

                    // Extract generated video URI
                    val respObj = json.optJSONObject("response")
                    val videos = respObj?.optJSONArray("generatedVideos")
                    val firstVideo = videos?.optJSONObject(0)?.optJSONObject("video")
                    val uri = firstVideo?.optString("uri")

                    Result.success(
                        GenerationJob(
                            jobId = jobId,
                            projectId = "",
                            status = JobStatus.COMPLETED,
                            progressPercentage = 100,
                            statusMessage = "Veo generation complete",
                            videoUrl = uri,
                            isMock = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelJob(jobId: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.success(Unit)
    }
}
