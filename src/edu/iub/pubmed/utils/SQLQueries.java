package edu.iub.pubmed.utils;

/**
 * Insert SQL Queries for the pubmed tables
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public interface SQLQueries {
	
	public static final String QUERY_INSERT_ARTICLE_TABLE = "INSERT INTO article VALUES ";	
	public static final String QUERY_INSERT_AUTHOR_TABLE = "INSERT INTO author VALUES";
	public static final String QUERY_INSERT_AUTHOR_REFERENCE_TABLE = "INSERT INTO author_reference VALUES";
	public static final String QUERY_INSERT_CATEGORY_TABLE = "INSERT INTO category VALUES";
	public static final String QUERY_INSERT_CATEGORY_REFERENCE_TABLE = "INSERT INTO category_reference VALUES";
	public static final String QUERY_INSERT_CONFERENCE_TABLE = "INSERT INTO conference VALUES";
	public static final String QUERY_INSERT_VOLUME_TABLE = "INSERT INTO volume VALUES";
	public static final String QUERY_INSERT_CITATION_TABLE = "INSERT INTO citation VALUES";
	public static final String QUERY_INSERT_PUBMED_REFERENCE_TABLE = "INSERT INTO citation_reference VALUES";
	public static final String QUERY_INSERT_KEYWORD_TABLE = "INSERT INTO keyword VALUES";
	public static final String QUERY_INSERT_KEYWORD_REFERENCE_TABLE = "INSERT INTO keyword_reference VALUES";
	
	
	//public static final String QUERY_VALUES_ARTICLE_TABLE = "(%s,%s,'%s','%s',%s,%s)";
	//public static final String QUERY_VALUES_AUTHOR_TABLE = "(%s,'%s','%s')";
	public static final String QUERY_VALUES_AUTHOR_REFERENCE_TABLE = "(%s,%s,%s)";
	public static final String QUERY_VALUES_KEYWORD_TABLE = "(%s,'%s',%s)";
	public static final String QUERY_VALUES_KEYWORD_REFERENCE_TABLE = "('%s',%s,%s)";
	//public static final String QUERY_VALUES_VOLUME_TABLE = "(%s,'%s','%s','%s','%s','%s')";
	//public static final String QUERY_VALUES_CONFERENCE_TABLE = "(%s,%s)";
	//public static final String QUERY_VALUES_CATEGORY_TABLE = "(%s,'%s','%s')";
	public static final String QUERY_VALUES_CATEGORY_REFERENCE_TABLE = "(%s,%s,%s)";
	public static final String QUERY_VALUES_CITATION_TABLE = "(%s,%s,%s,%s,%s)";
	public static final String QUERY_VALUES_PUBMED_REFERENCE_TABLE = "(%s,%s,'%s','%s')";
	
	
	
	
}
