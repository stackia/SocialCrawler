package core.storage;

import core.model.QQUserRaw;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Project: SocialCrawler
 * Package: core.storage
 * Created by Stackia <jsq2627@gmail.com> on 11/19/14.
 */
public class QQUserRawStorage implements UserStorage<QQUserRaw> {

    /**
     * The database connection this storage will use.
     */
    private Connection dbConnection;

    /**
     * Create a storage of QQ users from a database connection.
     *
     * @param dbConnection The database connection this storage will use.
     */
    public QQUserRawStorage(Connection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public boolean insert(QQUserRaw newUser) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("INSERT INTO social_spider.qq_users_raw (uin, personal_info, personal_info_detail, message_board, recent_visitors, recent_visitors_detail, friends) VALUES (?, ?, ?, ?, ?, ?, ?);");
            statement.setLong(1, newUser.getUin());
            statement.setString(2, newUser.getPersonalInfo());
            statement.setString(3, newUser.getPersonalInfoDetail());
            statement.setString(4, newUser.getMessageBoard());
            statement.setString(5, newUser.getRecentVisitors());
            statement.setString(6, newUser.getRecentVisitorsDetail());
            statement.setString(7, newUser.getFriends());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean insert(long uin) {
        QQUserRaw newUser = new QQUserRaw(uin);
        return insert(newUser);
    }

    @Override
    public boolean delete(QQUserRaw userToDelete) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("DELETE FROM social_spider.qq_users_raw WHERE uin = ?;");
            statement.setLong(1, userToDelete.getUin());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(long uin) {
        QQUserRaw userToDelete = new QQUserRaw(uin);
        return delete(userToDelete);
    }

    @Override
    public QQUserRaw find(long offset) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT * FROM social_spider.qq_users_raw LIMIT 1 OFFSET ?");
            statement.setLong(1, offset);
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.first()) {
                return null;
            }
            QQUserRaw user = new QQUserRaw(resultSet.getLong("uin"));
            user.setPersonalInfo(resultSet.getString("personal_info"));
            user.setPersonalInfoDetail(resultSet.getString("personal_info_detail"));
            user.setMessageBoard(resultSet.getString("message_board"));
            user.setRecentVisitors(resultSet.getString("recent_visitors"));
            user.setRecentVisitorsDetail(resultSet.getString("recent_visitors_detail"));
            user.setFriends(resultSet.getString("friends"));
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean exists(QQUserRaw user) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("SELECT uin FROM social_spider.qq_users_raw WHERE uin = ?;");
            statement.setLong(1, user.getUin());
            ResultSet resultSet = statement.executeQuery();
            return resultSet.first();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean exists(long uin) {
        QQUserRaw user = new QQUserRaw(uin);
        return exists(user);
    }

    @Override
    public boolean update(QQUserRaw user) {
        try {
            PreparedStatement statement = dbConnection.prepareStatement("UPDATE social_spider.qq_users_raw SET personal_info = ?, personal_info_detail = ?, message_board = ?, recent_visitors = ?, recent_visitors_detail = ?, friends = ?  WHERE uin = ?;");
            statement.setString(1, user.getPersonalInfo());
            statement.setString(2, user.getPersonalInfoDetail());
            statement.setString(3, user.getMessageBoard());
            statement.setString(4, user.getRecentVisitors());
            statement.setString(5, user.getRecentVisitorsDetail());
            statement.setString(6, user.getFriends());
            statement.setLong(7, user.getUin());
            return statement.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
