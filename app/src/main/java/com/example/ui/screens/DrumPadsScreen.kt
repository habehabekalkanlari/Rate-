package com.example.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.model.DrumType
import com.example.viewmodel.DawViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DrumPadsScreen(
    viewModel: DawViewModel,
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val pressedMap = remember { mutableStateMapOf<DrumType, Boolean>() }
    val view = LocalView.current

    val padOrder = listOf(
        DrumType.TOM, DrumType.CRASH, DrumType.HIHAT_OPEN, DrumType.HIHAT_CLOSED,
        DrumType.RIMSHOT, DrumType.CLAP, DrumType.SNARE, DrumType.KICK
    )

    val padColors = mapOf(
        DrumType.KICK to Color(0xFFFF5252),
        DrumType.SNARE to Color(0xFFFF922B),
        DrumType.HIHAT_CLOSED to Color(0xFFFCC419),
        DrumType.HIHAT_OPEN to Color(0xFFFFE066),
        DrumType.CLAP to Color(0xFF51CF66),
        DrumType.RIMSHOT to Color(0xFF20C997),
        DrumType.TOM to Color(0xFF339AF0),
        DrumType.CRASH to Color(0xFFCC5DE8)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF141518))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pad header info
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ableton Push Drum Rack",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD2D6E0)
            )

            if (isRecording) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFF5252))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "● OVERDUB REC",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = "Live Mode (Tap to play)",
                    fontSize = 11.sp,
                    color = Color(0xFF868E96)
                )
            }
        }

        // 4x2 MPC/Push Pad Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(padOrder) { drumType ->
                val isPressed = pressedMap[drumType] == true
                val baseColor = padColors[drumType] ?: Color(0xFFFF764D)

                val animatedBg by animateColorAsState(
                    targetValue = if (isPressed) baseColor else baseColor.copy(alpha = 0.22f),
                    animationSpec = tween(durationMillis = 80),
                    label = "pad_color"
                )

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBg)
                        .border(
                            width = if (isPressed) 2.5.dp else 1.5.dp,
                            color = if (isPressed) Color.White else baseColor.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .pointerInput(drumType) {
                            detectTapGestures(
                                onPress = {
                                    pressedMap[drumType] = true
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    viewModel.triggerDrumPad(drumType, 1.0f)
                                    tryAwaitRelease()
                                    pressedMap[drumType] = false
                                }
                            )
                        }
                        .testTag("pad_${drumType.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = drumType.shortName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPressed) Color.White else Color(0xFFEDEDED)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = drumType.displayName.split(" ").first(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isPressed) Color.White.copy(alpha = 0.9f) else Color(0xFFA0A5B2)
                        )
                    }
                }
            }
        }
    }
}
