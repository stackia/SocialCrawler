package core.crawler;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public class FetcherPool {

    /**
     * Unique ID for this FetcherPool object.
     */
    private static int seq = 0;

    /**
     * Singleton of a general-use FetcherPool.
     */
    private static FetcherPool defaultFetcherPool;

    /**
     * Used by FetcherThread to notify the pool there is an available fetcher.
     */
    private final Object freeFetcherMonitor = new Object();

    /**
     * Used to controlling HTTP connection number.
     */
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();

    /**
     * The maximum number of fetchers.
     */
    private int maxFetcherNum;

    /**
     * A thread group holding all fetcher threads.
     */
    private ThreadGroup fetcherThreadGroup = new ThreadGroup("FetcherThreadGroup-" + seq);

    /**
     * The common http client for all fetchers.
     */
    private CloseableHttpClient httpClient;

    /**
     * Create a fetcher pool.
     *
     * @param maxFetcherNum The maximum number of fetcher.
     */
    public FetcherPool(int maxFetcherNum) {
        ++seq;
        setMaxFetcherNum(maxFetcherNum);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1500).build();
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build();
    }

    /**
     * Return a singleton of FetcherPool for using in most situations.
     *
     * @return A new FetcherPool if there hasn't been one before, or the existed default FetcherPool.
     */
    public static FetcherPool DefaultPool() {
        if (defaultFetcherPool == null) {
            defaultFetcherPool = new FetcherPool(20);
        }
        return defaultFetcherPool;
    }

    /**
     * Execute a FetchRequest on a free fetcher.
     * <p/>
     * If there isn't any available fetcher, it will try to create a new fetcher, or wait until there is one if maxFetcherNum is reached.
     *
     * @param fetchRequest FetchRequest to be executed.
     * @throws InterruptedException
     */
    public void executeRequest(FetchRequest fetchRequest) throws InterruptedException {
        int activeThreadCount = fetcherThreadGroup.activeCount();
        if (activeThreadCount > maxFetcherNum) { // We can't create a new fetcher
            synchronized (freeFetcherMonitor) {
                OUTER:
                while (true) {
                    Thread[] threads = new Thread[activeThreadCount];
                    fetcherThreadGroup.enumerate(threads);
                    for (Thread thread : threads) { // Try to find a free fetcher
                        if (thread instanceof FetcherThread) {
                            FetcherThread fetcherThread = (FetcherThread) thread;
                            if (!fetcherThread.isBusy()) {
                                fetcherThread.setFetchRequest(fetchRequest);
                                break OUTER;
                            }
                        }
                    }
                    freeFetcherMonitor.wait(); // If not found, wait for a fetcher's notification and begin next loop.
                }
            }
        } else { // Ya! We can create a new fetcher
            FetcherThread fetcherThread = new FetcherThread(fetcherThreadGroup, fetchRequest);
            fetcherThread.start();
        }
    }

    /**
     * Abort an FetchRequest being executed.
     *
     * @param fetchRequest FetchRequest to be terminated.
     */
    public void abortRequest(FetchRequest fetchRequest) {
        Thread[] threads = new Thread[fetcherThreadGroup.activeCount()];
        fetcherThreadGroup.enumerate(threads);
        for (Thread thread : threads) {
            if (thread instanceof FetcherThread) {
                FetcherThread fetcherThread = (FetcherThread) thread;
                if (fetcherThread.getFetchRequest() == fetchRequest) {
                    fetcherThread.abortFetchRequest();
                    return;
                }
            }
        }
    }

    /**
     * Set the maximum number of fetchers.
     *
     * @param maxFetcherNum The maximum number.
     */
    public void setMaxFetcherNum(int maxFetcherNum) {
        this.maxFetcherNum = maxFetcherNum;
        connectionManager.setDefaultMaxPerRoute(maxFetcherNum / 2 + 1);
        connectionManager.setMaxTotal(maxFetcherNum * 2 + 1);
    }

    /**
     * A FetcherThread represents a 'fetcher'. Fetcher can work independently in parallel. If it lasts free for over 5 seconds, the thread will be released.
     */
    private class FetcherThread extends Thread {

        private FetchRequest fetchRequest;
        private HttpContext httpContext = new BasicHttpContext();

        public FetcherThread(ThreadGroup group, FetchRequest fetchRequest) {
            super(group, (Runnable) null);
            this.fetchRequest = fetchRequest;
        }

        @Override
        public void run() {
            int cycleTick = 0;
            while (fetchRequest != null || cycleTick < 5) {
                if (fetchRequest == null) {
                    try {
                        Thread.sleep(1000);
                        ++cycleTick;
                    } catch (InterruptedException e) { // Interrupted because of a new FetchRequest
                        Thread.interrupted(); // Clear interrupted status
                    }
                    continue;
                }
                cycleTick = 0;
                String responseString = null;
                try {
                    CloseableHttpResponse response = httpClient.execute(fetchRequest.getHttpRequest(), httpContext);
                    responseString = new BasicResponseHandler().handleResponse(response);
                    fetchRequest.setState(FetchRequest.State.Successful);
                } catch (Exception e) {
                    fetchRequest.setState(FetchRequest.State.Failed);
                }
                fetchRequest.getSender().onFetchRequestPostExecution(fetchRequest, responseString);
                fetchRequest = null;
                synchronized (freeFetcherMonitor) {
                    freeFetcherMonitor.notify(); // Notify the pool I am free
                }
            }
        }

        /**
         * Abort current FetchRequest.
         */
        public void abortFetchRequest() {
            if (fetchRequest != null) {
                fetchRequest.getHttpRequest().abort();
                fetchRequest.setState(FetchRequest.State.Failed);
                fetchRequest = null;
            }
        }

        /**
         * Get the current FetchRequest.
         *
         * @return Current FetchRequest.
         */
        public FetchRequest getFetchRequest() {
            return fetchRequest;
        }

        /**
         * Give the fetcher a new FetchRequest.
         *
         * @param fetchRequest FetchRequest to give.
         */
        public void setFetchRequest(FetchRequest fetchRequest) {
            this.fetchRequest = fetchRequest;
            interrupt();
        }

        /**
         * Return whether the fetcher is busy (has a pending FetchRequest).
         *
         * @return true if it is busy, otherwise false.
         */
        public boolean isBusy() {
            return fetchRequest != null;
        }
    }
}
