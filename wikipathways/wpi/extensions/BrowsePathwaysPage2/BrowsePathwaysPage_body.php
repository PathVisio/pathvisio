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
        	
		global $wgOut, $from, $pick, $all, $pickCat, $allCat;

		$category = 'category=';
		$picked;
                $category_picked;
                $all = 'All Species';
                $none = 'None';
                $pick = $_GET["browse"];
		if (!isset($pick)){
                        $pick = 'Human';
                }
                if ($pick == $all){
			$picked = '';
	                $arr = Pathway::getAvailableSpecies();
        	        asort($arr);
                	foreach ($arr as $index) {
				$picked .=  $index."|";	
			}
			$picked[strlen($picked)-1] = ' ';
			$category_picked = $category.$picked;
		} else if ($pick == $none){
		        //$picked = '';
			$category = 'notcategory=';
                        $arr = Pathway::getAvailableSpecies();
                        asort($arr);
                        foreach ($arr as $index) {
                                //$picked .=  $index."|";
				$category_picked .= $category.$index."\n";
                        }
		} else {
			$picked = $pick;
                	$category_picked = $category.$picked;
                }
		
		$categoryCat = 'category=';
		$pickedCat;
		$category_pickedCat;
                $allCat = 'All Categories';
                $noneCat = 'None';
                $pickCat = $_GET["browseCat"];
                if (!isset($pickCat)){
                        $pickCat = $allCat;
                }
		if ($pickCat == $allCat){
			$pickedCat = '';
                        $arrCat = Pathway::getAvailableCategories();
                        asort($arrCat);
                        foreach ($arrCat as $cat) {
                                $pickedCat .=  $cat."|";
                        }
                        $pickedCat[strlen($pickedCat)-1] = ' ';
			$category_pickedCat = $categoryCat.$pickedCat;
		} else if ($pickCat == $none){
			$categoryCat = 'notcategory=';
                        $arrCat = Pathway::getAvailableCategories();
                        asort($arrCat);
                        foreach ($arrCat as $cat) {
                        	$category_pickedCat .= $categoryCat.$cat."\n";
			}
		} else {
		        $pickedCat = $pickCat;
	                $category_pickedCat = $categoryCat.$pickedCat;
 		}
                //$wgOut->addHtml("Pick: ".$category_picked.$category_pickedCat."<br/>");

	        $wgOut->setPagetitle("Browse Pathways");

		$nsForm = $this->namespaceForm( $namespace, $pick, $pickCat);       

                $wgOut->addHtml( $nsForm . '<hr />');

		$wgOut->addWikiText("<DPL>
			$category_picked
                       	$category_pickedCat
		        notnamespace=Image
                        namespace=Pathway
                        shownamespace=false
                        mode=category
                        ordermethod=title
                                </DPL>");
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
function namespaceForm ( $namespace = NS_PATHWAY, $pick, $pickCat ) {
	global $wgScript, $wgContLang, $wgOut;
	$t = SpecialPage::getTitleFor( $this->name );

/** AP20070419
 *	$namespaceselect = HTMLnamespaceselector($namespace, null);
 *
 *	$frombox = "<input type='text' size='20' name='from' id='nsfrom' value=\""
 *	            . htmlspecialchars ( $from ) . '"/>';
 */
	/**
	 * Species Selection
 	 */
                $speciesselect = "\n<select name='browse' class='namespaceselector'>\n";

		$arr = Pathway::getAvailableSpecies();
		asort($arr);
		$selected = $pick;
		$all = 'All Species';
		$none = 'None';

                foreach ($arr as $index) {
                        if ($index == $selected) {
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

		if ($selected == $none){
			$speciesselect .= "\t" . Xml::element("option",
                                         array("value" => $none, "selected" => "selected"), $none) . "\n";
                 } else {
                         $speciesselect .= "\t" . Xml::element("option", array("value" => $none), $none) . "\n";
                 }

                $speciesselect .= "</select>\n";

	/**
	 * Category Selection
	 */
	        $catselect = "\n<select name='browseCat' class='namespaceselector'>\n";

		$arrCat = Pathway::getAvailableCategories();
		asort($arrCat);
		$selectedCat = $pickCat;
		$allCat = 'All Categories';
		$noneCat = 'None';
	
		foreach ($arrCat as $cat){	
		       if ($cat == $selectedCat) {
                                $catselect .= "\t" . Xml::element("option",
                                                array("value" => $cat, "selected" => "selected"), $cat) . "\n";
                        } else {
                                $catselect .= "\t" . Xml::element("option", array("value" => $cat), $cat) . "\n";
                        }
                }
                if ($selectedCat == $allCat){
                        $catselect .= "\t" . Xml::element("option",
                                         array("value" => $allCat, "selected" => "selected"), $allCat) . "\n";
                 } else {
                         $catselect .= "\t" . Xml::element("option", array("value" => $allCat), $allCat) . "\n";
                 }
                if ($selectedCat == $noneCat){
                        $catselect .= "\t" . Xml::element("option",
                                         array("value" => $noneCat, "selected" => "selected"), $noneCat) . "\n";
                 } else {
                         $catselect .= "\t" . Xml::element("option", array("value" => $noneCat), $noneCat) . "\n";
                 }

                $catselect .= "</select>\n";


	$submitbutton = '<input type="submit" value="Go" name="pick" />';

	$out = "<form method='get' action='{$wgScript}'>";
	$out .= '<input type="hidden" name="title" value="'.$t->getPrefixedText().'" />';
	$out .= "
<table id='nsselect' class='allpages'>
	<tr>
		<td align='right'>Display pathways from species:</td>
		<td align='left'>$speciesselect</td> 
		<td align='right'> in category:</td>
		<td align='left'>
		$catselect
		$submitbutton
		</td>
	</tr>
</table>
";

	$out .= '</form>';
		return $out;
}
}

?>
