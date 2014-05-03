package edu.iub.pubmed.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import static edu.iub.pubmed.utils.Constants.*;

public class DumpFiles {
	String dumpDirectory = null;
	String articleFile = null;
	String keywordFile = null;
	String keywordReferenceFile = null;
	String authorFile = null;
	String authorReferenceFile = null;
	String categoryFile = null;
	String categoryReferenceFile = null;
	String volumeFile = null;
	String conferenceFile = null;
	String pumbedReferenceFile = null;
	
	
	
	public DumpFiles(String dumpDirectory){
		this.dumpDirectory = dumpDirectory;
		articleFile = createFileName(FILE_PREFIX_ARTICLE);
		keywordFile = createFileName(FILE_PREFIX_KEYWORD);
		keywordReferenceFile = createFileName(FILE_PREFIX_KEYWORD_REFERENCE);
		authorFile = createFileName(FILE_PREFIX_AUTHOR);
		authorReferenceFile = createFileName(FILE_PREFIX_AUTHOR_REFERENCE);
		categoryFile = createFileName(FILE_PREFIX_CATEGORY);
		categoryReferenceFile = createFileName(FILE_PREFIX_CATEGORY_REFERENCE);
		volumeFile = createFileName(FILE_PREFIX_VOLUME);
		conferenceFile = createFileName(FILE_PREFIX_CONFERENCE);
		pumbedReferenceFile = createFileName(FILE_PREFIX_PUBMED_REFERENCE);
		
	}
	

	public String createDump(List<String> values, String dumpFile,
			String prefix, String initialQuery) throws Exception {
		if (checkFileSize(dumpFile)) {
			dumpFile = createFileName(prefix);
			writeToFile(initialQuery, values, dumpFile);
		} else {
			writeToFile(null, values, dumpFile);
		}
		return dumpFile;
	}

	public boolean checkFileSize(String fileName) {
		double kiloBytes = 0;
		double megaBytes = 0;
		File file = new File(fileName);
		kiloBytes = file.length() / 1024;
		megaBytes = kiloBytes / 1024;
		if (megaBytes < 5) {
			return false;
		}
		return true;
	}

	public void writeToFile(String initialQuery, List<String> values,
			String fileName) throws Exception {

		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(fileName, true)))) {
			if (initialQuery != null) {
				out.print(initialQuery);
			}
			for (String value : values) {
				out.print(value);
				out.print(",");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

	public String createFileName(String prefix) {
		StringBuilder fileNameBuilder = new StringBuilder();
		fileNameBuilder.append(dumpDirectory);
		fileNameBuilder.append("\\");
		fileNameBuilder.append(prefix);
		fileNameBuilder.append(getCurrentTime());
		fileNameBuilder.append(".sql");
		return fileNameBuilder.toString();
	}

	public String getCurrentTime() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"YYYY-M-dd-HH-mm-SSS");
		return dateFormat.format(date);

	}


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


	public String getPumbedReferenceFile() {
		return pumbedReferenceFile;
	}


	public void setPumbedReferenceFile(String pumbedReferenceFile) {
		this.pumbedReferenceFile = pumbedReferenceFile;
	}
	
	
	
	

}
