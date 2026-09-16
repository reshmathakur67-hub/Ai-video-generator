package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MovieCreation
import androidx.compose.material.icons.filled.Queue
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobStatus
import com.example.ui.theme.DevModeOrange
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun JobStatusCard(
    status: JobStatus,
    progressPercentage: Int,
    statusMessage: String,
    isDevelopmentMode: Boolean,
    modifier: Modifier = Modifier,
    jobId: String? = null
) {
    val (statusColor, statusIcon) = when (status) {
        JobStatus.PREPARING -> Pair(NeonViolet, Icons.Default.Tune)
        JobStatus.QUEUED -> Pair(NeonCyan, Icons.Default.Queue)
        JobStatus.GENERATING -> Pair(NeonPink, Icons.Default.MovieCreation)
        JobStatus.PROCESSING -> Pair(NeonAmber, Icons.Default.Memory)
        JobStatus.ADDING_AUDIO -> Pair(NeonViolet, Icons.Default.Audiotrack)
        JobStatus.FINALIZING -> Pair(NeonCyan, Icons.Default.HourglassTop)
        JobStatus.COMPLETED -> Pair(NeonGreen, Icons.Default.CheckCircle)
        JobStatus.FAILED -> Pair(NeonRed, Icons.Default.Error)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = (progressPercentage.toFloat() / 100f).coerceIn(0.05f, 1f),
        label = "job_progress"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = StudioCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("job_status_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(statusColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (status != JobStatus.COMPLETED && status != JobStatus.FAILED) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = statusColor,
                                strokeWidth = 2.dp
                            )
                        }
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = status.displayName,
                            tint = statusColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = status.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (jobId != null) {
                            Text(
                                text = "Job: ${jobId.take(16)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Status Badge
                Surface(
                    color = if (isDevelopmentMode) DevModeOrange.copy(alpha = 0.2f) else statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDevelopmentMode) DevModeOrange.copy(alpha = 0.6f) else statusColor.copy(alpha = 0.4f)
                    )
                ) {
                    Text(
                        text = if (isDevelopmentMode) "Dev Simulation" else "${progressPercentage}%",
                        color = if (isDevelopmentMode) DevModeOrange else statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress Bar
            if (status != JobStatus.COMPLETED && status != JobStatus.FAILED) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = statusColor,
                    trackColor = StudioCardElevated
                )
            } else if (status == JobStatus.COMPLETED) {
                LinearProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonGreen,
                    trackColor = StudioCardElevated
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = statusMessage.ifBlank { "Processing video rendering pipeline..." },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
