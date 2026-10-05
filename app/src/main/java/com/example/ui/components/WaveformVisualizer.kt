package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WaveformVisualizer(
    waveformData: FloatArray,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    lineColor: Color = Color(0xFFFF764D)
) {
    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(0xFF141518))
            .border(1.dp, Color(0xFF2B2D33), RoundedCornerShape(4.dp))
    ) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        // Draw subtle grid center line
        drawLine(
            color = Color(0x22FFFFFF),
            start = Offset(0f, midY),
            end = Offset(w, midY),
            strokeWidth = 1f
        )

        if (waveformData.isEmpty()) return@Canvas

        val path = Path()
        val stepX = w / (waveformData.size - 1).coerceAtLeast(1)

        for (i in waveformData.indices) {
            val sample = if (isPlaying) waveformData[i].coerceIn(-1f, 1f) else 0f
            val x = i * stepX
            val y = midY - sample * (h * 0.42f)

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        drawPath(
            path = path,
            color = if (isPlaying) lineColor else Color(0x66FF764D),
            style = Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
