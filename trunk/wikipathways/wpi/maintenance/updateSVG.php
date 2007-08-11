<?php

$dir = getcwd();
chdir("../"); //Ugly, but we need to change to the MediaWiki install dir to include these files, otherwise we'll get an error
require_once('wpi.php');
chdir($dir);

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