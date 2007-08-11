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
				
				$html = tag('p', 'To create a new pathway, specify the pathway name and species 
									and then click "create pathway" to open the pathway editor.<br>
									You can then draw your pathway and close the editor to save it to WikiPathway.org');
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
				$html = tag('form', $html, array('action'=>$wpiScriptURL, 'method'=>'get'));
				$html .= "<HR><A href='http://blog.bigcat.unimaas.nl/~gmmlvisio/pathvisio_v1.jnlp'>Create new local pathway</A>";
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
