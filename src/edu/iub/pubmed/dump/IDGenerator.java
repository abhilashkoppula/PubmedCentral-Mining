package edu.iub.pubmed.dump;

import java.util.HashMap;
import java.util.Map;


/**
 * ID Generator for actual tables . 
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public class IDGenerator {
		
	public  long keywodIDSequence = 999;
	public  long categoryIDSequence = 9999;
	public  long authorIDSequence = 9;
	public  long confSequence = 999999;
	public  long volSequence = 99999999;	
	public  Map<String, Long> keyword_ids = null;
	public  Map<String, Long> category_ids = null;
	public  Map<String, Long> author_ids = null;
	public  Map<String, Long> conf_ids = null;
	public  Map<String, Long> vol_ids = null;
	PubmedDump pubmedDump = null;

	
	
	public IDGenerator(PubmedDump pubmedDump) {
		this.pubmedDump = pubmedDump;
		 keyword_ids = new HashMap<String, Long>();
		 category_ids = new HashMap<String, Long>();
		 author_ids = new HashMap<String, Long>();
		 conf_ids = new HashMap<String, Long>();
		 vol_ids = new HashMap<String, Long>();
	}
	
	
	/**
	 * 
	 * Generates Id for the given author . If doesn't exist in the author_ids ,
	 * new Id is generated and the given values along with Id is inserted into 'author' table
	 * 
	 * @param givenName - givenName of the author
	 * @param surName - surName of the author
	 * @return - generated Id
	 * 
	 */

	public  String getAuthorId(String firstName, String lastName, String email, String affiliation) {
		StringBuilder author = new StringBuilder(lastName);
		if (firstName != null) 
			author.append("-" + firstName);
		if (email != null) 
			author.append("-" + email);
		if (affiliation != null) 
			author.append("-" + affiliation);
		Long authorId = null;
		authorId = author_ids.get(author.toString());
		if (authorId == null) {
			authorId = ++authorIDSequence;
			author_ids.put(author.toString(), authorId);
			pubmedDump.addToAuthorValues(String.valueOf(authorId), firstName, lastName, email, affiliation);
		}
		return String.valueOf(authorId);
	}

	/**
	 * Generates Id for this keyword . If keyword doesnt exist in the keyword_ids ,
	 * new id is created and also inserted into keyword table
	 * 
	 * @param keyword - keyword Text
	 * @return - generated Id for give keyword
	 * 
	 */
	public String generateKeywordId(String keyword) {
		Long keywordId = null;
		keywordId = keyword_ids.get(keyword);
		if (keywordId == null) {
			keywordId = ++keywodIDSequence;
			keyword_ids.put(keyword, keywordId);
			pubmedDump.addToKeyWordValues(String.valueOf(keywordId), keyword);
		}
			
		return String.valueOf(keywordId);
	}

	/**
	 * Generates Id for the given conference details . Conference name and number uniquely
	 * identifies a conference . If this key doesnt exist in the conf_ids map , new id is
	 * generated and also these conference details are inserted into the conference table 
	 * 
	 * @param confDate - date of Conference
	 * @param confName - name of the conference
	 * @param confNum - conference number
	 * @param confLoc - conference location
	 * @param confSpon - conference sponsor
	 * @param confTheme - conference theme
	 * @param acronym - acronym of the conference
	 * @return - generated Id for this conference
	 */
	public  String generateConferenceId(String confDate , String confName , String confNum , String confLoc , String confSpon , String confTheme,String acronym) {
		String conf = confName + "-" + confNum;
		Long confId = conf_ids.get(conf);
		if (confId == null) {
			confId = ++confSequence;
			conf_ids.put(conf, confId);
			pubmedDump.addToConferenceValues(String.valueOf(confId), confDate,confName,confNum,confLoc,confSpon,confTheme,acronym);
		}
		return String.valueOf(confId);
	}
	
	/**
	 * Generates Id for the give category subject. If this category 
	 * doesn't exist in the category_ids map, new Id is generated and also 
	 * this category is inserted into category table 
	 * 
	 * @param subject - subject of category
	 * @return - generated Id for this category
	 */
	public  String getCategoryId(String subject , String parentCategory){
		Long categoryId = null;
		String key = subject + "-" + parentCategory;
		categoryId = category_ids.get(key);
		if (categoryId == null) {
			categoryId = ++keywodIDSequence;
			category_ids.put(key, categoryId);
			pubmedDump.addToCategoryValues(String.valueOf(categoryId), parentCategory, subject);
		}			
		return String.valueOf(categoryId);		
	}
	
	/**
	 * Generates Id for the given volume details . Combination of 
	 *  journalTitle + volume+issue is defined as unique key. If key doesn't exist in 
	 *  vol_ids table , new key generated and also insert into the volume insert values
	 *  to create a new row for this volume
	 * 
	 * @param volId - generated Volume Id
	 * @param vol - volume of the article
	 * @param issue - issue of the article
	 * @param journalId - journal id from journal-meta
	 * @param journalTitle - journal title from journal-meta
	 * @param publisherName - publisher name from journal-meta
	 * @return - generate volumeId for this volume
	 */
	public String getVolumeId(String journalTitle , String journalId, String volume , String issue , String publisherName){
		Long volId = null;
		String volKey = journalId+"-"+volume+"-"+issue;
		volId = vol_ids.get(volId);
		if(volId == null){
			volId = ++volSequence;
			vol_ids.put(volKey, volId);
			pubmedDump.addToVolumeValues(String.valueOf(volId), volume, issue, journalId, journalTitle, publisherName);
		}
		return String.valueOf(volId);
	}

}
