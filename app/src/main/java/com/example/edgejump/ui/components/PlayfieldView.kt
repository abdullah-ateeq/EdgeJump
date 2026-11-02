package com.example.edgejump.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.edgejump.model.Direction
import com.example.edgejump.model.GamePhase
import com.example.edgejump.model.GameState
import com.example.edgejump.model.JumpOutcome
import com.example.edgejump.model.Platform
import com.example.edgejump.ui.theme.AccentAmber
import com.example.edgejump.ui.theme.AccentCoral
import com.example.edgejump.ui.theme.AccentMint
import com.example.edgejump.ui.theme.PrimaryNeonCyan
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PlayfieldView(
    gameState: GameState,
    onDirectionInput: (Direction) -> Unit,
    modifier: Modifier = Modifier,
    enableSwipe: Boolean = true
) {
    // Screen shake animation on game over
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(gameState.phase, gameState.lastOutcome) {
        if (gameState.phase == GamePhase.GAME_OVER || gameState.lastOutcome != JumpOutcome.SUCCESS) {
            for (i in 0..4) {
                val magnitude = (4 - i) * 3f
                shakeOffset.animateTo(if (i % 2 == 0) magnitude else -magnitude, tween(30))
            }
            shakeOffset.animateTo(0f, tween(40))
        }
    }

    // Target pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "target_pulse"
    )

    // Swipe gesture tracking
    var dragAccumX by remember { mutableFloatStateOf(0f) }
    var dragAccumY by remember { mutableFloatStateOf(0f) }
    var gestureConsumed by remember { mutableStateOf(false) }

    val swipeModifier = if (enableSwipe && !gameState.isInputLocked) {
        Modifier.pointerInput(gameState.targetPlatform.id) {
            detectDragGestures(
                onDragStart = {
                    dragAccumX = 0f
                    dragAccumY = 0f
                    gestureConsumed = false
                },
                onDragEnd = {
                    dragAccumX = 0f
                    dragAccumY = 0f
                    gestureConsumed = false
                },
                onDragCancel = {
                    dragAccumX = 0f
                    dragAccumY = 0f
                    gestureConsumed = false
                },
                onDrag = { change, dragAmount ->
                    change.consume()
                    if (!gestureConsumed) {
                        dragAccumX += dragAmount.x
                        dragAccumY += dragAmount.y
                        val detected = Direction.fromDelta(dragAccumX, dragAccumY, threshold = 40f)
                        if (detected != null) {
                            gestureConsumed = true
                            onDirectionInput(detected)
                        }
                    }
                }
            )
        }
    } else {
        Modifier
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .background(surfaceColor)
            .then(swipeModifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Apply screen shake
            drawContext.transform.translate(left = shakeOffset.value, top = 0f)

            // Draw subtle arena grid/radar lines
            drawGrid(width, height, borderColor)

            // Draw arena borders with sleek corner accents
            drawArenaBorders(width, height, borderColor)

            // Draw current platform
            drawPlatform(
                platform = gameState.currentPlatform,
                width = width,
                height = height,
                isTarget = false,
                color = primaryColor,
                pulse = 1f
            )

            // Draw target platform (highlighted with pulse)
            if (gameState.phase != GamePhase.GAME_OVER) {
                drawPlatform(
                    platform = gameState.targetPlatform,
                    width = width,
                    height = height,
                    isTarget = true,
                    color = AccentMint,
                    pulse = pulseScale
                )

                // Draw subtle directional indicator from current towards target
                drawDirectionIndicator(
                    fromX = gameState.characterX * width,
                    fromY = gameState.characterY * height,
                    targetEdge = gameState.targetPlatform.edge,
                    color = PrimaryNeonCyan.copy(alpha = 0.55f),
                    pulse = pulseScale
                )
            }

            // Draw the Character
            val charCenterX = gameState.characterX * width
            val charCenterY = gameState.characterY * height

            drawCharacter(
                centerX = charCenterX,
                centerY = charCenterY,
                phase = gameState.phase,
                outcome = gameState.lastOutcome,
                jumpProgress = gameState.jumpProgress,
                primaryColor = primaryColor
            )
        }
    }
}

private fun DrawScope.drawGrid(width: Float, height: Float, color: Color) {
    val lineCol = color.copy(alpha = 0.12f)
    val midX = width / 2f
    val midY = height / 2f

    // Center crosshair lines
    drawLine(lineCol, Offset(midX, height * 0.15f), Offset(midX, height * 0.85f), strokeWidth = 1.5f)
    drawLine(lineCol, Offset(width * 0.15f, midY), Offset(width * 0.85f, midY), strokeWidth = 1.5f)

    // Center circular radar ring
    drawCircle(
        color = lineCol,
        radius = width.coerceAtMost(height) * 0.18f,
        center = Offset(midX, midY),
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawArenaBorders(width: Float, height: Float, borderColor: Color) {
    val strokeWidth = 3f
    val cornerRadius = 24.dp.toPx()
    drawRoundRect(
        color = borderColor.copy(alpha = 0.65f),
        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
        size = Size(width - strokeWidth, height - strokeWidth),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Stroke(width = strokeWidth)
    )
}

private fun DrawScope.drawPlatform(
    platform: Platform,
    width: Float,
    height: Float,
    isTarget: Boolean,
    color: Color,
    pulse: Float
) {
    val thickness = platform.thicknessFraction * (if (platform.edge == Direction.LEFT || platform.edge == Direction.RIGHT) width else height)
    val spanLength = platform.widthFraction * (if (platform.edge == Direction.LEFT || platform.edge == Direction.RIGHT) height else width) * (if (isTarget) pulse else 1f)

    val (rectTopLeft, rectSize) = when (platform.edge) {
        Direction.LEFT -> {
            val top = platform.offsetFraction * height - (spanLength / 2f)
            Pair(Offset(0f, top), Size(thickness, spanLength))
        }
        Direction.RIGHT -> {
            val top = platform.offsetFraction * height - (spanLength / 2f)
            Pair(Offset(width - thickness, top), Size(thickness, spanLength))
        }
        Direction.UP -> {
            val left = platform.offsetFraction * width - (spanLength / 2f)
            Pair(Offset(left, 0f), Size(spanLength, thickness))
        }
        Direction.DOWN -> {
            val left = platform.offsetFraction * width - (spanLength / 2f)
            Pair(Offset(left, height - thickness), Size(spanLength, thickness))
        }
    }

    val cr = CornerRadius(8.dp.toPx(), 8.dp.toPx())

    // Glow background for target
    if (isTarget) {
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = 0.45f), Color.Transparent),
                center = Offset(rectTopLeft.x + rectSize.width / 2f, rectTopLeft.y + rectSize.height / 2f),
                radius = spanLength * 0.7f
            ),
            topLeft = rectTopLeft - Offset(12f, 12f),
            size = Size(rectSize.width + 24f, rectSize.height + 24f)
        )
    }

    // Platform solid body
    drawRoundRect(
        color = color,
        topLeft = rectTopLeft,
        size = rectSize,
        cornerRadius = cr
    )

    // Inner highlight bar
    val highlightColor = Color.White.copy(alpha = if (isTarget) 0.85f else 0.4f)
    drawRoundRect(
        color = highlightColor,
        topLeft = rectTopLeft + Offset(2f, 2f),
        size = Size((rectSize.width - 4f).coerceAtLeast(1f), (rectSize.height - 4f).coerceAtLeast(1f)),
        cornerRadius = cr,
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.drawCharacter(
    centerX: Float,
    centerY: Float,
    phase: GamePhase,
    outcome: JumpOutcome,
    jumpProgress: Float,
    primaryColor: Color
) {
    val baseRadius = 16.dp.toPx()

    // Squash & stretch dynamics based on jump progress
    val (scaleX, scaleY) = when (phase) {
        GamePhase.JUMPING -> {
            // Elongate along flight, squash slightly across
            val stretch = sin(jumpProgress * PI.toFloat()) * 0.35f
            Pair(1f - stretch * 0.4f, 1f + stretch)
        }
        GamePhase.LANDED -> {
            // Squash on impact
            Pair(1.35f, 0.75f)
        }
        else -> Pair(1f, 1f)
    }

    val charColor = when {
        outcome != JumpOutcome.SUCCESS && phase == GamePhase.GAME_OVER -> AccentCoral
        phase == GamePhase.LANDED -> AccentMint
        else -> primaryColor
    }

    // Outer glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(charColor.copy(alpha = 0.5f), Color.Transparent),
            center = Offset(centerX, centerY),
            radius = baseRadius * 2.2f
        ),
        radius = baseRadius * 2.2f,
        center = Offset(centerX, centerY)
    )

    // Character core: rotated diamond / prism
    val path = Path().apply {
        val rx = baseRadius * scaleX
        val ry = baseRadius * scaleY
        moveTo(centerX, centerY - ry)
        lineTo(centerX + rx, centerY)
        lineTo(centerX, centerY + ry)
        lineTo(centerX - rx, centerY)
        close()
    }

    drawPath(path = path, color = charColor)

    // Inner bright diamond core
    val innerPath = Path().apply {
        val rx = baseRadius * scaleX * 0.5f
        val ry = baseRadius * scaleY * 0.5f
        moveTo(centerX, centerY - ry)
        lineTo(centerX + rx, centerY)
        lineTo(centerX, centerY + ry)
        lineTo(centerX - rx, centerY)
        close()
    }
    drawPath(path = innerPath, color = Color.White)
}

private fun DrawScope.drawDirectionIndicator(
    fromX: Float,
    fromY: Float,
    targetEdge: Direction,
    color: Color,
    pulse: Float
) {
    val arrowLength = 26.dp.toPx() * pulse
    val (dx, dy) = when (targetEdge) {
        Direction.LEFT -> Pair(-arrowLength, 0f)
        Direction.RIGHT -> Pair(arrowLength, 0f)
        Direction.UP -> Pair(0f, -arrowLength)
        Direction.DOWN -> Pair(0f, arrowLength)
    }

    val start = Offset(fromX, fromY)
    val end = Offset(fromX + dx, fromY + dy)

    drawLine(
        color = color,
        start = start,
        end = end,
        strokeWidth = 3f
    )

    // Draw arrowhead
    val angle = kotlin.math.atan2(dy, dx)
    val headSize = 10.dp.toPx()
    val p1 = Offset(
        end.x - headSize * cos(angle - PI / 6).toFloat(),
        end.y - headSize * sin(angle - PI / 6).toFloat()
    )
    val p2 = Offset(
        end.x - headSize * cos(angle + PI / 6).toFloat(),
        end.y - headSize * sin(angle + PI / 6).toFloat()
    )

    val headPath = Path().apply {
        moveTo(end.x, end.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }
    drawPath(path = headPath, color = color)
}
