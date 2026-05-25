package com.example.ui.widgets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SettingsInputAntenna
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.CarThemeStyle

@Composable
fun DashboardWidget(
    modifier: Modifier = Modifier,
    themeStyle: CarThemeStyle,
    speed: Float,
    rpm: Float,
    fuelLevel: Int,
    engineTemp: Int,
    isDrivingEngaged: Boolean,
    gpsSatellites: Int,
    onToggleDriving: () -> Unit,
    glowColor: Color = MaterialTheme.colorScheme.primary
) {
    // Smoothen needle rotations & bar slides
    val smoothedSpeed by animateFloatAsState(
        targetValue = speed,
        animationSpec = tween(150),
        label = "speed"
    )

    val smoothedRpm by animateFloatAsState(
        targetValue = rpm,
        animationSpec = tween(150),
        label = "rpm"
    )

    GlassmorphicCard(
        modifier = modifier.testTag("dashboard_widget"),
        glowColor = glowColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT PANEL: Gauge Canvas (3D needle sweep speedometer)
            Box(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .padding(8.dp)
                ) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Draw outer styling rings based on active style
                    val rimColor = when (themeStyle) {
                        CarThemeStyle.TESLA -> Color(0xFFE82127).copy(alpha = 0.3f)
                        CarThemeStyle.BMW -> Color(0xFF00D1FF).copy(alpha = 0.3f)
                        CarThemeStyle.MERCEDES -> Color(0x3300F0FF)
                        CarThemeStyle.CYBERPUNK -> Color(0xFFFCEE09).copy(alpha = 0.3f)
                    }

                    drawCircle(
                        color = rimColor,
                        radius = radius,
                        center = center,
                        style = Stroke(width = 4f)
                    )

                    // Tick segments (Arc from 135 to 405 degrees)
                    val startAngle = 135f
                    val sweepAngle = 270f
                    val markerCount = 10

                    for (i in 0..markerCount) {
                        val angle = startAngle + (i * (sweepAngle / markerCount))
                        val rad = Math.toRadians(angle.toDouble())
                        val innerRad = radius - 15f
                        val outerRad = radius - 5f

                        val startX = (center.x + Math.cos(rad) * innerRad).toFloat()
                        val startY = (center.y + Math.sin(rad) * innerRad).toFloat()
                        val endX = (center.x + Math.cos(rad) * outerRad).toFloat()
                        val endY = (center.y + Math.sin(rad) * outerRad).toFloat()

                        drawLine(
                            color = if (smoothedSpeed > (i * 22f)) rimColor.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.15f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )
                    }

                    // Active sweep arc overlay
                    val speedPercentage = (smoothedSpeed / 220f).coerceIn(0f, 1f)
                    val activeSweep = sweepAngle * speedPercentage
                    
                    drawArc(
                        color = when (themeStyle) {
                            CarThemeStyle.TESLA -> Color(0xFFE82127)
                            CarThemeStyle.BMW -> Color(0xFF00D1FF)
                            CarThemeStyle.MERCEDES -> Color(0xAA00F0FF)
                            CarThemeStyle.CYBERPUNK -> Color(0xFFFCEE09)
                        },
                        startAngle = startAngle,
                        sweepAngle = activeSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 6f, cap = StrokeCap.Round)
                    )

                    // Gauge Needle Pointer
                    val needleAngle = startAngle + activeSweep
                    val needleRad = Math.toRadians(needleAngle.toDouble())
                    val needleLength = radius - 25f
                    val needleEndX = (center.x + Math.cos(needleRad) * needleLength).toFloat()
                    val needleEndY = (center.y + Math.sin(needleRad) * needleLength).toFloat()

                    drawLine(
                        color = when (themeStyle) {
                            CarThemeStyle.TESLA -> Color(0xFFFF2D37)
                            CarThemeStyle.BMW -> Color(0xFFFF0055)
                            CarThemeStyle.MERCEDES -> Color(0xEEFFFFFF)
                            CarThemeStyle.CYBERPUNK -> Color(0xFFFF007F)
                        },
                        start = center,
                        end = Offset(needleEndX, needleEndY),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )

                    // Core center knob
                    drawCircle(color = Color.Black, radius = 12f, center = center)
                    drawCircle(color = Color.White, radius = 5f, center = center)
                }

                // Speed Text overlay inside gauge central point
                Column(
                    modifier = Modifier.padding(top = 90.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = smoothedSpeed.toInt().toString(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "KM/H",
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // RIGHT PANEL: Engine statistics & Cyber slider RPM indicator
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                // RPM gauge slider representation (Cyber / Audi hybrid visual style)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "VÒNG TUA (RPM)",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${smoothedRpm.toInt()}",
                            color = when (themeStyle) {
                                CarThemeStyle.CYBERPUNK -> Color(0xFF00FDFD)
                                CarThemeStyle.BMW -> Color(0xFF00D1FF)
                                else -> MaterialTheme.colorScheme.primary
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(6.dp))

                    // Draw segmented linear spectrum for RPM
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(4.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val segments = 24
                            val gap = 4f
                            val blockWidth = (size.width - (segments - 1) * gap) / segments
                            val activeBlocks = (smoothedRpm / 8000f * segments).toInt().coerceIn(0, segments)

                            for (i in 0 until segments) {
                                val x = i * (blockWidth + gap)
                                val col = when {
                                    i > 20 -> Color(0xFFFF0055) // Redline
                                    i > 15 -> Color(0xFFFFA726) // High rpm yellow
                                    else -> when (themeStyle) {
                                        CarThemeStyle.TESLA -> Color(0xFFE82127)
                                        CarThemeStyle.BMW -> Color(0xFF00D1FF)
                                        CarThemeStyle.MERCEDES -> Color(0xFF00F0FF)
                                        CarThemeStyle.CYBERPUNK -> Color(0xFFFCEE09)
                                    }
                                }

                                drawRoundRect(
                                    color = if (i < activeBlocks) col else Color.White.copy(alpha = 0.15f),
                                    topLeft = Offset(x, 0f),
                                    size = Size(blockWidth, size.height),
                                    cornerRadius = CornerRadius(2f, 2f)
                                )
                            }
                        }
                    }
                }

                // Grid detail sensors (Fuel, Temp, GPS Signal satellites count)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fuel metrics
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = "Mức xăng",
                            tint = if (fuelLevel < 20) Color.Red else Color.Green,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Nhiên liệu", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                            Text("$fuelLevel%", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Engine Temp
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Thermostat,
                            contentDescription = "Nhiệt độ động cơ",
                            tint = if (engineTemp > 105) Color.Red else Color.Cyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Nhiệt độ", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                            Text("$engineTemp°C", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // GPS Signal satellites counts
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SettingsInputAntenna,
                            contentDescription = "Tín hiệu GPS",
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text("Vệ tinh GPS", color = Color.White.copy(alpha = 0.4f), fontSize = 8.sp)
                            Text("$gpsSatellites hạt", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Gear indicators (P R N D) with Simulation control switch!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gear selectors standard look
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val currentGear = if (isDrivingEngaged) "D" else "P"
                        listOf("P", "R", "N", "D").forEach { gear ->
                            val isActive = currentGear == gear
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = gear,
                                    color = if (isActive) Color.Black else Color.White.copy(alpha = 0.4f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Simulation Play Trigger Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDrivingEngaged) Color.Red.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            .clickable { onToggleDriving() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDrivingEngaged) Icons.Default.Stop else Icons.Default.PlayArrow,
                                contentDescription = "Truyền động",
                                tint = if (isDrivingEngaged) Color.Red else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isDrivingEngaged) "Dừng xe" else "Chạy thử",
                                color = if (isDrivingEngaged) Color.Red else MaterialTheme.colorScheme.primary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
