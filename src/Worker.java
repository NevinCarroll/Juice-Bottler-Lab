/**
 * The Worker class represents a single worker in a plant.
 * <p>
 * Each worker operates in its own thread and is responsible for
 * one stage of the orange processing pipeline:
 * - Fetching
 * - Peeling
 * - Squeezing
 * - Bottling
 * <p>
 * Workers continuously:
 * 1. Request an orange from the plant.
 * 2. Process the orange.
 * 3. Return the orange to the next production stage.
 * <p>
 * The worker stops when it is signaled and the working flag becomes false.
 */
public class Worker implements Runnable {

    /**
     * Indicates whether this worker should continue running.
     */
    private boolean working = false;

    /**
     * Reference to the plant this worker belongs to.
     */
    private final Plant plant;

    /**
     * The thread executing this worker.
     */
    private final Thread thread;

    /**
     * The production stage this worker is responsible for.
     */
    private final Orange.State orangeState;

    /**
     * Constructs a Worker assigned to a specific plant and production stage.
     *
     * @param plant       The plant this worker operates in.
     * @param orangeState The stage of processing this worker performs.
     */
    Worker(Plant plant, Orange.State orangeState) {
        this.plant = plant;
        this.orangeState = orangeState;

        // Create a new thread that will execute this worker's run() method
        thread = new Thread(this);
    }

    /**
     * The main execution loop of the worker thread.
     * <p>
     * While working is true:
     * 1. Request an orange from the plant.
     * 2. If an orange is available, process it.
     * 3. Return it to the next stage of production.
     * <p>
     * The loop exits when stopWork() sets working to false.
     */
    @Override
    public void run() {
        while (working) {

            // Request an orange at this worker's production stage
            Orange orange = plant.requestOrange(orangeState);

            // If an orange was successfully retrieved, process it
            if (orange != null) {

                // Perform this stage's work on the orange
                orange.runProcess();

                // Return the orange to the plant for the next stage
                plant.returnOrange(orange, orangeState);
            }
        }
    }

    /**
     * Starts the worker's thread and begins processing.
     * <p>
     * This method:
     * 1. Sets working to true.
     * 2. Starts the thread, which calls run().
     */
    public void startWork() {
        working = true;
        thread.start();
    }

    /**
     * Signals the worker to stop processing.
     * <p>
     * The thread will finish its current iteration
     * and then exit the run loop.
     */
    public void stopWork() {
        working = false;
    }

    /**
     * Waits for this worker's thread to terminate.
     * <p>
     * Ensures clean shutdown of all worker threads.
     */
    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException ignored) {
            // Ignore interruption during shutdown
        }
    }
}
