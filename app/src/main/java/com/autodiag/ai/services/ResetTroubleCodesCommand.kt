package com.autodiag.ai.services

import com.github.pires.obd.commands.ObdCommand

/**
 * Custom OBD command to reset/clear trouble codes (Mode 04).
 * This command is not available in the base obd-java-api library (1.0-RC16),
 * so we implement it as a custom command.
 * 
 * Mode 04 clears DTCs and turns off the MIL (Check Engine Light).
 * Command format: 04
 * Expected response: 44 (acknowledgment)
 */
class ResetTroubleCodesCommand : ObdCommand("04") {
    
    private var result: String = ""
    
    override fun performCalculations() {
        // Mode 04 returns 44 as acknowledgment
        result = rawData
    }
    
    override fun getFormattedResult(): String {
        return result
    }
    
    override fun getCalculatedResult(): String {
        return "Trouble codes cleared"
    }
    
    override fun getName(): String {
        return "Reset Trouble Codes"
    }
}
