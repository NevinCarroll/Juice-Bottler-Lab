import java.util.ArrayList;
import java.util.List;

public class Plant {
    private final int ORANGES_PER_BOTTLE = 3;

    private final int NUMBER_OF_FETCHERS = 2;
    private final int NUMBER_OF_PEELERS = 3;
    private final int NUMBER_OF_SQUEEZERS = 3;
    private final int NUMBER_OF_BOTTLERS = 2;

    private final int NUMBER_OF_WORKERS;
    private final Worker[] workers;

    private volatile int orangesProvided;
    private volatile int orangesProcessed;

    private final List<Orange> peeledOranges;
    private final List<Orange> squeezedOranges;
    private final List<Orange> bottledOranges;

    private final boolean[] orangeAvailability;
    private final int ORANGES_IN_QUEUE = 10; // Only this amount of oranges can be in queue at a time TODO Remove

    // Create multiple queues, assign each worker a number and queue they work in

    Plant(int threadNum) {
        orangesProvided = ORANGES_IN_QUEUE; // Set oranges provided to how many oranges start in the array
        orangesProcessed = 0;
        peeledOranges = new ArrayList<Orange>();
        squeezedOranges = new ArrayList<Orange>();
        bottledOranges = new ArrayList<Orange>();
        orangeAvailability = new boolean[ORANGES_IN_QUEUE];

        NUMBER_OF_WORKERS = NUMBER_OF_FETCHERS + NUMBER_OF_PEELERS + NUMBER_OF_SQUEEZERS + NUMBER_OF_BOTTLERS;

        workers = new Worker[threadNum];


        for (int i = 0; i < NUMBER_OF_FETCHERS; i++) {
            workers[i] = new Worker(this);
        }
    }

    public void startPlant() {
        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
            workers[i].startWork();
        }
    }

    public void stopPlant() {
        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
            workers[i].stopWork();
        }
    }

    public void waitToStop() {
        for (int i = 0; i < NUMBER_OF_WORKERS; i++) {
            workers[i].waitToStop();
        }
    }

    public synchronized Orange requestOrange() {
        boolean findingOrange = true;
        int foundOrange = -1;
        while (findingOrange) { // Make a yield wait
            for (int i = 0; i < ORANGES_IN_QUEUE; i++) {
                if (orangeAvailability[i]) {
                    orangeAvailability[i] = false;
                    foundOrange = i;
                    findingOrange = false;
                    break;
                }
            }
            if (foundOrange == -1) {
                try {
                    wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        return oranges[foundOrange];
    }

    public synchronized void returnOrange(Orange orange) {
        int orangeIndex = findOrange(orange);

        if (oranges[orangeIndex].getState() == Orange.State.Bottled) {
            orangesProcessed++;
            oranges[orangeIndex] = new Orange();
            orangesProvided++;
        }

        orangeAvailability[orangeIndex] = true;
        notifyAll();
    }

    public int findOrange(Orange o) {
        for (int i = 0; i < ORANGES_IN_QUEUE; i++) {
            if (oranges[i] == o) {
                return i;
            }
        }

        return -1; // Something has gone terribly wrong
    }

    public int getProvidedOranges() {
        return orangesProvided;
    }

    public int getProcessedOranges() {
        return orangesProcessed;
    }

    public int getBottles() {
        return orangesProcessed / ORANGES_PER_BOTTLE;
    }

    public int getWaste() {
        return orangesProcessed % ORANGES_PER_BOTTLE;
    }

    public int getNUMBER_OF_FETCHERS() {
        return NUMBER_OF_FETCHERS;
    }
}