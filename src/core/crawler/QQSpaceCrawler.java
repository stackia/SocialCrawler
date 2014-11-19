package core.crawler;

import core.model.QQUserRaw;
import core.storage.QQUserRawStorage;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;

import java.util.List;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public class QQSpaceCrawler extends Crawler<QQUserRaw> {

    /**
     * uin in auth cookie.
     */
    private long uin;

    /**
     * skey in auth cookie.
     */
    private String skey;

    /**
     * Create a new crawler for QQ Space.
     *
     * @param fetcherPool FetchPool to use.
     * @param userStorage UserStorage to use.
     * @param uin uin in auth cookie.
     * @param skey skey in auth cookie.
     */
    public QQSpaceCrawler(FetcherPool fetcherPool, QQUserRawStorage userStorage, long uin, String skey) {
        super(fetcherPool, userStorage);
        this.uin = uin;
        this.skey = skey;
    }

    /**
     * A util function that generates g_tk from skey.
     *
     * @param skey From which g_tk will generate.
     * @return Generated g_tk from skey.
     */
    public static String generateGTK(String skey) {
        int hash = 5381;

        for (int i = 0; i < skey.length(); ++i) {
            hash += (hash << 5) + (int) skey.charAt(i);
        }

        return Integer.toString(hash & 0x7fffffff);
    }

    @Override
    protected List<FetchRequest> generateFetchRequest(QQUserRaw user) {
        PersonalInfoFetchRequest personalInfoFetchRequest = new PersonalInfoFetchRequest(user);

        return null;
    }

    @Override
    protected void onFetchRequestPostExecutionImpl(FetchRequest fetchRequest, String content) {
        if (fetchRequest instanceof PersonalInfoFetchRequest) {
            QQUserRaw user = ((PersonalInfoFetchRequest) fetchRequest).getRelatedModel();
        }
    }

    /**
     * Add the auth cookie (uin/skey) to a http request.
     *
     * @param httpRequest An http request to receive the cookie.
     */
    public void addAuthCookie(HttpRequest httpRequest) {
        httpRequest.addHeader("Cookie", String.format("uin=o%010d; skey=%s", uin, skey));
    }

    private class PersonalInfoFetchRequest extends FetchRequest<QQUserRaw> {
        public PersonalInfoFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://r.qzone.qq.com/cgi-bin/user/cgi_personal_card?uin=%d&fupdate=1&g_tk=%s&format=json", user.getUin(), generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }
}
