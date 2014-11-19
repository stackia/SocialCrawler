package core.model;

/**
 * Project: SocialCrawler
 * Package: core.model
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public class QQUserRaw {
    private long uin;
    private String personalInfo;
    private String personalInfoDetail;
    private String messageBoard;
    private String recentVisitors;
    private String recentVisitorsDetail;
    private String friends;

    public QQUserRaw() {
    }

    public QQUserRaw(long uin) {
        this.uin = uin;
    }

    public long getUin() {
        return uin;
    }

    public void setUin(long uin) {
        this.uin = uin;
    }

    public String getPersonalInfo() {
        return personalInfo;
    }

    public void setPersonalInfo(String personalInfo) {
        this.personalInfo = personalInfo;
    }

    public String getPersonalInfoDetail() {
        return personalInfoDetail;
    }

    public void setPersonalInfoDetail(String personalInfoDetail) {
        this.personalInfoDetail = personalInfoDetail;
    }

    public String getMessageBoard() {
        return messageBoard;
    }

    public void setMessageBoard(String messageBoard) {
        this.messageBoard = messageBoard;
    }

    public String getRecentVisitors() {
        return recentVisitors;
    }

    public void setRecentVisitors(String recentVisitors) {
        this.recentVisitors = recentVisitors;
    }

    public String getRecentVisitorsDetail() {
        return recentVisitorsDetail;
    }

    public void setRecentVisitorsDetail(String recentVisitorsDetail) {
        this.recentVisitorsDetail = recentVisitorsDetail;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }
}
