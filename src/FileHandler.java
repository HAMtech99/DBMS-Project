import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * FileHandler.java. Responsible for reading vehicle data from a plain text file and loading
 * valid records into the VehicleService inventory. Each line represents one vehicle with fields
 * separated by commas in the following order: id,make,model,year,price,sold,color,mileage.
 * Example: 1,Toyota,Camry,2022,25000.00,false,White,15000.
 * Lines that are blank, malformed, or contain invalid field values are skipped with a descriptive
 * message. The program never crashes on a bad line. A summary of added and skipped records is
 * printed after the file is fully processed. This class is instantiated as an object and passed
 * to MenuHandler, consistent with the no-static-methods requirement of this project.
 * Phase applicability: Phase 1, Phase 2, and Phase 3.
 */
public class FileHandler {

    /**
     * Reads the file at the given path and loads valid vehicle records into the provided
     * VehicleService instance. Invalid lines are skipped without stopping the load process.
     * @param filePath the absolute or relative path to the text file
     * @param service  the VehicleService instance to load vehicles into
     * @return true if the file was read without an IO error, false otherwise
     */
    public boolean loadFromFile(final String filePath, final VehicleService service) {
        int added      = 0;
        int skipped    = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;

                final Vehicle vehicle = parseVehicleLine(line, lineNumber, service);
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
     * Parses a single comma-delimited line into a Vehicle object. Validates each field and prints
     * a descriptive message for any failure. Returns null if the line cannot produce a valid Vehicle.
     * Expected format: id,make,model,year,price,sold,color,mileage.
     * @param line       the raw text line from the file
     * @param lineNumber the current line number used in error messages
     * @param service    the VehicleService used for validation checks
     * @return a valid Vehicle object, or null if the line was invalid
     */
    private Vehicle parseVehicleLine(final String line, final int lineNumber, final VehicleService service) {
        final String[] tokens = line.split(",", 8);

        if (tokens.length != 8) {
            System.out.println("Line " + lineNumber + " skipped: expected 8 fields, found " + tokens.length + ".");
            return null;
        }

        final long id;
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

        final String make = tokens[1].trim();
        if (!service.isValidMake(make)) {
            System.out.println("Line " + lineNumber + " skipped: invalid make - must contain letters and spaces only.");
            return null;
        }

        final String model = tokens[2].trim();
        if (!service.isValidModel(model)) {
            System.out.println("Line " + lineNumber + " skipped: invalid model - must contain at least one letter.");
            return null;
        }

        final int year;
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

        final double price;
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

        final String soldToken = tokens[5].trim().toLowerCase();
        if (!soldToken.equals("true") && !soldToken.equals("false")) {
            System.out.println("Line " + lineNumber + " skipped: invalid sold value - must be true or false.");
            return null;
        }
        final boolean sold = Boolean.parseBoolean(soldToken);

        final String color = tokens[6].trim();
        if (!service.isValidColor(color)) {
            System.out.println("Line " + lineNumber + " skipped: invalid color - " + color
                    + ". Must be one of: " + service.getValidColors());
            return null;
        }

        final int mileage;
        try {
            mileage = Integer.parseInt(tokens[7].trim());
        } catch (NumberFormatException e) {
            System.out.println("Line " + lineNumber + " skipped: invalid mileage format - " + tokens[7].trim());
            return null;
        }

        if (!service.isValidMileage(mileage)) {
            System.out.println("Line " + lineNumber + " skipped: mileage out of range - must be between "
                    + service.getMinMileage() + " and " + String.format("%,d", service.getMaxMileage()) + ".");
            return null;
        }

        return new Vehicle(id, make, model, year, price, sold, color, mileage);
    }
}