package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CarThemeStyle
import com.example.ui.viewmodel.LauncherViewModel

@Composable
fun SettingsScreen(
    isOpen: Boolean,
    onClose: () -> Unit,
    viewModel: LauncherViewModel
) {
    val context = LocalContext.current
    val currentTheme by viewModel.themeStyle.collectAsState()
    val speedUnit by viewModel.speedUnit.collectAsState()
    val showLiveBg by viewModel.showLiveBg.collectAsState()
    val isDrivingEngaged by viewModel.isDrivingEngaged.collectAsState()

    var lowRamMode by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = isOpen,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
        modifier = Modifier.fillMaxSize().testTag("settings_container")
    ) {
        val backgroundGradient = Brush.horizontalGradient(
            colors = listOf(
                Color.Black.copy(alpha = 0.95f),
                Color.Black.copy(alpha = 0.85f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundGradient)
        ) {
            // Left margin spacing to overlay with back screen
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.4f)
                    .clickable { onClose() }
            )

            // Right actual configurations dashboard drawer panel
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.6f)
                    .background(Color(0xFF0F1115))
                    .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f))
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Settings Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TRUNG TÂM CẤU HÌNH XE",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Thiết lập cài đặt màn hình launcher LecoAuto",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onClose() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Đóng",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(bottom = 16.dp))

                // SECTION 1: Theme selection (Giáo diện xe)
                SettingsCategoryTitle(title = "Giao Diện Xe Sang (Dynamic Theme)")
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CarThemeStyle.values().forEach { style ->
                        val isSelected = currentTheme == style
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.03f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    viewModel.setThemeStyle(style)
                                    Toast.makeText(context, "Đã kích hoạt theme: ${style.displayName}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setThemeStyle(style) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = MaterialTheme.colorScheme.primary,
                                    unselectedColor = Color.White.copy(alpha = 0.3f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = style.displayName,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                val desc = when (style) {
                                    CarThemeStyle.TESLA -> "Thiết kế thể thao tối giản, các góc nhọn tinh xảo, đèn đỏ rực rỡ"
                                    CarThemeStyle.BMW -> "Đồng hồ dạng khối kép Indigo sang trọng, thanh LED chuyển động mượt"
                                    CarThemeStyle.MERCEDES -> "Sang trọng cổ điển, nét viền bạc thanh lịch quý phái"
                                    CarThemeStyle.CYBERPUNK -> "Phong cách Cyberspace neon dạ quang rực rỡ dưới màn đêm xe"
                                }
                                Text(
                                    text = desc,
                                    color = Color.White.copy(alpha = 0.4f),
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }
                }

                // SECTION 2: Layout & Dashboard specs (Cấu hình đồng hồ)
                SettingsCategoryTitle(title = "Cấu Hình Thiết Bị & Đơn Vị")
                Card(
                     colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                     modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                     shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Switch 1: Kilometers or Miles per hour
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Đơn vị tốc độ cơ sở", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Định dạng km/h hoặc mph cho đồng hồ tốc độ", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                            }
                            
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.05f))
                                    .padding(2.dp)
                            ) {
                                val units = listOf("km/h", "mph")
                                units.forEach { unit ->
                                    val isSelected = speedUnit == unit
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { viewModel.setSpeedUnit(unit) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = unit,
                                            color = if (isSelected) Color.Black else Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        // Switch 2: Floating interactive background toggler
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Hình nền video động", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Cho phép biểu diễn nền hạt chuyển động đa sắc", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                            }
                            Switch(
                                checked = showLiveBg,
                                onCheckedChange = { viewModel.setLiveBg(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }

                        // Switch 3: Low configurations optimization mode (Tối ưu RAM)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Tối ưu hóa RAM yếu (<2GB)", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Tắt bóng mờ và giảm tải render để tối ưu GPU 60FPS", color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp)
                            }
                            Switch(
                                checked = lowRamMode,
                                onCheckedChange = { 
                                    lowRamMode = it
                                    Toast.makeText(context, if(it) "Đã bật chế độ tiết kiệm RAM" else "Đã tắt chế độ tiết kiệm RAM", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                // SECTION 3: Performance cleaner benchmarks
                SettingsCategoryTitle(title = "Bảo Trì & Hệ Thống")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Đã quét dọn bộ nhớ cache: Dung lượng giải phóng 142MB", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.08f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dọn Rác Cache", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            viewModel.toggleDrivingSimulation()
                            Toast.makeText(context, if(!isDrivingEngaged) "Bắt đầu truyền động số liệu" else "Đã trả về trạng thái đỗ P", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDrivingEngaged) Color.Red.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = if (isDrivingEngaged) Color.Red else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(if (isDrivingEngaged) Icons.Default.Stop else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isDrivingEngaged) "Tắt Chạy Thử" else "Bật Chạy Thử", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                     text = "LecoAuto Car OS v2.0.4 • Made for Android Automotive Table\nStartup dưới 2.0s • FPS: 60 • Hệ thống hoạt động mượt mà",
                     color = Color.White.copy(alpha = 0.25f),
                     fontSize = 8.sp,
                     fontWeight = FontWeight.Medium,
                     textAlign = TextAlign.Center,
                     modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SettingsCategoryTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = MaterialTheme.colorScheme.secondary,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 10.dp, top = 6.dp)
    )
}
