<?php

class CategoryHandler {
	private $pathway;
	
	function __construct($pathway) {
		$this->pathway = $pathway;
	}
	
	/**
	 * Get all categories the pathway belongs to
	 * (As stored in the MediaWiki table)
	 */
	public function getCategories() {
		//Get the categories from mediawiki
		$categories = $this->getMediaWikiCategories();
		return $categories;
	}
	
	/**
	 * Applies the categories stored in GPML to the
	 * MediaWiki database
	 */
	public function setGpmlCategories() {	
		wfDebug("::setGpmlCategories\n");
			
		$dbw =& wfGetDB( DB_MASTER );
		$clFrom = $this->pathway->getTitleObject()->getArticleID();
		$categorylinks = $dbw->tableName( 'categorylinks' );
		
		$dbw->immediateBegin();	
		//Purge old categories
		$dbw->delete( $categorylinks, array( 'cl_from' => $clFrom ) );
		$dbw->immediateCommit();
		
		$dbw->immediateBegin();
		
		//Add the GPML categories
		foreach($this->getGpmlCategories() as $cat) {
			wfDebug("\tCategory: $cat\n");
			$clTo = $dbw->addQuotes($cat);
			$sort = $dbw->addQuotes($this->pathway->getName());
		
			$sql = "INSERT INTO $categorylinks (cl_from, cl_to, cl_sortkey) 
					VALUES ({$dbw->addQuotes($clFrom)}, $clTo, $sort)";
			$dbw->query( $sql, 'SpecialMovepage::doSubmit' );
		}
		$dbw->immediateCommit();		
	}
	
	private function getMediaWikiCategories() {
		$categories = array();
		$title = $this->pathway->getTitleObject();
		$id = $title->getArticleID();
		
		$dbr = wfGetDB( DB_SLAVE );
		$res = $dbr->select( "categorylinks", array("cl_to"), array("cl_from" => $id));
		while( $row = $dbr->fetchRow( $res )) {
			array_push($categories, $row[0]);
		}
		$dbr->freeResult($res);
		return $categories;
	
	}
	
	private function getGpmlCategories() {
		$cats = $this->pathway->getPathwayData()->getWikiCategories();
		array_push($cats, $this->pathway->species());
		return $cats;
	}
	
	/**
	 * Get all available categories
	 * \param includeSpecies whether to include species categories (e.g. Human) or not
	 */
	public static function getAvailableCategories($includeSpecies = false) {
		$arrayCat = array();
      	        $NScat = NS_CATEGORY;
                $dbr = wfGetDB( DB_SLAVE );
                $categorylinks = $dbr->tableName( 'categorylinks' );
                $implicit_groupby = $dbr->implicitGroupby() ? '1' : 'cl_to';
                $sqlCat= "SELECT 'Categories' as type,
                                {$NScat} as namespace,
                                cl_to as title,
                                $implicit_groupby as value,
                                COUNT(*) as count
                           FROM $categorylinks
                           GROUP BY 1,2,3,4";
                $res = $dbr->query($sqlCat, get_class($this) . '::getAvailableCategories');
                while ($obj =  $dbr->fetchObject( $res ) ) {
                        $cat = $obj->title;
			array_push($arrayCat, $cat);
		}
		//Exclude species if needed
		if(!$includeSpecies) { 
			$arraySpecies = Pathway::getAvailableSpecies();		
			$arrayCat = array_diff($arrayCat, $arraySpecies);
		}
		return $arrayCat;
	}
}

?>
