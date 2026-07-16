package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.AppDesignSystem

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : androidx.fragment.app.FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        com.example.util.PrinterStatusMonitor.startMonitoring(
            applicationContext, 
            com.example.data.AppDatabase.getDatabase(applicationContext)
        )
        
        enableEdgeToEdge()
        setContent {
            AppDesignSystem {
                MainScreen()
            }
        }
    }
}
