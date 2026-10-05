package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.DawViewModel

enum class MusicalScale(val displayName: String, val intervals: List<Int>) {
    CHROMATIC("Chromatic", listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)),
    MINOR_PENTATONIC("Minor Pentatonic", listOf(0, 3, 5, 7, 10)),
    NATURAL_MINOR("Minor", listOf(0, 2, 3, 5, 7, 8, 10)),
    MAJOR("Major", listOf(0, 2, 4, 5, 7, 9, 11)),
    DORIAN("Dorian", listOf(0, 2, 3, 5, 7, 9, 10))
}

@Composable
fun SynthKeysScreen(
    viewModel: DawViewModel,
    modifier: Modifier = Modifier
) {
    var baseOctave by remember { mutableIntStateOf(3) }
    var selectedScale by remember { mutableStateOf(MusicalScale.CHROMATIC) }
    val pressedKeys = remember { mutableStateMapOf<Int, Boolean>() }
    val view = LocalView.current

    val semitoneNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
            .padding(8.dp)
    ) {
        // Top Toolbar: Octave Selector & Scale Lock
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Octave Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Octave:", fontSize = 11.sp, color = Color(0xFF9EA3B0), fontWeight = FontWeight.Bold)
                for (oct in 2..5) {
                    Box(
                        modifier = Modifier
                            .size(width = 34.dp, height = 26.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (oct == baseOctave) Color(0xFF339AF0) else Color(0xFF25272F))
                            .pointerInput(oct) {
                                detectTapGestures { baseOctave = oct }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "C$oct",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (oct == baseOctave) Color.White else Color(0xFFA0A5B2)
                        )
                    }
                }
            }

            // Scale Lock Selector
            val scaleScroll = rememberScrollState()
            Row(
                modifier = Modifier.horizontalScroll(scaleScroll),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                MusicalScale.values().forEach { scale ->
                    val isSelected = scale == selectedScale
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) Color(0xFF20C997) else Color(0xFF22242B))
                            .pointerInput(scale) {
                                detectTapGestures { selectedScale = scale }
                            }
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .testTag("scale_${scale.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = scale.displayName,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF141518) else Color(0xFFC0C4D0)
                        )
                    }
                }
            }
        }

        // Piano Keyboard Area
        if (selectedScale == MusicalScale.CHROMATIC) {
            // Traditional 2-octave piano keyboard layout
            ChromaticKeyboard(
                baseOctave = baseOctave,
                pressedKeys = pressedKeys,
                onKeyDown = { note ->
                    pressedKeys[note] = true
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.triggerPianoKey(note)
                },
                onKeyUp = { note ->
                    pressedKeys[note] = false
                    viewModel.releasePianoKey(note)
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Scale Pad Grid (Only in-scale notes - Push style)
            ScalePadsLayout(
                baseOctave = baseOctave,
                scale = selectedScale,
                pressedKeys = pressedKeys,
                onKeyDown = { note ->
                    pressedKeys[note] = true
                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    viewModel.triggerPianoKey(note)
                },
                onKeyUp = { note ->
                    pressedKeys[note] = false
                    viewModel.releasePianoKey(note)
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun ChromaticKeyboard(
    baseOctave: Int,
    pressedKeys: Map<Int, Boolean>,
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hScroll = rememberScrollState()
    val whiteKeyWidth = 44.dp
    val blackKeyWidth = 28.dp

    // 14 white keys spanning ~2 octaves
    val whiteNotes = listOf(
        Pair(0, "C"), Pair(2, "D"), Pair(4, "E"), Pair(5, "F"),
        Pair(7, "G"), Pair(9, "A"), Pair(11, "B"),
        Pair(12, "C"), Pair(14, "D"), Pair(16, "E"), Pair(17, "F"),
        Pair(19, "G"), Pair(21, "A"), Pair(23, "B")
    )

    // Black notes relative to base octave (offset, name, horizontal key offset)
    val blackNotes = listOf(
        Triple(1, "C#", 0.7f),
        Triple(3, "D#", 1.7f),
        Triple(6, "F#", 3.7f),
        Triple(8, "G#", 4.7f),
        Triple(10, "A#", 5.7f),
        Triple(13, "C#", 7.7f),
        Triple(15, "D#", 8.7f),
        Triple(18, "F#", 10.7f),
        Triple(20, "G#", 11.7f),
        Triple(22, "A#", 12.7f)
    )

    val baseMidi = (baseOctave + 1) * 12

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1B1C22))
            .border(1.dp, Color(0xFF2C2F3A), RoundedCornerShape(8.dp))
            .horizontalScroll(hScroll)
            .padding(4.dp)
    ) {
        // White Keys Row
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            whiteNotes.forEach { (offset, name) ->
                val midiNote = baseMidi + offset
                val isPressed = pressedKeys[midiNote] == true

                Box(
                    modifier = Modifier
                        .width(whiteKeyWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .background(if (isPressed) Color(0xFF20C997) else Color(0xFFECEFF4))
                        .border(1.dp, Color(0xFF888E99), RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp))
                        .pointerInput(midiNote) {
                            detectTapGestures(
                                onPress = {
                                    onKeyDown(midiNote)
                                    tryAwaitRelease()
                                    onKeyUp(midiNote)
                                }
                            )
                        }
                        .padding(bottom = 12.dp)
                        .testTag("key_white_$midiNote"),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPressed) Color.White else Color(0xFF2E3440)
                    )
                }
            }
        }

        // Black Keys Overlay
        blackNotes.forEach { (offset, name, keyIndexOffset) ->
            val midiNote = baseMidi + offset
            val isPressed = pressedKeys[midiNote] == true
            val xOffset = whiteKeyWidth * keyIndexOffset + (keyIndexOffset * 2).dp

            Box(
                modifier = Modifier
                    .padding(start = xOffset)
                    .width(blackKeyWidth)
                    .fillMaxHeight(0.6f)
                    .clip(RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .background(if (isPressed) Color(0xFFFF764D) else Color(0xFF242730))
                    .border(1.dp, Color(0xFF141519), RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp))
                    .pointerInput(midiNote) {
                        detectTapGestures(
                            onPress = {
                                onKeyDown(midiNote)
                                tryAwaitRelease()
                                onKeyUp(midiNote)
                            }
                        )
                    }
                    .padding(bottom = 6.dp)
                    .testTag("key_black_$midiNote"),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(
                    text = name,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isPressed) Color.White else Color(0xFFD8DEE9)
                )
            }
        }
    }
}

@Composable
fun ScalePadsLayout(
    baseOctave: Int,
    scale: MusicalScale,
    pressedKeys: Map<Int, Boolean>,
    onKeyDown: (Int) -> Unit,
    onKeyUp: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val semitoneNames = listOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    val baseMidi = (baseOctave + 1) * 12

    // Build 2 octaves of scale notes
    val notes = mutableListOf<Pair<Int, String>>()
    for (oct in 0..1) {
        scale.intervals.forEach { interval ->
            val midi = baseMidi + oct * 12 + interval
            val noteName = semitoneNames[(interval) % 12] + (baseOctave + oct)
            notes.add(Pair(midi, noteName))
        }
    }

    val rows = notes.chunked(5)

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { rowNotes ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowNotes.forEach { (midi, noteName) ->
                    val isPressed = pressedKeys[midi] == true
                    val isRoot = noteName.startsWith("C")

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isPressed -> Color(0xFF20C997)
                                    isRoot -> Color(0xFF339AF0).copy(alpha = 0.35f)
                                    else -> Color(0xFF23252E)
                                }
                            )
                            .border(
                                width = if (isPressed || isRoot) 2.dp else 1.dp,
                                color = when {
                                    isPressed -> Color(0xFF20C997)
                                    isRoot -> Color(0xFF339AF0)
                                    else -> Color(0xFF343844)
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                            .pointerInput(midi) {
                                detectTapGestures(
                                    onPress = {
                                        onKeyDown(midi)
                                        tryAwaitRelease()
                                        onKeyUp(midi)
                                    }
                                )
                            }
                            .testTag("scale_key_$midi"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = noteName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPressed) Color(0xFF141518) else if (isRoot) Color(0xFF74C0FC) else Color.White
                        )
                    }
                }
            }
        }
    }
}
