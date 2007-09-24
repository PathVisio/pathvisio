<?php
require_once('wpi/wpi.php');
$wgHooks['SpecialMovepageAfterMove'][] = 'movePathwayPages';

/**
 * Handles actions needed after moving a page in the pathway namespace
 */
function movePathwayPages(&$movePageForm , &$ot , &$nt) {
	if($ot->getNamespace() == NS_PATHWAY) {
		$pwOld = Pathway::newFromTitle($ot);		
		//Clean up old cache and update for the new page
		$pwOld->clearCache(null, true);
		
		$pwNew = Pathway::newFromTitle($nt);
		$pwNew->updateCache();
		$pwNew->updateCategories();
	}
	return(true);
}
