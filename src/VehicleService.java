import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * VehicleService.java. Serves as both the business logic layer and the data access layer for
 * Phase 1 and Phase 2 of the Vehicle Inventory Management System. All vehicle records are stored
 * in a private ArrayList held in memory for the duration of the program session. This class is
 * designed with future phases in mind. In Phase 4, the ArrayList will be replaced by a SQLite
 * database, but the method signatures defined here will remain unchanged so that MenuHandler
 * requires no modification when storage is swapped. All input validation logic lives here.
 * MenuHandler reads raw user input and delegates to this class for validation and execution.
 * The DAO layer is never reached with invalid data.
 * Phase applicability: Phase 1 and Phase 2. Modified in Phase 4 to integrate SQLite via JDBC.
 */
public class VehicleService {

    // The sole in-memory data store for all vehicle records in Phase 1 and 2. Replaced by SQLite in Phase 4.
    private final ArrayList<Vehicle> vehicles;

    // Internal counter used to assign unique sequential IDs. Never resets or reuses a deleted ID.
    private long nextId;

    // Minimum valid model year accepted by the system.
    private static final int MIN_YEAR = 2000;

    // Maximum valid model year accepted by the system.
    private static final int MAX_YEAR = 2026;

    // Minimum valid sale price in US dollars.
    private static final double MIN_PRICE = 2000.00;

    // Maximum valid sale price in US dollars.
    private static final double MAX_PRICE = 500000.00;

    // Minimum valid mileage in miles.
    private static final int MIN_MILEAGE = 0;

    // Maximum valid mileage in miles.
    private static final int MAX_MILEAGE = 300000;

    // Predefined list of accepted exterior colors. In Phase 3 this list will populate
    // a dropdown selector in the GUI, replacing the need for manual text entry entirely.
    private static final List<String> VALID_COLORS = Arrays.asList(
            "White", "Black", "Gray", "Silver", "Red", "Blue",
            "Green", "Yellow", "Orange", "Brown", "Gold", "Purple", "Beige"
    );

    /**
     * Constructs a new VehicleService with an empty vehicle list.
     * The ID counter starts at 1 and increments with each vehicle added.
     */
    public VehicleService() {
        vehicles = new ArrayList<>();
        nextId   = 1;
    }

    /**
     * Adds a new vehicle to the inventory with a system-assigned ID.
     * The sold field defaults to false. Called after all field validation has passed in MenuHandler.
     * @param make    the validated manufacturer name
     * @param model   the validated model name
     * @param year    the validated model year
     * @param price   the validated sale price
     * @param color   the validated exterior color
     * @param mileage the validated odometer reading
     * @return the newly created Vehicle object with its assigned ID
     */
    public Vehicle addVehicle(String make, String model, int year, double price, String color, int mileage) {
        final Vehicle vehicle = new Vehicle(nextId++, make, model, year, price, color, mileage);
        vehicles.add(vehicle);
        return vehicle;
    }

    /**
     * Adds a pre-built Vehicle object directly to the inventory.
     * Used exclusively by FileHandler when loading vehicles from a text file.
     * The nextId counter is updated to stay ahead of any loaded IDs.
     * @param vehicle the fully constructed Vehicle object to add
     * @return the Vehicle object that was added
     */
    public Vehicle addVehicleFromFile(final Vehicle vehicle) {
        vehicles.add(vehicle);
        if (vehicle.getId() >= nextId) {
            nextId = vehicle.getId() + 1;
        }
        return vehicle;
    }

    /**
     * Returns all vehicle records currently in the inventory.
     * Returns an empty list if no vehicles have been added. Never returns null.
     * @return the full ArrayList of Vehicle objects
     */
    public ArrayList<Vehicle> getAllVehicles() {
        return vehicles;
    }

    /**
     * Finds and returns the vehicle with the matching ID.
     * Returns null if no vehicle with that ID exists in the system.
     * @param id the ID to search for
     * @return the matching Vehicle object, or null if not found
     */
    public Vehicle getVehicleById(final long id) {
        for (final Vehicle v : vehicles) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Permanently removes the vehicle with the given ID from the inventory.
     * Returns true if found and removed. Returns false if not found. The ID is never reused.
     * @param id the ID of the vehicle to remove
     * @return true if removed, false if not found
     */
    public boolean deleteVehicle(final long id) {
        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getId() == id) {
                vehicles.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates one or more fields of the vehicle with the given ID.
     * Any parameter passed as null or a sentinel value indicates that field should not be changed.
     * Called after all validation has passed in MenuHandler.
     * @param id      the ID of the vehicle to update
     * @param make    the new make, or null to leave unchanged
     * @param model   the new model, or null to leave unchanged
     * @param year    the new year, or -1 to leave unchanged
     * @param price   the new price, or -1 to leave unchanged
     * @param color   the new color, or null to leave unchanged
     * @param mileage the new mileage, or -1 to leave unchanged
     * @return the updated Vehicle object, or null if not found
     */
    public Vehicle updateVehicle(final long id, final String make, final String model,
                                 final int year, final double price, final String color, final int mileage) {
        final Vehicle vehicle = getVehicleById(id);
        if (vehicle == null) return null;
        if (make    != null) vehicle.setMake(make);
        if (model   != null) vehicle.setModel(model);
        if (year    != -1)   vehicle.setYear(year);
        if (price   != -1)   vehicle.setPrice(price);
        if (color   != null) vehicle.setColor(color);
        if (mileage != -1)   vehicle.setMileage(mileage);
        return vehicle;
    }

    /**
     * Marks the vehicle with the given ID as sold. Blocked if the vehicle does not exist or is
     * already sold. Calculates and returns a SaleSummary with inventory statistics after the sale.
     * The vehicle remains in the inventory list with a Sold status.
     * @param id the ID of the vehicle to mark as sold
     * @return a SaleSummary containing the result and inventory stats, or null if not found
     */
    public SaleSummary markAsSold(final long id) {
        final Vehicle vehicle = getVehicleById(id);
        if (vehicle == null) return null;
        if (vehicle.isSold()) return new SaleSummary(vehicle, false, 0, 0, 0.00);

        vehicle.setSold(true);

        int totalVehicles     = vehicles.size();
        int availableCount    = 0;
        double inventoryValue = 0.00;

        for (final Vehicle v : vehicles) {
            if (!v.isSold()) {
                availableCount++;
                inventoryValue += v.getPrice();
            }
        }

        return new SaleSummary(vehicle, true, totalVehicles, availableCount, inventoryValue);
    }

    /**
     * Displays all vehicles in the inventory to the console.
     * Prints a message if the inventory is empty. Includes a total count at the bottom.
     */
    public void displayAllVehicles() {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }
        System.out.println("\n========== Vehicle Inventory ==========");
        for (final Vehicle v : vehicles) {
            System.out.println(v);
        }
        System.out.println("Total vehicles: " + vehicles.size());
        System.out.println("=======================================");
    }

    /**
     * Returns the total number of vehicles currently in the inventory.
     * @return the size of the vehicle list
     */
    public int getVehicleCount() {
        return vehicles.size();
    }

    /**
     * Returns true if the given ID does not already exist in the inventory.
     * Used by FileHandler to prevent duplicate IDs when loading from file.
     * @param id the ID to check for uniqueness
     * @return true if the ID is unique, false if it already exists
     */
    public boolean isIdUnique(final long id) {
        for (final Vehicle v : vehicles) {
            if (v.getId() == id) return false;
        }
        return true;
    }

    /**
     * Returns true if the given string is a valid vehicle make.
     * Make must contain only letters and spaces. No numbers or special characters.
     * @param value the make string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMake(final String value) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.trim().matches("[a-zA-Z ]+");
    }

    /**
     * Returns true if the given string is a valid vehicle model.
     * Must contain at least one letter. Allows letters, numbers, hyphens, and spaces.
     * This permits real model names such as M2, F-150, and CX-5 while rejecting purely numeric input.
     * @param value the model string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidModel(final String value) {
        if (value == null || value.trim().isEmpty()) return false;
        return value.trim().matches("(?=.*[a-zA-Z])[a-zA-Z0-9\\- ]+");
    }

    /**
     * Returns true if the given year falls within the accepted range of 2000 to 2026 inclusive.
     * @param year the year to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidYear(final int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }

    /**
     * Returns true if the given price falls within the accepted range of $2,000.00 to $500,000.00 inclusive.
     * @param price the price to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPrice(final double price) {
        return price >= MIN_PRICE && price <= MAX_PRICE;
    }

    /**
     * Returns true if the given color matches one of the predefined accepted colors case-insensitively.
     * In Phase 3 this list will become a dropdown selector in the browser GUI.
     * @param color the color string to validate
     * @return true if the color is in the accepted list, false otherwise
     */
    public boolean isValidColor(final String color) {
        if (color == null || color.trim().isEmpty()) return false;
        for (final String valid : VALID_COLORS) {
            if (valid.equalsIgnoreCase(color.trim())) return true;
        }
        return false;
    }

    /**
     * Returns true if the given mileage falls within the accepted range of 0 to 300,000 inclusive.
     * @param mileage the mileage to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMileage(final int mileage) {
        return mileage >= MIN_MILEAGE && mileage <= MAX_MILEAGE;
    }

    // Returns the predefined list of accepted color values. Used by MenuHandler to display options.
    public List<String> getValidColors() { return VALID_COLORS; }

    // Returns the minimum accepted model year.
    public int getMinYear() { return MIN_YEAR; }

    // Returns the maximum accepted model year.
    public int getMaxYear() { return MAX_YEAR; }

    // Returns the minimum accepted sale price.
    public double getMinPrice() { return MIN_PRICE; }

    // Returns the maximum accepted sale price.
    public double getMaxPrice() { return MAX_PRICE; }

    // Returns the minimum accepted mileage.
    public int getMinMileage() { return MIN_MILEAGE; }

    // Returns the maximum accepted mileage.
    public int getMaxMileage() { return MAX_MILEAGE; }
}