/**
 * Vehicle.java. Represents a single vehicle record in the dealership inventory.
 * This class serves as the core data model for the entire application and will
 * remain unchanged across all four phases of development. Each Vehicle object
 * encapsulates eight attributes spanning five distinct data types. The id field
 * is assigned by the system and is never entered by the user directly.
 * Phase applicability: All phases (1 through 4).
 */
public class Vehicle {

    // System-generated unique identifier. Assigned by VehicleService at creation and never reused.
    // Declared final because a vehicle ID must never change once assigned.
    private final long id;

    // The manufacturer of the vehicle, for example Toyota or Ford.
    // Must contain letters and spaces only and cannot be empty.
    private String make;

    // The specific model name, for example Camry or F-150.
    // Must contain letters, numbers, hyphens, and spaces with at least one letter.
    private String model;

    // The four-digit model year. Must be between 2000 and 2026 inclusive.
    private int year;

    // The listed sale price in US dollars. Must be between $2,000.00 and $500,000.00 inclusive.
    private double price;

    // Indicates whether this vehicle has been marked as sold.
    // Defaults to false at creation. Once set to true this field is not reversed.
    private boolean sold;

    // The exterior color selected from a predefined list in VehicleService.
    private String color;

    // The odometer reading in miles. Must be between 0 and 300,000 inclusive.
    private int mileage;

    /**
     * Constructs a fully populated Vehicle for manual entry.
     * The sold field defaults to false and is set separately through Mark as Sold.
     * @param id      the system-assigned unique identifier
     * @param make    the manufacturer name
     * @param model   the model name
     * @param year    the four-digit model year
     * @param price   the listed sale price in US dollars
     * @param color   the exterior color from the accepted color list
     * @param mileage the odometer reading in miles
     */
    public Vehicle(long id, String make, String model, int year, double price, String color, int mileage) {
        this.id      = id;
        this.make    = make;
        this.model   = model;
        this.year    = year;
        this.price   = price;
        this.sold    = false;
        this.color   = color;
        this.mileage = mileage;
    }

    /**
     * Constructs a Vehicle with a pre-existing sold status.
     * Used exclusively by FileHandler when loading vehicles from a text file.
     * @param id      the system-assigned unique identifier
     * @param make    the manufacturer name
     * @param model   the model name
     * @param year    the four-digit model year
     * @param price   the listed sale price in US dollars
     * @param sold    the existing sold status read from the file
     * @param color   the exterior color
     * @param mileage the odometer reading in miles
     */
    public Vehicle(long id, String make, String model, int year, double price, boolean sold, String color, int mileage) {
        this.id      = id;
        this.make    = make;
        this.model   = model;
        this.year    = year;
        this.price   = price;
        this.sold    = sold;
        this.color   = color;
        this.mileage = mileage;
    }

    // Returns the system-assigned unique identifier for this vehicle.
    public long getId() { return id; }

    // Returns the manufacturer name of this vehicle.
    public String getMake() { return make; }

    // Returns the model name of this vehicle.
    public String getModel() { return model; }

    // Returns the four-digit model year of this vehicle.
    public int getYear() { return year; }

    // Returns the listed sale price of this vehicle in US dollars.
    public double getPrice() { return price; }

    // Returns true if this vehicle has been marked as sold, false otherwise.
    public boolean isSold() { return sold; }

    // Returns the exterior color of this vehicle.
    public String getColor() { return color; }

    // Returns the odometer reading of this vehicle in miles.
    public int getMileage() { return mileage; }

    // Updates the manufacturer name of this vehicle.
    public void setMake(String make) { this.make = make; }

    // Updates the model name of this vehicle.
    public void setModel(String model) { this.model = model; }

    // Updates the four-digit model year of this vehicle.
    public void setYear(int year) { this.year = year; }

    // Updates the listed sale price of this vehicle.
    public void setPrice(double price) { this.price = price; }

    // Marks this vehicle as sold. Once set to true this should not be reversed.
    public void setSold(boolean sold) { this.sold = sold; }

    // Updates the exterior color of this vehicle.
    public void setColor(String color) { this.color = color; }

    // Updates the odometer reading of this vehicle in miles.
    public void setMileage(int mileage) { this.mileage = mileage; }

    /**
     * Returns a formatted string representation of this vehicle for CLI display.
     * Price is formatted to two decimal places. Mileage uses comma separators.
     * Sold status displays as Available or Sold for readability.
     * @return a single formatted line describing this vehicle
     */
    @Override
    public String toString() {
        return String.format(
                "ID: %d | Make: %s | Model: %s | Year: %d | Color: %s | Mileage: %,d mi | Price: $%,.2f | Status: %s",
                id, make, model, year, color, mileage, price, sold ? "Sold" : "Available"
        );
    }
}