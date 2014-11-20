# Social Crawler

Use IntelliJ IDEA to open this project.

Database schema:

```
CREATE TABLE `qq_users_raw` (
  `id` int(8) unsigned NOT NULL AUTO_INCREMENT,
  `uin` int(8) unsigned NOT NULL,
  `personal_info` longtext,
  `personal_info_detail` longtext,
  `message_board` longtext,
  `recent_visitors` longtext,
  `recent_visitors_detail` longtext,
  `friends` longtext,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uin` (`uin`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4;
```