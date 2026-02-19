/**
 * The Orange class represents a single orange moving through
 * the juice production pipeline.
 * <p>
 * Each orange progresses through a sequence of states:
 * Fetched → Peeled → Squeezed → Bottled → Processed
 * <p>
 * Each state requires a simulated amount of time to complete,
 * represented using Thread.sleep().
 */
public class Orange {

    /**
     * Represents the processing stages of an orange.
     * <p>
     * Each state stores the time (in milliseconds) required
     * to complete that stage.
     */
    public enum State {

        /**
         * Orange has been fetched from supply.
         */
        Fetched(15),

        /**
         * Orange has been peeled.
         */
        Peeled(38),

        /**
         * Orange has been squeezed into juice.
         */
        Squeezed(29),

        /**
         * Juice has been bottled.
         */
        Bottled(17),

        /**
         * Orange has fully completed processing.
         */
        Processed(1);

        /**
         * Index of the final state in the enum.
         */
        private static final int finalIndex = State.values().length - 1;

        /**
         * Time in milliseconds required to complete this stage.
         */
        final int timeToComplete;

        /**
         * Constructs a processing state with its required time.
         *
         * @param timeToComplete Processing time in milliseconds.
         */
        State(int timeToComplete) {
            this.timeToComplete = timeToComplete;
        }

        /**
         * Returns the next processing state in the pipeline.
         *
         * @return The next State.
         * @throws IllegalStateException if already at the final state.
         */
        State getNext() {
            int currIndex = this.ordinal();

            if (currIndex >= finalIndex) {
                throw new IllegalStateException("Already at final state");
            }

            return State.values()[currIndex + 1];
        }
    }

    /**
     * Current state of the orange in the processing pipeline.
     */
    private State state;

    /**
     * Constructs a new Orange.
     * <p>
     * A new orange starts in the Fetched state and
     * immediately simulates the time required for that stage.
     */
    public Orange() {
        state = State.Fetched;
        doWork();
    }

    /**
     * Returns the current processing state of the orange.
     *
     * @return The current State.
     */
    public State getState() {
        return state;
    }

    /**
     * Advances the orange to the next stage of processing.
     * <p>
     * This method:
     * 1. Validates the orange is not already fully processed.
     * 2. Advances to the next state.
     * 3. Simulates the time required for that stage.
     *
     * @throws IllegalStateException if the orange is already processed.
     */
    public void runProcess() {

        // Prevent processing beyond the final state
        if (state == State.Processed) {
            throw new IllegalStateException(
                    "This orange has already been processed");
        }

        // Advance to next production stage
        state = state.getNext();

        // Simulate work required for this stage
        doWork();
    }

    /**
     * Simulates the time required to complete the current stage
     * by putting the thread to sleep.
     * <p>
     * If interrupted, a warning message is printed.
     */
    private void doWork() {
        try {
            Thread.sleep(state.timeToComplete);
        } catch (InterruptedException e) {
            System.err.println(
                    "Incomplete orange processing, juice may be bad");
        }
    }
}
