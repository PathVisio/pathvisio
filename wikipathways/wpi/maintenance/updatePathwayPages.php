<?php

$dir = getcwd();
chdir("../"); //Ugly, but we need to change to the MediaWiki install dir to include these files, otherwise we'll get an error
require_once('wpi.php');
chdir($dir);

set_time_limit(0);

$dbr =& wfGetDB(DB_SLAVE);
$res = $dbr->select( "page", array("page_title"), array("page_namespace" => NS_PATHWAY));
$np = $dbr->numRows( $res );
echo 'nrow: ' . $np . '<br>';
$i = 0;
while( $row = $dbr->fetchRow( $res )) {
	// TESTER
	//if(!ereg("Human:Sandbox", $row[0])) continue;
	//if($i++ > 3) exit;
	// END TESTER
	
	echo "Updating $row[0]<br>";
	$pathway = Pathway::newFromTitle($row[0]);
	$title = $pathway->getTitleObject();
	$revision = Revision::newFromTitle($title);
	$article = new Article($title);
	$text = $revision->getText();
	
	$regex = "/\[\[Category:(.+?)\|\{\{.+?\}\}\]\]/";
	
	$imagePage = $pathway->getFileTitle(FILETYPE_IMG)->getFullText();
	$gpmlPage = $pathway->getFileTitle(FILETYPE_GPML)->getFullText();
	
	echo '<I>' . $text . '</I><BR>';
	$text = preg_replace($regex, "[[Category:\\1]]", $text);

	if(!$_GET['doit']) {
		echo $text . '<BR>';
	} else {
		if($article->doEdit($text, 'Updated template', EDIT_UPDATE | EDIT_FORCE_BOT)) {
			echo "Updated to:<br>$text<br>";
		} else {
			echo "UPDATE FAILED";
		}
		$wgLoadBalancer->commitAll();
	}
}
?>
