-- 电影评分系统建表脚本（TiDB / MySQL 通用精简版）
-- 用法：在 TiDB Cloud SQL Editor 中整段粘贴执行，或 mysql 客户端 source 导入
-- 建表顺序已按外键依赖排列：被引用的表先建

CREATE DATABASE IF NOT EXISTS `movie_info1707` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `movie_info1707`;

DROP TABLE IF EXISTS `user1707`;
CREATE TABLE `user1707` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Username` varchar(50) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `RegisterTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Username` (`Username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `movie1707`;
CREATE TABLE `movie1707` (
  `MovieID` int NOT NULL AUTO_INCREMENT,
  `Title` varchar(100) NOT NULL,
  `ReleaseYear` int DEFAULT NULL,
  `Duration` int DEFAULT NULL,
  `Genre` varchar(50) DEFAULT NULL,
  `Language` varchar(50) DEFAULT NULL,
  `Country` varchar(50) DEFAULT NULL,
  `Synopsis` text,
  `Rating` decimal(3,1) DEFAULT '0.0',
  `PosterUrl` varchar(255) DEFAULT NULL,
  `JumpUrl` varchar(255) DEFAULT NULL COMMENT '跳转URL',
  PRIMARY KEY (`MovieID`),
  KEY `idx_movie_title` (`Title`),
  KEY `idx_movie_rating` (`Rating`),
  KEY `idx_movie_year` (`ReleaseYear`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `actor1707`;
CREATE TABLE `actor1707` (
  `ActorID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL,
  `Gender` char(1) DEFAULT '男',
  `BirthDate` date DEFAULT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  `PhotoUrl` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ActorID`),
  CONSTRAINT `actor1707_chk_1` CHECK (`Gender` in ('男','女'))
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `director1707`;
CREATE TABLE `director1707` (
  `DirectorID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL,
  `Gender` char(1) DEFAULT '男',
  `BirthDate` date DEFAULT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`DirectorID`),
  CONSTRAINT `director1707_chk_1` CHECK (`Gender` in ('男','女'))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `comment1707`;
CREATE TABLE `comment1707` (
  `CommentID` int NOT NULL AUTO_INCREMENT,
  `UserID` int DEFAULT NULL,
  `MovieID` int DEFAULT NULL,
  `Content` text NOT NULL,
  `CommentTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`CommentID`),
  KEY `UserID` (`UserID`),
  KEY `MovieID` (`MovieID`),
  CONSTRAINT `comment1707_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user1707` (`UserID`),
  CONSTRAINT `comment1707_ibfk_2` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `rating1707`;
CREATE TABLE `rating1707` (
  `RatingID` int NOT NULL AUTO_INCREMENT,
  `UserID` int DEFAULT NULL,
  `MovieID` int DEFAULT NULL,
  `Score` int DEFAULT NULL,
  `RatingTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`RatingID`),
  UNIQUE KEY `UserID` (`UserID`,`MovieID`),
  KEY `MovieID` (`MovieID`),
  CONSTRAINT `rating1707_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user1707` (`UserID`),
  CONSTRAINT `rating1707_ibfk_2` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`),
  CONSTRAINT `rating1707_chk_1` CHECK (`Score` between 1 and 10)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `movie_actor1707`;
CREATE TABLE `movie_actor1707` (
  `MovieID` int NOT NULL,
  `ActorID` int NOT NULL,
  `RoleName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`MovieID`,`ActorID`),
  KEY `ActorID` (`ActorID`),
  CONSTRAINT `movie_actor1707_ibfk_1` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE,
  CONSTRAINT `movie_actor1707_ibfk_2` FOREIGN KEY (`ActorID`) REFERENCES `actor1707` (`ActorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP TABLE IF EXISTS `movie_director1707`;
CREATE TABLE `movie_director1707` (
  `MovieID` int NOT NULL,
  `DirectorID` int NOT NULL,
  PRIMARY KEY (`MovieID`,`DirectorID`),
  KEY `DirectorID` (`DirectorID`),
  CONSTRAINT `movie_director1707_ibfk_1` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE,
  CONSTRAINT `movie_director1707_ibfk_2` FOREIGN KEY (`DirectorID`) REFERENCES `director1707` (`DirectorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

DROP VIEW IF EXISTS `v_hot_movies`;
CREATE VIEW `v_hot_movies` AS
SELECT `m`.`MovieID` AS `MovieID`,
       `m`.`Title` AS `Title`,
       `m`.`Rating` AS `Rating`,
       `m`.`PosterUrl` AS `PosterUrl`,
       (SELECT COUNT(0) FROM `comment1707` WHERE `comment1707`.`MovieID` = `m`.`MovieID`) AS `CommentCount`
FROM `movie1707` `m`
WHERE `m`.`Rating` >= 8.0
ORDER BY (`m`.`Rating` * LOG(`CommentCount` + 1)) DESC
LIMIT 10;
