package com.autodiag.ai.aiagent

import com.autodiag.ai.data.model.EngineParameters
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit тесты для SafeAdaptiveEngineAgent
 * Покрытие: анализ профиля, рекомендации, валидация данных
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SafeAdaptiveEngineAgentTest {

    private lateinit var agent: SafeAdaptiveEngineAgent
    private val mockObdManager = mockk<com.autodiag.ai.services.ObdConnectionManager>(relaxed = true)

    @Before
    fun setup() {
        agent = SafeAdaptiveEngineAgent(mockObdManager)
    }

    @Test
    fun `startDataCollection should initialize analysis with correct parameters`() = runTest {
        // When
        agent.startDataCollection(durationKm = 15)
        
        // Then
        val analysis = agent.currentAnalysis.first()
        assertEquals(AnalysisStatus.COLLECTING, analysis.status)
        assertEquals(15, analysis.targetDistanceKm)
        assertTrue(analysis.message.contains("Сбор данных начат"))
    }

    @Test
    fun `stopAndAnalyze with insufficient data should return insufficient status`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When - добавляем мало образцов
        repeat(5) {
            agent.addSample(createSampleEngineParams())
        }
        
        // Then
        val result = agent.stopAndAnalyze()
        assertEquals(AnalysisStatus.INSUFFICIENT_DATA, result.status)
        assertTrue(result.message.contains("Недостаточно данных"))
    }

    @Test
    fun `stopAndAnalyze with sufficient data should detect economical profile`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When - добавляем образцы экономичного вождения
        repeat(20) {
            agent.addSample(createEconomicalDrivingParams())
        }
        
        // Then
        val result = agent.stopAndAnalyze()
        assertEquals(AnalysisStatus.COMPLETED, result.status)
        assertEquals(DriverProfile.ECONOMICAL, result.profile)
        assertNotNull(result.recommendations)
    }

    @Test
    fun `stopAndAnalyze with sufficient data should detect dynamic profile`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When - добавляем образцы динамичного вождения
        repeat(20) {
            agent.addSample(createDynamicDrivingParams())
        }
        
        // Then
        val result = agent.stopAndAnalyze()
        assertEquals(AnalysisStatus.COMPLETED, result.status)
        assertEquals(DriverProfile.DYNAMIC, result.profile)
    }

    @Test
    fun `recommendations for economical profile should have positive ignition timing`() = runTest {
        // Given
        agent.startDataCollection()
        repeat(20) { agent.addSample(createEconomicalDrivingParams()) }
        
        // When
        agent.stopAndAnalyze()
        
        // Then
        val recommendation = agent.recommendedSettings.first()
        assertNotNull(recommendation)
        assertTrue(recommendation!!.ignitionTimingOffset > 0)
        assertTrue(recommendation.fuelMixtureBias < 0)
        assertTrue(recommendation.isWithinSafeLimits())
    }

    @Test
    fun `recommendations for dynamic profile should have negative ignition timing`() = runTest {
        // Given
        agent.startDataCollection()
        repeat(20) { agent.addSample(createDynamicDrivingParams()) }
        
        // When
        agent.stopAndAnalyze()
        
        // Then
        val recommendation = agent.recommendedSettings.first()
        assertNotNull(recommendation)
        assertTrue(recommendation!!.ignitionTimingOffset < 0)
        assertTrue(recommendation.fuelMixtureBias > 0)
    }

    @Test
    fun `ignition timing offset should be within safe limits`() = runTest {
        // Given - тестируем все профили
        val profiles = listOf(
            DriverProfile.ECONOMICAL to { createEconomicalDrivingParams() },
            DriverProfile.DYNAMIC to { createDynamicDrivingParams() },
            DriverProfile.HIGHWAY to { createHighwayDrivingParams() },
            DriverProfile.URBAN to { createUrbanDrivingParams() }
        )
        
        profiles.forEach { (expectedProfile, paramsProvider) ->
            agent = SafeAdaptiveEngineAgent(mockObdManager)
            agent.startDataCollection()
            repeat(20) { agent.addSample(paramsProvider()) }
            
            // When
            agent.stopAndAnalyze()
            
            // Then
            val recommendation = agent.recommendedSettings.first()
            assertNotNull("Recommendation should not be null for $expectedProfile", recommendation)
            assertTrue(
                "Ignition timing ${recommendation!!.ignitionTimingOffset} exceeds safe limits for $expectedProfile",
                kotlin.math.abs(recommendation.ignitionTimingOffset) <= 2f
            )
            assertTrue(
                "Fuel mixture ${recommendation.fuelMixtureBias} exceeds safe limits for $expectedProfile",
                kotlin.math.abs(recommendation.fuelMixtureBias) <= 5f
            )
        }
    }

    @Test
    fun `temperature warning should be generated when coolant temp exceeds threshold`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When
        agent.addSample(createSampleEngineParams(coolantTemp = 105f))
        
        // Then
        val alerts = agent.voiceAlerts.first()
        assertTrue(alerts.any { it.type == AlertType.TEMPERATURE })
    }

    @Test
    fun `critical temperature alert should be generated when temp exceeds critical threshold`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When
        agent.addSample(createSampleEngineParams(coolantTemp = 115f))
        
        // Then
        val alerts = agent.voiceAlerts.first()
        val criticalAlert = alerts.find { it.type == AlertType.TEMPERATURE }
        assertNotNull(criticalAlert)
        assertEquals(AlertPriority.CRITICAL, criticalAlert!!.priority)
    }

    @Test
    fun `knock detection should generate warning alert`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When
        agent.addSample(createSampleEngineParams(knockSensor = 3.0f))
        
        // Then
        val alerts = agent.voiceAlerts.first()
        assertTrue(alerts.any { it.type == AlertType.KNOCK })
    }

    @Test
    fun `lean mixture should be detected when fuel trim is low`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When
        agent.addSample(createSampleEngineParams(shortTermFuelTrim = -20f))
        
        // Then
        val alerts = agent.voiceAlerts.first()
        assertTrue(alerts.any { it.type == AlertType.FUEL_MIXTURE })
    }

    @Test
    fun `rich mixture should be detected when fuel trim is high`() = runTest {
        // Given
        agent.startDataCollection()
        
        // When
        agent.addSample(createSampleEngineParams(shortTermFuelTrim = 20f))
        
        // Then
        val alerts = agent.voiceAlerts.first()
        assertTrue(alerts.any { it.type == AlertType.FUEL_MIXTURE })
    }

    @Test
    fun `clearAlerts should remove all voice alerts`() = runTest {
        // Given
        agent.startDataCollection()
        agent.addSample(createSampleEngineParams(coolantTemp = 105f))
        assertTrue(agent.voiceAlerts.first().isNotEmpty())
        
        // When
        agent.clearAlerts()
        
        // Then
        assertTrue(agent.voiceAlerts.first().isEmpty())
    }

    @Test
    fun `resetToFactory should clear recommended settings`() = runTest {
        // Given
        agent.startDataCollection()
        repeat(20) { agent.addSample(createEconomicalDrivingParams()) }
        agent.stopAndAnalyze()
        assertNotNull(agent.recommendedSettings.first())
        
        // When
        agent.resetToFactory()
        
        // Then
        assertNull(agent.recommendedSettings.first())
    }

    @Test
    fun `getCurrentParameters should return null when no samples collected`() {
        // When
        val params = agent.getCurrentParameters()
        
        // Then
        assertNull(params)
    }

    @Test
    fun `getCurrentParameters should return last sample data`() = runTest {
        // Given
        agent.startDataCollection()
        val sample = createSampleEngineParams(rpm = 2500, speed = 60, coolantTemp = 90f)
        agent.addSample(sample)
        
        // When
        val params = agent.getCurrentParameters()
        
        // Then
        assertNotNull(params)
        assertEquals(2500f, params!!.rpm)
        assertEquals(60f, params.speed)
        assertEquals(90f, params.coolantTemp)
    }

    @Test
    fun `addSample should not add data when not collecting`() = runTest {
        // Given - не запускаем сбор данных
        
        // When
        agent.addSample(createSampleEngineParams())
        
        // Then
        val params = agent.getCurrentParameters()
        assertNull(params)
    }

    // Helper methods
    
    private fun createSampleEngineParams(
        rpm: Int? = 2000,
        speed: Int? = 50,
        coolantTemp: Float? = 85f,
        knockSensor: Float? = 0f,
        shortTermFuelTrim: Float? = 0f
    ): EngineParameters {
        return EngineParameters(
            rpm = rpm,
            speed = speed,
            coolantTemperature = coolantTemp,
            throttlePosition = 30f,
            engineLoad = 40f,
            knockSensorValue = knockSensor,
            shortTermFuelTrim = shortTermFuelTrim,
            longTermFuelTrim = 0f,
            intakeAirTemperature = 25f
        )
    }
    
    private fun createEconomicalDrivingParams(): EngineParameters {
        return EngineParameters(
            rpm = (1500..2200).random(),
            speed = (30..70).random(),
            coolantTemperature = 85f,
            throttlePosition = (10..25).random().toFloat(),
            engineLoad = (20..40).random().toFloat()
        )
    }
    
    private fun createDynamicDrivingParams(): EngineParameters {
        return EngineParameters(
            rpm = (4000..5500).random(),
            speed = (80..120).random(),
            coolantTemperature = 95f,
            throttlePosition = (60..90).random().toFloat(),
            engineLoad = (70..90).random().toFloat()
        )
    }
    
    private fun createHighwayDrivingParams(): EngineParameters {
        return EngineParameters(
            rpm = (2000..2800).random(),
            speed = (90..130).random(),
            coolantTemperature = 88f,
            throttlePosition = (25..40).random().toFloat(),
            engineLoad = (30..50).random().toFloat()
        )
    }
    
    private fun createUrbanDrivingParams(): EngineParameters {
        return EngineParameters(
            rpm = (1200..2500).random(),
            speed = (20..45).random(),
            coolantTemperature = 92f,
            throttlePosition = (15..45).random().toFloat(),
            engineLoad = (25..55).random().toFloat()
        )
    }
}
