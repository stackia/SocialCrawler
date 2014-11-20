package core.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import core.model.QQUser;
import core.model.QQUserRaw;
import core.storage.QQUserRawStorage;
import org.apache.http.HttpRequest;
import org.apache.http.client.methods.HttpGet;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

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
        List<FetchRequest> fetchRequests = new ArrayList<FetchRequest>();

        // If he is the logged in user
        if (user.getUin() == uin) {
            if (user.getRecentVisitorsDetail() == null) {
                fetchRequests.add(new RecentVisitorsDetailFetchRequest(user));
            }
            if (user.getFriends() == null) {
                fetchRequests.add(new FriendsFetchRequest(user));
            }
        }

        // For any user
//        if (user.getPersonalInfo() == null) {
//            fetchRequests.add(new PersonalInfoFetchRequest(user));
//        }

        // If QQ Space is not private
        if (user.getPersonalInfoDetail() == null) {
            fetchRequests.add(new PersonalInfoDetailFetchRequest(user));
        }
        if (user.getMessageBoard() == null) {
            fetchRequests.add(new MessageBoardFetchRequest(user));
        }

        // If he is a friend of the logged in user
//        if (user.getRecentVisitors() == null) {
//            fetchRequests.add(new RecentVisitorsFetchRequest(user));
//        }

        return fetchRequests;
    }

    @Override
    protected void onFetchRequestPostExecutionImpl(FetchRequest fetchRequest, String content) {
        int newUserCount = 0;
        String newUserSource = "";
        QQUserRawStorage userStorage = (QQUserRawStorage) getUserStorage();
        if (fetchRequest instanceof PersonalInfoFetchRequest) {
            QQUserRaw user = ((PersonalInfoFetchRequest) fetchRequest).getRelatedModel();
            user.setPersonalInfo(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Personal info updated.", user.getUin()));
        } else if (fetchRequest instanceof PersonalInfoDetailFetchRequest) {
            QQUserRaw user = ((PersonalInfoDetailFetchRequest) fetchRequest).getRelatedModel();
            user.setPersonalInfoDetail(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Personal info detail updated.", user.getUin()));
        } else if (fetchRequest instanceof MessageBoardFetchRequest) {
            QQUserRaw user = ((MessageBoardFetchRequest) fetchRequest).getRelatedModel();
            user.setMessageBoard(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Message board updated.", user.getUin()));
            newUserSource = "Message Board";
            try {
                JsonElement root = new JsonParser().parse(content);
                JsonArray jsonUserArray = root.getAsJsonObject().get("data").getAsJsonObject().get("commentList").getAsJsonArray();
                for (JsonElement jsonUser : jsonUserArray) {
                    long uin = jsonUser.getAsJsonObject().get("uin").getAsLong();
                    if (userStorage.insertIfNotExisted(uin))
                        ++newUserCount;
                }
            } catch (Exception ignored) {
            }
        } else if (fetchRequest instanceof RecentVisitorsFetchRequest) {
            QQUserRaw user = ((RecentVisitorsFetchRequest) fetchRequest).getRelatedModel();
            user.setRecentVisitors(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Recent visitors updated.", user.getUin()));
            newUserSource = "Recent Visitors";
            try {
                JsonElement root = new JsonParser().parse(content);
                JsonArray jsonUserArray = root.getAsJsonObject().get("data").getAsJsonObject().get("items").getAsJsonArray();
                for (JsonElement jsonUser : jsonUserArray) {
                    long uin = jsonUser.getAsJsonObject().get("uin").getAsLong();
                    if (userStorage.insertIfNotExisted(uin))
                        ++newUserCount;
                }
            } catch (Exception ignored) {
            }
        } else if (fetchRequest instanceof RecentVisitorsDetailFetchRequest) {
            QQUserRaw user = ((RecentVisitorsDetailFetchRequest) fetchRequest).getRelatedModel();
            user.setRecentVisitorsDetail(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Recent visitors detail updated.", user.getUin()));

        } else if (fetchRequest instanceof FriendsFetchRequest) {
            QQUserRaw user = ((FriendsFetchRequest) fetchRequest).getRelatedModel();
            user.setFriends(content);
            userStorage.update(user);
//            System.out.println(String.format("[%d] Friends updated.", user.getUin()));
            newUserSource = "Friends";
            try {
                SAXBuilder saxBuilder = new SAXBuilder();
                Document document = saxBuilder.build(new StringReader(content));
                List<Element> groups = document.getRootElement().getChildren("group");
                for (Element group : groups) {
                    List<Element> friends = group.getChildren("friend");
                    for (Element friend : friends) {
                        long uin = Long.parseLong(friend.getAttributeValue("uin"));
                        if (userStorage.insertIfNotExisted(uin))
                            ++newUserCount;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (newUserCount > 0)
            System.out.println(String.format("[%s] %d new QQ user(s) added.", newUserSource, newUserCount));
    }

    /**
     * Add the auth cookie (uin/skey) to a http request.
     *
     * @param httpRequest An http request to receive the cookie.
     */
    private void addAuthCookie(HttpRequest httpRequest) {
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

    private class PersonalInfoDetailFetchRequest extends FetchRequest<QQUserRaw> {
        public PersonalInfoDetailFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://base.s8.qzone.qq.com/cgi-bin/user/cgi_userinfo_get_all?uin=%d&fupdate=1&g_tk=%s&&format=json", user.getUin(), generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }

    private class MessageBoardFetchRequest extends FetchRequest<QQUserRaw> {
        public MessageBoardFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://m.qzone.qq.com/cgi-bin/new/get_msgb?hostUin=%d&start=0&format=json&num=20&g_tk=%s", user.getUin(), generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }

    private class RecentVisitorsFetchRequest extends FetchRequest<QQUserRaw> {
        public RecentVisitorsFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://g.edu.qzone.qq.com/cgi-bin/friendshow/cgi_get_visitor_simple?uin=%d&mask=2&g_tk=%s&page=1&fupdate=1&format=json", user.getUin(), generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }

    private class RecentVisitorsDetailFetchRequest extends FetchRequest<QQUserRaw> {
        public RecentVisitorsDetailFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://g.edu.qzone.qq.com/cgi-bin/friendshow/cgi_get_visitor_more?uin=%d&mask=7&g_tk=%s&page=1&fupdate=1", user.getUin(), generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }

    private class FriendsFetchRequest extends FetchRequest<QQUserRaw> {
        public FriendsFetchRequest(QQUserRaw user) {
            super(QQSpaceCrawler.this, user);
            HttpGet get = new HttpGet(String.format("http://show.qq.com/cgi-bin/qqshow_user_friendgroup?g_tk=%s", generateGTK(skey)));
            addAuthCookie(get);
            setHttpRequest(get);
        }
    }
}
