<?php

require_once("Maintenance.php");

$dbr =& wfGetDB(DB_SLAVE);
$res = $dbr->select("page", array("page_title"), array("page_namespace"=> NS_PATHWAY));
while($row = $dbr->fetchRow($res)) {
	try {
		$pathway = Pathway::newFromTitle($row[0]);
		$pathway->updateCache();
	} catch(Exception $e) {
		echo "Exception: {$e->getMessage()}<BR>\n";
	}
}

?>
