<?php

$dir = getcwd();
chdir("../../");
#require_once( '.StartProfiler.php' );
require_once ( 'includes/WebStart.php');
require_once( 'includes/Wiki.php' );
chdir($dir);

$exts = array('.JPG', '.jpg'); //in case SQL server is case sensitive


foreach($exts as $ext) {
	deleteByExtension($ext);
}

function deleteByExtension($ext) {
	$dbr =& wfGetDB(DB_SLAVE);
	$res = $dbr->select( "page", array("page_namespace, page_title"), "page_title LIKE '%$ext'" );
	$np = $dbr->numRows( $res );
	echo("Number of pages found for $ext: $nr<BR>");

	while( $row = $dbr->fetchRow( $res )) {
		$ns = $row[0];
		$pt = $row[1];
		$title = Title::makeTitle($ns, $pt);

		if($ns == NS_IMAGE) {
			$img = new Image($title);
			$status = $img->delete("removed jpgs");
		}
		$art = new Article($title);
		$status = $art->doDeleteArticle("removed jpgs");

		echo("Removing image $pt: $status<BR>");
	}
	$dbr->freeResult( $res );
}

$wgLoadBalancer->commitAll();
?>
