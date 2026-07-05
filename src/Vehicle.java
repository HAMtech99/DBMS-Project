/**
 * Vehicle.java
 * Represents a single vehicle record in the dealership inventory.
 * This class serves as the core data model for the entire application
 * and will remain unchanged across all four phases of development.
 * Each Vehicle object encapsulates six attributes spanning five distinct
 * data types. The id field is assigned by the system and is never entered
 * by the user directly.
 * Phase applicability: All phases (1 through 4).
 */
public class Vehicle {

    // System-generated unique identifier for this vehicle record.
    // Assigned by VehicleService at the time of creation and never reused.
    private final long id;

    // The manufacturer of the vehicle, for example Toyota or Ford.
    // Must contain letters and spaces only and cannot be empty.
    private String make;

    // The specific model name of the vehicle, for example Camry or F-150.
    // Must contain letters and spaces only and cannot be empty.
    private String model;

    // The four-digit model year of the vehicle.
    // Must be between 2000 and 2026 inclusive.
    private int year;

    // The listed sale price of the vehicle in US dollars.
    // Must be between $2,000.00 and $500,000.00 inclusive.
    private double price;

    // Indicates whether this vehicle has been marked as sold.
    // Defaults to false when a vehicle is first created.
    // Once set to true, this field cannot be reversed.
    private boolean sold;

    /**
     * Constructs a fully populated Vehicle object with all six fields.
     * The sold field is not included as a parameter because it always
     * defaults to false at the time of creation. It is set separately
     * through the markAsSold operation.
     *
     * @param id    the system-assigned unique identifier for this vehicle
     * @param make  the manufacturer name
     * @param model the model name
     * @param year  the four-digit model year
     * @param price the listed sale price in US dollars
     */
    public Vehicle(long id, String make, String model, int year, double price) {
        this.id    = id;
        this.make  = make;
        this.model = model;
        this.year  = year;
        this.price = price;
        this.sold  = false;
    }

    /**
     * Constructs a Vehicle object with a pre-existing sold status.
     * Used exclusively by FileHandler when loading vehicles from a text
     * file where the sold field is already specified.
     *
     * @param id    the system-assigned unique identifier
     * @param make  the manufacturer name
     * @param model the model name
     * @param year  the four-digit model year
     * @param price the listed sale price in US dollars
     * @param sold  the existing sold status read from the file
     */
    public Vehicle(long id, String make, String model, int year, double price, boolean sold) {
        this.id    = id;
        this.make  = make;
        this.model = model;
        this.year  = year;
        this.price = price;
        this.sold  = sold;
    }

    // Returns the system-assigned unique identifier for this vehicle.
    public long getId() {
        return id;
    }

    // Returns the manufacturer name of this vehicle.
    public String getMake() {
        return make;
    }

    // Returns the model name of this vehicle.
    public String getModel() {
        return model;
    }

    // Returns the four-digit model year of this vehicle.
    public int getYear() {
        return year;
    }

    // Returns the listed sale price of this vehicle in US dollars.
    public double getPrice() {
        return price;
    }

    // Returns true if this vehicle has been marked as sold, false otherwise.
    public boolean isSold() {
        return sold;
    }

    // Updates the manufacturer name of this vehicle.
    public void setMake(String make) {
        this.make = make;
    }

    // Updates the model name of this vehicle.
    public void setModel(String model) {
        this.model = model;
    }

    // Updates the four-digit model year of this vehicle.
    public void setYear(int year) {
        this.year = year;
    }

    // Updates the listed sale price of this vehicle.
    public void setPrice(double price) {
        this.price = price;
    }

    // Marks this vehicle as sold. Once set to true this should not be reversed.
    public void setSold(boolean sold) {
        this.sold = sold;
    }

    /**
     * Returns a formatted string representation of this vehicle.
     * Used when displaying the inventory list to the user in the CLI.
     * Price is formatted to two decimal places with a dollar sign.
     * Sold status displays as Available or Sold for readability.
     *
     * @return a single formatted line describing this vehicle
     */
    @Override
    public String toString() {
        return String.format(
                "ID: %d | Make: %s | Model: %s | Year: %d | Price: $%,.2f | Status: %s",
                id, make, model, year, price, sold ? "Sold" : "Available"
        );
    }
}