<?php
$xml1 = simplexml_load_file('Hs_Fatty_Acid_Beta_Oxidation_1_BiGCaT.gpml');

foreach ($xml1->attributes() as $a => $b)
	print "$a => $b<br>";
foreach ($xml1->{"Graphics"}->attributes() as $a => $b)
	print "$a => $b<br>";
print "<HR>";
$xml2 = simplexml_load_file('Hs_Fatty_Acid_Beta_Oxidation_2_BiGCaT.gpml');

foreach ($xml2->attributes() as $a => $b)
	print "$a => $b<br>";
foreach ($xml2->{"Graphics"}->attributes() as $a => $b)
	print "$a => $b<br>";
print "<HR>";
$xml3 = simplexml_load_file('Hs_Fatty_Acid_Beta_Oxidation_3_BiGCaT.gpml');

foreach ($xml3->attributes() as $a => $b)
	print "$a => $b<br>";
foreach ($xml3->{"Graphics"}->attributes() as $a => $b)
	print "$a => $b<br>";
print "<HR>";

$xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><Pathway></Pathway>";
$complete = new SimpleXMLElement($xml);
$complete->addAttribute('Name', 'Fatty Acid Beta Oxidation complete');
$complete->addAttribute('Data-Source', $xml1['Data-Source']);
$datestamp = date("d/m/y");
$complete->addAttribute('Version', $xml1['Last-Modified']);
$complete->addAttribute('Author', $xml1['Author']);
$complete->addAttribute('Maintained-By', $xml1['Maintained-By']);
$complete->addAttribute('Email', $xml1['Email']);
$complete->addAttribute('Availability', $xml1['Availability']);
$complete->addAttribute('Last-Modified', $datestamp);
$complete->addChild('Graphics');

$maxWindowWidth = intval($xml1->{"Graphics"}['WindowWidth']);
$maxWindowHeight = intval($xml1->{"Graphics"}['WindowHeight']);
if ($maxWindowHeight  < intval($xml2->{"Graphics"}['WindowHeight'])) $maxWindowHeight  = intval($xml2->{"Graphics"}['WindowHeight']); 
if ($maxWindowHeight  < intval($xml3->{"Graphics"}['WindowHeight'])) $maxWindowHeight  = intval($xml3->{"Graphics"}['WindowHeight']); 
if ($maxWindowWidth  < intval($xml2->{"Graphics"}['WindowWidth'])) $maxWindowWidth = intval($xml2->{"Graphics"}['WindowWidth']); 
if ($maxWindowWidth  < intval($xml3->{"Graphics"}['WindowWidth'])) $maxWindowWidth  = intval($xml3->{"Graphics"}['WindowWidth']);  
$complete->{"Graphics"}->addAttribute('WindowWidth', $maxWindowWidth);
$complete->{"Graphics"}->addAttribute('WindowHeight', $maxWindowHeight);
$teller = 0;
foreach($xml2->GeneProduct as $geneproduct){
	$teller++;
	print "$teller<BR>";
        $current_gp = $complete->addChild('GeneProduct');
	foreach ($geneproduct->attributes() as $a => $b) $current_gp->addAttribute(strval($a), strval($b));
	$current_gp->addChild("Notes", $xml2->GeneProduct->Notes);
	foreach($geneproduct->Notes->attributes() as $a => $b) $current_gp->Notes->addAttribute($a, $b);
	$current_gp->addChild("Comment", $xml2->GeneProduct->Comment);
	foreach($geneproduct->Comment->attributes() as $a => $b) $current_gp->Comment->addAttribute($a, $b);
	$current_gp->addChild("Graphics", $xml2->GeneProduct->Graphics);
	foreach($xml2->Graphics->attributes() as $a => $b) $current_gp->Graphics->addAttribute($a, $b);
}
print $complete->asXML("test.xml");
	


?>
