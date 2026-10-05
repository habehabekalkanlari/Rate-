package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DawTab

@Composable
fun DawHeader(
    isPlaying: Boolean,
    isRecording: Boolean,
    metronomeEnabled: Boolean,
    bpm: Int,
    currentBar: Int,
    currentStep: Int,
    masterVu: Float,
    waveform: FloatArray,
    selectedTab: DawTab,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    onToggleRecord: () -> Unit,
    onToggleMetronome: () -> Unit,
    onBpmChange: (Int) -> Unit,
    onTapTempo: () -> Unit,
    onSelectTab: (DawTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF18191C))
            .border(width = 1.dp, color = Color(0xFF26282E))
    ) {
        // Top Transport Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Ableton-style transport controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Play / Pause Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isPlaying) Color(0xFF20C997) else Color(0xFF2D3037))
                        .clickable { onTogglePlay() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = if (isPlaying) Color(0xFF141518) else Color(0xFFE2E4E9),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Stop Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2D3037))
                        .clickable { onStop() }
                        .testTag("stop_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = "Stop",
                        tint = Color(0xFFE2E4E9),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Record Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isRecording) Color(0xFFFF4D4D) else Color(0xFF2D3037))
                        .clickable { onToggleRecord() }
                        .testTag("record_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.FiberManualRecord,
                        contentDescription = "Record",
                        tint = if (isRecording) Color.White else Color(0xFFFF6B6B),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Metronome Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (metronomeEnabled) Color(0xFF339AF0) else Color(0xFF25272D))
                        .clickable { onToggleMetronome() }
                        .testTag("metronome_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sensors,
                        contentDescription = "Metronome",
                        tint = if (metronomeEnabled) Color.White else Color(0xFF868E96),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // BPM & Tap Tempo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // BPM Adjuster Box
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF22242A))
                        .border(1.dp, Color(0xFF32353E), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "-",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier
                            .clickable { onBpmChange(bpm - 1) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Text(
                        text = "$bpm",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF922B),
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    Text(
                        text = "+",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAAAAAA),
                        modifier = Modifier
                            .clickable { onBpmChange(bpm + 1) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                // Tap Tempo Button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF2D3037))
                        .clickable { onTapTempo() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("tap_tempo_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TAP",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E4E9)
                    )
                }
            }

            // Time Counter: BAR : BEAT : 16TH
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF141518))
                    .border(1.dp, Color(0xFF282A31), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val beatInBar = (currentStep / 4) + 1
                val stepInBeat = (currentStep % 4) + 1
                Text(
                    text = String.format("%02d.%d.%d", currentBar, beatInBar, stepInBeat),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) Color(0xFF51CF66) else Color(0xFF868E96)
                )
            }

            // Oscilloscope Mini Waveform Monitor
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 32.dp)
            ) {
                WaveformVisualizer(
                    waveformData = waveform,
                    isPlaying = isPlaying,
                    modifier = Modifier.fillMaxWidth().height(32.dp)
                )
            }

            // Master Peak VU Meter
            VuMeter(
                level = masterVu,
                modifier = Modifier
                    .width(8.dp)
                    .height(32.dp)
            )
        }

        // Horizontal DAW View Navigation Bar
        val scrollState = rememberScrollState()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1B1C20))
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DawTab.values().forEach { tab ->
                val isSelected = tab == selectedTab
                val icon = when (tab) {
                    DawTab.SESSION -> Icons.Filled.GridOn
                    DawTab.SEQUENCER -> Icons.Filled.MusicNote
                    DawTab.PADS -> Icons.Filled.ViewModule
                    DawTab.KEYS -> Icons.Filled.Layers
                    DawTab.MIXER -> Icons.Filled.GraphicEq
                    DawTab.FX -> Icons.Filled.Tune
                    DawTab.PROJECTS -> Icons.Filled.VolumeUp
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) Color(0xFFFF764D) else Color(0xFF25272D))
                        .clickable { onSelectTab(tab) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("tab_${tab.name.lowercase()}"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) Color(0xFF141518) else Color(0xFF9EA3B0),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color(0xFF141518) else Color(0xFFD2D6E0)
                    )
                }
            }
        }
    }
}
