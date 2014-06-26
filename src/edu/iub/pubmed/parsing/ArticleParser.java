package edu.iub.pubmed.parsing;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.dom4j.io.DOMReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.iub.pubmed.dump.CitationPair;
import edu.iub.pubmed.dump.IDGenerator;
import edu.iub.pubmed.dump.PubmedDump;
import edu.iub.pubmed.exceptions.NoPubmedIdException;
import edu.iub.pubmed.utils.Constants;
import edu.iub.pubmed.utils.UtilityMethods;

/**
 *  Pubmed XML Parser Class
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */

public class ArticleParser {

	private static final Logger LOGGER = Logger.getLogger("PubmedMining");
	private static DocumentBuilder documentBuilder = null;
	private static DOMReader domReader = null;
	private org.dom4j.Document dom4jDoc = null;
	private static XPath xPath = null;
	private String fileName = null;
	private Document document = null;
	private String pubmedId = null;              // ID used for the article.  Preferably a pmid, but others if no pmid is available
	private byte idType = Constants.IDTYPE_NO_ID;// Type for the ID based on types in Constants
	private java.sql.Date publicationDate = null;
	private String pubDateType = null; 
	private String confId = null;
	private String volId = null;
	Set<String> uniqueKeyWords = null;
	Map<String,Double> refValues = null;
	private PubmedDump dumpCreator = null;
	private IDGenerator idGenerator = null;
	private HashSet<String> articleCategories = null;   // used to track if an article contains the same category more than once

	/**
	 * Static initializer to create document factory 
	 */
	static {
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			dbFactory.setValidating(false);
			dbFactory.setNamespaceAware(true);
			dbFactory.setFeature("http://xml.org/sax/features/namespaces",
					false);
			dbFactory.setFeature("http://xml.org/sax/features/validation",
					false);
			dbFactory
					.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-dtd-grammar",
							false);
			dbFactory
					.setFeature(
							"http://apache.org/xml/features/nonvalidating/load-external-dtd",
							false);
			documentBuilder = dbFactory.newDocumentBuilder();
			domReader = new DOMReader();
			xPath = XPathFactory.newInstance().newXPath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * 
	 * @param fileName - XML file name
	 * @param idGenerator - 
	 * @throws SAXException throws SAXException if XPath evaluation fails
	 * @throws IOException throws IOException if file not found  
	 */
	public ArticleParser(String fileName, IDGenerator idGenerator , PubmedDump pubmedDump) throws SAXException, IOException {
		this.fileName = fileName;
		this.document = documentBuilder.parse(fileName);
		this.dom4jDoc = domReader.read(document);
		this.document.getDocumentElement().normalize();
		this.idGenerator = idGenerator;
		this.dumpCreator = pubmedDump;
	}

	/**
	 * Parse the XML file and extracts all the required values 
	 * @throws Exception 
	 */
	public void parse() throws Exception {
		NodeList rootElements = null;
		Element articleMeta = null;

		try {
			rootElements = document.getElementsByTagName("article-meta");
			if (rootElements.getLength() > 1) {
				LOGGER.severe("ArticleParser-parse: the file " + fileName +
						" contained " + rootElements.getLength() + 
						" article metadata elements, but we only processed the first one.");
			}
			articleMeta = (Element) rootElements.item(0);
			extractConference(articleMeta);
			extractVolume(document);
			extractArticlemeta(articleMeta);
			extractAuthors(articleMeta);
			articleCategories = new HashSet<String>();
			extractCategories(articleMeta);
			extractKeyWords(articleMeta);
			extractPubmedRef(document);

			document = null;
		} catch (Exception ex) {
			LOGGER.log(Level.SEVERE,"Exception while parsing {0} and discarding this file",new Object[]{fileName});
			ex.printStackTrace();
			throw ex;
		} finally {
			try {articleCategories.clear();} catch(Exception e){}
			articleCategories = null;
		}
	}
	/**
	 * Calculates how many times each citation is cited in the paper
	 * 
	 * @param document - document object
	 * 
	 * @return - map of citations and their frequencies
	 *
	 */
	private Map<String, Integer> findRefFrequency(Document document) {
		Map<String, Integer> refCount = null;
		NodeList xRefNodes = document.getElementsByTagName("xref");
		int noOfXRefNodes = xRefNodes.getLength();
		String refId = null;
		int refFreq = 0;
		refCount = new HashMap<String, Integer>(noOfXRefNodes);
		for (int index = 0; index < noOfXRefNodes; index++) {
			Element element = (Element) xRefNodes.item(index);
			refId = (String) element.getAttribute("rid");
			if (refCount.containsKey(refId)) {
				refFreq = refCount.get(refId);
				refFreq++;
			} else {
				refFreq = 1;
			}
			refCount.put(refId, refFreq);
		}
		return refCount;
	}
	
	
	
	
	/**
	 * For the given refId , the left and right text from where the citation is cited in the
	 * paragraphs of the body , are extracted and inserted into database.	 
	 * 
	 * @param citedPubmedId - citation Pubmed Id
	 * @param refId - refId of the citation
	 * 
	 */
	private void addCitationContext(String citedPubmedId , String refId) {
		List<org.dom4j.Node> citedParas = null;
		String citedXML = null;
		int citationIndex = -1;
		String leftText = null;
		String rightText = null;
		int beginIndex = 0;
		int endIndex = 0;
		String citationString = "<xref ref-type=\"bibr\" rid=\"?\">";
		String xPath = "/article/body//p[.//xref[@rid = '?']]";
		citationString = citationString.replace("?", refId);
		xPath = xPath.replace("?", refId);
		citedParas = dom4jDoc.selectNodes(xPath);
		for (org.dom4j.Node citedPara : citedParas) {
			citedXML = citedPara.asXML();
			citationIndex = -1;
			citationIndex = citedXML.indexOf(citationString);
			if (citationIndex != -1) {
				if (citationIndex <= Constants.CITATION_CONTEXT_LENGTH) {
					beginIndex = 0;
				} else {
					beginIndex = citationIndex - Constants.CITATION_CONTEXT_LENGTH;
				}
				leftText = citedXML.substring(beginIndex, citationIndex);
				if (citedXML.length() > (citationIndex
						+ citationString.length() + Constants.CITATION_CONTEXT_LENGTH)) {
					endIndex = citationIndex + citationString.length() + Constants.CITATION_CONTEXT_LENGTH;

				} else {
					endIndex = citedXML.length();
				}
				rightText = citedXML.substring(
						citationIndex + citationString.length(), endIndex);
				//TODO: When we get IDs for citations other than pubmed in the future, need to set the cited ID type
				CitationPair citation = idGenerator.getCitationId(pubmedId, idType, citedPubmedId, Constants.IDTYPE_PUBMED);
				long citationId = citation.getCitationId();
				short referenceId = citation.getReferenceId();
				if (referenceId == 1) //first citation for this pair
					dumpCreator.addToCitationValues(citationId, pubmedId, idType, citedPubmedId, Constants.IDTYPE_PUBMED);
				// Add the specific citation context (possibly many between each pair)
				dumpCreator.addToPubmedReference(citationId, citation.getReferenceId(), UtilityMethods.formatString(leftText), UtilityMethods.formatString(rightText));
			}
		}
	}
	
	
	/**
	 * 
	 * Extracts citations from the article. Citations are with ref tag . There are multiple
	 * type of citations and for this project only mixed-citations , element-citations
	 * and citations are extracted . Out of these citations only citation with pubmed id are inserted 
	 * into db and rest are discarded .
	 * <br>
	 * A citation is considered as pubmed citation if it has <pub-id> tag with pub-id-type attribute
	 * as pmid
	 * <br>
	 * For each citation along with pubmed id, frequency is also inserted into database. 
	 * 
	 * 
	 * @param document
	 */
	
	private void extractPubmedRef(Document document) throws Exception {
		List<String> validCitations = new ArrayList<String>();
		validCitations.add("mixed-citation");
		validCitations.add("element-citation");
		validCitations.add("citation");
		Map<String, Integer> refFreq = null;
		Set<String> citeIDs = new HashSet<String>();
		String pubMedCitation = null;
		String refId = null;
		Element refNode = null;
		refValues = new HashMap<String, Double>();
		double totalWeight = 1d / document.getElementsByTagName("xref")
				.getLength();
		try {
			refFreq = findRefFrequency(document);
			NodeList refNodes = document.getElementsByTagName("ref");
			for (int index = 0; index < refNodes.getLength(); index++) {
				refNode = (Element) refNodes.item(index);
				refId = refNode.getAttribute("id");
				NodeList pubMedNode = refNode.getElementsByTagName("pub-id");
				if (pubMedNode != null && pubMedNode.getLength() >= 1) {
					for (int pubIdIndex = 0; pubIdIndex < pubMedNode
							.getLength(); pubIdIndex++) {
						Element pubMedElement = (Element) pubMedNode
								.item(pubIdIndex);
						if (pubMedElement.getAttribute("pub-id-type").equals(
								"pmid")) {
							pubMedCitation = pubMedElement.getTextContent();
							if (citeIDs.add(pubMedCitation)
									&& pubMedCitation.length() < 10) {
								if (refFreq.get(refId) != null) {
									addCitationContext(pubMedCitation,refId);
									double weight = totalWeight
											* refFreq.get(refId);
									refValues.put(pubMedCitation, weight);
								}
							}
							break;
						}
					}

				}
			}
		} catch (Exception ex) {
			LOGGER.severe("Exception while Parsing Citations for file "
					+ fileName);
			ex.printStackTrace();
			throw ex;
		}
	}

	/**
	 * Extracts volume details from the XML . Volume details are available under both journal-meta
	 * and article-meta elements. From journal-meta ,journalId , journalTitle and publisherName are extracted
	 * and from article-meta element volume and issue are extracted . There are other elements like volume-series
	 *, issue-title , issue-id which contains details about the volume , but these elements are discarded as the frequency
	 * of their occurrence is very low . <br>
	 * 
	 * All the above mentioned elements combinedly define a unique volume.
	 *  
	 * @param document - root Element .
	 * 
	 * 
	 */
	private void extractVolume(Document document) throws Exception {	
		String journalId = null;
		String journalTitle = null;
		String publisherName = null;
		String volume = null;	
		String issue = null;	
		Element articleMeta = null;
		Element journalMeta = null;
		
		try {
			articleMeta = (Element) document.getElementsByTagName("article-meta").item(0);
			journalMeta = (Element) document.getElementsByTagName("journal-meta").item(0);
			Element journalIdElement = (Element) journalMeta.getElementsByTagName("journal-id").item(0);
			Element journalTitleElement  = (Element) journalMeta.getElementsByTagName("journal-title").item(0);
			Element publisherNameElement = (Element) journalMeta.getElementsByTagName("publisher-name").item(0);
			Element volumeElement = (Element) articleMeta.getElementsByTagName("volume").item(0);
			Element issueElement = (Element) articleMeta.getElementsByTagName("issue").item(0);			
			
			
			journalId = journalIdElement.getTextContent();
			
			if(journalTitleElement != null){
				journalTitle = journalTitleElement.getTextContent();
			}
			
			if(publisherNameElement != null){
				publisherName = publisherNameElement.getTextContent();
			}
			
			if(volumeElement != null){
				volume = volumeElement.getTextContent();
			}
			
			if(issueElement != null){
				issue = issueElement.getTextContent();
			}
			
			volId = idGenerator.getVolumeId(journalTitle, journalId, volume, issue, publisherName);
			
		} catch (Exception ex) {
			LOGGER.warning("Exception while parsing Volume Information for " + fileName);
			ex.printStackTrace();
		}
	}

	/**
	 * Extracts the Conference node from article-meta and traverses to its child Nodes
	 * to get conference details . Conference element is present at following locations 
	 * <article><article-meta><conference> . This conference element have all the required 
	 * child nodes to populate conference table . <br>
	 * 
	 * Conference name and number is identified as a unique conference . 
	 * 
	 * @param articleMeta - <article-meta> element
	 */
	private void extractConference(Element articleMeta) throws Exception {
		NodeList conferenceNode = null;
		Element conferenceElement = null;
		String confDate = null;
		String confName = null;
		String confLoc = null;
		String confSponsor = null;
		String confAcronym = null;
		String confTheme = null;
		String confNum = null;
		
		try {
			conferenceNode = articleMeta.getElementsByTagName("conference");
			if (conferenceNode != null && conferenceNode.getLength() == 1) {
				 conferenceElement = (Element) conferenceNode.item(0);
				for (int index = 0; index < conferenceElement.getChildNodes()
						.getLength(); index++) {
					Element childNode = (Element) conferenceElement
							.getChildNodes().item(index);
					String nodeName = childNode.getNodeName();
					String textContent = childNode.getTextContent();
					if (nodeName.equals("conf-date")) {
						confDate = textContent;
					} else if (nodeName.equals("conf-name")) {
						confName = textContent;
					} else if (nodeName.equals("conf-loc")) {
						confLoc = textContent;
					} else if (nodeName.equals("conf-sponsor")) {
						confSponsor = textContent;
					} else if (nodeName.equals("conf-acronym")) {
						confAcronym = textContent;
					} else if (nodeName.equals("conf-theme")) {
						confTheme = textContent;
					} else if (nodeName.equals("conf-num")) {
						confNum = textContent;
					}
				}
			   confId = idGenerator.generateConferenceId(confDate,confName,confNum,confLoc,confSponsor,confTheme,confAcronym);			  
			}
		}catch(Exception ex){
			LOGGER.warning("Exception while parsing conference element :: " + ex.getMessage());
			throw ex;
		}		
	}

	/**
	 * Returns the pubmeId of this article 
	 * @return - pubmedId
	 */
	public String getPubmedId(){
		return this.pubmedId;
	}
	
	
	/**
	 * Returns list of keywords used in this article
	 * @return - list of keywords
	 */
	public Set<String> getKeywords(){
		return uniqueKeyWords;
	}
	
	/**
	 * Returns citations and their edge weights
	 * 
	 * @return - hashmap of citations and their edge weights
	 * 
	 */
	public Map<String,Double> getCitations(){
		return refValues;
	}
	/**
	 * Extract required details from Article meta
	 * @param articleMeta
	 * @throws Exception 
	 */
	private void extractArticlemeta(Element articleMeta) throws Exception {
		String articleTitle = null;
		String abstractText = null;
		try {
		extractPubmedId(articleMeta);
		articleTitle = getArticleTitle(articleMeta);
		abstractText = getAbstractText(articleMeta);
		extractPubDate(articleMeta);
		   dumpCreator.addToArticleValues(pubmedId, idType, publicationDate.toString(), pubDateType, UtilityMethods.formatString(articleTitle), UtilityMethods.formatString(abstractText), confId,  volId);
		}catch(Exception ex){
			LOGGER.warning("Exception while parsing article-meta element for article details :: " + ex.getMessage());
			throw ex;
		}	
	}	
	
	/**
	 * extractPubDate<br/>
	 * Retrieves the pubDate from article-meta.  The publication date (pub-date) element
	 * is optional, so if the article metadata contains no pub-date, the publication
	 * date is set to null.
	 * 
	 * Each article can have multiple publication dates (e.g., acceptance, e-publication, 
	 * collection, etc.)  There is a hierarchy as to preference when setting the publication 
	 * date.  The type of date used is also stored in the database.
	 *  
	 * @param articleMeta  Element with the article metadata
	 */
	private void extractPubDate(Element articleMeta) {
		NodeList pubDates = null;
		Element pubDateElement = null;
		HashMap<String,Date> publicationDates = null;
		
		publicationDate = null;//default
		pubDateType = null;
		try {
			publicationDates = new HashMap<String,Date>();
			pubDates = articleMeta.getElementsByTagName("pub-date");
			for (int i = 0; i < pubDates.getLength(); i++) {
				pubDateElement = (Element) pubDates.item(i);
				processPubDate(publicationDates, pubDateElement);
			} //loop through the possible publication dates
			if (publicationDates.size() == 0)
				return; //we found no date that would work - leave the date and type as null
			// there are some publication dates we have a preference for, but if those do not
			// exist for this article, we will take the first date with a label
			if (publicationDates.containsKey(Constants.PUB_TYPE_PPUB)) {
				publicationDate = publicationDates.get(Constants.PUB_TYPE_PPUB);
				pubDateType = Constants.PUB_TYPE_PPUB;
			} else if (publicationDates.containsKey(Constants.PUB_TYPE_EPUB)) {
				publicationDate = publicationDates.get(Constants.PUB_TYPE_EPUB);
				pubDateType = Constants.PUB_TYPE_EPUB;
			} else if (publicationDates.containsKey(Constants.PUB_TYPE_COLLECTION)) {
				publicationDate = publicationDates.get(Constants.PUB_TYPE_COLLECTION);
				pubDateType = Constants.PUB_TYPE_COLLECTION;
			} else if (publicationDates.containsKey(Constants.PUB_TYPE_PMC)) {
				publicationDate = publicationDates.get(Constants.PUB_TYPE_PMC);
				pubDateType = Constants.PUB_TYPE_PMC;
			} else if (publicationDates.containsKey(Constants.PUB_TYPE_ACCEPTED)) {
				publicationDate = publicationDates.get(Constants.PUB_TYPE_ACCEPTED);
				pubDateType = Constants.PUB_TYPE_ACCEPTED;
			} else {
				Iterator<Entry<String,Date>> it = publicationDates.entrySet().iterator();
				Entry<String,Date> entry = it.next();
				publicationDate = entry.getValue();
				pubDateType = entry.getKey();
			}			
			return;
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing out the publication date for " + fileName);
			publicationDate = null;
			pubDateType = null;
		} finally {
			pubDates = null;
			pubDateElement = null;
			try{publicationDates.clear();}catch(Exception e){}
		}
	} //end of extractPubDate
		
	
	/**
	 * processPubDate<br/>
	 * This method parses out a single date form the publication dates.  This is a separate
	 * method so that any error in processing a single publication date can be caught without
	 * preventing the processing of other publication dates for an article.
	 * 
	 * @param publicationDates  HashMap of Strings and dates where the string is the type 
	 *                          of publication date and the date is the corresponding SQL Date.
	 *                          The processed date will be added to this hashmap.
	 * @param pubDateElement    The element to be processed.
	 */
	private void processPubDate(HashMap<String,Date> publicationDates, Element pubDateElement) {
		NodeList dateChildren = null;
		Node child = null;
		
		try {
			// The pub-date can contain a variety of different elements, 
			// including year, month, and day, but it can also be expressed
			// as a season or other text values.
			// if there is not at least a year, we will not process the date.
			int year = 0; int month = 0; int day = 1; //if only the year is set, default to the first day
			dateChildren = pubDateElement.getChildNodes();
			for (int j = 0; j < dateChildren.getLength(); j++) {
				child = dateChildren.item(j);
				if (child.getNodeType() != Node.ELEMENT_NODE)
					continue; //we only care about element nodes
				String tag = ((Element)child).getTagName();
				if (tag.equals(Constants.PUB_YEAR_TAG))
					year = Integer.parseInt( child.getTextContent().trim() );
				else if (tag.equals(Constants.PUB_MONTH_TAG))
					month = Integer.parseInt( child.getTextContent().trim() );
				else if (tag.equals(Constants.PUB_DAY_TAG))
					day = Integer.parseInt( child.getTextContent().trim() );
			} //get the relevant child elements out of a pub-date
			if (year == 0)
				return;;  //we cannot build a publication date without a year
			// get the date type
			Calendar pubCalendar = Calendar.getInstance();
			pubCalendar.set(year, month, day);
			publicationDates.put(pubDateElement.getAttribute(Constants.PUB_TYPE_LABEL), new Date(pubCalendar.getTimeInMillis()));
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing out a publication date for " + fileName + 
					" the date being processed was:\n" + pubDateElement.toString());
			publicationDate = null;
			pubDateType = null;
		} finally {
			dateChildren = null;
			child = null;
		}
	} //end of processPubDate

	/**
	 * Retrieves text form the Article Meta
	 * @param articleMeta
	 * @return
	 */
	
	private String getAbstractText(Element articleMeta) {
		String abstractStr = null;
		NodeList abstractElements = null;
		abstractElements = articleMeta.getElementsByTagName("abstract");
		if (abstractElements.getLength() > 0) {
			Element abstractElem = (Element) abstractElements.item(0);
			String abstractText = abstractElem.getTextContent();
			abstractStr =  UtilityMethods.formatString(abstractText);
		}
		abstractElements = null;
		return (abstractStr);
	}

	/**
	 * Retrieves Article tile from the Article meta.  Since the title
	 * group is optional in the schema, if there is no title for the article,
	 * this method will return null.
	 * 
	 * @param articleMeta   Element with the article metadata
	 * @return              String with the article title or null 
	 *                      if no title is specified in the article metadata.
	 */
	private String getArticleTitle(Element articleMeta) {
		String articleTitle = null; 
		Element titleGroup = null;
		NodeList titleGroups = articleMeta.getElementsByTagName("title-group");
		if (titleGroups.getLength() > 0) {
			titleGroup = (Element) titleGroups.item(0);
			// although title group is optional, each 
			// title group must contain a title.
			articleTitle = ((Element) titleGroup.getElementsByTagName("article-title").item(0)).getTextContent();
		}
		return (articleTitle);
	} //end of getArticleTitle
	
	
	/**
	 * extractPubmedId<br/>
	 * Retrieves an ID for the article. If it has a pubmed ID (pmid), 
	 * that is preferable, otherwise we will identify by one of the 
	 * other recognized ID types.  Based on the schema, an article is NOT
	 * required to have any IDs. If the article has no ID, or ones we do 
	 * not recognize, an Exception is thrown that lists the unregnized 
	 * IDs available (if any).
	 * @param articleMeta  Element containing an articleMeta element
	 * @return
	 * @throws Exception - if pubMed doesn't exist
	 */
	private  void extractPubmedId(Element articleMeta) throws Exception{
		String articleId = null;
		NodeList articleIdNodes = null;
		Element articleIdElement = null;
		HashMap<String,String> articleIds = null;
		String articleIdType = null;
		try {
			articleIds = new HashMap<String,String>();
			articleIdNodes = articleMeta.getElementsByTagName("article-id");
			for (int index = 0; index < articleIdNodes.getLength(); index++) {
				articleIdElement = (Element) articleIdNodes.item(index);
				articleIdType = articleIdElement.getAttribute("pub-id-type");
				if (articleIdType == null)
					continue;
				articleId = articleIdElement.getTextContent();
				articleIds.put(articleIdType, articleId);
				if (articleIdType.equals("pmid")) 
					break; //pmid is our top choice for an ID
			}
			// We need to decide which ID to use.  Preference is for a pmid
			pubmedId = null;
			idType = Constants.IDTYPE_NO_ID;
			if (articleIds.containsKey(Constants.PUBMED_LABEL)) { //second choice
				idType = Constants.IDTYPE_PUBMED;
				pubmedId = articleIds.get(Constants.PUBMED_LABEL);
			} else if (articleIds.containsKey(Constants.PMC_LABEL)) { //third choice
				idType = Constants.IDTYPE_PMC;
				pubmedId = articleIds.get(Constants.PMC_LABEL);
			} else if (articleIds.containsKey(Constants.DOI_LABEL)) { //fourth choice
				idType = Constants.IDTYPE_DOI;
				pubmedId = articleIds.get(Constants.DOI_LABEL);
			} 
			if (idType == Constants.IDTYPE_NO_ID) {
				// we could not find a suitable ID - log what we found
				StringBuilder msg = new StringBuilder("No suitable ID found in file: " + fileName);
				for (Entry<String,String> entry: articleIds.entrySet()) {
					msg.append("\nID type: " + entry.getKey() + ", value: " + entry.getValue());
				}
				throw new NoPubmedIdException(msg.toString());
			}
			return;	
		} catch(Exception ex){
			LOGGER.severe("Exception while parsing for Pubmed ID " + fileName);
			throw ex;
		} finally {
			articleIdNodes = null;
			articleIdElement = null;
			try{articleIds.clear();}catch(Exception e){}
		}
	} //end of extractPubmedId

	
	/**
	 * extractCategories<br/>
	 * Processes each of the subj-group categories within the 
	 * article-categories metadata.
	 * 
	 * @param articleMeta Element with the article metadata
	 * 
	 */
	private void extractCategories(Element articleMeta) {
		NodeList categoryNodes = null;
		NodeList subjGrpNodes = null;
		try {
			// There is at most one article-categories element in the metadata
			categoryNodes = articleMeta.getElementsByTagName(Constants.ELEMENT_CATEGORY_TAG);
			if (categoryNodes.getLength() == 0)
				return; //the optional article-categories node was not included
			// Get the child subj-group nodes and process them
			subjGrpNodes = categoryNodes.item(0).getChildNodes();
			for (int i = 0; i < subjGrpNodes.getLength(); i++) {
				Node node = subjGrpNodes.item(i);
				if (node.getNodeType() != Node.ELEMENT_NODE)
					continue;
				String tag = ((Element)node).getTagName();
				if (tag == null || tag.compareTo(Constants.ELEMENT_SUBJ_GROUP_TAG) != 0)
					continue; //not a subj-group element, so we don't want it
				processSubjGroup(node, null);
			} //loop through the child nodes of article-categories
			return;
		} catch(Exception ex){
			LOGGER.severe("Exception while parsing a Category element :: " + ex.getMessage() + 
					" Category listings for article: " + pubmedId + " may not be complete.");
			// No further exception is thrown - articles should not be excluded due
			// to an exception in parsing the descriptive categories. 
			//TODO: It would be good to keep a log of the total exceptions by type
			//      of error so we could dump it to the log at the end.
		} finally {
			categoryNodes = null;
			subjGrpNodes = null;
		}
	} //end of extractCategories
	
	/**
	 * processSubjGroup<br/>
	 * Based on the NIH website: http://dtd.nlm.nih.gov/archiving/tag-library/3.0/n-qjs2.html <br/>
	 * Each subj-group consists of one or more subject elements and optionally, one
	 * or more child subj-group elements.  This method is used to recursively process 
	 * subj-group elements.  Each category is based on its subject and the parent
	 * subject group's ID (a string).  For the top level categories, the parent ID is null.
	 * 
	 * @param subjGrpNode  Node that contains a subj-group Element
	 * @param parentId     String with the parent category's ID, or null if this 
	 *                     is a top-level category.
	 */
	private void processSubjGroup(Node subjGrpNode, String parentId) {
		ArrayList<Node> subjects = null;
		ArrayList<Node> subCategories = null;
		String mySubjectId = null; //set to the first subject when processing child nodes
		try {
			subjects = new ArrayList<Node>();
			subCategories = new ArrayList<Node>();
			NodeList children = subjGrpNode.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.getNodeType() != Node.ELEMENT_NODE)
					continue; //we only want elements
				String tag = ((Element) child).getTagName();
				// Check if we have a subject or subject group
				if (tag.equals(Constants.ELEMENT_SUBJECT_TAG))
					subjects.add(child);
				else if(tag.equals(Constants.ELEMENT_SUBJ_GROUP_TAG))
					subCategories.add(child);
			}
			// Each subject group is REQUIRED to have at least one subject
			// Process subjects first so we have a category ID for sub-categories
			while(!subjects.isEmpty() ) {
				Element subjElement = (Element) subjects.remove(0);
				String subject = subjElement.getTextContent().trim();
				if (subject.length() == 0)
					continue;  //we don't want empty subjects
				if (!articleCategories.add(subject) ) {
					LOGGER.severe("ArticleParser-processSubjGroup: The file " + fileName + 
							" contained the subject " + subject + 
							" as a category within the same subject group more than once");
					continue;
				}
				String categoryId = idGenerator.getCategoryId(subject, parentId);
				dumpCreator.addToCategoryReferenceValues(pubmedId, idType, categoryId);
				if (mySubjectId == null)
					mySubjectId = categoryId; //set to the first subject's ID
			}
			// Process the subcategories
			while(!subCategories.isEmpty() ) {
				processSubjGroup(subCategories.remove(0), mySubjectId);
			}
		} catch(Exception ex){
			LOGGER.severe("Exception while parsing a Category subject group element : " + ex.getMessage() + 
					" Category listings for article: " + pubmedId + 
					" in file " + fileName + " may not be complete.");
			// No further exception is thrown - articles should not be excluded due
			// to an exception in parsing the descriptive categories. 
			//TODO: It would be good to keep a log of the total exceptions by type
			//      of error so we could dump it to the log at the end.
		} finally {
			try{subjects.clear();}catch(Exception e){}
			try{subCategories.clear();}catch(Exception e){}
		}
	} //end of processSubjGroup
	
	
//	/**
//	 * Extracts category information Recursively i.e for each category if there are child
//	 * categories they are traversed further.
//	 * @param document
//	 * @throws Exception 
//	 */
//	private void extractCategories(Element articleMeta) throws Exception {
//		NodeList categoryNodes = null;
//		Element categoryElem = null;
//		String categoryId = null;
//		try {
//			categoryNodes = articleMeta.getElementsByTagName("subj-group");
//			for (int index = 0; index < categoryNodes.getLength(); index++) {
//			categoryElem = (Element) categoryNodes.item(index);
//			categoryId = idGenerator.getCategoryId(categoryElem.getFirstChild()
//						.getTextContent() , null);
//			dumpCreator.addToCategoryReferenceValues(pubmedId, categoryId);
//			if (categoryElem.getChildNodes().getLength() > 1) {
//				setSubCategories(categoryElem.getLastChild(), categoryId);
//				}
//			}
//		}catch(Exception ex){
//			LOGGER.warning("Exception while parsing Cateogry element :: " + ex.getMessage());
//			throw ex;
//		}
//	}
	
//	/**
//	 * Extract subcategories
//	 * @param lastChild - Subcategory nodes
//	 * @param categoryId - parent category Id
//	 */
//		private void setSubCategories(Node lastChild, String categoryId) {			
//			String subCategoryId = null;
//			subCategoryId = idGenerator.getCategoryId(lastChild.getFirstChild().getNodeValue(),categoryId);
//			dumpCreator.addToCategoryReferenceValues(pubmedId, subCategoryId);
//			if (lastChild.getChildNodes().getLength() > 1) {
//				setSubCategories(lastChild.getLastChild(), subCategoryId);
//			}
//		}

	/**
	 * <ul>
	 * <li>Extracts keyword information form the XML file .</li>
	 * 
	 * <li>Keyword information is available at
	 * <article><article-meta><kwd-group><kwd></li>
	 * 
	 * <li>Extracted keyword is formatted and check for null and length < 200.</li>
	 * <li>For each keyword, keyword Id is retrieved and added to keyword
	 * reference dump</li>
	 * </ul>
	 * 
	 */
	private void extractKeyWords(Element articleMeta) throws Exception {
		NodeList kwdNodes = null;
		String keyWord = null;
		String keywordId = null;
		try {
			kwdNodes = articleMeta.getElementsByTagName("kwd");
			 uniqueKeyWords = new HashSet<String>();
			for (int index = 0; index < kwdNodes.getLength(); index++) {
				Element kwdNode = (Element) kwdNodes.item(index);
				keyWord = kwdNode.getTextContent();
				keyWord = UtilityMethods.formatString(keyWord);
				if (keyWord != null && keyWord.length() < 200
						&& uniqueKeyWords.add(keyWord)) {
					keywordId = idGenerator.generateKeywordId(keyWord);
					dumpCreator.addToKeyWordReferenceValues(pubmedId, idType, keywordId);
				}
			}
		} catch (Exception ex) {
			LOGGER.warning("Exception while parsing Keyword element :: " + ex.getMessage());
			throw ex;
		}
	}

	/**
	 * Extracts author information form the XML file . Author information is
	 * available at <article><article-meta><contrib-group><contrib
	 * contrib-type="author"><name> .
	 * 
	 * For each author(GivenName + SurName) , author Id is retrieved and added
	 * to author reference dump
	 */
	private void extractAuthors(Element articleMeta) throws Exception {
		NodeList contributors = null;
		try {
			contributors = (NodeList) articleMeta.getElementsByTagName(Constants.CONTRIBUTOR_GROUP);
			if (contributors.getLength() == 0)
				return; //we have no authors
			// process each contribution group (based on the
			// schema there can be multiple contribution groups).
			for (int i = 0; i < contributors.getLength();i++) {
				processContributorGroup( (Element) contributors.item(i));
			}
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing author information for file: " + 
					fileName + ". Exception: " + ex.getMessage());
			// No further exception is thrown - if we lack complete author information, 
			// we still want the paper.
		} finally {
			contributors = null;
			
		}
	} //end of extractAuthors
		
		
	private void processContributorGroup(Element contributorGroup) {
		HashMap<String,String> affiliations = null;
		NodeList contributors = null;
		NodeList names = null; //used to get name components for an author
		Element contributor = null;
		Element xref = null;
		try {
			affiliations = getAffiliations(contributorGroup.getElementsByTagName(Constants.AUTHOR_AFF_TAG));
			boolean useAffiliations = affiliations != null;
			// Get all of the contributors that are authors
			// and add them to the output if we at least have a last name.
			// Each contributor group MUST have at least one contributor.
			contributors = contributorGroup.getElementsByTagName(Constants.AUTHOR_TAG);
			for (int i = 0; i < contributors.getLength(); i++) {
				contributor = (Element) contributors.item(i);
				// Is this an author?
				if (!contributor.getAttribute(Constants.CONTRIBUTOR_TYPE).equals(Constants.AUTHOR_TYPE) )
					continue;
				names = contributor.getElementsByTagName(Constants.AUTHOR_LAST_NAME);
				if (names.getLength() == 0)
					continue;  //without a last name, there is not a name based on the schema
				String lastName = names.item(0).getTextContent();
				names = contributor.getElementsByTagName(Constants.AUTHOR_FIRST_NAME);
				String firstName = (names.getLength() > 0)? names.item(0).getTextContent() : null;
				names = contributor.getElementsByTagName(Constants.AUTHOR_EMAIL);
				String email = (names.getLength() > 0)? names.item(0).getTextContent() : null;
				String affiliation = null; //default
				if (useAffiliations) {
					names = contributor.getElementsByTagName(Constants.AUTHOR_AFF_TAG);
					if (names.getLength() > 0)
						affiliation = affiliations.get(names.item(0).getTextContent());
					else { //check for an xref
						names = contributor.getElementsByTagName(Constants.AUTHOR_AFF_XREF); //get all of the xref elements
						for (int j = 0; j < names.getLength(); j++) {
							xref = (Element) names.item(j);
							if (xref.hasAttribute(Constants.REFERENCE_TYPE) ) {
								if (xref.getAttribute(Constants.REFERENCE_TYPE).equals(Constants.AUTHOR_AFF_TAG)) {
									affiliation = affiliations.get(xref.getAttribute(Constants.AUTHOR_REF_TYPE) );
									break; //stop looping through xref elements - we found the one we want
								}
							}
						} // end of loop through the xref elements
					} //done checking for an xref
				} //done checking for an affilaition
				// Get an ID for the author
				String authorId = idGenerator.getAuthorId(firstName, lastName, email, affiliation);
				dumpCreator.addToAuthorReferenceValues(pubmedId, idType, authorId);
			} //end of loop through the contributors
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing an author contributor group for the file: " + 
					fileName + ". Exception: " + ex.getMessage());
			// No further exception is thrown - if we lack complete author information, 
			// we still want the paper and any other contributor groups
		} finally {
			try {affiliations.clear();}catch(Exception e){}
			contributors = null;
			contributor = null;
			names = null;
			xref = null;
		}
	} //end of processContributorGroup

	
	/**
	 * getAffiliations<br/>
	 * For each author we want to get their affiliation if it's provided.
	 * This method is passed a Nodelist of all of the aff Elements within
	 * a contribution group.  The aff elements may be referenced by one 
	 * or more authors in the contribution group.
	 * 
	 * @return   HashMap of affiliation IDs and affiliations.  Each affiliation ID
	 *           would be used in an xref by one or more authors in the contribution
	 *           group.
	 */
	private HashMap<String,String> getAffiliations(NodeList authorAffiliations) {
		HashMap<String,String> affilaitions = null;
		try {
			if (authorAffiliations.getLength() == 0)
				return (null); //there are no affiliation elements
			affilaitions = new HashMap<String,String>();
			for (int i = 0; i < authorAffiliations.getLength(); i++) {
				Element affiliation = (Element) authorAffiliations.item(i);
				String location = affiliation.getTextContent();
				// The affiliation is likely to start with (one or more) digit(s) (1-9)
				// since these are used to affiliate authors with an institution. We want to
				// strip these off (and any subsequent blank space
				Matcher parsed = Constants.AFFILIATION_PATTERN.matcher(location);
				if (parsed.matches())
					location = parsed.group(2);
				String affId = affiliation.getAttribute(Constants.AUTHOR_AFF_ID_LABEL);
				if (affId != null && affId.length() != 0) //it could be setup as an ID for an Xref
					affilaitions.put(affId, location.trim());
				else { //instead of using an ID, it could be a value within the aff element
					affId = affiliation.getTextContent();
					if (affId != null && affId.length() != 0)
						affilaitions.put(affId, location.trim());
				}
			}
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing author affiliations for acontributor group for the file: " + 
					fileName + ". Exception: " + ex.getMessage());
			// No further exception is thrown - if we lack complete author information, 
			// we still want the paper and the author names
			try {affilaitions.clear();} catch(Exception e){}
			affilaitions = null;
		}
		return (affilaitions);
	} //end of getAffiliations
	
	
}
