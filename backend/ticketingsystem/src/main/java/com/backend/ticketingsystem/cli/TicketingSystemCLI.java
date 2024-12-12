package com.backend.ticketingsystem.cli;

import java.io.*;
import java.util.*;

public class TicketingSystemCLI {
    // File for saving and loading system configuration
    private static final String CONFIG_FILE = "config.json";

    // Shared pool of tickets
    private static TicketPool ticketPool;

    // Threads for producer and consumer
    private static Thread producerThread;
    private static Thread consumerThread;

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            // Load or configure the system
            Configuration config = configureSystem(scanner);
            ticketPool = new TicketPool(config.maxTicketCapacity);

            // Command loop for user interaction
            while (true) {
                System.out.println("\nCommands: START, STOP, STATUS, EXIT");
                System.out.print("Enter command: ");
                String command = scanner.nextLine().trim().toUpperCase();

                switch (command) {
                    case "START":
                        // Start producer and consumer threads
                        startSystem(config);
                        break;

                    case "STOP":
                        // Stop running threads
                        stopSystem();
                        break;

                    case "STATUS":
                        // Display current number of available tickets
                        System.out.println("Current Ticket Pool Size: " + ticketPool.getAvailableTickets());
                        break;

                    case "EXIT":
                        // Stop the system and exit
                        stopSystem();
                        System.out.println("Exiting the system. Goodbye!");
                        return;

                    default:
                        // Handle invalid commands
                        System.out.println("Invalid command! Please try again.");
                        break;
                }
            }
        }
    }

    // Start producer and consumer threads
    private static void startSystem(Configuration config) {
        if (producerThread == null || !producerThread.isAlive()) {
            producerThread = new Thread(new Vendor(ticketPool, config.ticketReleaseRate));
            producerThread.start();
        }
        if (consumerThread == null || !consumerThread.isAlive()) {
            consumerThread = new Thread(new Customer(ticketPool, config.customerRetrievalRate));
            consumerThread.start();
        }
        System.out.println("System started!");
    }

    // Stop producer and consumer threads gracefully
    private static void stopSystem() {
        if (producerThread != null && producerThread.isAlive()) {
            producerThread.interrupt();
        }
        if (consumerThread != null && consumerThread.isAlive()) {
            consumerThread.interrupt();
        }
        try {
            if (producerThread != null) producerThread.join(); // Wait for producer thread to terminate
            if (consumerThread != null) consumerThread.join(); // Wait for consumer thread to terminate
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Interrupted while stopping threads.");
        }
        System.out.println("System stopped!");
    }

    // Configure system settings, either by loading or prompting the user
    private static Configuration configureSystem(Scanner scanner) {
        System.out.print("Do you want to load an existing configuration? (yes/no): ");
        String response = scanner.nextLine().trim().toLowerCase();

        if ("yes".equals(response)) {
            Configuration config = loadConfiguration();
            if (config != null) {
                return config; // Return loaded configuration if valid
            }
            System.out.println("No valid configuration found. Proceeding with new configuration.");
        }

        // Prompt user for new configuration settings
        int totalTickets = getIntInput(scanner, "Enter Total Number of Tickets: ");
        int ticketReleaseRate = getIntInput(scanner, "Enter Ticket Release Rate: ");
        int customerRetrievalRate = getIntInput(scanner, "Enter Customer Retrieval Rate: ");
        int maxTicketCapacity = getIntInput(scanner, "Enter Maximum Ticket Capacity: ");
        Configuration config = new Configuration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);

        saveConfiguration(config); // Save the new configuration to a file
        return config;
    }

    // Utility method to get valid integer input from the user
    private static int getIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int input = Integer.parseInt(scanner.nextLine());
                if (input > 0) {
                    return input; // Return valid positive input
                } else {
                    System.out.println("Please enter a positive number.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    // Save the configuration to a file
    private static void saveConfiguration(Configuration config) {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            writer.write(config.toString()); // Write configuration as JSON
            System.out.println("Configuration saved to " + CONFIG_FILE);
        } catch (IOException e) {
            System.out.println("Error saving configuration: " + e.getMessage());
        }
    }

    // Load the configuration from a file
    private static Configuration loadConfiguration() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE))) {
            String json = reader.readLine(); // Read JSON configuration
            String[] values = json.replaceAll("[{}\"]", "").split(",");
            int totalTickets = Integer.parseInt(values[0].split(":")[1].trim());
            int ticketReleaseRate = Integer.parseInt(values[1].split(":")[1].trim());
            int customerRetrievalRate = Integer.parseInt(values[2].split(":")[1].trim());
            int maxTicketCapacity = Integer.parseInt(values[3].split(":")[1].trim());
            return new Configuration(totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading configuration: " + e.getMessage());
            return null;
        }
    }
}

// Configuration class to hold system settings
class Configuration {
    int totalTickets;
    int ticketReleaseRate;
    int customerRetrievalRate;
    int maxTicketCapacity;

    public Configuration(int totalTickets, int ticketReleaseRate, int customerRetrievalRate, int maxTicketCapacity) {
        this.totalTickets = totalTickets;
        this.ticketReleaseRate = ticketReleaseRate;
        this.customerRetrievalRate = customerRetrievalRate;
        this.maxTicketCapacity = maxTicketCapacity;
    }

    // Convert configuration to JSON-like string
    @Override
    public String toString() {
        return String.format("{\"totalTickets\": %d, \"ticketReleaseRate\": %d, \"customerRetrievalRate\": %d, \"maxTicketCapacity\": %d}",
                totalTickets, ticketReleaseRate, customerRetrievalRate, maxTicketCapacity);
    }
}

// TicketPool class to manage ticket storage and access
class TicketPool {
    private final List<Integer> tickets = Collections.synchronizedList(new ArrayList<>()); // Thread-safe list for tickets
    private final int maxCapacity;

    public TicketPool(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    // Add tickets to the pool, waiting if the pool is full
    public synchronized void addTickets(int amount, String vendorId) {
        while (tickets.size() + amount > maxCapacity) {
            try {
                wait(); // Wait until there is space
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        for (int i = 0; i < amount; i++) {
            tickets.add(1); // Add tickets
        }
        System.out.println(vendorId + " added " + amount + " tickets. Total tickets: " + tickets.size());
        notifyAll(); // Notify waiting threads
    }

    // Remove tickets from the pool, waiting if not enough tickets are available
    public synchronized void removeTickets(int amount, String customerId) {
        while (tickets.size() < amount) {
            try {
                wait(); // Wait until there are enough tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
        tickets.subList(0, amount).clear(); // Remove the required number of tickets
        System.out.println(customerId + " purchased " + amount + " tickets. Remaining tickets: " + tickets.size());
        notifyAll(); // Notify waiting threads
    }

    // Get the current number of available tickets
    public synchronized int getAvailableTickets() {
        return tickets.size();
    }
}

// Vendor thread to add tickets periodically
class Vendor implements Runnable {
    private static int vendorCount = 0; // Track vendor instances
    private final TicketPool ticketPool;
    private final int releaseRate;
    private final String vendorId;

    public Vendor(TicketPool ticketPool, int releaseRate) {
        this.ticketPool = ticketPool;
        this.releaseRate = releaseRate;
        vendorCount++;
        this.vendorId = "Vendor " + vendorCount;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ticketPool.addTickets(releaseRate, vendorId);
            try {
                Thread.sleep(1000); // Wait before adding more tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}

// Customer thread to remove tickets periodically
class Customer implements Runnable {
    private static int customerCount = 0; // Track customer instances
    private final TicketPool ticketPool;
    private final int retrievalRate;
    private final String customerId;

    public Customer(TicketPool ticketPool, int retrievalRate) {
        this.ticketPool = ticketPool;
        this.retrievalRate = retrievalRate;
        customerCount++;
        this.customerId = "Customer " + customerCount;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            ticketPool.removeTickets(retrievalRate, customerId);
            try {
                Thread.sleep(1000); // Wait before removing more tickets
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
