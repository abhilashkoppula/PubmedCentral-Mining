package edu.iub.pubmed;

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
	
	private DatasetOperations datasetOperations = null;

	/**
	 * Set of pubmedIds that are processed already . This is to avoid 
	 * processing the duplicate articles(articles with same pubmedId) .
	 */
	public static Set<String> pubmedIds = new HashSet<>();

	public PubmedCentral(String dumpfilesPath) {
		pubmedDump = new PubmedDump(dumpfilesPath);
		idGenerator = new IDGenerator(pubmedDump);
		graphDelegator = new GraphDelegator();
	}

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
			articleParser = new ArticleParser(fileName,idGenerator);
			articleParser.parse();
			LOGGER.log(Level.FINEST,"Parsing of {0} successfull ", fileName);
			if (true) {
				graphDelegator.updateGraph(articleParser.getPubmedId(),
						articleParser.getKeywords(),
						articleParser.getCitations());
			}
			articleParser = null;
		} catch (Exception ex) {
			LOGGER.warning("Exception while parsing ::" + ex.getMessage());
		}

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
		datasetOperations = new DatasetOperations(datasetPath, this);
		datasetOperations.traverseAndLoad(datasetPath);
		pubmedDump.createDump(); // To dump the last values
		LOGGER.log(Level.INFO, "Completed parsing and loading of the dataset");
		LOGGER.log(Level.INFO, "Removing pubmed nodes from graph which doesnt have article data");
		//graphDelegator.removeNonPubmedCentralNodes();
		LOGGER.log(Level.INFO, "Completed Dataset loading and Graph creation");
	}

	/** 
	 *  Checks if values exceeded the MAX_LIMIT and if exceeds 
	 *  creates dump files 
	 *  
	 * @param prevDirectory - directory up to which parsing and dumping is successful
	 * @param currentDirectory - directory up to which parsing is successful and eligible for dumping
	 * @throws Exception - 
	 */
	public void checkForDumping(String prevDirectory, String currentDirectory)
			throws Exception {
		if (pubmedDump.checkAndDump()) {
			LOGGER.info(" Creating dumping ");
			pubmedDump.createDump();
		}

	}

	public static void main(String args[]) throws Exception {
		if (args.length < 3) {
			System.out
					.println("************************************************************");
			System.out
					.println("Invalid number of arguments : Usage : java PubmedCentral <DataSet path> <DumpFiles path> <Log Directory>");
			System.out
					.println("************************************************************");
		}
		String datasetPath = args[0];
		String dumpfilesPath = args[1];
		String logDirectory = args[2];
		PubmedCentral pubmedCentral = new PubmedCentral(dumpfilesPath);
		pubmedCentral.load(datasetPath);
	}

}
