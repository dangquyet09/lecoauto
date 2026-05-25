package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.LauncherHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LauncherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Full Edge-to-Edge immersion for automotive dashboards
        enableEdgeToEdge()
        
        setContent {
            val viewModel: LauncherViewModel = viewModel()
            val activeTheme by viewModel.themeStyle.collectAsState()

            MyApplicationTheme(themeStyle = activeTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    LauncherHomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
