package edu.iub.pubmed.dump;

import java.util.ArrayList;
import java.util.List;

import edu.iub.pubmed.utils.SQLQueries;
import static edu.iub.pubmed.utils.Constants.*;

/**
 * 
 * Holds the values of all the tables to create dump file.
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public class PubmedDump {
	
	List<String> articleValues = null;
	List<String> authorValues = null;
	List<String> authorReferencesValues = null;
	List<String> keywordValues = null;
	List<String> keywordReference = null;
	List<String> volumeValues = null;
	List<String> conferenceValues = null;
	List<String> categoryValues = null;
	List<String> categoryReferenceValues = null;
	List<String> pubmeReferenceValues = null;
	DumpFiles dumpFiles = null;
	
	
	public PubmedDump(String dumpDirectory){
		dumpFiles = new DumpFiles(dumpDirectory);
		articleValues = new ArrayList<>();
		authorValues = new ArrayList<>();
		volumeValues = new ArrayList<>();
		authorReferencesValues = new ArrayList<>();
		keywordValues = new ArrayList<>();
		keywordReference = new ArrayList<>();
		conferenceValues = new ArrayList<>();
		categoryValues = new ArrayList<>();
		categoryReferenceValues = new ArrayList<>();
		pubmeReferenceValues = new ArrayList<>();
	}
	
	
	/**
	 *  Adds the given values to 'article' table insert values.
	 *  
	 * @param pubmedId - pubmed Id of Article
	 * @param pubDate - published date
	 * @param articleTitle - title of the article
	 * @param abstractTex - abstract Text
	 * @param confId - conference Id
	 * @param volId - volume Id
	 */
	public void addToArticleValues(String pubmedId , String pubDate , String articleTitle , String abstractTex , String confId , String volId ){
		String values = String.format(SQLQueries.QUERY_VALUES_ARTICLE_TABLE,pubmedId,pubDate,articleTitle,abstractTex,confId,volId);
		addToList(values,articleValues);
	}
	
	/**
	 * Adds the given values to 'author_reference' table insert values.
	 * @param pubmedId - pubmedId of article
	 * @param authorId - authorId from article table
	 */
	public void addToAuthorReferenceValues(String pubmedId , String authorId){
		String values = String.format(SQLQueries.QUERY_VALUES_AUTHOR_REFERENCE_TABLE, pubmedId, authorId);
		addToList(values,authorReferencesValues);
	}
	/**
	 * Adds the given author values to 'author' table insert values.
	 * 
	 * @param authorId - Generated author Id
	 * @param givenName - author given name
	 * @param surName - author surName
	 */
	public void addToAuthorValues(String authorId , String givenName , String surName){
		String values = String.format(SQLQueries.QUERY_VALUES_AUTHOR_TABLE, authorId , givenName ,surName);
		addToList(values,authorValues);
	}
	
	/**
	 * Adds the given value to given list.
	 * 
	 * @param value - value string
	 * @param list - list holding values of table
	 */
	public void addToList(String value , List<String> list){
		list.add(value);
	}
	
	/**
	 * Adds the given values 'keyword' table insert values.
	 * 
	 * @param keyword - keyword text
	 * @param keywordId - generated id of keyword
	 */
	public void addToKeyWordValues(String keywordId , String keyword ){
		String values = String.format(SQLQueries.QUERY_VALUES_KEYWORD_TABLE,keywordId,keyword);
		addToList(values,keywordValues);
		
	}
	
	/**
	 * Adds the given values to 'keyword_reference' table.
	 * 
	 * @param pubmedId - pubmedId of the article 
	 * @param keywordId - keyword Id from keyword table
	 */
	public void addToKeyWordReferenceValues(String pubmedId , String keywordId){
		String values = String.format(SQLQueries.QUERY_VALUES_KEYWORD_REFERENCE_TABLE,pubmedId,keywordId);
		addToList(values,keywordReference);
	}
	
	/**
	 * Adds the given values to 'conference' table.
	 * 
	 * @param confId - generated Id for the conference
	 * @param confDate - date of Conference
	 * @param confName - name of the conference
	 * @param confNum - conference number
	 * @param confLoc - conference location
	 * @param confSpon - conference sponsor
	 * @param confTheme - conference theme
	 * @param acronym - acronym of the conference
	 */
	public void addToConferenceValues(String confId , String confDate , String confName , String confNum , String confLoc , String confSpon , String confTheme,String acronym){
		String values = String.format(SQLQueries.QUERY_VALUES_CONFERENCE_TABLE, confId,confDate,confName,confNum,confLoc,confSpon,confTheme,acronym,null);
		addToList(values,conferenceValues);
	}
	
	/**
	 * Adds the given values to 'volume' table insert values.
	 * 
	 * @param volId - generated Volume Id
	 * @param vol - volume of the article
	 * @param issue - issue of the article
	 * @param journalId - journal id from journal-meta
	 * @param journalTitle - journal title from journal-meta
	 * @param publisherName - publisher name from journal-meta
	 */
	public void addToVolumeValues(String volId , String vol, String issue , String journalId , String journalTitle, String publisherName){
		String values = String.format(SQLQueries.QUERY_VALUES_VOLUME_TABLE, volId,vol,issue,journalId,journalTitle,publisherName);
		addToList(values,volumeValues);
	}
	
	/**
	 * Adds the given values to 'category' table insert values.
	 * 
	 * @param categoryId - generated category Id
	 * @param parentCategory - parent category
	 * @param subj - subj of the category
	 * @param seriesTitle
	 * @param seriesText
	 */
	public void addToCategoryValues(String categoryId, String parentCategory,String subj ){
		String values = String.format(SQLQueries.QUERY_VALUES_CATEGORY_TABLE, categoryId,parentCategory,subj);
		addToList(values,categoryValues);
	}
	
	/**
	 * Adds the given values to 'category_reference' table insert values.
	 * 
	 * @param pubmedId - pubmedId of the article
	 * @param categoryId - id of the category used from category table
	 */
	public void addToCategoryReferenceValues(String pubmedId , String categoryId){
		String values = String.format(SQLQueries.QUERY_VALUES_CATEGORY_REFERENCE_TABLE, pubmedId,categoryId);
		addToList(values,categoryReferenceValues);
	}
	
	/**
	 * Adds the given values to 'pubmed_reference' table insert values.
	 * 
	 * @param pubmedId - pubmed Id of the article
	 * @param citedpubmedId - pubmed Id of the citation
	 * @param leftText - left 150 characters from where this article is cited
	 * @param rightText - right 150 characters from where this article is cited
	 */
	public void addToPubmedReference(String pubmedId , String citedpubmedId , String leftText , String rightText){
		String values = String.format(SQLQueries.QUERY_VALUES_PUBMED_REFERENCE_TABLE,pubmedId,citedpubmedId,leftText,rightText);
		addToList(values,pubmeReferenceValues);
	}
	
	/**
	 * 
	 * Checks if values exceeded max limit . As frequency of inserting into pubmed table
	 * is more than other , chances overflow is more for this table.
	 * 
	 * @return true if values exceeds values limit
	 */
	public boolean checkAndDump(){
		if(pubmeReferenceValues.size() > VALUES_LIMIT){
			return true;
		}
		return false;
	}
	
	/**
	 * Creates dump files for all the tables.
	 * 
	 * @throws Exception
	 */
	public void createDump() throws Exception{
		String dumpFile = null;
		// Dumping authors table
		dumpFile = dumpFiles.getAuthorFile();
		dumpFile = createDump(authorValues, dumpFiles.getAuthorFile(), FILE_PREFIX_AUTHOR, SQLQueries.QUERY_INSERT_AUTHOR_TABLE);
		dumpFiles.setAuthorFile(dumpFile);
	
		// Dumping author reference table
		dumpFile = dumpFiles.getAuthorReferenceFile();
		dumpFile = createDump(authorReferencesValues, dumpFile, FILE_PREFIX_AUTHOR_REFERENCE, SQLQueries.QUERY_INSERT_AUTHOR_REFERENCE_TABLE);
		dumpFiles.setAuthorReferenceFile(dumpFile);
		
		// Dumping article table
		dumpFile = dumpFiles.getArticleFile();
		dumpFile = createDump(articleValues, dumpFile, FILE_PREFIX_ARTICLE, SQLQueries.QUERY_INSERT_ARTICLE_TABLE);
		dumpFiles.setArticleFile(dumpFile);
		
		
		// Dumping volume table
		dumpFile = dumpFiles.getVolumeFile();
		dumpFile = createDump(volumeValues, dumpFile, FILE_PREFIX_VOLUME, SQLQueries.QUERY_INSERT_VOLUME_TABLE);
		dumpFiles.setVolumeFile(dumpFile);
		
		
		// Dumping conference table
		dumpFile = dumpFiles.getConferenceFile();
		dumpFile = createDump(conferenceValues, dumpFile, FILE_PREFIX_CONFERENCE, SQLQueries.QUERY_INSERT_CONFERENCE_TABLE);
		dumpFiles.setConferenceFile(dumpFile);
		
		
		// Dumping category table
		dumpFile = dumpFiles.getCategoryFile();
		dumpFile = createDump(categoryValues, dumpFile, FILE_PREFIX_CATEGORY, SQLQueries.QUERY_INSERT_CATEGORY_TABLE);
		dumpFiles.setCategoryFile(dumpFile);
		
		
		// Dumping category reference table
		dumpFile = dumpFiles.getCategoryReferenceFile();
		dumpFile = createDump(categoryReferenceValues, dumpFile, FILE_PREFIX_CATEGORY_REFERENCE, SQLQueries.QUERY_INSERT_CATEGORY_REFERENCE_TABLE);
		dumpFiles.setCategoryReferenceFile(dumpFile);
		
		// Dumping Keyword table	
		dumpFile = dumpFiles.getKeywordFile();
		dumpFile = createDump(keywordValues, dumpFile, FILE_PREFIX_KEYWORD, SQLQueries.QUERY_INSERT_KEYWORD_TABLE);
		dumpFiles.setKeywordFile(dumpFile);
	

		// Dumping Keyword reference table
		dumpFile = dumpFiles.getKeywordReferenceFile();
		dumpFile = createDump(keywordReference, dumpFile, FILE_PREFIX_KEYWORD_REFERENCE, SQLQueries.QUERY_INSERT_KEYWORD_REFERENCE_TABLE);
		dumpFiles.setKeywordReferenceFile(dumpFile);
		
		
		// Dumping pubmed reference table
		dumpFile = dumpFiles.getPumbedReferenceFile();
		dumpFile = createDump(pubmeReferenceValues, dumpFile, FILE_PREFIX_PUBMED_REFERENCE, SQLQueries.QUERY_INSERT_PUBMED_REFERENCE_TABLE);
		dumpFiles.setPumbedReferenceFile(dumpFile);
		
	}
	
	/**
	 * Dumps any remaining values in the lists . This method is called after processing all the
	 * 
	 * articles.
	 * @throws Exception 
	 * 
	 */
	
	 public void dumpRemaining() throws Exception{
		 createDump();
		 dumpFiles.closeAllDumpFiles();
	 }
	
	/**
	 * Creates dump file for the given values in given dump file
	 * 
	 * @param values - list of values
	 * @param dumpFile - dump file name for this table
	 * @param filePrefix - file prefix for this table
	 * @param initialQuery - initial query to be added
	 * @return fileName - name of the dump file returned from the creatDump method
	 * @throws Exception
	 */
	public String createDump(List<String> values , String dumpFile , String filePrefix, String initialQuery) throws Exception{
		if(values != null && values.size() > 0){
		dumpFile = dumpFiles.createDump(values, dumpFile, filePrefix, initialQuery);
		values = new ArrayList<>();
		}
		return dumpFile;
	}
	
	
	
	
	

}
