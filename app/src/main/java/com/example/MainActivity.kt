package com.example

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import com.example.ui.screens.MainScreen
import com.example.ui.theme.AppDesignSystem

@androidx.compose.material3.ExperimentalMaterial3Api
class MainActivity : androidx.fragment.app.FragmentActivity() {

    companion object {
        const val EXTRA_NOTIFICATION_TARGET = "extra_notification_target"
    }

    private val notificationTarget = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        com.example.util.PrinterStatusMonitor.startMonitoring(
            applicationContext, 
            com.example.data.AppDatabase.getDatabase(applicationContext)
        )
        
        com.example.util.AppNotificationManager.createChannels(applicationContext)
        
        intent?.getStringExtra(EXTRA_NOTIFICATION_TARGET)?.let {
            notificationTarget.value = it
        }
        
        enableEdgeToEdge()
        setContent {
            AppDesignSystem {
                MainScreen(
                    notificationTarget = notificationTarget.value,
                    onNotificationTargetConsumed = { notificationTarget.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(EXTRA_NOTIFICATION_TARGET)?.let {
            notificationTarget.value = it
        }
    }
}
