package edu.iub.pubmed.utils;

/**
 * Constants used in this project
 * @author Abhilash
 *
 */
public interface Constants {
	
	// Logger Name
	public static final String LOGGER_NAME = "PubmedCentral_Mining";
	
	// Neo4j local dir paths
	public static final String NEO4J_RELEVANCE_GRAPH_PATH = "relevancegraph";
	public static final String NEO4J_HETROGENEOUS_GRAPH_PATH = "hetrogeneousgraph";
	
	// Names of the property of nodes
	public static final String PROPERTY_PUBMED_ID = "pubmedId";
	public static final String PROPERTY_KEYWORD = "keywordText";
	public static final String ARTICLE = "article";
	public static final String PROPERTY_KEYWORDS = "keywords";
	public static final String PROPERTY_WEIGHT = "weight";
	public static final String PROPERTY_PRIOR = "prior";
	
	// Delimiter for DUMP files
	public static final String DELIMITER = ",";
	
	// For the correct syntax of sql file
	public static final String QUERY_CLOSING_CHARACTER = ";";
	
	// Double Object for value 0
	public static final Double DOUBLE_ZERO = new Double(0);
	
	
	// File prefixes for the Dump files
	public static final String FILE_PREFIX_KEYWORD = "keywords\\keyword_";
	public static final String FILE_PREFIX_KEYWORD_REFERENCE = "keywords\\keyword_reference_";
	public static final String FILE_PREFIX_AUTHOR = "authors\\author_";
	public static final String FILE_PREFIX_AUTHOR_REFERENCE = "authors\\author_reference_";
	public static final String FILE_PREFIX_CATEGORY = "category\\category_";
	public static final String FILE_PREFIX_CATEGORY_REFERENCE = "category\\category_reference_";
	public static final String FILE_PREFIX_ARTICLE = "article\\article_";
	public static final String FILE_PREFIX_PUBMED_REFERENCE = "citations\\pubmed_reference_";
	public static final String FILE_PREFIX_VOLUME = "volume\\volume_";
	public static final String FILE_PREFIX_CONFERENCE = "conference\\conference_";
	
	// Values Limit
	public static final int VALUES_LIMIT = 20000;
	// Maximum file size of each dump file
	public static final int MAX_DUMP_FILE_SIZE = 5000; // 5MB
	// Date format
	public static final String DUMP_FILE_DATE_FORMAT = "YYYY-M-dd-HH-mm-SSS";

}
