package com.autodiag.ai.data.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.autodiag.ai.data.model.EngineParameters
import com.autodiag.ai.services.ObdConnectionManager
import kotlinx.coroutines.flow.Flow

class BluetoothRepository(
    private val context: Context,
    private val obdManager: ObdConnectionManager
) {
    
    fun getPairedDevices(): Set<BluetoothDevice> {
        return obdManager.getPairedDevices()
    }
    
    suspend fun connect(device: BluetoothDevice): Result<Unit> {
        return obdManager.connect(device)
    }
    
    fun disconnect() {
        obdManager.disconnect()
    }
    
    fun isConnected(): Boolean {
        return obdManager.isConnected()
    }
    
    fun startMonitoring(intervalMs: Long): Flow<EngineParameters> {
        return obdManager.startMonitoring(intervalMs)
    }
    
    suspend fun readTroubleCodes(): List<String> {
        return obdManager.readTroubleCodes()
    }
    
    suspend fun clearTroubleCodes(): Result<Unit> {
        return obdManager.clearTroubleCodes()
    }
}
