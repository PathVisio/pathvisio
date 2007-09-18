<?php

require_once("Maintenance.php");

$exts = array('.JPG', '.jpg'); //in case SQL server is case sensitive

$dbr =& wfGetDB(DB_SLAVE);
	$res = $dbr->select( "page", array("page_title"), array("page_namespace" => NS_PATHWAY));
	$np = $dbr->numRows( $res );
	while( $row = $dbr->fetchRow( $res )) {
		echo "Updating $row[0]<br>";
		$pathway = Pathway::newFromTitle($row[0]);
		try {
			$pathway->updateSVG();
		} catch(Exception $e) {
			echo "\tERROR: unable to update: $e<br>";
		}
	}
?>
