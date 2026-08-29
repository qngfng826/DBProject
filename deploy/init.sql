-- MySQL dump 10.13  Distrib 9.6.0, for Win64 (x86_64)
--
-- Host: localhost    Database: movie_info1707
-- ------------------------------------------------------
-- Server version	9.6.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `movie_info1707`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `movie_info1707` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `movie_info1707`;

--
-- Table structure for table `actor1707`
--

DROP TABLE IF EXISTS `actor1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `actor1707` (
  `ActorID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL,
  `Gender` char(1) DEFAULT '男',
  `BirthDate` date DEFAULT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  `PhotoUrl` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`ActorID`),
  CONSTRAINT `actor1707_chk_1` CHECK ((`Gender` in (_utf8mb4'男',_utf8mb4'女')))
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `actor1707`
--

LOCK TABLES `actor1707` WRITE;
/*!40000 ALTER TABLE `actor1707` DISABLE KEYS */;
INSERT INTO `actor1707` VALUES (1,'蒂姆·罗宾斯','男','1958-10-16','美国',NULL),(2,'摩根·弗里曼','男','1937-06-01','美国',NULL),(3,'张国荣','男','1956-09-12','中国香港',NULL),(4,'张丰毅','男','1956-09-01','中国',NULL),(5,'巩俐','女','1965-12-31','中国',NULL),(6,'柊瑠美','女','1987-08-01','日本',NULL),(7,'入野自由','男','1988-02-19','日本',NULL),(8,'夏木真理','女','1952-08-26','日本',NULL),(9,'莱昂纳多·迪卡普里奥','男','1974-11-11','美国',NULL),(10,'约瑟夫·高登-莱维特','男','1981-02-17','美国',NULL),(11,'马龙·白兰度','男','1924-04-03','美国',NULL),(12,'阿尔·帕西诺','男','1940-04-25','美国',NULL),(13,'汤姆·汉克斯','男','1956-07-09','美国',NULL),(14,'罗宾·怀特','女','1966-04-08','美国',NULL),(15,'理查·基尔','男','1949-08-31','美国',NULL),(16,'莎拉·罗默尔','女','1978-06-04','美国',NULL),(17,'菲利普·努瓦雷','男','1930-04-12','法国',NULL),(18,'萨瓦特利·卡西欧','男','1976-01-20','意大利',NULL),(19,'让·雷诺','男','1948-07-30','法国',NULL),(20,'娜塔莉·波特曼','女','1981-06-09','美国',NULL),(21,'玛德琳·卡罗尔','女','1996-03-16','美国',NULL),(22,'卡兰·麦克奥利菲','男','1995-01-24','澳大利亚',NULL),(23,'凯特·温斯莱特','女','1975-10-05','英国',''),(24,'马修·麦康纳','男','1969-11-04','美国',''),(25,'安妮·海瑟薇','女','1982-11-12','美国',''),(26,'王梓','男','1986-04-13','中国',''),(27,'陶典','女','1992-10-05','中国',''),(28,'沈达威','男','1984-08-24','中国',''),(29,'徐娇','女','1997-08-05','中国','');
/*!40000 ALTER TABLE `actor1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `comment1707`
--

DROP TABLE IF EXISTS `comment1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `comment1707`
--

LOCK TABLES `comment1707` WRITE;
/*!40000 ALTER TABLE `comment1707` DISABLE KEYS */;
INSERT INTO `comment1707` VALUES (1,1,1,'希望是美好的事物，也许是世上最美好的事物，美好的事物从不消逝。','2026-08-26 16:17:36'),(2,2,1,'每次看都有新的感悟，真正的经典永不过时。','2026-08-26 16:17:36'),(3,1,2,'不疯魔不成活，张国荣之后再无程蝶衣。','2026-08-26 16:17:36'),(4,2,2,'华语电影的巅峰，至今无人超越。','2026-08-26 16:17:36'),(5,1,3,'宫崎骏最好的作品，每次重温都会被不同的细节打动。','2026-08-26 16:17:36'),(6,1,4,'诺兰把梦境拍成了艺术，层层嵌套的结构令人叹为观止。','2026-08-26 16:17:36'),(7,2,5,'我会给他一个无法拒绝的条件。影史最伟大的黑帮电影。','2026-08-26 16:17:36'),(8,1,6,'人生就像一盒巧克力，你永远不知道下一颗是什么味道。','2026-08-26 16:17:36'),(9,1,7,'狗狗的世界只有你，看完哭得不能自已。','2026-08-26 16:17:36'),(10,2,8,'生活和电影不一样，生活难多了。艾佛特的话句句戳心。','2026-08-26 16:17:36'),(11,1,9,'里昂和马蒂尔达之间那种超越年龄的感情，纯粹而令人心碎。','2026-08-26 16:17:36'),(12,2,10,'斯人若彩虹，遇上方知有。最美好的初恋电影。','2026-08-26 16:17:36'),(16,2,6,'就喜欢阿甘的这种精神！！！','2026-08-27 16:17:09'),(17,1,11,'生死两茫茫 不思量 自难忘','2026-08-27 22:48:26'),(18,1,13,'好科幻','2026-08-28 11:43:34');
/*!40000 ALTER TABLE `comment1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `director1707`
--

DROP TABLE IF EXISTS `director1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `director1707` (
  `DirectorID` int NOT NULL AUTO_INCREMENT,
  `Name` varchar(50) NOT NULL,
  `Gender` char(1) DEFAULT '男',
  `BirthDate` date DEFAULT NULL,
  `Nationality` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`DirectorID`),
  CONSTRAINT `director1707_chk_1` CHECK ((`Gender` in (_utf8mb4'男',_utf8mb4'女')))
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `director1707`
--

LOCK TABLES `director1707` WRITE;
/*!40000 ALTER TABLE `director1707` DISABLE KEYS */;
INSERT INTO `director1707` VALUES (1,'弗兰克·德拉邦特','男','1959-01-28','美国'),(2,'陈凯歌','男','1952-08-12','中国'),(3,'宫崎骏','男','1941-01-05','日本'),(4,'克里斯托弗·诺兰','男','1970-07-30','英国'),(5,'弗朗西斯·福特·科波拉','男','1939-04-07','美国'),(6,'罗伯特·泽米吉斯','男','1952-05-14','美国'),(7,'拉斯·霍尔斯道姆','男','1946-06-02','瑞典'),(8,'朱塞佩·托纳多雷','男','1956-05-27','意大利'),(9,'吕克·贝松','男','1959-03-18','法国'),(10,'罗伯·莱纳','男','1947-03-06','美国'),(11,'詹姆斯·卡梅隆','男','1954-08-16','加拿大'),(12,'宋岳峰','男','1981-05-20','中国');
/*!40000 ALTER TABLE `director1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie1707`
--

DROP TABLE IF EXISTS `movie1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie1707`
--

LOCK TABLES `movie1707` WRITE;
/*!40000 ALTER TABLE `movie1707` DISABLE KEYS */;
INSERT INTO `movie1707` VALUES (1,'肖申克的救赎',1994,142,'剧情/犯罪','英语','美国','一场谋杀案使银行家安迪蒙冤入狱，在肖申克监狱中他用智慧和希望完成了自我救赎。',10.0,'https://www.themoviedb.org/t/p/w1280/Aqo8yM5S5ZEdlcyeBBxj7s0vkTf.jpg','https://www.dytiantang.tv/vod/detail/id/45733.html'),(2,'霸王别姬',1993,171,'剧情/爱情/同性','汉语普通话','中国大陆/中国香港','段小楼与程蝶衣是一对打小一起长大的师兄弟，两人一个演生一个饰旦，一向配合天衣无缝，尤其一出《霸王别姬》更是誉满京城。',9.5,'https://www.themoviedb.org/t/p/w1280/cJNXKybd4l8ILJ7sZTeiLdAIrZd.jpghttps://www.themoviedb.org/t/p/w1280/cJNXKybd4l8ILJ7sZTeiLdAIrZd.jpg','https://www.dytiantang.tv/vod/detail/id/84216.html'),(3,'千与千寻',2001,125,'剧情/动画/奇幻','日语','日本','千寻和爸爸妈妈一同驱车前往新家，误入了一个神秘的隧道，来到了一个诡异的中世纪小镇。',9.5,'https://image.tmdb.org/t/p/w500/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg','https://www.dytiantang.tv/vod/detail/id/17094.html'),(4,'盗梦空间',2010,148,'剧情/科幻/悬疑','英语','美国/英国','道姆·柯布是一位经验老到的窃贼，他能够潜入人们精神最为脆弱的梦境中窃取潜意识中有价值的秘密。',9.0,'https://image.tmdb.org/t/p/w500/ljsZTbVsrQSqZgWeep2B1QiDKuh.jpg','https://www.dytiantang.tv/vod/detail/id/81672.html'),(5,'教父',1972,175,'剧情/犯罪','英语','美国','40年代的美国，\"教父\"维托·柯里昂是黑手党柯里昂家族的首领，带领家族从事非法勾当的同时也是许多弱小平民的保护神。',9.0,'https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg',NULL),(6,'阿甘正传',1994,142,'剧情/爱情','英语','美国','阿甘于二战结束后不久出生在美国南方阿拉巴马州一个闭塞的小镇，他先天弱智但妈妈是一个性格坚强的女性。',9.5,'https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg','https://www.dytiantang.tv/vod/detail/id/43408.html'),(7,'忠犬八公的故事',2009,93,'剧情','英语','美国/英国','大学教授帕克在小镇的火车站拣到一只走失的小狗，冥冥中似乎注定小狗和帕克教授有着某种缘分。',10.0,'https://www.themoviedb.org/t/p/w1280/bJY4HwYR7rrWBqY4t1XofjhGnVh.jpg','https://www.dytiantang.tv/vod/detail/id/116391.html'),(8,'天堂电影院',1988,155,'剧情/爱情','意大利语','意大利/法国','意大利南部小镇，古灵精怪的小男孩多多喜欢看电影，更喜欢看放映师艾佛特放电影。',9.0,'https://image.tmdb.org/t/p/w500/gCI2AeMV4IHSewhJkzsur5MEp6R.jpg','https://www.dytiantang.tv/vod/detail/id/53922.html'),(9,'这个杀手不太冷',1994,110,'剧情/动作/犯罪','英语/法语','法国/美国','里昂是名孤独的职业杀手，受人雇佣。一天邻居家小姑娘马蒂尔达敲开他的房门要求在他那里暂避杀身之祸。',9.5,'https://img9.doubanio.com/view/photo/s_ratio_poster/public/p2913554676.webp','https://www.dytiantang.tv/vod/detail/id/40050.html'),(10,'怦然心动',2010,90,'剧情/喜剧/爱情','英语','美国','布莱斯全家搬到小镇，邻家女孩朱丽前来帮忙并对他一见钟情，心愿是获得他的吻。',9.0,'https://www.themoviedb.org/t/p/w1280/4Nu5blbEXzNm58UKVVZ9qhe3O3y.jpg','https://www.dytiantang.tv/vod/detail/id/111490.html'),(11,'泰坦尼克号',1997,194,'剧情 / 爱情 / 灾难','英语','美国','富家少女罗丝与母亲及未婚夫卡尔坐上了头等舱；另一边，放荡不羁的少年画家杰克也在码头的一场赌博中 赢得了下等舱的船票。罗丝厌倦了上流社会虚伪的生活，不愿嫁给卡尔，打算投海自尽，被杰克救起。很快，美丽活泼的罗丝与英俊开朗的杰克相爱，杰克带罗丝参加下等舱的舞会、为她画像，二人的感情逐渐升温。一个风平浪静的夜晚。泰坦尼克号撞上了冰山，“永不沉没的”泰坦尼克号面临沉船的命运，罗丝和杰克刚萌芽的爱情也将经历生死的考验。',9.0,'https://www.themoviedb.org/t/p/w1280/lFYUkUPcFXDzZzSfkiCDsvHIJxj.jpg','https://www.dytiantang.tv/vod/detail/id/89052.html'),(13,'星际穿越',2014,169,'剧情 / 科幻 / 冒险','英语','美国 / 英国 / 加拿大','近未来的地球黄沙遍野，小麦、秋葵等基础农作物相继因枯萎病灭绝，人类不再像从前那样仰望星空，放纵想象力和灵感的迸发，而是每日在沙尘暴的肆虐下倒数着所剩不多的光景。在家务农的前NASA宇航员库珀接连在女儿墨菲的书房发现奇怪的重力场现象，随即得知在某个未知区域内前NASA成员仍秘密进行一个拯救人类的计划。多年以前土星附近出现神秘虫洞，NASA借机将数名宇航员派遣到遥远的星系寻找适合居住的星球。在布兰德教授的劝说下，库珀忍痛告别了女儿，和其他三名专家教授女儿艾米莉亚·布兰德、罗米利、多伊尔搭乘宇宙飞船前往目前已知的最有希望的三颗星球考察。\n　　他们穿越遥远的星系银河，感受了一小时七年光阴的沧海桑田，窥见了未知星球和黑洞的壮伟与神秘。在浩瀚宇宙的绝望而孤独角落，总有一份超越了时空的笃定情怀将他们紧紧相连……',8.0,'https://www.themoviedb.org/t/p/w1280/spQm5r317XPNHK1941ChWmqzkZs.jpg','https://www.dytiantang.tv/vod/detail/id/50226.html'),(16,'龙之谷：精灵王座',2016,104,'动画','汉语普通话','中国大陆','　　人类少年小鱼受邀前往遥远神秘的精灵王国，参加精灵女王的婚礼。借此机会，他终于可以见到阔别已久的精灵女友莉雅。黑暗精灵携大军突然闯入，胁迫女王交出传说中掌控精灵命脉、拥有至高力量的生命宝玉。在遭到反抗后，黑暗精灵强行劫走了女王。\n　　精灵王国陷入惶恐，被卷入纷争的小鱼为保护莉雅，毅然同精灵们一起踏上营救女王的冒险。可他没有想到的是，这次征途不仅充满危险，还将是对他以及他们间爱情的巨大考验。',7.0,'https://www.themoviedb.org/t/p/w1280/byaBhHMwt9BzYyb4CdVtSY1swzL.jpg','https://www.dytiantang.tv/vod/detail/id/27240.html'),(17,'龙之谷：破晓奇兵',2014,88,'动画 / 奇幻 / 冒险','汉语普通话','中国大陆','不务正业的流浪少年兰伯特意外听到了邪恶的大黑龙手下魔族准备攻占阿尔特里亚大陆的情况，为了保护家乡他与魔物周旋被困，却被游侠巴尔纳救下，加入到了游侠杰兰特、阿尔杰塔的队伍中。精灵一族中的法师首领卡拉秋也带着公主内尔文与弓箭手随从莉亚前来邀请人类国王参战，面对魔物的入侵，他们和游侠队伍以及人类骑士贝斯柯德组成了对付大黑龙的联盟。众人历经困难来到了大黑龙的巢穴，兰伯特与莉亚日久生情，但是他们却不知道杰兰特和阿尔杰塔，以及贝斯柯德各自带着不为人知的秘密……',8.0,'https://www.themoviedb.org/t/p/w1280/g2w3C3qap1btgCvFOJiYki4aNmy.jpg','https://www.dytiantang.tv/vod/detail/id/27247.html');
/*!40000 ALTER TABLE `movie1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie_actor1707`
--

DROP TABLE IF EXISTS `movie_actor1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie_actor1707` (
  `MovieID` int NOT NULL,
  `ActorID` int NOT NULL,
  `RoleName` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`MovieID`,`ActorID`),
  KEY `ActorID` (`ActorID`),
  CONSTRAINT `movie_actor1707_ibfk_1` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE,
  CONSTRAINT `movie_actor1707_ibfk_2` FOREIGN KEY (`ActorID`) REFERENCES `actor1707` (`ActorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_actor1707`
--

LOCK TABLES `movie_actor1707` WRITE;
/*!40000 ALTER TABLE `movie_actor1707` DISABLE KEYS */;
INSERT INTO `movie_actor1707` VALUES (1,1,'安迪·杜佛兰'),(1,2,'瑞德'),(2,3,'程蝶衣'),(2,4,'段小楼'),(2,5,'菊仙'),(3,6,'荻野千寻'),(3,7,'赈早见琥珀主'),(3,8,'汤婆婆'),(4,9,'道姆·柯布'),(4,10,'亚瑟'),(5,11,'维托·柯里昂'),(5,12,'迈克尔·柯里昂'),(6,13,'阿甘'),(6,14,'珍妮'),(7,15,'帕克教授'),(7,16,'安妮'),(8,17,'艾佛特'),(8,18,'多多'),(9,19,'里昂'),(9,20,'马蒂尔达'),(10,21,'朱丽'),(10,22,'布莱斯'),(11,9,'杰克'),(11,23,'罗丝'),(13,24,'库珀'),(13,25,'布兰德'),(16,26,'小鱼'),(16,27,'莉雅'),(17,28,'兰伯特'),(17,29,'莉雅');
/*!40000 ALTER TABLE `movie_actor1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `movie_director1707`
--

DROP TABLE IF EXISTS `movie_director1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `movie_director1707` (
  `MovieID` int NOT NULL,
  `DirectorID` int NOT NULL,
  PRIMARY KEY (`MovieID`,`DirectorID`),
  KEY `DirectorID` (`DirectorID`),
  CONSTRAINT `movie_director1707_ibfk_1` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE,
  CONSTRAINT `movie_director1707_ibfk_2` FOREIGN KEY (`DirectorID`) REFERENCES `director1707` (`DirectorID`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `movie_director1707`
--

LOCK TABLES `movie_director1707` WRITE;
/*!40000 ALTER TABLE `movie_director1707` DISABLE KEYS */;
INSERT INTO `movie_director1707` VALUES (1,1),(2,2),(3,3),(4,4),(13,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10),(11,11),(16,12),(17,12);
/*!40000 ALTER TABLE `movie_director1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `rating1707`
--

DROP TABLE IF EXISTS `rating1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
  CONSTRAINT `rating1707_chk_1` CHECK ((`Score` between 1 and 10))
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `rating1707`
--

LOCK TABLES `rating1707` WRITE;
/*!40000 ALTER TABLE `rating1707` DISABLE KEYS */;
INSERT INTO `rating1707` VALUES (1,1,1,10,'2026-08-27 16:00:12'),(2,1,2,10,'2026-08-26 16:14:22'),(3,1,3,9,'2026-08-26 16:14:22'),(4,1,4,9,'2026-08-26 16:14:22'),(5,1,6,10,'2026-08-26 16:14:22'),(6,1,7,10,'2026-08-26 16:14:22'),(7,1,8,9,'2026-08-26 16:14:22'),(8,1,9,10,'2026-08-26 16:14:22'),(9,2,1,10,'2026-08-26 16:16:05'),(10,2,2,9,'2026-08-26 16:16:05'),(11,2,3,10,'2026-08-26 16:16:05'),(12,2,5,9,'2026-08-26 16:16:05'),(14,2,8,9,'2026-08-26 16:16:05'),(15,2,9,9,'2026-08-26 16:16:05'),(16,2,10,9,'2026-08-26 16:16:05'),(22,2,6,9,'2026-08-27 16:17:29'),(23,1,11,9,'2026-08-27 22:47:33'),(24,1,13,8,'2026-08-28 11:43:25'),(25,1,17,8,'2026-08-28 13:58:17'),(26,1,16,7,'2026-08-28 13:58:30');
/*!40000 ALTER TABLE `rating1707` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_movie_rating_after_insert` AFTER INSERT ON `rating1707` FOR EACH ROW BEGIN
    UPDATE movie1707 
    SET Rating = (SELECT AVG(Score) FROM rating1707 WHERE MovieID = NEW.MovieID)
    WHERE MovieID = NEW.MovieID;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_movie_rating_after_update` AFTER UPDATE ON `rating1707` FOR EACH ROW BEGIN
    UPDATE movie1707 
    SET Rating = (SELECT AVG(Score) FROM rating1707 WHERE MovieID = NEW.MovieID)
    WHERE MovieID = NEW.MovieID;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `trg_update_movie_rating_after_delete` AFTER DELETE ON `rating1707` FOR EACH ROW BEGIN
    UPDATE movie1707 
    SET Rating = (SELECT AVG(Score) FROM rating1707 WHERE MovieID = OLD.MovieID)
    WHERE MovieID = OLD.MovieID;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `user1707`
--

DROP TABLE IF EXISTS `user1707`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user1707` (
  `UserID` int NOT NULL AUTO_INCREMENT,
  `Username` varchar(50) NOT NULL,
  `Password` varchar(100) NOT NULL,
  `Email` varchar(100) DEFAULT NULL,
  `RegisterTime` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserID`),
  UNIQUE KEY `Username` (`Username`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user1707`
--

LOCK TABLES `user1707` WRITE;
/*!40000 ALTER TABLE `user1707` DISABLE KEYS */;
INSERT INTO `user1707` VALUES (1,'admin','$2a$10$2pfXfUau3hNpEZ93/jKB4.j4nRvEgHu9EK9r314lUKcbhfjZta1wu','','2026-08-25 22:17:13'),(2,'123','$2a$10$xQ5FVoYt9G9gJJa/06ecTOCKpYpj8VgaLkwSqXCHg7awq.e6Bq.Mi','','2026-08-26 16:15:45'),(3,'zcode','$2a$10$mIPajMEKhhIK5JTae3NxvOK6LamSJIHFTTj7KdXJjVlcVdepTyeyS','zcode@test.com','2026-08-27 15:44:36');
/*!40000 ALTER TABLE `user1707` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `v_hot_movies`
--

DROP TABLE IF EXISTS `v_hot_movies`;
/*!50001 DROP VIEW IF EXISTS `v_hot_movies`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_hot_movies` AS SELECT 
 1 AS `MovieID`,
 1 AS `Title`,
 1 AS `Rating`,
 1 AS `PosterUrl`,
 1 AS `CommentCount`*/;
SET character_set_client = @saved_cs_client;

--
-- Dumping routines for database 'movie_info1707'
--
/*!50003 DROP PROCEDURE IF EXISTS `sp_query_comments_by_movie` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_query_comments_by_movie`(IN p_title VARCHAR(100))
BEGIN
    SELECT c.CommentID, u.Username, c.Content, c.CommentTime
    FROM comment1707 c
    JOIN user1707 u ON c.UserID = u.UserID
    JOIN movie1707 m ON c.MovieID = m.MovieID
    WHERE m.Title = p_title
    ORDER BY c.CommentTime DESC;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 DROP PROCEDURE IF EXISTS `sp_query_movies_by_actor` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `sp_query_movies_by_actor`(IN p_actor_name VARCHAR(50))
BEGIN
    SELECT m.*
    FROM movie1707 m
    JOIN movie_actor1707 ma ON m.MovieID = ma.MovieID
    JOIN actor1707 a ON ma.ActorID = a.ActorID
    WHERE a.Name = p_actor_name;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Current Database: `movie_info1707`
--

USE `movie_info1707`;

--
-- Final view structure for view `v_hot_movies`
--

/*!50001 DROP VIEW IF EXISTS `v_hot_movies`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_hot_movies` AS select `m`.`MovieID` AS `MovieID`,`m`.`Title` AS `Title`,`m`.`Rating` AS `Rating`,`m`.`PosterUrl` AS `PosterUrl`,(select count(0) from `comment1707` where (`comment1707`.`MovieID` = `m`.`MovieID`)) AS `CommentCount` from `movie1707` `m` where (`m`.`Rating` >= 8.0) order by (`m`.`Rating` * log((`CommentCount` + 1))) desc limit 10 */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-29 10:16:50
