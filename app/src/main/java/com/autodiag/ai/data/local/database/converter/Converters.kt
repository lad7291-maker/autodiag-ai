package com.autodiag.ai.data.local.database.converter

import androidx.room.TypeConverter
import com.autodiag.ai.data.model.DiagnosticStep
import com.autodiag.ai.data.model.DetectedIssue
import com.autodiag.ai.data.model.FaultCause
import com.autodiag.ai.data.model.OperatingTip
import com.autodiag.ai.data.model.RepairCost
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // List<String>
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return json.decodeFromString(value)
    }
    
    // List<FaultCause>
    @TypeConverter
    fun fromFaultCauseList(value: List<FaultCause>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toFaultCauseList(value: String): List<FaultCause> {
        return json.decodeFromString(value)
    }
    
    // List<DiagnosticStep>
    @TypeConverter
    fun fromDiagnosticStepList(value: List<DiagnosticStep>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toDiagnosticStepList(value: String): List<DiagnosticStep> {
        return json.decodeFromString(value)
    }
    
    // RepairCost
    @TypeConverter
    fun fromRepairCost(value: RepairCost?): String {
        return value?.let { json.encodeToString(it) } ?: ""
    }
    
    @TypeConverter
    fun toRepairCost(value: String): RepairCost? {
        return if (value.isEmpty()) null else json.decodeFromString(value)
    }
    
    // List<DetectedIssue>
    @TypeConverter
    fun fromDetectedIssueList(value: List<DetectedIssue>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toDetectedIssueList(value: String): List<DetectedIssue> {
        return json.decodeFromString(value)
    }
    
    // List<OperatingTip>
    @TypeConverter
    fun fromOperatingTipList(value: List<OperatingTip>): String {
        return json.encodeToString(value)
    }
    
    @TypeConverter
    fun toOperatingTipList(value: String): List<OperatingTip> {
        return json.decodeFromString(value)
    }
}
