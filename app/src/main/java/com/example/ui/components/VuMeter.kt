package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VuMeter(
    level: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier.width(6.dp).fillMaxHeight(),
    segmentCount: Int = 12
) {
    Canvas(modifier = modifier) {
        val totalHeight = size.height
        val totalWidth = size.width
        val segmentGap = 2f
        val segmentHeight = (totalHeight - (segmentCount - 1) * segmentGap) / segmentCount

        val activeCount = (level.coerceIn(0f, 1f) * segmentCount).toInt()

        for (i in 0 until segmentCount) {
            // Draw from bottom to top
            val fromBottomIdx = i
            val y = totalHeight - (i + 1) * segmentHeight - i * segmentGap
            val isActive = fromBottomIdx < activeCount

            val segmentColor = when {
                i >= segmentCount - 2 -> if (isActive) Color(0xFFFF4D4D) else Color(0x33FF4D4D) // Red clip
                i >= segmentCount - 5 -> if (isActive) Color(0xFFFFB800) else Color(0x33FFB800) // Amber warn
                else -> if (isActive) Color(0xFF20C997) else Color(0x2220C997)                  // Green ok
            }

            drawRoundRect(
                color = segmentColor,
                topLeft = Offset(0f, y),
                size = Size(totalWidth, segmentHeight),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
        }
    }
}
