Process Flow
--------------
Each XML file in the dataset is parsed to extract the required fields to create insert queries and update the graph as shown below . The initial process of data parsing completes after all files are parsed completed

![alt tag](https://raw.githubusercontent.com/abhilashkoppula/PubmedCentral-Mining/master/docs/processflow.JPG)

### Parsing
+ edu.iub.pubmed.parser.ArticleParser has methods to extract required information from the file
+ Each XML file is parsed to extract following information 
<table>
<tr>
<th>Article</th>
<th>Conference</th>
<th>Volume</th>
<th>Author</th>
<th>Category</th>
<th>Keyword</th>
<th>Citations</th>
</tr>
<tr>
<td>
 + PubmedId
 + Article Title
 + Published Date
 + Absrtact Text
</td>
<td>
  + conf-name
  + conf-num
  + con-loc
  + conf-sponsor
  + conf-acronym
  + conf-theme
</td>
<td>
 + volume
 + issue
 + journal-id
 + journal-title
 + publisher-name
</td>
<td>
 + given-name
 + sur-name
</td>
<td>
 + subject
 + parent-category
</td>
<td>
 + keywordText
</td>
<td>
 + citedPubmedId
 + leftText
 + rightText
</td>
</tr>
</table>

### Creating Dump Files
    edu.iub.pubmed.dump.DumpFiles
    edu.iub.pubmed.dump.PubmedDump

+ As the dataset is huge , inserting the extracted data into database immediately after parsing will increase total time to process the whole dataset . So instead of inserting dynamically , the extracted data is stored in the form of SQL insert dump files . After processing whole data , these dump files are used to insert into database .

+ Seperate dump files are created for each table and  stored in the dumpdirectory which is given as input to the PubmedCentral class.

+ Each dump file is rolled over after reaching size of 5MB and is configured in the Constants file.

+ Each dump file have name in the format 'tablename_currentTime.sql'

+ Sample Dump File - [KeyWord Table Dump][DumpFile]

 [DumpFile]:https://github.com/abhilashkoppula/PubmedCentral-Mining/blob/master/docs/keyword_2014-4-11-02-36-651.sql


### Updating Graph
     edu.iub.pubmed.graph.GraphDelegator

+ After successfull parsing of a XML file , both [Relevant and Hetrogeneous graphs][graphs] are updated by passing pubmedId of the XML file , keywords used in the file and pubmed citaions .


[graphs]: https://github.com/abhilashkoppula/PubmedCentral-Mining/blob/master/docs/graph.md
