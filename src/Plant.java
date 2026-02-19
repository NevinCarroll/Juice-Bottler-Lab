import java.util.LinkedList;
import java.util.List;

/**
 * The Plant class represents a single juice processing plant.
 * <p>
 * Each plant simulates a production pipeline consisting of:
 * - Fetchers
 * - Peelers
 * - Squeezers
 * - Bottlers
 * <p>
 * Workers operate concurrently and pass Orange objects through
 * each stage of production using shared buffers and synchronization.
 */
public class Plant {

    /**
     * Number of oranges required to produce one bottle of juice.
     */
    private final int ORANGES_PER_BOTTLE = 3;

    /**
     * Number of workers per production stage.
     */
    private final int NUMBER_OF_FETCHERS = 2;
    private final int NUMBER_OF_PEELERS = 3;
    private final int NUMBER_OF_SQUEEZERS = 3;
    private final int NUMBER_OF_BOTTLERS = 2;

    /**
     * Worker arrays for each production stage.
     */
    private final Worker[] fetchers;
    private final Worker[] peelers;
    private final Worker[] squeezers;
    private final Worker[] bottlers;

    /**
     * Total oranges supplied to this plant.
     */
    private volatile int orangesProvided;

    /**
     * Total oranges fully processed (bottled).
     */
    private volatile int orangesProcessed;

    /**
     * Buffers between production stages.
     */
    private final List<Orange> peeledOranges;
    private final List<Orange> squeezedOranges;
    private final List<Orange> bottledOranges;

    /**
     * Locks used to synchronize access between stages.
     */
    private final Object fetchLock = new Object();
    private final Object peelLock = new Object();
    private final Object squeezeLock = new Object();
    private final Object bottleLock = new Object();
    private final Object processedLock = new Object();

    /**
     * Indicates whether the plant is actively running.
     */
    private volatile boolean running = true;

    /**
     * Constructs a Plant and initializes all worker threads.
     *
     * @param threadNum Identifier for the plant (not currently stored,
     *                  but may be used for debugging/logging).
     */
    Plant(int threadNum) {

        orangesProcessed = 0;

        // Stage buffers (queues between production stages)
        peeledOranges = new LinkedList<>();
        squeezedOranges = new LinkedList<>();
        bottledOranges = new LinkedList<>();

        // Initialize worker arrays
        fetchers = new Worker[NUMBER_OF_FETCHERS];
        peelers = new Worker[NUMBER_OF_PEELERS];
        squeezers = new Worker[NUMBER_OF_SQUEEZERS];
        bottlers = new Worker[NUMBER_OF_BOTTLERS];

        // Create workers for each production stage
        for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
            fetchers[i] = new Worker(this, Orange.State.Fetched);
        }

        for (int i = 0; i < NUMBER_OF_PEELERS; i++) {
            peelers[i] = new Worker(this, Orange.State.Peeled);
        }

        for (int i = 0; i < NUMBER_OF_SQUEEZERS; i++) {
            squeezers[i] = new Worker(this, Orange.State.Squeezed);
        }

        for (int i = 0; i < NUMBER_OF_BOTTLERS; i++) {
            bottlers[i] = new Worker(this, Orange.State.Bottled);
        }
    }

    /**
     * Starts all workers in the plant.
     * Each worker begins executing in its own thread.
     */
    public void startPlant() {
        for (Worker w : fetchers) w.startWork();
        for (Worker w : peelers) w.startWork();
        for (Worker w : squeezers) w.startWork();
        for (Worker w : bottlers) w.startWork();
    }

    /**
     * Signals the plant to stop running.
     * <p>
     * This:
     * 1. Sets running to false.
     * 2. Wakes any threads waiting on stage buffers.
     * 3. Tells all workers to stop.
     */
    public void stopPlant() {
        running = false;

        // Wake up any workers waiting on buffers
        synchronized (peelLock) {
            peelLock.notifyAll();
        }
        synchronized (squeezeLock) {
            squeezeLock.notifyAll();
        }
        synchronized (bottleLock) {
            bottleLock.notifyAll();
        }

        // Signal workers to stop
        for (Worker w : fetchers) w.stopWork();
        for (Worker w : peelers) w.stopWork();
        for (Worker w : squeezers) w.stopWork();
        for (Worker w : bottlers) w.stopWork();
    }

    /**
     * Waits for all worker threads to fully terminate.
     * Ensures clean shutdown before summarizing results.
     */
    public void waitToStop() {
        for (Worker w : fetchers) w.waitToStop();
        for (Worker w : peelers) w.waitToStop();
        for (Worker w : squeezers) w.waitToStop();
        for (Worker w : bottlers) w.waitToStop();
    }

    /**
     * Requests an orange for a specific production stage.
     * <p>
     * Depending on the state:
     * - Fetchers create new oranges.
     * - Other workers remove oranges from their stage buffer.
     * <p>
     * Uses wait()/notifyAll() to coordinate producer/consumer behavior.
     *
     * @param state The production stage requesting the orange.
     * @return An Orange object or null if the plant is shutting down.
     */
    public Orange requestOrange(Orange.State state) {

        Orange foundOrange = null;

        if (state == Orange.State.Fetched) {
            // Fetchers generate new oranges
            synchronized (fetchLock) {
                orangesProvided++;
                foundOrange = new Orange();
            }
        } else if (state == Orange.State.Peeled) {
            synchronized (peelLock) {
                while (peeledOranges.isEmpty() && running) {
                    try {
                        peelLock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!peeledOranges.isEmpty()) {
                    foundOrange = peeledOranges.removeFirst();
                }
            }
        } else if (state == Orange.State.Squeezed) {
            synchronized (squeezeLock) {
                while (squeezedOranges.isEmpty() && running) {
                    try {
                        squeezeLock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!squeezedOranges.isEmpty()) {
                    foundOrange = squeezedOranges.removeFirst();
                }
            }
        } else if (state == Orange.State.Bottled) {
            synchronized (bottleLock) {
                while (bottledOranges.isEmpty() && running) {
                    try {
                        bottleLock.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
                if (!bottledOranges.isEmpty()) {
                    foundOrange = bottledOranges.removeFirst();
                }
            }
        }

        return foundOrange;
    }

    /**
     * Returns an orange to the next stage of processing.
     * <p>
     * Moves the orange forward in the pipeline and
     * notifies waiting workers.
     *
     * @param orange The orange being passed forward.
     * @param state  The stage that just completed.
     */
    public void returnOrange(Orange orange, Orange.State state) {

        if (state == Orange.State.Fetched) {
            synchronized (peelLock) {
                peeledOranges.add(orange);
                peelLock.notifyAll();
            }
        } else if (state == Orange.State.Peeled) {
            synchronized (squeezeLock) {
                squeezedOranges.add(orange);
                squeezeLock.notifyAll();
            }
        } else if (state == Orange.State.Squeezed) {
            synchronized (bottleLock) {
                bottledOranges.add(orange);
                bottleLock.notifyAll();
            }
        } else if (state == Orange.State.Bottled) {
            synchronized (processedLock) {
                orangesProcessed++;
                processedLock.notifyAll();
            }
        }
    }

    /**
     * @return Total oranges supplied to this plant.
     */
    public int getProvidedOranges() {
        return orangesProvided;
    }

    /**
     * @return Total oranges fully processed.
     */
    public int getProcessedOranges() {
        return orangesProcessed;
    }

    /**
     * Calculates the number of full bottles produced.
     *
     * @return Number of bottles created.
     */
    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    /**
     * Calculates leftover oranges that did not form a full bottle.
     *
     * @return Number of wasted oranges.
     */
    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }
}
