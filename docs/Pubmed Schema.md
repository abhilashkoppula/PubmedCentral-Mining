Pubmed Schema
----------------------
Detailed information regarding the XML schema of the pubmed files is at [nlm.nih.gov][schema]

#### High level overview

     <article>
      <front></front>[Metadata of Article like abstract,keywords,categories]
      <body></body>[Actual text content]
      <back></back>[Citations]
     <article>

### Elements and Paths

Below table will help in understanding how the required fields are extracted from the XML file.

**Note** : XPath is not used in actual parsing. Used here only for better understanding.

<table>
<tr>
<th>Element/DB field</th>
<th>Xpath</th>
<th>Remarks</th>
</tr>
<tr>
<td>PubmedId</td>
<td>/article/front/article-meta/article-id[@pub-id-type='pmid']/text()</td>
<td>If PubmedId is not present , article will be discarded</td>
</tr>
<tr>
<td>ArticleTitle</td>
<td>/article/front/article-meta/title-group/article-tile/text()</td>
<td></td>
</tr>
<tr>
<td>Abstract</td>
<td>/article/front/article-meta/abstract/text()</td>
<td></td>
<tr>
<td>PubDate</td>
<td>/article/front/article-meta/pub-date/</td>
<td>Child nodes year,month and date</td>
</tr>
<tr>
<td>Author</td>
<td>/article/front/article-meta/contrib-group/contrib/author</td>
<td>Multiple contrib elements implies multiple authors</td>
</tr>
<tr>
<td>Categories</td>
<td>/article/front/article-meta/article-categories/subj-group/subject</td>
<td>Multiple subj groups and subjects possible </td>
</tr>
<tr>
<td>Keywords</td>
<td>/article/front/article-meta/kwd-group[@type='author']/kwd/text()</td>
<td>Multiple kwd elements implies multiple keywords</td>
</tr>
<tr>
<td>Conference</td>
<td>/article/front/article-meta/conference/childNodes</td>
<td>Each childNodes contains details like name,number ,loc etc</td>
</tr>
<tr>
<td>Keywords</td>
<td>
/article/front/journal-meta/journal-id<br>
/article/front/journal-meta/journal-title<br>
/article/front/article-meta/volume/text()<br>
/article/front/article-meta/issue/text()
</td>
<td> Combination of article-meta and journal-meta elements </td>
</tr>
<tr>
<td>Conference</td>
<td>/article/front/article-meta/conference/childNodes</td>
<td>Each childNodes contains details like name,number ,loc etc</td>
</tr>

</table>


[schema]:http://dtd.nlm.nih.gov/archiving/tag-library/3.0/
