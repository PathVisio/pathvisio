<?php

require_once("Maintenance.php");

//Init
$cats = readCategories("maintenance/pathways.txt");

$dbr =& wfGetDB(DB_SLAVE);
$res = $dbr->select( "page", array("page_title"), array("page_namespace" => NS_PATHWAY));
$np = $dbr->numRows( $res );
while( $row = $dbr->fetchRow( $res )) {
	echo "Resetting $row[0]<br>";
	$pathway = Pathway::newFromTitle($row[0]);
	$title = $pathway->getTitleObject();
	$revision = Revision::newFromTitle($title);
	$article = new Article($title);

	$imagePage = $pathway->getFileTitle(FILETYPE_IMG)->getFullText();
	$gpmlPage = $pathway->getFileTitle(FILETYPE_GPML)->getFullText();
	$categories = findCategories($pathway, $cats);
	
	$text = "{{subst:Template:NewPathwayPage|pathwayPage={{FULLPAGENAMEE}}|categories=$categories}}";
	
	if($article->doEdit($text, 'reset', EDIT_UPDATE | EDIT_FORCE_BOT)) {
		echo "Reset to: $text<br>";
	} else {
		echo "UPDATE FAILED";
	}	$wgLoadBalancer->commitAll();

	flush();
	ob_flush();
	
}

function findCategories($pathway, $categories) {	//Add species category
	$species = $pathway->species();
	$cattext = "[[Category:$species|{{PAGENAME}}]]";

	//Find categories from array
	$custcat = $categories[$pathway->getFilePrefix()];
	if($custcat) {
		$cattext .= "$custcat\n";
	}
	return $cattext;
}

function readCategories($file) {
	$lines = file($file);
	$categories = array();
	foreach($lines as $line) {
		$line = trim($line);
		if($line{0} == '@') {
			$curcat = substr($line, 1);
			echo "Found category: $curcat<br>";
		} elseif ($line{0} == '>') {
			$categories[substr($line, 1)] = $curcat;
			echo "Added pathway: $line<br>";
		}
	}
	return $categories;
}
?>uire_once('wpi.php');
chdir($dir);

set_time_limit(0);
