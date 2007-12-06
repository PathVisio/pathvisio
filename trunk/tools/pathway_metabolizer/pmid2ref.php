<link rel="stylesheet" type="text/css" href="tm_ebi.css">
<?php
print "<h1>References</h1>";
$pmid= "16140533";
$url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&id=$pmid&retmode=xml";
$xml = simplexml_load_file($url);
//title
$title = $xml->PubmedArticle->MedlineCitation->Article->ArticleTitle;
//authors
$i=1;
foreach ($xml->PubmedArticle->MedlineCitation->Article->AuthorList->Author as $author){
    $authors .= $author->LastName." ". $author->Initials;
    if ($i<count($author)) $authors .= ", ";
    $i++;
}
// Pages
$pages = $xml->PubmedArticle->MedlineCitation->Article->Pagination->MedlinePgn;

// Journal
$journal = $xml->PubmedArticle->MedlineCitation->Article->Journal->ISOAbbreviation;

//pubdate
$pubdate = $xml->PubmedArticle->MedlineCitation->Article->Journal->JournalIssue->PubDate->Month." ".$xml->PubmedArticle->MedlineCitation->Article->Journal->JournalIssue->PubDate->Year;

//Volume
$volume = $xml->PubmedArticle->MedlineCitation->Article->Journal->JournalIssue->Volume;

//Issue
$issue = $xml->PubmedArticle->MedlineCitation->Article->Journal->JournalIssue->Issue;

//PMID
$pmid = $xml->PubmedArticle->MedlineCitation->PMID;
print "<a href=\"http://view.ncbi.nlm.nih.gov/pubmed/$pmid\" target = \"_new\">pubmed</a> ";
print "<span class=\"authors\">$authors</span> ";
print "<span class=\"title\">$title</span>. ";
print "<span class=\"journal\">$journal</span>. ";
print "<span class=\"pages\">$pages</span>";
print "<span class=\"pubdate\">$pubdate</span>";
print "<span class=\"issue\">$issue</span>";



?>