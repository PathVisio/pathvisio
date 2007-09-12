<?php
require_once("wpi/wpi.php");

class CreatePathwayPage extends SpecialPage
{		
        function CreatePathwayPage() {
                SpecialPage::SpecialPage("CreatePathwayPage");
                self::loadMessages();
        }

        function execute( $par ) {
                global $wgRequest, $wgOut, $wpiScriptURL;
                $this->setHeaders();

		if($_GET['action'] == 'new') { //Submit button pressed
			$this->startEditor($_GET['pwName'], $_GET['pwSpecies']);
		} else {
			$this->showForm();
		}        }

	function startEditor($pwName, $pwSpecies) {
		global $wgRequest, $wgOut, $wpiScriptURL;

		$backlink = '<a href="javascript:history.back(-1)">Back</a>';
		if(!$pwName) {
			$wgOut->addHTML("<B>Please specify a name for the pathway<BR>$backlink</B>");
			return;
		}
		if(!$pwSpecies) {
			$wgOut->addHTML("<B>Please specify a species for the pathway<BR>$backlink</B>");
			return;
		}

		//TODO
		$pathway = new Pathway($pwName, $pwSpecies, false); //new pathway, but no caching
		$wgOut->addHTML("<div id='applet'></div>");
		$pwTitle = $pathway->getTitleObject()->getText();
		$wgOut->addWikiText("{{#editApplet:direct|applet|true|$pwTitle}}");
	}

	function showForm() {
		global $wgRequest, $wgOut, $wpiScriptURL;
		$html = tag('p', 'To create a new pathway on WikiPathways, specify the pathway name and species 
				and then click "create pathway" to start the pathway editor.<br>'
				);
		$html .= "	<input type='hidden' name='action' value='new'>
				<td>Pathway name:
				<td><input type='text' name='pwName'>
				<tr><td>Species:<td>
				<select name='pwSpecies'>";
		$species = Pathway::getAvailableSpecies();
		foreach($species as $sp) {
			$html .= "<option value=$sp" . (!$selected ? ' selected' : '') . ">$sp";
			$selected = true;
		}
		$html .= '</select>';
		$html = tag('table', $html);
		$html .= tag('input', "", array('type'=>'submit', 'value'=>'Create pathway'));
		$html = tag('form', $html, array('action'=>"http://" . $_SERVER['HTTP_HOST'] . '/index.php/Special:CreatePathwayPage', 'method'=>'get'));
		$wgOut->addHTML($html);
	}

        function loadMessages() {
                static $messagesLoaded = false;
                global $wgMessageCache;
                if ( $messagesLoaded ) return;
                $messagesLoaded = true;

                require( dirname( __FILE__ ) . '/CreatePathwayPage.i18n.php' );
                foreach ( $allMessages as $lang => $langMessages ) {
                        $wgMessageCache->addMessages( $langMessages, $lang );
                }
        }
}
?>
