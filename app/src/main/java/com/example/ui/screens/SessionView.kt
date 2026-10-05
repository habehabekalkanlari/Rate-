package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Clip
import com.example.model.ClipState
import com.example.model.DawProject
import com.example.model.Track
import com.example.viewmodel.DawTab
import com.example.viewmodel.DawViewModel

@Composable
fun SessionView(
    viewModel: DawViewModel,
    project: DawProject,
    currentStep: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val hScrollState = rememberScrollState()
    val vScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
    ) {
        // Session Matrix Container
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(hScrollState)
        ) {
            // Track Columns
            project.tracks.forEach { track ->
                TrackColumn(
                    track = track,
                    scenesCount = project.scenes.size,
                    currentStep = currentStep,
                    isPlaying = isPlaying,
                    onLaunchClip = { sceneIdx ->
                        viewModel.launchClip(track.id, sceneIdx)
                    },
                    onStopTrack = {
                        viewModel.stopTrackClip(track.id)
                    },
                    onToggleMute = {
                        viewModel.toggleTrackMute(track.id)
                    },
                    onToggleSolo = {
                        viewModel.toggleTrackSolo(track.id)
                    },
                    onEditClip = { clipIdx ->
                        viewModel.selectTrack(track.id)
                        viewModel.selectClip(clipIdx)
                        viewModel.selectTab(DawTab.SEQUENCER)
                    }
                )
            }

            // Master Column (Scene Launch buttons - Signature Ableton Live)
            MasterSceneColumn(
                project = project,
                onLaunchScene = { sceneIdx ->
                    viewModel.launchScene(sceneIdx)
                },
                onStopAll = {
                    viewModel.stopAllClips()
                }
            )
        }
    }
}

@Composable
fun TrackColumn(
    track: Track,
    scenesCount: Int,
    currentStep: Int,
    isPlaying: Boolean,
    onLaunchClip: (sceneIndex: Int) -> Unit,
    onStopTrack: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    onEditClip: (clipIndex: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = Color(track.colorHex)

    Column(
        modifier = modifier
            .width(135.dp)
            .fillMaxHeight()
            .background(Color(0xFF1A1B20))
            .border(width = 0.5.dp, color = Color(0xFF26282E))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Track Header (Ableton Strip)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF22242B))
                .border(1.dp, trackColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(trackColor)
                )
                Text(
                    text = track.type.displayName,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = trackColor
                )
            }

            Text(
                text = track.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp)
            )

            // Mute / Solo mini buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Mute
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (track.isMuted) Color(0xFFFF5252) else Color(0xFF2C2F38))
                        .clickable { onToggleMute() }
                        .testTag("mute_${track.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "M",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.isMuted) Color.White else Color(0xFF9EA3B0)
                    )
                }

                // Solo
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(22.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (track.isSolo) Color(0xFF40C057) else Color(0xFF2C2F38))
                        .clickable { onToggleSolo() }
                        .testTag("solo_${track.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "S",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (track.isSolo) Color(0xFF141518) else Color(0xFF9EA3B0)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Clip Slots (1 for each scene)
        for (sceneIdx in 0 until scenesCount) {
            val clip = track.clips.find { it.sceneIndex == sceneIdx }
            val clipIdx = if (clip != null) track.clips.indexOf(clip) else -1
            val isClipPlaying = track.activeClipIndex == clipIdx && clipIdx != -1 && isPlaying

            ClipSlot(
                clip = clip,
                isPlaying = isClipPlaying,
                currentStep = currentStep,
                trackColor = trackColor,
                onLaunch = { onLaunchClip(sceneIdx) },
                onEdit = {
                    if (clipIdx >= 0) onEditClip(clipIdx)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 3.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Track Stop Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF262831))
                .clickable { onStopTrack() }
                .testTag("stop_track_${track.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Stop,
                contentDescription = "Stop Track",
                tint = Color(0xFF868E96),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ClipSlot(
    clip: Clip?,
    isPlaying: Boolean,
    currentStep: Int,
    trackColor: Color,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (clip == null) {
        // Empty clip slot
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF1B1C22))
                .border(0.5.dp, Color(0xFF25272F), RoundedCornerShape(6.dp))
                .clickable { onLaunch() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Empty Slot",
                tint = Color(0xFF383B44),
                modifier = Modifier.size(14.dp)
            )
        }
        return
    }

    // Active or Stopped Clip
    val infiniteTransition = rememberInfiniteTransition(label = "clip_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val borderMod = if (isPlaying) {
        Modifier.border(2.dp, Color(0xFF51CF66), RoundedCornerShape(6.dp))
    } else {
        Modifier.border(0.5.dp, Color(0xFF383B44), RoundedCornerShape(6.dp))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isPlaying) Color(0xFF243029) else Color(0xFF202228))
            .then(borderMod)
            .clickable { onLaunch() }
            .testTag("clip_slot_${clip.name.lowercase().replace(" ", "_")}")
    ) {
        // Progress bar for current step in playing clip
        if (isPlaying) {
            val progressFraction = (currentStep + 1) / 16f
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .background(trackColor.copy(alpha = 0.25f))
            )
        }

        // Clip Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Indicator Icon
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) Color(0xFF51CF66) else trackColor.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "Play",
                        tint = if (isPlaying) Color(0xFF141518) else Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Edit pencil button
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .clickable { onEdit() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit Clip",
                        tint = Color(0xFFA0A5B2),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Text(
                text = clip.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isPlaying) Color(0xFF51CF66) else Color(0xFFE2E4E9),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Step count badge
            Text(
                text = "16 steps",
                fontSize = 9.sp,
                color = Color(0xFF7A8090)
            )
        }
    }
}

@Composable
fun MasterSceneColumn(
    project: DawProject,
    onLaunchScene: (sceneIndex: Int) -> Unit,
    onStopAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(130.dp)
            .fillMaxHeight()
            .background(Color(0xFF16171B))
            .border(width = 0.5.dp, color = Color(0xFF26282E))
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Master Column Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF23252C))
                .border(1.dp, Color(0xFFFF922B).copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "MASTER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFF922B)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Scene Launch Rows
        project.scenes.forEach { scene ->
            val sceneColor = Color(scene.colorHex)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF202229))
                    .border(1.dp, sceneColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .clickable { onLaunchScene(scene.index) }
                    .padding(6.dp)
                    .testTag("launch_scene_${scene.index}")
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = scene.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = sceneColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Launch Row",
                            fontSize = 8.sp,
                            color = Color(0xFF868E96)
                        )
                    }

                    // Green Launch Arrow
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF20C997)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Launch Scene",
                            tint = Color(0xFF141518),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Global Stop All Clips Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFFFF5252))
                .clickable { onStopAll() }
                .testTag("stop_all_clips"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "STOP ALL",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
