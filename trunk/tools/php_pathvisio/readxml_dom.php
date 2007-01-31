<?php
// Andra Waagmeester - Phasar
// This scripts merge three pathways into one merged pathway
// E-mail: a.waagmeester@micc.unimaas.nl

$doc1 = new DOMDocument();
$doc2 = new DOMDocument();
$doc3 = new DOMDocument();
$doc_complete = new DOMDocument();

//In this section all xml files are opened. Then the dimension are compared
//We need to have all the three files merged into on file. This implies that 
//The window heights need to be added and the biggest widths needs to be
//the final width

$doc_complete->load('Hs_Fatty_Acid_Beta_Oxidation_1_BiGCaT.gpml');//first file
$entry1 = $doc_complete->getElementsByTagName("Graphics");
$windowwidth = intval($entry1->item(0)->getAttribute("WindowWidth"));
$windowheight = intval($entry1->item(0)->getAttribute("WindowHeight"));
$height1 = $windowheight;
$boardwidth = intval($entry1->item(0)->getAttribute("BoardWidth"));
$boardheight = intval($entry1->item(0)->getAttribute("BoardHeight"));
	print  "WindowWidth:". $windowwidth."<BR>";
	print  "WindowHeight:". $windowheight."<BR>";
	print  "BoardWidth:".$boardwidth ."<BR>";
	print  "BoardHeight:".$boardheight ."<BR>";

$doc2->load('Hs_Fatty_Acid_Beta_Oxidation_2_BiGCaT.gpml');
$entry2 = $doc2->getElementsByTagName("Graphics");
print "<HR>";
if ($windowwidth < intval($entry2->item(0)->getAttribute("WindowWidth")))
	$windowwidth = intval($entry2->item(0)->getAttribute("WindowWidth"));
$windowheight = $windowheight + intval($entry2->item(0)->getAttribute("WindowHeight"));
$height2 = intval($entry2->item(0)->getAttribute("WindowHeight"));
if ($boardwidth < intval($entry2->item(0)->getAttribute("BoardWidth")))
	$boardwidth = intval($entry2->item(0)->getAttribute("BoardWidth")); 
$boardheight = $boardheight + intval($entry2->item(0)->getAttribute("BoardHeight"));
	print  "WindowWidth:". $windowwidth."<BR>";
	print  "WindowHeight:". $windowheight."<BR>";
	print  "BoardWidth:".$boardwidth ."<BR>";
	print  "BoardHeight:".$boardheight ."<BR>";
$doc3->load('Hs_Fatty_Acid_Beta_Oxidation_3_BiGCaT.gpml');
$entry3 = $doc3->getElementsByTagName("Graphics");
print "<HR>";
if ($windowwidth < intval($entry3->item(0)->getAttribute("WindowWidth")))
	$windowwidth = intval($entry3->item(0)->getAttribute("WindowWidth"));
$windowheight = $windowheight + intval($entry3->item(0)->getAttribute("WindowHeight"));
if ($boardwidth < intval($entry3->item(0)->getAttribute("BoardWidth")))
	$boardwidth = intval($entry3->item(0)->getAttribute("BoardWidth")); 
$boardheight = $boardheight + intval($entry3->item(0)->getAttribute("BoardHeight"));
	print  "WindowWidth:". $windowwidth."<BR>";
	print  "WindowHeight:". $windowheight."<BR>";
	print  "BoardWidth:".$boardwidth ."<BR>";
	print  "BoardHeight:".$boardheight ."<BR>";

$entry1->item(0)->setAttribute("WindowWidth", $windowwidth);
$entry1->item(0)->setAttribute("WindowHeight", $windowheight);
$entry1->item(0)->setAttribute("BoardWidth", $boardwidth);
$entry1->item(0)->setAttribute("BoardHeight", $boardheight);

//In the following section, the two final xml files are merged into the complete file
//To achieve this al GeneProduct nodes are merged.

$attachpoint = $doc_complete->getElementsByTagName("Pathway");
$attachnode = $attachpoint->item(0);
$entries = $doc2->getElementsByTagName("GeneProduct");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->setAttribute("CenterY", $oldvalue + $height1);
	$attachnode->appendChild($imported_node);
}

$entries = $doc2->getElementsByTagName("Line");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("y"));
	$imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->setAttribute("y", $oldvalue + $height1);
$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("y"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->setAttribute("y", $oldvalue + $height1);
	$attachnode->appendChild($imported_node);
}

$entries = $doc2->getElementsByTagName("Label");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->setAttribute("CenterY", $oldvalue + $height1);
	$attachnode->appendChild($imported_node);
}

$entries = $doc3->getElementsByTagName("GeneProduct");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->setAttribute("CenterY", $oldvalue + $height1 + $height2);
	$attachnode->appendChild($imported_node);
}

$entries = $doc3->getElementsByTagName("Line");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->getAttribute("y"));
	$imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(0)->setAttribute("y", $oldvalue + $height1 + $height2);
$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->getAttribute("y"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->getElementsByTagName("Point")->item(1)->setAttribute("y", $oldvalue + $height1 + $height2);
	$attachnode->appendChild($imported_node);
}

$entries = $doc3->getElementsByTagName("Label");
foreach ($entries as $entry){
	$imported_node = $doc_complete->importNode($entry, TRUE);
	$oldvalue = intval($imported_node->getElementsByTagName("Graphics")->item(0)->getAttribute("CenterY"));
        $imported_node->getElementsByTagName("Graphics")->item(0)->setAttribute("CenterY", $oldvalue + $height1 + $height2);
	$attachnode->appendChild($imported_node);
}

print $doc_complete->save('Hs_Fatty_Acid_Beta_Oxidation_all_BiGCaT.gpml');


?> 

