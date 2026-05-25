package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.FloatingWindow
import com.example.ui.viewmodel.LauncherPipType
import com.example.ui.viewmodel.LauncherViewModel
import kotlin.math.roundToInt

@Composable
fun PiPSystemRenderer(
    viewModel: LauncherViewModel
) {
    // Collect list of floating windows
    val windows = viewModel.floatingWindows

    // We paint current open floating windows in order (last element on top)
    windows.forEach { win ->
        FloatingPipWindow(
            window = win,
            onClose = { viewModel.closePipWindow(win.type) },
            onMinimize = { viewModel.minimizePipWindow(win.type) },
            onMaximize = { viewModel.toggleMaximizePip(win.type) },
            onDrag = { dx, dy ->
                val newX = win.x + dx
                val newY = win.y + dy
                // Snapping engine: if within 40 pixels of screen borders, snap!
                val snappedX = when {
                    newX < 40f -> 10f // Snap to Left
                    newX > 850f -> 850f // Snap to Right
                    else -> newX
                }
                val snappedY = when {
                    newY < 40f -> 10f // Snap to Top
                    newY > 400f -> 400f // Snap to Bottom
                    else -> newY
                }
                viewModel.updatePipPosition(win.type, snappedX, snappedY)
            },
            onResize = { dw, dh ->
                viewModel.resizePipWindow(win.type, dw, dh)
            }
        ) {
            // Render actual unique content based on PipType
            when (win.type) {
                LauncherPipType.MAPS -> MapMiniPipContent()
                LauncherPipType.YOUTUBE -> YoutubeMiniPipContent()
                LauncherPipType.REAR_CAMERA -> CameraMiniPipContent()
                LauncherPipType.MUSIC -> MusicMiniPipContent(viewModel)
            }
        }
    }
}

@Composable
fun FloatingPipWindow(
    window: FloatingWindow,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onMaximize: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .offset { IntOffset(window.x.roundToInt(), window.y.roundToInt()) }
            .size(window.width.dp, window.height.dp)
            .shadow(24.dp, RoundedCornerShape(20.dp), clip = false)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE01E222B))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        Color.White.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("floating_pip_${window.type.name}")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // WINDOW TITLE HEADER: Draggable bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp)
                    .background(Color.White.copy(alpha = 0.05f))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDrag(dragAmount.x, dragAmount.y)
                        }
                    }
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Title and icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getVectorIconForName(window.type.systemIcon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = window.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Window action controls
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Minimize icon button
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFA726))
                            .clickable { onMinimize() }
                    )

                    // Maximize icon button
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF29B6F6))
                            .clickable { onMaximize() }
                    )

                    // Close icon button
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF5350))
                            .clickable { onClose() }
                    )
                }
            }

            // WINDOW INNER CONTENT CANVAS
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black.copy(alpha = 0.3f)),
                content = content
            )
        }

        // BOTTOM-RIGHT RESIZE GRIP HANDLE
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onResize(dragAmount.x, dragAmount.y)
                    }
                },
            contentAlignment = Alignment.BottomEnd
        ) {
            Canvas(modifier = Modifier.size(12.dp)) {
                // Draw three diagonal grip ribs
                drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(size.width, 0f), end = Offset(0f, size.height), strokeWidth = 2f)
                drawLine(color = Color.White.copy(alpha = 0.4f), start = Offset(size.width, size.height / 2f), end = Offset(size.width / 2f, size.height), strokeWidth = 2f)
            }
        }
    }
}

// ------------------------------------------------------------------------
// Cửa sổ 1: Maps Mini view simulation
// ------------------------------------------------------------------------
@Composable
fun MapMiniPipContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF1E212B))
            // Simulated roads
            drawLine(color = Color.White.copy(alpha = 0.08f), start = Offset(size.width / 2, 0f), end = Offset(size.width / 2, size.height), strokeWidth = 30f)
            drawLine(color = Color.White.copy(alpha = 0.08f), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 30f)
            // Pulse route rẽ
            drawLine(color = Color(0xFF00E5FF), start = Offset(size.width / 2, size.height), end = Offset(size.width / 2, size.height / 2), strokeWidth = 10f)
            drawLine(color = Color(0xFF00E5FF), start = Offset(size.width / 2, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 10f)
            // Target arrow pointer
            drawCircle(color = Color(0xFFFF0055), radius = 10f, center = Offset(size.width / 2, size.height / 2))
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.62f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                     text = "HÀ NỘI MAPS • Rẽ phải Lê Hồng Phong 180m nữa",
                     color = Color(0xFF00E5FF),
                     fontSize = 10.sp,
                     fontWeight = FontWeight.Bold,
                     modifier = Modifier.padding(6.dp)
                )
            }
        }
    }
}

// ------------------------------------------------------------------------
// Cửa sổ 2: YouTube Mini simulation
// ------------------------------------------------------------------------
@Composable
fun YoutubeMiniPipContent() {
    var playStatus by remember { mutableStateOf(true) }
    var frameTicker by remember { mutableStateOf(0) }

    LaunchedEffect(playStatus) {
        if (playStatus) {
            while (true) {
                delay(120)
                frameTicker = (frameTicker + 1) % 360
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Draw visual simulated video feed
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rad = Math.toRadians(frameTicker.toDouble())
            val centerX = size.width / 2f + (50f * Math.cos(rad)).toFloat()
            val centerY = size.height / 2f + (30f * Math.sin(rad * 1.5)).toFloat()

            // Dynamic video particles drawing
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xBBFF0055), Color(0x33000000)),
                    center = Offset(centerX, centerY),
                    radius = size.minDimension / 2.3f
                ),
                radius = size.minDimension / 1.5f,
                center = Offset(centerX, centerY)
            )

            // Draw a spinning dashboard ring just for animation entertainment
            drawCircle(
                color = Color(0xFFFFA726).copy(alpha = 0.3f),
                radius = 40f + (15f * Math.sin(rad)).toFloat(),
                center = Offset(size.width / 2, size.height / 2),
                style = Stroke(width = 4f)
            )
        }

        // Overlay status overlay
        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "VIDEO LIVE: Đánh giá siêu xe VinFast VF9 sành điệu",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { playStatus = !playStatus },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (playStatus) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color.Red))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("LIVE FEED", color = Color.White.copy(alpha = 0.8f), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ------------------------------------------------------------------------
// Cửa sổ 3: Dashcam back camera simulator
// ------------------------------------------------------------------------
@Composable
fun CameraMiniPipContent() {
    var cameraNoiseTick by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(150)
            cameraNoiseTick = (cameraNoiseTick + 1) % 10
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color(0xFF1E262B))
            // Grid guides and horizon alignment lines
            drawLine(color = Color(0x3300FF33), start = Offset(0f, size.height / 2), end = Offset(size.width, size.height / 2), strokeWidth = 1.5f)
            
            // Reversing distance grid (red, yellow, green)
            val pathGreen = Path().apply {
                moveTo(size.width * 0.15f, size.height)
                lineTo(size.width * 0.35f, size.height * 0.5f)
                lineTo(size.width * 0.65f, size.height * 0.5f)
                lineTo(size.width * 0.85f, size.height)
            }
            drawPath(pathGreen, color = Color(0x8833FF33), style = Stroke(width = 3f))

            // Horizon warning bar
            drawLine(color = Color(0xAAFF0055), start = Offset(size.width * 0.35f, size.height * 0.5f), end = Offset(size.width * 0.65f, size.height * 0.5f), strokeWidth = 5f)
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("CAMERA LÙI FULL HD", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text("REC 🔴", color = Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                text = "CHÚ Ý QUAN SÁT CHƯỚNG NGẠI VẬT PHÍA SAU XE",
                color = Color.Yellow,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------------
// Cửa sổ 4: Music mini controllers
// ------------------------------------------------------------------------
@Composable
fun MusicMiniPipContent(viewModel: LauncherViewModel) {
    val song by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.songProgressSeconds.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(song.title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(song.artist, color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, maxLines = 1)
        }

        LinearProgressIndicator(
            progress = progress.toFloat() / song.durationSeconds.coerceAtLeast(1),
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth().height(3.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            IconButton(onClick = { viewModel.prevSong() }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { viewModel.togglePlayPause() }, modifier = Modifier.size(30.dp)) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            IconButton(onClick = { viewModel.nextSong() }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}
