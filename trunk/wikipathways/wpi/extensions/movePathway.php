<?php
require_once('wpi/wpi.php');
$wgHooks['SpecialMovepageAfterMove'][] = 'movePathwayPages';

function movePathwayPages(&$movePageForm , &$ot , &$nt) {
	if($ot->getNamespace() == NS_PATHWAY) {
		wfDebug("MOVINGPATHWAY!!");
		$pwOld = Pathway::newFromTitle($ot);
		$pwNew = Pathway::newFromTitle($nt);
		moveAlong($pwOld->getFileTitle(FILETYPE_GPML), $pwNew->getFileTitle(FILETYPE_GPML), $movePageForm);
		
		$pwOld->clearCache(null, true);
		$pwNew->updateCache();
	}
	return(true);
}

function moveAlong($ot, $nt, $movePageForm) {
		wfDebug("MOVING PAGE " . $ot->getFullText() . " TO " . $nt->getFullText() . "\n");
		##MODIFIED FROM SpecialMovePage, doSubmit()
		##This is a quick hack to make moving pathway pages work
		##Will be removed when all pathway information is combined on a single wiki page
		$error = $ot->moveTo( $nt, true, $movePageForm->reason );
		wfDebug("\tMOVING....title says:$error\n");
		# Move the talk page if relevant, if it exists, and if we've been told to
		$ott = $ot->getTalkPage();
		if( $ott->exists() ) {
			if( !$ot->isTalkPage() && !$nt->isTalkPage() ) {
				$ntt = $nt->getTalkPage();
	
				# Attempt the move
				$ott->moveTo( $ntt, true, $movePageForm->reason );
			}
		}
		
/*		
		# Deal with watches
		if( $movePageForm->watch ) {
			$wgUser->addWatch( $ot );
			$wgUser->addWatch( $nt );
		} else {
			$wgUser->removeWatch( $ot );
			$wgUser->removeWatch( $nt );
		}
*/
}
