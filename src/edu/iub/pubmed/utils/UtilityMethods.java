package edu.iub.pubmed.utils;

import java.io.File;

public class UtilityMethods {
	
	
/**
 * Formats the given string so that SQL query formed with this string would not
 * result SQL query parsing issues
 * 
 * @param givenString
 * @return
 */
public static String formatString(String givenString){
	String formattedString = null;
		
	if (givenString != null) {
		formattedString = givenString.toLowerCase().trim();
		//formattedString = formattedString.replaceAll("[\\n,\\.,*]", " ");
		formattedString = formattedString.replaceAll("[\\n,*]", " ");
		formattedString = formattedString.replace("  ", " ");
		formattedString = formattedString.replace("'", "");
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
	new File(Constants.DUMP_DIR + File.separator + "article").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "author").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "volume").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "conference").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "keywords").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "category").mkdirs();
	new File(Constants.DUMP_DIR + File.separator + "citations").mkdirs();
	}catch(Exception ex){
		
	}
	
}


}
