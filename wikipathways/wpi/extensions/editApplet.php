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
 * @paramater type, type of the applet to start (editor, bibliography, ...)
 * @parameter $idClick Id of the element to attach an 'onclick' event 
 * to that will trigger the applet to start. If this argument equals 'direct', 
 * the applet will be activated directly.
 * @parameter $idReplace Id of the element that will be replaced by the applet
 * @parameter $new Whether the pathway is yet to be created (will be passed on to the applet)
 * @parameter $pwTitle The title of the pathway to be edited (Species:Pathwayname)
*/
function createApplet( &$parser, $idClick = 'direct', $idReplace = 'pwThumb', $new = false, $pwTitle = '', $type = 'editor', $width = 0, $height = '500px' ) {
	global $wgUser;
	
	//Check user rights
	if( !$wgUser->isLoggedIn()) {
		return ""; //Don't return any applet code
	}
	
	$parser->disableCache();
	
	$param = array(); //Extra parameters
	$main = 'org.pathvisio.gui.wikipathways.';
	switch($type) {
		case 'bibliography': 
		$main .= 'BibliographyApplet';
		break;
		case 'description':
		$main .= 'DescriptionApplet';
		break;
		case 'categories':
		$main .= 'CategoryApplet';
		$cats = implode(',', Pathway::getAvailableCategories());
		$param = array('categories' => $cats);
		break;
		default: $main .= 'AppletMain';
	}
	
	try {
		if(!$pwTitle) {
			$pathway = Pathway::newFromTitle($parser->mTitle);
		} else {
			$pathway = Pathway::newFromTitle($pwTitle);
		}
		$editApplet = new EditApplet($pathway, $main, $idReplace, $idClick, $new, $width, $height, $param);
		$appletCode = $editApplet->makeAppletFunctionCall();
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


class EditApplet {
	private $pathway;
	private $mainClass;
	private $idReplace;
	private $idClick;
	private $isNew;
	private $width, $height;
	private $param;

	function __construct($pathway, $mainClass, $idReplace, $idClick, $isNew, $width, $height, $param = array()) {
		$this->pathway = $pathway;
		$this->mainClass = $mainClass;
		$this->idReplace = $idReplace;
		$this->idClick = $idClick;
		$this->isNew = $isNew;
		$this->width = $width;
		$this->height = $height;
		$this->param = $param;
	}

	function makeAppletObjectCall() {
		global $wgUser, $wgScriptPath;
		if($this->isNew) {
			$pwUrl = $this->pathway->getTitleObject()->getFullURL();
		} else {
			$pwUrl = $this->pathway->getFileURL(FILETYPE_GPML);
		}

		//Read cache jars and update version
		$jardir = WPI_SCRIPT_PATH . '/applet';
		$cache_archive = explode(' ', file_get_contents("$jardir/cache_archive"));
		$version_file = explode("\n", file_get_contents("$jardir/cache_version"));
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
			'rpcUrl' => WPI_URL . "/wpi_rpc.php",
			'pwName' =>     $this->pathway->name(),
			'pwSpecies' => $this->pathway->species(),
			'pwUrl' => $pwUrl,
			'cache_archive' => $archive_string,
			'cache_version' => $version_string
		);

		if($wgUser && $wgUser->isLoggedIn()) {
			$args = array_merge($args, array('user' => $wgUser->getRealName()));
		}
		if($this->isNew) {
			$args = array_merge($args, array('new' => true));
		}
		$args = array_merge($args, $this->param);
		$keys = createJsArray(array_keys($args));
		$values = createJsArray(array_values($args));

		return "doApplet('{$this->idReplace}', 'applet', '$wgScriptPath/wpi/applet', '{$this->mainClass}', '{$this->width}', '{$this->height}', {$keys}, {$values});";
	}

	function makeAppletFunctionCall() {
		$function = $this->makeAppletObjectCall();
		if($this->idClick == 'direct') {
			return scriptTag($function);
		} else {
			return scriptTag(
				"var elm = document.getElementById('{$this->idClick}');" . 
				"var listener = function() { $function };" .
				"if(elm.attachEvent) { elm.attachEvent('onclick',listener); }" .
				"else { elm.addEventListener('click',listener, false); }"
			);
		}
	}
}
?>
