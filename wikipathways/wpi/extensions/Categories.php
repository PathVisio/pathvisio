<?php

require_once("wpi/wpi.php");

/*
List categories for this page
*/

#### DEFINE EXTENSION
# Define a setup function
$wgExtensionFunctions[] = 'wfCategories';
# Add a hook to initialise the magic word
$wgHooks['LanguageGetMagic'][]  = 'wfCategories_Magic';

function wfCategories() {
        global $wgParser;
        # Set a function hook associating the "example" magic word with our function
        $wgParser->setFunctionHook( 'categories', 'getCategories' );
}

function wfCategories_Magic( &$magicWords, $langCode ) {
        # Add the magic word
        # The first array element is case sensitive, in this case it is not case sensitive
        # All remaining elements are synonyms for our parser function
        $magicWords['categories'] = array( 0, 'categories' );
        # unless we return true, other parser functions extensions won't get loaded.
        return true;
}

function getCategories( &$parser ) {
	$title = $parser->mTitle;
	$id = $title->getArticleID();
	$dbr = wfGetDB( DB_SLAVE );
	$res = $dbr->select( "categorylinks", array("cl_to"), array("cl_from" => $id));
	while( $row = $dbr->fetchRow( $res )) {
		$cat = Title::newFromText($row[0], NS_CATEGORY);
		$name = $cat->getText();
		$link = $cat->getFullText();
		$catlist .= "* [[:$link|$name]]\n";
	}
	$dbr->freeResult($res);
	return $catlist;
}

?>
