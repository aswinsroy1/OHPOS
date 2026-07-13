package com.example.util

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.data.AutoBackupPreferencesRepository
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

object AutomaticBackupManager {
    suspend fun performAutomaticBackup(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val prefs = AutoBackupPreferencesRepository(context)
            val isEnabled = prefs.isAutoBackupEnabled.first()
            if (!isEnabled) return@withContext false

            val folderUriString = prefs.backupFolderUri.first()
            if (folderUriString == null) return@withContext false

            val folderUri = Uri.parse(folderUriString)
            val folder = DocumentFile.fromTreeUri(context, folderUri)
            
            if (folder == null || !folder.exists() || !folder.isDirectory || !folder.canWrite()) {
                return@withContext false
            }

            val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)
            val filename = "OH_POS_Backup_${dateFormat.format(Date())}"
            val mimeType = "application/zip"
            
            val newFile = folder.createFile(mimeType, filename) ?: return@withContext false
            val success = BackupRestoreManager.performBackup(context, newFile.uri)
            
            if (success) {
                prefs.setLastSuccessfulBackup(System.currentTimeMillis())
                
                // Delete old backups based on retention
                val retention = prefs.backupRetention.first()
                if (retention != -1) {
                    val files = folder.listFiles().filter { 
                        val name = it.name ?: ""
                        name.startsWith("OH_POS_Backup_") && name.endsWith(".zip") 
                    }
                    if (files.size > retention) {
                        val sortedFiles = files.sortedByDescending { it.lastModified() }
                        for (i in retention until sortedFiles.size) {
                            sortedFiles[i].delete()
                        }
                    }
                }
                return@withContext true
            }
            return@withContext false
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}
