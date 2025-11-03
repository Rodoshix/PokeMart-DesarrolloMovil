package com.pokermart.ecommerce.ui.common

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
fun LogoGiratorio(modifier: Modifier = Modifier) {
    val transicion = rememberInfiniteTransition(label = "logo-pokemart")
    val rotacion by transicion.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotacion-logo"
    )

    Canvas(modifier = modifier.size(96.dp)) {
        rotate(rotacion) {
            drawCircle(color = Color(0xFFEF5350))
            drawArc(
                color = Color.White,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = true
            )
            drawCircle(
                color = Color.Black,
                radius = size.minDimension / 6,
                style = Stroke(width = size.minDimension / 12)
            )
            drawCircle(color = Color.White, radius = size.minDimension / 8)
        }
    }
}
