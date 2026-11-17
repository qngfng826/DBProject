-- 存量库迁移：comment1707 / rating1707 外键补 ON DELETE CASCADE（路线规划待办⑥）
-- 背景：原 4 个外键无 ON DELETE 行为（默认 RESTRICT），管理后台删除有评论/评分的用户或电影会被外键卡住报 500。
-- 适用：已按旧 schema.sql / init.sql 建好的库（本地 MySQL 与 TiDB 生产各执行一次，先备份再执行）。
-- 新库无需执行：schema.sql 已含级联定义。
-- 注意：重复执行会在 DROP FOREIGN KEY 处报 1091（约束不存在），可忽略；同名重建保持约束名不变。
-- TiDB 执行方式见 DEPLOY.md：必须走 TLS + MySQL 8.4.8 客户端（D:\Download\MySQL848\bin\mysql.exe）。

USE `movie_info1707`;

ALTER TABLE `comment1707` DROP FOREIGN KEY `comment1707_ibfk_1`;
ALTER TABLE `comment1707` DROP FOREIGN KEY `comment1707_ibfk_2`;
ALTER TABLE `rating1707` DROP FOREIGN KEY `rating1707_ibfk_1`;
ALTER TABLE `rating1707` DROP FOREIGN KEY `rating1707_ibfk_2`;

ALTER TABLE `comment1707` ADD CONSTRAINT `comment1707_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user1707` (`UserID`) ON DELETE CASCADE;
ALTER TABLE `comment1707` ADD CONSTRAINT `comment1707_ibfk_2` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE;
ALTER TABLE `rating1707` ADD CONSTRAINT `rating1707_ibfk_1` FOREIGN KEY (`UserID`) REFERENCES `user1707` (`UserID`) ON DELETE CASCADE;
ALTER TABLE `rating1707` ADD CONSTRAINT `rating1707_ibfk_2` FOREIGN KEY (`MovieID`) REFERENCES `movie1707` (`MovieID`) ON DELETE CASCADE;

-- 验证：SHOW CREATE TABLE comment1707; / SHOW CREATE TABLE rating1707;
-- 预期 4 个约束均带 ON DELETE CASCADE，rating1707_chk_1（Score 1~10 的 CHECK）不受影响。
