package edu.iub.pubmed.file;

import java.io.File;
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
		File rootDirectory = null;
		File[] subFiles = null;
		File currentFile = null;
		rootDirectory = new File(currentPath);
		if (rootDirectory != null && rootDirectory.exists()) {
			subFiles = rootDirectory.listFiles();
			for (int index = 0; index < subFiles.length; index++) {
				currentFile = subFiles[index];
				if (currentFile.isFile()) { // If file parse else get the files of the directory
					pubmedCentral.loadFileToBackend(currentFile
							.getAbsolutePath());
				} else if (currentFile.isDirectory()) {
					pubmedCentral.checkForDumping(prevDirectory,
							currentDirectory);
					currentDirectory = currentFile.getAbsolutePath();
					LOGGER.info("Processing  directory :: "
							+ currentFile.getName());
					traverseAndLoad(currentFile.getAbsolutePath()); // Recursive call to process this directory
					prevDirectory = currentDirectory;
				}
			}
		}

	}

}
