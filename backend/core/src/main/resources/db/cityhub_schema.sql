-- CityHub local initialization schema (MySQL 5.6+).
-- Intended for a fresh development database named cityhub.
-- The five CityHub core tables use the lightweight activity/ticket model.

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `tb_reservation_order`;
DROP TABLE IF EXISTS `tb_seckill_ticket`;
DROP TABLE IF EXISTS `tb_ticket`;
DROP TABLE IF EXISTS `tb_activity`;
DROP TABLE IF EXISTS `tb_activity_category`;

CREATE TABLE `tb_activity_category` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(32) NOT NULL,
  `icon` varchar(255) NOT NULL,
  `sort` int(11) unsigned NOT NULL DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CityHub activity categories';

CREATE TABLE `tb_activity` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(128) NOT NULL,
  `category_id` bigint(20) unsigned NOT NULL,
  `images` varchar(1024) NOT NULL,
  `area` varchar(128) DEFAULT NULL,
  `address` varchar(255) NOT NULL,
  `x` double unsigned NOT NULL,
  `y` double unsigned NOT NULL,
  `avg_price` bigint(10) unsigned DEFAULT NULL,
  `sold` int(10) unsigned NOT NULL DEFAULT '0',
  `comments` int(10) unsigned NOT NULL DEFAULT '0',
  `score` int(10) unsigned NOT NULL DEFAULT '0',
  `open_hours` varchar(64) DEFAULT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_activity_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CityHub activities';

CREATE TABLE `tb_ticket` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `activity_id` bigint(20) unsigned NOT NULL,
  `title` varchar(255) NOT NULL,
  `sub_title` varchar(255) DEFAULT NULL,
  `rules` varchar(1024) DEFAULT NULL,
  `pay_value` bigint(10) unsigned NOT NULL,
  `actual_value` bigint(10) NOT NULL,
  `type` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_ticket_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CityHub reservation tickets';

CREATE TABLE `tb_seckill_ticket` (
  `ticket_id` bigint(20) unsigned NOT NULL,
  `stock` int(8) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `begin_time` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `end_time` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='limited CityHub reservation tickets';

CREATE TABLE `tb_reservation_order` (
  `id` bigint(20) NOT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `ticket_id` bigint(20) unsigned NOT NULL,
  `pay_type` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `status` tinyint(1) unsigned NOT NULL DEFAULT '1',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `pay_time` timestamp NULL DEFAULT NULL,
  `use_time` timestamp NULL DEFAULT NULL,
  `refund_time` timestamp NULL DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reservation_order_user_ticket` (`user_id`, `ticket_id`),
  KEY `idx_reservation_order_ticket` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='CityHub reservation orders';

-- Community and login tables deliberately remain available to the existing modules.
CREATE TABLE IF NOT EXISTS `tb_user` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `phone` varchar(11) NOT NULL,
  `password` varchar(128) NOT NULL DEFAULT '',
  `nick_name` varchar(32) NOT NULL,
  `icon` varchar(255) NOT NULL DEFAULT '',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_user_info` (
  `user_id` bigint(20) unsigned NOT NULL,
  `city` varchar(64) DEFAULT '',
  `introduce` varchar(128) DEFAULT NULL,
  `fans` int(8) unsigned DEFAULT '0',
  `followee` int(8) unsigned DEFAULT '0',
  `gender` tinyint(1) unsigned DEFAULT '0',
  `birthday` date DEFAULT NULL,
  `credits` int(8) unsigned DEFAULT '0',
  `level` tinyint(1) unsigned DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_blog` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` bigint(20) NOT NULL,
  `activity_id` bigint(20) unsigned DEFAULT NULL,
  `user_id` bigint(20) unsigned NOT NULL,
  `title` varchar(255) NOT NULL,
  `images` varchar(2048) NOT NULL,
  `content` varchar(2048) NOT NULL,
  `liked` int(8) unsigned DEFAULT '0',
  `comments` int(8) unsigned DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_blog_user` (`user_id`), KEY `idx_blog_activity` (`activity_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_blog_comments` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `blog_id` bigint(20) unsigned NOT NULL,
  `parent_id` bigint(20) unsigned NOT NULL DEFAULT '0',
  `answer_id` bigint(20) unsigned NOT NULL DEFAULT '0',
  `content` varchar(255) NOT NULL,
  `liked` int(8) unsigned DEFAULT '0',
  `status` tinyint(1) unsigned DEFAULT '0',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), KEY `idx_blog_comments_blog` (`blog_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `tb_follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) unsigned NOT NULL,
  `follow_user_id` bigint(20) unsigned NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`), UNIQUE KEY `uk_follow_user` (`user_id`, `follow_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tb_activity_category` (`id`, `name`, `icon`, `sort`) VALUES
  (1, '展览', '/types/exhibition.png', 1),
  (2, '音乐', '/types/music.png', 2),
  (3, '市集', '/types/market.png', 3),
  (4, '演出', '/types/performance.png', 4),
  (5, '讲座', '/types/talk.png', 5),
  (6, '手作', '/types/workshop.png', 6),
  (7, '体育', '/types/sport.png', 7),
  (8, '亲子', '/types/family.png', 8);

INSERT INTO `tb_activity` (`id`, `title`, `category_id`, `images`, `area`, `address`, `x`, `y`, `avg_price`, `sold`, `comments`, `score`, `open_hours`) VALUES
  (1, '城市青年创意市集', 3, '/imgs/activities/creative-market.jpg', '黄浦滨江', '苗江路678号', 121.487321, 31.190542, 0, 0, 0, 50, '周末 10:00-18:00'),
  (2, '夏日爵士音乐会', 2, '/imgs/activities/summer-jazz.jpg', '徐汇滨江', '龙腾大道2555号', 121.456780, 31.178930, 12800, 0, 0, 50, '周六 19:30-21:00'),
  (3, '当代摄影艺术展', 1, '/imgs/activities/photo-exhibition.jpg', '静安', '胶州路699号', 121.442110, 31.228640, 6800, 0, 0, 50, '每日 10:00-18:00'),
  (4, '周末陶艺体验课', 6, '/imgs/activities/ceramic-workshop.jpg', '长宁', '愚园路111号', 121.426220, 31.224860, 9800, 0, 0, 50, '周日 14:00-16:30'),
  (5, '城市文化主题讲座', 5, '/imgs/activities/city-talk.jpg', '浦东', '世纪大道100号', 121.517020, 31.238430, 0, 0, 0, 50, '周三 19:00-20:30');

INSERT INTO `tb_ticket` (`id`, `activity_id`, `title`, `sub_title`, `rules`, `pay_value`, `actual_value`, `type`, `status`) VALUES
  (1, 1, '创意市集预约凭证', '免费入场，限量预约', '每位用户限预约一次', 0, 0, 1, 1),
  (2, 2, '爵士音乐会预约凭证', '限量座位预约', '每位用户限预约一次', 12800, 12800, 1, 1),
  (3, 3, '摄影展预约凭证', '展览入场预约', '每位用户限预约一次', 6800, 6800, 1, 1),
  (4, 4, '陶艺体验预约凭证', '含基础材料', '每位用户限预约一次', 9800, 9800, 1, 1),
  (5, 5, '文化讲座预约凭证', '免费讲座', '每位用户限预约一次', 0, 0, 1, 1);

INSERT INTO `tb_seckill_ticket` (`ticket_id`, `stock`, `begin_time`, `end_time`) VALUES
  (1, 100, '2030-01-01 00:00:00', '2030-12-31 23:59:59'),
  (2, 80, '2030-01-01 00:00:00', '2030-12-31 23:59:59'),
  (3, 120, '2030-01-01 00:00:00', '2030-12-31 23:59:59'),
  (4, 24, '2030-01-01 00:00:00', '2030-12-31 23:59:59'),
  (5, 150, '2030-01-01 00:00:00', '2030-12-31 23:59:59');

INSERT IGNORE INTO `tb_user` (`id`, `phone`, `nick_name`, `icon`) VALUES
  (1, '13600000001', '城市漫游者', ''),
  (2, '13600000002', '周末探索家', '');

INSERT IGNORE INTO `tb_blog` (`id`, `shop_id`, `activity_id`, `user_id`, `title`, `images`, `content`, `liked`, `comments`) VALUES
  (1, 0, 1, 1, '城市青年创意市集体验分享', '/imgs/activities/creative-market.jpg', '在黄浦滨江逛创意市集的一日记录。', 0, 0),
  (2, 0, 2, 2, '夏日爵士音乐会现场记录', '/imgs/activities/summer-jazz.jpg', '徐汇滨江的夏夜爵士现场氛围很好。', 0, 0),
  (3, 0, 3, 1, '当代摄影艺术展观展笔记', '/imgs/activities/photo-exhibition.jpg', '记录这次摄影展中最喜欢的作品。', 0, 0),
  (4, 0, 4, 2, '周末陶艺体验课体验', '/imgs/activities/ceramic-workshop.jpg', '第一次拉坯的周末体验与小建议。', 0, 0);

SET FOREIGN_KEY_CHECKS = 1;
