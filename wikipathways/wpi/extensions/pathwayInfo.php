<?php

require_once("wpi/wpi.php");
require_once("Article.php");
require_once("ImagePage.php");

/*
Statistics for pathway page
- Description
	> parse pathway comments from GPML
- Datanode list
	> generate list of datanodes
*/

#### DEFINE EXTENSION
# Define a setup function
$wgExtensionFunctions[] = 'wfPathwayInfo';
# Add a hook to initialise the magic word
$wgHooks['LanguageGetMagic'][]  = 'wfPathwayInfo_Magic';

function wfPathwayInfo() {
        global $wgParser;
        # Set a function hook associating the "example" magic word with our function
        $wgParser->setFunctionHook( 'pathwayInfo', 'getPathwayInfo' );
}

function getPathwayInfo( &$parser, $pathway, $type ) {
	$parser->disableCache();
	try {
		$pathway = Pathway::newFromTitle($pathway);
		$info = new PathwayInfo($parser, $pathway);
		return $info->$type();
	} catch(Exception $e) {
		return "Error: $e";
	}
}

function wfPathwayInfo_Magic( &$magicWords, $langCode ) {
        # Add the magic word
        # The first array element is case sensitive, in this case it is not case sensitive
        # All remaining elements are synonyms for our parser function
        $magicWords['pathwayInfo'] = array( 0, 'pathwayInfo' );
        # unless we return true, other parser functions extensions won't get loaded.
        return true;
}

class PathwayInfo {
	private $parser;
	private $pathway;
	private $gpml;
	private $pubXRefs;

	function __construct($parser, $pathway) {
		$this->parser = $parser;
		$this->pathway = $pathway;
	}
		
	function comments() {
		$this->loadGpml();
		foreach($this->gpml->Comment as $comment) {
			$text = (string)$comment;
			$text = htmlentities($text);
			$text = nl2br($text);
			$text = formatPubMed($text);
			$text = uni2ascii($text);
			//$text = str_replace('&#xD','<br>',$text);
			if(!$text) continue;
			$output .= "; " . $comment['Source'] . " : " . $text . "\n";
		}
		return $output;
	}
	
	function findPublicationXRefs() {
		$this->pubXRefs = array();
		
		$this->loadGpml();
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

	function literature() {
		$this->loadGpml();
		$this->findPublicationXRefs();
		$gpml = $this->gpml;

		if(!$gpml->Biopax) return "";

		//Format literature references
		$out = "<biblio>";
		foreach(array_keys($this->pubXRefs) as $id) {
			$xref = $this->pubXRefs[$id];

			$authors = $title = $source = $year = '';

			if((string)$xref->ID && (strtolower($xref->DB) == 'pubmed')) {
				//We have a pubmed id, use biblio extension
				$out .= "#$id $xref pmid=" . $xref->ID . "\n";
			} else {
				//Format the citation ourselves
				//Authors, title, source, year
				foreach($xref->AUTHORS as $a) {
					$authors .= "$a, ";
				}

				if($authors) $authors = substr($authors, 0, -2) . "; ";
				if($xref->TITLE) $title = $xref->TITLE . "; ";
				if($xref->SOURCE) $source = $xref->SOURCE;
				if($xref->YEAR) $year = ", " . $xref->YEAR;
				$out .= "#$id $authors$title$source$year\n";
			}
		}
		$out .= "</biblio>";

		//Create list with pathway elements that refer to literature
		//Go over BiopaxRefs in pathway element
		$refList = $this->reflist($gpml);

		//Go over BiopaxRefs in children
		foreach($gpml->children() as $child) {
			$refList .= $this->reflist($child);
		}
		if($out) {
			$out = "<B>References</B><BR>\n$out\n<BR>";
		}
		if($refList) {
			$refList = "<B>Elements literature references</B><BR>\n$refList\n<BR>";
		}
		return $out . $refList;
	}

	function reflist($elm) {
		$cite = "<cite>";
		foreach($elm->BiopaxRef as $ref) {
			$ref = (string)$ref;
			if($this->pubXRefs[$ref]) { //Only refs that are PublicationXRefs
				$cite .= "$ref, ";
			}
		}
		if(!($cite == "<cite>")) {
			$cite = substr($cite, 0, -2) . "</cite>";
			$label = $elm['TextLabel'];
			if($label) $label = " <I>($label)</I>) ";
			$cite = "\n* " . $elm->getName() . $label . " $cite";
			return $cite;
		}
	}

	function datanodes() {
		$this->loadGpml();
					$table = <<<TABLE
{|class="wikitable"
|-
!Name
!Type
!Backpage Header
!Database reference

TABLE;
//style="border:1px #AAA solid;margin:1em 1em 0;background:#F9F9F9"
			$nodes = getUniqueDataNodes($this->gpml, 'TextLabel');
			sort($nodes);
			foreach($nodes as $datanode) {
				$table .= "|-\n";
				$table .= '|' . $datanode['TextLabel'] . "\n";
				$table .= '|' . $datanode['Type'] . "\n";
				$table .= '|' . $datanode['BackpageHead'] . "\n";
				$xref = $datanode->Xref;
				if(!$xref['ID']) {
					$table .= '|-\n';
				} else {
					$table .= '|[' . getXrefLink($xref) . " $xref[ID] ($xref[Database])]\n";
				}
			}
			$table .= '|}';
			return $table;
	}
	
	private function loadGpml() {
		if(!$this->gpml) {
			$gpmlFile = $this->pathway->getFileLocation(FILETYPE_GPML);
			$this->gpml = simplexml_load_file($gpmlFile);
		}
	}
}

$unicode_array = array(
        "&#8211;" => "-",
        "&#8212;" => "-",
        "?" => "-",
        "&#8216;" => "'",
        "?" => "'",
        "&#8217;" => "'",
        "?" => "'",
        "&#8230;" => "...",
        "?" => "...",
        "?" => "\"",
        "&#8220;" => "\"",
        "?" => "\"",
        "&#8221;" => "\"",
);
	
function uni2ascii($str) {
	global $unicode_array;
	return strtr($str, $unicode_array);
}

function formatPubMed($text) {
	$link = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=pubmed&cmd=Retrieve&dopt=AbstractPlus&list_uids=";
	if(preg_match_all("/PMID: ([0-9]+)/", $text, $ids)) {
		foreach($ids[1] as $id) {
			$text = str_replace($id, "[$link$id $id]", $text);
		}
	}
	return $text;
}

function getUniqueDataNodes($gpml, $uniqueAttribute) {
	$unique = array();
	foreach($gpml->DataNode as $node) {
		$key = $node[$uniqueAttribute];
		$unique[(string)$key] = $node;
	}
	return $unique;
}

function getXrefLink($xref) {
	$db = $xref['Database'];
	$id = $xref['ID'];

	switch($db) {
	case 'Ensembl':
		return "http://www.ensembl.org/Homo_sapiens/searchview?species=all&idx=Gene&q=" . $id;
	case 'Entrez Gene':
		return "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=" . $id;
	case 'SwissProt':
		return "http://www.expasy.org/uniprot/" . $id;
	case 'GenBank':
		return "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&id=" . $id;
	case 'RefSeq':
		$ret = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?";
		if(substr($id,0,2) == 'NM') return $ret . "db=Nucleotide&cmd=Search&term=" . $id;
		else return $ret . "db=Protein&cmd=search&term=" . $id;		
	default:
		return $id;
	}
}

/* TODO: put in switch
if(c.equalsIgnoreCase("En"))
			return "http://www.ensembl.org/Homo_sapiens/searchview?species=all&idx=Gene&q=" + id;
		if(c.equalsIgnoreCase("P"))
			return "http://www.expasy.org/uniprot/" + id;
		if(c.equalsIgnoreCase("Q")) {
			String pre = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?";
			if(id.startsWith("NM")) {
				return pre + "db=Nucleotide&cmd=Search&term=" + id;
			} else {
				return pre + "db=Protein&cmd=search&term=" + id;
			}
		}
		if(c.equalsIgnoreCase("T"))
			return "http://godatabase.org/cgi-bin/go.cgi?view=details&search_constraint=terms&depth=0&query=" + id;
		if(c.equalsIgnoreCase("I"))
			return "http://www.ebi.ac.uk/interpro/IEntry?ac=" + id;
		if(c.equalsIgnoreCase("Pd"))
			return "http://bip.weizmann.ac.il/oca-bin/ocashort?id=" + id;
		if(c.equalsIgnoreCase("X"))
			return "http://www.ensembl.org/Homo_sapiens/featureview?type=OligoProbe;id=" + id;
		if(c.equalsIgnoreCase("Em"))
			return "http://www.ebi.ac.uk/cgi-bin/emblfetch?style=html&id=" + id;
		if(c.equalsIgnoreCase("L"))
			return "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=gene&cmd=Retrieve&dopt=full_report&list_uids=" + id;
		if(c.equalsIgnoreCase("H"))
			return "http://www.gene.ucl.ac.uk/cgi-bin/nomenclature/get_data.pl?hgnc_id=" + id;
		if(c.equalsIgnoreCase("I"))
			return "http://www.ebi.ac.uk/interpro/IEntry?ac=" + id;
		if(c.equalsIgnoreCase("M"))
			return "http://www.informatics.jax.org/searches/accession_report.cgi?id=" + id;
		if(c.equalsIgnoreCase("Om"))
			return "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=OMIM&cmd=Search&doptcmdl=Detailed&term=?" + id;
		if(c.equalsIgnoreCase("Pf"))
			return "http://www.sanger.ac.uk//cgi-bin/Pfam/getacc?" + id;
		if(c.equalsIgnoreCase("R"))
			return "http://rgd.mcw.edu/generalSearch/RgdSearch.jsp?quickSearch=1&searchKeyword=" + id;
		if(c.equalsIgnoreCase("D"))
			return "http://db.yeastgenome.org/cgi-bin/locus.pl?locus=" + id;
		if(c.equalsIgnoreCase("S"))
			return "http://www.expasy.org/uniprot/" + id;
		if(c.equalsIgnoreCase("U")) {
			String [] org_nr = id.split("\\.");
			if(org_nr.length == 2) {
				return "http://www.ncbi.nlm.nih.gov/UniGene/clust.cgi?ORG=" + 
				org_nr[0] + "&CID=" + org_nr[1];
			}
			else {
				return null;
			}
		}
		if (c.equalsIgnoreCase("Nw"))
		{
			return "http://nugowiki.org/index.php/" + id;
		}
		if (c.equalsIgnoreCase("Ca"))
		{
			return "http://chem.sis.nlm.nih.gov/chemidplus/direct.jsp?regno=" + id;
		}
		if (c.equalsIgnoreCase("Cp"))
		{
			return "http://pubchem.ncbi.nlm.nih.gov/summary/summary.cgi?cid=" + id;
		}
		if (c.equalsIgnoreCase("Ce"))
		{
			return "http://www.ebi.ac.uk/chebi/searchId=CHEBI:" + id;
		}
		if (c.equalsIgnoreCase("Ch"))
		{
			return "http://www.hmdb.ca/scripts/show_card.cgi?METABOCARD=" + id + ".txt";
		}
		if (c.equalsIgnoreCase("Ck"))
		{
			return "http://www.genome.jp/dbget-bin/www_bget?cpd:" + id;
		}
*/
?>
