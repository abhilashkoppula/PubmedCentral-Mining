package edu.iub.pubmed.exceptions;

public class NoPubmedIdException extends Exception {
	
	public NoPubmedIdException(){
		super();
	}

	public NoPubmedIdException(String message){
		super(message);
	}
}
