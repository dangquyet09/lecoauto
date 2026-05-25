package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppShortcut

@Composable
fun AppDock(
    modifier: Modifier = Modifier,
    shortcuts: List<AppShortcut>,
    onAppClick: (String) -> Unit, // packageName / type
    onOpenDrawer: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .testTag("app_dock"),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left block: Dedicated App Drawer Toggle
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                    )
                    .clickable { onOpenDrawer() }
                    .testTag("drawer_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Trình chọn ứng dụng",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Central block: Custom horizontal row of shortcuts loaded from Room database
            Row(
                modifier = Modifier.weight(1f).padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Limit to max 7 shortcuts for layout constraints
                shortcuts.take(6).forEach { shortcut ->
                    val vectorIcon = getVectorIconForName(shortcut.iconName)
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable {
                                onAppClick(shortcut.iconName)
                                Toast.makeText(context, "Mở ứng dụng: ${shortcut.appName}", Toast.LENGTH_SHORT).show()
                            }
                            .testTag("dock_shortcut_${shortcut.iconName}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vectorIcon,
                                contentDescription = shortcut.appName,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        Text(
                            text = shortcut.appName,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }

            // Right block: Dedicated Quick System settings configuration
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.05f))
                    .clickable { onOpenSettings() }
                    .testTag("settings_toggle_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Cài đặt launcher",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Map short identifier to material symbols
@Composable
fun getVectorIconForName(name: String): ImageVector {
    return when (name) {
        "map" -> Icons.Default.Map
        "music" -> Icons.Default.MusicNote
        "youtube" -> Icons.Default.VideoLibrary
        "camera" -> Icons.Default.CameraAlt
        "chrome" -> Icons.Default.Language
        "weather" -> Icons.Default.CloudQueue
        "settings" -> Icons.Default.Settings
        "tire_pressure" -> Icons.Default.Speed
        "obd" -> Icons.Default.Build
        "voice" -> Icons.Default.Mic
        else -> Icons.Default.Extension
    }
}
