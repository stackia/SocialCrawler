package core.crawler;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.util.HashMap;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
 */
class FetchRequest {
    private HttpUriRequest httpRequest;
    private Crawler sender;
    private Status status = Status.Pending;

    public FetchRequest(HttpUriRequest httpRequest, Crawler sender) {
        this.httpRequest = httpRequest;
        this.sender = sender;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public HttpUriRequest getHttpRequest() {
        return httpRequest;
    }

    public Crawler getSender() {
        return sender;
    }

    public enum Status {
        Pending,
        Successful,
        Failed,
    }
}
