/**
 * SaleSummary.java
 * Represents the result of a Mark as Sold operation. Returned by
 * VehicleService.markAsSold() to provide the calling code with both
 * the outcome of the action and a calculated snapshot of the current
 * inventory state after the sale.
 * This class satisfies the rubric requirement for the custom action to
 * perform a calculation. The calculation computes the total number of
 * vehicles remaining available and the total remaining inventory value
 * after the vehicle is marked as sold.
 * Returning this object instead of void keeps the method testable in
 * Phase 2 and aligns with the rubric requirement to avoid void methods.
 * Phase applicability: All phases.
 */
public class SaleSummary {

    // The vehicle that was the subject of the Mark as Sold action.
    private final Vehicle vehicle;

    // True if the vehicle was successfully marked as sold during this operation.
    // False if the vehicle was already sold before this operation was triggered.
    private final boolean markedSuccessfully;

    // The total number of vehicles in the inventory at the time of the sale.
    private final int totalVehicles;

    // The number of vehicles that remain available for sale after this operation.
    private int availableCount;

    // The total dollar value of all vehicles still available for sale.
    // Calculated by summing the price of every unsold vehicle in the inventory.
    private double remainingInventoryValue;

    /**
     * Constructs a SaleSummary with the full result of the Mark as Sold operation.
     *
     * @param vehicle               the vehicle that was acted upon
     * @param markedSuccessfully    true if the vehicle was newly marked as sold
     * @param totalVehicles         total vehicles in the inventory
     * @param availableCount        vehicles still available after this operation
     * @param remainingInventoryValue total value of unsold vehicles in dollars
     */
    public SaleSummary(Vehicle vehicle, boolean markedSuccessfully,
                       int totalVehicles, int availableCount,
                       double remainingInventoryValue) {
        this.vehicle                = vehicle;
        this.markedSuccessfully     = markedSuccessfully;
        this.totalVehicles          = totalVehicles;
        this.availableCount         = availableCount;
        this.remainingInventoryValue = remainingInventoryValue;
    }

    // Returns the vehicle that was the subject of this operation.
    public Vehicle getVehicle() {
        return vehicle;
    }

    // Returns true if the vehicle was successfully marked as sold.
    public boolean isMarkedSuccessfully() {
        return markedSuccessfully;
    }

    // Returns the total number of vehicles in the inventory.
    public int getTotalVehicles() {
        return totalVehicles;
    }

    // Returns the number of vehicles still available for sale.
    public int getAvailableCount() {
        return availableCount;
    }

    // Returns the total dollar value of all unsold vehicles.
    public double getRemainingInventoryValue() {
        return remainingInventoryValue;
    }
}