package com.example.service

import com.example.model.GenerationJob
import com.example.model.GenerationSettings
import com.example.model.JobStatus
import com.example.model.SceneItem
import com.example.model.VideoProject
import kotlinx.coroutines.delay
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class DevelopmentMockVideoProvider : VideoGenerationProvider {
    override val providerName: String = "Development Mode (Simulation)"
    override val isSimulation: Boolean = true

    private val activeJobs = ConcurrentHashMap<String, MockJobState>()

    private data class MockJobState(
        val jobId: String,
        val projectId: String,
        val prompt: String,
        val startTimeMs: Long,
        var status: JobStatus = JobStatus.PREPARING,
        var progress: Int = 5,
        var message: String = "Initializing test generation..."
    )

    override suspend fun generateFromText(
        project: VideoProject,
        settings: GenerationSettings
    ): Result<GenerationJob> {
        return createMockJob(project.id, project.prompt)
    }

    override suspend fun generateFromImage(
        project: VideoProject,
        imageUri: String,
        settings: GenerationSettings
    ): Result<GenerationJob> {
        return createMockJob(project.id, "${project.prompt} (Image-to-Video)")
    }

    override suspend fun generateFromScenes(
        project: VideoProject,
        scenes: List<SceneItem>,
        settings: GenerationSettings
    ): Result<GenerationJob> {
        return createMockJob(project.id, "Multi-scene story (${scenes.size} scenes)")
    }

    private fun createMockJob(projectId: String, prompt: String): Result<GenerationJob> {
        val jobId = "mock_job_${UUID.randomUUID().toString().substring(0, 8)}"
        val state = MockJobState(
            jobId = jobId,
            projectId = projectId,
            prompt = prompt,
            startTimeMs = System.currentTimeMillis()
        )
        activeJobs[jobId] = state

        return Result.success(
            GenerationJob(
                jobId = jobId,
                projectId = projectId,
                status = JobStatus.PREPARING,
                progressPercentage = 5,
                statusMessage = "[Development Mode] Job queued in local mock runner",
                isMock = true
            )
        )
    }

    override suspend fun getJobStatus(jobId: String): Result<GenerationJob> {
        val state = activeJobs[jobId] ?: return Result.failure(Exception("Job $jobId not found in test runner"))
        val elapsedMs = System.currentTimeMillis() - state.startTimeMs

        val (newStatus, progress, msg, videoUrl) = when {
            elapsedMs < 3000 -> {
                Quad(JobStatus.QUEUED, 15, "[Development Mode] Dispatching prompt to simulation engine...", null)
            }
            elapsedMs < 7000 -> {
                Quad(JobStatus.GENERATING, 45, "[Development Mode] Synthesizing video frames (1080p)...", null)
            }
            elapsedMs < 11000 -> {
                Quad(JobStatus.PROCESSING, 70, "[Development Mode] Rendering motion and temporal coherence...", null)
            }
            elapsedMs < 14000 -> {
                Quad(JobStatus.ADDING_AUDIO, 88, "[Development Mode] Mixing ambient audio & dialogue...", null)
            }
            elapsedMs < 17000 -> {
                Quad(JobStatus.FINALIZING, 96, "[Development Mode] Encoding MP4 stream...", null)
            }
            else -> {
                // Reliable public H.264 MP4 stream for verification
                Quad(
                    JobStatus.COMPLETED,
                    100,
                    "[Development Mode] Video simulation complete",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                )
            }
        }

        state.status = newStatus
        state.progress = progress
        state.message = msg

        return Result.success(
            GenerationJob(
                jobId = jobId,
                projectId = state.projectId,
                status = newStatus,
                progressPercentage = progress,
                statusMessage = msg,
                videoUrl = videoUrl,
                thumbnailUrl = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600",
                isMock = true
            )
        )
    }

    override suspend fun cancelJob(jobId: String): Result<Unit> {
        activeJobs.remove(jobId)
        return Result.success(Unit)
    }

    private data class Quad(
        val status: JobStatus,
        val progress: Int,
        val message: String,
        val videoUrl: String?
    )
}
