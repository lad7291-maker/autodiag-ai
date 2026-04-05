package com.autodiag.ai.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.autodiag.ai.MainActivity
import com.autodiag.ai.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Фоновый сервис для мониторинга OBD2 параметров
 * Работает в foreground mode для стабильного подключения
 */
class ObdMonitoringService : Service() {

    private val obdManager: ObdConnectionManager by inject()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        const val CHANNEL_ID = "obd_monitoring_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_START = "ACTION_START_MONITORING"
        const val ACTION_STOP = "ACTION_STOP_MONITORING"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        val notification = createNotification("Мониторинг OBD2 активен", "Сбор данных...")
        startForeground(NOTIFICATION_ID, notification)

        serviceScope.launch {
            obdManager.startMonitoring().collectLatest { params ->
                // Обновляем уведомление с текущими параметрами
                val updateText = buildString {
                    params.rpm?.let { append("Обороты: $it | ") }
                    params.speed?.let { append("Скорость: $it км/ч | ") }
                    params.coolantTemperature?.let { append("Темп: ${it.toInt()}°C") }
                }
                updateNotification(updateText)
            }
        }
    }

    private fun stopMonitoring() {
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "OBD2 Мониторинг",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Фоновый мониторинг параметров двигателя"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()
    }

    private fun updateNotification(content: String) {
        val notification = createNotification("OBD2 Мониторинг", content)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
