package edu.iub.pubmed.graph;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.iub.pubmed.utils.Constants;

public class GraphDelegator {
	
	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	RelevanceGraph relevanceGraph = null;
	HetrogeneousGraph hetroGraph = null;
	
	
	public GraphDelegator() {
		relevanceGraph = new RelevanceGraph();
		hetroGraph = new HetrogeneousGraph();
	}
	
	public void updateGraph(String pubmedId ,  Set<String> keywords , Map<String,Double> citations){
		LOGGER.fine("Updating Relevance Graph");
		relevanceGraph.updateGraph(pubmedId, citations	, keywords);
		LOGGER.fine("Updating Hetrogeneous Graph");
		hetroGraph.updateGraph(pubmedId, citations	, keywords);
	}
	
	public void removeNonPubmedCentralNodes(){
		
	}

}
