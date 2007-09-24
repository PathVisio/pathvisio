<?php

require_once("Maintenance.php");

## Reset the MediaWiki categorylinks records for each pathway to contain only the GPML attributes
## And remove categories on images

$dbr =& wfGetDB(DB_SLAVE);
$res = $dbr->select( "page", array("page_title"), array("page_namespace" => NS_PATHWAY));
$np = $dbr->numRows( $res );
echo 'nrow: ' . $np . '<br>';
$i = 0;
while( $row = $dbr->fetchRow( $res )) {
	try {
		$pathway = Pathway::newFromTitle($row[0]);
		echo("PROCESSING: {$pathway->getTitleObject()->getFullText()}<BR>\n");
				
		if($doit) {
			//Remove categories for image file
			$dbw =& wfGetDB( DB_MASTER );
			$dbw->immediateBegin();
			$clFrom = $pathway->getFileTitle(FILETYPE_IMG)->getArticleID();
			$dbw->delete( 'categorylinks', array( 'cl_from' => $clFrom ) );
			$dbw->immediateCommit();
		
			//Set pathway categories
			$pathway->getCategoryHandler()->setGpmlCategories();
		}
	} catch(Exception $e) {
		echo "ERROR: $e<BR>\n";
		continue;
	}
}
?>
