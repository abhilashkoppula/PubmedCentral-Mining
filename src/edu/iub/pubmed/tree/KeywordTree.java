package edu.iub.pubmed.tree;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexManager;
import org.neo4j.graphdb.index.ReadableIndex;

import edu.iub.pubmed.utils.Constants;



/**
 * Sample test class for Tree Construction
 * @author Abhilash(akoppula@indiana.edu)
 *
 */

public class KeywordTree {
	
   GraphDatabaseService hetroGraph = null;   
   ReadableIndex<Node> autoNodeIndex = null;
   
   
   
   private static enum RelTypes implements RelationshipType {
		CITED, CONTRIBUTED, RELEVANT
	}
   
   /**
    * Finds all paths for given keyword as follows
    * 
    *  keyword1 -> Paper -> Paper -> Keyword
    * 
    * @param keyword
    * @return
    */
	public List<TreePath> getPaths(String keyword) {
		List<TreePath> treePaths = null;
		TreePath treePath = null;
		Node rootKeyNode = null;

		try (Transaction transaction = hetroGraph.beginTx()) {
			treePaths = new ArrayList<>();
			rootKeyNode = autoNodeIndex
					.get(Constants.PROPERTY_KEYWORD, keyword).getSingle();
			Node paperNode = null;
			Node citedNode = null;
			Node endKeywordNode = null;
			
			Iterable<Relationship> relevantEdges = rootKeyNode
					.getRelationships(Direction.INCOMING);
			for (Relationship rel : relevantEdges) {
				paperNode = rel.getStartNode();
				
				Iterable<Relationship> citedEdges = paperNode.getRelationships(
						Direction.OUTGOING, RelTypes.CITED);
				for (Relationship cite : citedEdges) {
					citedNode = cite.getEndNode();
					
					Iterable<Relationship> relevantOutEdges = citedNode
							.getRelationships(Direction.OUTGOING,RelTypes.RELEVANT);
					for (Relationship relOut : relevantOutEdges) {
						endKeywordNode = rel.getStartNode();
						treePath = new TreePath(3);
						treePath.addNode(keyword);
						treePath.addNode((String) paperNode.getProperty(Constants.PROPERTY_PUBMED_ID));
						treePath.addEdge((double) rel.getProperty(Constants.PROPERTY_WEIGHT));
						treePath.addNode((String) citedNode.getProperty(Constants.PROPERTY_PUBMED_ID));
						treePath.addEdge((double) cite.getProperty(Constants.PROPERTY_WEIGHT));
						treePath.addNode((String) endKeywordNode.getProperty(Constants.PROPERTY_KEYWORD));
						treePath.addEdge((double) relOut.getProperty(Constants.PROPERTY_WEIGHT));
				}
			}

		}
		}
		return treePaths;
	}
	   
	   
	   
	   
	   
	   
	  
   

}
