package com.autodiag.ai.utils

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import com.autodiag.ai.BuildConfig

/**
 * Utility class for Firebase Analytics and Crashlytics logging.
 * Automatically disables logging in debug builds.
 */
object FirebaseLogger {

    private const val TAG = "FirebaseLogger"
    private val analytics: FirebaseAnalytics? by lazy {
        if (BuildConfig.DEBUG) null else Firebase.analytics
    }
    private val crashlytics: FirebaseCrashlytics? by lazy {
        if (BuildConfig.DEBUG) null else Firebase.crashlytics
    }

    init {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Firebase logging disabled in debug build")
        }
    }

    // ==================== Analytics Events ====================

    /**
     * Log screen view event
     */
    fun logScreenView(screenName: String, screenClass: String? = null) {
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { param(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Screen view: $screenName")
    }

    /**
     * Log OBD2 connection event
     */
    fun logObdConnection(deviceName: String?, success: Boolean) {
        analytics?.logEvent("obd_connection") {
            param("device_name", deviceName ?: "unknown")
            param("success", success.toString())
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "OBD connection: $deviceName, success=$success")
    }

    /**
     * Log diagnostic scan completion
     */
    fun logDiagnosticScan(faultCount: Int, durationMs: Long) {
        analytics?.logEvent("diagnostic_scan") {
            param("fault_count", faultCount.toLong())
            param("duration_ms", durationMs)
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Diagnostic scan: $faultCount faults, ${durationMs}ms")
    }

    /**
     * Log AI analysis request
     */
    fun logAiAnalysis(parametersCount: Int) {
        analytics?.logEvent("ai_analysis") {
            param("parameters_count", parametersCount.toLong())
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "AI analysis: $parametersCount parameters")
    }

    /**
     * Log vehicle data update
     */
    fun logVehicleDataUpdate(make: String?, model: String?) {
        analytics?.logEvent("vehicle_data_update") {
            param("make", make ?: "unknown")
            param("model", model ?: "unknown")
        }
    }

    /**
     * Log export/share action
     */
    fun logExport(exportType: String) {
        analytics?.logEvent("export_data") {
            param("type", exportType)
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Export: $exportType")
    }

    /**
     * Log custom event
     */
    fun logEvent(eventName: String, params: Map<String, String> = emptyMap()) {
        analytics?.logEvent(eventName) {
            params.forEach { (key, value) ->
                param(key, value)
            }
        }
        if (BuildConfig.DEBUG) Log.d(TAG, "Event: $eventName, params=$params")
    }

    // ==================== Crashlytics ====================

    /**
     * Log non-fatal exception
     */
    fun logException(throwable: Throwable, message: String? = null) {
        message?.let { crashlytics?.log(it) }
        crashlytics?.recordException(throwable)
        if (BuildConfig.DEBUG) Log.e(TAG, "Exception logged: $message", throwable)
    }

    /**
     * Set user identifier
     */
    fun setUserId(userId: String) {
        crashlytics?.setUserId(userId)
        analytics?.setUserId(userId)
    }

    /**
     * Set custom key for crash reports
     */
    fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
    }

    /**
     * Set custom key for crash reports (boolean)
     */
    fun setCustomKey(key: String, value: Boolean) {
        crashlytics?.setCustomKey(key, value)
    }

    /**
     * Set custom key for crash reports (int)
     */
    fun setCustomKey(key: String, value: Int) {
        crashlytics?.setCustomKey(key, value)
    }

    /**
     * Log message to Crashlytics
     */
    fun log(message: String) {
        crashlytics?.log(message)
        if (BuildConfig.DEBUG) Log.d(TAG, message)
    }

    /**
     * Force crash (for testing only)
     */
    fun forceCrash() {
        crashlytics?.crash()
    }

    /**
     * Check if Firebase services are available
     */
    fun isAvailable(): Boolean {
        return analytics != null && crashlytics != null
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
