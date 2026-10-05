package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.core.content.FileProvider
import com.example.data.ProjectEntity
import com.example.model.DawProject
import com.example.viewmodel.DawViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
    viewModel: DawViewModel,
    project: DawProject,
    savedProjects: List<ProjectEntity>,
    isExporting: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf(project.name) }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Project", color = Color.White) },
            text = {
                Column {
                    Text("Enter project name:", fontSize = 12.sp, color = Color(0xFFA0A5B2))
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveNameInput,
                        onValueChange = { saveNameInput = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFFFF764D),
                            unfocusedBorderColor = Color(0xFF383B44)
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("save_project_name_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveProject(saveNameInput)
                        showSaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF764D)),
                    modifier = Modifier.testTag("confirm_save_button")
                ) {
                    Text("Save", color = Color(0xFF141518), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel", color = Color(0xFFA0A5B2))
                }
            },
            containerColor = Color(0xFF1F2128)
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Current Project Actions (Save & Export WAV)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1D1F26))
                    .border(1.dp, Color(0xFF2B2E38), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "CURRENT SESSION: ${project.name}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF922B)
                )
                Text(
                    text = "${project.bpm} BPM • ${project.tracks.size} Tracks • ${project.scenes.size} Scenes",
                    fontSize = 11.sp,
                    color = Color(0xFF9EA3B0),
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Save Project Button
                    Button(
                        onClick = {
                            saveNameInput = project.name
                            showSaveDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF764D)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).testTag("save_project_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Save",
                            tint = Color(0xFF141518),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Save Project", color = Color(0xFF141518), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Export WAV Button
                    Button(
                        onClick = {
                            viewModel.exportToWav(context) { wavFile ->
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    wavFile
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "audio/wav"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Audio WAV"))
                            }
                        },
                        enabled = !isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF20C997)),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).testTag("export_wav_button")
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                color = Color(0xFF141518),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = "Export WAV",
                                tint = Color(0xFF141518),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Export WAV", color = Color(0xFF141518), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Preset Templates
        item {
            Text(
                text = "GENRE PRESET TEMPLATES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF868E96)
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PresetCard(
                    title = "Berlin Techno",
                    bpm = 128,
                    color = Color(0xFFFF6B6B),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.loadPreset("techno") }
                )
                PresetCard(
                    title = "Synthwave 80s",
                    bpm = 115,
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.loadPreset("synthwave") }
                )
                PresetCard(
                    title = "Atlanta 808",
                    bpm = 140,
                    color = Color(0xFFBE4BDB),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.loadPreset("trap") }
                )
            }
        }

        // Saved Projects (Room Database)
        item {
            Text(
                text = "SAVED LOCAL PROJECTS (${savedProjects.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF868E96)
            )
        }

        if (savedProjects.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved projects yet. Tap \"Save Project\" above to store your live jam!",
                        fontSize = 11.sp,
                        color = Color(0xFF636875)
                    )
                }
            }
        } else {
            items(savedProjects, key = { it.id }) { entity ->
                SavedProjectCard(
                    entity = entity,
                    onLoad = { viewModel.loadProjectFromDb(entity) },
                    onDelete = { viewModel.deleteProject(entity.id) }
                )
            }
        }
    }
}

@Composable
fun PresetCard(
    title: String,
    bpm: Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2028))
            .border(1.dp, color.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("preset_${title.lowercase().replace(" ", "_")}")
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
            Text(
                text = "$bpm BPM",
                fontSize = 10.sp,
                color = Color(0xFF9EA3B0),
                modifier = Modifier.padding(top = 2.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = "Load",
                    tint = color,
                    modifier = Modifier.size(12.dp)
                )
                Text("Load", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
    }
}

@Composable
fun SavedProjectCard(
    entity: ProjectEntity,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateStr = remember(entity.updatedAt) {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(entity.updatedAt))
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1B1C22))
            .border(1.dp, Color(0xFF272933), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entity.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${entity.bpm} BPM • $dateStr",
                fontSize = 10.sp,
                color = Color(0xFF868E96)
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            // Load button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF20C997))
                    .clickable { onLoad() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("load_project_${entity.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text("LOAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF141518))
            }

            // Delete button
            IconButton(
                onClick = { onDelete() },
                modifier = Modifier.size(32.dp).testTag("delete_project_${entity.id}")
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF6B6B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
