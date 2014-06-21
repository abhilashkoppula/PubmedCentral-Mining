package edu.iub.pubmed.dump;

public class CitationPair {

	private long citationId;
	private short referenceId;

	public CitationPair(long citationId, short referenceId) {
		this.citationId = citationId;
		this.referenceId = referenceId; 
	}

	public CitationPair(CitationPair citation) {
		this.citationId = citation.getCitationId();
		this.referenceId = citation.getReferenceId();
	}

	public long getCitationId() {
		return citationId;
	}

	public short getReferenceId() {
		return referenceId;
	}

	public void incrementReferenceId() {
		referenceId++;
	}
} //end of class CitationPair

