/**
 * The Company class simulates a juice company that operates multiple
 * orange processing plants for a fixed amount of time.
 * <p>
 * Each plant runs concurrently, processes oranges into bottles,
 * and tracks production statistics. After the processing time ends,
 * the plants are stopped and a summary report is printed.
 */
public class Company {

    /**
     * The total time (in milliseconds) that all plants
     * should run before being shut down.
     * Currently set to 5 seconds.
     */
    public static final long PROCESSING_TIME = 5 * 1000;

    /**
     * The total number of plants the company operates.
     */
    private static final int NUM_PLANTS = 2;

    /**
     * Main entry point of the program.
     * <p>
     * This method:
     * 1. Creates and starts all plants.
     * 2. Allows them to run for a fixed duration.
     * 3. Stops all plants.
     * 4. Waits for shutdown to complete.
     * 5. Prints a production summary.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        // Create an array to hold all plant instances
        Plant[] plants = new Plant[NUM_PLANTS];

        // Initialize and start each plant
        for (int i = 0; i < NUM_PLANTS; i++) {
            plants[i] = new Plant(i);   // Create plant with ID i
            plants[i].startPlant();     // Begin plant operation (likely starts a thread)
        }

        // Allow plants to run for the designated processing time
        delay(PROCESSING_TIME, "Plant malfunction");

        // Signal all plants to stop processing
        for (Plant p : plants) {
            p.stopPlant();
        }

        // Wait for each plant to fully shut down
        for (Plant p : plants) {
            p.waitToStop();
        }

        // Gather and summarize production results from all plants
        int totalProvided = 0;   // Total oranges supplied to plants
        int totalProcessed = 0;  // Total oranges processed into juice
        int totalBottles = 0;    // Total bottles produced
        int totalWasted = 0;     // Total oranges wasted

        for (Plant p : plants) {
            totalProvided += p.getProvidedOranges();
            totalProcessed += p.getProcessedOranges();
            totalBottles += p.getBottles();
            totalWasted += p.getWaste();
        }

        // Print final summary report
        System.out.println("Total provided/processed = "
                + totalProvided + "/" + totalProcessed);
        System.out.println("Created " + totalBottles
                + ", wasted " + totalWasted + " oranges");
    }

    /**
     * Causes the current thread to sleep for the specified amount of time.
     * <p>
     * This method ensures the sleep time is at least 1 millisecond
     * to avoid invalid sleep durations.
     *
     * @param time   The amount of time (in milliseconds) to pause execution.
     * @param errMsg The error message printed if the sleep is interrupted.
     */
    private static void delay(long time, String errMsg) {
        // Ensure sleep time is at least 1 millisecond
        long sleepTime = Math.max(1, time);

        try {
            // Pause execution for the specified duration
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            // Print error message if the thread is interrupted
            System.err.println(errMsg);
        }
    }
}
