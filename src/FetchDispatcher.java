//import core.crawler.FetchRequest;
//import org.apache.http.client.config.RequestConfig;
//import org.apache.http.impl.client.CloseableHttpClient;
//import org.apache.http.impl.client.HttpClients;
//import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
//
//import java.util.Vector;
//import java.util.concurrent.PriorityBlockingQueue;
//
///**
// * Project: QQSpider
// * Package: PACKAGE_NAME
// * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
// */
//public class FetchDispatcher {
//    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
//    private Vector<FetchWorker> workers;
//    private PriorityBlockingQueue<FetchRequest> requests = new PriorityBlockingQueue<FetchRequest>();
//    private State state = State.Paused;
//    private OnDispatcherStateChangedListener onDispatcherStateChangedListener;
//
//    public FetchDispatcher(int workerNum) {
//        workers = new Vector<FetchWorker>(workerNum);
//        connectionManager.setDefaultMaxPerRoute(workerNum / 2 + 1);
//        connectionManager.setMaxTotal(workerNum * 2 + 1);
//        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(1500).build();
//        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build();
//        for (int i = 0; i < workerNum; ++i) {
//            workers.add(new FetchWorker(requests, httpClient));
//        }
//    }
//
//    public void start() {
//        state = State.Starting;
//        for (FetchWorker worker : workers) {
//            worker.start();
//        }
//        state = State.Started;
//        if (onDispatcherStateChangedListener != null) {
//            onDispatcherStateChangedListener.onDispatcherStarted();
//        }
//    }
//
//    public void pause() {
//        state = State.Pausing;
//        for (FetchWorker worker : workers) {
//            worker.pause();
//        }
//        state = State.Paused;
//        if (onDispatcherStateChangedListener != null) {
//            onDispatcherStateChangedListener.onDispatcherPaused();
//        }
//    }
//
//    // Clear request queue
//    public void reset() {
//        connectionManager.close();
//        requests.clear();
//    }
//
//    public void addRequest(FetchRequest request) {
//        requests.add(request);
//    }
//
//    public void setOnDispatcherStateChangedListener(OnDispatcherStateChangedListener onDispatcherStateChangedListener) {
//        this.onDispatcherStateChangedListener = onDispatcherStateChangedListener;
//    }
//
//    public State getState() {
//        return state;
//    }
//
//    public enum State {
//        Starting,
//        Started,
//        Pausing,
//        Paused,
//    }
//
//    public interface OnDispatcherStateChangedListener {
//        public void onDispatcherStarted();
//
//        public void onDispatcherPaused();
//    }
//}
