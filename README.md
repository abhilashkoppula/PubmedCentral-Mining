PubmedCentral-Mining
====================

More information regarding process flow and different components can be found in docs folder . 

Compilation 
-------------------

Import into eclipse and add following dependencies/external jars

+ Neo4j         - [Neo4j/lib][Neo4j-1]
+ Neo4j Server  - [Neo4j/system][Neo4j-2]
+ PageRank      - [Jung Framework][Jung]
+ Neo4j_PageRank Interface - [BluePrints][Blueprints]


Running
-------------------

#### Parsing Data Set
     edu.iub.pubmed.PubmedCental.java
     java PubmedCentral <DataSet Path> <Directory for Dump Files>

#### Running Page Rank
     edu.iub.pubmed.pagerank.PageRank.java
     java PageRank  <File with KeyWords>

#### Neo4j Server (localhost:7474/webadmin/)
     edu.iub.pubmed.graph.PubmedServer.java
     java PubmedServer <Graph Type>(0 - Relevant Graph & 1 - HetrogeneousGraph)
     


[Neo4j-1]:http://docs.neo4j.org/chunked/stable/tutorials-java-embedded-setup.html
[Neo4j-2]:http://fooo.fr/~vjeux/github/github-recommandation/db/doc/manual/html/server-embedded.html
[Jung]:http://jung.sourceforge.net/download.html
[Blueprints]:https://github.com/tinkerpop/blueprints/wiki
