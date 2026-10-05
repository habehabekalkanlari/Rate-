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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DawProject
import com.example.model.Track
import com.example.ui.components.KnobControl
import com.example.ui.components.VuMeter
import com.example.viewmodel.DawViewModel
import kotlin.math.log10

@Composable
fun MixerScreen(
    viewModel: DawViewModel,
    project: DawProject,
    masterVu: Float,
    modifier: Modifier = Modifier
) {
    val hScroll = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
            .horizontalScroll(hScroll)
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Individual Track Channel Strips
        project.tracks.forEach { track ->
            TrackMixerStrip(
                track = track,
                onVolumeChange = { vol -> viewModel.setTrackVolume(track.id, vol) },
                onPanChange = { pan -> viewModel.setTrackPan(track.id, pan) },
                onToggleMute = { viewModel.toggleTrackMute(track.id) },
                onToggleSolo = { viewModel.toggleTrackSolo(track.id) }
            )
        }

        // Master Output Channel Strip
        MasterMixerStrip(
            masterVolume = project.masterVolume,
            masterVu = masterVu,
            swing = project.swing,
            onMasterVolumeChange = { viewModel.setMasterVolume(it) },
            onSwingChange = { viewModel.setSwing(it) }
        )
    }
}

@Composable
fun TrackMixerStrip(
    track: Track,
    onVolumeChange: (Float) -> Unit,
    onPanChange: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onToggleSolo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor = Color(track.colorHex)

    Column(
        modifier = modifier
            .width(86.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1C1D23))
            .border(1.dp, Color(0xFF282A33), RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(trackColor)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Pan Knob (-1f to 1f mapped to 0f..1f)
        val panNorm = (track.pan + 1f) / 2f
        KnobControl(
            label = "PAN",
            value = panNorm,
            onValueChange = { norm ->
                onPanChange(norm * 2f - 1f)
            },
            size = 36.dp,
            activeColor = trackColor,
            displayValue = when {
                track.pan < -0.05f -> "${(-track.pan * 50).toInt()}L"
                track.pan > 0.05f -> "${(track.pan * 50).toInt()}R"
                else -> "C"
            }
        )

        // Mute / Solo Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (track.isMuted) Color(0xFFFF5252) else Color(0xFF2A2C36))
                    .clickable { onToggleMute() }
                    .testTag("mixer_mute_${track.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "M",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (track.isMuted) Color.White else Color(0xFF9EA3B0)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (track.isSolo) Color(0xFF40C057) else Color(0xFF2A2C36))
                    .clickable { onToggleSolo() }
                    .testTag("mixer_solo_${track.id}"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "S",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (track.isSolo) Color(0xFF141518) else Color(0xFF9EA3B0)
                )
            }
        }

        // Fader & VU Meter Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track VU Level
            val simLevel = if (track.isMuted) 0f else track.volume * 0.65f
            VuMeter(
                level = simLevel,
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Volume Fader Slider
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Slider(
                    value = track.volume,
                    onValueChange = { onVolumeChange(it) },
                    valueRange = 0f..1.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = trackColor,
                        activeTrackColor = trackColor,
                        inactiveTrackColor = Color(0xFF2B2D37)
                    ),
                    modifier = Modifier.fillMaxHeight().width(32.dp).testTag("fader_${track.id}")
                )
            }
        }

        // dB readout
        val db = if (track.volume > 0.001f) {
            (20 * log10(track.volume.toDouble())).toInt()
        } else -99
        Text(
            text = if (db <= -60) "-inf dB" else "${if (db > 0) "+$db" else "$db"} dB",
            fontSize = 9.sp,
            color = Color(0xFFA0A5B2)
        )
    }
}

@Composable
fun MasterMixerStrip(
    masterVolume: Float,
    masterVu: Float,
    swing: Float,
    onMasterVolumeChange: (Float) -> Unit,
    onSwingChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(96.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF21232B))
            .border(1.5.dp, Color(0xFFFF922B).copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Master Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "MASTER",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFF922B)
            )
        }

        // Swing Groove Knob
        KnobControl(
            label = "GROOVE",
            value = swing / 0.5f,
            onValueChange = { onSwingChange(it * 0.5f) },
            size = 36.dp,
            activeColor = Color(0xFFFF922B),
            displayValue = "${(swing * 200).toInt()}%"
        )

        // Fader & Stereo Master VU Meters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Stereo VU meters (L & R)
            VuMeter(
                level = masterVu,
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(2.dp))
            VuMeter(
                level = (masterVu * 0.95f).coerceIn(0f, 1f),
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Master Slider
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Slider(
                    value = masterVolume,
                    onValueChange = { onMasterVolumeChange(it) },
                    valueRange = 0f..1.2f,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF922B),
                        activeTrackColor = Color(0xFFFF922B),
                        inactiveTrackColor = Color(0xFF2E313C)
                    ),
                    modifier = Modifier.fillMaxHeight().width(32.dp).testTag("master_fader")
                )
            }
        }

        // Master dB readout
        val db = if (masterVolume > 0.001f) {
            (20 * log10(masterVolume.toDouble())).toInt()
        } else -99
        Text(
            text = if (db <= -60) "-inf dB" else "${if (db > 0) "+$db" else "$db"} dB",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF922B)
        )
    }
}
