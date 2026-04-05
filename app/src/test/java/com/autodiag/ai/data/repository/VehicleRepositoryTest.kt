package com.autodiag.ai.data.repository

import com.autodiag.ai.data.local.database.dao.VehicleDao
import com.autodiag.ai.data.model.FuelType
import com.autodiag.ai.data.model.TransmissionType
import com.autodiag.ai.data.model.Vehicle
import com.autodiag.ai.data.model.VehicleBrand
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit тесты для VehicleRepository
 * Покрытие: CRUD операции, выбор автомобиля, фильтрация
 */
@OptIn(ExperimentalCoroutinesApi::class)
class VehicleRepositoryTest {

    private lateinit var repository: VehicleRepository
    private val mockVehicleDao: VehicleDao = mockk(relaxed = true)

    @Before
    fun setup() {
        repository = VehicleRepository(mockVehicleDao)
    }

    @Test
    fun `getAllVehicles should return flow from dao`() = runTest {
        // Given
        val vehicles = listOf(
            createTestVehicle("VIN12345678901234"),
            createTestVehicle("VIN98765432109876")
        )
        coEvery { mockVehicleDao.getAll() } returns flowOf(vehicles)

        // When
        val result = repository.getAllVehicles().first()

        // Then
        assertEquals(2, result.size)
        assertEquals("VIN12345678901234", result[0].vin)
    }

    @Test
    fun `getVehicleByVin should return vehicle from dao`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        val vehicle = createTestVehicle(vin)
        coEvery { mockVehicleDao.getByVin(vin) } returns vehicle

        // When
        val result = repository.getVehicleByVin(vin)

        // Then
        assertNotNull(result)
        assertEquals(vin, result?.vin)
    }

    @Test
    fun `getVehicleByVin should return null when not found`() = runTest {
        // Given
        coEvery { mockVehicleDao.getByVin(any()) } returns null

        // When
        val result = repository.getVehicleByVin("NONEXISTENT")

        // Then
        assertNull(result)
    }

    @Test
    fun `getSelectedVehicle should return selected vehicle from dao`() = runTest {
        // Given
        val vehicle = createTestVehicle("VIN12345678901234", isSelected = true)
        coEvery { mockVehicleDao.getSelectedVehicle() } returns vehicle

        // When
        val result = repository.getSelectedVehicle()

        // Then
        assertNotNull(result)
        assertTrue(result?.isSelected == true)
    }

    @Test
    fun `addVehicle should insert vehicle into dao`() = runTest {
        // Given
        val vehicle = createTestVehicle("VIN12345678901234")
        coEvery { mockVehicleDao.insert(any()) } returns Unit

        // When
        repository.addVehicle(vehicle)

        // Then
        coVerify { mockVehicleDao.insert(vehicle) }
    }

    @Test
    fun `updateVehicle should update vehicle in dao`() = runTest {
        // Given
        val vehicle = createTestVehicle("VIN12345678901234", mileage = 50000)
        coEvery { mockVehicleDao.update(any()) } returns Unit

        // When
        repository.updateVehicle(vehicle)

        // Then
        coVerify { mockVehicleDao.update(vehicle) }
    }

    @Test
    fun `deleteVehicle should delete vehicle from dao`() = runTest {
        // Given
        val vehicle = createTestVehicle("VIN12345678901234")
        coEvery { mockVehicleDao.delete(any()) } returns Unit

        // When
        repository.deleteVehicle(vehicle)

        // Then
        coVerify { mockVehicleDao.delete(vehicle) }
    }

    @Test
    fun `setSelectedVehicle should clear selection and set new`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        coEvery { mockVehicleDao.clearSelection() } returns Unit
        coEvery { mockVehicleDao.setSelected(any()) } returns Unit

        // When
        repository.setSelectedVehicle(vin)

        // Then
        coVerify { mockVehicleDao.clearSelection() }
        coVerify { mockVehicleDao.setSelected(vin) }
    }

    @Test
    fun `updateMileage should update mileage in dao`() = runTest {
        // Given
        val vin = "VIN12345678901234"
        val mileage = 50000
        coEvery { mockVehicleDao.updateMileage(any(), any()) } returns Unit

        // When
        repository.updateMileage(vin, mileage)

        // Then
        coVerify { mockVehicleDao.updateMileage(vin, mileage) }
    }

    @Test
    fun `getVehiclesByBrand should return filtered vehicles`() = runTest {
        // Given
        val vazVehicles = listOf(
            createTestVehicle("VIN1", brand = VehicleBrand.VAZ),
            createTestVehicle("VIN2", brand = VehicleBrand.VAZ)
        )
        coEvery { mockVehicleDao.getByBrand(VehicleBrand.VAZ) } returns flowOf(vazVehicles)

        // When
        val result = repository.getVehiclesByBrand(VehicleBrand.VAZ).first()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.brand == VehicleBrand.VAZ })
    }

    @Test
    fun `vehicle data should be correctly mapped`() = runTest {
        // Given
        val vehicle = Vehicle(
            vin = "VIN12345678901234",
            brand = VehicleBrand.VAZ,
            model = "Granta",
            year = 2020,
            engineType = "1.6 8V",
            engineCode = "11183",
            engineVolume = 1.6f,
            fuelType = FuelType.PETROL_92,
            transmission = TransmissionType.MANUAL_5,
            mileage = 45000,
            isSelected = true
        )
        coEvery { mockVehicleDao.getByVin(any()) } returns vehicle

        // When
        val result = repository.getVehicleByVin(vehicle.vin)

        // Then
        assertNotNull(result)
        assertEquals("VIN12345678901234", result?.vin)
        assertEquals(VehicleBrand.VAZ, result?.brand)
        assertEquals("Granta", result?.model)
        assertEquals(2020, result?.year)
        assertEquals(1.6f, result?.engineVolume)
        assertEquals(45000, result?.mileage)
        assertTrue(result?.isSelected == true)
    }

    // Helper methods
    
    private fun createTestVehicle(
        vin: String,
        brand: VehicleBrand = VehicleBrand.VAZ,
        isSelected: Boolean = false,
        mileage: Int = 10000
    ): Vehicle {
        return Vehicle(
            vin = vin,
            brand = brand,
            model = "Test Model",
            year = 2020,
            engineType = "1.6",
            fuelType = FuelType.PETROL_92,
            transmission = TransmissionType.MANUAL_5,
            mileage = mileage,
            isSelected = isSelected
        )
    }
}
