PageRank Algorithm
====================

The page rank algorithm is based on PageRankWithPriors from [JUNG Framework][JUNG] . 

##Process Flow 

![alt tag](https://raw.githubusercontent.com/abhilashkoppula/PubmedCentral-Mining/master/docs/PR.jpg)

+ PageRank algorithm is exectued on the Relevance Graph for each keyword
+ After execution , top 100 pagerank scores are selected and these nodes are updated in Hetrogeneous graph to add the CONTRIBUTED edge from the current keyword to these nodes.

       edu.iub.pubmed.pagerank.PageRank

###Running Page Rank
+ [PageRankWithPriors][PageRankPriors] class is used to run the algorithm
+ Relevance graph is created using NEO4j and PageRankWithPriors runs only on the graph created using JUNG framework . So to make the relevance graph compatible with JUNG framework , [blueprints][blueprints] framework which provides the interface to convert the neo4j graph to Jung framework .
+Relevance graph is created with edge weights(citation probability) and nodes with initial prior(1/numberOfKeywords).JUNG framework can access these(weights&priors) values through [Transformer][transformer] class . So Transformer class is implemented to return weights for edges and priors for nodes using Neo4j getProperty() method.

####Prior
+ Each node have an intial prior which is equal to 1/numberOfKeywords in this paper.
+ For PageRankWithPriors sum of all priors should be 1 . So to satisfy this before running pagerank , for each keywords priors are adjusted by normalizing them .And these new prior is returned when JUNG frameworks access through Transformer class.

###PseudoCode
        for each keyword {
          adjustPriors();
          createPageRankWithPriors();
          executePageRankWithPriors();
          updateHetrogeneousGraph();
          }




[JUNG]:http://jung.sourceforge.net/
[PageRankPriors]:http://jung.sourceforge.net/doc/api/edu/uci/ics/jung/algorithms/scoring/PageRankWithPriors.html
[blueprints]:https://github.com/tinkerpop/blueprints/wiki
[transformer]:http://commons.apache.org/proper/commons-collections/javadocs/api-3.2.1/org/apache/commons/collections/Transformer.html
