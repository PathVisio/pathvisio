<?php

$dir = getcwd();
chdir("../"); //Ugly, but we need to change to the MediaWiki install dir to include these files, otherwise we'll get an error
require_once('wpi.php');
chdir($dir);

/*****

V Create GPML namespace

* Maintainance script to transfer all pathway file contents to GPML pages
* How to transfer history?
  * Iterate over all file history and subsequentiall edit
  * Skip bot edits (except if it's the current)
* Update braces (manually make list of pathways to skip)

****/

//Iterate over all GPML 'Image' files
set_time_limit(0);

$dbr =& wfGetDB(DB_SLAVE);

$res = $dbr->query("SELECT img_name, img_description FROM image WHERE img_name LIKE '%.gpml'");
while( $row = $dbr->fetchRow( $res )) {
	$img = $row[0];
	$img_description = $row[1];

	/* TEST */	
	//if($img != "Hs_IL-5_NetPath_17.gpml") continue;
	/* TEST */

	echo "Processing: $img<BR>";

	//Test if pathway exists for this file
	try {
		$pathway = Pathway::newFromFileTitle($img);
		$title = $pathway->getTitleObject();
		$article = new Article($title);
		if( !$article->exists()) throw new Exception("Description page does not exist");
	} catch(Exception $e) {
		echo "Not a pathway: $e";
		continue;
	}
	
	//Create the new article for the GPML data
	$title = Title::newFromText($pathway->getTitleObject()->getText(), NS_GPML);
	$article = new Article($title);
	
	//Iterate over file history
	$res_hist = $dbr->query("SELECT oi_name, oi_archive_name, oi_description FROM oldimage WHERE oi_name = '$img'");
	while( $histrow = $dbr->fetchRow( $res_hist ) ) {
		$oi_archive_name = $histrow[1];
		$oi_description = $histrow[2];

		//Exclude 'gpml file for [[%'
		//Exclude 'test'
		if(	ereg("gpml file for \[\[.*", $oi_description) ||
			ereg("test", $oi_description) ) {
			continue;
		}

		echo "->$oi_archive_name; $oi_description<BR>";
		
		echo "->-> transfer to {$title->getFullURL()}<BR>";
		try {
			transferGpmlFile($article, $img, $oi_archive_name, $oi_description, true);
		} catch (Exception $e) {
			echo "Exception: $e\n";
		}	
	}
	//Transfer the current version
	transferGpmlFile($article, $img, $img, $img_description, false);
	cleanup($img);
}

function transferGpmlFile($toArticle, $img, $fileName, $description, $archive = false) {
	//Get the GPML file contents
	if($archive) {
		$fileName = wfImageArchiveDir($img) . "/$fileName";
	} else {
		$fileName = wfImageDir($img) . "/$fileName";
	}
	$gpml = file_get_contents($fileName);
	
	if(!$gpml) throw new Exception("Unable to read file contents for $fileName");

	//Update article
	$succ =  $toArticle->doEdit($gpml, $description);
	if(!$succ) throw new Exception("Unable to create new GPML page for $fileName");
}

//Do this after succesful transfer
function cleanup($img) {
	$title = Title::newFromText($img, NS_IMAGE);
	$img = new Image($title);
	$img->delete("cleanup");
	Pathway::deleteArticle($title, "cleanup");
}
