package com.autodiag.ai.domain.usecase

import com.autodiag.ai.domain.model.*
import com.autodiag.ai.domain.repository.AnalysisRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit тесты для AnalysisUseCase
 * Покрытие: анализ двигателя, рекомендации, бэкапы настроек
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisUseCaseTest {

    private lateinit var analysisUseCase: AnalysisUseCase
    private val mockAnalysisRepository: AnalysisRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        analysisUseCase = AnalysisUseCase(mockAnalysisRepository)
    }

    @Test
    fun `analyzeEngine with empty VIN should fail`() = runTest {
        // When
        val result = analysisUseCase.analyzeEngine("", createEngineParams())

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("VIN") == true)
    }

    @Test
    fun `analyzeEngine with critical temperature should detect issue`() = runTest {
        // Given
        val params = createEngineParams(coolantTemp = 110f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Критическая температура") } == true)
        assertEquals(AnalysisUseCase.EngineStatus.CRITICAL, result.getOrNull()?.engineStatus)
    }

    @Test
    fun `analyzeEngine with high temperature should detect issue`() = runTest {
        // Given
        val params = createEngineParams(coolantTemp = 100f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Повышенная температура") } == true)
    }

    @Test
    fun `analyzeEngine with low temperature should detect issue`() = runTest {
        // Given
        val params = createEngineParams(coolantTemp = 60f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("не прогрет") } == true)
    }

    @Test
    fun `analyzeEngine with critical RPM should detect issue`() = runTest {
        // Given
        val params = createEngineParams(rpm = 6500)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Критические обороты") } == true)
    }

    @Test
    fun `analyzeEngine with unstable idle should detect issue`() = runTest {
        // Given
        val params = createEngineParams(rpm = 1500, speed = 0)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Нестабильный холостой ход") } == true)
    }

    @Test
    fun `analyzeEngine with critical fuel trim should detect issue`() = runTest {
        // Given
        val params = createEngineParams(shortTermFuelTrim = 30f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Критическое отклонение") } == true)
    }

    @Test
    fun `analyzeEngine with high fuel trim should detect issue`() = runTest {
        // Given
        val params = createEngineParams(shortTermFuelTrim = 20f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.any { it.contains("Отклонение топливной смеси") } == true)
    }

    @Test
    fun `analyzeEngine with normal parameters should return healthy status`() = runTest {
        // Given
        val params = createEngineParams()

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.issues?.isEmpty() == true)
        assertEquals(100f, result.getOrNull()?.healthScore)
    }

    @Test
    fun `analyzeEngine with invalid RPM should fail validation`() = runTest {
        // Given
        val params = createEngineParams(rpm = 20000)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Некорректные обороты") == true)
    }

    @Test
    fun `analyzeEngine with invalid temperature should fail validation`() = runTest {
        // Given
        val params = createEngineParams(coolantTemp = 200f)

        // When
        val result = analysisUseCase.analyzeEngine("VIN12345678901234", params)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Некорректная температура") == true)
    }

    @Test
    fun `getRecommendations for economical profile should have safe adjustments`() = runTest {
        // When
        val result = analysisUseCase.getRecommendations(DriverProfileDomain.ECONOMICAL)

        // Then
        assertTrue(result.isSuccess)
        val recommendation = result.getOrNull()
        assertNotNull(recommendation)
        assertTrue(recommendation!!.ignitionTimingOffset in 0f..2f)
        assertTrue(recommendation.fuelMixtureBias in -5f..0f)
        assertTrue(recommendation.isWithinSafeLimits())
    }

    @Test
    fun `getRecommendations for dynamic profile should have safe adjustments`() = runTest {
        // When
        val result = analysisUseCase.getRecommendations(DriverProfileDomain.DYNAMIC)

        // Then
        assertTrue(result.isSuccess)
        val recommendation = result.getOrNull()
        assertNotNull(recommendation)
        assertTrue(recommendation!!.ignitionTimingOffset in -2f..0f)
        assertTrue(recommendation.fuelMixtureBias in 0f..5f)
        assertTrue(recommendation.isWithinSafeLimits())
    }

    @Test
    fun `getRecommendations for highway profile should have safe adjustments`() = runTest {
        // When
        val result = analysisUseCase.getRecommendations(DriverProfileDomain.HIGHWAY)

        // Then
        assertTrue(result.isSuccess)
        val recommendation = result.getOrNull()
        assertNotNull(recommendation)
        assertTrue(recommendation!!.ignitionTimingOffset in 0f..2f)
        assertTrue(recommendation.fuelMixtureBias in -5f..0f)
    }

    @Test
    fun `getRecommendations for urban profile should have safe adjustments`() = runTest {
        // When
        val result = analysisUseCase.getRecommendations(DriverProfileDomain.URBAN)

        // Then
        assertTrue(result.isSuccess)
        val recommendation = result.getOrNull()
        assertNotNull(recommendation)
        assertEquals(0f, recommendation!!.ignitionTimingOffset)
        assertTrue(recommendation.fuelMixtureBias in 0f..5f)
    }

    @Test
    fun `getRecommendations should normalize unsafe adjustments`() = runTest {
        // This test verifies that any profile-specific logic that might produce
        // unsafe values gets properly normalized
        
        // When
        val result = analysisUseCase.getRecommendations(DriverProfileDomain.ECONOMICAL)
        
        // Then
        assertTrue(result.isSuccess)
        val recommendation = result.getOrNull()
        assertTrue(recommendation!!.isWithinSafeLimits())
        assertTrue(kotlin.math.abs(recommendation.ignitionTimingOffset) <= 2f)
        assertTrue(kotlin.math.abs(recommendation.fuelMixtureBias) <= 5f)
    }

    @Test
    fun `backupSettings should create backup in repository`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        coEvery { mockAnalysisRepository.createSettingsBackup(any()) } returns Result.success(1L)

        // When
        val result = analysisUseCase.backupSettings(vin, 0f, 0f)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
        coVerify { mockAnalysisRepository.createSettingsBackup(any()) }
    }

    @Test
    fun `restoreLastSettings should restore from latest backup`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        val backup = SettingsBackupDomain(
            id = 1L,
            vehicleVin = vin,
            originalIgnitionTiming = 1f,
            originalFuelMixture = -2f
        )
        coEvery { mockAnalysisRepository.getLatestBackup(vin) } returns backup
        coEvery { mockAnalysisRepository.restoreFromBackup(1L) } returns Result.success(backup)

        // When
        val result = analysisUseCase.restoreLastSettings(vin)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1f, result.getOrNull()?.originalIgnitionTiming)
        assertEquals(-2f, result.getOrNull()?.originalFuelMixture)
    }

    @Test
    fun `restoreLastSettings with no backup should fail`() = runTest {
        // Given
        coEvery { mockAnalysisRepository.getLatestBackup(any()) } returns null

        // When
        val result = analysisUseCase.restoreLastSettings("VIN12345678901234")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Резервная копия не найдена") == true)
    }

    @Test
    fun `saveAnalysis should call repository`() = runTest {
        // Given
        val analysis = createDrivingAnalysis()
        coEvery { mockAnalysisRepository.saveAnalysis(any()) } returns Result.success(Unit)

        // When
        val result = analysisUseCase.saveAnalysis(analysis)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockAnalysisRepository.saveAnalysis(analysis) }
    }

    @Test
    fun `getAllAnalyses should return flow from repository`() = runTest {
        // Given
        val analyses = listOf(createDrivingAnalysis(), createDrivingAnalysis())
        coEvery { mockAnalysisRepository.getAllAnalyses() } returns flowOf(analyses)

        // When
        val result = analysisUseCase.getAllAnalyses()

        // Then
        result.collect { list ->
            assertEquals(2, list.size)
        }
    }

    @Test
    fun `getAnalysisStatistics with empty data should return empty stats`() = runTest {
        // Given
        coEvery { mockAnalysisRepository.getAllAnalyses() } returns flowOf(emptyList())

        // When
        val result = analysisUseCase.getAnalysisStatistics()

        // Then
        assertEquals(0, result.totalAnalyses)
        assertEquals(DriverProfileDomain.UNKNOWN, result.mostCommonProfile)
        assertEquals(0, result.averageSpeed)
    }

    @Test
    fun `getAnalysisStatistics should calculate correct statistics`() = runTest {
        // Given
        val analyses = listOf(
            createDrivingAnalysis(profile = DriverProfileDomain.ECONOMICAL, avgSpeed = 50, avgRpm = 2000),
            createDrivingAnalysis(profile = DriverProfileDomain.ECONOMICAL, avgSpeed = 60, avgRpm = 2200),
            createDrivingAnalysis(profile = DriverProfileDomain.DYNAMIC, avgSpeed = 80, avgRpm = 3000)
        )
        coEvery { mockAnalysisRepository.getAllAnalyses() } returns flowOf(analyses)

        // When
        val result = analysisUseCase.getAnalysisStatistics()

        // Then
        assertEquals(3, result.totalAnalyses)
        assertEquals(DriverProfileDomain.ECONOMICAL, result.mostCommonProfile)
        assertEquals(63, result.averageSpeed) // (50 + 60 + 80) / 3
        assertEquals(2400, result.averageRpm) // (2000 + 2200 + 3000) / 3
    }

    @Test
    fun `isWithinSafeLimits should return true for valid adjustments`() {
        // Given
        val recommendation = EngineTuneRecommendationDomain(
            profile = DriverProfileDomain.ECONOMICAL,
            description = "Test",
            ignitionTimingOffset = 1.5f,
            fuelMixtureBias = -3f,
            reasoning = emptyList(),
            safetyNotes = emptyList()
        )

        // Then
        assertTrue(recommendation.isWithinSafeLimits())
    }

    @Test
    fun `isWithinSafeLimits should return false for excessive adjustments`() {
        // Given
        val recommendation = EngineTuneRecommendationDomain(
            profile = DriverProfileDomain.ECONOMICAL,
            description = "Test",
            ignitionTimingOffset = 5f, // Exceeds ±2° limit
            fuelMixtureBias = -10f, // Exceeds ±5% limit
            reasoning = emptyList(),
            safetyNotes = emptyList()
        )

        // Then
        assertFalse(recommendation.isWithinSafeLimits())
    }

    @Test
    fun `normalized should clamp values to safe limits`() {
        // Given
        val recommendation = EngineTuneRecommendationDomain(
            profile = DriverProfileDomain.ECONOMICAL,
            description = "Test",
            ignitionTimingOffset = 5f,
            fuelMixtureBias = -10f,
            reasoning = emptyList(),
            safetyNotes = emptyList()
        )

        // When
        val normalized = recommendation.normalized()

        // Then
        assertEquals(2f, normalized.ignitionTimingOffset)
        assertEquals(-5f, normalized.fuelMixtureBias)
        assertTrue(normalized.isWithinSafeLimits())
    }

    // Helper methods

    private fun createEngineParams(
        rpm: Int? = 2500,
        speed: Int? = 60,
        coolantTemp: Float? = 85f,
        shortTermFuelTrim: Float? = 0f
    ): AnalysisUseCase.EngineParametersInput {
        return AnalysisUseCase.EngineParametersInput(
            rpm = rpm,
            speed = speed,
            coolantTemp = coolantTemp,
            shortTermFuelTrim = shortTermFuelTrim
        )
    }

    private fun createDrivingAnalysis(
        profile: DriverProfileDomain = DriverProfileDomain.BALANCED,
        avgSpeed: Int = 50,
        avgRpm: Int = 2000
    ): DrivingAnalysisDomainModel {
        return DrivingAnalysisDomainModel(
            profile = profile,
            drivingStyle = DrivingStyleDomain.BALANCED,
            samplesCount = 100,
            averageSpeed = avgSpeed,
            averageRpm = avgRpm,
            status = AnalysisStatusDomain.COMPLETED
        )
    }
}
