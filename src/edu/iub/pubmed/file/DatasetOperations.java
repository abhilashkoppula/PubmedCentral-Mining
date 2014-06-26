package edu.iub.pubmed.file;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Logger;

import edu.iub.pubmed.PubmedCentral;
import edu.iub.pubmed.dump.PubmedDump;
import edu.iub.pubmed.utils.Constants;

/**
 * Class to access the dataset .
 * 
 * @author Abhilash
 * 
 */
public class DatasetOperations {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	/**
	 * Directory up to which parsing and dumping was successful
	 */
	private String prevDirectory = null;
	/**
	 * All the directories between prev and current are getting dumped
	 */
	private String currentDirectory = null;
	private PubmedCentral pubmedCentral = null;

	/**
	 * 
	 * @param dataSetPath
	 * @param pubmedCentral
	 */
	public DatasetOperations(String dataSetPath, PubmedCentral pubmedCentral) {
		this.prevDirectory = dataSetPath;
		this.pubmedCentral = pubmedCentral;
	}



}
