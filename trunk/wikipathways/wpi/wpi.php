<?php
require_once('globals.php');
//Initialize MediaWiki
set_include_path(get_include_path().PATH_SEPARATOR.realpath('../includes').PATH_SEPARATOR.realpath('../').PATH_SEPARATOR);
$dir = getcwd();
chdir("../"); //Ugly, but we need to change to the MediaWiki install dir to include these files, otherwise we'll get an error
require_once ( 'WebStart.php');
require_once( 'Wiki.php' );
chdir($dir);

//Parse HTTP request (only if script is directly called!)
if(realpath($_SERVER['SCRIPT_FILENAME']) == realpath(__FILE__)) {
$action = $_GET['action'];
switch($action) {
	case 'launchPathVisio':
		$pathway = Pathway::newFromTitle($_GET['pwTitle']);
		$ignore = $_GET['ignoreWarning'];
		launchPathVisio($pathway, $ignore);
		break;
	case 'downloadFile':
		downloadFile($_GET['type'], $_GET['pwTitle']);
		break;
	case 'revert':
		revert($_GET['pwTitle'], $_GET['oldId']);
		break;
	case 'new':
		$pathway = new Pathway($_GET['pwName'], $_GET['pwSpecies'], false);
		$ignore = $_GET['ignoreWarning'];
		launchPathVisio($pathway, $ignore, true);
		break;
	case 'delete':
		delete($_GET['pwTitle']);
		break;
	}
}

function delete($title) {
	global $wgUser;
	$pathway = Pathway::newFromTitle($_GET['pwTitle']);
	if($wgUser->isAllowed('delete')) {
		$pathway = Pathway::newFromTitle($_GET['pwTitle']);
		$pathway->delete();
		echo "<h1>Deleted</h1>";
		echo "<p>Pathway $title was deleted, go back to <a href=http://{$_SERVER['HTTP_HOST']}>wikipathways</a>";
	} else {
		echo "<h1>Error</h1>";
		echo "<p>Pathway $title is not deleted, you have no delete permissions</a>";
		$wgOut->permissionRequired( 'delete' );
	}
	exit;
}

function revert($pwTitle, $oldId) {
	$pathway = Pathway::newFromTitle($pwTitle);
	$pathway->revert($oldId);
	//Redirect to old page
	$url = $pathway->getTitleObject()->getFullURL();
	header("Location: $url");
	exit;
}

function launchPathVisio($pathway, $ignore = null, $new = false) {
	global $wgUser;
	
	if(!$new) {
		$gpml = $pathway->getGpml();
		if(!$gpml) {
			throw new Exception("GPML does not exist");
		}
	}
	
	$webstart = file_get_contents("bin/pathvisio_wikipathways.jnlp");
	$arg .= createJnlpArg("-rpcUrl", "http://" . $_SERVER['HTTP_HOST'] . "/wpi/wpi_rpc.php");
	$arg .= createJnlpArg("-pwName", $pathway->name());
	$arg .= createJnlpArg("-pwSpecies", $pathway->species());
	if($new) {
		$arg .= createJnlpArg("-pwUrl", $pathway->getTitleObject()->getFullURL());
	} else {
		$arg .= createJnlpArg("-pwUrl", $pathway->getFileURL(FILETYPE_GPML));
	}
	foreach (array_keys($_COOKIE) as $key) {
		$arg .= createJnlpArg("-c", $key . "=" . $_COOKIE[$key]);
	} 
	if($wgUser && $wgUser->isLoggedIn()) {
		$arg .= createJnlpArg("-user", $wgUser->getRealName());
	}
	if($new) {
		$arg .= createJnlpArg("-new", "1");
	}
	$webstart = str_replace("<!--ARG-->", $arg, $webstart);

	$msg = null;
	if( $wgUser->isLoggedIn() ) {
		if( $wgUser->isBlocked() ) {
			$msg = "Warning: your user account is blocked!";
		}
	} else {
		$msg = "Warning: you are not logged in! You will not be able to save modifications to WikiPathways.org.";
	}
	if($msg && !$ignore) { //If $msg is not null, then we have an error
		$name = $pathway->name();
		$url = $pathway->getFullURL();
		$title = $pathway->getTitleObject()->getPartialURL();
		$jnlp = $wpiScript . "?action=launchPathVisio&pwTitle=$title&ignoreWarning=1";
		$script = 
<<<JS
<html>
<body>
<p>Back to <a href={$url}>{$name}</a></p>
<script type="text/javascript">
var view = confirm("{$msg} You will not be able to save modifications to WikiPathways.org.\\n\\nDo you still want to open the pathway?");
if(view) {
window.location="{$jnlp}";
} else {
history.go(-1);
}
</script>
</body>
</html>
JS;
		echo($script);
		exit;
	}
	sendWebstart($webstart, $pathway->name());//This exits script
}

function sendWebstart($webstart, $tmpname) {
	ob_start();
	ob_clean();
	$os = getClientOs();
	if($os == 'linux') { //return shell script that sets MOZILLA_FIVE_HOME and opens webstart
		header("Content-type: application/x-shellscript");
		header("Content-Disposition: attachment; filename=\"PathVisio.sh\"");
		echo "#!/bin/sh\n";
		echo "export MOZILLA_FIVE_HOME=/usr/lib/firefox\n";
		echo "LD_LIBRARY_PATH=/usr/lib/firefox:$LD_LIBRARY_PATH\n";
		echo 'javaws "'. getJnlpURL($webstart, $tmpname) . '"';
	} else { //return webstart file directly
		header("Content-type: application/x-java-jnlp-file");
		header("Content-Disposition: attachment; filename=\"PathVisio.jnlp\"");
		echo $webstart;
	}
	exit;
}

function getJnlpURL($webstart, $tmpname) {
	$wsFile = tempnam(getcwd() . "/tmp",$tmpname);
	writeFile($wsFile, $webstart);
	return 'http://' . $_SERVER['HTTP_HOST'] . '/wpi/tmp/' . basename($wsFile);
}

function createJnlpArg($flag, $value) {
	//return "<argument>" . $flag . ' "' . $value . '"' . "</argument>\n";
	if(!$flag || !$value) return '';
	return "<argument>" . htmlspecialchars($flag) . "</argument>\n<argument>" . htmlspecialchars($value) . "</argument>\n";
}

function downloadFile($fileType, $pwTitle) {
	ob_start();
	$pathway = Pathway::newFromTitle($pwTitle);
	$file = $pathway->getFileLocation($fileType);
	$fn = $pathway->getFileName($fileType);
	
	ob_clean();
	switch($fileType) {
		case FILETYPE_GPML:
			header("Content-type: text/xml");
			break;
		case FILETYPE_IMG:
			header("Content-type: image/svg+xml");
			break;
		case FILETYPE_PNG:
			header("Content-type: image/png");
			break;
	}
	header("Cache-Control: must-revalidate, post-check=0, pre-check=0");
	header("Content-Disposition: attachment; filename=\"$fn\"");
	header("Content-Length: " . filesize($file));
	set_time_limit(0);
	@readfile($file);
	exit();
}

function getClientOs() {
	$regex = array(
		'windows' => '([^dar]win[dows]*)[\s]?([0-9a-z]*)[\w\s]?([a-z0-9.]*)',
		'mac' => '(68[k0]{1,3})|(ppc mac os x)|([p\S]{1,5}pc)|(darwin)',
		'linux' => 'x11|inux');
	$ua = $_SERVER['HTTP_USER_AGENT'];
	foreach (array_keys($regex) as $os) {
		if(eregi($regex[$os], $ua)) return $os;
	}	
}
 
$spName2Code = array('Human' => 'Hs', 'Rat' => 'Rn', 'Mouse' => 'Mm');//TODO: complete

function toGlobalLink($localLink) {
	if($wgScriptPath && $wgScriptPath != '') {
		$wgScriptPath = "$wgScriptPath/";
	}
	return urlencode("http://" . $_SERVER['HTTP_HOST'] . "$wgScriptPath$localLink");
}

class Pathway {
	private static $spName2Code = array('Human' => 'Hs', 'Rat' => 'Rn', 'Mouse' => 'Mm');
	private static $spCode2Name; //TODO: complete species
	
	private $file_ext = array(FILETYPE_IMG => 'svg', FILETYPE_GPML => 'gpml', FILETYPE_MAPP => 'mapp', FILETYPE_PNG => 'png');
	
	private $pwName;
	private $pwSpecies;

	function __construct($name, $species, $updateCache = false) {
		wfDebug("Creating pathway: $name, $species\n");
		if(!$name) throw new Exception("name argument missing in constructor for Pathway");
		if(!$species) throw new Exception("species argument missing in constructor for Pathway");
		
		$this->pwName = $name;
		$this->pwSpecies = $species;
		if($updateCache) $this->updateCache();
	}
		
	public static function speciesFromCode($code) {
		if(!Pathway::$spCode2Name) {
			foreach(array_keys(Pathway::$spName2Code) as $name) {
				Pathway::$spCode2Name[Pathway::$spName2Code[$name]] = $name;
			}
		}
		return Pathway::$spCode2Name[$code];
	}
	
	public static function codeFromSpecies($species) {
		return Pathway::$spName2Code[$species];
	}
	
	public function newFromTitle($title) {
		if($title instanceof Title) {
			$title = $title->getFullText();
		}
		
		$name = Pathway::nameFromTitle($title);
		$species = Pathway::speciesFromTitle($title);
		$code = Pathway::$spName2Code[$species]; //Check whether this is a valid species
		if($name && $code) {
			return new Pathway($name, $species);
		} else {
		
	throw new Exception("Couldn't parse pathway article title: $title");
		}
	}
	
	public function newFromFileTitle($title) {
		if($title instanceof Title) {
			$title = $title->getText();
		}
		//"Hs_testpathway.ext"
		if(ereg("^([A-Z][a-z])_(.+)\.[A-Za-z]{3,4}$", $title, $regs)) {
			$species = Pathway::speciesFromCode($regs[1]);
			$name = $regs[2];
		}
		if(!$name || !$species) throw new Exception("Couldn't parse file title: $title");
		return new Pathway($name, $species);
	}
	
	public function getFullURL() {
		return $this->getTitleObject()->getFullURL();
	}
	
	public function getTitleObject() {
		//wfDebug("TITLE OBJECT: $this->species():$this->name()\n");
		return Title::newFromText($this->species() . ':' . $this->name(), NS_PATHWAY);
	}
	
	public static function getAvailableSpecies() {
		return array_keys(Pathway::$spName2Code);
	}
	
	private static function nameFromTitle($title) {
		$parts = explode(':', $title);

		if(count($parts) < 2) {
			throw new Exception("Invalid pathway article title: $title");
		}
		return array_pop($parts);
	}

	private static function speciesFromTitle($title) {
		$parts = explode(':', $title);

		if(count($parts) < 2) {
			throw new Exception("Invalid pathway article title: $title");
		}
		$species = array_slice($parts, -2, 1);
		return array_pop($species);
	}

	public function name($name = NULL) {
		if($name) {
			$this->pwName = $name;
		}
		return $this->pwName;
	}
	
	public function getName($textForm = true) {
		return Pathway::nameFromTitle($this->getTitleObject()->getText());
	}
	
	public function species($species = NULL) {
		if($species) {
			$this->pwSpecies = $species;
		}
		return $this->pwSpecies;
	}
	
	public function getSpeciesCode() {
		return Pathway::$spName2Code[$this->pwSpecies];
	}

	public function getGpml() {
		$gpmlTitle = $this->getFileTitle(FILETYPE_GPML);
		$gpmlRef = Revision::newFromTitle($gpmlTitle);
		
		return $gpmlRef == NULL ? "no gpml" : $gpmlRef->getText();
	}

	public function getFileName($fileType) {
		if($fileType == FILETYPE_GPML) {
			return $this->getFilePrefix() . '.' . $this->file_ext[$fileType];
		} else {
			return $this->getFileTitle($fileType)->getDBKey();
		}
	}
	
	public function getFileLocation($fileType, $updateCache = true) {
		if($updateCache) { //Make sure to have up to date version
			$this->updateCache($fileType);	
		}
		$fn = $this->getFileName($fileType);
		return wfImageDir( $fn ) . "/$fn";
	}
	
	public function getFileURL($fileType, $updateCache = true) {
		if($updateCache) {
			$this->updateCache($fileType);
		}
		return "http://" . $_SERVER['HTTP_HOST'] . Image::imageURL($this->getFileName($fileType));
	}
	
	public function getFileTitle($fileType) {
		switch($fileType) {
			case FILETYPE_GPML:
				$title = Title::newFromText($this->getTitleObject()->getText(), NS_GPML);			
				break;
			default:
				$prefix = $this->getFilePrefix();
				$title = Title::newFromText( "$prefix." . $this->file_ext[$fileType], NS_IMAGE );
				if(!$title) {
					throw new Exception("Invalid file title for pathway " + $fileName);
				}
				break;
		}
		return $title;
	}

	public function getFilePrefix() {
		$prefix = $this->getSpeciesCode() . "_" . $this->pwName;
		/*
		 * Filter out illegal characters, and try to make a legible name
		 * out of it. We'll strip some silently that Title would die on.
		 */
		$filtered = preg_replace ( "/[^".Title::legalChars()."]|:/", '-', $prefix );
		$title = Title::newFromText( $filtered, NS_IMAGE );
		if(!$title) {
			throw new Exception("Invalid file title for pathway " + $fileName);
		}
		return $title->getDBKey();
	}

	public function getImageTitle() {
		return $this->getFileTitle(FILETYPE_IMG);
	}

	public function updatePathway($gpmlData, $description) {
		$this->saveGpml($gpmlData, $description);
	}

	public function revert($oldId) {
		global $wgUser, $wgLang;
		$rev = Revision::newFromId($oldId);
		$gpml = $rev->getText();
		if($gpml) {
			$usr = $wgUser->getSkin()->userLink($wgUser->getId(), $wgUser->getName());
			$date = $wgLang->timeanddate( $rev->getTimestamp(), true );
			$this->updatePathway($gpml, "Reverted to version '$date' by $usr");
		} else {
			throw new Exception("Unable to get gpml content");
		}
	}
	
	private function saveImage($gpmlFile, $description) {
		$imgName = $this->getFileName(FILETYPE_IMG);

		# Convert gpml to svg
		$gpmlFile = realpath($gpmlFile);

		$basePath = dirname(realpath(__FILE__));
		$imgFile = $basePath . '/tmp/' . $imgName;
		$cmd = "java -jar bin/pathvisio_converter.jar $gpmlFile $imgFile 2>&1";
		wfDebug($cmd);
		exec($cmd, $output, $status);
		
		foreach ($output as $line) {
			$msg .= $line . "\n";
		}
		wfDebug("Converting to SVG:\nStatus:$status\nMessage:$msg");
		if($status != 0 ) {
			throw new Exception("Unable to convert to SVG:\nStatus:$status\nMessage:$msg");
		}
		# Upload svg file to wiki
		return Pathway::saveFileToWiki($imgFile, $imgName, $description);
	}

	private function newPathwayArticle($gpmlData) {		
		//Create description page
		$title = $this->getTitleObject();
		$article = new Article($title);
		$species = $this->species();
		return $article->doEdit('{{subst:Template:NewPathwayPage|categories=[[Category:'.$species.']]}}', "Created new pathway", EDIT_NEW);
	}

	public function delete() {
		global $wgLoadBalancer;
		wfDebug("Deleting pathway" . $this->getTitleObject()->getFullText() . "\n");
		$reason = 'Deleted pathway';
		$title = $this->getTitleObject();
		Pathway::deleteArticle($title, $reason);
		//Clean up GPML and SVG pages
		$title = $this->getFileTitle(FILETYPE_GPML);
		Pathway::deleteArticle($title, $reason);
		$this->clearCache(null, true);
		$wgLoadBalancer->commitAll();
	}

	private function deleteImagePage($reason) {
		global $wgLoadBalancer;
		$title = $this->getFileTitle(FILETYPE_IMG);
		Pathway::deleteArticle($title, $reason);
		$img = new Image($title);
		$img->delete($reason);
		$wgLoadBalancer->commitAll();
	}

	public static function deleteArticle($title, $reason='not specified') {
		global $wgUser, $wgLoadBalancer;
		
		$article = new Article($title);
		
		if (wfRunHooks('ArticleDelete', array(&$this, &$wgUser, &$reason))) {
			$article->doDeleteArticle($reason);
			$wgLoadBalancer->commitAll();
			wfRunHooks('ArticleDeleteComplete', array(&$this, &$wgUser, $reason));
		}
	}

	private function saveGpml($gpmlData, $description) {
		global $wgLoadBalancer;
		$gpmlTitle = $this->getFileTitle(FILETYPE_GPML);
		$gpmlArticle = new Article($gpmlTitle);		
		
		$new = !$gpmlArticle->exists();

		$succ = true;
		$succ =  $gpmlArticle->doEdit($gpmlData, $description);
		$wgLoadBalancer->commitAll();
		$this->updateCache();
			
		if($new) $succ = $this->newPathwayArticle();
		
		return $succ;
	}
	
	private function saveImageCache() {
		$file = $this->getFileLocation(FILETYPE_GPML);
		$this->saveImage($file, "Updated SVG cache");
	}
	
	private function saveGpmlCache() {
		$gpml = $this->getGpml();
		$file = $this->getFileLocation(FILETYPE_GPML, false);
		writeFile($file, $gpml);
	}
	
	private function savePngCache() {
		global $wgSVGConverters, $wgSVGConverter, $wgSVGConverterPath;
		
		$input = $this->getFileLocation(FILETYPE_IMG);
		$output = $this->getFileLocation(FILETYPE_PNG, false);
		
		$width = 1000;
		$retval = 0;
		if(isset($wgSVGConverters[$wgSVGConverter])) {
			$cmd = str_replace( //TODO: calculate proper height for rsvg
				array( '$path/', '$width', '$input', '$output' ),
				array( $wgSVGConverterPath ? wfEscapeShellArg( "$wgSVGConverterPath/" ) : "",
				intval( $width ),
				wfEscapeShellArg( $input ),
				wfEscapeShellArg( $output ) ),
				$wgSVGConverters[$wgSVGConverter] ) . " 2>&1";
			$err = wfShellExec( $cmd, $retval );
			if($retval != 0 || !file_exists($output)) {
				throw new Exception("Unable to convert to png: $err\nCommand: $cmd");
			}
		} else {
			throw new Exception("Unable to convert to png, no SVG rasterizer found");
		}
		$ex = file_exists($output);
		wfDebug("PNG CACHE SAVED: $output, $ex;\n");
	}
		
	public function updateCache($fileType = null) {
		wfDebug("updateCache called for filetype $fileType\n");
		if(!$fileType) { //Update all
			$this->updateCache(FILETYPE_GPML);
			$this->updateCache(FILETYPE_PNG);
			$this->updateCache(FILETYPE_IMG);
			return;
		}
		if($this->isOutOfDate($fileType)) {
			wfDebug("\t->Updating cached file for $fileType\n");
			switch($fileType) {
			case FILETYPE_PNG:
				$this->savePngCache();
				break;
			case FILETYPE_GPML:
				$this->saveGpmlCache();
				break;
			case FILETYPE_IMG:

				$this->saveImageCache();
			}
		}
	}
	
	public function clearCache($fileType = null, $forceImagePage=false) {
		if($forceImagePage) { //Only delete the image file when explicitly asked for!
			$this->deleteImagePage("Clearing cache");
		}
		if(!$fileType) { //Update all
			$this->clearCache(FILETYPE_PNG);
			$this->clearCache(FILETYPE_GPML);
		} else {
			unlink($this->getFileLocation($fileType, false)); //Delete the cached file
		}
	}

	//Check if the cached version of the GPML data derived file is out of date
	private function isOutOfDate($fileType) {		
		wfDebug("isOutOfDate for $fileType\n");
		
		$gpmlTitle = $this->getFileTitle(FILETYPE_GPML);
		$gpmlRev = Revision::newFromTitle($gpmlTitle);
		if($gpmlRev) {
			$gpmlDate = $gpmlRev->getTimestamp();
		} else {
			$gpmlDate = -1;
		}
		
		$file = $this->getFileLocation($fileType, false);

		if(file_exists($file)) {
			$fmt = wfTimestamp(TS_MW, filemtime($file));
			wfDebug("\tFile exists, cache: $fmt, gpml: $gpmlDate\n");
			return  $fmt < $gpmlDate;
		} else { //No cached version yet, so definitely out of date
			wfDebug("\tFile doesn't exist\n");
			return true;
		}
	}
	
	public function getGpmlModificationTime() {
		$gpmlTitle = $this->getFileTitle(FILETYPE_GPML);
		$gpmlRev = Revision::newFromTitle($gpmlTitle);
		if($gpmlRev) {
			$gpmlDate = $gpmlRev->getTimestamp();
		} else {
			throw new Exception("No GPML page");
		}
		return $gpmlDate;
	}

	## Based on SpecialUploadForm.php
	## Assumes $saveName is already checked to be a valid Title
	//TODO: run hooks
	static function saveFileToWiki( $fileName, $saveName, $description ) {
		global $wgLoadBalancer, $wgUser;
				
		wfDebug("========= UPLOADING FILE FOR WIKIPATHWAYS ==========\n");
		wfDebug("=== IN: $fileName\n=== OUT: $saveName\n");		
		# Check permissions
		if( $wgUser->isLoggedIn() ) {
			if( !$wgUser->isAllowed( 'upload' ) ) {
				throw new Exception( "User has no permission to upload" );
			}
		} else {
			//Print out http headers (for debugging)
			$hds = $_SERVER;
			wfDebug("REQUEST HEADERS\n");
			foreach (array_keys($hds) as $key) {
				$out .= $key . "=" . $hds[$key] . "\n";
			}
			throw new Exception( "You are not logged on, please log in or create an account first" );
		}

		# Check blocks
		if( $wgUser->isBlocked() ) {
			throw new Exception( "User is blocked" );
		}

		if( wfReadOnly() ) {
			throw new Exception( "Page is read-only" );
		}

		# Move the file to the proper directory
		$dest = wfImageDir( $saveName );
		$archive = wfImageArchiveDir( $saveName );
		if ( !is_dir( $dest ) ) wfMkdirParents( $dest );
		if ( !is_dir( $archive ) ) wfMkdirParents( $archive );

		$toFile = "{$dest}/{$saveName}";
		if( is_file( $toFile) ) {
			$oldVersion = gmdate( 'YmdHis' ) . "!{$saveName}";
			$success = rename($toFile, "{$archive}/{$oldVersion}");
			if(!$success) {
				throw new Exception( 
					"Unable to rename file $olddVersion to {$archive}/{$oldVersion}" );
			}
		}
		$success = rename($fileName, $toFile);
		if(!$success) {
			throw new Exception( "Unable to rename file $fileName to $toFile" );
		}
		chmod($toFile, 0644);
		
		# Update the image page
		$img = Image::newFromName( $saveName );
		$success = $img->recordUpload( $oldVersion,
			                           $description,
			                           wfMsgHtml( 'license' ),
			                           "", //Copyright
			                           $fileName,
			                           FALSE ); //Watchthis
		if(!$success) {
			throw new Exception( "Couldn't create description page" );
		}

		$wgLoadBalancer->commitAll();
		return $toFile; # return the saved file
	}
}

function writeFile($filename, $data) {
	$handle = fopen($filename, 'w');
	if(!$handle) {
		throw new Exception ("Couldn't open file $filename");
	}
	if(fwrite($handle, $data) === FALSE) {
		throw new Exception ("Couldn't write file $filename");
	}
	if(fclose($handle) === FALSE) {
		throw new Exception ("Couln't close file $filename");
	}
}

function tag($name, $text, $attributes = array()) {
	foreach(array_keys($attributes) as $key) {
		if($value = $attributes[$key])$attr .= $key . '="' . $value . '" ';
	}
	return "<$name $attr>$text</$name>";
}
?>
