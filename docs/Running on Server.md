## Running on Server

+ Copy PMC.jar into directory where you have write permissions
+ Download Dependencies folder from IU Box


## To Parse
       java -cp <JARLOCATION>:<dependencies>/*: edu.iub.pubmed.PubmedCentral <Dataset Path Name>
       
       Eg :
       java -cp /usr/abhilash/pubmded/PMC.jar:/usr/abhilash/pubmed/Depenedencies/*: edu.iub.pubmed.PubmedCentral /usr/abhilash/Data
       
## Page Rank
      java -cp <JARLOCATION>:<dependencies>/*: edu.iub.pubmed.pagerank.PageRank <KeywordsFile>

       Eg :
        java -cp /usr/abhilash/pubmded/PMC.jar:/usr/abhilash/pubmed/Depenedencies/*: edu.iub.pubmed.pagerank.PageRank /usr/abhilash/keywords.txt
       
       
## Graphs

       java -cp <JARLOCATION>;<dependencies>/*: edu.iub.pubmed.graph.PubmedServer <Number>

       Eg :
        java -cp /usr/abhilash/pubmded/PMC.jar:/usr/abhilash/pubmed/Depenedencies/*: edu.iub.pubmed.graph.PubmedServer 0 
        java -cp /usr/abhilash/pubmded/PMC.jar:/usr/abhilash/pubmed/Depenedencies/*: edu.iub.pubmed.graph.PubmedServer 1
        
## Trees
