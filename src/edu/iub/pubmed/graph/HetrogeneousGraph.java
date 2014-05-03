package edu.iub.pubmed.graph;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.ReadableIndex;

import edu.iub.pubmed.exceptions.NoKeywordException;
import edu.iub.pubmed.exceptions.NoPubmedIdException;
import edu.iub.pubmed.utils.Constants;

/**
 * Heterogeneous graph with nodes for papers and keywords and relationships
 * cited,relevance and contributed . This graph is created after calculating 
 * page rank for the papers from the relevance graph . 		
 * 
 * @author Abhilash
 *
 */
public class HetrogeneousGraph {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	GraphDatabaseService graphDb = null;
	ReadableIndex<Node> autoNodeIndex = null;

	private static enum RelTypes implements RelationshipType {
		CITED, CONTRIBUTED, RELEVANT
	}

	private static enum Lables implements Label {
		PAPER, KEYWORD
	}
	
	
	/**
	 * Creates a new graph from the NEO4J_HETROGENEOUS_GRAPH_PATH location and
	 * creates two indexes for pubmedId and Keyword
	 * 
	 */
	public HetrogeneousGraph() {
		LOGGER.log(Level.CONFIG , "Creating Hetrogeneous graph at directory " + Constants.NEO4J_HETROGENEOUS_GRAPH_PATH);
		LOGGER.log(Level.CONFIG , "Creating Index for Nodes on property pubmedId and keywordText");
		graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder(
						Constants.NEO4J_HETROGENEOUS_GRAPH_PATH)
				.setConfig(GraphDatabaseSettings.node_keys_indexable,
						"pubmedId,keywordText")
				.setConfig(GraphDatabaseSettings.node_auto_indexing, "true")
				.newGraphDatabase();
		autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
	}
	
	
	/**
	 * Creates a new paper node for the given pubmedId if not present in the graph and 
	 * sets the property {@link Constants.PROPERTY_PUBMED_ID}
	 * 
	 * @param keyValue - new pubmed Id 
	 * @return Node - graph node
	 * 
	 */
	Node createPaperNode(String keyValue) {
		Node node = null;
		node = autoNodeIndex.get(Constants.PROPERTY_PUBMED_ID, keyValue)
				.getSingle();
		if (node == null) {
			LOGGER.log(Level.FINER,"Node doesnt exist for pubmed Id {0}. Creating new Paper node " , keyValue);
			node = graphDb.createNode(Lables.PAPER);
			node.setProperty(Constants.PROPERTY_PUBMED_ID, keyValue);
		}
		return node;
	}

	/**
	 * Returns the graphdb object
	 * @return graphdatabaseservice object
	 */
	public GraphDatabaseService getGraph() {
		return this.graphDb;
	}

	/**
	 * Creates a new keyword node for the given keyword if not present in the graph and 
	 * sets the property {@link Constants.PROPERTY_KEYWORD}
	 * 
	 * @param keyValue - new keyword 
	 * @return Node - graph node
	 */
	Node createKeyWordNode(String keyValue) {
		Node node = null;
		node = autoNodeIndex.get(Constants.PROPERTY_KEYWORD, keyValue)
				.getSingle();
		if (node == null) {
			LOGGER.log(Level.FINER,"Node doesnt exist for keyword '{0}'. Creating new Keyword node " , keyValue);
			node = graphDb.createNode(Lables.KEYWORD);
			node.setProperty(Constants.PROPERTY_KEYWORD, keyValue);
		}
		return node;
	}
	

	/**
	 *  Updates graph by creating a new node for the given pub med Id and new nodes if
	 *  required for the citedPubmeds . For every new paper node created , two  properties
	 *  {@link Constants}.PROPERTY_PUBMED_ID,{@linkConstants}.PRIOR
	 *  
	 *  A new keyword is created if required for the given list of keyword and relationship 
	 *  'RELEVANT' for this pubmed node and keyword node .
	 *  
	 *  For each cited pubmed Id , relationship 'CITED' is created  between pubmedId and citedpubmed Id
	 *  
	 * @param pubmedId - new pubmed id 
	 * @param citedPubmedIds - list of cited pubmed Ids
	 * @param keyWords - list of keywords used in this pubmedid
	 */
	public void updateGraph(String pubmedId, Map<String,Double> citedPubmedIds,
			Set<String> keyWords) {
		Node node = null;
		Node keywordNode = null;
		Node citedNode = null;
		Relationship relevant = null;
		Relationship cited = null;
		double keyWordsWeight = 0;
		try (Transaction tx = graphDb.beginTx()) {
			LOGGER.log(Level.FINER,"Updating graph for pubmedId {0} with keywords {1} and citations {2} " , new Object[]{pubmedId , keyWords,citedPubmedIds});
			node = createPaperNode(pubmedId);
			if (keyWords.size() >= 1) {
				keyWordsWeight = 1d / keyWords.size();
			}
			node.setProperty(Constants.PROPERTY_PRIOR, new Double(
					keyWordsWeight));
			for (String keyword : keyWords) {
				keywordNode = createKeyWordNode(keyword);
				relevant = node.createRelationshipTo(keywordNode,
						RelTypes.RELEVANT);
				relevant.setProperty(Constants.PROPERTY_WEIGHT, keyWordsWeight);
			}
			for (String citation : citedPubmedIds.keySet()) {
				citedNode = createPaperNode(citation);
				cited = node.createRelationshipTo(citedNode, RelTypes.CITED);
				cited.setProperty(Constants.PROPERTY_WEIGHT, citedPubmedIds.get(citation));
			}
			LOGGER.log(Level.FINER,"Updating Successfull " );
			tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Updates the graph by adding edges between keywords and papers based on
	 * the authority values from page rank algorithm . 
	 * 
	 * @param keyword - current keyword
	 * @param contributedPubmedIds - hashmap of pubmed Ids and pagerank(authority) values for this
	 * 								keyword
	 */
	public void updateGraphWithAuthority(String keyword,
			Map<String, Double> contributedPubmedIds) {
		Node keywordNode = null;
		Node paperNode = null;
		Relationship contributed = null;
		double weight = 0;
		try (Transaction tx = graphDb.beginTx()) {
			LOGGER.log(Level.FINER,"Updating graph for keyword {0} contributions {2} " , new Object[]{keyword , contributedPubmedIds});
			keywordNode = createKeyWordNode(keyword);
			if (keywordNode == null) {
				throw new NoKeywordException();
			}
			for (String pubmedId : contributedPubmedIds.keySet()) {
				weight = contributedPubmedIds.get(pubmedId);
				if (weight > 0) {
					paperNode = createPaperNode(pubmedId);
					if (pubmedId == null) {
						throw new NoPubmedIdException();
					}
					contributed = keywordNode.createRelationshipTo(paperNode,
							RelTypes.CONTRIBUTED);
					contributed.setProperty(Constants.PROPERTY_WEIGHT, weight);
				}
			}
			LOGGER.log(Level.FINER,"Updating Successfull " );
			tx.success();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Registers a shutdown hook for the Neo4j instance so that it shuts down
	 * nicely when the VM exits (even if you "Ctrl-C" the running application).
	 * 
	 * @param graphDb - graphdatabase service object
	 */
	private static void registerShutdownHook(final GraphDatabaseService graphDb) {
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

}
