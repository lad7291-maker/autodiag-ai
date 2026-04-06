package com.autodiag.ai.utils

import android.util.Log
/**
 * Stub implementation for Firebase Analytics and Crashlytics logging.
 * Firebase is disabled for CI build - this class provides no-op implementations.
 */
object FirebaseLogger {

    private const val TAG = "FirebaseLogger"

    init {
        Log.d(TAG, "Firebase logging disabled (stub implementation)")
    }

    // ==================== Analytics Events ====================

    /**
     * Log screen view event (no-op)
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        Log.d(TAG, "Screen view: $screenName")
    }

    /**
     * Log OBD2 connection event (no-op)
     */
    fun logObdConnection(deviceName: String?, success: Boolean) {
        Log.d(TAG, "OBD connection: $deviceName, success=$success")
    }

    /**
     * Log diagnostic scan completion (no-op)
     */
    fun logDiagnosticScan(faultCount: Int, durationMs: Long) {
        Log.d(TAG, "Diagnostic scan: $faultCount faults, ${durationMs}ms")
    }

    /**
     * Log AI analysis request (no-op)
     */
    fun logAiAnalysis(parametersCount: Int) {
        Log.d(TAG, "AI analysis: $parametersCount parameters")
    }

    /**
     * Log vehicle data update (no-op)
     */
    fun logVehicleDataUpdate(make: String?, model: String?) {
        Log.d(TAG, "Vehicle data update: make=$make, model=$model")
    }

    /**
     * Log export/share action (no-op)
     */
    fun logExport(exportType: String) {
        Log.d(TAG, "Export: $exportType")
    }

    /**
     * Log custom event (no-op)
     */
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        Log.d(TAG, "Event: $eventName, params=$params")
    }

    // ==================== Crashlytics ====================

    /**
     * Log non-fatal exception (no-op, logs to Logcat in debug)
     */
    fun logException(throwable: Throwable, message: String? = null) {
        Log.e(TAG, "Exception logged: $message", throwable)
    }

    /**
     * Set user identifier (no-op)
     */
    fun setUserId(userId: String) {
        // No-op
    }

    /**
     * Set custom key for crash reports (no-op)
     */
    fun setCustomKey(key: String, value: String) {
        // No-op
    }

    /**
     * Set custom key for crash reports (no-op)
     */
    fun setCustomKey(key: String, value: Boolean) {
        // No-op
    }

    /**
     * Set custom key for crash reports (no-op)
     */
    fun setCustomKey(key: String, value: Int) {
        // No-op
    }

    /**
     * Log message to Crashlytics (no-op, logs to Logcat in debug)
     */
    fun log(message: String) {
        Log.d(TAG, message)
    }

    /**
     * Force crash (no-op)
     */
    fun forceCrash() {
        // No-op
    }

    /**
     * Check if Firebase services are available (always returns false in stub)
     */
    fun isAvailable(): Boolean {
        return false
    }
}

/**
 * Extension function to safely execute and log exceptions
 */
inline fun <T> safeExecuteWithLogging(
    operationName: String,
    block: () -> T
): T? {
    return try {
        block()
    } catch (e: Exception) {
        FirebaseLogger.logException(e, "Failed to execute: $operationName")
        null
    }
}
