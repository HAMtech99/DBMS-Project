import java.util.Scanner;

/**
 * MenuHandler.java. Owns all user interaction for the Vehicle Inventory Management System.
 * Displays the main menu, reads and validates user input at every prompt, and delegates all
 * business logic to VehicleService. FileHandler is used exclusively when the user chooses to
 * load vehicles from a text file. Every operation can be cancelled by pressing Enter on the
 * first prompt. Once inside an operation, invalid input loops and re-prompts rather than
 * returning to the menu, ensuring users are never penalized for a typo. All user input is read
 * using nextLine() to prevent Scanner buffer issues. Numeric values are parsed manually so the
 * buffer is always clean. A top-level try catch in the run loop acts as a last-resort safety net.
 * Phase applicability: Phase 1 and Phase 2. Replaced by a browser-based interface in Phase 3.
 */
public class MenuHandler {

    // Single Scanner instance shared across all input methods for the program lifetime.
    private final Scanner scanner;

    // The VehicleService instance that owns all business logic and inventory data.
    private final VehicleService service;

    // The FileHandler instance used to load vehicle data from a text file.
    private final FileHandler fileHandler;

    /**
     * Constructs a MenuHandler with the required service and file handler.
     * A single Scanner is created here and shared across all input methods.
     * @param service     the VehicleService instance managing inventory data
     * @param fileHandler the FileHandler instance for loading from file
     */
    public MenuHandler(final VehicleService service, final FileHandler fileHandler) {
        this.service     = service;
        this.fileHandler = fileHandler;
        this.scanner     = new Scanner(System.in);
    }

    /**
     * Starts the main menu loop. Runs indefinitely until the user selects Exit.
     * A top-level try catch wraps every iteration as a last-resort safety net to prevent
     * any unforeseen exception from crashing the program.
     */
    public void run() {
        while (true) {
            try {
                displayMenu();
                final int choice = readMenuChoice();
                routeMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Returning to menu.");
            }
        }
    }

    /**
     * Prints the main menu to the console with all available operations.
     */
    public void displayMenu() {
        System.out.println("\n=================================================");
        System.out.println("       Vehicle Inventory Management System");
        System.out.println("=================================================");
        System.out.println("  1. Load Vehicles from File");
        System.out.println("  2. Display All Vehicles");
        System.out.println("  3. Add Vehicle");
        System.out.println("  4. Delete Vehicle");
        System.out.println("  5. Update Vehicle");
        System.out.println("  6. Mark Vehicle as Sold");
        System.out.println("  7. Exit");
        System.out.println("=================================================");
        System.out.print("Enter your choice: ");
    }

    /**
     * Reads and validates the user's menu selection.
     * Loops until the user enters a valid integer between 1 and 7.
     * @return a valid integer menu choice between 1 and 7
     */
    private int readMenuChoice() {
        while (true) {
            final String input = scanner.nextLine().trim();
            try {
                final int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 7) return choice;
                System.out.print("Invalid option. Please enter a number between 1 and 7: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid option. Please enter a number between 1 and 7: ");
            }
        }
    }

    /**
     * Routes the validated menu choice to the corresponding handler method.
     * @param choice the validated integer menu selection
     */
    public void routeMenuChoice(final int choice) {
        switch (choice) {
            case 1: handleLoadFromFile();  break;
            case 2: handleDisplayAll();    break;
            case 3: handleAddVehicle();    break;
            case 4: handleDeleteVehicle(); break;
            case 5: handleUpdateVehicle(); break;
            case 6: handleMarkAsSold();    break;
            case 7: handleExit();          break;
        }
    }

    /**
     * Handles option 1: Load vehicles from a text file.
     * Pressing Enter cancels. Delegates to FileHandler and displays inventory after a successful load.
     */
    public void handleLoadFromFile() {
        System.out.print("\nEnter the path to your vehicle data file (or press Enter to cancel): ");
        String path = scanner.nextLine().trim();
        if (path.isEmpty()) { System.out.println("Load cancelled. Returning to menu."); return; }

        while (!fileHandler.loadFromFile(path, service)) {
            System.out.print("Re-enter path (or press Enter to cancel): ");
            path = scanner.nextLine().trim();
            if (path.isEmpty()) { System.out.println("Load cancelled. Returning to menu."); return; }
        }

        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 2: Display all vehicles. Delegates to VehicleService.
     */
    public void handleDisplayAll() {
        service.displayAllVehicles();
    }

    /**
     * Handles option 3: Add a new vehicle manually.
     * Pressing Enter on the make prompt cancels the operation.
     * All subsequent invalid inputs loop and re-prompt. Displays inventory after a successful add.
     */
    public void handleAddVehicle() {
        System.out.println("\nPress Enter at the make prompt to cancel and return to the menu.");

        final String make = readMake("Enter vehicle make: ");
        if (make == null) { printCancelled("Add"); return; }

        final String model   = readModel("Enter vehicle model: ");
        final int year       = readYear();
        final double price   = readPrice();
        final String color   = readColor();
        final int mileage    = readMileage();

        final Vehicle vehicle = service.addVehicle(make, model, year, price, color, mileage);
        System.out.println("\nVehicle added successfully:");
        System.out.println(vehicle);
        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 4: Delete a vehicle by ID.
     * Pressing Enter on the ID prompt cancels. Invalid IDs and IDs not found loop and re-prompt.
     * The yes/no confirmation loops until a valid response is received.
     * Sold vehicles cannot be deleted. Displays inventory after a successful deletion.
     */
    public void handleDeleteVehicle() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to delete (or press Enter to cancel): ");
        final String input = scanner.nextLine().trim();
        if (input.isEmpty()) { printCancelled("Delete"); return; }

        final Vehicle vehicle = readVehicleById(input);
        if (vehicle == null) { printCancelled("Delete"); return; }

        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + vehicle.getId() + " is marked as sold and cannot be deleted.");
            return;
        }

        System.out.println("Vehicle found: " + vehicle);
        final boolean confirmed = readYesNo("Are you sure you want to delete this vehicle? (yes/no): ");

        if (confirmed) {
            service.deleteVehicle(vehicle.getId());
            System.out.println("Vehicle ID " + vehicle.getId() + " has been permanently removed from the inventory.");
        } else {
            System.out.println("Delete cancelled.");
        }

        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 5: Update any field of an existing vehicle.
     * Pressing Enter on the ID prompt cancels. Invalid IDs loop and re-prompt.
     * After all fields are read a before and after summary is shown. The yes/no confirmation
     * loops until a valid response is received. Sold vehicles cannot be updated.
     * Displays inventory after a successful update.
     */
    public void handleUpdateVehicle() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to update (or press Enter to cancel): ");
        final String input = scanner.nextLine().trim();
        if (input.isEmpty()) { printCancelled("Update"); return; }

        final Vehicle vehicle = readVehicleById(input);
        if (vehicle == null) { printCancelled("Update"); return; }

        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + vehicle.getId() + " is marked as sold and cannot be updated.");
            return;
        }

        System.out.println("Vehicle found: " + vehicle);
        System.out.println("Press Enter on any field to leave it unchanged.");

        final String newMake   = readOptionalMake("Enter new make [" + vehicle.getMake() + "]: ");
        final String newModel  = readOptionalModel("Enter new model [" + vehicle.getModel() + "]: ");
        final int newYear      = readOptionalYear("Enter new year [" + vehicle.getYear() + "]: ");
        final double newPrice  = readOptionalPrice("Enter new price [" + String.format("$%,.2f", vehicle.getPrice()) + "]: ");
        final String newColor  = readOptionalColor("Enter new color [" + vehicle.getColor() + "]: ");
        final int newMileage   = readOptionalMileage("Enter new mileage [" + String.format("%,d", vehicle.getMileage()) + " mi]: ");

        if (newMake == null && newModel == null && newYear == -1
                && newPrice == -1 && newColor == null && newMileage == -1) {
            System.out.println("No changes entered. Update cancelled.");
            return;
        }

        System.out.println("\n--- Proposed Changes ---");
        System.out.println("Make    : " + vehicle.getMake()  + " -> " + (newMake  != null ? newMake  : vehicle.getMake()  + " (unchanged)"));
        System.out.println("Model   : " + vehicle.getModel() + " -> " + (newModel != null ? newModel : vehicle.getModel() + " (unchanged)"));
        System.out.println("Year    : " + vehicle.getYear()  + " -> " + (newYear  != -1   ? newYear  : vehicle.getYear()  + " (unchanged)"));
        System.out.println("Price   : $" + String.format("%,.2f", vehicle.getPrice()) + " -> "
                + (newPrice != -1 ? "$" + String.format("%,.2f", newPrice) : "$" + String.format("%,.2f", vehicle.getPrice()) + " (unchanged)"));
        System.out.println("Color   : " + vehicle.getColor() + " -> " + (newColor != null ? newColor : vehicle.getColor() + " (unchanged)"));
        System.out.println("Mileage : " + String.format("%,d", vehicle.getMileage()) + " mi -> "
                + (newMileage != -1 ? String.format("%,d", newMileage) + " mi" : String.format("%,d", vehicle.getMileage()) + " mi (unchanged)"));
        System.out.println("------------------------");

        final boolean confirmed = readYesNo("Apply these changes? (yes/no): ");
        if (!confirmed) { System.out.println("Update cancelled. No changes were made."); return; }

        final Vehicle updated = service.updateVehicle(vehicle.getId(), newMake, newModel, newYear, newPrice, newColor, newMileage);
        System.out.println("\nVehicle updated successfully:");
        System.out.println(updated);
        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 6: Mark a vehicle as sold.
     * Pressing Enter on the ID prompt cancels. Invalid IDs loop and re-prompt.
     * The yes/no confirmation loops until a valid response is received.
     * Displays a sale summary with inventory statistics after marking sold.
     */
    public void handleMarkAsSold() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to mark as sold (or press Enter to cancel): ");
        final String input = scanner.nextLine().trim();
        if (input.isEmpty()) { printCancelled("Mark as Sold"); return; }

        final Vehicle vehicle = readVehicleById(input);
        if (vehicle == null) { printCancelled("Mark as Sold"); return; }

        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + vehicle.getId() + " is already marked as sold. No changes made.");
            return;
        }

        System.out.println("Vehicle found: " + vehicle);
        final boolean confirmed = readYesNo("Are you sure you want to mark this vehicle as sold? (yes/no): ");
        if (!confirmed) { System.out.println("Mark as Sold cancelled. No changes were made."); return; }

        final SaleSummary summary = service.markAsSold(vehicle.getId());
        if (summary == null || !summary.isMarkedSuccessfully()) {
            System.out.println("Unable to mark vehicle as sold. Please try again.");
            return;
        }

        System.out.println("\nVehicle successfully marked as sold:");
        System.out.println(summary.getVehicle());
        System.out.println("\n--- Sale Summary ---");
        System.out.println("Total vehicles in inventory : " + summary.getTotalVehicles());
        System.out.println("Vehicles still available    : " + summary.getAvailableCount());
        System.out.println("Remaining inventory value   : $" + String.format("%,.2f", summary.getRemainingInventoryValue()));
        System.out.println("--------------------");
        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 7: Exit the program cleanly.
     */
    public void handleExit() {
        System.out.println("Exiting Vehicle Inventory Management System. Goodbye.");
        System.exit(0);
    }

    /**
     * Reads and validates a required make field.
     * Pressing Enter returns null to signal cancellation of the entire operation.
     * Non-empty input that fails validation loops and re-prompts.
     * @param prompt the message to display before reading input
     * @return the validated make string, or null if the user cancelled
     */
    private String readMake(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            if (service.isValidMake(input)) return input;
            System.out.println("Invalid make. Must contain letters and spaces only. No numbers or special characters.");
        }
    }

    /**
     * Reads and validates a required model field.
     * Loops until a valid model is entered. Empty input re-prompts.
     * @param prompt the message to display before reading input
     * @return the validated model string
     */
    private String readModel(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) { System.out.println("Model cannot be empty."); continue; }
            if (service.isValidModel(input)) return input;
            System.out.println("Invalid model. Must contain at least one letter. Letters, numbers, hyphens, and spaces are allowed.");
        }
    }

    /**
     * Reads and validates a required year field.
     * Loops until a valid year between 2000 and 2026 is entered.
     * @return the validated year as an integer
     */
    private int readYear() {
        while (true) {
            System.out.print("Enter vehicle year (" + service.getMinYear() + " to " + service.getMaxYear() + "): ");
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) { System.out.println("Year cannot be empty."); continue; }
            try {
                final int year = Integer.parseInt(input);
                if (service.isValidYear(year)) return year;
                System.out.println("Invalid year. Must be between " + service.getMinYear() + " and " + service.getMaxYear() + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for the year.");
            }
        }
    }

    /**
     * Reads and validates a required price field.
     * Loops until a valid price between $2,000.00 and $500,000.00 is entered.
     * @return the validated price as a double
     */
    private double readPrice() {
        while (true) {
            System.out.print("Enter vehicle price ($" + String.format("%,.2f", service.getMinPrice())
                    + " to $" + String.format("%,.2f", service.getMaxPrice()) + "): ");
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) { System.out.println("Price cannot be empty."); continue; }
            try {
                final double price = Double.parseDouble(input);
                if (service.isValidPrice(price)) return price;
                System.out.println("Invalid price. Must be between $"
                        + String.format("%,.2f", service.getMinPrice())
                        + " and $" + String.format("%,.2f", service.getMaxPrice()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric price value.");
            }
        }
    }

    /**
     * Reads and validates a required color field from the predefined list.
     * Displays valid options and loops until one is selected.
     * @return the validated color string matching a value in the accepted list
     */
    private String readColor() {
        System.out.println("Valid colors: " + service.getValidColors());
        while (true) {
            System.out.print("Enter vehicle color: ");
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) { System.out.println("Color cannot be empty. Please select a color from the list."); continue; }
            if (service.isValidColor(input)) return capitalizeFirst(input);
            System.out.println("Invalid color. Please choose from: " + service.getValidColors());
        }
    }

    /**
     * Reads and validates a required mileage field.
     * Loops until a valid mileage between 0 and 300,000 is entered.
     * @return the validated mileage as an integer
     */
    private int readMileage() {
        while (true) {
            System.out.print("Enter vehicle mileage (" + service.getMinMileage()
                    + " to " + String.format("%,d", service.getMaxMileage()) + " miles): ");
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) { System.out.println("Mileage cannot be empty."); continue; }
            try {
                final int mileage = Integer.parseInt(input);
                if (service.isValidMileage(mileage)) return mileage;
                System.out.println("Invalid mileage. Must be between " + service.getMinMileage()
                        + " and " + String.format("%,d", service.getMaxMileage()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for mileage.");
            }
        }
    }

    /**
     * Reads an optional make field during an update operation.
     * Pressing Enter returns null to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new make string, or null to leave unchanged
     */
    private String readOptionalMake(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            if (service.isValidMake(input)) return input;
            System.out.println("Invalid make. Must contain letters and spaces only.");
        }
    }

    /**
     * Reads an optional model field during an update operation.
     * Pressing Enter returns null to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new model string, or null to leave unchanged
     */
    private String readOptionalModel(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            if (service.isValidModel(input)) return input;
            System.out.println("Invalid model. Must contain at least one letter.");
        }
    }

    /**
     * Reads an optional year field during an update operation.
     * Pressing Enter returns -1 to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new year, or -1 to leave unchanged
     */
    private int readOptionalYear(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1;
            try {
                final int year = Integer.parseInt(input);
                if (service.isValidYear(year)) return year;
                System.out.println("Invalid year. Must be between " + service.getMinYear() + " and " + service.getMaxYear() + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for the year.");
            }
        }
    }

    /**
     * Reads an optional price field during an update operation.
     * Pressing Enter returns -1 to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new price, or -1 to leave unchanged
     */
    private double readOptionalPrice(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1;
            try {
                final double price = Double.parseDouble(input);
                if (service.isValidPrice(price)) return price;
                System.out.println("Invalid price. Must be between $"
                        + String.format("%,.2f", service.getMinPrice())
                        + " and $" + String.format("%,.2f", service.getMaxPrice()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric price value.");
            }
        }
    }

    /**
     * Reads an optional color field during an update operation.
     * Pressing Enter returns null to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new color string, or null to leave unchanged
     */
    private String readOptionalColor(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            if (service.isValidColor(input)) return capitalizeFirst(input);
            System.out.println("Invalid color. Please choose from: " + service.getValidColors());
        }
    }

    /**
     * Reads an optional mileage field during an update operation.
     * Pressing Enter returns -1 to signal that this field should remain unchanged.
     * @param prompt the message including the current value
     * @return the new mileage, or -1 to leave unchanged
     */
    private int readOptionalMileage(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim();
            if (input.isEmpty()) return -1;
            try {
                final int mileage = Integer.parseInt(input);
                if (service.isValidMileage(mileage)) return mileage;
                System.out.println("Invalid mileage. Must be between " + service.getMinMileage()
                        + " and " + String.format("%,d", service.getMaxMileage()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for mileage.");
            }
        }
    }

    /**
     * Reads a vehicle ID from the given initial input and loops if invalid or not found.
     * Returns the matching Vehicle if found, or null if the user presses Enter to cancel.
     * @param initialInput the first input string already read before calling this method
     * @return the matching Vehicle object, or null if the user cancelled
     */
    private Vehicle readVehicleById(final String initialInput) {
        String input = initialInput;
        while (true) {
            final long id;
            try {
                id = Long.parseLong(input);
                if (id <= 0) {
                    System.out.print("Invalid ID. Must be a positive whole number. Re-enter (or press Enter to cancel): ");
                    input = scanner.nextLine().trim();
                    if (input.isEmpty()) return null;
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.print("Invalid ID. Must be a positive whole number. Re-enter (or press Enter to cancel): ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return null;
                continue;
            }

            final Vehicle vehicle = service.getVehicleById(id);
            if (vehicle == null) {
                System.out.print("No vehicle with ID " + id + " was found. Re-enter (or press Enter to cancel): ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) return null;
                continue;
            }
            return vehicle;
        }
    }

    /**
     * Reads a yes or no response and loops until one is provided.
     * Accepts yes or no case-insensitively. Any other input re-prompts.
     * @param prompt the confirmation question to display
     * @return true if the user entered yes, false if the user entered no
     */
    private boolean readYesNo(final String prompt) {
        while (true) {
            System.out.print(prompt);
            final String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("yes")) return true;
            if (input.equals("no"))  return false;
            System.out.println("Please enter yes or no.");
        }
    }

    /**
     * Capitalizes the first letter of a string and lowercases the rest.
     * Used to normalize color input so storage is consistent regardless of how the user typed it.
     * @param value the string to capitalize
     * @return the normalized string with first letter capitalized
     */
    private String capitalizeFirst(final String value) {
        if (value == null || value.isEmpty()) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    // Prints a standardized cancellation message for the given operation name.
    private void printCancelled(final String operation) {
        System.out.println(operation + " cancelled. Returning to menu.");
    }
}