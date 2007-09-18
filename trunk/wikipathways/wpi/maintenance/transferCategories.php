<?php	

## Transfers all categories to GPML

require_once("Maintenance.php");

##Get the database
$dbr =& wfGetDB(DB_SLAVE);

//List all pages in a category that are in the Pathway namespace
//$res = $dbr->select( "categorylinks", array("page_id"), array("page_namespace" => NS_PATHWAY));

$res = $dbr->query("SELECT cl_from, cl_to, cl_sortkey FROM categorylinks");

$np = $dbr->numRows( $res );
$i = 0;
while( $row = $dbr->fetchRow( $res )) {
	$title = Title::newFromID($row[0]);
	echo("PROCESSING: " . $title->getFullText() . "<BR>\n");

	if($title->getNamespace() != NS_PATHWAY) {
		echo("SKIPPED {$title->getFullText()}, wrong namespace<BR>\n");
		continue;
	}
	$category = $row[1];
	$sort = $row[2];
	
	//Skip species categories
	if($category == 'Rat' || $category == 'Mouse' || $category == 'Human') {
		echo("SKIPPED, organism category<BR>\n");
		continue;
	}
			
	//Transfer categories to GPML
	transferToGpml($title, $category);
	
	//if(++$i > 2) break;
}

function transferToGpml($title, $category) {
	global $doit, $wgLoadBalancer;
	
	$pathway = Pathway::newFromTitle($title);
	$title = $pathway->getFileTitle(FILETYPE_GPML);
	$article = new Article($title);
	$rev = Revision::newFromTitle($title);
	$text = $rev->getText();
		
	$category = "<Comment Source=\"WikiPathways-category\">$category</Comment>";
	
	if(stripos($text, $category) !== false) {
		//The category is already present, nothing to do
		echo "SKIPPING: category already in GPML<BR>\n";
		return;
	}
	
	$text = preg_replace('/(<Pathway (?U).+)(<Graphics)/s',"$1$category\n$2", $text);
	$id = $rev->getId();
	//Write new text to database
	if($doit) {
		if($article->doEdit($text, 'Added categories to GPML', EDIT_UPDATE | EDIT_FORCE_BOT)) {
			echo "UPDATED<BR>\n";
		} else {
			echo "UPDATE FAILED<BR>\n";
		}
		$wgLoadBalancer->commitAll();
	}
}
?>
