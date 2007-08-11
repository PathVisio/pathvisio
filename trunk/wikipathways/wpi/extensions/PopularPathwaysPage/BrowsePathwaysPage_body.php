<?php
/**
 * @package MediaWiki
 * @subpackage SpecialPage
 */

/** AP20070419
 * Added wpi.php to access Pathway class and getAvailableSpecies()
 */
require_once('/var/www/wikipathways/wpi/wpi.php');

/**
 * Entry point : initialise variables and call subfunctions.
 * @param $par String: becomes "FOO" when called like Special:BrowsePathwaysPage/FOO (default NULL)
 * @param $specialPage @see SpecialPage object.
 */
function wfSpecialBrowsePathwaysPage( $par=NULL, $specialPage ) {
	global $wgRequest, $wgOut, $wgContLang, $from;

	# GET values

//	$pick = $_POST["from"];
//	if (isset($_POST['browse'])){
//	echo "Pick: ".$pick."<br/>";
//	}

/** AP20070419	
 * Parse species header from 'from' so that prev/next links can work
 */
	$from = $wgRequest->getVal( 'from' );
	$from_pathway = null;
	if(preg_match('/\:/', $from)){	   
		$from_pathway = $from;
		$parts = explode(':', $from);
        	if(count($parts) < 1) {
        		     throw new Exception("Invalid pathway article title: $from");
        	}
        	$from = array_shift($parts);
	}


/** AP20070419	
 *	$namespace = $wgRequest->getInt( 'namespace' );
 *	
 *	Set $namespace to equal 100 (Pathway)
 */
	$namespace = 100;

	$namespaces = $wgContLang->getNamespaces();

	$indexPage = new BrowsePathwaysPage();

	if( !in_array($namespace, array_keys($namespaces)) )
		$namespace = 0;

echo $from, $namespace;

/** AP20070419	
 *	$wgOut->setPagetitle( $namespace > 0 ?
 *		wfMsg( 'allinnamespace', str_replace( '_', ' ', $namespaces[$namespace] ) ) :
 *		wfMsg( 'allarticles' )
 *		);
 *
 *	Set Pagetitle to "Browse Pathways"
 */
	// $wgOut->setPagetitle("Browse Pathways");

/** AP20070419
 *	Set default $indexPage to show Human
 */
}

class BrowsePathwaysPage extends SpecialPage {

	function BrowsePathwaysPage() {
		SpecialPage::SpecialPage("BrowsePathwaysPage");
		self::loadMessages();
	}

	function execute( $par) {
        	
		global $wgOut, $from, $pick, $all;
 		//$pick = $wgRequest->getVal( 'browse'); 
		$pick = $_GET["browse"];
		$picked = $pick;
		$all = 'All Species';
		if ($pick == $all){
			$picked = '';
	                $arr = Pathway::getAvailableSpecies();
        	        asort($arr);
                	foreach ($arr as $index) {
				$picked .=  $index."|";	
			}
			$picked[strlen($picked)-1] = ' ';
		}
        	if (!isset($pick)){
			$pick = 'Human';
			$picked = $pick;
        	}
  		//$wgOut->addHtml("Pick: ".$pick.$picked."<br/>");

	        $wgOut->setPagetitle("Browse Pathways");

		$nsForm = $this->namespaceForm( $namespace, $pick);       

                $wgOut->addHtml( $nsForm . '<hr />');
		$wgOut->addWikiText("<DPL>category=$picked
                                notnamespace=Image
                                namespace=Pathway
                                shownamespace=false
                                mode=category
                                ordermethod=title
                                </DPL>");

//                                notcategory=Cellular Process
//                                notcategory=Molecular Function
//                                notcategory=Metabolic Process
//                                notcategory=Physiological Process

	}

       function loadMessages() {
                static $messagesLoaded = false;
                global $wgMessageCache;
                if ( $messagesLoaded ) return;
                $messagesLoaded = true;

                require( dirname( __FILE__ ) . '/BrowsePathwaysPage.i18n.php' );
                foreach ( $allMessages as $lang => $langMessages ) {
                        $wgMessageCache->addMessages( $langMessages, $lang );
                }
        }

	var $maxPerPage=960;
	var $topLevelMax=50;
	var $name='BrowsePathwaysPage';
	# Determines, which message describes the input field 'nsfrom' (->SpecialPrefixindex.php)
	var $nsfromMsg='allpagesfrom';


/**
 * HTML for the top form
 * @param integer $namespace A namespace constant (default NS_PATHWAY).
 * @param string $from Article name we are starting listing at.
 */
function namespaceForm ( $namespace = NS_PATHWAY, $pick ) {
	global $wgScript, $wgContLang;
	$t = SpecialPage::getTitleFor( $this->name );

/** AP20070419
 *	$namespaceselect = HTMLnamespaceselector($namespace, null);
 *
 *	$frombox = "<input type='text' size='20' name='from' id='nsfrom' value=\""
 *	            . htmlspecialchars ( $from ) . '"/>';
 */

                $speciesselect = "\n<select name='browse' class='namespaceselector'>\n";

		$arr = Pathway::getAvailableSpecies();
		asort($arr);
		$selected = $pick;
		$all = 'All Species';

                foreach ($arr as $index) {
                        if ($index === $selected) {
                                $speciesselect .= "\t" . Xml::element("option",
                                                array("value" => $index, "selected" => "selected"), $index) . "\n";
                        } else {
                                $speciesselect .= "\t" . Xml::element("option", array("value" => $index), $index) . "\n";
                        }
                }
		if ($selected == $all){
			$speciesselect .= "\t" . Xml::element("option",
                                         array("value" => $all, "selected" => "selected"), $all) . "\n";
                 } else {
                         $speciesselect .= "\t" . Xml::element("option", array("value" => $all), $all) . "\n";
                 }

                $speciesselect .= "</select>\n";

	$submitbutton = '<input type="submit" value="Go" name="pick" />';

	$out = "<form method='get' action='{$wgScript}'>";
	$out .= '<input type="hidden" name="title" value="'.$t->getPrefixedText().'" />';
	$out .= "
<table id='nsselect' class='allpages'>
	<tr>
		<td align='right'>Display pathways from:</td>
		<td align='left'>
		    $speciesselect $submitbutton
		</td>
	</tr>
</table>
";

	$out .= '</form>';
		return $out;
}
}

?>
