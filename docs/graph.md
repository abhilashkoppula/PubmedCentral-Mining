Pubmed Graphs
==================
Two types of graphs are created using Neo4j

+ Relevant Graph
+ Hetrogeneous Graph

Relevant Garph
-----------------
 * Graph with only paper nodes and is used to calculate Page Rank . 
 * Graph is updated for each article parsed . 
 * Each node in the graph represents either a pubmed article or cited pubmed article
 * Each node has three properties 
    1. PubmedId
    2. Array of keywords
    3. Initial Prior - 1/number of keywords used in paper
 * Only one relationship type **CITED** between two paper nodes . Each edge have an edge weight equal to *(no of times cited)/(total number of citaions)*
 
Hetrogeneous Graph
-------------------
 * Graph with both paper nodes and keyword nodes and is used to find the paths using random walks 
 * Graph is updated in two stages . i) Parsing the whole dataset and ii) After calculating page rank
 * Paper nodes represents pubmed article and cited pubmed article. Each paper node has property pubmedId
 * Keyword nodes represents actual keywords used in the article . Each keyword node has property keywordText
 * Three type of relations exists in the graph
    + **CITED** : From paper node to cited paper nodes and have an edge weight similar to above graph
    + **RELAVANT** : From paper node to keyword nodes used in the paper . Each edge have edge weight equal to 1/number of keywords
    +  **CONTRIBUTED** : From keyword node to paper node based on the page rank/authority scores . Each edge have an edge weight equal to page rank score


##### Source Code
    edu.iub.pubmed.graph.RelevantGraph
    edu.iub.pubmed.graph.HetrogeneousGraph
