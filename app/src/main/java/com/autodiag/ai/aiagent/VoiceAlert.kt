package com.autodiag.ai.aiagent

/**
 * Голосовое предупреждение
 */
data class VoiceAlert(
    val priority: AlertPriority,
    val message: String,
    val type: AlertType,
    val timestamp: Long = System.currentTimeMillis()
)
