package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.JobStatus
import com.example.model.VideoProject
import com.example.ui.components.JobStatusCard
import com.example.ui.components.VideoPlayerView
import com.example.ui.theme.DevModeOrange
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonRed
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StudioViewModel

@Composable
fun VideoResultScreen(
    project: VideoProject,
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isFullScreen by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        if (isFullScreen) {
            VideoPlayerView(
                videoUrl = project.videoUrl,
                isDevelopmentMode = project.isDevelopmentMode,
                aspectRatio = project.aspectRatio,
                isFullScreen = true,
                onFullScreenToggle = { isFullScreen = false },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.closePlayback() },
                            modifier = Modifier.testTag("close_result_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary
                            )
                        }

                        Text(
                            text = project.mode.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Check out this AI Video generated with AI Video Studio: \"${project.prompt}\" \n${project.videoUrl ?: ""}"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Video Prompt"))
                            },
                            modifier = Modifier.testTag("share_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = NeonCyan
                            )
                        }
                    }
                }

                // Video Player or Active Generation Card
                item {
                    if (project.status == JobStatus.COMPLETED && !project.videoUrl.isNullOrBlank()) {
                        VideoPlayerView(
                            videoUrl = project.videoUrl,
                            isDevelopmentMode = project.isDevelopmentMode,
                            aspectRatio = project.aspectRatio,
                            isFullScreen = false,
                            onFullScreenToggle = { isFullScreen = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        JobStatusCard(
                            status = project.status,
                            progressPercentage = project.progressPercentage,
                            statusMessage = project.statusMessage,
                            isDevelopmentMode = project.isDevelopmentMode,
                            jobId = project.jobId
                        )
                    }
                }

                // Title & Metadata Pills
                item {
                    Column {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetaPill("${project.durationSeconds}s", NeonViolet)
                            MetaPill(project.aspectRatio, NeonCyan)
                            MetaPill(project.styleName, NeonAmber)
                            MetaPill(project.quality, TextSecondary)
                            if (project.isDevelopmentMode) {
                                MetaPill("Dev Mode", DevModeOrange)
                            }
                        }
                    }
                }

                // Prompts Inspector Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = StudioCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Original Prompt",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = project.prompt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )

                            if (!project.enhancedPrompt.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Enhanced Cinematic Prompt",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonViolet
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = project.enhancedPrompt,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                // Technical Parameters Card
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = StudioCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Generation Parameters",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            ParamRow("Camera Motion", project.cameraMotion)
                            ParamRow("Motion Strength", project.motionStrength)
                            ParamRow("Character Consistency", if (project.keepCharacterConsistent) "Active" else "Standard")
                            if (!project.voiceLanguage.isNullOrBlank()) {
                                ParamRow("Voice Language", "${project.voiceLanguage} (${project.voiceGender ?: "Default"})")
                            }
                            if (project.enableLipSync) {
                                ParamRow("Lip Synchronization", "Enabled")
                            }
                            if (!project.negativePrompt.isNullOrBlank()) {
                                ParamRow("Negative Constraints", project.negativePrompt)
                            }
                        }
                    }
                }

                // Actions: Edit in Studio, Delete
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.duplicateProject(project)
                                viewModel.closePlayback()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("load_into_studio_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Load in Studio")
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.deleteProject(project.id)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).testTag("delete_project_button")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaPill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ParamRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
