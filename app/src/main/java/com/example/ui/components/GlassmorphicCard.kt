package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Custom modifier to draw high-end outer neon glow
fun Modifier.neonGlow(
    color: Color,
    radius: Dp = 8.dp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp)
) = this.drawBehind {
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        this.color = Color.Transparent.toArgb()
        setShadowLayer(
            radius.toPx(),
            0f,
            0f,
            color.copy(alpha = 0.5f).toArgb()
        )
    }
    drawIntoCanvas { canvas ->
        val customPaint = androidx.compose.ui.graphics.Paint().apply {
            val nativePaint = this.asFrameworkPaint()
            nativePaint.set(paint)
        }
        canvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            shape.topStart.toPx(size, this),
            shape.topStart.toPx(size, this),
            customPaint
        )
    }
}

@Composable
fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    glowColor: Color? = null,
    borderStroke: BorderStroke? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val baseGlowColor = glowColor ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    
    val border = borderStroke ?: BorderStroke(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.15f),
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    )

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surface.copy(alpha = 0.45f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.15f)
        )
    )

    var rootModifier = modifier
        .shadow(
            elevation = 12.dp,
            shape = shape,
            clip = false,
            ambientColor = Color.Black.copy(alpha = 0.5f),
            spotColor = baseGlowColor.copy(alpha = 0.2f)
        )
        .neonGlow(color = baseGlowColor, radius = 6.dp, shape = shape)
        .clip(shape)
        .background(backgroundBrush)
        .border(border, shape)

    if (onClick != null) {
        rootModifier = rootModifier.clickable(onClick = onClick)
    }

    Box(
        modifier = rootModifier,
        content = content
    )
}
