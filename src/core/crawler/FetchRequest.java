package core.crawler;

import org.apache.http.client.methods.HttpUriRequest;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
 */
class FetchRequest<T> {

    /**
     * An http request related to this FetchRequest. This is what will be actually executed.
     */
    private HttpUriRequest httpRequest;

    /**
     * The crawler that sent this FetchRequest.
     */
    private Crawler<T> sender;

    /**
     *  State of this FetchRequest.
     */
    private State state = State.Pending;

    /**
     * Related model of this FetchRequest. Usually a user object.
     */
    private T relatedModel;

    /**
     * Create a fetch request.
     *
     * @param sender The crawler that will send this fetch request.
     * @param relatedModel A data model associated with this fetch request.
     */
    public FetchRequest(Crawler<T> sender, T relatedModel) {
        this.sender = sender;
        this.relatedModel = relatedModel;
    }

    /**
     * Create a fetch request.
     *
     * @param httpRequest An http request that will be actually executed.
     * @param sender The crawler that will send this fetch request.
     * @param relatedModel A data model associated with this fetch request.
     */
    public FetchRequest(HttpUriRequest httpRequest, Crawler<T> sender, T relatedModel) {
        this.httpRequest = httpRequest;
        this.sender = sender;
        this.relatedModel = relatedModel;
    }

    public T getRelatedModel() {
        return relatedModel;
    }

    public void setRelatedModel(T relatedModel) {
        this.relatedModel = relatedModel;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public HttpUriRequest getHttpRequest() {
        return httpRequest;
    }

    public void setHttpRequest(HttpUriRequest httpRequest) {
        this.httpRequest = httpRequest;
    }

    public Crawler<T> getSender() {
        return sender;
    }

    /**
     * Represent the state of an fetch request.
     */
    public enum State {
        Pending,
        Successful,
        Failed,
    }
}
