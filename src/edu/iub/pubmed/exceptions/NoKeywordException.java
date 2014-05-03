package edu.iub.pubmed.exceptions;

public class NoKeywordException extends Exception {

	public NoKeywordException() {
		super("Given Keyword is not found in the graph");
	}

	public NoKeywordException(String message) {
		super("Given Keyword is not found in the graph" + message);
	}
}
