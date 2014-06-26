package edu.iub.pubmed.dump;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import edu.iub.pubmed.utils.Constants;


/**
 * Methods for creating dump files for all the tables 
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public class DumpFiles {
	private static final Logger LOGGER = Logger.getLogger("PubmedMining");
	String dumpDirectory = null;
	String articleFile = null;            // an entry for each article
	String keywordFile = null;            // keywords used to describe the articles
	String keywordReferenceFile = null;   // relationship between a keyword and a paper
	String authorFile = null;             // each author in any of the papers - the author 
	                                      // is currently considered unique based on their 
	                                      // first and last name as well as their email and 
	                                      // the name of the institution they are affiliated 
	                                      // with as listed in the paper.
	String authorReferenceFile = null;    // relationship between an author and an article
	String categoryFile = null;           // papers are classified into broad categories
	String categoryReferenceFile = null;  // relationship between an article and a category  
	String volumeFile = null;             // Where a paper was published
	String conferenceFile = null;
	String citationFile = null;           // relationship between citing and cited paper
	String pumbedReferenceFile = null;    // the contextual citation of a referenced paper - there 
	                                      // could be multiple records for a relation in the citation
	                                      // file, but there will generally be at least one (unless 
	                                      // a paper lists another paper as a reference without citing it). 
	

	/**
	 * Creates a dump file for the given values either by appending to existing file or creating new file . <br>
	 * 
	 * Before appending to current file , this method checks the size of the current file . If the size of file 
	 * exceeds the MAX_VALUE , closes the current file and creates new dump file
	 * 
	 * 
	 * @param values - list of values to write to dumpFile
	 * @param dumpFile - name of the dump file
	 * @param prefix - prefix for file name if new dump file has to be created
	 * @param initialQuery - initial insert query in case of new dump file
	 * 
	 * @return - name of the dump file new or existing 
	 * 
	 * @throws Exception - if file Not found
	 */
	public String createDump(List<String> values, String dumpFileName,
			String prefix, String initialQuery) throws Exception {
		// Check if we should start a new dump file
		if (dumpFileName == null || checkFileSize(dumpFileName)) {
			if(dumpFileName != null){
				closeDumpFile(dumpFileName);
			}
			dumpFileName = createFileName(prefix); //start a new dump file
			writeToFile(initialQuery, values, dumpFileName);
		} else { //just append to the existing file
			writeToFile(null, values, dumpFileName);
		}
		return dumpFileName;
	} //end of createDump

	/**
	 * Appends ";" to the end of the file to make the dump file a valid
	 *  SQL file .
	 * 
	 * @param dumpFile - name of the dumpFile
	 * @throws IOException - if file doesn't exist
	 */
	private void closeDumpFile(String dumpFile) throws IOException {
		PrintWriter out = null;
		if(dumpFile != null){
			try {
				out = new PrintWriter(new BufferedWriter(
					new FileWriter(dumpFile, true))); 
				out.print(Constants.QUERY_CLOSING_CHARACTER);
				out.flush();
			}catch (IOException e) {
				e.printStackTrace();
				throw e;
			} finally {
				try{out.close();}catch(Exception e){}
			}
		}
		
	}
	
	/**
	 * Appends ";" to all the dumpfiles. This method is called after processing all the articles
	 * 
	 * and to dump the remaining values
	 */
	 public void closeAllDumpFiles() throws IOException{
		 closeDumpFile(articleFile);
		 closeDumpFile(authorFile);
		 closeDumpFile(authorReferenceFile);
		 closeDumpFile(categoryFile);
		 closeDumpFile(categoryReferenceFile);
		 closeDumpFile(keywordFile);
		 closeDumpFile(keywordReferenceFile);
		 closeDumpFile(volumeFile);
		 closeDumpFile(conferenceFile);
		 closeDumpFile(citationFile);
		 closeDumpFile(pumbedReferenceFile);
	 }

	/**
	 * Checks the size of the given file and compares with the MAX_DUMP_FILE_SIZE 
	 *  value in the constants file 
	 * 
	 * 
	 * @param fileName - name of the dump file
	 * @return - true if file size exceeds MAX_DUMP_FILE_SIZE else false
	 */

	public boolean checkFileSize(String fileName) {
		double kiloBytes = 0;
		double megaBytes = 0;
		File file = new File(fileName);
		kiloBytes = file.length() / 1024;
		megaBytes = kiloBytes / 1024;
		if (megaBytes < Constants.MAX_DUMP_FILE_SIZE) {
			return false;
		}
		return true;
	}

	/**
	 * Appends the given list of values to the given file . There are two possible scenarios
	 * for this method . The given file is either new dump file or existing dump file . In case 
	 * of new file , before appending the values initial insert query "INSERT INTO (tableName) VALUES"
	 * is added and then values are appended .<br>
	 * <br/>
	 * Initial Query is used to check whether given file is new or existing . If not null , initial query and 
	 * first set of values of are appended initially else all the values are directly appended.
	 * <br/>
	 * The List passed as a parameter will be cleaned out as part of the print process.
	 * 
	 * @param initialQuery - Initial insert query
	 * @param values - list of values to be inserted into table
	 * @param fileName - name of the dump file 
	 * @throws Exception - If file not found or there is an error accessing the list.
	 */
	public void writeToFile(String initialQuery, List<String> values, String fileName) throws Exception {
		PrintWriter out = null;
		if (values == null || values.isEmpty() ) {
			LOGGER.severe("DumpFiles-writeToFile: called with an arraylist of values that was null or empty.");
			return;
		}
		try {
			out = new PrintWriter(new BufferedWriter(
					new FileWriter(fileName, true)));
			if (initialQuery != null) {
				out.print(initialQuery);
				out.print(values.remove(0));
			}
			while (!values.isEmpty() ) {
				out.println(",");
				out.print(values.remove(0));
			}
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try{out.close();} catch(Exception e){}
		}
	}

	/**
	 * Creates a file name by appending dumpDirectory , given prefix and current system time.
	 * 
	 * <br> <br>
	 * 
	 * Eg : If prefix is 'keyword_reference_' , this method returns 'dumpDirectory\\keyword_reference_YYYY-M-dd-HH-mm-SSS.sql'
	 *  
	 * @param prefix - file Name prefix which is usually table name
	 * 
	 * @return - file name for the new dump file
	 */
	public String createFileName(String prefix) {
		StringBuilder fileNameBuilder = new StringBuilder();
		//fileNameBuilder.append(dumpDirectory);
		//fileNameBuilder.append("\\");
		fileNameBuilder.append(prefix);
		fileNameBuilder.append(getCurrentTime());
		fileNameBuilder.append(".sql");
		return fileNameBuilder.toString();
	}

	/**
	 * Returns the current time in the  format specified in {@link Constants}
	 * 
	 * @return - returns date in the above mentioned format
	 */
	public String getCurrentTime() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"YYYY-M-dd-HH-mm-SSS");
		return dateFormat.format(date);

	}

	/**
	 * Getter and setter methods for the dump file names.
	 * @return
	 */

	public String getDumpDirectory() {
		return dumpDirectory;
	}


	public void setDumpDirectory(String dumpDirectory) {
		this.dumpDirectory = dumpDirectory;
	}


	public String getArticleFile() {
		return articleFile;
	}


	public void setArticleFile(String articleFile) {
		this.articleFile = articleFile;
	}


	public String getKeywordFile() {
		return keywordFile;
	}


	public void setKeywordFile(String keywordFile) {
		this.keywordFile = keywordFile;
	}


	public String getKeywordReferenceFile() {
		return keywordReferenceFile;
	}


	public void setKeywordReferenceFile(String keywordReferenceFile) {
		this.keywordReferenceFile = keywordReferenceFile;
	}


	public String getAuthorFile() {
		return authorFile;
	}


	public void setAuthorFile(String authorFile) {
		this.authorFile = authorFile;
	}


	public String getAuthorReferenceFile() {
		return authorReferenceFile;
	}


	public void setAuthorReferenceFile(String authorReferenceFile) {
		this.authorReferenceFile = authorReferenceFile;
	}


	public String getCategoryFile() {
		return categoryFile;
	}


	public void setCategoryFile(String categoryFile) {
		this.categoryFile = categoryFile;
	}


	public String getCategoryReferenceFile() {
		return categoryReferenceFile;
	}


	public void setCategoryReferenceFile(String categoryReferenceFile) {
		this.categoryReferenceFile = categoryReferenceFile;
	}


	public String getVolumeFile() {
		return volumeFile;
	}


	public void setVolumeFile(String volumeFile) {
		this.volumeFile = volumeFile;
	}


	public String getConferenceFile() {
		return conferenceFile;
	}


	public void setConferenceFile(String conferenceFile) {
		this.conferenceFile = conferenceFile;
	}
	
	
	public String getCitationFile() {
		return citationFile;
	}
	
	public void setCitationFile(String citationFile) {
		this.citationFile = citationFile;
	}

	public String getPumbedReferenceFile() {
		return pumbedReferenceFile;
	}


	public void setPumbedReferenceFile(String pumbedReferenceFile) {
		this.pumbedReferenceFile = pumbedReferenceFile;
	}
	
}
