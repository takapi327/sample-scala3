CREATE DATABASE sample_doobie DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE sample_doobie;

DROP TABLE IF EXISTS country;

CREATE TABLE country (
  code       character(3)  NOT NULL,
  name       text          NOT NULL,
  population integer       NOT NULL,
  gnp        numeric(10,2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES country WRITE;
INSERT INTO country VALUES ('USA', 'Afghanistan', 1, 1.1),
                           ('BRA', 'Netherlands', 2, 1.2),
                           ('PAK', 'Netherlands Antilles', 3, 1.3),
                           ('GBR', 'Albania', 4, 1.4);
UNLOCK TABLES;

DROP TABLE IF EXISTS person;

CREATE TABLE person (
  id   SERIAL,
  name VARCHAR(255) NOT NULL UNIQUE,
  age  SMALLINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

LOCK TABLES person WRITE;
INSERT INTO person VALUES (1, 'takapi', 25);
UNLOCK TABLES;
