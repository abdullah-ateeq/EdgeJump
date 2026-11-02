package com.example.edgejump.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.edgejump.model.Direction

@Composable
fun DirectionPad(
    onDirectionClick: (Direction) -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 56.dp,
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // UP Button
        DirectionButton(
            icon = AppIcons.ArrowUp,
            contentDescription = "Jump Up",
            onClick = { onDirectionClick(Direction.UP) },
            buttonSize = buttonSize,
            enabled = enabled
        )

        // LEFT, CENTER DECORATION, RIGHT
        Row(
            horizontalArrangement = Arrangement.spacedBy(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DirectionButton(
                icon = AppIcons.ArrowLeft,
                contentDescription = "Jump Left",
                onClick = { onDirectionClick(Direction.LEFT) },
                buttonSize = buttonSize,
                enabled = enabled
            )

            DirectionButton(
                icon = AppIcons.ArrowRight,
                contentDescription = "Jump Right",
                onClick = { onDirectionClick(Direction.RIGHT) },
                buttonSize = buttonSize,
                enabled = enabled
            )
        }

        // DOWN Button
        DirectionButton(
            icon = AppIcons.ArrowDown,
            contentDescription = "Jump Down",
            onClick = { onDirectionClick(Direction.DOWN) },
            buttonSize = buttonSize,
            enabled = enabled
        )
    }
}

@Composable
private fun DirectionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    buttonSize: Dp,
    enabled: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .size(buttonSize)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = primaryColor),
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(28.dp)
        )
    }
}
