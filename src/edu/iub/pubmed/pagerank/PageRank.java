package edu.iub.pubmed.pagerank;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections15.Transformer;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.oupls.jung.GraphJung;

import edu.iub.pubmed.graph.HetrogeneousGraph;
import edu.iub.pubmed.graph.RelevanceGraph;
import edu.iub.pubmed.utils.Constants;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;

/**
 * Calculates the pagerank for all the nodes in the relevant graph for the
 * keywords given in the input file and updates the Heterogeneous graph to create
 * contributed edges/relationships between paper nodes and keyword nodes
 * 
 * <br>
 * Usage : javac PageRank <br>
 *         java PageRank KeywordInputFile
 * @author Abhilash
 *
 */
public class PageRank {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	private PageRankWithPriors<Vertex, Edge> pageRankWithPriors = null;
	private Graph graph = null;
	private RelevanceGraph relevanceGraph = null;
	private HetrogeneousGraph hetrogeneousGraph = null;
	private String keyWord = null;
	private Double priorWeight = 0.0;
	private List<String> totalKeyWords = null;
	
	

	public PageRank(String keyWordFile) {
		relevanceGraph = new RelevanceGraph();
		hetrogeneousGraph = new HetrogeneousGraph();
		graph = new Neo4j2Graph(relevanceGraph.getGraph());
		loadKeyWords(keyWordFile);
	}

	/**
	 * Loads the keywords from the file which has list of keywords for which
	 * page has to be calculated
	 * @param keywordFile - page rank keyword file
	 */
	private void loadKeyWords(String keywordFile) {
		totalKeyWords = new ArrayList<String>();
		String value = null;
		try {
			FileReader fileReader = new FileReader(keywordFile);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((value = bufferedReader.readLine()) != null) {
				totalKeyWords.add(value);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	

	/**
	 * Runs the Pagerank for each keyword of the input list. Following steps are 
	 * involved for every iteration
	 * 
	 * <ul>
	 * <li> Normalize the priors such that sum of all priors is 1.
	 * <li> Run the Jung PageRankWithPriors.evaluate method to calculate the page rank
	 * <li> Update the Heterogeneous graph for the current keyword and nodes with authorithy greater than 0.0
	 * </ul>
	 * 
	 */

	public void runPageRank() {
		GraphJung<Graph> jungGraph = new GraphJung<Graph>(graph);
		for (String currentKeyWord : totalKeyWords) {
			keyWord = currentKeyWord;
			LOGGER.log(Level.INFO,"Running page rank for Keyword :: " +  currentKeyWord);	
			findPriorWeight(keyWord);
			pageRankWithPriors = new PageRankWithPriors<Vertex, Edge>(
					jungGraph, priors, 0.1);
			pageRankWithPriors.setEdgeWeights(weights);
			pageRankWithPriors.evaluate();
			updateHetrogeneousGraph();
		}

	}

	/**
	 * 
	 * <b> TO DO : <b>  Change this method to update the Heterogeneous depending upon
	 * parameters like minimum page rank or Top 100 page ranks etc 
	 * 
	 * Updates the heterogeneous graph to add the edges/relationships 'contributed'
	 * 
	 * between keyword and paper nodes
	 * 
	 */
	private void updateHetrogeneousGraph() {
		Map<String,Double> contributions = new HashMap<String,Double>();
		double vertexScore = 0;		
		for(Node node : relevanceGraph.getGraph().getAllNodes()){
			vertexScore = pageRankWithPriors.getVertexScore(graph
					.getVertex(node.getId()));
			if(vertexScore > Constants.PAGE_RANK_THRESHOLD){
				contributions.put((String) node.getProperty(Constants.PROPERTY_PUBMED_ID), vertexScore);
			}
		}
		LOGGER.log(Level.INFO,"Updating Hetrogeneous Graph  for Keyword :: " +  keyWord);
			hetrogeneousGraph.updateGraphWithAuthority(keyWord, contributions);
	}
	
	
	/**
	 * Calculates the total prior to normalize each prior .
	 * @param currentKeyWord - keyword in current iteration
	 */
	private void findPriorWeight(String currentKeyWord) {
		priorWeight = 0.0;
		Transaction transaction = relevanceGraph.getGraph().beginTx();
		for (Node node : relevanceGraph.getGraph().getAllNodes()) {
			if(node.hasProperty(Constants.PROPERTY_KEYWORDS)){
				String[] keywords = (String[]) node.getProperty(Constants.PROPERTY_KEYWORDS);
				for (String keyword : keywords) {
					if (keyword.equals(currentKeyWord)) {
						priorWeight += (Double) node.getProperty(Constants.PROPERTY_PRIOR);
						break;
					}
				}
			}
		}
		transaction.success();
	}

	
    /**
     * Jung Pagerank algorithm internally calls transform for every edge to find the
     *  edge weight
     */
	Transformer<Edge, Double> weights = new Transformer<Edge, Double>() {
		@Override
		public Double transform(Edge edge) {
			return edge.getProperty(Constants.PROPERTY_WEIGHT);

		}
	};

	/**
     * Jung Pagerank algorithm internally calls transform for every node to find the
     *  prior
     */
	Transformer<Vertex, Double> priors = new Transformer<Vertex, Double>() {
		@Override
		public Double transform(Vertex vertex) {
			if(vertex.getPropertyKeys().contains(Constants.PROPERTY_PRIOR)){
			return ((Double) vertex.getProperty(Constants.PROPERTY_PRIOR) / priorWeight);
			} else {
				return Constants.DOUBLE_ZERO;
			}
		}
	};
	
	
	/**
	 * Returns true if the given keyword is present in the list of actual keywords used in the paper
	 * @param keywords - list of actual keywords of paper.
	 * 
	 * @param keyword - current keyword in the page rank iteration
	 * @return - true if current keyword is one of actual keywords
	 */
	public boolean hasKeyWord(String[] keywords, String keyword) {
		if (keywords == null) {
			return false;
		}
		for (String kwd : keywords) {
			if (kwd.equals(keyword)) {
				return true;
			}
		}
		return false;
	}

	
	/**
	 * Client method
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out
					.println("************************************************************");
			System.out
					.println("Invalid number of arguments : Usage : java PageRank <KeyWord Path> ");
			System.out
					.println("************************************************************");
		}
		String keywordsInputFile = args[0];
		PageRank pr = new PageRank(keywordsInputFile);
		pr.runPageRank();
	}

}