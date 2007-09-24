<?php

define('COMMENT_WP_CATEGORY', 'WikiPathways-category');
define('COMMENT_WP_DESCRIPTION', 'WikiPathways-description');

/**
 * Object that holds the actual data from a pathway (as stored in GPML)
 */
class PathwayData {
	private $pathway;
	private $gpml;
	
	function __construct($pathway) {
		$this->pathway = $pathway;
		$this->loadGpml();
	}
	
	/**
	 * Gets the SimpleXML representation of the GPML code
	 */
	function getGpml() {
		return $this->gpml;
	}
	
	/**
	 * Gets the WikiPathways categories that are stored in GPML
	 * Categories are stored as Comments with Source attribute COMMENT_WP_CATEGORY
	 */
	function getWikiCategories() {
		$categories = array();
		foreach($this->gpml->Comment as $comment) {
			if($comment['Source'] == COMMENT_WP_CATEGORY) {
				array_push($categories, (string)$comment);
			}
		}
		return $categories;
	}
	
	/**
	 * Gets the WikiPathways description that is stored in GPML
	 * The description is stored as Comment with Source attribute COMMENT_WP_DESCRIPTION
	 */
	function getWikiDescription() {
		foreach($this->gpml->Comment as $comment) {
			if($comment['Source'] == COMMENT_WP_DESCRIPTION) {
				return (string)$comment;
			}
		}
	}
	
	/**
	 * Get a list of unique elements
	 * \param name The name of the elements to include
	 * \param uniqueAttribute The attribute of which the value has to be unique
	 */
	function getUniqueElements($name, $uniqueAttribute) {
		$unique = array();
		foreach($this->gpml->$name as $elm) {
			$key = $elm[$uniqueAttribute];
			$unique[(string)$key] = $elm;
		}
		return $unique;
	}
	
	function getElementsForPublication($xrefId) {
		$gpml = $this->getGpml();
		$elements = array();
		foreach($gpml->children() as $elm) {
			foreach($elm->BiopaxRef as $ref) {
				$ref = (string)$ref;
				if($xrefId == $ref) {
					array_push($elements, $elm);
				}
			}
		}
		return $elements;
	}
	
	private $pubXRefs;
	
	public function getPublicationXRefs() {
		return $this->pubXRefs;
	}
	
	private function findPublicationXRefs() {
		$this->pubXRefs = array();
		
		$gpml = $this->gpml;

		//Format literature references
		if(!$gpml->Biopax) return;

		$bpChildren = $gpml->Biopax[0]->children('bp', true);
		$xrefs = $bpChildren->PublicationXRef;

		foreach($xrefs as $xref) {
			//Get the rdf:id attribute
			$attr = $xref->attributes('rdf', true);
			$id = $attr['id'] ? $attr['id'] : $i++;
			$this->pubXRefs[(string)$id] = $xref;
		}
	}
	
	private function loadGpml() {
		if(!$this->gpml) {
			$gpml = $this->pathway->getGpml();
			$this->gpml = new SimpleXMLElement($gpml);
			
			//Pre-parse some data
			$this->findPublicationXRefs();
		}
	}
}

?>
