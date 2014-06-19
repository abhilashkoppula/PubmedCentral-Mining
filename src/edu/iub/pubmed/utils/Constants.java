package edu.iub.pubmed.utils;

import java.io.File;

import org.w3c.dom.Element;

import edu.iub.pubmed.PubmedCentral;

/**
 * Constants used in this project
 * @author Abhilash
 *
 */
public class Constants {
	// Logger Name
	public static final String LOGGER_NAME = "PubmedCentral_Mining";
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
	
	public static String JAR_PATH = PubmedCentral.class.getProtectionDomain().getCodeSource().getLocation().getPath();
	
	
	//public static  String PUBMED_DIR = PubmedCentral.class.getProtectionDomain().getCodeSource().getLocation()+"/../pubmed";
	public static  String PUBMED_DIR = new File(JAR_PATH).getParentFile().getAbsolutePath()+"/pubmed";
	// Neo4j local dir paths
	public static  String GRAPH_DIR = PUBMED_DIR+"/graph";
	public static  String DUMP_DIR = PUBMED_DIR+"/dumpfiles";
	public static  String LOGS_DIR = PUBMED_DIR+"/logs";
	
	public static  String NEO4J_RELEVANCE_GRAPH_PATH = GRAPH_DIR+"/relevancegraph";
	public static  String NEO4J_HETROGENEOUS_GRAPH_PATH = GRAPH_DIR+"/hetrogeneousgraph";
	
	
	// File prefixes for the Dump files
	public static  String FILE_PREFIX_KEYWORD = DUMP_DIR+"/keywords/keyword_";
	public static  String FILE_PREFIX_KEYWORD_REFERENCE = DUMP_DIR+"/keywords/keyword_reference_";
	public static  String FILE_PREFIX_AUTHOR = DUMP_DIR+"/author/author_";
	public static  String FILE_PREFIX_AUTHOR_REFERENCE = DUMP_DIR+"/author/author_reference_";
	public static  String FILE_PREFIX_CATEGORY = DUMP_DIR+"/category/category_";
	public static  String FILE_PREFIX_CATEGORY_REFERENCE = DUMP_DIR+"/category/category_reference_";
	public static  String FILE_PREFIX_ARTICLE = DUMP_DIR+"/article/article_";
	public static  String FILE_PREFIX_PUBMED_REFERENCE = DUMP_DIR+"/citations/pubmed_reference_";
	public static  String FILE_PREFIX_VOLUME = DUMP_DIR+"/volume/volume_";
	public static  String FILE_PREFIX_CONFERENCE = DUMP_DIR+"/conference/conference_";
	
	// Values Limit
	public static final int VALUES_LIMIT = 20000;
	// Maximum file size of each dump file
	public static final int MAX_DUMP_FILE_SIZE = 5000; // 5MB
	// Date format
	public static final String DUMP_FILE_DATE_FORMAT = "YYYY-M-dd-HH-mm-SSS";
	
	//Citation context length 
	public static final int CITATION_CONTEXT_LENGTH = 150;
	
	// Page Rank Threshold
	public static final double PAGE_RANK_THRESHOLD = 10.00;	
	
	// Formatting Strings
	public static final String DOUBLE_QUOTE = "\"";
	public static final String SINGLE_QUOTE = "'";

	//Element Names for Parsing in the Article Parser Class
	public static final String ELEMENT_CATEGORY_TAG = "article-categories";
	public static final String ELEMENT_SUBJ_GROUP_TAG = "subj-group";
	public static final String ELEMENT_SUBJECT_TAG = "subject";
	// publication ID types in article parser class
	public static final String PUBMED_LABEL = "pmid";  // pubmed ID
	public static final String PMC_LABEL = "pmc";      // pubmed central ID
	public static final String DOI_LABEL = "doi";      // Document object identifier (doi)
	public static final byte IDTYPE_NO_ID = 0;        // no ID type set or found
	public static final byte IDTYPE_PUBMED = 1;       // pubmed ID
	public static final byte IDTYPE_PMC = 2;          // pubmed central ID
	public static final byte IDTYPE_DOI = 3;          // Document object identifier (doi)
	// publication date constants in article parser class
	public static final String PUB_YEAR_TAG = "year";
	public static final String PUB_MONTH_TAG = "month";
	public static final String PUB_DAY_TAG = "day";
	public static final String PUB_TYPE_LABEL = "pub-type"; // label for the publication type attribute in a pub-date
	public static final String PUB_TYPE_COLLECTION = "collection";
	public static final String PUB_TYPE_EPUB = "epub";
	public static final String PUB_TYPE_ACCEPTED = "accepted";
	public static final String PUB_TYPE_PMC = "pmc-release";
	public static final String PUB_TYPE_PPUB = "ppub";
	// author constants in article parser class
	public static final String CONTRIBUTOR_GROUP = "contrib-group";
	public static final String AUTHOR_AFF_TAG = "aff"; //tag for the author affiliation elements in a contributor group
	public static final String AUTHOR_AFF_ID_LABEL = "id"; //name of the attribute in the aff element for the affiliation ID used in the author xref
	public static final String AUTHOR_TAG = "contrib"; //tag for each contributor - we need to check if they are an author
	public static final String CONTRIBUTOR_TYPE = "contrib-type";  //name of the attribute for the contributor type - we only want contributors that are authors
	public static final String AUTHOR_TYPE = "author"; //value for the author contributor type
	public static final String AUTHOR_LAST_NAME = "surname";
	public static final String AUTHOR_FIRST_NAME = "given-names";
	public static final String AUTHOR_EMAIL = "email"; //element name for the email address of an author
	public static final String AUTHOR_AFF_XREF = "xref"; //xref element
	public static final String REFERENCE_TYPE = "ref-type"; //type attribute for an xref
	public static final String AUTHOR_REF_TYPE = "rid"; //attribute value for an author reference type
	
	
}
