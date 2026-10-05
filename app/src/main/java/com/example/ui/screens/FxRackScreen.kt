package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EffectsConfig
import com.example.ui.components.KnobControl
import com.example.viewmodel.DawViewModel

@Composable
fun FxRackScreen(
    viewModel: DawViewModel,
    effects: EffectsConfig,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
            .verticalScroll(scrollState)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: XY Filter Touch Pad
        XyFilterPad(
            cutoff = effects.filterCutoff,
            resonance = effects.filterResonance,
            isHighPass = effects.filterType == 1,
            onFilterChange = { newCutoff, newRes ->
                viewModel.updateEffects {
                    copy(filterCutoff = newCutoff, filterResonance = newRes)
                }
            },
            onToggleType = {
                viewModel.updateEffects {
                    copy(filterType = if (filterType == 0) 1 else 0)
                }
            }
        )

        // Section 2: Delay & Reverb Units
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stereo Delay Unit
            FxModuleCard(
                title = "ECHO / DELAY",
                color = Color(0xFF339AF0),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    KnobControl(
                        label = "TIME",
                        value = effects.delayTimeSec / 0.75f,
                        onValueChange = { norm ->
                            viewModel.updateEffects { copy(delayTimeSec = norm * 0.75f) }
                        },
                        displayValue = "${(effects.delayTimeSec * 1000).toInt()}ms",
                        activeColor = Color(0xFF339AF0)
                    )

                    KnobControl(
                        label = "FEEDBACK",
                        value = effects.delayFeedback / 0.85f,
                        onValueChange = { norm ->
                            viewModel.updateEffects { copy(delayFeedback = norm * 0.85f) }
                        },
                        displayValue = "${(effects.delayFeedback * 100).toInt()}%",
                        activeColor = Color(0xFF339AF0)
                    )

                    KnobControl(
                        label = "MIX",
                        value = effects.delayMix,
                        onValueChange = { norm ->
                            viewModel.updateEffects { copy(delayMix = norm) }
                        },
                        displayValue = "${(effects.delayMix * 100).toInt()}%",
                        activeColor = Color(0xFF339AF0)
                    )
                }
            }

            // Studio Reverb Unit
            FxModuleCard(
                title = "STUDIO REVERB",
                color = Color(0xFFCC5DE8),
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    KnobControl(
                        label = "SIZE",
                        value = effects.reverbSize,
                        onValueChange = { norm ->
                            viewModel.updateEffects { copy(reverbSize = norm) }
                        },
                        displayValue = "${(effects.reverbSize * 100).toInt()}%",
                        activeColor = Color(0xFFCC5DE8)
                    )

                    KnobControl(
                        label = "MIX",
                        value = effects.reverbMix,
                        onValueChange = { norm ->
                            viewModel.updateEffects { copy(reverbMix = norm) }
                        },
                        displayValue = "${(effects.reverbMix * 100).toInt()}%",
                        activeColor = Color(0xFFCC5DE8)
                    )
                }
            }
        }

        // Section 3: Analog Overdrive / Saturator Unit
        FxModuleCard(
            title = "SATURATOR / OVERDRIVE",
            color = Color(0xFFFF922B),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Warm Tape / Tube Saturation",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE2E4E9)
                    )
                    Text(
                        text = "Adds analog warmth and harmonic richness to drums and bass.",
                        fontSize = 10.sp,
                        color = Color(0xFF868E96)
                    )
                }

                KnobControl(
                    label = "DRIVE",
                    value = effects.drive / 0.9f,
                    onValueChange = { norm ->
                        viewModel.updateEffects { copy(drive = norm * 0.9f) }
                    },
                    size = 52.dp,
                    displayValue = "${(effects.drive * 100).toInt()}%",
                    activeColor = Color(0xFFFF922B)
                )
            }
        }
    }
}

@Composable
fun XyFilterPad(
    cutoff: Float,
    resonance: Float,
    isHighPass: Boolean,
    onFilterChange: (Float, Float) -> Unit,
    onToggleType: () -> Unit,
    modifier: Modifier = Modifier
) {
    FxModuleCard(
        title = "AUTOFILTER (XY TOUCH PAD)",
        color = Color(0xFF20C997),
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "X: Cutoff (${(cutoff * 100).toInt()}%) | Y: Resonance (${(resonance * 100).toInt()}%)",
                    fontSize = 11.sp,
                    color = Color(0xFFD2D6E0),
                    fontWeight = FontWeight.Medium
                )

                // LP / HP Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isHighPass) Color(0xFFFF922B) else Color(0xFF20C997))
                        .clickable { onToggleType() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("filter_type_toggle")
                ) {
                    Text(
                        text = if (isHighPass) "MODE: HIGH-PASS" else "MODE: LOW-PASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF141518)
                    )
                }
            }

            // Interactive 2D XY Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF15171C))
                    .border(1.dp, Color(0xFF2B2E38), RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val normX = (change.position.x / size.width).coerceIn(0.05f, 0.99f)
                            val normY = (1f - change.position.y / size.height).coerceIn(0f, 0.85f)
                            onFilterChange(normX, normY)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val normX = (offset.x / size.width).coerceIn(0.05f, 0.99f)
                            val normY = (1f - offset.y / size.height).coerceIn(0f, 0.85f)
                            onFilterChange(normX, normY)
                        }
                    }
                    .testTag("xy_filter_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Grid lines
                    for (xStep in 1..3) {
                        drawLine(
                            color = Color(0x15FFFFFF),
                            start = Offset(w * (xStep / 4f), 0f),
                            end = Offset(w * (xStep / 4f), h),
                            strokeWidth = 1f
                        )
                    }
                    for (yStep in 1..3) {
                        drawLine(
                            color = Color(0x15FFFFFF),
                            start = Offset(0f, h * (yStep / 4f)),
                            end = Offset(w, h * (yStep / 4f)),
                            strokeWidth = 1f
                        )
                    }

                    // Puck position
                    val puckX = cutoff.coerceIn(0f, 1f) * w
                    val puckY = (1f - (resonance / 0.85f).coerceIn(0f, 1f)) * h

                    // Crosshair lines
                    drawLine(
                        color = Color(0x5520C997),
                        start = Offset(puckX, 0f),
                        end = Offset(puckX, h),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawLine(
                        color = Color(0x5520C997),
                        start = Offset(0f, puckY),
                        end = Offset(w, puckY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Glowing Puck Circle
                    drawCircle(
                        color = Color(0x4420C997),
                        radius = 22.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                    drawCircle(
                        color = Color(0xFF20C997),
                        radius = 9.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                }
            }
        }
    }
}

@Composable
fun FxModuleCard(
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1E2027))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        content()
    }
}
