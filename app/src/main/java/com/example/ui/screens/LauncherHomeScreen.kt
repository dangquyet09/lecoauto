package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.theme.CarThemeStyle
import com.example.ui.viewmodel.LauncherPipType
import com.example.ui.viewmodel.LauncherViewModel
import com.example.ui.widgets.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun LauncherHomeScreen(
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current

    // Observe central configurations
    val activeTheme by viewModel.themeStyle.collectAsState()
    val speedUnit by viewModel.speedUnit.collectAsState()
    val showLiveBg by viewModel.showLiveBg.collectAsState()

    // Observe engine status telemetry
    val speed by viewModel.speed.collectAsState()
    val rpm by viewModel.rpm.collectAsState()
    val fuelLevel by viewModel.fuelLevel.collectAsState()
    val engineTemp by viewModel.engineTemp.collectAsState()
    val isDrivingEngaged by viewModel.isDrivingEngaged.collectAsState()
    val gpsSatellites by viewModel.gpsSatellites.collectAsState()

    // Observe Music states
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val progress by viewModel.songProgressSeconds.collectAsState()
    val currentLyric by viewModel.currentLyricLine.collectAsState()
    val audioWaveform by viewModel.audioWaveform.collectAsState()

    // Observe Weather
    val cityIndex by viewModel.selectedCityIndex.collectAsState()
    val weatherText by viewModel.weatherState.collectAsState()

    val shortcuts by viewModel.shortcutsList.collectAsState(initial = emptyList())

    // Internal drawers overlays states
    var isAppDrawerOpen by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }

    // Live background infinite color waves timer
    val infiniteTransition = rememberInfiniteTransition(label = "live_wallpaper")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Design layout colors dynamically aligning with selection
    val baseThemeGlow = when (activeTheme) {
        CarThemeStyle.TESLA -> Color(0xFFFF2D37).copy(alpha = 0.25f)
        CarThemeStyle.BMW -> Color(0xFF00D1FF).copy(alpha = 0.25f)
        CarThemeStyle.MERCEDES -> Color(0xFF00F0FF).copy(alpha = 0.2f)
        CarThemeStyle.CYBERPUNK -> Color(0xFFFF007F).copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                // Outer background canvas painting
                if (showLiveBg) {
                    // Draw futuristic deep automotive wallpaper with live ambient vectors waves
                    val wRadius = size.minDimension / 1.5f
                    val sinOffset = 100f * sin(waveOffset).toFloat()

                    val center1 = Offset(size.width * 0.15f + sinOffset, size.height * 0.2f)
                    val center2 = Offset(size.width * 0.85f - sinOffset, size.height * 0.8f)

                    val color1 = when (activeTheme) {
                        CarThemeStyle.TESLA -> Color(0x3E230505)
                        CarThemeStyle.BMW -> Color(0x2E021330)
                        CarThemeStyle.MERCEDES -> Color(0x1F2B1D3D)
                        CarThemeStyle.CYBERPUNK -> Color(0x442C003E)
                    }
                    val color2 = when (activeTheme) {
                        CarThemeStyle.TESLA -> Color(0x11020202)
                        CarThemeStyle.BMW -> Color(0x1F0B121E)
                        CarThemeStyle.MERCEDES -> Color(0x1F0F161A)
                        CarThemeStyle.CYBERPUNK -> Color(0x44002B49)
                    }

                    drawRect(color = Color(0xFF030509))

                    drawCircle(
                        brush = Brush.radialGradient(colors = listOf(color1, Color.Transparent), center = center1, radius = wRadius),
                        radius = wRadius,
                        center = center1
                    )
                    drawCircle(
                        brush = Brush.radialGradient(colors = listOf(color2, Color.Transparent), center = center2, radius = wRadius),
                        radius = wRadius,
                        center = center2
                    )

                    // Subtle decorative alignment grids
                    val gridGap = 80f
                    for (x in 0..(size.width / gridGap).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.015f),
                            start = Offset(x * gridGap, 0f),
                            end = Offset(x * gridGap, size.height)
                        )
                    }
                    for (y in 0..(size.height / gridGap).toInt()) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.015f),
                            start = Offset(0f, y * gridGap),
                            end = Offset(size.width, y * gridGap)
                        )
                    }
                } else {
                    // Flat solid carbon back
                    drawRect(color = Color(0xFF0A0C10))
                }
            }
            .systemBarsPadding()
            .padding(10.dp)
            .testTag("launcher_root_view")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ROW 1: Complete widgets grid layout
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // COLUMN A: Center gauge speedometer dashboard
                DashboardWidget(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    themeStyle = activeTheme,
                    speed = speed,
                    rpm = rpm,
                    fuelLevel = fuelLevel,
                    engineTemp = engineTemp,
                    isDrivingEngaged = isDrivingEngaged,
                    gpsSatellites = gpsSatellites,
                    onToggleDriving = { viewModel.toggleDrivingSimulation() },
                    glowColor = baseThemeGlow
                )

                // COLUMN B: Central double vertically stacked widgets (Clock, Weather, and mini Maps)
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.9f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Clock widget
                        ClockWidget(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            glowColor = baseThemeGlow
                        )

                        // Weather Widget
                        WeatherWidget(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            cityIndex = cityIndex,
                            weatherText = weatherText,
                            onWidgetClick = { viewModel.changeCity() },
                            glowColor = baseThemeGlow
                        )
                    }

                    // Map Mini Navigation directions widget
                    MapsWidget(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.1f),
                        speed = speed,
                        onOpenPip = { viewModel.openPipWindow(LauncherPipType.MAPS) },
                        glowColor = baseThemeGlow
                    )
                }

                // COLUMN C: Interactive music player controls widget on the right
                MusicWidget(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    song = currentSong,
                    isPlaying = isPlaying,
                    progress = progress,
                    currentLyric = currentLyric,
                    visualizerData = audioWaveform,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onPrevClick = { viewModel.prevSong() },
                    onNextClick = { viewModel.nextSong() },
                    onScrub = { viewModel.seekProgress(it) },
                    glowColor = baseThemeGlow
                )
            }

            // ROW 2: Multi application quick-shelf system Dock bottom!
            AppDock(
                shortcuts = shortcuts,
                onAppClick = { appType ->
                    val pipType = when (appType) {
                        "map" -> LauncherPipType.MAPS
                        "music" -> LauncherPipType.MUSIC
                        "youtube" -> LauncherPipType.YOUTUBE
                        "camera" -> LauncherPipType.REAR_CAMERA
                        else -> null
                    }
                    if (pipType != null) {
                        viewModel.openPipWindow(pipType)
                    }
                },
                onOpenDrawer = { isAppDrawerOpen = true },
                onOpenSettings = { isSettingsOpen = true }
            )
        }

        // FLOATING SYSTEM: Picture In Picture (PiP) Window renderer layer
        PiPSystemRenderer(viewModel = viewModel)

        // DRAWER OVERLAY: Full client drawer app selector
        AppDrawer(
            isOpen = isAppDrawerOpen,
            onClose = { isAppDrawerOpen = false },
            viewModel = viewModel,
            shortcuts = shortcuts
        )

        // CONFIGURATION BOARD OVERLAY: Slide configurations settings screen
        SettingsScreen(
            isOpen = isSettingsOpen,
            onClose = { isSettingsOpen = false },
            viewModel = viewModel
        )
    }
}
