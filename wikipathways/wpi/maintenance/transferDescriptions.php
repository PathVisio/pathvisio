<?php

## Transfers all descriptions to GPML

require_once("Maintenance.php");

##Get the database
$dbr =& wfGetDB(DB_SLAVE);

##List all pages in the Pathway namespace
$res = $dbr->select( "page", array("page_id"), array("page_namespace" => NS_PATHWAY));

$i = 0;
$np = $dbr->numRows( $res );
while( $row = $dbr->fetchRow( $res )) {
	$title = Title::newFromID($row[0]);
	
	echo("PROCESSING: " . $title->getFullText() . "<BR>\n");

	if($title->getNamespace() != NS_PATHWAY) {
		echo("SKIPPED {$title->getFullText()}, wrong namespace<BR>\n");
		continue;
	}
	
	$rev = Revision::newFromTitle($title);
	$text = $rev->getText();
		
	##Find the description
	$title = $title->getText();
	
	//DescriptionStub}}<!-- PLEASE DO NOT MODIFY THIS LINE -->\n|DESCR|\n== Biblio
	if(ereg("DescriptionStub\}\}<!-- PLEASE DO NOT MODIFY THIS LINE -->\n(.+)\n== Biblio", $text, $regs)) {
		echo("DESCRIPTION: {$regs[1]}<BR>\n");
		transferToGpml($title, $regs[1]);
	} else {
		echo("NO DESCRIPTION<BR>\n");
	}
}

function transferToGpml($title, $description) {
	global $doit, $wgLoadBalancer;
	
	$pathway = Pathway::newFromTitle($title);
	$title = $pathway->getFileTitle(FILETYPE_GPML);
	$article = new Article($title);
	$rev = Revision::newFromTitle($title);
	$text = $rev->getText();
		
	$category = "<Comment Source=\"WikiPathways-description\">$description</Comment>";
	
	if(stripos($text, $category) !== false) {
		//The category is already present, nothing to do
		echo "SKIPPING: category already in GPML<BR>\n";
		return;
	}
	
	$text = preg_replace('/(<Pathway (?U).+)(<Graphics)/s',"$1$category\n$2", $text);
	$id = $rev->getId();
	//Write new text to database
	if($doit) {
		if($article->doEdit($text, 'Added description to GPML', EDIT_UPDATE | EDIT_FORCE_BOT)) {
			echo "UPDATED<BR>\n";
		} else {
			echo "UPDATE FAILED<BR>\n";
		}
		$wgLoadBalancer->commitAll();
	}
}
?>
