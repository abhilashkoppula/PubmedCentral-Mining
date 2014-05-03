package edu.iub.pubmed.dump;

import java.util.ArrayList;
import java.util.List;

import edu.iub.pubmed.file.DumpFiles;
import edu.iub.pubmed.utils.SQLQueries;
import static edu.iub.pubmed.utils.Constants.*;

/**
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
		articleValues = new ArrayList<>();
		authorReferencesValues = new ArrayList<>();
		keywordValues = new ArrayList<>();
		keywordReference = new ArrayList<>();
		conferenceValues = new ArrayList<>();
		categoryValues = new ArrayList<>();
		categoryReferenceValues = new ArrayList<>();
		pubmeReferenceValues = new ArrayList<>();
	}
	
	
	public void addToArticle(String pubmedId , String pubDate , String articleTitle , String subTitle , String abstractTex , String confId , String custoTex, String volId ){
		String values = String.format(SQLQueries.QUERY_VALUES_ARTICLE_TABLE,pubmedId,pubDate,articleTitle,subTitle,abstractTex,confId,custoTex,volId,null);
		addToList(values,articleValues);
	}
	
	public void addToAuthorReferences(String pubmedId , String authorId){
		String values = String.format(SQLQueries.QUERY_VALUES_AUTHOR_REFERENCE_TABLE, pubmedId, authorId);
		addToList(values,authorReferencesValues);
	}
	
	public void addToAuthor(String authorId , String givenName , String surName){
		String values = String.format(SQLQueries.QUERY_VALUES_AUTHOR_TABLE, authorId , givenName ,surName);
		addToList(values,authorValues);
	}
	
	public void addToList(String value , List<String> list){
		list.add(value);
	}
	
	
	public void addToKeyWordDump(String keyword , String keywordId){
		String values = String.format(SQLQueries.QUERY_VALUES_KEYWORD_TABLE,keyword,keywordId);
		addToList(values,keywordValues);
		
	}
	
	public void addToKeyWordReferenceDump(String pubmedId , String keywordId){
		String values = String.format(SQLQueries.QUERY_VALUES_KEYWORD_REFERENCE_TABLE,pubmedId,keywordId);
		addToList(values,keywordValues);
	}
	
	public void addToConference(String confId , String confDate , String confName , String confNum , String confLoc , String confSpon , String confTheme,String acronym){
		String values = String.format(SQLQueries.QUERY_VALUES_CONFERENCE_TABLE, confId,confDate,confName,confNum,confLoc,confSpon,confTheme,acronym,null);
		addToList(values,conferenceValues);
	}
	
	public void addToVolume(String volId , String vol, String issue , String issue_Id , String issue_title, String issue_spon , String issue_part , String issbn){
		String values = String.format(SQLQueries.QUERY_VALUES_VOLUME_TABLE, volId,vol,issue,issue_Id,issue_title,issue_spon,issue_part,issbn);
		addToList(values,volumeValues);
	}
	
	public void addToCategory(String categoryId, String parentCategory,String subj , String seriesTitle , String seriesText){
		String values = String.format(SQLQueries.QUERY_VALUES_CATEGORY_TABLE, categoryId,parentCategory,subj,seriesTitle,seriesText);
		addToList(values,categoryValues);
	}
	
	public void addToCategoryReference(String pubmedId , String categoryId){
		String values = String.format(SQLQueries.QUERY_VALUES_CATEGORY_REFERENCE_TABLE, pubmedId,categoryId);
		addToList(values,categoryReferenceValues);
	}
	
	public void addToPubmedReference(String pubmedId , String citedpubmedId , String leftText , String rightText){
		String values = String.format(SQLQueries.QUERY_VALUES_PUBMED_REFERENCE_TABLE,pubmedId,citedpubmedId,leftText,rightText);
		addToList(values,pubmeReferenceValues);
	}
	
	public boolean checkAndDump(){
		if(pubmeReferenceValues.size() > VALUES_LIMIT){
			return true;
		}
		return false;
	}
	
	
	public void createDump() throws Exception{
		String dumpFile = null;
		// Dumping authors table
		dumpFile = dumpFiles.getAuthorFile();
		dumpFiles.setAuthorFile(createDump(authorValues, dumpFiles.getAuthorFile(), FILE_PREFIX_AUTHOR, SQLQueries.QUERY_INSERT_AUTHOR_TABLE));
		dumpFiles.setAuthorFile(dumpFile);
	
		// Dumping author reference table
		dumpFile = dumpFiles.getAuthorReferenceFile();
		dumpFile = dumpFiles.createDump(authorReferencesValues, dumpFile, FILE_PREFIX_AUTHOR_REFERENCE, SQLQueries.QUERY_INSERT_AUTHOR_REFERENCE_TABLE);
		dumpFiles.setAuthorReferenceFile(dumpFile);
		
		// Dumping article table
		dumpFile = dumpFiles.getArticleFile();
		dumpFile = dumpFiles.createDump(articleValues, dumpFile, FILE_PREFIX_ARTICLE, SQLQueries.QUERY_INSERT_ARTICLE_TABLE);
		dumpFiles.setArticleFile(dumpFile);
		
		
		// Dumping volume table
		dumpFile = dumpFiles.getVolumeFile();
		dumpFile = dumpFiles.createDump(volumeValues, dumpFile, FILE_PREFIX_VOLUME, SQLQueries.QUERY_INSERT_VOLUME_TABLE);
		dumpFiles.setVolumeFile(dumpFile);
		
		
		// Dumping conference table
		dumpFile = dumpFiles.getConferenceFile();
		dumpFile = dumpFiles.createDump(conferenceValues, dumpFile, FILE_PREFIX_CONFERENCE, SQLQueries.QUERY_INSERT_CONFERENCE_TABLE);
		dumpFiles.setConferenceFile(dumpFile);
		
		
		// Dumping category table
		dumpFile = dumpFiles.getCategoryFile();
		dumpFile = dumpFiles.createDump(categoryValues, dumpFile, FILE_PREFIX_CATEGORY, SQLQueries.QUERY_INSERT_CATEGORY_TABLE);
		dumpFiles.setCategoryReferenceFile(dumpFile);
		
		
		// Dumping category reference table
		dumpFile = dumpFiles.getCategoryReferenceFile();
		dumpFile = dumpFiles.createDump(categoryReferenceValues, dumpFile, FILE_PREFIX_CATEGORY_REFERENCE, SQLQueries.QUERY_INSERT_CATEGORY_REFERENCE_TABLE);
		dumpFiles.setCategoryReferenceFile(dumpFile);
		
		// Dumping Keyword table	
		dumpFile = dumpFiles.getKeywordFile();
		dumpFile = dumpFiles.createDump(keywordValues, dumpFile, FILE_PREFIX_KEYWORD, SQLQueries.QUERY_INSERT_KEYWORD_TABLE);
		dumpFiles.setKeywordReferenceFile(dumpFile);
	

		// Dumping Keyword reference table
		dumpFile = dumpFiles.getKeywordReferenceFile();
		dumpFile = dumpFiles.createDump(keywordReference, dumpFile, FILE_PREFIX_KEYWORD_REFERENCE, SQLQueries.QUERY_INSERT_KEYWORD_REFERENCE_TABLE);
		dumpFiles.setKeywordReferenceFile(dumpFile);
		
		
		// Dumping pubmed reference table
		dumpFile = dumpFiles.getPumbedReferenceFile();
		dumpFile = dumpFiles.createDump(pubmeReferenceValues, dumpFile, FILE_PREFIX_PUBMED_REFERENCE, SQLQueries.QUERY_INSERT_PUBMED_REFERENCE_TABLE);
		dumpFiles.setPumbedReferenceFile(dumpFile);
		
		
	}
	
	public String createDump(List<String> values , String dumpFile , String filePrefix, String initialQuery) throws Exception{
		dumpFile = dumpFiles.createDump(values, dumpFile, filePrefix, initialQuery);
		values = new ArrayList<>();
		return dumpFile;
	}
	
	
	
	
	

}
