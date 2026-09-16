package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GenerationMode
import com.example.ui.components.CameraMotionView
import com.example.ui.components.FormatControlsView
import com.example.ui.components.PromptBoxView
import com.example.ui.components.ReferenceImagesView
import com.example.ui.components.SceneBuilderView
import com.example.ui.components.StyleSelectorView
import com.example.ui.components.VoiceAudioView
import com.example.ui.theme.DevModeOrange
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioDarkBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.StudioViewModel

@Composable
fun CreateScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val currentMode by viewModel.currentCreateMode.collectAsState()
    val promptText by viewModel.promptText.collectAsState()
    val enhancedPrompt by viewModel.enhancedPrompt.collectAsState()
    val isEnhancing by viewModel.isEnhancingPrompt.collectAsState()
    val selectedImageUri by viewModel.selectedImageUri.collectAsState()
    val characterReferenceUri by viewModel.characterReferenceUri.collectAsState()
    val startingFrameUri by viewModel.startingFrameUri.collectAsState()
    val endingFrameUri by viewModel.endingFrameUri.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val scenes by viewModel.scenes.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    var showNegativePrompt by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? -> viewModel.setSelectedImageUri(uri?.toString()) }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(StudioDarkBg)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header & Mode Switcher
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Creation Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )

                        // Mode indicator
                        Text(
                            text = when (currentMode) {
                                GenerationMode.TEXT_TO_VIDEO -> "Text-to-Video"
                                GenerationMode.IMAGE_TO_VIDEO -> "Image-to-Video"
                                GenerationMode.SCENE_BUILDER -> "Scene Builder"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Mode Tabs
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = StudioCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            ModeTabItem(
                                title = "Text to Video",
                                isSelected = currentMode == GenerationMode.TEXT_TO_VIDEO,
                                onClick = { viewModel.setCreateMode(GenerationMode.TEXT_TO_VIDEO) },
                                modifier = Modifier.weight(1f)
                            )

                            ModeTabItem(
                                title = "Image to Video",
                                isSelected = currentMode == GenerationMode.IMAGE_TO_VIDEO,
                                onClick = { viewModel.setCreateMode(GenerationMode.IMAGE_TO_VIDEO) },
                                modifier = Modifier.weight(1f)
                            )

                            ModeTabItem(
                                title = "Scene Builder",
                                isSelected = currentMode == GenerationMode.SCENE_BUILDER,
                                onClick = { viewModel.setCreateMode(GenerationMode.SCENE_BUILDER) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Image-to-Video Reference Image Card (when in Image to Video mode)
            if (currentMode == GenerationMode.IMAGE_TO_VIDEO) {
                item {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = StudioCardBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Source Reference Image",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(StudioCardElevated)
                                    .border(
                                        1.dp,
                                        if (selectedImageUri != null) NeonCyan else StudioCardBorder,
                                        RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedImageUri != null) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Source Image",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = { viewModel.setSelectedImageUri(null) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AddPhotoAlternate,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Tap to upload starting image",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "PNG, JPG, WebP supported",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Scene Builder Timeline (when in Scene Builder mode)
            if (currentMode == GenerationMode.SCENE_BUILDER) {
                item {
                    SceneBuilderView(
                        scenes = scenes,
                        onAddScene = { viewModel.addScene() },
                        onUpdateScene = { idx, item -> viewModel.updateScene(idx, item) },
                        onRemoveScene = { idx -> viewModel.removeScene(idx) }
                    )
                }
            } else {
                // Prompt Box (Text-to-Video & Image-to-Video)
                item {
                    PromptBoxView(
                        promptText = promptText,
                        onPromptChange = { viewModel.setPromptText(it) },
                        enhancedPrompt = enhancedPrompt,
                        onEnhancedPromptChange = { viewModel.setEnhancedPrompt(it) },
                        isEnhancing = isEnhancing,
                        onEnhanceClick = { viewModel.enhancePrompt() },
                        onApplyEnhancedClick = { viewModel.applyEnhancedPromptToMain() }
                    )
                }
            }

            // Format Controls (Aspect Ratio, Duration, Quality)
            item {
                FormatControlsView(
                    duration = settings.duration,
                    onDurationChange = { viewModel.updateDuration(it) },
                    aspectRatio = settings.aspectRatio,
                    onAspectRatioChange = { viewModel.updateAspectRatio(it) },
                    quality = settings.quality,
                    onQualityChange = { viewModel.updateQuality(it) }
                )
            }

            // Visual Style Selector
            item {
                StyleSelectorView(
                    selectedStyle = settings.style,
                    onStyleSelected = { viewModel.updateStyle(it) }
                )
            }

            // Camera Motion Controls
            item {
                CameraMotionView(
                    cameraMotion = settings.cameraMotion,
                    onCameraMotionChange = { viewModel.updateCameraMotion(it) },
                    motionStrength = settings.motionStrength,
                    onMotionStrengthChange = { viewModel.updateMotionStrength(it) }
                )
            }

            // Character Consistency & Keyframe References
            item {
                ReferenceImagesView(
                    characterReferenceUri = characterReferenceUri,
                    onCharacterReferenceChange = { viewModel.setCharacterReferenceUri(it) },
                    keepConsistent = settings.keepCharacterConsistent,
                    onKeepConsistentChange = { viewModel.setKeepCharacterConsistent(it) },
                    startingFrameUri = startingFrameUri,
                    onStartingFrameChange = { viewModel.setStartingFrameUri(it) },
                    endingFrameUri = endingFrameUri,
                    onEndingFrameChange = { viewModel.setEndingFrameUri(it) }
                )
            }

            // Voice, Dialogue, Audio & Lip Sync
            item {
                VoiceAudioView(
                    voiceConfig = settings.voiceConfig,
                    onVoiceConfigChange = { viewModel.updateVoiceConfig(it) }
                )
            }

            // Negative Prompt Section (Expandable)
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = StudioCardBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showNegativePrompt = !showNegativePrompt },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Negative Prompt (Optional)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = if (showNegativePrompt) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }

                        AnimatedVisibility(visible = showNegativePrompt) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                OutlinedTextField(
                                    value = settings.negativePrompt,
                                    onValueChange = { viewModel.updateNegativePrompt(it) },
                                    placeholder = {
                                        Text("Elements to avoid: blurry, distorted faces, low frame rate, watermark...", fontSize = 12.sp)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(75.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = NeonViolet,
                                        unfocusedBorderColor = StudioCardBorder,
                                        focusedContainerColor = StudioCardElevated,
                                        unfocusedContainerColor = StudioCardElevated
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Sticky Bottom Generation Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = StudioCardElevated.copy(alpha = 0.96f),
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${settings.duration.label} • ${settings.aspectRatio.ratioString}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = settings.providerType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (settings.providerType.displayName.contains("Dev")) DevModeOrange else TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Button(
                    onClick = { viewModel.generateVideo() },
                    enabled = !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonViolet,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("generate_video_button")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Dispatching...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Video", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModeTabItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) NeonViolet else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp
        )
    }
}
