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

/**
 * Creates the applet
 * @parameter $idClick Id of the element to attach an 'onclick' event 
 * to that will trigger the applet to start. If this argument equals 'direct', 
 * the applet will be activated directly.
 * @parameter $idReplace Id of the element that will be replaced by the applet
 * @parameter $new Whether the pathway is yet to be created (will be passed on to the applet)
 * @parameter $pwTitle The title of the pathway to be edited (Species:Pathwayname)
*/
function createApplet( &$parser, $idClick = 'direct', $idReplace = 'pwThumb', $new = '', $pwTitle = '' ) {
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

function increase_version($old) {
	//echo("increasing version: $old\n");
	$numbers = explode('.', $old);
	$last = hexdec($numbers[count($numbers) - 1]);
	$numbers[count($numbers) - 1] = dechex(++$last);
	//echo("increased to: " . implode('.', $numbers));
	return implode('.', $numbers);
}

function makeAppletObjectCall($pathway, $idReplace, $new) {
	global $wgUser;
	if($new) {
		$pwUrl = $pathway->getTitleObject()->getFullURL();
	} else {
		$pwUrl = $pathway->getFileURL(FILETYPE_GPML);
	}

	//Read cache jars and update version
	$jardir = '/var/www/wikipathways/wpi/applet';
	$cache_archive = explode(' ', file_get_contents("$jardir/cache_archive"));
	$version_file = explode("\n", file_get_contents('/var/www/wikipathways/wpi/applet/cache_version'));
	$cache_version = array();
	if($version_file) {
		foreach($version_file as $ver) {
			$jarver = explode("|", $ver);
			if($jarver && count($jarver) == 3) {
				$cache_version[$jarver[0]] = array('ver'=>$jarver[1], 'mod'=>$jarver[2]);
			}
		}
	}
	foreach($cache_archive as $jar) {
		$mod = filemtime("$jardir/$jar");
		if($ver = $cache_version[$jar]) {
			if($ver['mod'] < $mod) {
				$realversion = increase_version($ver['ver']);
			} else {
				$realversion = $ver['ver'];
			}
		} else {
			$realversion = '0.0.0.0';
		}
		$cache_version[$jar] = array('ver'=>$realversion, 'mod'=>$mod);
		$archive_string .= $jar . ', ';
		$version_string .= $realversion . ', ';
	}
	$version_string = substr($version_string, 0, -2);
	$archive_string = substr($archive_string, 0, -2);

	//Write new cache version file
	foreach(array_keys($cache_version) as $jar) {
		$out .= $jar . '|' . $cache_version[$jar]['ver'] . '|' . $cache_version[$jar]['mod'] . "\n";
	}
	writefile("$jardir/cache_version", $out);

	$args = array(
		'rpcUrl' => "http://" . $_SERVER['HTTP_HOST'] . "/wpi/wpi_rpc.php",
		'pwName' =>     $pathway->name(),
		'pwSpecies' => $pathway->species(),
		'pwUrl' => $pwUrl,
		'cache_archive' => $archive_string,
		'cache_version' => $version_string
	);

	if($wgUser && $wgUser->isLoggedIn()) {
		$args = array_merge($args, array('user' => $wgUser->getRealName()));
	}
	if($new) {
		$args = array_merge($args, array('new' => true));
	}
	$keys = createJsArray(array_keys($args));
	$values = createJsArray(array_values($args));

	return "doApplet('{$idReplace}', 'applet', {$keys}, {$values});";
}

function makeAppletFunctionCall($pathway, $idReplace, $idClick, $new) {
	$function = makeAppletObjectCall($pathway, $idReplace, $new);
	if($idClick == 'direct') {
		return scriptTag($function);
	} else {
		return scriptTag(
			"var elm = document.getElementById('{$idClick}');" . 
			"var listener = function() { $function };" .
			"if(elm.attachEvent) { elm.attachEvent('onclick',listener); }" .
			"else { elm.addEventListener('click',listener, false); }"
		);
	}
}
?>
