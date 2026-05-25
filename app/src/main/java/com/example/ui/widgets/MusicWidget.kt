package com.example.ui.widgets

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import com.example.ui.viewmodel.SongInfo

@Composable
fun MusicWidget(
    modifier: Modifier = Modifier,
    song: SongInfo,
    isPlaying: Boolean,
    progress: Int,
    currentLyric: String,
    visualizerData: FloatArray,
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onScrub: (Int) -> Unit,
    glowColor: Color = MaterialTheme.colorScheme.primary
) {
    val durationFormatted = formatTime(song.durationSeconds)
    val progressFormatted = formatTime(progress)
    val percentage = progress.toFloat() / song.durationSeconds.coerceAtLeast(1)

    GlassmorphicCard(
        modifier = modifier.testTag("music_widget"),
        glowColor = glowColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // First Row: Album design & Track name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vinyl CD rotating animation or album display
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Simulating vinyl circles
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = Color.Black.copy(alpha = 0.3f), radius = size.minDimension / 2.5f)
                        drawCircle(color = Color.Black, radius = size.minDimension / 6f)
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = size.minDimension / 15f)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = song.title,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = "Phát từ ${song.source}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Waveform bars inside first row right side
                val activeBarColor = MaterialTheme.colorScheme.primary
                Canvas(
                    modifier = Modifier
                        .width(70.dp)
                        .height(35.dp)
                ) {
                    val barWidth = 3f
                    val gap = 2f
                    val barCount = visualizerData.size.coerceAtMost(14)
                    val blockWidth = barWidth + gap

                    for (i in 0 until barCount) {
                        val coeff = visualizerData.getOrElse(i) { 0.1f }
                        val h = size.height * coeff
                        val x = i * blockWidth + (size.width - (barCount * blockWidth)) / 2f
                        val y = (size.height - h) / 2f

                        drawRoundRect(
                            color = if (isPlaying) activeBarColor else Color.White.copy(alpha = 0.3f),
                            topLeft = Offset(x, y),
                            size = Size(barWidth, h),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }

            // Real-time scrolling/highlighted Lyrics row (Tiếng Việt)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currentLyric,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp)
                )
            }

            // Scrubber Seek Player
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Seekbar liner click track
                Slider(
                    value = percentage,
                    onValueChange = { newVal ->
                        val targetSeconds = (newVal * song.durationSeconds).toInt()
                        onScrub(targetSeconds)
                    },
                    colors = SliderDefaults.colors(
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                        thumbColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .height(18.dp)
                        .fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressFormatted,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                    Text(
                        text = durationFormatted,
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp
                    )
                }
            }

            // Playback Actions control
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPrevClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("prev_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastRewind,
                        contentDescription = "Bài trước",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(36.dp))

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { onPlayPauseClick() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Tạm dừng" else "Phát nhạc",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(36.dp))

                IconButton(
                    onClick = onNextClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("next_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FastForward,
                        contentDescription = "Bài sau",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
