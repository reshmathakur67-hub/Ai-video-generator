package com.example.data

import androidx.room.TypeConverter
import com.example.model.GenerationMode
import com.example.model.JobStatus

class DatabaseConverters {
    @TypeConverter
    fun fromGenerationMode(mode: GenerationMode): String = mode.name

    @TypeConverter
    fun toGenerationMode(value: String): GenerationMode =
        try { GenerationMode.valueOf(value) } catch (_: Exception) { GenerationMode.TEXT_TO_VIDEO }

    @TypeConverter
    fun fromJobStatus(status: JobStatus): String = status.name

    @TypeConverter
    fun toJobStatus(value: String): JobStatus =
        try { JobStatus.valueOf(value) } catch (_: Exception) { JobStatus.QUEUED }
}
