<?php

require_once("wpi/wpi.php");

/*
Statistics for main page
- how many pathways	{{PAGESINNS:NS_PATHWAY}}
- how many organisms
- how many pathways per organism
*/

#### DEFINE EXTENSION
# Define a setup function
$wgExtensionFunctions[] = 'wfSiteStats';
# Add a hook to initialise the magic word
$wgHooks['LanguageGetMagic'][]  = 'wfSiteStats_Magic';

function wfSiteStats() {
        global $wgParser;
        # Set a function hook associating the "example" magic word with our function
        $wgParser->setFunctionHook( 'siteStats', 'getSiteStats' );
}

function wfSiteStats_Magic( &$magicWords, $langCode ) {
        # Add the magic word
        # The first array element is case sensitive, in this case it is not case sensitive
        # All remaining elements are synonyms for our parser function
        $magicWords['siteStats'] = array( 0, 'siteStats' );
        # unless we return true, other parser functions extensions won't get loaded.
        return true;
}

function getSiteStats( &$parser, $tableAttr ) {
	$output = "* There are '''{{PAGESINNS:" . NS_PATHWAY . "}}''' pathways";
	$table = <<<EOD

* Number of pathways per species:
{| align="center" $tableAttr
EOD;
	foreach(Pathway::getAvailableSpecies() as $species) {
		$nr = howManyPathways($species);
		$table .= <<<EOD

|-align="left"
|$species:
|'''$nr'''
EOD;
	}
	$table .= "\n|}";
	$output .= $table;
	$output .= "\n* There are '''{{NUMBEROFUSERS}}''' registered users";
	return $output;
}

function howManyPathways($species) {
	$dbr =& wfGetDB(DB_SLAVE);
	//Fetch number of pathways for this species
	$res = $dbr->query("SELECT COUNT(*) FROM page WHERE page_namespace=" . NS_PATHWAY . " AND page_title LIKE '$species%' AND page_is_redirect = 0");
	$row = $dbr->fetchRow($res);
	$dbr->freeResult($res);
	return $row[0];
}

function getSpecies() {
	return Pathway::getAvailableSpecies();
}

?>
