public class Worker implements Runnable {

    /**
     * Worker requests orange from plant,
     * then search through there, then returns orange and index,
     * then returns it when done
     */

    private boolean working = false;
    private final Plant plant;
    private final Thread thread;
    private final Orange.State orangeState; // The state the worker will work on

    Worker(Plant plant, Orange.State orangeState) {
        this.plant = plant;
        this.orangeState = orangeState;
        thread = new Thread(this);
    }

    @Override
    public void run() {
        while (working) {
            Orange orange = plant.requestOrange();

            if (orange != null) {
                orange.runProcess();
                plant.returnOrange(orange);
            }
        }
    }

    public void startWork() {
        working = true;
        thread.start();
    }

    public void stopWork() {
        working = false;
    }

    public void waitToStop() {
        try {
            thread.join();
        } catch (InterruptedException ignored) {
        }
    }

}
