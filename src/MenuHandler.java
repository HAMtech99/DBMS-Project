import java.util.Scanner;

/**
 * MenuHandler.java
 * Owns all user interaction for the Vehicle Inventory Management System.
 * Displays the main menu, reads and validates user input at every prompt,
 * and delegates all business logic to VehicleService. FileHandler is used
 * exclusively when the user chooses to load vehicles from a text file.
 * Every operation in this class can be cancelled at any time by pressing
 * Enter on any prompt without typing a value. This returns the user to
 * the main menu immediately without making any changes to the inventory.
 * All user input is read using nextLine() to prevent Scanner buffer issues
 * that would occur with nextInt() or nextDouble(). Numeric values are
 * parsed manually after reading so the buffer is always clean.
 * The program runs indefinitely until the user explicitly selects Exit.
 * No input can cause a crash or an unhandled exception.
 * Phase applicability: Phase 1. Replaced by a browser-based interface
 * in Phase 3 using Spring MVC and Thymeleaf.
 */
public class MenuHandler {

    // The single Scanner instance used for all console input.
    // Created once and reused throughout the program lifetime.
    private Scanner scanner;

    // The VehicleService instance that owns all business logic and data.
    private VehicleService service;

    // The FileHandler instance used to load vehicle data from a text file.
    private FileHandler fileHandler;

    /**
     * Constructs a MenuHandler with the required service and file handler.
     * A single Scanner is created here and shared across all input methods.
     *
     * @param service     the VehicleService instance managing inventory data
     * @param fileHandler the FileHandler instance for loading from file
     */
    public MenuHandler(VehicleService service, FileHandler fileHandler) {
        this.service     = service;
        this.fileHandler = fileHandler;
        this.scanner     = new Scanner(System.in);
    }

    /**
     * Starts the main menu loop. Displays the menu, reads the user's choice,
     * and routes to the appropriate handler. This loop runs indefinitely
     * until the user selects the Exit option.
     *
     * A top-level try catch wraps every loop iteration as a last-resort safety
     * net. If any completely unexpected exception somehow slips past all the
     * specific validation and error handling deeper in the program, it is caught
     * here and the program recovers gracefully rather than crashing. This catch
     * block should never fire under normal or intentional misuse conditions.
     */
    public void run() {
        while (true) {
            try {
                displayMenu();
                int choice = readMenuChoice();
                routeMenuChoice(choice);
            } catch (Exception e) {
                System.out.println("An unexpected error occurred. Returning to menu.");
            }
        }
    }

    /**
     * Prints the main menu to the console with all available operations.
     * The menu is reprinted after every operation so the user always
     * knows their options.
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
     * Reads and validates the user's menu selection. Loops until the user
     * enters a valid integer between 1 and 7. Letters, symbols, decimals,
     * and out-of-range numbers are all rejected with a clear message.
     *
     * @return a valid integer menu choice between 1 and 7
     */
    private int readMenuChoice() {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int choice = Integer.parseInt(input);
                if (choice >= 1 && choice <= 7) {
                    return choice;
                }
                System.out.print("Invalid option. Please enter a number between 1 and 7: ");
            } catch (NumberFormatException e) {
                System.out.print("Invalid option. Please enter a number between 1 and 7: ");
            }
        }
    }

    /**
     * Routes the validated menu choice to the corresponding handler method.
     *
     * @param choice the validated integer menu selection
     */
    public void routeMenuChoice(int choice) {
        switch (choice) {
            case 1: handleLoadFromFile();   break;
            case 2: handleDisplayAll();     break;
            case 3: handleAddVehicle();     break;
            case 4: handleDeleteVehicle();  break;
            case 5: handleUpdateVehicle();  break;
            case 6: handleMarkAsSold();     break;
            case 7: handleExit();           break;
        }
    }

    /**
     * Handles option 1: Load vehicles from a text file.
     * Prompts the user for a file path. Pressing Enter cancels and returns
     * to the menu. Delegates parsing and loading to FileHandler.
     * Displays the updated inventory after a successful load.
     */
    public void handleLoadFromFile() {
        System.out.print("\nEnter the path to your vehicle data file (or press Enter to cancel): ");
        String path = scanner.nextLine().trim();

        if (path.isEmpty()) {
            System.out.println("Load cancelled. Returning to menu.");
            return;
        }

        boolean success = fileHandler.loadFromFile(path, service);
        if (success) {
            System.out.println("\nUpdated inventory:");
            service.displayAllVehicles();
        }
    }

    /**
     * Handles option 2: Display all vehicles.
     * Delegates directly to VehicleService which handles the empty case.
     */
    public void handleDisplayAll() {
        service.displayAllVehicles();
    }

    /**
     * Handles option 3: Add a new vehicle manually.
     * Prompts the user for make, model, year, and price in sequence.
     * Pressing Enter at any prompt cancels the entire operation and returns
     * to the menu without adding any vehicle. Every field is validated
     * before the next field is requested. The ID is assigned automatically.
     * Displays the updated inventory after a successful add.
     */
    public void handleAddVehicle() {
        System.out.println("\nPress Enter at any prompt to cancel and return to the menu.");

        // Read and validate make.
        String make = readMake("Enter vehicle make: ");
        if (make == null) { printCancelled("Add"); return; }

        // Read and validate model.
        String model = readModel("Enter vehicle model: ");
        if (model == null) { printCancelled("Add"); return; }

        // Read and validate year.
        int year = readYear();
        if (year == -1) { printCancelled("Add"); return; }

        // Read and validate price.
        double price = readPrice();
        if (price == -1) { printCancelled("Add"); return; }

        // All fields valid. Add the vehicle.
        Vehicle vehicle = service.addVehicle(make, model, year, price);
        System.out.println("\nVehicle added successfully:");
        System.out.println(vehicle);

        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 4: Delete a vehicle by ID.
     * Prompts the user for the ID of the vehicle to remove. Pressing Enter
     * cancels. If the ID does not exist, a clear message is shown and no
     * changes are made. Prompts for confirmation before deleting.
     * Displays the updated inventory after a successful deletion.
     */
    public void handleDeleteVehicle() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to delete (or press Enter to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            printCancelled("Delete");
            return;
        }

        long id = parseId(input);
        if (id == -1) {
            System.out.println("Invalid ID. Must be a positive whole number.");
            return;
        }

        Vehicle vehicle = service.getVehicleById(id);
        if (vehicle == null) {
            System.out.println("No vehicle with ID " + id + " was found in the inventory.");
            return;
        }

        // Sold vehicles are locked from deletion as they represent completed sale records.
        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + id + " is marked as sold and cannot be deleted.");
            return;
        }

        System.out.println("Vehicle found: " + vehicle);
        System.out.print("Are you sure you want to delete this vehicle? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            service.deleteVehicle(id);
            System.out.println("Vehicle ID " + id + " has been permanently removed from the inventory.");
        } else {
            System.out.println("Delete cancelled.");
        }

        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 5: Update any field of an existing vehicle.
     * Prompts the user for the vehicle ID, then presents each field
     * with its current value. The user may press Enter to skip any field
     * and leave it unchanged. After all fields are read, a before and after
     * summary of proposed changes is displayed and the user must confirm
     * before any changes are applied. Pressing Enter on all fields cancels.
     * Displays the updated inventory after a successful update.
     */
    public void handleUpdateVehicle() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to update (or press Enter to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            printCancelled("Update");
            return;
        }

        long id = parseId(input);
        if (id == -1) {
            System.out.println("Invalid ID. Must be a positive whole number.");
            return;
        }

        Vehicle vehicle = service.getVehicleById(id);
        if (vehicle == null) {
            System.out.println("No vehicle with ID " + id + " was found in the inventory.");
            return;
        }

        // Sold vehicles are locked from editing as they represent completed sale records.
        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + id + " is marked as sold and cannot be updated.");
            return;
        }

        System.out.println("Vehicle found: " + vehicle);
        System.out.println("Press Enter on any field to leave it unchanged. Press Enter on all fields to cancel.");

        // Read new make, or null to keep existing.
        String newMake = readOptionalMake("Enter new make [" + vehicle.getMake() + "]: ");

        // Read new model, or null to keep existing.
        String newModel = readOptionalModel("Enter new model [" + vehicle.getModel() + "]: ");

        // Read new year, or -1 to keep existing.
        int newYear = readOptionalYear("Enter new year [" + vehicle.getYear() + "]: ");

        // Read new price, or -1 to keep existing.
        double newPrice = readOptionalPrice("Enter new price [" + String.format("$%,.2f", vehicle.getPrice()) + "]: ");

        // If no fields were changed, treat as cancellation.
        if (newMake == null && newModel == null && newYear == -1 && newPrice == -1) {
            System.out.println("No changes entered. Update cancelled.");
            return;
        }

        // Show a before and after summary of proposed changes before committing.
        System.out.println("\n--- Proposed Changes ---");
        System.out.println("Make  : " + vehicle.getMake()  + " -> " + (newMake  != null ? newMake  : vehicle.getMake()  + " (unchanged)"));
        System.out.println("Model : " + vehicle.getModel() + " -> " + (newModel != null ? newModel : vehicle.getModel() + " (unchanged)"));
        System.out.println("Year  : " + vehicle.getYear()  + " -> " + (newYear  != -1   ? newYear  : vehicle.getYear()  + " (unchanged)"));
        System.out.println("Price : $" + String.format("%,.2f", vehicle.getPrice()) + " -> "
                + (newPrice != -1 ? "$" + String.format("%,.2f", newPrice) : "$" + String.format("%,.2f", vehicle.getPrice()) + " (unchanged)"));
        System.out.println("------------------------");

        // Ask for confirmation before applying any changes.
        System.out.print("Apply these changes? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println("Update cancelled. No changes were made.");
            return;
        }

        // Apply updates. Null and sentinel values are handled inside updateVehicle.
        Vehicle updated = service.updateVehicle(id, newMake, newModel, newYear, newPrice);
        System.out.println("\nVehicle updated successfully:");
        System.out.println(updated);

        System.out.println("\nUpdated inventory:");
        service.displayAllVehicles();
    }

    /**
     * Handles option 6: Mark a vehicle as sold.
     * Prompts the user for the vehicle ID. Pressing Enter cancels.
     * Displays the vehicle details and asks for confirmation before marking.
     * Blocked if the vehicle does not exist or is already sold.
     * Displays a sale summary with inventory statistics after marking sold.
     */
    public void handleMarkAsSold() {
        if (service.getVehicleCount() == 0) {
            System.out.println("No vehicles are currently in the inventory.");
            return;
        }

        System.out.print("\nEnter the ID of the vehicle to mark as sold (or press Enter to cancel): ");
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            printCancelled("Mark as Sold");
            return;
        }

        long id = parseId(input);
        if (id == -1) {
            System.out.println("Invalid ID. Must be a positive whole number.");
            return;
        }

        // Look up the vehicle first to show details before asking for confirmation.
        Vehicle vehicle = service.getVehicleById(id);

        if (vehicle == null) {
            System.out.println("No vehicle with ID " + id + " was found in the inventory.");
            return;
        }

        if (vehicle.isSold()) {
            System.out.println("Vehicle ID " + id + " is already marked as sold. No changes made.");
            return;
        }

        // Show the vehicle and ask for confirmation before marking as sold.
        System.out.println("Vehicle found: " + vehicle);
        System.out.print("Are you sure you want to mark this vehicle as sold? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (!confirm.equals("yes")) {
            System.out.println("Mark as Sold cancelled. No changes were made.");
            return;
        }

        SaleSummary summary = service.markAsSold(id);

        if (summary == null) {
            System.out.println("No vehicle with ID " + id + " was found in the inventory.");
            return;
        }

        if (!summary.isMarkedSuccessfully()) {
            System.out.println("Vehicle ID " + id + " is already marked as sold. No changes made.");
            return;
        }

        // Display the sale result and inventory calculation.
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
     * Handles option 7: Exit the program.
     * Prints a goodbye message and terminates the JVM cleanly.
     */
    public void handleExit() {
        System.out.println("Exiting Vehicle Inventory Management System. Goodbye.");
        System.exit(0);
    }

    /**
     * Reads and validates a required make field.
     * Make must contain only letters and spaces. No numbers or special characters.
     * Pressing Enter returns null to signal cancellation.
     *
     * @param prompt the message to display before reading input
     * @return the validated make string, or null if the user cancelled
     */
    private String readMake(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            if (service.isValidMake(input)) {
                return input;
            }

            System.out.println("Invalid make. Must contain letters and spaces only. No numbers or special characters.");
        }
    }

    /**
     * Reads and validates a required model field.
     * Model may contain letters, numbers, hyphens, and spaces but must
     * include at least one letter. Pressing Enter returns null to signal cancellation.
     *
     * @param prompt the message to display before reading input
     * @return the validated model string, or null if the user cancelled
     */
    private String readModel(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            if (service.isValidModel(input)) {
                return input;
            }

            System.out.println("Invalid model. Must contain at least one letter. Letters, numbers, hyphens, and spaces are allowed.");
        }
    }

    /**
     * Reads an optional make field during an update operation.
     * Pressing Enter returns null to signal that this field should remain unchanged.
     *
     * @param prompt the message to display including the current value
     * @return the new make string, or null if the user chose to skip this field
     */
    private String readOptionalMake(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            if (service.isValidMake(input)) {
                return input;
            }

            System.out.println("Invalid make. Must contain letters and spaces only. No numbers or special characters.");
        }
    }

    /**
     * Reads an optional model field during an update operation.
     * Pressing Enter returns null to signal that this field should remain unchanged.
     *
     * @param prompt the message to display including the current value
     * @return the new model string, or null if the user chose to skip this field
     */
    private String readOptionalModel(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return null;
            }

            if (service.isValidModel(input)) {
                return input;
            }

            System.out.println("Invalid model. Must contain at least one letter. Letters, numbers, hyphens, and spaces are allowed.");
        }
    }

    /**
     * Reads and validates a required year field.
     * Loops until the user enters a valid integer between 2000 and 2026.
     * Pressing Enter returns -1 to signal cancellation.
     *
     * @return the validated year as an integer, or -1 if the user cancelled
     */
    private int readYear() {
        while (true) {
            System.out.print("Enter vehicle year (" + service.getMinYear() + " to " + service.getMaxYear() + "): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return -1;
            }

            try {
                int year = Integer.parseInt(input);
                if (service.isValidYear(year)) {
                    return year;
                }
                System.out.println("Invalid year. Must be between " + service.getMinYear()
                        + " and " + service.getMaxYear() + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for the year.");
            }
        }
    }

    /**
     * Reads an optional year field during an update operation.
     * Pressing Enter returns -1 to signal that this field should remain unchanged.
     *
     * @param prompt the message to display including the current value
     * @return the new year as an integer, or -1 if the user chose to skip
     */
    private int readOptionalYear(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return -1;
            }

            try {
                int year = Integer.parseInt(input);
                if (service.isValidYear(year)) {
                    return year;
                }
                System.out.println("Invalid year. Must be between " + service.getMinYear()
                        + " and " + service.getMaxYear() + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a whole number for the year.");
            }
        }
    }

    /**
     * Reads and validates a required price field.
     * Loops until the user enters a valid number between $2,000.00 and $500,000.00.
     * Pressing Enter returns -1 to signal cancellation.
     *
     * @return the validated price as a double, or -1 if the user cancelled
     */
    private double readPrice() {
        while (true) {
            System.out.print("Enter vehicle price ($" + String.format("%,.2f", service.getMinPrice())
                    + " to $" + String.format("%,.2f", service.getMaxPrice()) + "): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return -1;
            }

            try {
                double price = Double.parseDouble(input);
                if (service.isValidPrice(price)) {
                    return price;
                }
                System.out.println("Invalid price. Must be between $"
                        + String.format("%,.2f", service.getMinPrice())
                        + " and $" + String.format("%,.2f", service.getMaxPrice()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric price value.");
            }
        }
    }

    /**
     * Reads an optional price field during an update operation.
     * Pressing Enter returns -1 to signal that this field should remain unchanged.
     *
     * @param prompt the message to display including the current value
     * @return the new price as a double, or -1 if the user chose to skip
     */
    private double readOptionalPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                return -1;
            }

            try {
                double price = Double.parseDouble(input);
                if (service.isValidPrice(price)) {
                    return price;
                }
                System.out.println("Invalid price. Must be between $"
                        + String.format("%,.2f", service.getMinPrice())
                        + " and $" + String.format("%,.2f", service.getMaxPrice()) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a numeric price value.");
            }
        }
    }

    /**
     * Attempts to parse the given string as a positive long integer ID.
     * Returns -1 if the string is not a valid positive integer.
     *
     * @param input the raw string entered by the user
     * @return the parsed ID as a long, or -1 if parsing failed
     */
    private long parseId(String input) {
        try {
            long id = Long.parseLong(input);
            if (id > 0) {
                return id;
            }
            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Prints a standardized cancellation message for the given operation name.
     *
     * @param operation the name of the operation that was cancelled
     */
    private void printCancelled(String operation) {
        System.out.println(operation + " cancelled. Returning to menu.");
    }
}
