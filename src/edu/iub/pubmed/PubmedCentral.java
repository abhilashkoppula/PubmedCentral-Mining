package edu.iub.pubmed;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.iub.pubmed.dump.IDGenerator;
import edu.iub.pubmed.dump.PubmedDump;
import edu.iub.pubmed.file.DatasetOperations;
import edu.iub.pubmed.graph.GraphDelegator;
import edu.iub.pubmed.parsing.ArticleParser;
import edu.iub.pubmed.utils.Constants;
import edu.iub.pubmed.utils.UtilityMethods;

/**
 * Handles total process flow
 * 
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public class PubmedCentral {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	private IDGenerator idGenerator = null;
	private PubmedDump pubmedDump = null;
	private GraphDelegator graphDelegator = null;
	HashSet<String> articleIds = null;
	
//	private DatasetOperations datasetOperations = null;

//	/**
//	 * Set of pubmedIds that are processed already . This is to avoid 
//	 * processing the duplicate articles(articles with same pubmedId) .
//	 */
//	public static Set<String> pubmedIds = new HashSet<String>();
 
	public PubmedCentral() {
		pubmedDump = new PubmedDump();
		idGenerator = new IDGenerator(pubmedDump);
		graphDelegator = new GraphDelegator();
	}

	
	/**
	 * Entry method to the pubmed mining which handles total data set parsing
	 * 
	 * @param datasetPath - root directory path of the data set
	 * @throws Exception - if any exception occurred in the process
	 * 
	 */
	public void load(String datasetPath) throws Exception {
		LOGGER.log(Level.INFO, "Started parsing and loading of the dataset");
		LOGGER.config("Given dataset location " + datasetPath);
		traverseAndLoad(datasetPath);
		pubmedDump.dumpRemaining(); // To dump the last values
		LOGGER.log(Level.INFO, "Completed parsing and loading of the dataset");
		LOGGER.log(Level.INFO, "Pruning the graphs for better performance");
		graphDelegator.removeNonPubmedCentralNodes();
		LOGGER.log(Level.INFO, "Completed Dataset loading and Graph creation");
	} //end of load
	
	
	/**
	 * Traverses the given path recursively and for each file invokes
	 * {@code pubmedCentral.loadFileToBackend} to parse and load the file . Also
	 * before processing a directory , values in {@link PubmedDump} are checked
	 * to avoid out of memory exception
	 * 
	 * @param currentPath
	 *            - path of the directory or file
	 * @throws Exception
	 *             - throws exception if parsing of the file fails
	 */
	public void traverseAndLoad(String currentPath) throws Exception {
		File currentDirectory = null;
		ArrayList<File> directoriesToProcess = new ArrayList<File>();
		articleIds = new HashSet<String>();
			
		currentDirectory = new File(currentPath);
		if (!checkDirectoryExists(currentDirectory) )
			return;
		//While there are directories, process them
		directoriesToProcess.add(currentDirectory);
		while (!directoriesToProcess.isEmpty()) {
			currentDirectory = directoriesToProcess.remove(0);
			LOGGER.info("Processing  directory :: " + currentDirectory.getName() );
			// Process This directory
			File[] subFiles = currentDirectory.listFiles();
			for (File currentFile: subFiles) {

				if (currentFile.isFile()) { // If file, then parse  
					loadFileToBackend(currentFile.getAbsolutePath());
				} else if (currentFile.isDirectory()) { 
					//add directory to list of directories to process
					directoriesToProcess.add(currentFile);
					//						currentDirectory = currentFile.getAbsolutePath();
					//						prevDirectory = currentDirectory;
				}
				// check if we need to dump after processing this directory
				checkForDumping();
			} //loop through contents of the directory
			articleIds.clear();  //done with this journal
		} //process all directories
		articleIds = null;
	} //end of traverseAndLoad
			
		
	private boolean checkDirectoryExists(File directory) {
		return (directory != null && directory.exists() );
	} //end of checkDirectoryExists

	
	/**
	 * Creates Article Parser object to parse the give file and if parsing is successful
	 * updates both the graphs 
	 * 
	 * @param fileName - XML file name
	 * @throws Exception  if parsing fails 
	 */
	public void loadFileToBackend(String fileName) throws Exception {
		ArticleParser articleParser = null;
		try {
			LOGGER.log(Level.FINEST,"Parsing file" + fileName);
			articleParser = new ArticleParser(fileName,idGenerator,pubmedDump);
			// check if the article ID is already in the dataset
			String articleId = articleParser.getPubmedId();
			if (articleIds.add(articleId) ){
				articleParser.parse();
				LOGGER.log(Level.FINEST,"Parsing of {0} successfull ", fileName);
				graphDelegator.updateGraph(articleParser.getPubmedId(),
						articleParser.getKeywords(),
						articleParser.getCitations());
			} else {
				LOGGER.warning("Article ID: " + articleId + 
					" from file " + fileName +
					" already exist is in the dataset and was not processed again.");
			}
			articleParser = null;
		} catch (Exception ex) {
			LOGGER.severe("Exception while parsing ::" + ex.getMessage());
		}
	}//end of loadFileToBackend
	

	/** 
	 *  Checks if values exceeded the MAX_LIMIT and if does, then 
	 *  creates dump files. 
	 *  
	 * @throws Exception if there is a problem in creating the dump file
	 */
	public void checkForDumping() throws Exception {
		if (pubmedDump.checkAndDump()) {
			LOGGER.info(" Creating dumping ");
			pubmedDump.createDump();
		}
	} //end of checkForDumping
	
	

	public static void main(String args[]) throws Exception {
		if (args.length < 1) {
			System.out
					.println("************************************************************");
			System.out
					.println("Invalid number of arguments : Usage : java PubmedCentral <DataSet path> ");
			System.out
					.println("************************************************************");
		} else {

			String datasetPath = args[0];
			UtilityMethods.createDirectories();
			PubmedCentral pubmedCentral = new PubmedCentral();
			pubmedCentral.load(datasetPath);
		}
	}

}
