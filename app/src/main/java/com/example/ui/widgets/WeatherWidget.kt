package com.example.ui.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard

@Composable
fun WeatherWidget(
    modifier: Modifier = Modifier,
    cityIndex: Int = 0,
    weatherText: String = "Nắng ấm nhẹ, 29°C",
    onWidgetClick: () -> Unit,
    glowColor: Color = MaterialTheme.colorScheme.primary
) {
    val cityName = when (cityIndex) {
        0 -> "Hà Nội"
        1 -> "TP. Hồ Chí Minh"
        2 -> "Đà Nẵng"
        3 -> "Hải Phòng"
        else -> "Việt Nam"
    }

    val tempStr = weatherText.substringAfterLast(", ").substringBefore("°C")
    val weatherLabel = weatherText.substringBefore(", ")

    // Infinite float animation for cloud floating or rain drop pulse
    val infiniteTransition = rememberInfiniteTransition(label = "weather")
    val weatherPulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "weatherPulse"
    )

    GlassmorphicCard(
        modifier = modifier
            .testTag("weather_widget")
            .clickable { onWidgetClick() },
        glowColor = glowColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Thành phố",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = cityName,
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = weatherLabel,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Độ ẩm: 65% • Khí áp: 1013hPa",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Weather dynamic 2D vector animation representation based on city
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .weight(0.8f),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.minDimension / 4
                    val center = Offset(size.width / 1.7f, size.height / 2.3f)

                    if (cityIndex % 2 == 0) { // Nắng ấm / rực rỡ (Hà Nội, Đà Nẵng)
                        // Sun drawing with glow rays
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFA726), Color(0xFFFF5722)),
                                center = center,
                                radius = radius
                            ),
                            radius = radius,
                            center = center
                        )

                        // Glowing sun rays using line draws with pulsing size
                        val rayCount = 8
                        for (i in 0 until rayCount) {
                            val angle = (i * (360f / rayCount)) + (weatherPulse * 45f)
                            val rad = Math.toRadians(angle.toDouble())
                            val lengthOffset = 4f * Math.sin(weatherPulse * Math.PI).toFloat()
                            val rayStartDist = radius + 6f
                            val rayEndDist = radius + 15f + lengthOffset

                            val startX = (center.x + Math.cos(rad) * rayStartDist).toFloat()
                            val startY = (center.y + Math.sin(rad) * rayStartDist).toFloat()
                            val endX = (center.x + Math.cos(rad) * rayEndDist).toFloat()
                            val endY = (center.y + Math.sin(rad) * rayEndDist).toFloat()

                            drawLine(
                                color = Color(0xFFFFB74D),
                                start = Offset(startX, startY),
                                end = Offset(endX, endY),
                                strokeWidth = 3f
                            )
                        }
                    } else { // Mưa rào / Nhiều mây (Sài Gòn, Hải Phòng)
                        // Cloud shapes
                        val cloudOffset = 6f * Math.sin(weatherPulse * 2 * Math.PI).toFloat()
                        val cloudLeft = center.x - 12f + cloudOffset
                        val cloudTop = center.y - 10f

                        // Draw Cloud base background
                        drawCircle(
                            color = Color(0xFF90A4AE),
                            radius = radius * 0.8f,
                            center = Offset(cloudLeft - 10f, cloudTop)
                        )
                        drawCircle(
                            color = Color(0xFFCFD8DC),
                            radius = radius,
                            center = Offset(cloudLeft, cloudTop - 6f)
                        )
                        drawCircle(
                            color = Color(0xFFCFD8DC),
                            radius = radius * 0.9f,
                            center = Offset(cloudLeft + 12f, cloudTop + 2f)
                        )

                        // Draw raindrops drawing
                        val dropCount = 4
                        for (i in 0 until dropCount) {
                            val xOffset = -15f + (i * 10f) + cloudOffset
                            val yStart = center.y + 12f + ((weatherPulse * 20f + i * 5f) % 20f)
                            val yEnd = yStart + 8f

                            drawLine(
                                color = Color(0xFF29B6F6),
                                start = Offset(cloudLeft + xOffset, yStart),
                                end = Offset(cloudLeft + xOffset - 3f, yEnd),
                                strokeWidth = 2.5f
                            )
                        }
                    }
                }

                // Small absolute text degrees inside box
                Text(
                    text = "${tempStr}°",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.BottomEnd)
                )
            }
        }
    }
}
