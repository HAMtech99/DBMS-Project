import java.util.ArrayList;

/**
 * VehicleService.java
 * Serves as both the business logic layer and the data access layer for
 * Phase 1 of the Vehicle Inventory Management System. All vehicle records
 * are stored in a private ArrayList held in memory for the duration of
 * the program session.
 * This class is designed with future phases in mind. In Phase 4, the
 * ArrayList will be replaced by a SQLite database, but the method
 * signatures defined here will remain unchanged so that MenuHandler
 * requires no modification when storage is swapped.
 * All input validation logic lives here. MenuHandler reads raw user input
 * and delegates to this class for validation and execution. The DAO layer
 * is never reached with invalid data.
 * Phase applicability: Phase 1 and Phase 2. Modified in Phase 4 to
 * integrate SQLite via JDBC.
 */
public class VehicleService {

    // The sole in-memory data store for all vehicle records in Phase 1.
    // Replaced by a SQLite-backed repository in Phase 4.
    private ArrayList<Vehicle> vehicles;

    // Internal counter used to assign unique sequential IDs to each vehicle.
    // Increments with every save and never resets or reuses a deleted ID.
    private long nextId;

    // Minimum valid model year accepted by the system.
    private static final int MIN_YEAR = 2000;

    // Maximum valid model year accepted by the system.
    private static final int MAX_YEAR = 2026;

    // Minimum valid sale price accepted by the system in US dollars.
    private static final double MIN_PRICE = 2000.00;

    // Maximum valid sale price accepted by the system in US dollars.
    private static final double MAX_PRICE = 500000.00;

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
     * The sold field defaults to false. This method is called after
     * all field validation has already passed in MenuHandler.
     *
     * @param make  the validated manufacturer name
     * @param model the validated model name
     * @param year  the validated model year
     * @param price the validated sale price
     * @return the newly created Vehicle object with its assigned ID
     */
    public Vehicle addVehicle(String make, String model, int year, double price) {
        Vehicle vehicle = new Vehicle(nextId++, make, model, year, price);
        vehicles.add(vehicle);
        return vehicle;
    }

    /**
     * Adds a pre-built Vehicle object directly to the inventory.
     * Used exclusively by FileHandler when loading vehicles from a text
     * file where the ID and sold status are already determined.
     * The nextId counter is updated to stay ahead of any loaded IDs.
     *
     * @param vehicle the fully constructed Vehicle object to add
     * @return the Vehicle object that was added
     */
    public Vehicle addVehicleFromFile(Vehicle vehicle) {
        vehicles.add(vehicle);
        if (vehicle.getId() >= nextId) {
            nextId = vehicle.getId() + 1;
        }
        return vehicle;
    }

    /**
     * Returns all vehicle records currently in the inventory.
     * Returns an empty list if no vehicles have been added yet.
     * Never returns null.
     *
     * @return the full ArrayList of Vehicle objects
     */
    public ArrayList<Vehicle> getAllVehicles() {
        return vehicles;
    }

    /**
     * Finds and returns the vehicle with the matching ID.
     * Returns null if no vehicle with that ID exists in the system.
     *
     * @param id the ID to search for
     * @return the matching Vehicle object, or null if not found
     */
    public Vehicle getVehicleById(long id) {
        for (Vehicle v : vehicles) {
            if (v.getId() == id) {
                return v;
            }
        }
        return null;
    }

    /**
     * Permanently removes the vehicle with the given ID from the inventory.
     * Returns true if the vehicle was found and removed successfully.
     * Returns false if no vehicle with that ID exists. The ID is never reused.
     *
     * @param id the ID of the vehicle to remove
     * @return true if removed, false if not found
     */
    public boolean deleteVehicle(long id) {
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
     * Any parameter passed as null or a sentinel value indicates that
     * field should not be changed. This method is called after all
     * validation has passed in MenuHandler.
     *
     * @param id       the ID of the vehicle to update
     * @param make     the new make, or null to leave unchanged
     * @param model    the new model, or null to leave unchanged
     * @param year     the new year, or -1 to leave unchanged
     * @param price    the new price, or -1 to leave unchanged
     * @return the updated Vehicle object, or null if not found
     */
    public Vehicle updateVehicle(long id, String make, String model, int year, double price) {
        Vehicle vehicle = getVehicleById(id);
        if (vehicle == null) {
            return null;
        }
        if (make  != null)   vehicle.setMake(make);
        if (model != null)   vehicle.setModel(model);
        if (year  != -1)     vehicle.setYear(year);
        if (price != -1)     vehicle.setPrice(price);
        return vehicle;
    }

    /**
     * Marks the vehicle with the given ID as sold.
     * Blocked if the vehicle does not exist or is already marked as sold.
     * Calculates and returns a sale summary after marking the vehicle sold.
     * The vehicle remains in the inventory list with a Sold status.
     *
     * @param id the ID of the vehicle to mark as sold
     * @return a SaleSummary object containing the result and inventory stats,
     *         or null if the vehicle was not found
     */
    public SaleSummary markAsSold(long id) {
        Vehicle vehicle = getVehicleById(id);

        if (vehicle == null) {
            return null;
        }

        if (vehicle.isSold()) {
            return new SaleSummary(vehicle, false, 0, 0, 0.00);
        }

        vehicle.setSold(true);

        int totalVehicles     = vehicles.size();
        int soldCount         = 0;
        int availableCount    = 0;
        double inventoryValue = 0.00;

        for (Vehicle v : vehicles) {
            if (v.isSold()) {
                soldCount++;
            } else {
                availableCount++;
                inventoryValue += v.getPrice();
            }
        }

        return new SaleSummary(vehicle, true, totalVehicles, availableCount, inventoryValue);
    }

    /**
     * Displays all vehicles in the inventory to the console.
     * Prints a message if the inventory is empty.
     * Includes a total count at the bottom of the list.
     */
    public void displayAllVehicles() {
        if (vehicles.isEmpty()) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }
        System.out.println("\n========== Vehicle Inventory ==========");
        for (Vehicle v : vehicles) {
            System.out.println(v);
        }
        System.out.println("Total vehicles: " + vehicles.size());
        System.out.println("=======================================");
    }

    /**
     * Returns the total number of vehicles currently in the inventory.
     *
     * @return the size of the vehicle list
     */
    public int getVehicleCount() {
        return vehicles.size();
    }

    /**
     * Returns true if the given ID does not already exist in the inventory.
     * Used by FileHandler to prevent duplicate IDs when loading from file.
     *
     * @param id the ID to check for uniqueness
     * @return true if the ID is unique, false if it already exists
     */
    public boolean isIdUnique(long id) {
        for (Vehicle v : vehicles) {
            if (v.getId() == id) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the given string is a valid vehicle make.
     * Make must contain only letters and spaces, no numbers or special characters.
     * Vehicle manufacturers such as Toyota, Ford, and BMW are all purely alphabetic.
     *
     * @param value the make string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidMake(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.trim().matches("[a-zA-Z ]+");
    }

    /**
     * Returns true if the given string is a valid vehicle model.
     * Model names may contain letters, numbers, hyphens, and spaces but
     * must include at least one letter. This allows real model names such
     * as M2, F-150, CX-5, and A4 while rejecting purely numeric input.
     *
     * @param value the model string to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidModel(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return value.trim().matches("(?=.*[a-zA-Z])[a-zA-Z0-9\\- ]+");
    }

    /**
     * Returns true if the given year falls within the accepted range
     * of 2000 to 2026 inclusive.
     *
     * @param year the year to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidYear(int year) {
        return year >= MIN_YEAR && year <= MAX_YEAR;
    }

    /**
     * Returns true if the given price falls within the accepted range
     * of $2,000.00 to $500,000.00 inclusive.
     *
     * @param price the price to validate
     * @return true if valid, false otherwise
     */
    public boolean isValidPrice(double price) {
        return price >= MIN_PRICE && price <= MAX_PRICE;
    }

    /**
     * Returns the minimum accepted model year for informational purposes.
     *
     * @return the minimum year constant
     */
    public int getMinYear() {
        return MIN_YEAR;
    }

    /**
     * Returns the maximum accepted model year for informational purposes.
     *
     * @return the maximum year constant
     */
    public int getMaxYear() {
        return MAX_YEAR;
    }

    /**
     * Returns the minimum accepted sale price for informational purposes.
     *
     * @return the minimum price constant
     */
    public double getMinPrice() {
        return MIN_PRICE;
    }

    /**
     * Returns the maximum accepted sale price for informational purposes.
     *
     * @return the maximum price constant
     */
    public double getMaxPrice() {
        return MAX_PRICE;
    }
}