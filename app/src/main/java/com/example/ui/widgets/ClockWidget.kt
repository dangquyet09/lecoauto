package com.example.ui.widgets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassmorphicCard
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ClockWidget(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary
) {
    var timeString by remember { mutableStateOf("08:00:33") }
    var amPmString by remember { mutableStateOf("CH") }
    var dateString by remember { mutableStateOf("Thứ Hai, 25 tháng 05, 2026") }

    LaunchedEffect(Unit) {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val amPmFormat = SimpleDateFormat("a", Locale("vi", "VN"))
        val dateFormat = SimpleDateFormat("EEEE, 'ngày' dd 'tháng' MM, yyyy", Locale("vi", "VN"))

        while (true) {
            val now = Calendar.getInstance().time
            timeString = timeFormat.format(now)
            amPmString = amPmFormat.format(now).uppercase()
            dateString = dateFormat.format(now)
            delay(1000)
        }
    }

    GlassmorphicCard(
        modifier = modifier.testTag("clock_widget"),
        glowColor = glowColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = timeString,
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 2.sp,
                    modifier = Modifier.testTag("clock_time_text")
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = amPmString,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Lịch",
                    tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                
                Spacer(modifier = Modifier.width(6.dp))
                
                Text(
                    text = dateString,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "HÀNH TRÌNH AN TOÀN • LECOAUTO",
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
        }
    }
}
