import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import java.util.PriorityQueue;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Project: QQSpider
 * Package: PACKAGE_NAME
 * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
 */
public class FetchWorker implements Runnable {

    private PriorityBlockingQueue<FetchRequest> requests;
    private Thread workerThread;
    private CloseableHttpClient httpClient;
    private HttpContext httpContext = new BasicHttpContext();
    private HttpUriRequest currentHttpRequest;

    public FetchWorker(PriorityBlockingQueue<FetchRequest> requests, CloseableHttpClient httpClient) {
        this.requests = requests;
        this.httpClient = httpClient;
    }

    public void start() {
        if (workerThread != null && workerThread.isAlive()) {
            return;
        }
        workerThread = new Thread(this);
        workerThread.start();
    }

    public void pause() {
        if (currentHttpRequest != null) {
            currentHttpRequest.abort();
        } else if (workerThread != null) {
            workerThread.interrupt();
        }
    }

    @Override
    public void run() {
        FetchRequest currentRequest = null;
        try {
            while (true) {
                currentRequest = requests.poll();
                if (currentRequest == null) {
                    Thread.sleep(4000);
                    continue;
                }
                switch (currentRequest.getType()) {
                    case Friends:
                        currentHttpRequest = new HttpGet("http://show.qq.com/cgi-bin/qqshow_user_friendgroup?g_tk=" + generateGTK(MainForm.vSKey));
                        break;
                    case RecentVisitors:
                        currentHttpRequest = new HttpGet("http://g.edu.qzone.qq.com/cgi-bin/friendshow/cgi_get_visitor_simple?uin=" + currentRequest.getUser().uin + "&mask=2&g_tk=" + generateGTK(MainForm.vSKey) + "&page=1&fupdate=1&format=json");
                        break;
                    case MessageBoard:
                        currentHttpRequest = new HttpGet("http://m.qzone.qq.com/cgi-bin/new/get_msgb?hostUin=" + currentRequest.getUser().uin + "&start=0&format=json&num=20&g_tk=" + generateGTK(MainForm.vSKey));
                        break;
                    case FullProfiles:
                        currentHttpRequest = new HttpGet("http://base.s8.qzone.qq.com/cgi-bin/user/cgi_userinfo_get_all?uin=" + currentRequest.getUser().uin + "&fupdate=1&g_tk=" + generateGTK(MainForm.vSKey) + "&format=json");
                        break;
                    case SimpleProfiles:
                        currentHttpRequest = new HttpGet("http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=" + currentRequest.getUser().uin + "&fupdate=1&g_tk=" + generateGTK(MainForm.vSKey) + "&format=json");
                        break;
                }
                currentHttpRequest.addHeader("Cookie", String.format("uin=o%010d; skey=%s", Long.parseLong(MainForm.vUIN), MainForm.vSKey));
                try {
                    CloseableHttpResponse response = httpClient.execute(currentHttpRequest, httpContext);
                    String responseString = new BasicResponseHandler().handleResponse(response);
                    switch (currentRequest.getType()) {
                        case Friends:
                            currentRequest.getUser().originalFriendsData = responseString;
                            break;
                        case RecentVisitors:
                            currentRequest.getUser().originalRecentVisitorsData = responseString;
                            break;
                        case MessageBoard:
                            currentRequest.getUser().originalMessageBoardData = responseString;
                            break;
                        case FullProfiles:
                            currentRequest.getUser().originalFullProfilesData = responseString;
                            break;
                        case SimpleProfiles:
                            currentRequest.getUser().originalSimpleProfilesData = responseString;
                            break;
                    }
                    if (currentRequest.getOnRequestFinishedListener() != null) {
                        currentRequest.getOnRequestFinishedListener().onRequestFinished(currentRequest);
                    }
                } catch (Exception e) {
                    System.out.println("Timeout.");
                    if (currentHttpRequest != null && currentHttpRequest.isAborted()) {
                        throw new InterruptedException();
                    } else {
                        currentRequest.setPriority(FetchRequest.Priority.High);
                        requests.add(currentRequest); // Add request back to queue
                    }
                }
                currentHttpRequest = null;
            }
        } catch (InterruptedException e) { // Pause fetching
            if (currentRequest != null) {
                currentRequest.setPriority(FetchRequest.Priority.High);
                requests.add(currentRequest); // Add request back to queue
            }
        }
    }

    public String generateGTK(String vSKey) {
        int hash = 5381;

        for (int i = 0; i < vSKey.length(); ++i) {
            hash += (hash << 5) + (int) vSKey.charAt(i);
        }

        return Integer.toString(hash & 0x7fffffff);
    }

    public static interface OnRequestFinishedListener {
        public void onRequestFinished(FetchRequest request);
    }
}
