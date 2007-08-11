<?php
/* 
When this extension is enabled, it redirects every Image Page to the actual file, 
unless 'showPage' is specified in the GET header 
(e.g. http://wiki.org/wiki.php/Image:test.jpg?showPage=1)
*/

$wgHooks['OutputPageBeforeHTML'][] = 'redirectOnImagePage';

function redirectOnImagePage($out, $text) {
	global $wgTitle;
	#Check if 'showPage' is specified in GET header
	#if so, return
	if($_GET['showPage']) {
		return true;
	} else {
                return false;
	}
	#If this is an Image Page, redirect to file
	$ns = Namespace::getCanonicalName($wgTitle->getNamespace());
	if($ns == 'Image') {
		$image = new Image($wgTitle);
                $url = $image->getURL();
                $out->redirect($url);
		return false;
	} else {
		return true;
	}
}
