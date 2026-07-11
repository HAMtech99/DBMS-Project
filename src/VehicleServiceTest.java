import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * VehicleServiceTest.java. Unit test class for VehicleService covering all five operations
 * required by the Phase 2 rubric: adding, removing, updating, the custom Mark as Sold action,
 * and file loading. Each operation has a happy path test verifying correct behavior with valid
 * input, and at least one edge case test verifying graceful handling of invalid or unexpected input.
 * Tests use JUnit 5 and follow the Arrange, Act, Assert pattern. A fresh VehicleService and
 * FileHandler instance is created before every test via @BeforeEach so no test affects another.
 * Phase applicability: Phase 2.
 */
public class VehicleServiceTest {

    // Fresh VehicleService instance created before every test.
    private VehicleService service;

    // Fresh FileHandler instance created before every test.
    private FileHandler fileHandler;

    /**
     * Runs before every test method. Creates a clean VehicleService and FileHandler
     * so each test starts with an empty inventory and no shared state.
     */
    @BeforeEach
    public void setUp() {
        service     = new VehicleService();
        fileHandler = new FileHandler();
    }

    // =========================================================
    // ADD VEHICLE TESTS
    // =========================================================

    /**
     * Happy path: Verifies a vehicle with valid input is added and returned with all fields correct.
     * Confirms sold defaults to false and the list size increases by one.
     */
    @Test
    public void addVehicle_validInput_shouldReturnVehicleWithCorrectFields() {
        final Vehicle result = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);

        assertNotNull(result);
        assertEquals("Toyota", result.getMake());
        assertEquals("Camry",  result.getModel());
        assertEquals(2022,     result.getYear());
        assertEquals(25000.00, result.getPrice());
        assertEquals("White",  result.getColor());
        assertEquals(15000,    result.getMileage());
        assertFalse(result.isSold());
        assertEquals(1, service.getVehicleCount());
    }

    /**
     * Edge case: Verifies that adding multiple vehicles assigns unique sequential IDs to each.
     * No two vehicles should ever share the same ID.
     */
    @Test
    public void addVehicle_multipleVehicles_shouldReceiveUniqueIds() {
        final Vehicle v1 = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final Vehicle v2 = service.addVehicle("Ford",   "F-150", 2021, 45000.00, "Black", 30000);
        final Vehicle v3 = service.addVehicle("BMW",    "M2",    2023, 62000.00, "Gray",  5000);

        assertNotEquals(v1.getId(), v2.getId());
        assertNotEquals(v2.getId(), v3.getId());
        assertNotEquals(v1.getId(), v3.getId());
        assertEquals(3, service.getVehicleCount());
    }

    /**
     * Edge case: Verifies that a make containing only numbers fails validation.
     * A make must contain letters only.
     */
    @Test
    public void isValidMake_numericOnly_shouldReturnFalse() {
        assertFalse(service.isValidMake("123"));
    }

    /**
     * Edge case: Verifies that adding a vehicle and deleting it does not cause
     * the next vehicle's ID to reuse the deleted ID.
     */
    @Test
    public void addVehicle_afterDeletion_shouldNotReuseDeletedId() {
        final Vehicle v1 = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final long deletedId = v1.getId();
        service.deleteVehicle(deletedId);

        final Vehicle v2 = service.addVehicle("Ford", "F-150", 2021, 45000.00, "Black", 30000);

        assertNotEquals(deletedId, v2.getId());
    }

    // =========================================================
    // REMOVE VEHICLE TESTS
    // =========================================================

    /**
     * Happy path: Verifies a vehicle with a valid ID is permanently removed.
     * Returns true and the vehicle can no longer be found by ID after deletion.
     */
    @Test
    public void deleteVehicle_validId_shouldReturnTrueAndRemoveVehicle() {
        final Vehicle vehicle = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final long id = vehicle.getId();

        final boolean result = service.deleteVehicle(id);

        assertTrue(result);
        assertNull(service.getVehicleById(id));
        assertEquals(0, service.getVehicleCount());
    }

    /**
     * Edge case: Verifies that attempting to delete a nonexistent ID returns false
     * without throwing an exception. The inventory must remain unchanged.
     */
    @Test
    public void deleteVehicle_nonexistentId_shouldReturnFalseWithoutCrashing() {
        service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final int countBefore = service.getVehicleCount();

        final boolean result = service.deleteVehicle(999L);

        assertFalse(result);
        assertEquals(countBefore, service.getVehicleCount());
    }

    // =========================================================
    // UPDATE VEHICLE TESTS
    // =========================================================

    /**
     * Happy path: Verifies that updating a vehicle's price reflects the new value.
     * All other fields must remain unchanged.
     */
    @Test
    public void updateVehicle_validPrice_shouldReturnVehicleWithUpdatedPrice() {
        final Vehicle vehicle = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final long id = vehicle.getId();

        final Vehicle updated = service.updateVehicle(id, null, null, -1, 27000.00, null, -1);

        assertNotNull(updated);
        assertEquals(27000.00, updated.getPrice());
        assertEquals("Toyota", updated.getMake());
        assertEquals("Camry",  updated.getModel());
        assertEquals(2022,     updated.getYear());
        assertEquals("White",  updated.getColor());
        assertEquals(15000,    updated.getMileage());
    }

    /**
     * Edge case: Verifies that attempting to update a vehicle with a nonexistent ID returns null.
     */
    @Test
    public void updateVehicle_nonexistentId_shouldReturnNull() {
        assertNull(service.updateVehicle(999L, "Honda", null, -1, -1, null, -1));
    }

    /**
     * Edge case: Verifies that passing all null or sentinel values leaves the vehicle unchanged.
     */
    @Test
    public void updateVehicle_noFieldsChanged_shouldReturnVehicleUnchanged() {
        final Vehicle vehicle = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final Vehicle result  = service.updateVehicle(vehicle.getId(), null, null, -1, -1, null, -1);

        assertNotNull(result);
        assertEquals("Toyota", result.getMake());
        assertEquals("Camry",  result.getModel());
        assertEquals(2022,     result.getYear());
        assertEquals(25000.00, result.getPrice());
        assertEquals("White",  result.getColor());
        assertEquals(15000,    result.getMileage());
    }

    // =========================================================
    // MARK AS SOLD TESTS (CUSTOM ACTION)
    // =========================================================

    /**
     * Happy path: Verifies marking an available vehicle as sold sets sold to true and returns
     * a SaleSummary with correct inventory calculations reflecting the state after the sale.
     */
    @Test
    public void markAsSold_availableVehicle_shouldReturnSummaryWithSoldTrue() {
        service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        final Vehicle target = service.addVehicle("Ford", "F-150", 2021, 45000.00, "Black", 30000);
        service.addVehicle("BMW", "M2", 2023, 62000.00, "Gray", 5000);

        final SaleSummary summary = service.markAsSold(target.getId());

        assertNotNull(summary);
        assertTrue(summary.isMarkedSuccessfully());
        assertTrue(summary.getVehicle().isSold());
        assertEquals(3, summary.getTotalVehicles());
        assertEquals(2, summary.getAvailableCount());
        assertEquals(25000.00 + 62000.00, summary.getRemainingInventoryValue(), 0.01);
    }

    /**
     * Edge case: Verifies that marking an already sold vehicle returns a SaleSummary
     * with markedSuccessfully set to false. The vehicle remains sold and inventory is unchanged.
     */
    @Test
    public void markAsSold_alreadySoldVehicle_shouldReturnUnsuccessfulSummary() {
        final Vehicle vehicle = service.addVehicle("Toyota", "Camry", 2022, 25000.00, "White", 15000);
        service.markAsSold(vehicle.getId());

        final SaleSummary summary = service.markAsSold(vehicle.getId());

        assertNotNull(summary);
        assertFalse(summary.isMarkedSuccessfully());
        assertTrue(vehicle.isSold());
    }

    /**
     * Edge case: Verifies that marking a nonexistent ID as sold returns null without crashing.
     */
    @Test
    public void markAsSold_nonexistentId_shouldReturnNull() {
        assertNull(service.markAsSold(999L));
    }

    // =========================================================
    // FILE LOADING TESTS
    // =========================================================

    /**
     * Happy path: Verifies that a valid vehicles.txt file loads correctly
     * and all valid records are added to the inventory.
     */
    @Test
    public void loadFromFile_validFile_shouldLoadAllRecords() {
        final boolean result = fileHandler.loadFromFile("vehicles.txt", service);

        assertTrue(result);
        assertTrue(service.getVehicleCount() > 0);
    }

    /**
     * Edge case: Verifies that a nonexistent file path returns false and leaves
     * the inventory empty. No exception should be thrown.
     */
    @Test
    public void loadFromFile_invalidPath_shouldReturnFalseWithoutCrashing() {
        final boolean result = fileHandler.loadFromFile("nonexistent_file.txt", service);

        assertFalse(result);
        assertEquals(0, service.getVehicleCount());
    }
}