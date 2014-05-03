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

public class PubmedCentral {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	private IDGenerator idGenerator = null;
	private PubmedDump pubmedDump = null;
	private GraphDelegator graphDelegator = null;
	private DatasetOperations datasetOperations = null;

	public static Set<String> pubmedIds = new HashSet<>();

	public PubmedCentral(String dumpfilesPath) {
		idGenerator = new IDGenerator();
		pubmedDump = new PubmedDump(dumpfilesPath);
		graphDelegator = new GraphDelegator();
	}

	public void loadFileToBackend(String fileName) throws Exception {
		ArticleParser articleParser = null;
		try {
			LOGGER.log(Level.FINEST,"Parsing file" + fileName);
			articleParser = new ArticleParser(fileName);
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

	public void load(String datasetPath) throws Exception {
		LOGGER.log(Level.INFO, "Started parsing and loading of the dataset");
		LOGGER.config("Given dataset location " + datasetPath);
		datasetOperations = new DatasetOperations(datasetPath, this);
		datasetOperations.traverseAndLoad(datasetPath);
		LOGGER.log(Level.INFO, "Completed parsing and loading of the dataset");
		LOGGER.log(Level.INFO, "Removing pubmed nodes from graph which doesnt have article data");
		graphDelegator.removeNonPubmedCentralNodes();
		LOGGER.log(Level.INFO, "Completed Dataset loading and Graph creation");
	}

	public void checkForDumping(String prevDirectory, String currentDirectory)
			throws Exception {
		if (pubmedDump.checkAndDump()) {
			LOGGER.info("");
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
