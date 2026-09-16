package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.JobStatus
import com.example.model.VideoProject
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM video_projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<VideoProject>>

    @Query("SELECT * FROM video_projects WHERE id = :id LIMIT 1")
    suspend fun getProjectById(id: String): VideoProject?

    @Query("SELECT * FROM video_projects WHERE jobId = :jobId LIMIT 1")
    suspend fun getProjectByJobId(jobId: String): VideoProject?

    @Query("SELECT * FROM video_projects WHERE status IN ('PREPARING', 'QUEUED', 'GENERATING', 'PROCESSING', 'ADDING_AUDIO', 'FINALIZING') ORDER BY createdAt DESC")
    fun getActiveProjects(): Flow<List<VideoProject>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: VideoProject)

    @Update
    suspend fun updateProject(project: VideoProject)

    @Query("UPDATE video_projects SET status = :status, progressPercentage = :progress, statusMessage = :message, videoUrl = :videoUrl, errorMessage = :error WHERE jobId = :jobId")
    suspend fun updateJobStatus(
        jobId: String,
        status: JobStatus,
        progress: Int,
        message: String,
        videoUrl: String?,
        error: String?
    )

    @Query("DELETE FROM video_projects WHERE id = :id")
    suspend fun deleteProjectById(id: String)
}
