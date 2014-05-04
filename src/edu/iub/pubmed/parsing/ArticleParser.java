package edu.iub.pubmed.parsing;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import edu.iub.pubmed.dump.IDGenerator;
import edu.iub.pubmed.dump.PubmedDump;


public class ArticleParser {

	private static final Logger LOGGER = Logger.getLogger("PubmedMining");
	private static DocumentBuilder documentBuilder = null;
	private static XPath xPath = null;
	private String fileName = null;
	private Document document = null;
	private String pubmedId = null;
	private String confId = null;
	private String volId = null;
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
	 */
	public void parse() {
		Element articleMeta = (Element) document.getElementsByTagName(
				"article-meta").item(0);
		
		extractConference(articleMeta);
		extractVolume(articleMeta);
		extractArticlemeta(articleMeta);		
		extractAuthor(articleMeta);
		extractCategories(articleMeta);
		extractKeyWords(articleMeta);
		extractPubmedRef(document);
	
		document = null;
	}

	
	private void extractPubmedRef(Document document2) {
		// TODO Auto-generated method stub
		
	}

	private void extractVolume(Element articleMeta) {
		try {
			Node volume = (Node) xPath.evaluate(
					"/article/front/article-meta/volume",
					document.getDocumentElement(), XPathConstants.NODE);
			Node issue = (Node) xPath.evaluate(
					"/article/front/article-meta/issue",
					document.getDocumentElement(), XPathConstants.NODE);
			Node issueTitle = (Node) xPath.evaluate(
					"/article/front/article-meta/issue-title",
					document.getDocumentElement(), XPathConstants.NODE);

			String journalTitle = (String) xPath
					.evaluate(
							"/article/front/journal-meta/journal-title-group/journal-title/text()",
							document.getDocumentElement(),
							XPathConstants.STRING);
			if (journalTitle == null) {
				journalTitle = (String) xPath.evaluate(
						"/article/front/journal-meta/jounrnal-title/text()",
						document.getDocumentElement(), XPathConstants.STRING);
			}
			String volumeId = (String) xPath.evaluate(
					"/article/front/journal-meta/issn/text()",
					document.getDocumentElement(), XPathConstants.STRING);

			dumpCreator.addToVolume(volumeId, volume.getTextContent(), issue.getTextContent(), null, issueTitle.getTextContent(), null, null, volumeId); 
			
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing Volume Information "
					+ fileName);
			ex.printStackTrace();
			//throw ex;
		}
		
	}

	/**
	 * Extracts the Conference node from article-meta and traverses to its child Nodes
	 * to get conference details
	 * @param articleMeta
	 * @return
	 * @throws Exception 
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
						// TO Do  traverse further to find the Day,Month , Year
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
			ex.printStackTrace();
		}
		
	}

	public String getPubmedId(){
		return this.getPubmedId();
	}
	
	public Set<String> getKeywords(){
		return null;
	}
	
	public Map<String,Double> getCitations(){
		return null;
	}
	
	
	/**
	 * Extract required details from Article meta
	 * @param articleMeta
	 */
	private void extractArticlemeta(Element articleMeta) {
		String pubmedId = null;
		String articleTitle = null;
		String abstractText = null;

		pubmedId = getPubmedId(articleMeta);
		articleTitle = getArticleTitle(articleMeta);
		abstractText = getAbstractText(articleMeta);
		
	   dumpCreator.addToArticle(pubmedId, null, articleTitle, null, abstractText, confId, null, volId);
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
	 */
	private  String getPubmedId(Element articleMeta){
		String pubmedId = null;
		NodeList articleIdNodes = null;
		Element articleIdElement = null;
		String articleIdType = null;
		try {
		articleIdNodes = articleMeta.getElementsByTagName("article-id");
		for(int index = 0 ; index < articleIdNodes.getLength() ; index++){
			articleIdElement = (Element) articleIdNodes.item(index);
			articleIdType = articleIdElement.getAttribute("pub-id-type");
			if(articleIdType != null && articleIdType.equals("pmid")){
				pubmedId = articleIdElement.getTextContent();
				break;
			}
			
		}
		} catch(Exception ex){
			
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
		Set<String> uniqueKeyWords = null;
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
