package com.example.data

import com.example.model.GenerationJob
import com.example.model.GenerationMode
import com.example.model.GenerationSettings
import com.example.model.JobStatus
import com.example.model.ProviderType
import com.example.model.SceneItem
import com.example.model.VideoProject
import com.example.service.BackendApiVideoProvider
import com.example.service.DevelopmentMockVideoProvider
import com.example.service.GoogleVeoVideoProvider
import com.example.service.VideoGenerationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val coroutineScope: CoroutineScope
) {
    val allProjects: Flow<List<VideoProject>> = projectDao.getAllProjects()
    val activeProjects: Flow<List<VideoProject>> = projectDao.getActiveProjects()

    val mockProvider = DevelopmentMockVideoProvider()
    val veoProvider = GoogleVeoVideoProvider()
    val backendProvider = BackendApiVideoProvider()

    fun getProvider(type: ProviderType): VideoGenerationProvider {
        return when (type) {
            ProviderType.DEVELOPMENT_MOCK -> mockProvider
            ProviderType.GOOGLE_VEO -> veoProvider
            ProviderType.SECURE_BACKEND -> backendProvider
        }
    }

    suspend fun getProjectById(id: String): VideoProject? = projectDao.getProjectById(id)

    suspend fun deleteProject(id: String) = projectDao.deleteProjectById(id)

    suspend fun startTextToVideo(
        prompt: String,
        enhancedPrompt: String?,
        settings: GenerationSettings
    ): Result<VideoProject> {
        val projectId = "proj_${UUID.randomUUID().toString().substring(0, 8)}"
        val initialProject = VideoProject(
            id = projectId,
            title = prompt.take(36).ifBlank { "Untitled Creation" },
            mode = GenerationMode.TEXT_TO_VIDEO,
            prompt = prompt,
            enhancedPrompt = enhancedPrompt,
            durationSeconds = settings.duration.seconds,
            aspectRatio = settings.aspectRatio.ratioString,
            styleName = settings.style.title,
            quality = settings.quality.label,
            cameraMotion = settings.cameraMotion.label,
            motionStrength = settings.motionStrength.label,
            negativePrompt = settings.negativePrompt,
            keepCharacterConsistent = settings.keepCharacterConsistent,
            voiceLanguage = if (settings.voiceConfig.enabled) settings.voiceConfig.language else null,
            voiceGender = if (settings.voiceConfig.enabled) settings.voiceConfig.voiceGender else null,
            dialogue = if (settings.voiceConfig.enabled) settings.voiceConfig.dialogueScript else null,
            enableLipSync = settings.voiceConfig.enableLipSync,
            backgroundMusic = settings.voiceConfig.backgroundMusic,
            soundEffects = settings.voiceConfig.soundEffects,
            status = JobStatus.PREPARING,
            jobId = "pending_$projectId",
            progressPercentage = 5,
            statusMessage = "Validating generation request...",
            providerType = settings.providerType.name,
            isDevelopmentMode = settings.providerType == ProviderType.DEVELOPMENT_MOCK
        )

        projectDao.insertProject(initialProject)

        val provider = getProvider(settings.providerType)
        val result = provider.generateFromText(initialProject, settings)

        return result.fold(
            onSuccess = { job ->
                val activeProject = initialProject.copy(
                    jobId = job.jobId,
                    status = job.status,
                    progressPercentage = job.progressPercentage,
                    statusMessage = job.statusMessage,
                    videoUrl = job.videoUrl,
                    thumbnailUrl = job.thumbnailUrl
                )
                projectDao.updateProject(activeProject)
                startJobPolling(activeProject, provider)
                Result.success(activeProject)
            },
            onFailure = { error ->
                val failedProject = initialProject.copy(
                    status = JobStatus.FAILED,
                    statusMessage = error.localizedMessage ?: "Generation failed to dispatch",
                    errorMessage = error.localizedMessage
                )
                projectDao.updateProject(failedProject)
                Result.failure(error)
            }
        )
    }

    suspend fun startImageToVideo(
        prompt: String,
        imageUri: String,
        settings: GenerationSettings
    ): Result<VideoProject> {
        val projectId = "proj_${UUID.randomUUID().toString().substring(0, 8)}"
        val initialProject = VideoProject(
            id = projectId,
            title = prompt.take(36).ifBlank { "Image Animation" },
            mode = GenerationMode.IMAGE_TO_VIDEO,
            prompt = prompt,
            imageReferenceUri = imageUri,
            durationSeconds = settings.duration.seconds,
            aspectRatio = settings.aspectRatio.ratioString,
            styleName = settings.style.title,
            quality = settings.quality.label,
            cameraMotion = settings.cameraMotion.label,
            motionStrength = settings.motionStrength.label,
            negativePrompt = settings.negativePrompt,
            keepCharacterConsistent = settings.keepCharacterConsistent,
            voiceLanguage = if (settings.voiceConfig.enabled) settings.voiceConfig.language else null,
            voiceGender = if (settings.voiceConfig.enabled) settings.voiceConfig.voiceGender else null,
            dialogue = if (settings.voiceConfig.enabled) settings.voiceConfig.dialogueScript else null,
            enableLipSync = settings.voiceConfig.enableLipSync,
            backgroundMusic = settings.voiceConfig.backgroundMusic,
            soundEffects = settings.voiceConfig.soundEffects,
            status = JobStatus.PREPARING,
            jobId = "pending_$projectId",
            progressPercentage = 5,
            statusMessage = "Analyzing reference image...",
            providerType = settings.providerType.name,
            isDevelopmentMode = settings.providerType == ProviderType.DEVELOPMENT_MOCK
        )

        projectDao.insertProject(initialProject)

        val provider = getProvider(settings.providerType)
        val result = provider.generateFromImage(initialProject, imageUri, settings)

        return result.fold(
            onSuccess = { job ->
                val activeProject = initialProject.copy(
                    jobId = job.jobId,
                    status = job.status,
                    progressPercentage = job.progressPercentage,
                    statusMessage = job.statusMessage,
                    videoUrl = job.videoUrl,
                    thumbnailUrl = job.thumbnailUrl
                )
                projectDao.updateProject(activeProject)
                startJobPolling(activeProject, provider)
                Result.success(activeProject)
            },
            onFailure = { error ->
                val failedProject = initialProject.copy(
                    status = JobStatus.FAILED,
                    statusMessage = error.localizedMessage ?: "Image animation dispatch error",
                    errorMessage = error.localizedMessage
                )
                projectDao.updateProject(failedProject)
                Result.failure(error)
            }
        )
    }

    suspend fun startSceneStory(
        scenes: List<SceneItem>,
        settings: GenerationSettings
    ): Result<VideoProject> {
        val projectId = "proj_${UUID.randomUUID().toString().substring(0, 8)}"
        val totalSecs = scenes.sumOf { it.durationSeconds }
        val compositePrompt = scenes.joinToString(" | ") { "Scene ${it.sceneNumber}: ${it.prompt}" }

        val initialProject = VideoProject(
            id = projectId,
            title = "Story: ${scenes.firstOrNull()?.prompt?.take(24) ?: "30s Short"}",
            mode = GenerationMode.SCENE_BUILDER,
            prompt = compositePrompt,
            durationSeconds = totalSecs,
            aspectRatio = settings.aspectRatio.ratioString,
            styleName = settings.style.title,
            quality = settings.quality.label,
            cameraMotion = settings.cameraMotion.label,
            motionStrength = settings.motionStrength.label,
            negativePrompt = settings.negativePrompt,
            keepCharacterConsistent = settings.keepCharacterConsistent,
            voiceLanguage = if (settings.voiceConfig.enabled) settings.voiceConfig.language else null,
            voiceGender = if (settings.voiceConfig.enabled) settings.voiceConfig.voiceGender else null,
            dialogue = if (settings.voiceConfig.enabled) settings.voiceConfig.dialogueScript else null,
            enableLipSync = settings.voiceConfig.enableLipSync,
            backgroundMusic = settings.voiceConfig.backgroundMusic,
            soundEffects = settings.voiceConfig.soundEffects,
            status = JobStatus.PREPARING,
            jobId = "pending_$projectId",
            progressPercentage = 5,
            statusMessage = "Stitching scenes into story pipeline...",
            providerType = settings.providerType.name,
            isDevelopmentMode = settings.providerType == ProviderType.DEVELOPMENT_MOCK
        )

        projectDao.insertProject(initialProject)

        val provider = getProvider(settings.providerType)
        val result = provider.generateFromScenes(initialProject, scenes, settings)

        return result.fold(
            onSuccess = { job ->
                val activeProject = initialProject.copy(
                    jobId = job.jobId,
                    status = job.status,
                    progressPercentage = job.progressPercentage,
                    statusMessage = job.statusMessage,
                    videoUrl = job.videoUrl,
                    thumbnailUrl = job.thumbnailUrl
                )
                projectDao.updateProject(activeProject)
                startJobPolling(activeProject, provider)
                Result.success(activeProject)
            },
            onFailure = { error ->
                val failed = initialProject.copy(
                    status = JobStatus.FAILED,
                    statusMessage = error.localizedMessage ?: "Scene generation failed",
                    errorMessage = error.localizedMessage
                )
                projectDao.updateProject(failed)
                Result.failure(error)
            }
        )
    }

    private fun startJobPolling(project: VideoProject, provider: VideoGenerationProvider) {
        coroutineScope.launch(Dispatchers.IO) {
            var isFinished = false
            var attempts = 0
            val maxAttempts = 120 // ~4-5 minutes max

            while (!isFinished && attempts < maxAttempts) {
                delay(2500)
                attempts++

                val statusResult = provider.getJobStatus(project.jobId)
                statusResult.fold(
                    onSuccess = { updatedJob ->
                        projectDao.updateJobStatus(
                            jobId = project.jobId,
                            status = updatedJob.status,
                            progress = updatedJob.progressPercentage,
                            message = updatedJob.statusMessage,
                            videoUrl = updatedJob.videoUrl,
                            error = updatedJob.errorMessage
                        )

                        if (updatedJob.status == JobStatus.COMPLETED || updatedJob.status == JobStatus.FAILED) {
                            isFinished = true
                        }
                    },
                    onFailure = { err ->
                        // Don't fail immediately on temporary network jitter, increment attempts
                        if (attempts > 30) {
                            projectDao.updateJobStatus(
                                jobId = project.jobId,
                                status = JobStatus.FAILED,
                                progress = 0,
                                message = "Polling timed out or network lost",
                                videoUrl = null,
                                error = err.localizedMessage
                            )
                            isFinished = true
                        }
                    }
                )
            }
        }
    }
}
