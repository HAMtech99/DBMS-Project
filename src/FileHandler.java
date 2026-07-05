import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * FileHandler.java
 * Responsible for reading vehicle data from a plain text file and loading
 * valid records into the VehicleService inventory. Each line in the file
 * represents one vehicle record with fields separated by commas in the
 * following order:
 *   id,make,model,year,price,sold
 * Example line:
 *   1,Toyota,Camry,2022,25000.00,false
 * Lines that are blank, malformed, or contain invalid field values are
 * skipped with a descriptive message. The program never crashes on a
 * bad line. A summary of added and skipped records is printed after
 * the file is fully processed.
 * This class is instantiated as an object and passed to MenuHandler,
 * consistent with the no-static-methods requirement of this project.
 * Phase applicability: Phase 1 and Phase 3. The file load feature
 * carries forward into Phase 3 where it is accessed through the GUI.
 */
public class FileHandler {

    /**
     * Reads the file at the given path and loads valid vehicle records
     * into the provided VehicleService instance. Invalid lines are
     * skipped without stopping the load process.
     *
     * @param filePath the absolute or relative path to the text file
     * @param service  the VehicleService instance to load vehicles into
     * @return true if the file was read without an IO error, false otherwise
     */
    public boolean loadFromFile(String filePath, VehicleService service) {
        int added     = 0;
        int skipped   = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip blank lines silently without counting them as skipped records.
                if (line.trim().isEmpty()) {
                    continue;
                }

                // Attempt to parse the line into a Vehicle object.
                Vehicle vehicle = parseVehicleLine(line, lineNumber, service);

                if (vehicle != null) {
                    service.addVehicleFromFile(vehicle);
                    System.out.println("Loaded: " + vehicle.getMake() + " " + vehicle.getModel()
                            + " (ID: " + vehicle.getId() + ")");
                    added++;
                } else {
                    skipped++;
                }
            }

        } catch (IOException e) {
            System.out.println("Error: File not found or could not be read - " + filePath);
            return false;
        }

        System.out.println("\nFile load complete. Loaded: " + added + " | Skipped: " + skipped);
        return true;
    }

    /**
     * Parses a single comma-delimited line into a Vehicle object.
     * Validates each field individually and prints a descriptive message
     * for any field that fails. Returns null if the line cannot be parsed
     * into a valid Vehicle.
     * Expected format: id,make,model,year,price,sold
     *
     * @param line       the raw text line from the file
     * @param lineNumber the current line number, used in error messages
     * @param service    the VehicleService used for validation checks
     * @return a valid Vehicle object, or null if the line was invalid
     */
    private Vehicle parseVehicleLine(String line, int lineNumber, VehicleService service) {

        // Split the line by comma into exactly 6 fields.
        String[] tokens = line.split(",", 6);

        if (tokens.length != 6) {
            System.out.println("Line " + lineNumber + " skipped: expected 6 fields, found " + tokens.length + ".");
            return null;
        }

        // Parse and validate the ID field.
        long id;
        try {
            id = Long.parseLong(tokens[0].trim());
        } catch (NumberFormatException e) {
            System.out.println("Line " + lineNumber + " skipped: invalid ID format - " + tokens[0].trim());
            return null;
        }

        if (id < 1) {
            System.out.println("Line " + lineNumber + " skipped: ID must be a positive number - " + id);
            return null;
        }

        if (!service.isIdUnique(id)) {
            System.out.println("Line " + lineNumber + " skipped: duplicate ID - " + id);
            return null;
        }

        // Validate the make field.
        String make = tokens[1].trim();
        if (!service.isValidMake(make)) {
            System.out.println("Line " + lineNumber + " skipped: invalid make - must contain letters and spaces only.");
            return null;
        }

        // Validate the model field.
        String model = tokens[2].trim();
        if (!service.isValidModel(model)) {
            System.out.println("Line " + lineNumber + " skipped: invalid model - must contain letters, numbers, hyphens, and spaces with at least one letter.");
            return null;
        }

        // Parse and validate the year field.
        int year;
        try {
            year = Integer.parseInt(tokens[3].trim());
        } catch (NumberFormatException e) {
            System.out.println("Line " + lineNumber + " skipped: invalid year format - " + tokens[3].trim());
            return null;
        }

        if (!service.isValidYear(year)) {
            System.out.println("Line " + lineNumber + " skipped: year out of range - must be between "
                    + service.getMinYear() + " and " + service.getMaxYear() + ".");
            return null;
        }

        // Parse and validate the price field.
        double price;
        try {
            price = Double.parseDouble(tokens[4].trim());
        } catch (NumberFormatException e) {
            System.out.println("Line " + lineNumber + " skipped: invalid price format - " + tokens[4].trim());
            return null;
        }

        if (!service.isValidPrice(price)) {
            System.out.println("Line " + lineNumber + " skipped: price out of range - must be between $"
                    + String.format("%,.2f", service.getMinPrice())
                    + " and $" + String.format("%,.2f", service.getMaxPrice()) + ".");
            return null;
        }

        // Parse the sold field. Accepts true or false case-insensitively.
        String soldToken = tokens[5].trim().toLowerCase();
        if (!soldToken.equals("true") && !soldToken.equals("false")) {
            System.out.println("Line " + lineNumber + " skipped: invalid sold value - must be true or false.");
            return null;
        }
        boolean sold = Boolean.parseBoolean(soldToken);

        // All fields valid. Return the constructed Vehicle.
        return new Vehicle(id, make, model, year, price, sold);
    }
}