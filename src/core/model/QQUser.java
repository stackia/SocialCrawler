package core.model;

import java.util.Vector;

/**
 * Project: QQSpider
 * Package: PACKAGE_NAME
 * Created by Stackia <jsq2627@gmail.com> on 11/16/14.
 */
public class QQUser {
    public String uin;
    public int depth;

    public Vector<QQUser> relatedUsers; // RelatedUsers = Friends + RecentVisitors + MessageBoardUsers
    public Object profiles; // Profiles = FullProfiles + SimpleProfiles

    public Vector<QQUser> friends;
    public Vector<QQUser> recentVisitors;
    public Vector<QQUser> messageBoardUsers;

    // Original fetched string
    public String originalFriendsData;
    public String originalRecentVisitorsData;
    public String originalMessageBoardData;
    public String originalFullProfilesData;
    public String originalSimpleProfilesData;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof QQUser) {
            QQUser target = (QQUser) obj;
            return uin.equals(target.uin);
        }
        return false;
    }
}
