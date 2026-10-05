package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun KnobControl(
    label: String,
    value: Float, // 0.0f to 1.0f
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    activeColor: Color = Color(0xFFFF764D),
    inactiveColor: Color = Color(0xFF32353D),
    displayValue: String = "${(value * 100).toInt()}%",
    defaultValue: Float = 0.5f
) {
    var accumulatedDrag by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.testTag("knob_${label.lowercase().replace(" ", "_")}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Canvas(
            modifier = Modifier
                .size(size)
                .pointerInput(label) {
                    detectTapGestures(
                        onDoubleTap = {
                            onValueChange(defaultValue)
                        }
                    )
                }
                .pointerInput(label) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Drag up = increase, drag down = decrease
                        val delta = -dragAmount.y / 200f
                        val newValue = (value + delta).coerceIn(0f, 1f)
                        onValueChange(newValue)
                    }
                }
        ) {
            val strokeWidth = 3.dp.toPx()
            val diameter = this.size.minDimension - strokeWidth * 2
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth, strokeWidth)

            val startAngle = 135f
            val totalSweep = 270f
            val currentSweep = value.coerceIn(0f, 1f) * totalSweep

            // Background track arc
            drawArc(
                color = inactiveColor,
                startAngle = startAngle,
                sweepAngle = totalSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Active value arc
            if (currentSweep > 0.5f) {
                drawArc(
                    color = activeColor,
                    startAngle = startAngle,
                    sweepAngle = currentSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Central dial body
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val innerRadius = diameter / 2f - 4.dp.toPx()
            drawCircle(
                color = Color(0xFF22242A),
                radius = innerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF383B44),
                radius = innerRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Pointer indicator line
            val angleRad = Math.toRadians((startAngle + currentSweep).toDouble())
            val pointerLen = innerRadius * 0.75f
            val pointerEnd = Offset(
                center.x + (pointerLen * cos(angleRad)).toFloat(),
                center.y + (pointerLen * sin(angleRad)).toFloat()
            )
            val pointerStart = Offset(
                center.x + (innerRadius * 0.2f * cos(angleRad)).toFloat(),
                center.y + (innerRadius * 0.2f * sin(angleRad)).toFloat()
            )

            drawLine(
                color = activeColor,
                start = pointerStart,
                end = pointerEnd,
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF9EA3B0),
            maxLines = 1
        )
        Text(
            text = displayValue,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFD2D6E0),
            maxLines = 1
        )
    }
}
