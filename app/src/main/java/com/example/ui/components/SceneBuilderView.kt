package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SceneItem
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardBorder
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SceneBuilderView(
    scenes: List<SceneItem>,
    onAddScene: () -> Unit,
    onUpdateScene: (Int, SceneItem) -> Unit,
    onRemoveScene: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = scenes.sumOf { it.durationSeconds }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = StudioCardBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = modifier
            .fillMaxWidth()
            .testTag("scene_builder_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesomeMotion,
                        contentDescription = null,
                        tint = NeonPink,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Scene Story Timeline",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${scenes.size} Scenes • Total Duration: ${totalSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPink,
                            fontSize = 11.sp
                        )
                    }
                }

                ElevatedButton(
                    onClick = onAddScene,
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = NeonPink.copy(alpha = 0.2f),
                        contentColor = NeonPink
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("add_scene_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Scene", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Scene Items List
            scenes.forEachIndexed { index, scene ->
                SceneItemCard(
                    scene = scene,
                    onUpdate = { updated -> onUpdateScene(index, updated) },
                    onDelete = { onRemoveScene(index) },
                    isOnlyScene = scenes.size == 1
                )
                if (index < scenes.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun SceneItemCard(
    scene: SceneItem,
    onUpdate: (SceneItem) -> Unit,
    onDelete: () -> Unit,
    isOnlyScene: Boolean
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = StudioCardElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Scene Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(NeonPink.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${scene.sceneNumber}",
                            color = NeonPink,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scene ${scene.sceneNumber}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Duration chips
                    listOf(5, 10, 15).forEach { sec ->
                        val isSelected = scene.durationSeconds == sec
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) NeonViolet.copy(alpha = 0.3f) else StudioCardBg)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonViolet else StudioCardBorder,
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { onUpdate(scene.copy(durationSeconds = sec)) }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${sec}s",
                                fontSize = 10.sp,
                                color = if (isSelected) NeonViolet else TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (!isOnlyScene) {
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Scene",
                                tint = Color.Red.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Scene prompt
            OutlinedTextField(
                value = scene.prompt,
                onValueChange = { onUpdate(scene.copy(prompt = it)) },
                placeholder = { Text("Scene visuals & action...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonPink,
                    unfocusedBorderColor = StudioCardBorder,
                    focusedContainerColor = StudioCardBg,
                    unfocusedContainerColor = StudioCardBg
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Character & Dialogue Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = scene.characterName,
                    onValueChange = { onUpdate(scene.copy(characterName = it)) },
                    placeholder = { Text("Character name", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedContainerColor = StudioCardBg,
                        unfocusedContainerColor = StudioCardBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                )

                OutlinedTextField(
                    value = scene.dialogueText,
                    onValueChange = { onUpdate(scene.copy(dialogueText = it)) },
                    placeholder = { Text("Spoken dialogue script", fontSize = 11.sp) },
                    modifier = Modifier.weight(2f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = StudioCardBorder,
                        focusedContainerColor = StudioCardBg,
                        unfocusedContainerColor = StudioCardBg
                    ),
                    shape = RoundedCornerShape(8.dp),
                    textStyle = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                )
            }
        }
    }
}
