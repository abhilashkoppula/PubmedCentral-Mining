package edu.iub.pubmed.utils;

public class UtilityMethods {
	
	
/**
 * Formats the given string by removing '\n'	
 * @param givenString
 * @return
 */
public static String formatString(String givenString){
	String formattedString = null;
		
	if (givenString != null) {
		formattedString = givenString.toLowerCase().trim();
		formattedString = formattedString.replaceAll("[\\n,\\.,*]", "");
		formattedString = formattedString.replaceAll("'", "\\'");
	}
		return formattedString;
	}

}
