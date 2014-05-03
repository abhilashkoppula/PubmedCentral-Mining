package edu.iub.pubmed.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.WrappingNeoServer;
import org.neo4j.server.WrappingNeoServerBootstrapper;

/**
 * Wraps the emebeded graph db to make them available via URL
 * http://localhost:7474/webadmin/#
 * <em> <br>
 * 			<b> Usage : </b> javac PubmedServer.java <br>
 *       	  java PumbedServer <GraphIdentifier> <br>
 *   		0-for Relevance Graph and 1 for Hetrogeneous Graph <br>
 *  </em> 
 * @author Abhilash
 *
 */
public class PubmedServer {
	WrappingNeoServer neoServer = null;
	GraphDatabaseService graphDb = null;

	/**
	 * Creates graphdb for relevance graph
	 */
	public void startRelevanceGraphSever() {
		graphDb = new RelevanceGraph().getGraph();
		startServer();
	}

	/**
	 * Starts the pubmed server
	 */
	private void startServer() {
		neoServer = new WrappingNeoServer((GraphDatabaseAPI) graphDb);
		neoServer.start();
	}

	/**
	 * Creates graphdb for Hetrogeneous graph
	 */
	public void startHetrogeneousGraphSever() {
		graphDb = new RelevanceGraph().getGraph();
		startServer();
	}

	public static void main(String[] args) {
		PubmedServer pubmedGraphServer = new PubmedServer();
		if (args[0] == "0") {
			pubmedGraphServer.startRelevanceGraphSever();
		} else {
			pubmedGraphServer.startHetrogeneousGraphSever();
		}

	}
}
