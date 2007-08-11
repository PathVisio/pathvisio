<?php
require_once('wpi/wpi.php');

define('SEPARATOR', ',');
define('PAR_PATHWAYS', 'pathways');

$wgExtensionFunctions[] = 'wfListPathways';

function wfListPathways() {
        global $wgParser;
        $wgParser->setHook('listPathways', 'createPathwayList');
}

function createPathwayList($input, $argv, &$parser) {
	$parser->disableCache();

	//Try to get the input parameters from $input and $argv
	if($input) $pwString = $input . SEPARATOR;


	//Try to get the input from the url request
	if($_GET[PAR_PATHWAYS]) $pwString .= $_GET[PAR_PATHWAYS];
	
	//Parse the pathway array
	$pathways = explode(SEPARATOR, $pwString);

	$pwTagList = array();
	foreach($pathways as $pw) {
		try {
			$pwObject = Pathway::newFromTitle($pw);
			$link = tag('a', $pwObject->name() . " (" . $pwObject->species() . ")", array('href' => $pwObject->getFullUrl()));
			array_push($pwTagList, $link);
		} catch(Exception $e) {
			//Ignore..
		}
	}
	
	//Create HTML
	foreach($pwTagList as $pwTag) {
		$html .= tag('li', $pwTag);
	}
	$html = tag('ul', $html);
	return $html;
}
?>
