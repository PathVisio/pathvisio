<?php
require_once('wpi/wpi.php');

$wgExtensionFunctions[] = 'wfEditApplet';
$wgHooks['LanguageGetMagic'][]  = 'wfEditApplet_Magic';

function wfEditApplet() {
	global $wgParser;
	$wgParser->setFunctionHook( "editApplet", "createApplet" );
}

function wfEditApplet_Magic( &$magicWords, $langCode ) {
	$magicWords['editApplet'] = array( 0, 'editApplet' );
	return true;
}

function createApplet( &$parser, $idClick = 'appletButton', $idReplace = 'pwThumb', $new = '', $pwTitle = '' ) {
	$parser->disableCache();
	try {
		if(!$pwTitle) {
			$pathway = Pathway::newFromTitle($parser->mTitle);
		} else {
			$pathway = Pathway::newFromTitle($pwTitle);
		}
		$appletCode = makeAppletFunctionCall($pathway, $idReplace, $idClick, $new);
		$output = scriptTag('', JS_SRC_APPLETOBJECT) . scriptTag('', JS_SRC_PROTOTYPE) . scriptTag('', JS_SRC_RESIZE) . scriptTag('', JS_SRC_EDITAPPLET) . $appletCode;
	} catch(Exception $e) {
		return "Error: $e";
	}

	return array($output, 'isHTML'=>1, 'noparse'=>1);
}

function scriptTag($code, $src = '') {
	$src = $src ? 'src="' . $src . '"' : '';
	return '<script type="text/javascript" ' . $src . '>' . $code . '</script>';
}

function createJsArray($array) {
	$jsa = "new Array(";      
	foreach($array as $elm) {
		$jsa .= "'{$elm}', ";
	}
	return substr($jsa, 0, strlen($jsa) - 2) . ')';
}

function makeAppletFunctionCall($pathway, $idReplace, $idClick, $new) {
	global $wgUser;
	if($new) {
		$pwUrl = $pathway->getTitleObject()->getFullURL();
	} else {
		$pwUrl = $pathway->getFileURL(FILETYPE_GPML);
	}

	$args = array(
		'rpcUrl' => "http://" . $_SERVER['HTTP_HOST'] . "/wpi/wpi_rpc.php",
		'pwName' =>     $pathway->name(),
		'pwSpecies' => $pathway->species(),
		'pwUrl' => $pwUrl
	);
	if($wgUser && $wgUser->isLoggedIn()) {
		$args = array_merge($args, array('user' => $wgUser->getRealName()));
	}
	if($new) {
		$args = array_merge($args, array('new' => true));
	}
	$keys = createJsArray(array_keys($args));
	$values = createJsArray(array_values($args));

	//$function = "replaceWithApplet('{$idReplace}', 'applet', {$keys}, {$values});";
	$function = "doApplet('{$idReplace}', 'applet', {$keys}, {$values});";
	return scriptTag(
		"var elm = document.getElementById('{$idClick}');" . 
		"var listener = function() { $function };" .
		"if(elm.attachEvent) { elm.attachEvent('onclick',listener); }" .
		"else { elm.addEventListener('click',listener, false); }"
	);
}
?>
