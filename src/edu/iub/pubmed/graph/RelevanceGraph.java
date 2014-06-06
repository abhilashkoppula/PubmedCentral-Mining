package edu.iub.pubmed.graph;


import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.ReadableIndex;
import edu.iub.pubmed.utils.Constants;


/**
 * 
 *  Graph with nodes for Papers and with CITED relationship to other papers 
 *  Each node in the graph has three properties (PubmedId,Prior,Keywords) .
 *  This graph is used for calculating page rank scores .
 * 
 * @author Abhilash
 *
 */
public class RelevanceGraph {

	static final Logger LOGGER = Logger.getLogger(Constants.LOGGER_NAME);
	private GraphDatabaseService graphDb;
	private ReadableIndex<Node> autoNodeIndex = null;

	private static enum RelTypes implements RelationshipType {
		CITED
	}
	
	/**
	 * Creates a new graph from the NEO4J_RELEVANCE_GRAPH_PATH location and
	 * creates  index for pubmedId 
	 * 
	 */
	public RelevanceGraph() {
		LOGGER.log(Level.CONFIG , "Creating Relevance graph at directory " + Constants.NEO4J_RELEVANCE_GRAPH_PATH);
		LOGGER.log(Level.CONFIG , "Creating Index for Nodes on property pubmedId ");
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(Constants.NEO4J_RELEVANCE_GRAPH_PATH).
				setConfig(GraphDatabaseSettings.node_keys_indexable, "pubmedId").
				setConfig(GraphDatabaseSettings.node_auto_indexing, "true").newGraphDatabase();
		autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
	}

	
	/**
	 * Returns the graphdb object
	 * @return graphdatabaseservice object
	 */
	public GraphDatabaseService getGraph() {
		return this.graphDb;
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
		node = autoNodeIndex.get(Constants.PROPERTY_PUBMED_ID, keyValue).getSingle();
		if (node == null) {
			LOGGER.log(Level.FINER,"Node doesnt exist for pubmed Id {0}. Creating new node " , keyValue);
			node = graphDb.createNode();
			node.setProperty(Constants.PROPERTY_PUBMED_ID, keyValue); 
		}
		return node;
	}
	
	
	/**
	 *  Updates graph by creating a new node for the given pub med Id and new nodes if
	 *  required for the citedPubmeds . For every new paper node created , three properties
	 *  {@link Constants}.PROPERTY_PUBMED_ID,{@link Constants}.PROPERTY_KEYWORD, {@link Constants}.PRIOR
	 *  
	 * @param pubmedId - new pubmed id 
	 * @param citedPubmedIds - list of cited pubmed Ids
	 * @param keyWords - list of keywords
	 */
	void updateGraph(String pubmedId, Map<String,Double> citedPubmedIds,
			Set<String> keyWords) {
		Node node = null;
		Node citedNode = null;
		Relationship cited = null;
		try {
			Transaction tx = graphDb.beginTx();
			LOGGER.log(Level.FINER,"Updating graph for pubmedId {0} with keywords {1} and citations {2} " , new Object[]{pubmedId , keyWords,citedPubmedIds});
			node = createPaperNode(pubmedId);
			node.setProperty(Constants.PROPERTY_KEYWORDS,
					(keyWords.toArray(new String[keyWords.size()])));
			if (keyWords.size() >= 1) {
				node.setProperty(Constants.PROPERTY_PRIOR, new Double(1d / keyWords.size()));
			} else {
				node.setProperty(Constants.PROPERTY_PRIOR, Constants.DOUBLE_ZERO);
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
	
	
	/**
	 * Removes nodes in the graph which doesn't have any keyword property
	 */
	private void removeNodes(){
		for(Node node : graphDb.getAllNodes()){
			if(!node.hasProperty(Constants.PROPERTY_KEYWORDS)){
				node.delete();
			}
		}
		
	}

}
