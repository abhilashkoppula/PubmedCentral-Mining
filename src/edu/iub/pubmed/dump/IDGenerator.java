package edu.iub.pubmed.dump;

import java.util.HashMap;
import java.util.Map;

public class IDGenerator {

	public static long keywodIDSequence = 999;
	public static long categoryIDSequence = 9999;
	public static long authorIDSequence = 9;
	public static long confSequence = 999999;
	public static long volSequence = 99999999;
	
	public static Map<String, Long> keyword_ids = new HashMap<>();
	public static Map<String, Long> category_ids = new HashMap<>();
	public static Map<String, Long> author_ids = new HashMap<>();
	public static Map<String, Long> conf_ids = new HashMap<>();

	public  long getId(String key, Map<String, Long> map, long authorId) {
		Long currentId = null;
		currentId = map.get(key);
		if (currentId == null) {
			currentId = ++authorId;
			map.put(key, currentId);
		}
		return currentId;
	}

	public  String getAuthorId(String givenName, String surName) {
		String author = givenName + "-" + surName;
		long authorId = getId(author, author_ids, authorIDSequence);
		return String.valueOf(authorId);
	}

	public  String generateKeywordId(String keyword) {
		long keywordId = getId(keyword, keyword_ids, keywodIDSequence);
		return String.valueOf(keywordId);

	}

	public  String generateConferenceId(String confName, String confNum) {
		String conf = confName + "-" + confNum;
		long confId = getId(conf,conf_ids,confSequence);
		return String.valueOf(confId);
	}
	
	public  String getCategoryId(String subject){
		long categoryId = getId(subject,category_ids,categoryIDSequence);
		return String.valueOf(categoryId);
	}

}
