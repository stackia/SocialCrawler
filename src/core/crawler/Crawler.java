package core.crawler;

import java.util.Vector;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/18/14.
 */
abstract class Crawler {

    /**
     * An internal thread that handles the crawler's job.
     */
    private Thread workingThread;

    /**
     * Current working state.
     */
    private State state = State.STOPPED;

    /**
     * The FetcherPool to which the crawler will send FetchRequest.
     */
    private FetcherPool fetcherPool;

    /**
     * FetchRequest that has been sent to FetcherPool but not yet returned.
     */
    private Vector<FetchRequest> pendingFetchRequests = new Vector<FetchRequest>();

    /**
     * FetchRequest that failed to execute and need to be resent to FetcherPool.
     */
    private Vector<FetchRequest> failedFetchRequests = new Vector<FetchRequest>();

    /**
     * A custom signal which helps handle pause/stop
     */
    private Signal signal;

    /**
     * workingThread will be blocked when paused. This monitor helps to wake it up when continued.
     */
    private final Object pauseMonitor = new Object();

    /**
     * Helps notify the crawler that workingThread has been successfully paused.
     */
    private final Object pauseSuccessMonitor = new Object();

    abstract protected void generateFetchRequest(); // TODO: Retrieve one record in UserStorage, return a group of FetchRequest

    // The fetchRequest is guaranteed to be successful
    abstract protected void onFetchRequestPostExecutionImpl(FetchRequest fetchRequest, String content); // TODO: Need a workaround to write back to UserStorage

    // TODO: Add UserStorage, FetcherPool
    public Crawler(FetcherPool fetcherPool) {
        this.fetcherPool = fetcherPool;
    }

    /**
     * Called when FetchRequest has been executed.
     *
     * This will remove the FetchRequest from the pending list. If the request failed to complete, it will be add to failed list to resend. Subclass's implementation will also be called.
     *
     * @param fetchRequest FetchRequest that has been executed.
     * @param content Fetch result (HTTP Body string), or null if request failed.
     */
    public void onFetchRequestPostExecution(FetchRequest fetchRequest, String content) {
        // Remove from pendingFetchRequests
        pendingFetchRequests.remove(fetchRequest);

        // Add failed request to failedFetchRequest for later resending
        if (fetchRequest.getStatus() == FetchRequest.Status.Failed) {
            failedFetchRequests.add(fetchRequest);
            return;
        }

        onFetchRequestPostExecutionImpl(fetchRequest, content); // Call the subclass's implementation
    }

    /**
     * Start the crawler.
     */
    public void start() {
        switch (state) {
            case PAUSED:
                pauseMonitor.notify();
                break;

            case STOPPED:
                workingThread = new Thread(new Runnable() {
                    @Override
                    public void run() { // Wow.
                        synchronized (pauseMonitor) { // Wow..
                            LOOP:
                            while (true) { // Wow...
                                // Retrieve signal
                                if (signal != null) { // Wow....
                                    Signal tSignal = signal;
                                    signal = null; // Consume signal
                                    switch (tSignal) { // Wow..... So many nest...
                                        case PAUSE:
                                            // Abort pending FetchRequest and move them into failedFetchRequests for later resending
                                            for (FetchRequest fetchRequest : pendingFetchRequests) {
                                                fetcherPool.abortRequest(fetchRequest);
                                                failedFetchRequests.add(fetchRequest);
                                            }
                                            pendingFetchRequests.clear();

                                            // Wait until started again
                                            state = State.PAUSED;
                                            pauseSuccessMonitor.notify();
                                            try {
                                                pauseMonitor.wait();
                                            } catch (InterruptedException e) {
                                                Thread.interrupted();
                                                break LOOP;
                                            }
                                            continue LOOP;

                                        case STOP:
                                            // Abort pending FetchRequest
                                            for (FetchRequest fetchRequest : pendingFetchRequests) {
                                                fetcherPool.abortRequest(fetchRequest);
                                            }
                                            pendingFetchRequests.clear();

                                            // Stop workingThread
                                            state = State.STOPPED;
                                            break LOOP;
                                    }
                                }
                            /* TODO: Resend one failed FetchRequest. */
                            /* TODO: Or read one user from UserStorage, generate FetchRequest and send to FetcherPool. */
                            }
                        }
                    }
                });
                workingThread.start();
                break;
        }
        state = State.WORKING;
    }

    /**
     * Pause the crawler without corrupting current working state.
     *
     * @throws InterruptedException
     */
    public void pause() throws InterruptedException {
        synchronized (pauseSuccessMonitor) {
            signal = Signal.PAUSE; // Send pause signal to workingThread
            pauseSuccessMonitor.wait();
        }
    }

    /**
     * Stop the crawler. Crawler is reusable, and it can be later started again without recreate the Crawler object.
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        signal = Signal.STOP; // Send stop signal to workingThread
        workingThread.join();
    }

    /**
     * Represents the crawler's working state.
     */
    private enum State {
        WORKING,
        PAUSED,
        STOPPED,
    }

    /**
     * A group of signals that the crawler can send to its workingThread.
     */
    private enum Signal {
        PAUSE,
        STOP,
    }
}
