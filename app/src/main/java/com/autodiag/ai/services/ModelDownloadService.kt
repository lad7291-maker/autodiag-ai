package com.autodiag.ai.services

import android.app.DownloadManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.os.IBinder
import com.autodiag.ai.utils.FirebaseLogger
import java.io.File

/**
 * Сервис для загрузки ML моделей
 * Использует DownloadManager для фоновой загрузки
 */
class ModelDownloadService : Service() {

    private lateinit var downloadManager: DownloadManager
    private var downloadId: Long = -1
    
    private val downloadReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (id == downloadId) {
                handleDownloadComplete(id)
            }
        }
    }

    companion object {
        const val ACTION_DOWNLOAD_MODEL = "ACTION_DOWNLOAD_MODEL"
        const val EXTRA_MODEL_URL = "EXTRA_MODEL_URL"
        const val EXTRA_MODEL_NAME = "EXTRA_MODEL_NAME"
        
        // Локальные пути для моделей
        const val MODELS_DIR = "ml-models"
        const val DIAGNOSIS_MODEL = "diagnosis_model.tflite"
    }

    override fun onCreate() {
        super.onCreate()
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        registerReceiver(downloadReceiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DOWNLOAD_MODEL -> {
                val url = intent.getStringExtra(EXTRA_MODEL_URL)
                val name = intent.getStringExtra(EXTRA_MODEL_NAME) ?: DIAGNOSIS_MODEL
                if (url != null) {
                    downloadModel(url, name)
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun downloadModel(url: String, fileName: String) {
        // Проверяем, есть ли уже модель
        val modelsDir = File(filesDir, MODELS_DIR)
        if (!modelsDir.exists()) {
            modelsDir.mkdirs()
        }
        
        val modelFile = File(modelsDir, fileName)
        if (modelFile.exists()) {
            FirebaseLogger.logEvent("model_already_exists", mapOf("model" to fileName))
            stopSelf()
            return
        }

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Загрузка AI модели")
            .setDescription("$fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(modelFile))
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
        FirebaseLogger.logEvent("model_download_started", mapOf("model" to fileName))
    }

    private fun handleDownloadComplete(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        
        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            val localUri = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
            
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    FirebaseLogger.logEvent("model_download_success", mapOf("path" to (localUri ?: "")))
                }
                DownloadManager.STATUS_FAILED -> {
                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    FirebaseLogger.logException(Exception("Model download failed: $reason"))
                }
            }
        }
        cursor.close()
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(downloadReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver not registered
        }
    }
    
    /**
     * Проверить, существует ли локальная модель
     */
    fun isModelAvailable(modelName: String = DIAGNOSIS_MODEL): Boolean {
        val modelFile = File(File(filesDir, MODELS_DIR), modelName)
        return modelFile.exists() && modelFile.length() > 0
    }
    
    /**
     * Получить путь к локальной модели
     */
    fun getModelPath(modelName: String = DIAGNOSIS_MODEL): String? {
        val modelFile = File(File(filesDir, MODELS_DIR), modelName)
        return if (modelFile.exists()) modelFile.absolutePath else null
    }
}
