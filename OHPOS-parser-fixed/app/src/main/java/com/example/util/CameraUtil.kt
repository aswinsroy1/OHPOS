package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object CameraUtil {
    fun createImageUri(context: Context): Uri {
        val file = File(context.cacheDir, "menu_scan_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
