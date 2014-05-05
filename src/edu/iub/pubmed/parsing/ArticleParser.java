package edu.iub.pubmed.parsing;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import edu.iub.pubmed.dump.IDGenerator;
import edu.iub.pubmed.dump.PubmedDump;
import edu.iub.pubmed.exceptions.NoPubmedIdException;



public class ArticleParser {

	private static final Logger LOGGER = Logger.getLogger("PubmedMining");
	private static DocumentBuilder documentBuilder = null;
	private static XPath xPath = null;
	private String fileName = null;
	private Document document = null;
	private String pubmedId = null;
	private String confId = null;
	private String volId = null;
	Set<String> uniqueKeyWords = null;
	Map<String,Double> refValues = null;
	private PubmedDump dumpCreator = null;
	private IDGenerator idGenerator = null;

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
	public ArticleParser(String fileName, IDGenerator idGenerator) throws SAXException, IOException {
		this.fileName = fileName;
		this.document = documentBuilder.parse(fileName);
		this.document.getDocumentElement().normalize();
		this.idGenerator = idGenerator;
	}

	/**
	 * Parse the XML file and extracts all the required values 
	 * @throws Exception 
	 */
	public void parse() throws Exception {
		Element articleMeta = (Element) document.getElementsByTagName(
				"article-meta").item(0);
		
		extractConference(articleMeta);
		extractVolume(document);
		extractArticlemeta(articleMeta);		
		extractAuthor(articleMeta);
		extractCategories(articleMeta);
		extractKeyWords(articleMeta);
		extractPubmedRef(document);
	
		document = null;
	}


	
	
	
	/**
	 * Calculates how many times each citation is cited in the paper
	 * 
	 * @param document - document object
	 * 
	 * @return - map of citations and their frequencies
	 *
	 */
	public Map<String, Integer> findRefFrequency(Document document) {
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
	
	private void extractPubmedRef(Document document) {
		List<String> validCitations = new ArrayList<String>();
		validCitations.add("mixed-citation");
		validCitations.add("element-citation");
		validCitations.add("citation");
		Map<String, Integer> refFreq = null;
		Set<String> citeIDs = new HashSet<>();
		String pubMedCitation = null;
		String refId = null;
		Element refNode = null;
		refValues = new HashMap<>();
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
									double weight = totalWeight
											* refFreq.get(refId);
									refValues.put(pubMedCitation,weight);
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
	private void extractVolume(Document document) {	
		String journalId = null;
		String journalTitle = null;
		String publisherName = null;
		String volume = null;	
		String issue = null;	
		Element articleMeta = null;
		Element journalMeta = null;
		
		try {
			articleMeta = (Element) document.getElementsByTagName("article-meta");
			journalMeta = (Element) document.getElementsByTagName("journal-meta");
			Element journalIdElement = (Element) journalMeta.getElementsByTagName("journal-id").item(0);
			Element journalTitleElement  = (Element) journalMeta.getElementsByTagName("journal-title");
			Element publisherNameElement = (Element) journalMeta.getElementsByTagName("publisher-name");
			Element volumeElement = (Element) articleMeta.getElementsByTagName("volume");
			Element issueElement = (Element) articleMeta.getElementsByTagName("issue");			
			
			
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
			
			
			//dumpCreator.addToVolume(volumeId, volume.getTextContent(), issue.getTextContent(), null, issueTitle.getTextContent(), null, null, volumeId); 
			
		} catch (Exception ex) {
			LOGGER.warning("Exception while parsing Volume Information "	+ fileName);
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
	 * 
	 *
	 */
	private void extractConference(Element articleMeta) {
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
			   confId = idGenerator.generateConferenceId(confName , confNum);
			   dumpCreator.addToConference(confId,confDate,confName,confNum,confLoc,confSponsor,confTheme,confAcronym);
			  
			}
		}catch(Exception ex){
			LOGGER.warning("Exception while parsing conference element for article :: " + fileName);
		}
		
	}

	/**
	 * Returns the pubmeId of this article 
	 * @return - pubmedId
	 */
	public String getPubmedId(){
		return this.getPubmedId();
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
		return null;
	}
	
	
	/**
	 * Extract required details from Article meta
	 * @param articleMeta
	 * @throws Exception 
	 */
	private void extractArticlemeta(Element articleMeta) throws Exception {
		String pubmedId = null;
		String articleTitle = null;
		String abstractText = null;
		Date  pubDate = null;
		pubmedId = getPubmedId(articleMeta);
		articleTitle = getArticleTitle(articleMeta);
		abstractText = getAbstractText(articleMeta);
		pubDate = getPubDate(articleMeta);
		
	   dumpCreator.addToArticle(pubmedId, null, articleTitle, null, abstractText, confId, null, volId);
	}
	
	
	
	
	
	
	private Date getPubDate(Element articleMeta) {
		Element pubDateElement = null;
		
		
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Retrieves text form the Article Meta
	 * @param articleMeta
	 * @return
	 */
	
	private String getAbstractText(Element articleMeta) {
		Element abstractElem = (Element) articleMeta.getElementsByTagName("abstract").item(0);
		String abstractText = abstractElem.getTextContent();
		return abstractText;
	}

	/**
	 * Retrieves Article tile from the Article meta
	 * @param articleMeta
	 * @return
	 */
	private String getArticleTitle(Element articleMeta) {
		Element titleGroup = (Element) articleMeta.getElementsByTagName("title-group");
		Element articleTitle = (Element) titleGroup.getElementsByTagName("article-title");
		return articleTitle.getTextContent();
	}
	
	
	/**
	 * Retrieves PubmId from Article Meta
	 * @param articleMeta
	 * @return
	 * @throws Exception - if pubMed doesnt exist
	 */
	private  String getPubmedId(Element articleMeta) throws Exception{
		String pubmedId = null;
		NodeList articleIdNodes = null;
		Element articleIdElement = null;
		String articleIdType = null;
		try {
			articleIdNodes = articleMeta.getElementsByTagName("article-id");
			for (int index = 0; index < articleIdNodes.getLength(); index++) {
				articleIdElement = (Element) articleIdNodes.item(index);
				articleIdType = articleIdElement.getAttribute("pub-id-type");
				if (articleIdType != null && articleIdType.equals("pmid")) {
					pubmedId = articleIdElement.getTextContent();
					break;
				}
			}
		
			if(pubmedId == null){
				throw new NoPubmedIdException();
			}
		
		} catch(Exception ex){
			LOGGER.severe("Exception while parsing for Pubmed ID " + fileName);
			throw ex;
		}
		return pubmedId;
	}

	
	/**
	 * Extracts category information Recursively i.e for each category if there are child
	 * categories they are traversed further.
	 * @param document
	 * @throws Exception 
	 */
	private void extractCategories(Element articleMeta) {
		NodeList categoryNodes = null;
		Element categoryElem = null;
		String categoryId = null;
		try {
			categoryNodes = articleMeta.getElementsByTagName("subj-group");
			for (int index = 0; index < categoryNodes.getLength(); index++) {
			categoryElem = (Element) categoryNodes.item(index);
			categoryId = idGenerator.getCategoryId(categoryElem.getFirstChild()
						.getTextContent());
			dumpCreator.addToCategoryReference(pubmedId, categoryId);
			if (categoryElem.getChildNodes().getLength() > 1) {
				setSubCategories(categoryElem.getLastChild(), categoryId);
			}
			}
		}catch(Exception ex){
			
		}
		
	}
	/**
	 * Extract subcategories
	 * @param lastChild - Subcategory nodes
	 * @param categoryId - parent category Id
	 */
		private void setSubCategories(Node lastChild, String categoryId) {			
			String subCategoryId = null;
			subCategoryId = idGenerator.getCategoryId(lastChild.getFirstChild().getNodeValue());
			dumpCreator.addToCategory(subCategoryId, categoryId, lastChild.getFirstChild().getNodeValue(), null, null);
			if (lastChild.getChildNodes().getLength() > 1) {
				setSubCategories(lastChild.getLastChild(), subCategoryId);
			}
		}

	/**
	 * <ul>
	 * <li>Extracts author information form the XML file .</li>
	 * 
	 * <li>Author information is available at
	 * <article><article-meta><kwd-group><kwd></li>
	 * 
	 * <li>Extracted keyword is formatted and check for null and length < 200 .</li>
	 * <li>For each keyword, keyword Id is retrieved and added to keyword
	 * reference dump</li>
	 * </ul>
	 * 
	 */
	private void extractKeyWords(Element articleMeta) {
		NodeList kwdNodes = null;
		String keyWord = null;
		String keywordId = null;
		try {
			kwdNodes = articleMeta.getElementsByTagName("kwd");
			 uniqueKeyWords = new HashSet<>();
			for (int index = 0; index < kwdNodes.getLength(); index++) {
				Element kwdNode = (Element) kwdNodes.item(index);
				keyWord = kwdNode.getTextContent();
				keyWord = formatString(keyWord);
				if (keyWord != null && keyWord.length() < 200
						&& uniqueKeyWords.add(keyWord)) {
					keywordId = idGenerator.generateKeywordId(keyWord);
					dumpCreator.addToKeyWordDump(pubmedId, keywordId);

				}
			}

		} catch (Exception ex) {
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
	private void extractAuthor(Element articleMeta) {
		NodeList contributors = (NodeList) articleMeta
				.getElementsByTagName("contrib");
		Element contributor = null;
		String surName = null;
		String givenName = null;
		String authorId = null;
		for (int index = 0; index < contributors.getLength(); index++) {
			contributor = (Element) contributors.item(index);
			try {
				if (contributor.getAttribute("contrib-type").equals("author")) {
					surName = ((Element) contributor.getElementsByTagName(
							"surname").item(0)).getTextContent();
					givenName = ((Element) contributor.getElementsByTagName(
							"given-names").item(0)).getTextContent();
					if (surName != null && givenName != null) {
						authorId = idGenerator.getAuthorId(givenName, surName);
						dumpCreator.addToAuthorReferences(pubmedId, authorId);
					}
				}
			} catch (NullPointerException ex) {
				System.out.println("Some of the require fields are missing");
			}
		}

	}
	
	
	/**
	 * Formats the given keyword by trimming and removing line breaks
	 * @param actualWord - actual keyword
	 * @return String - formatted keyword
	 */
	public String formatString(String actualWord) {
		String formatKeyword = null;
		if (actualWord != null) {
			formatKeyword = actualWord.toLowerCase().trim();
			formatKeyword = formatKeyword.replaceAll("[\\n,\\.]", "");
		}
		return formatKeyword;
	}

}
