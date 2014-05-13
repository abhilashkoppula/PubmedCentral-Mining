package edu.iub.pubmed.utils;

import java.io.File;

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


/**
 * Creates directories 
 */
public static void createDirectories() {
	
	try {
	new File(Constants.PUBMED_DIR).mkdirs();
	new File(Constants.GRAPH_DIR).mkdirs();
	new File(Constants.LOGS_DIR).mkdirs();
	new File(Constants.DUMP_DIR).mkdirs();
	new File(Constants.DUMP_DIR).mkdirs();
	new File(Constants.DUMP_DIR).mkdirs();
	new File(Constants.DUMP_DIR+"\\article").mkdirs();
	new File(Constants.DUMP_DIR+"\\author").mkdirs();
	new File(Constants.DUMP_DIR+"\\volume").mkdirs();
	new File(Constants.DUMP_DIR+"\\conference").mkdirs();
	new File(Constants.DUMP_DIR+"\\keywords").mkdirs();
	new File(Constants.DUMP_DIR+"\\category").mkdirs();
	new File(Constants.DUMP_DIR+"\\citations").mkdirs();
	}catch(Exception ex){
		
	}
	
}


}
