package edu.iub.pubmed.graph;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import edu.iub.pubmed.utils.Constants;


/**
 * Delegator class for the graph classes
 * @author Abhilash(akoppula@indiana.edu)
 *
 */
public class GraphDelegator {
	
	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	RelevanceGraph relevanceGraph = null;
	HetrogeneousGraph hetroGraph = null;
	
	
	public GraphDelegator() {
		relevanceGraph = new RelevanceGraph();
		hetroGraph = new HetrogeneousGraph();
	}
	
	/**
	 * Delegates call to update methods of both the graphs
	 * 
	 * @param pubmedId - pubmed Id
	 * @param keywords - list of keywords
	 * @param citations - map of citations and edge weights
	 * 
	 * 
	 */
	public void updateGraph(String pubmedId ,  Set<String> keywords , Map<String,Double> citations){
		LOGGER.fine("Updating Relevance Graph");
		relevanceGraph.updateGraph(pubmedId, citations	, keywords);
		LOGGER.fine("Updating Hetrogeneous Graph");
		hetroGraph.updateGraph(pubmedId, citations	, keywords);
	}
	

}
