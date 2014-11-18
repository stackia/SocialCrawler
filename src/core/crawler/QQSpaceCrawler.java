package core.crawler;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public class QQSpaceCrawler extends Crawler {

    public QQSpaceCrawler(FetcherPool fetcherPool) {
        super(fetcherPool);
    }

    @Override
    protected void generateFetchRequest() {

    }

    @Override
    protected void onFetchRequestPostExecutionImpl(FetchRequest fetchRequest, String content) {

    }
}
