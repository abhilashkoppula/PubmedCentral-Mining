-- This file assumes that the pubmedcentral database has been
-- created and just adds the tables used for the MeSH descriptors
-- and qualifiers.  It should eventually be merged into the 
-- schema for the pubmedcentral database after any design 
-- decisions are finalized.

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
-- Table structure for table mesh_keywords
--
DROP TABLE IF EXISTS mesh_keywords;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE mesh_keywords (
  pubmed_id          VARCHAR(45)   NOT NULL,
  id_type            TINYINT       NOT NULL,
  descriptor_id      INT           NOT NULL,
  qualifier_id       INT           NOT NULL DEFAULT 0, -- default to 0 for not a qualifier
  major_topic        TINYINT       NOT NULL DEFAULT 0, -- 1 = true, a major topic of the paper, 0 = false
  PRIMARY KEY (pubmed_id, id_type, descriptor_id, qualifier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table mesh_descriptor
--
DROP TABLE IF EXISTS mesh_descriptor;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE mesh_descriptor (
  descriptor_id      INT           NOT NULL,
  descriptor_name    VARCHAR(45)   NOT NULL,
  date_established   DATETIME      DEFAULT NULL,-- date first used in the MeSH vocabulary
  date_created       DATETIME      DEFAULT NULL,
  PRIMARY KEY (descriptor_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table descriptor_hierarchy
--
DROP TABLE IF EXISTS descriptor_hierarchy;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Although MeSH terms are presented as a tree, they
-- form more of a graph structure in that the same 
-- descriptor can appear in multiple branches of
-- the tree with different parents, and each
-- descriptor can have multiple childeren.
CREATE TABLE descriptor_hierarchy (
  parent_id           INT           NOT NULL,
  child_id            INT           NOT NULL,
  PRIMARY KEY (parent_id, child_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table mesh_qualifier
--
DROP TABLE IF EXISTS mesh_qualifier;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE mesh_qualifier (
  qualifier_id       INT           NOT NULL,
  qualifier_name     VARCHAR(45)   NOT NULL,
  date_established   DATETIME      DEFAULT NULL, -- date first used in the MeSH vocabulary
  date_created       DATETIME      DEFAULT NULL,
  PRIMARY KEY (qualifier_id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table allowed_qualifiers
--
DROP TABLE IF EXISTS allowed_qualifiers;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Qualifiers can be used to qualify multiple descriptors
-- but are only valid for some descriptors.
CREATE TABLE allowed_qualifiers (
  descriptor_id      INT           NOT NULL,
  qualifier_id       INT           NOT NULL,
  PRIMARY KEY (descriptor_id, qualifier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table descriptor_concept
--
DROP TABLE IF EXISTS descriptor_concept;
-- Each descriptor can be defined by multiple concepts
-- but only one concept is the preferred concept
-- for a descriptor.
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Each descriptor can be defined using one or more concepts
-- but one of the concepts is the preferred concept for 
-- the descriptor.
CREATE TABLE descriptor_concept (
  descriptor_id      INT           NOT NULL,
  concept_id         INT           NOT NULL,
  preferred_flag     TINYINT       NOT NULL DEFAULT 0, -- 1 = true (preferred), 0 = false
  PRIMARY KEY (descriptor_id, concept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table qualifier_concept
--
DROP TABLE IF EXISTS qualifier_concept;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Each qualifier can be defined using one or more concepts
-- but one of the concepts is the preferred concept for 
-- the qualifier.
CREATE TABLE qualifier_concept (
  qualifier_id       INT           NOT NULL,
  concept_id         INT           NOT NULL,
  preferred_flag     TINYINT       NOT NULL DEFAULT 0, -- 1 = true (preferred), 0 = false
  PRIMARY KEY (qualifier_id, concept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table concept
--
DROP TABLE IF EXISTS concept;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Each descriptor or qualifier is defined using one or more 
-- concepts, although one of the concepts is defined as the
-- preferred concept.  The scope_note for the concept is 
-- a free-text description of the concept.  The CASIN1name
-- is a field that is only populated for concepts that 
-- are a chemical.  For concepts related to qualifiers 
-- the casin_name will always be NULL.
CREATE TABLE concept (
  concept_id         INT           NOT NULL,
  concept_name       VARCHAR(45)   NOT NULL,
  scope_note         TEXT          DEFAULT NULL,
  casin_name         TEXT          DEFAULT NULL,
  PRIMARY KEY (concept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table concept_term
--
DROP TABLE IF EXISTS concept_term;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Each concept has one or more terms, where one term is
-- the preferred term and the other terms are synonyms.
-- These are different from related concepts in that
-- terms within the same concept are considered to be
-- true synonyms and not just related.
CREATE TABLE concept_term (
  concept_id         INT           NOT NULL,
  term_id            INT           NOT NULL,
  term_name          VARCHAR(45)   NOT NULL,
  preferred_flag     TINYINT       NOT NULL DEFAULT 0, -- 1 = true (preferred), 0 = false
  PRIMARY KEY (concept_id, term_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
/*!40101 SET character_set_client = @saved_cs_client */;


--
-- Table structure for table concept_concept
--
DROP TABLE IF EXISTS concept_concept;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
-- Concepts can be related to another concept where one is the
-- preferred concept for a descriptor or qualifier and then 
-- the other concept is either a narrower definition, a 
-- broader definition, or a related concept that is neither
-- more narrow nor broader.
CREATE TABLE concept_concept (
  pref_concept_id    INT           NOT NULL,
  rel_concept_id     INT           NOT NULL,
  rel_type           TINYINT       NOT NULL DEFAULT 0, -- 0 = none, 1 = narrower, 2 = broader, 3 = related, but not narrower or broader
  PRIMARY KEY (pref_concept_id, rel_concept_id)
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


