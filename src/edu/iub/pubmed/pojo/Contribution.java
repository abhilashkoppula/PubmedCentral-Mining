package edu.iub.pubmed.pojo;

public class Contribution {
	String pubmedId = null;
double weight = 0.0;
	
	public Contribution(String pubmedId , double weight){
		this.pubmedId = pubmedId;
		this.weight = weight;
	}

	public String getPubmedId() {
		return pubmedId;
	}

	public void setPubmedId(String pubmedId) {
		this.pubmedId = pubmedId;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "[ " + pubmedId +" , " + weight +" ]";
	}

}
