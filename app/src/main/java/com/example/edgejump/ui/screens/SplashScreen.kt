package com.example.edgejump.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edgejump.ui.theme.AccentMint
import com.example.edgejump.ui.theme.PrimaryNeonCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.4f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500)
        )
        delay(900L)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable { onSplashFinished() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            }
        ) {
            // EdgeJump Geometric Emblem
            Canvas(modifier = Modifier.size(120.dp)) {
                val w = size.width
                val h = size.height
                val midX = w / 2f
                val midY = h / 2f

                // Outer edge accents
                val barLen = 32.dp.toPx()
                val barThick = 5.dp.toPx()
                val cr = CornerRadius(2.dp.toPx(), 2.dp.toPx())

                // Left bar
                drawRoundRect(PrimaryNeonCyan, Offset(8f, midY - barLen / 2f), Size(barThick, barLen), cr)
                // Right bar
                drawRoundRect(PrimaryNeonCyan, Offset(w - 8f - barThick, midY - barLen / 2f), Size(barThick, barLen), cr)
                // Top bar
                drawRoundRect(PrimaryNeonCyan, Offset(midX - barLen / 2f, 8f), Size(barLen, barThick), cr)
                // Bottom bar
                drawRoundRect(PrimaryNeonCyan, Offset(midX - barLen / 2f, h - 8f - barThick), Size(barLen, barThick), cr)

                // Central glowing diamond
                val diamondRadius = 24.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(PrimaryNeonCyan.copy(alpha = 0.5f), Color.Transparent),
                        center = Offset(midX, midY),
                        radius = diamondRadius * 2.2f
                    ),
                    radius = diamondRadius * 2.2f,
                    center = Offset(midX, midY)
                )

                val diamondPath = Path().apply {
                    moveTo(midX, midY - diamondRadius)
                    lineTo(midX + diamondRadius, midY)
                    lineTo(midX, midY + diamondRadius)
                    lineTo(midX - diamondRadius, midY)
                    close()
                }
                drawPath(diamondPath, AccentMint)

                val innerPath = Path().apply {
                    val r = diamondRadius * 0.45f
                    moveTo(midX, midY - r)
                    lineTo(midX + r, midY)
                    lineTo(midX, midY + r)
                    lineTo(midX - r, midY)
                    close()
                }
                drawPath(innerPath, Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "EDGEJUMP",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "PRECISION ARCADE",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 3.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = PrimaryNeonCyan
            )
        }
    }
}
