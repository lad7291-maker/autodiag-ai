package com.autodiag.ai.services

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.autodiag.ai.data.model.EngineParameters
import com.github.pires.obd.commands.SpeedCommand
import com.github.pires.obd.commands.engine.LoadCommand
import com.github.pires.obd.commands.engine.MassAirFlowCommand
import com.github.pires.obd.commands.engine.RPMCommand
import com.github.pires.obd.commands.engine.RuntimeCommand
import com.github.pires.obd.commands.engine.ThrottlePositionCommand
import com.github.pires.obd.commands.fuel.ConsumptionRateCommand
import com.github.pires.obd.commands.fuel.FuelLevelCommand
import com.github.pires.obd.commands.pressure.BarometricPressureCommand
import com.github.pires.obd.commands.pressure.IntakeManifoldPressureCommand
import com.github.pires.obd.commands.protocol.EchoOffCommand
import com.github.pires.obd.commands.protocol.LineFeedOffCommand
import com.github.pires.obd.commands.protocol.ObdResetCommand
import com.github.pires.obd.commands.protocol.SelectProtocolCommand
import com.github.pires.obd.commands.protocol.TimeoutCommand
import com.github.pires.obd.commands.temperature.AirIntakeTemperatureCommand
import com.github.pires.obd.commands.temperature.EngineCoolantTemperatureCommand
import com.github.pires.obd.enums.ObdProtocols
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID

class ObdConnectionManager(private val context: Context) {
    
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private var isConnected = false
    
    companion object {
        private val OBD_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        private const val DEFAULT_TIMEOUT = 5000L
    }
    
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }
    
    fun getPairedDevices(): Set<BluetoothDevice> {
        return bluetoothAdapter?.bondedDevices ?: emptySet()
    }
    
    suspend fun connect(device: BluetoothDevice): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(OBD_UUID)
            bluetoothSocket?.connect()
            
            // Инициализация OBD
            initializeObd()
            
            isConnected = true
            Result.success(Unit)
        } catch (e: IOException) {
            isConnected = false
            Result.failure(e)
        }
    }
    
    private suspend fun initializeObd() {
        withContext(Dispatchers.IO) {
            bluetoothSocket?.let { socket ->
                ObdResetCommand().run(socket.inputStream, socket.outputStream)
                delay(500)
                
                EchoOffCommand().run(socket.inputStream, socket.outputStream)
                LineFeedOffCommand().run(socket.inputStream, socket.outputStream)
                TimeoutCommand(62).run(socket.inputStream, socket.outputStream)
                SelectProtocolCommand(ObdProtocols.AUTO).run(
                    socket.inputStream,
                    socket.outputStream
                )
            }
        }
    }
    
    fun disconnect() {
        try {
            bluetoothSocket?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        isConnected = false
        bluetoothSocket = null
    }
    
    fun isConnected(): Boolean = isConnected
    
    fun startMonitoring(intervalMs: Long = 1000): Flow<EngineParameters> = flow {
        while (isConnected && bluetoothSocket != null) {
            val params = readParameters()
            emit(params)
            delay(intervalMs)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Чтение параметров двигателя через OBD2
     * ИСПРАВЛЕНО: Теперь корректно сохраняет все значения
     */
    private suspend fun readParameters(): EngineParameters = withContext(Dispatchers.IO) {
        // Начинаем с базового объекта
        var params = EngineParameters(timestamp = System.currentTimeMillis())
        
        try {
            bluetoothSocket?.let { socket ->
                val input = socket.inputStream
                val output = socket.outputStream
                
                // RPM
                try {
                    val rpmCmd = RPMCommand()
                    rpmCmd.run(input, output)
                    params = params.copy(rpm = rpmCmd.rpm)
                } catch (e: Exception) { /* Игнорируем ошибку отдельного параметра */ }
                
                // Speed
                try {
                    val speedCmd = SpeedCommand()
                    speedCmd.run(input, output)
                    params = params.copy(speed = speedCmd.metricSpeed)
                } catch (e: Exception) { }
                
                // Coolant Temperature
                try {
                    val tempCmd = EngineCoolantTemperatureCommand()
                    tempCmd.run(input, output)
                    params = params.copy(coolantTemperature = tempCmd.temperature.toFloat())
                } catch (e: Exception) { }
                
                // Throttle Position
                try {
                    val throttleCmd = ThrottlePositionCommand()
                    throttleCmd.run(input, output)
                    params = params.copy(throttlePosition = throttleCmd.percentage)
                } catch (e: Exception) { }
                
                // Engine Load
                try {
                    val loadCmd = LoadCommand()
                    loadCmd.run(input, output)
                    params = params.copy(engineLoad = loadCmd.percentage)
                } catch (e: Exception) { }
                
                // Intake Temperature
                try {
                    val intakeTempCmd = AirIntakeTemperatureCommand()
                    intakeTempCmd.run(input, output)
                    params = params.copy(intakeAirTemperature = intakeTempCmd.temperature.toFloat())
                } catch (e: Exception) { }
                
                // Intake Pressure
                try {
                    val pressureCmd = IntakeManifoldPressureCommand()
                    pressureCmd.run(input, output)
                    params = params.copy(intakeManifoldPressure = pressureCmd.pressure.toFloat())
                } catch (e: Exception) { }
                
                // Mass Air Flow
                try {
                    val mafCmd = MassAirFlowCommand()
                    mafCmd.run(input, output)
                    params = params.copy(massAirFlow = mafCmd.maf.toFloat())
                } catch (e: Exception) { }
                
                // Fuel Level
                try {
                    val fuelCmd = FuelLevelCommand()
                    fuelCmd.run(input, output)
                    params = params.copy(fuelLevel = fuelCmd.fuelLevel)
                } catch (e: Exception) { }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        params
    }
    
    suspend fun readTroubleCodes(): List<String> = withContext(Dispatchers.IO) {
        val codes = mutableListOf<String>()
        try {
            bluetoothSocket?.let { socket ->
                val input = socket.inputStream
                val output = socket.outputStream
                
                // Используем библиотечную команду для чтения кодов
                val dtcCmd = com.github.pires.obd.commands.control.TroubleCodesCommand()
                dtcCmd.run(input, output)
                codes.addAll(dtcCmd.formattedResult.split("\n"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        codes
    }
    
    suspend fun clearTroubleCodes(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            bluetoothSocket?.let { socket ->
                val input = socket.inputStream
                val output = socket.outputStream
                
                val clearCmd = ResetTroubleCodesCommand()
                clearCmd.run(input, output)
                Result.success(Unit)
            } ?: Result.failure(IOException("Not connected"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
