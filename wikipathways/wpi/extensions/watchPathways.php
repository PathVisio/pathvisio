<?php
require_once("wpi/wpi.php");
/* 
Also watche image page on watching pathway page
*/

$wgHooks['WatchArticleComplete'][] = 'watchPathway';
$wgHooks['UnWatchArticleComplete'][] = 'unwatchPathway';

function watchPathway($user, $article) {
	$title = $article->getTitle();
	$pathway = Pathway::newFromTitle($title);
	//Also watch GPML image page
	$gpml = $pathway->getFileTitle(FILETYPE_GPML);
	$user->addWatch($gpml);
	return true;
}

function unwatchPathway($user, $article) {
	$title = $article->getTitle();
	$pathway = new Pathway($title);
	//Also unwatch GPML image page
	$gpml = $pathway->getFileTitle(FILETYPE_GPML);
	$user->removeWatch($gpml);
	return true;
}
