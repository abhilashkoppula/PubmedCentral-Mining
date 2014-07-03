package edu.iub.pubmed.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


/**
 * CheckDirectories<br/>
 * the only purpose of this class is that when the program dies, we can check the 
 * log and see which directories were not processed. 
 * @author scjensen
 *
 */
public class CheckDirectories {
	private static final String PROCESSING_PREFIX = "INFO: Processing  directory :: ";
	private static final int PREFIX_LENGTH = PROCESSING_PREFIX.length();

	public static void CheckLog(String logName, String dataName, String outputName) throws Exception {
		
		File logFile = new File(logName);
		if (!logFile.isFile())
			throw new Exception("the logfile does not exist or is not a file: " + logName);
		File dataDir = new File(dataName);
		if (!dataDir.isDirectory())
			throw new Exception("the data directory does not exist or is not a directory: " + dataName);
		File outputFile = new File(outputName);
		if (outputFile.exists())
			throw new Exception("the output file already exists: " + outputName);
		
		BufferedReader br = null;
		TreeSet<String> processed = null;
		TreeMap<String,Integer> notProcessed = null;
		ArrayList<File> dirToCheck = null;
		PrintWriter pw = null;
		try {
			br = new BufferedReader( new FileReader(logFile) );
			processed = new TreeSet<String>();
			String logLine = br.readLine();
			while (logLine != null) {
				if (logLine.trim().startsWith(PROCESSING_PREFIX) ) {
					String journalName = logLine.substring(PREFIX_LENGTH).trim();
					processed.add(journalName);
				}
				logLine = br.readLine();
			} //loop through the log file
			br.close();
			// Check if each directory in the data files is in the log file
			notProcessed = new TreeMap<String,Integer>();
			dirToCheck = new ArrayList<File>(processed.size());
			dirToCheck.add(dataDir);
			while (!dirToCheck.isEmpty()) {
				File dir = dirToCheck.remove(0);
				File[] contents = dir.listFiles();
				boolean hasFiles = false;
				for (File myFile: contents) {
					if (myFile.isDirectory())
						dirToCheck.add(myFile);
					else
						hasFiles = true;
				}
				String dirName = dir.getName();
				if (hasFiles) {  //check if we processed it
					if (!processed.contains(dirName))
						notProcessed.put(dirName, contents.length);
				} else {
					// if there are no files, remove it from the processed list
					processed.remove(dirName);
				}
				contents = null;
			} //Print to the output file
			pw = new PrintWriter(outputFile);
			pw.println("Number of directories processed: " + processed.size());
			pw.println();
			if (notProcessed.size() == 0)
				pw.println("No directories were not processed.");
			else {
				pw.println("Directories not processed (with file counts): " + notProcessed.size() );
				for (Entry<String, Integer> entry: notProcessed.entrySet() ) {
					pw.println(entry.getKey() + "   (" + entry.getValue() + ")"); 
				}
			}
			pw.flush();
			return;
		} finally {
			try{br.close();}catch(Exception e){}
			try{pw.close();}catch(Exception e){}
			try{processed.clear();}catch(Exception e){}
			try{notProcessed.clear();}catch(Exception e){}
			try{dirToCheck.clear();}catch(Exception e){}
		}
	} //end of CheckLog
	
	
	/**
	 * @param args
	 * Argument 1: The full path and file name of the log file generated
	 * Argument 2: The full path and file name of the directory containing the data directories
	 * Argument 3: The full path and file name of the output file to be created
	 */
	public static void main(String[] args) throws Exception {
		String logFileName = args[0];
		String dataDirName = args[1];
		String outputFileName = args[2];
		CheckLog(logFileName, dataDirName, outputFileName);
		System.out.println("Done");
	} //end of main

}
