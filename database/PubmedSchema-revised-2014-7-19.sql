CREATE DATABASE  IF NOT EXISTS pubmedcentral 
DEFAULT CHARACTER SET = utf8mb4
DEFAULT COLLATE = utf8mb4_unicode_ci;

USE pubmedcentral;
-- MySQL dump 10.13  Distrib 5.6.13, for Win32 (x86)
--
-- Host: rdc04.uits.iu.edu    Database: pubmedcentral
-- ------------------------------------------------------
-- Server version	5.5.8-enterprise-commercial-advanced

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `article`
--

DROP TABLE IF EXISTS article;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE article (
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  pub_date           DATETIME      DEFAULT NULL,
  pub_date_type      VARCHAR(10)   DEFAULT NULL,
  article_title      VARCHAR(1000) DEFAULT NULL,
  abstract_text      TEXT          DEFAULT NULL,
  conference_id      INT           DEFAULT NULL,
  volume_id          INT           DEFAULT NULL,
  PRIMARY KEY (pubmed_id, id_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `author`
--

DROP TABLE IF EXISTS author;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE author (
  id                 INT           NOT NULL,
  given_name         VARCHAR(50)   DEFAULT NULL,
  surname            VARCHAR(50)   DEFAULT NULL,
  email              VARCHAR(100)  DEFAULT NULL,
  affiliation        VARCHAR(250)  DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `author_reference`
--

DROP TABLE IF EXISTS author_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE author_reference (
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  author_id          INT           NOT NULL,
  -- role               VARCHAR(45)   DEFAULT NULL,
  -- comments           VARCHAR(100)  DEFAULT NULL,
  PRIMARY KEY (pubmed_id, id_type, author_id)
) ENGINE=InnoDB AUTO_INCREMENT=86 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS category;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE category (
  category_id          INT           NOT NULL,
  parent_category_id   int           DEFAULT NULL,
  subject              VARCHAR(45)   NOT NULL,
  -- series_title         VARCHAR(45)   DEFAULT NULL,
  -- series_text          VARCHAR(45)   DEFAULT NULL,
  PRIMARY KEY (category_id),
  KEY fk_parent_category_idx (parent_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category_reference`
--

DROP TABLE IF EXISTS category_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE category_reference (
  pubmed_id          VARCHAR(45)   NOT NULL,
  type_id            TINYINT       NOT NULL,
  category_id        INT           NOT NULL,
  -- dummy_col          VARCHAR(10)   DEFAULT NULL,
  PRIMARY KEY (pubmed_id, type_id, category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citation`
--

DROP TABLE IF EXISTS citation;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE citation (
  citation_id        INT           NOT NULL,
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  cited_pubmed_id    VARCHAR(45)   NOT NULL,
  cited_id_type      TINYINT       NOT NULL DEFAULT 1,
  -- publication_type varchar(45) DEFAULT NULL,
  -- publication_format varchar(45) DEFAULT NULL,
  -- publisher_type varchar(45) DEFAULT NULL,
  -- source varchar(250) DEFAULT NULL,
  -- authors varchar(256) DEFAULT NULL,
  -- article_title varchar(1000) DEFAULT NULL,
  -- volume varchar(45) DEFAULT NULL,
  -- issue varchar(45) DEFAULT NULL,
  -- name varchar(45) DEFAULT NULL,
  -- date date DEFAULT NULL,
  PRIMARY KEY (citation_id),
  UNIQUE KEY (pubmed_id, id_type, cited_pubmed_id, cited_id_type),
  KEY cited (cited_pubmed_id, cited_id_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `citation_reference`
--

DROP TABLE IF EXISTS citation_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE citation_reference (
  citation_id        INT           NOT NULL,  -- from the citation table
  reference_id       INT           NOT NULL,  -- each reference within the document is incremented
  left_text          TEXT,
  right_text         TEXT,
  PRIMARY KEY (citation_id, reference_id),
  KEY fk_citation_id_idx (citation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `conference`
--

DROP TABLE IF EXISTS conference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE conference (
  conference_id      INT           NOT NULL,
  conf_date          VARCHAR(45)   DEFAULT NULL,
  name               VARCHAR(45)   DEFAULT NULL,
  num                VARCHAR(45)   DEFAULT NULL,
  loc                VARCHAR(45)   DEFAULT NULL,
  sponsor            VARCHAR(45)   DEFAULT NULL,
  theme              VARCHAR(45)   DEFAULT NULL,
  acronym            VARCHAR(45)   DEFAULT NULL,
  full_name          VARCHAR(100)  DEFAULT NULL,
  PRIMARY KEY (conference_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `filtered_pubmed_reference`
--

DROP TABLE IF EXISTS filtered_pubmed_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE filtered_pubmed_reference (
  pubmed_id int(11) NOT NULL,
  cited_pubmed_id int(11) NOT NULL,
  count int(11) DEFAULT NULL,
  PRIMARY KEY (pubmed_id,cited_pubmed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keyword`
--

DROP TABLE IF EXISTS keyword;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE keyword (
  keyword_id         INT           NOT NULL,
  keyword_text       VARCHAR(250)  DEFAULT NULL,
  count              INT           DEFAULT NULL,
  PRIMARY KEY (keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `keyword_reference`
--

DROP TABLE IF EXISTS keyword_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE keyword_reference (
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  keyword_id         INT           NOT NULL,
  PRIMARY KEY (pubmed_id, id_type, keyword_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `original_keywords`
--

DROP TABLE IF EXISTS original_keywords;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE original_keywords (
  original_keyword varchar(200) DEFAULT NULL,
  group_id int(11) DEFAULT NULL,
  frequency int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pubmed_reference`
--

DROP TABLE IF EXISTS pubmed_reference;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE pubmed_reference (
  pubmed_id int(11) NOT NULL DEFAULT '0',
  cited_pubmed_id int(11) NOT NULL DEFAULT '0',
  count int(11) DEFAULT NULL,
  PRIMARY KEY (pubmed_id,cited_pubmed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stemmed_keywords`
--

DROP TABLE IF EXISTS stemmed_keywords;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE stemmed_keywords (
  group_id int(11) DEFAULT NULL,
  stemmed_word varchar(200) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `unstructured_keyword_group`
--

DROP TABLE IF EXISTS unstructured_keyword_group;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE unstructured_keyword_group (
  id int(11) NOT NULL,
  pubmed_id varchar(45) NOT NULL,
  unstructured_keyword varchar(256) NOT NULL,
  PRIMARY KEY (id,pubmed_id),
  KEY fk_pubmed_id_idx (pubmed_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `volume`
--

DROP TABLE IF EXISTS volume;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE volume (
  volume_id          INT           NOT NULL,
  volume             VARCHAR(45)   DEFAULT NULL,
  issue              VARCHAR(45)   DEFAULT NULL,
  journal_id         VARCHAR(100)  DEFAULT NULL,
  journal_title      VARCHAR(200)  DEFAULT NULL,
  publisher_name     VARCHAR(200)  DEFAULT NULL,
  PRIMARY KEY (volume_id)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


DROP TABLE IF EXISTS article_meshterm;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE article_meshterm (
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  descriptor_name    VARCHAR(45)   NOT NULL,
  descriptor_id      INT           NOT NULL, 
  qualifier_name     VARCHAR(45)   DEFAULT NULL, -- not all descriptors will have a qualifier
  qualifier_id       INT           NOT NULL, -- if there is no qualifier for a descriptor, this will be 0
  major_topic        TINYINT       DEFAULT 0, -- 0 = not a major topic
  PRIMARY KEY (pubmed_id, id_type, descriptor_id, qualifier_id),
  KEY topic (descriptor_name,qualifier_name),
  KEY topic_id ( descriptor_id, qualifier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed
