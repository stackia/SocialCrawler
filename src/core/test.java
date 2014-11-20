package core;

import core.crawler.FetcherPool;
import core.crawler.QQSpaceCrawler;
import core.storage.QQUserRawStorage;
import sun.awt.image.URLImageSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Project: SocialCrawler
 * Package: core.crawler
 * Created by Stackia <jsq2627@gmail.com> on 11/20/14.
 */
public class test {
    public static void main(String[] args) throws SQLException, InterruptedException {
        long uin = 1481455339;
        String skey = "@0LnGbfY0J";
        if (args.length == 2) {
            uin = Long.parseLong(args[0]);
            skey = args[1];
        }

        Connection dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "root", "8242627");
        FetcherPool fetcherPool = new FetcherPool(40);
        QQUserRawStorage qqUserRawStorage = new QQUserRawStorage(dbConnection);
        QQSpaceCrawler qqSpaceCrawler = new QQSpaceCrawler(fetcherPool,  qqUserRawStorage, uin, skey);
        qqSpaceCrawler.start();
        Thread.sleep(Integer.MAX_VALUE);
    }
}
