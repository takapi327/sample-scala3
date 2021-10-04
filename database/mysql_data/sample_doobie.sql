CREATE DATABASE `sample_doobie` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `sample_doobie`;

DROP TABLE IF EXISTS `country`;

CREATE TABLE country (
  code       character(3)  NOT NULL,
  name       text          NOT NULL,
  population integer       NOT NULL,
  gnp        numeric(10,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES `country` WRITE;
INSERT INTO `country` VALUES ('1', 'Afghanistan', 1, 1.1),
                             ('2', 'Netherlands', 2, 1.2),
                             ('3', 'Netherlands Antilles', 3, 1.3),
                             ('4', 'Albania', 4, 1.4);