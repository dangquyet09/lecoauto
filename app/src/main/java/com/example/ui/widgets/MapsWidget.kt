package com.example.ui.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard

@Composable
fun MapsWidget(
    modifier: Modifier = Modifier,
    speed: Float = 0f,
    onOpenPip: () -> Unit,
    glowColor: Color = MaterialTheme.colorScheme.primary
) {
    // Alternate simulated directions
    var currentInstruction by remember { mutableStateOf("Đi thẳng theo Phố Huế") }
    var distanceLeft by remember { mutableStateOf("450m") }
    var destinationName by remember { mutableStateOf("Bờ Hồ Hoàn Kiếm") }

    LaunchedEffect(speed) {
        if (speed > 80f) {
            currentInstruction = "Chuẩn bị rẽ trái vào Lê Thái Tổ"
            distanceLeft = "150m"
        } else if (speed > 40f) {
            currentInstruction = "Đi thẳng theo Hàng Khay"
            distanceLeft = "300m"
        } else if (speed > 5f) {
            currentInstruction = "Đi thẳng theo Phố Huế"
            distanceLeft = "550m"
        } else {
            currentInstruction = "Bắt đầu hành trình di chuyển"
            distanceLeft = "Sẵn sàng"
        }
    }

    // infinite rotation for map mesh grid layout
    val infiniteTransition = rememberInfiniteTransition(label = "maps")
    val gridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridOffset"
    )

    GlassmorphicCard(
        modifier = modifier,
        glowColor = glowColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
        ) {
            // Left side: Direction & Text Info
            Column(
                modifier = Modifier
                    .weight(1.3f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.Start
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rotation navigation pointer based on instruction
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Dẫn đường",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Column {
                            Text(
                                text = currentInstruction,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Còn $distanceLeft • Đến $destinationName",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GPS Coordinate & Speed overlay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "21.0285° N, 105.8542° E",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 8.sp
                        )
                        Text(
                            text = "Google Maps mini v1.4",
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onOpenPip,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Phóng To PiP", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right side: stylized scrolling vector mesh grid representing maps route!
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw localized grid mesh lines
                    val verticalLines = (size.width / 20f).toInt()
                    val horizontalLines = (size.height / 20f).toInt()

                    for (i in 0..verticalLines) {
                        val posX = (i * 20f) + (gridOffset % 20f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(posX, 0f),
                            end = Offset(posX, size.height),
                            strokeWidth = 1f
                        )
                    }
                    for (i in 0..horizontalLines) {
                        val posY = (i * 20f) + (gridOffset % 20f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.04f),
                            start = Offset(0f, posY),
                            end = Offset(size.width, posY),
                            strokeWidth = 1f
                        )
                    }

                    // Draw stylized route path curve (neon glowing line)
                    val rPath = Path().apply {
                        moveTo(size.width / 2f, size.height)
                        quadraticTo(
                            size.width / 4f, size.height * 0.6f,
                            size.width / 2f, size.height * 0.3f
                        )
                        cubicTo(
                            size.width * 0.8f, size.height * 0.1f,
                            size.width / 3f, size.height * 0.1f,
                            size.width / 4f, 0f
                        )
                    }

                    drawPath(
                        path = rPath,
                        color = Color.White.copy(alpha = 0.2f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                    )

                    drawPath(
                        path = rPath,
                        color = Color(0xFF00E6FF),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                    )

                    // Draw our moving target car point
                    val carY = size.height * 0.3f
                    val carX = size.width / 2f
                    drawCircle(
                        color = Color(0xFF00E6FF).copy(alpha = 0.3f),
                        radius = 12f,
                        center = Offset(carX, carY)
                    )
                    drawCircle(
                        color = Color(0xFF00E6FF),
                        radius = 6f,
                        center = Offset(carX, carY)
                    )
                }
            }
        }
    }
}
