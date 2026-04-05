package com.autodiag.ai.domain.usecase

import com.autodiag.ai.domain.model.VehicleBrandDomain
import com.autodiag.ai.domain.model.VehicleDomainModel
import com.autodiag.ai.domain.repository.VehicleRepository
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
 * Unit тесты для VehicleUseCase
 * Покрытие: валидация, бизнес-логика, обработка ошибок
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VehicleUseCaseTest {

    private lateinit var vehicleUseCase: VehicleUseCase
    private val mockVehicleRepository: VehicleRepository = mockk(relaxed = true)

    @Before
    fun setup() {
        vehicleUseCase = VehicleUseCase(mockVehicleRepository)
    }

    @Test
    fun `addVehicle with valid data should succeed`() = runTest {
        // Given
        val vehicle = createValidVehicle()
        coEvery { mockVehicleRepository.vehicleExists(any()) } returns false
        coEvery { mockVehicleRepository.addVehicle(any()) } returns Result.success(Unit)

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(vehicle.vin, result.getOrNull())
    }

    @Test
    fun `addVehicle with empty VIN should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle().copy(vin = "")

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("VIN") == true)
    }

    @Test
    fun `addVehicle with invalid VIN length should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle().copy(vin = "SHORT")

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("17 символов") == true)
    }

    @Test
    fun `addVehicle with duplicate VIN should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle()
        coEvery { mockVehicleRepository.vehicleExists(vehicle.vin) } returns true

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("уже существует") == true)
    }

    @Test
    fun `addVehicle with invalid year should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle().copy(year = 1800)

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("год") == true)
    }

    @Test
    fun `addVehicle with empty model should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle().copy(model = "")
        coEvery { mockVehicleRepository.vehicleExists(any()) } returns false

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Модель") == true)
    }

    @Test
    fun `addVehicle with negative mileage should fail`() = runTest {
        // Given
        val vehicle = createValidVehicle().copy(mileage = -100)
        coEvery { mockVehicleRepository.vehicleExists(any()) } returns false

        // When
        val result = vehicleUseCase.addVehicle(vehicle)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Пробег") == true)
    }

    @Test
    fun `selectVehicle with valid VIN should succeed`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        coEvery { mockVehicleRepository.vehicleExists(vin) } returns true
        coEvery { mockVehicleRepository.setSelectedVehicle(any()) } returns Result.success(Unit)

        // When
        val result = vehicleUseCase.selectVehicle(vin)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `selectVehicle with empty VIN should fail`() = runTest {
        // When
        val result = vehicleUseCase.selectVehicle("")

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("VIN") == true)
    }

    @Test
    fun `selectVehicle with non-existent VIN should fail`() = runTest {
        // Given
        val vin = "NONEXISTENT123456"
        coEvery { mockVehicleRepository.vehicleExists(vin) } returns false

        // When
        val result = vehicleUseCase.selectVehicle(vin)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("не найден") == true)
    }

    @Test
    fun `updateMileage with valid data should succeed`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        val mileage = 50000
        coEvery { mockVehicleRepository.updateMileage(any(), any()) } returns Result.success(Unit)

        // When
        val result = vehicleUseCase.updateMileage(vin, mileage)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockVehicleRepository.updateMileage(vin, mileage) }
    }

    @Test
    fun `updateMileage with negative value should fail`() = runTest {
        // When
        val result = vehicleUseCase.updateMileage("VIN12345678901234", -100)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Пробег") == true)
    }

    @Test
    fun `deleteVehicle with existing VIN should succeed`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        val vehicle = createValidVehicle().copy(vin = vin)
        coEvery { mockVehicleRepository.getVehicleByVin(vin) } returns vehicle
        coEvery { mockVehicleRepository.deleteVehicle(any()) } returns Result.success(Unit)

        // When
        val result = vehicleUseCase.deleteVehicle(vin)

        // Then
        assertTrue(result.isSuccess)
        coVerify { mockVehicleRepository.deleteVehicle(vehicle) }
    }

    @Test
    fun `deleteVehicle with non-existent VIN should fail`() = runTest {
        // Given
        val vin = "NONEXISTENT123456"
        coEvery { mockVehicleRepository.getVehicleByVin(vin) } returns null

        // When
        val result = vehicleUseCase.deleteVehicle(vin)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("не найден") == true)
    }

    @Test
    fun `checkServiceRequired should return vehicles with service status`() = runTest {
        // Given
        val vehicles = listOf(
            createValidVehicle().copy(vin = "VIN1", mileage = 15000, nextServiceMileage = 10000),
            createValidVehicle().copy(vin = "VIN2", mileage = 5000, nextServiceMileage = 10000)
        )
        coEvery { mockVehicleRepository.getAllVehicles() } returns flowOf(vehicles)

        // When
        val result = vehicleUseCase.checkServiceRequired()

        // Then
        assertEquals(2, result.size)
        assertTrue(result[0].second) // Пробег превышает ТО
        assertFalse(result[1].second) // Пробег меньше ТО
    }

    @Test
    fun `isServiceRequired should return true when mileage exceeds next service`() {
        // Given
        val vehicle = createValidVehicle().copy(mileage = 15000, nextServiceMileage = 10000)

        // Then
        assertTrue(vehicle.isServiceRequired())
    }

    @Test
    fun `isServiceRequired should return false when mileage less than next service`() {
        // Given
        val vehicle = createValidVehicle().copy(mileage = 5000, nextServiceMileage = 10000)

        // Then
        assertFalse(vehicle.isServiceRequired())
    }

    @Test
    fun `isServiceRequired should return false when nextServiceMileage is null`() {
        // Given
        val vehicle = createValidVehicle().copy(mileage = 50000, nextServiceMileage = null)

        // Then
        assertFalse(vehicle.isServiceRequired())
    }

    @Test
    fun `displayName should contain brand model and year`() {
        // Given
        val vehicle = createValidVehicle()

        // When
        val displayName = vehicle.displayName

        // Then
        assertTrue(displayName.contains("ВАЗ/LADA"))
        assertTrue(displayName.contains("Granta"))
        assertTrue(displayName.contains("2020"))
    }

    // Helper methods

    private fun createValidVehicle(): VehicleDomainModel {
        return VehicleDomainModel(
            vin = "VIN12345678901234",
            brand = VehicleBrandDomain.VAZ,
            model = "Granta",
            year = 2020,
            engineType = "1.6 8V",
            engineCode = "11183",
            engineVolume = 1.6f,
            fuelType = com.autodiag.ai.domain.model.FuelTypeDomain.PETROL_92,
            transmission = com.autodiag.ai.domain.model.TransmissionTypeDomain.MANUAL_5,
            mileage = 10000,
            nextServiceMileage = 15000
        )
    }
}
