/**
 * Main.java
 *
 * Entry point for the Vehicle Inventory Management System.
 * Responsible for creating all application objects and wiring them together
 * before handing control to the MenuHandler.
 *
 * This class intentionally contains no business logic. Its sole purpose is
 * to instantiate the required objects, pass them to each other as needed,
 * and start the application loop. This pattern is called dependency injection
 * and ensures that each class receives the tools it needs without creating
 * them internally.
 * The main method is the only static method in this project.
 * Phase applicability: Phase 1. Replaced or significantly modified in
 * Phase 3 when Spring Boot takes over application startup.
 */
public class Main {

    /**
     * Application entry point. Creates the VehicleService, FileHandler,
     * and MenuHandler in that order, then starts the menu loop.
     * The program runs until the user selects Exit from the menu.
     *
     * @param args command-line arguments, not used in this application
     */
    public static void main(String[] args) {

        // Print a welcome message to greet the user on startup.
        System.out.println("=================================================");
        System.out.println("   Welcome to the Vehicle Inventory System");
        System.out.println("=================================================");

        // Create the VehicleService which owns all vehicle data and business logic.
        VehicleService service = new VehicleService();

        // Create the FileHandler which handles reading vehicle data from a text file.
        FileHandler fileHandler = new FileHandler();

        // Create the MenuHandler and pass it the service and file handler.
        // MenuHandler owns all user interaction from this point forward.
        MenuHandler menuHandler = new MenuHandler(service, fileHandler);

        // Start the menu loop. The program runs until the user selects Exit.
        menuHandler.run();
    }
}