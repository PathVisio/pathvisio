<?php
require_once('wpi/wpi.php');
require_once('Pager.php');
require_once('PageHistory.php');


$wgExtensionFunctions[] = "wfPathwayHistory";

function wfPathwayHistory() {
    global $wgParser;
    $wgParser->setHook( "pathwayHistory", "history" );
}

function history( $input, $argv, &$parser ) {
	try {
		$pathway = Pathway::newFromTitle($parser->mTitle);
		return getHistory($pathway);
	} catch(Exception $e) {
		return "Error: $e";
	}
}

function getHistory($pathway) {
		global $wgUser, $wpiScriptURL;
		
		$gpmlTitle = $pathway->getFileTitle(FILETYPE_GPML);
		$gpmlArticle = new Article($gpmlTitle);
		$hist = new PageHistory($gpmlArticle);

		$pager = new GpmlHistoryPager( $pathway, $hist );

		$s = $pager->getBody();
		
		if($wgUser->isAllowed('delete')) {
			$pwTitle = $pathway->getTitleObject()->getDBKey();
			$delete = "<p><a href=$wpiScriptURL?action=delete&pwTitle=$pwTitle>Delete this pathway</a></p>";
			$s = $delete . $s;
		}
		return $s;
		/*global $wgUser, $wpiScriptURL;
		$sk = $wgUser->getSkin();
		
		$imgTitle = $pathway->getFileTitle(FILETYPE_GPML);
		$img = new Image($imgTitle);
		$line = $img->nextHistoryLine();
		$nrShow = 4;
		$buttonStyle = 'color:#0000FF';
		$expand = "<B>View all</B>";
		$collapse = "<B>View last " . ($nrShow - 1) . "</B>";
		if ( $line ) {
			$table = "<TABLE  id='historyTable' class='wikitable'><TR><TH><TH>Time<TH>User<TH>Comment";
			$table .= historyRow(historyLine(true, $line, $pathway), '');
			$nr = 0;
			while($line = $img->nextHistoryLine()) {
				$h = historyLine(false, $line, $pathway);
				$style = $n<$nrShow ? '' : 'style="display:none"';
				$table .= historyRow($h, $style);
				$n++;
			}
			$table .= "</TABLE>";
		}
		if($n >= $nrShow - 1) {
			$button = "<p onClick='toggleRows(\"historyTable\", this, \"$expand\", 
				\"$collapse\", $nrShow, true)' style='cursor:pointer;color:#0000FF'>$expand</p>";
			$table = $button . $table;
		}
		if($wgUser->isAllowed('delete')) {
			$pwTitle = $pathway->getTitleObject()->getDBKey();
			$delete = "<p><a href=$wpiScriptURL?action=delete&pwTitle=$pwTitle>Delete this pathway</a></p>";
			$table = $delete . $table;
		}
		return $table;
		*/
}

function historyRow($h, $style) {
	return "<TR $style><TD>$h[rev]$h[view]<TD>$h[date]<TD>$h[user]<TD>$h[descr]";
}

function historyLine($pathway, $row, $cur = false) {
	global $wpiScript, $wgLang, $wgUser, $wgTitle;
	
	$rev = new Revision( $row );
	$rev->setTitle( $pathway->getFileTitle(FILETYPE_GPML) );

	$revUrl = 'http://'.$_SERVER['HTTP_HOST'] . '/' .$wpiScript . '?action=revert&pwTitle=' .
				$pathway->getTitleObject()->getPartialURL() .
				"&oldId={$rev->getId()}";
	
	$revert = "";
	if($wgUser->getID() != 0 && $wgTitle && $wgTitle->userCanEdit()) {
		$revert = $cur ? "" : "(<A href=$revUrl>revert</A>), ";
	}
	
	$dt = $wgLang->timeanddate( wfTimestamp(TS_MW, $rev->getTimestamp()), true );
	$view = $wgUser->getSkin()->makeKnownLinkObj($pathway->getFileTitle(FILETYPE_GPML), 'view', "oldid=" . $rev->getId() );

	$date = $wgLang->timeanddate( $rev->getTimestamp(), true );
	$user = $wgUser->getSkin()->userLink( $rev->getUser(), $rev->getUserText() );
	$descr = $rev->getComment();
	return array('rev'=>$revert, 'view'=>$view, 'date'=>$date, 'user'=>$user, 'descr'=>$descr);
}

class GpmlHistoryPager extends PageHistoryPager {
	private $pathway;
	private $nrShow = 4;

	function __construct( $pathway, $pageHistory ) {
		parent::__construct( $pageHistory );
		$this->pathway = $pathway;
	}

	function formatRow( $row ) {
		$latest = $this->mCounter == 1;
		$style = ($this->mCounter <= $this->nrShow) ? '' : 'style="display:none"';
		
		$s = historyRow(historyLine($this->pathway, $row, $latest), $style);
		
		$this->mLastRow = $row;
		$this->mCounter++;
		return $s;
	}

	function getStartBody() {
		$this->mLastRow = false;
		$this->mCounter = 1;
		
		$nr = $this->getNumRows();
		
		if($nr < 1) {
			$table = '';
		} else {
			$table = "<TABLE  id='historyTable' class='wikitable'><TR><TH><TH>Time<TH>User<TH>Comment";
		}

		if($nr >= $this->nrShow) {
			$expand = "<B>View all</B>";
			$collapse = "<B>View last " . ($this->nrShow - 1) . "</B>";
			$button = "<p onClick='toggleRows(\"historyTable\", this, \"$expand\", 
				\"$collapse\", {$this->nrShow}, true)' style='cursor:pointer;color:#0000FF'>$expand</p>";
			$table = $button . $table;
		}

		return $table;
	}

	function getEndBody() {
		return "</TABLE>";
	}
}

?>
