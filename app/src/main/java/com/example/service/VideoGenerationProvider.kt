package com.example.service

import com.example.model.GenerationJob
import com.example.model.GenerationSettings
import com.example.model.SceneItem
import com.example.model.VideoProject

interface VideoGenerationProvider {
    val providerName: String
    val isSimulation: Boolean

    suspend fun generateFromText(
        project: VideoProject,
        settings: GenerationSettings
    ): Result<GenerationJob>

    suspend fun generateFromImage(
        project: VideoProject,
        imageUri: String,
        settings: GenerationSettings
    ): Result<GenerationJob>

    suspend fun generateFromScenes(
        project: VideoProject,
        scenes: List<SceneItem>,
        settings: GenerationSettings
    ): Result<GenerationJob>

    suspend fun getJobStatus(jobId: String): Result<GenerationJob>

    suspend fun cancelJob(jobId: String): Result<Unit>
}
