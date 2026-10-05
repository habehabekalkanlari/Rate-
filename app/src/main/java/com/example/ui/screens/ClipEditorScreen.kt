package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DawProject
import com.example.model.DrumType
import com.example.model.TrackType
import com.example.viewmodel.DawViewModel

@Composable
fun ClipEditorScreen(
    viewModel: DawViewModel,
    project: DawProject,
    selectedTrackId: String?,
    selectedClipIndex: Int?,
    currentStep: Int,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val track = project.tracks.find { it.id == selectedTrackId } ?: project.tracks.firstOrNull()
    val clipIdx = selectedClipIndex ?: 0
    val clip = track?.clips?.getOrNull(clipIdx)

    var currentOctave by remember { mutableIntStateOf(3) } // 2, 3, 4, 5

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
    ) {
        // Track & Clip Selection Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1D1F24))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Track selector chips
            val trackScroll = rememberScrollState()
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(trackScroll),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                project.tracks.forEach { tr ->
                    val isSelected = tr.id == track?.id
                    val color = Color(tr.colorHex)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) color.copy(alpha = 0.25f) else Color(0xFF282A31))
                            .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(6.dp))
                            .clickable { viewModel.selectTrack(tr.id) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("select_track_${tr.name.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = tr.name,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) color else Color(0xFFC0C4D0)
                        )
                    }
                }
            }

            // Clear clip button
            if (track != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2C2E36))
                        .clickable { viewModel.clearClip(track.id, clipIdx) }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("clear_clip_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteSweep,
                            contentDescription = "Clear",
                            tint = Color(0xFFFF6B6B),
                            modifier = Modifier.size(14.dp)
                        )
                        Text("Clear", fontSize = 10.sp, color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Clip Scene Selector (Which clip slot of this track is being edited)
        if (track != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF181A1F))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Clip Slot:",
                    fontSize = 10.sp,
                    color = Color(0xFF868E96),
                    fontWeight = FontWeight.SemiBold
                )
                track.clips.forEachIndexed { idx, cl ->
                    val isSelected = idx == clipIdx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFFFF764D) else Color(0xFF25272F))
                            .clickable { viewModel.selectClip(idx) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("clip_slot_tab_$idx")
                    ) {
                        Text(
                            text = "${idx + 1}: ${cl.name}",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF141518) else Color(0xFFD2D6E0)
                        )
                    }
                }
            }
        }

        // Sequencer Grid View
        if (track == null || clip == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a track to edit patterns", color = Color.Gray)
            }
        } else if (track.type == TrackType.DRUMS) {
            // Drum Step Sequencer Grid (8 Instruments x 16 Steps)
            DrumStepSequencer(
                clip = clip,
                currentStep = currentStep,
                isPlaying = isPlaying,
                onToggleStep = { drumType, step ->
                    viewModel.toggleDrumStep(drumType, step)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Melodic Piano Roll Sequencer
            MelodicPianoRoll(
                clip = clip,
                currentOctave = currentOctave,
                currentStep = currentStep,
                isPlaying = isPlaying,
                onOctaveChange = { currentOctave = it.coerceIn(1, 5) },
                onToggleNote = { step, midiNote ->
                    viewModel.toggleMelodicNote(step, midiNote)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun DrumStepSequencer(
    clip: com.example.model.Clip,
    currentStep: Int,
    isPlaying: Boolean,
    onToggleStep: (DrumType, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        // Instrument Names Column
        Column(
            modifier = Modifier
                .width(76.dp)
                .verticalScroll(vScroll)
        ) {
            // Step Header Spacer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(Color(0xFF1B1C22)),
                contentAlignment = Alignment.Center
            ) {
                Text("INST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF868E96))
            }

            DrumType.values().forEach { drumType ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .padding(vertical = 2.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF22242B))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = drumType.shortName,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF922B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 16-Step Grid
        Column(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(hScroll)
                .verticalScroll(vScroll)
        ) {
            // Step Numbers Header (1 .. 16)
            Row(
                modifier = Modifier
                    .height(26.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                for (s in 0 until 16) {
                    val isCursor = isPlaying && s == currentStep
                    val isBeatDownbeat = s % 4 == 0

                    Box(
                        modifier = Modifier
                            .width(36.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isCursor -> Color(0xFF20C997)
                                    isBeatDownbeat -> Color(0xFF2D303A)
                                    else -> Color(0xFF1E2026)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${s + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCursor) Color(0xFF141518) else Color(0xFF9EA3B0),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Step Buttons for each instrument
            DrumType.values().forEach { drumType ->
                val steps = clip.drumSteps[drumType] ?: List(16) { false }

                Row(
                    modifier = Modifier
                        .height(38.dp)
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (step in 0 until 16) {
                        val isActive = steps.getOrNull(step) == true
                        val isCursor = isPlaying && step == currentStep
                        val isBeatGroup = (step / 4) % 2 == 0

                        val cellBg = when {
                            isActive -> Color(0xFFFF6B6B)
                            isBeatGroup -> Color(0xFF24262E)
                            else -> Color(0xFF1C1E24)
                        }

                        val borderMod = if (isCursor) {
                            Modifier.border(2.dp, Color(0xFF20C997), RoundedCornerShape(4.dp))
                        } else {
                            Modifier.border(0.5.dp, Color(0xFF333642), RoundedCornerShape(4.dp))
                        }

                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(cellBg)
                                .then(borderMod)
                                .clickable { onToggleStep(drumType, step) }
                                .testTag("step_${drumType.name.lowercase()}_$step"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isActive) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(Color.White)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MelodicPianoRoll(
    clip: com.example.model.Clip,
    currentOctave: Int,
    currentStep: Int,
    isPlaying: Boolean,
    onOctaveChange: (Int) -> Unit,
    onToggleNote: (step: Int, midiNote: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hScroll = rememberScrollState()
    val vScroll = rememberScrollState()

    // Notes in octave (from B down to C)
    val noteNames = listOf("B", "A#", "A", "G#", "G", "F#", "F", "E", "D#", "D", "C#", "C")
    val semitoneOffsets = listOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0)

    Column(modifier = modifier.fillMaxSize().padding(4.dp)) {
        // Octave Navigator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Octave:", fontSize = 11.sp, color = Color(0xFF9EA3B0), fontWeight = FontWeight.Bold)
            for (oct in 1..5) {
                Box(
                    modifier = Modifier
                        .size(width = 32.dp, height = 24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (oct == currentOctave) Color(0xFF339AF0) else Color(0xFF25272F))
                        .clickable { onOctaveChange(oct) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C$oct",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (oct == currentOctave) Color.White else Color(0xFFA0A5B2)
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize()) {
            // Note pitch labels column
            Column(
                modifier = Modifier
                    .width(46.dp)
                    .verticalScroll(vScroll)
            ) {
                // Header spacer
                Spacer(modifier = Modifier.height(26.dp))

                noteNames.forEachIndexed { idx, name ->
                    val isBlackKey = name.contains("#")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                            .padding(vertical = 1.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isBlackKey) Color(0xFF1E2026) else Color(0xFF2E313C))
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = "$name$currentOctave",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlackKey) Color(0xFFFF922B) else Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // 16-Step Pitch Grid
            Column(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(hScroll)
                    .verticalScroll(vScroll)
            ) {
                // Step Numbers Header
                Row(
                    modifier = Modifier.height(26.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    for (s in 0 until 16) {
                        val isCursor = isPlaying && s == currentStep
                        val isBeatDownbeat = s % 4 == 0

                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isCursor -> Color(0xFF20C997)
                                        isBeatDownbeat -> Color(0xFF2D303A)
                                        else -> Color(0xFF1E2026)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${s + 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCursor) Color(0xFF141518) else Color(0xFF9EA3B0),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Pitch Rows
                noteNames.forEachIndexed { rowIdx, noteName ->
                    val midiPitch = (currentOctave + 1) * 12 + semitoneOffsets[rowIdx]
                    val isBlackKey = noteName.contains("#")

                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .padding(vertical = 1.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        for (step in 0 until 16) {
                            val hasNote = clip.melodicNotes.any { it.step == step && it.midiNote == midiPitch }
                            val isCursor = isPlaying && step == currentStep
                            val isBeatGroup = (step / 4) % 2 == 0

                            val cellBg = when {
                                hasNote -> Color(0xFF51CF66)
                                isBlackKey -> if (isBeatGroup) Color(0xFF1F2128) else Color(0xFF18191E)
                                else -> if (isBeatGroup) Color(0xFF282B35) else Color(0xFF22242C)
                            }

                            val borderMod = if (isCursor) {
                                Modifier.border(2.dp, Color(0xFF20C997), RoundedCornerShape(4.dp))
                            } else {
                                Modifier.border(0.5.dp, Color(0xFF333642), RoundedCornerShape(4.dp))
                            }

                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(cellBg)
                                    .then(borderMod)
                                    .clickable { onToggleNote(step, midiPitch) }
                                    .testTag("note_${midiPitch}_$step"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (hasNote) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color(0xFF141518))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
