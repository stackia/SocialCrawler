package core.crawler;

import core.storage.UserStorage;

import java.util.List;
import java.util.Vector;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/18/14.
 */
abstract class Crawler<T> {

    /**
     * workingThread will be blocked when paused. This monitor helps to wake it up when continued.
     */
    private final Object pauseMonitor = new Object();

    /**
     * Helps notify the crawler that workingThread has been successfully paused.
     */
    private final Object pauseSuccessMonitor = new Object();

    /**
     * An internal thread that handles the crawler's job.
     */
    private Thread workingThread;

    /**
     * Current working state.
     */
    private State state = Crawler.State.STOPPED;

    /**
     * The FetcherPool to which the crawler will send FetchRequest.
     */
    private FetcherPool fetcherPool;

    /**
     * The UserStorage to read and write users.
     */
    private UserStorage<T> userStorage;

    /**
     * Current user offset in userStorage.
     */
    private long userOffset;

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
     * Create a new general crawler.
     *
     * @param fetcherPool FetchPool to use.
     * @param userStorage UserStorage to use.
     */
    public Crawler(FetcherPool fetcherPool, UserStorage<T> userStorage) {
        this.fetcherPool = fetcherPool;
        this.userStorage = userStorage;
    }

    /**
     * Generate a group of FetchRequest according to the given user.
     * <p/>
     * Implemented by subclasses. The FetchRequest will then be sent to fetcherPool.
     *
     * @param user From which the FetchRequest will be generated
     * @return A list of FetchRequest.
     */
    abstract protected List<FetchRequest> generateFetchRequest(T user);

    // The fetchRequest is guaranteed to be successful

    /**
     * Called when a FetchRequest is successfully finished.
     * <p/>
     * Implemented by subclasses. Usually do some post-processing procedures. The FetchRequest received here is guaranteed to be successful.
     *
     * @param fetchRequest The FetchRequest that has been finished.
     * @param content Fetch result (HTTP Body) as String.
     */
    abstract protected void onFetchRequestPostExecutionImpl(FetchRequest fetchRequest, String content);

    /**
     * Called when FetchRequest has been executed.
     * <p/>
     * This will remove the FetchRequest from the pending list. If the request failed to complete, it will be add to failed list to resend. Subclass's implementation will also be called.
     *
     * @param fetchRequest FetchRequest that has been executed.
     * @param content      Fetch result (HTTP Body string), or null if request failed.
     */
    public void onFetchRequestPostExecution(FetchRequest fetchRequest, String content) {
        // Remove from pendingFetchRequests
        pendingFetchRequests.remove(fetchRequest);

        // Add failed request to failedFetchRequest for later resending
        if (fetchRequest.getState() == FetchRequest.State.Failed) {
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
                        Vector<FetchRequest> bufferedFetchRequests = new Vector<FetchRequest>(); // Store FetchRequest that is generated but has not been sent to FetcherPool
                        synchronized (pauseMonitor) { // Wow..
                            LOOP:
                            while (true) { // Wow...
                                // Handle signal
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
                                            state = Crawler.State.PAUSED;
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

                                            // Clear variables for reusing
                                            userOffset = 0;
                                            pendingFetchRequests.clear();
                                            failedFetchRequests.clear();

                                            // Stop workingThread
                                            state = Crawler.State.STOPPED;

                                            // TODO: Add OnCrawlerStopListener
                                            System.out.println(this.getClass().getName() + " stopped.");

                                            break LOOP;
                                    }
                                }

                                // Send FetchRequest. ONLY ONE FetcherRequest will be sent in a loop.
                                try {
                                    // Resend one failed FetchRequest
                                    if (!failedFetchRequests.isEmpty()) {
                                        FetchRequest fetchRequest = failedFetchRequests.firstElement();
                                        failedFetchRequests.remove(fetchRequest);
                                        fetcherPool.executeRequest(fetchRequest);
                                        pendingFetchRequests.add(fetchRequest);
                                        continue;
                                    }

                                    // Send one already generated FetchRequest in the buffer
                                    if (!bufferedFetchRequests.isEmpty()) {
                                        FetchRequest fetchRequest = bufferedFetchRequests.firstElement();
                                        bufferedFetchRequests.remove(fetchRequest);
                                        fetcherPool.executeRequest(fetchRequest);
                                        pendingFetchRequests.add(fetchRequest);
                                        continue;
                                    }

                                    // Read one user from UserStorage, generate a group of FetchRequest, send the first one to FetcherPool, buffer the others.
                                    T user = userStorage.find(userOffset);
                                    ++userOffset;
                                    if (user == null) { // There is no more user
                                        signal = Signal.STOP; // Stop the crawler
                                        continue;
                                    }
                                    List<FetchRequest> fetchRequests = generateFetchRequest(user);
                                    FetchRequest fetchRequest = fetchRequests.get(0);
                                    fetcherPool.executeRequest(fetchRequest);
                                    pendingFetchRequests.add(fetchRequest); // Send the first one
                                    for (int i = 1; i < fetchRequests.size(); ++i) { // Buffer the others
                                        bufferedFetchRequests.add(fetchRequests.get(i));
                                    }

                                } catch (InterruptedException e) {
                                    Thread.interrupted();
                                    break;
                                }
                            }
                        }
                    }
                });
                workingThread.start();
                break;
        }
        state = Crawler.State.WORKING;
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
     *
     * @throws InterruptedException
     */
    public void stop() throws InterruptedException {
        signal = Signal.STOP; // Send stop signal to workingThread
        workingThread.join();
    }

    /**
     * Represents an crawler's working state.
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
