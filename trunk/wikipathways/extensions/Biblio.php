<?php
/*
  Extension of MediaWiki for web-based bibliography retrieval and formatting.
  Copyright (C) 2005, 2006 Martin Jambon and others

  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
  USA.
*/

/*
  Authors:
  =======

  The Biblio extension was developed by Martin Jambon and 
  several contributors: 
    Jason Stajich (reference formatting), 
    Austin Che (parser issues),
    Alexandre Gattiker (cache for pubmed & isbndb queries)

  Many thanks to all of you who contributed ideas and suggestions.


  Setup instructions
  ==================

  Brief instructions are given here. For more info, see
    http://wikiomics.org/wiki/Biblio

  Requirements:

  It requires the PHP files from a recent version of Nusoap, 
  which should be put in the extensions/nusoap/ directory.

  It is recommended that MediaWiki be configured to use APC
  or another cache mechanism. Otherwise the PubMed / ISBN
  databases will need to be contacted upon each request, which
  will considerably slow down the site.

  To activate the extension, you have to:

  1) place the Biblio.php file in the extensions/ subdirectory of your 
     MediaWiki installation

  2) get an access key for the ISBN database (isbndb.com). It is highly
     recommended since the daily quota of queries is by default 
     limited to 500. Otherwise, you would share a key with everyone else.
     Follow this link, register and create a key:

         https://isbndb.com/account/create.html

     Please contact support@isbndb.com if you want to increase your quota. 
     Tell them you are using the Biblio extension for Mediawiki, 
     and that it links each ISBN-referenced book to their site.

  3) Update your LocalSettings.php file with these lines, in that order:

         $BiblioForce = true; // true is the default; see below
         $isbndb_access_key = '12345678'; // your access key
         require_once("extensions/Biblio.php");

  Optionally:

  If $BiblioForce is set to false, references that are present 
  in the <biblio> section (possibly included from a template) 
  are listed only if they are actually cited in the text or 
  forced using <nocite>.


  Features:
  ========

  This module provides tags "cite" and "biblio". 
  "cite" tags indicate a citation in the text, one or several keys are given.
  These keys must be defined in the "biblio" section.
  There is at most one "biblio" section on the page and it must come after 
  the last citation.
  Example:

  some wikitext
  As reported previously <cite>somekey wikipedia</cite>, blabla
  some wikitext
  <biblio>
  #wikipedia [http://www.wikipedia.org Wikipedia]
  #somekey pmid=12345678
  #abook isbn=0-4714-1761-0 // figure 5, page 72 is particularly interesting
  </biblio>
*/

require_once('nusoap/nusoap.php');

/*
  Time To Live; store var in the cache for CACHE_TTL seconds.
*/
define('CACHE_TTL', 3600 * 24);

$BiblioForce = true;

// This module reserves the names starting with "Biblio"

// Registration of this extension
$wgExtensionFunctions[] = "BiblioExtension";

function BiblioExtension() {
  global $wgParser;
  // register the extension with the WikiText parser
  // the first parameter is the name of the new tag.
  // In this case it defines the tag <example> ... </example>
  // the second parameter is the callback function for
  // processing the text between the tags
  $wgParser->setHook("cite", "Biblio_render_cite");
  $wgParser->setHook("nocite", "Biblio_render_nocite");
  $wgParser->setHook("biblio", "Biblio_render_biblio");
}


// XML parsing

class Biblio_xml {
  var $parser;
  var $data;

  function tag_open($parser, $name, $attrs) {
    $data['name'] = $name;
    $data['attributes'] = $attrs;
    $this->data[] = $data;
  }

  function tag_close($parser, $name) {
    if (count($this->data) > 1) {
      $data = array_pop($this->data);
      $index = count($this->data) - 1;
      $this->data[$index]['child'][] = $data;
    }
  }

  function cdata($parser, $s) {
    $index = count($this->data) - 1;
    if (array_key_exists('content',$this->data[$index]))
      $this->data[$index]['content'] .= $s;
    else
      $this->data[$index]['content'] = $s;
  }

  function parse ($text) {
    $this->parser = xml_parser_create();
    xml_set_object($this->parser, $this);
    xml_set_element_handler($this->parser, "tag_open", "tag_close");
    xml_set_character_data_handler($this->parser, "cdata");
    xml_parse($this->parser, $text, true);
    xml_parser_free($this->parser);
    return $this->data;
  }
}

class Biblio {
  
  var $BiblioVersion = "0.8.1";

  // Debugging functions 
 
  function comment($x) {
    echo "<!-- $x -->\n";
  }

  function comment_r($x) {
    echo "<!--\n";
    print_r ($x);
    echo "\n-->\n";
  }

  // Array functions which avoid notices if E_NOTICE is used
  
  function get($array, $key) {
    if (is_array($array) && array_key_exists($key, $array))
      return $array[$key];
    else
      return NULL;
  }

  function unbracket($link) {
    $matches = array();
    preg_match ('/ \[ ( [^\]]* ) \] /sx', $link, $matches);
    return $matches[1];
  }

  // Convert a link (http://a.b.c or [internal] or [inter:wiki]) to a URL.
  function make_url($link, $query = '') {
    // $query is an additional CGI parameter, such as "action=raw".
    // Set the URL if it is a wiki or interwiki link
    $m = Biblio::unbracket($link);
    if (isset($m)) {
      $title = Title::newFromText($m);
      $url = $title->getFullURL($query);
      return $url;
    }
    else
      return $link;
  }

  // Read text from a URL
  function fetch_url($url) {
    $old_url_fopen = ini_set("allow_url_fopen", true);
    $result = @implode('', file($url));
    ini_set("allow_url_fopen", $old_url_fopen);
    return $result;
  }

  // Read the source code of a local wiki page
  function fetch_page($title) {
    $rev = Revision::newFromTitle($title);
    return $rev->getText();
  }


  // Management of the citation indices 
  // (order in which they appear in the text)

  var $Citations = array();

  // Find the number of the reference if already cited or,
  // if $create is true then assign a new one, otherwise return -1.
  function CitationIndex($key, $create = true) {
    if (array_key_exists($key, $this->Citations)) { 
      // ref was already cited
      return $this->Citations[$key];
    } 
    else // ref was not cited yet
      if ($create) { 
	$index = count($this->Citations);
	$this->Citations[$key] = $index;
	return $index;
      } 
      else 
	return -1;
  }
  
  // Retrieve PubMed entry from their SOAP webservice
  function eSummary($pmids) {
    $soap_result = array('DocSum' => array());
    if (count($pmids) == 0) { return $soap_result; }
    $server_url = 
      'http://eutils.ncbi.nlm.nih.gov/entrez/eutils/soap/eutils.wsdl';
    $operation = 'run_eSummary';
    $param = array('db' => 'pubmed',
		   'id' => implode(",", $pmids));
    
    $proxyhost = isset($_POST['proxyhost']) ? $_POST['proxyhost'] : '';
    $proxyport = isset($_POST['proxyport']) ? $_POST['proxyport'] : '';
    $proxyusername = 
      isset($_POST['proxyusername']) ? $_POST['proxyusername'] : '';
    $proxypassword =
      isset($_POST['proxypassword']) ? $_POST['proxypassword'] : '';
    $client = 
      new nusoapclient($server_url, true,
		       $proxyhost, $proxyport, 
		       $proxyusername, $proxypassword);
    $err = $client->getError();
    
    if (!$err) {
      $result = $client->call($operation, array('parameters' => $param), 
			      '', '', false, true);
      if (!$client->fault) {
	$err = $client->getError();
	if ($err) {
	  return $soap_result;
	} else {
	  $soap_result = $result;
	}
      }
    }
    
    return $soap_result;
  }

  function UnWiki($s) {
    return preg_replace(array("/\n+/", "/ /"), array("<br>", "&nbsp;"), $s);
  }

  function Untag($tag, $text) {
    return preg_replace(array("/<\/?$tag>/"), array(""), $text);
  }
  
  function period($s) {
    return $s == '' ? '' : ($s[strlen($s)-1] == '.' ? $s : "$s.");
  }

  function italic($s) {
    return $s == '' ? '' : "<i>$s</i>";
  }


  // Browsing Nusoap's  XML tree

  /*
    Important note about the format of parsed XML:
    Nusoap uses a different representation depending on whether there
    is only one subnode with a given name, or several.
    
    1 subnode (e.g. one author in AuthorList):
    <Item Name="x" ...> y     ->    Item = { !Name = x; ...; ! = y }
    
    Several subnodes (e.g. multiple authors in AuthorList):
    <Item Name="x1" ...> y1       
    <Item Name="x2" ...> y2     ->  Item = [ { !Name = x1; ...; ! = y1 };
                                             { !Name = x2; ...; ! = y2 } ]
  */

  function find_all_fields($field, $items) {
    //$a = is_null($items['!']) ? $items : array($items);
    $a = is_array($items) ? (array_key_exists('!',$items) ? 
			     array($items) : $items) : array();
    $result = array();
    foreach ($a as $x) {
      if (is_array($x)) {
	if ($this->get($x,'!Name') == $field) {
	  if (array_key_exists('!',$x)) {
	    $result[] = $x['!'];
	  } else if (array_key_exists('Item',$x)) {
	    $result[] = $x['Item'];
	  }
	}
      }
    };
    return $result;
  }
  
  function find_field($field, $items) {
    //$a = is_null($items['!']) ? $items : array($items);
    $a = is_array($items) ? (array_key_exists('!',$items) ? 
			     array($items) : $items) : array ();
    foreach ($a as $x) {
      if (is_array($x)) {
	if ($x['!Name'] == $field) {
	  if (array_key_exists('!',$x)) {
	    return $x['!'];
	  } else if (array_key_exists('Item',$x)) {
	    return $x['Item'];
	  }
	}
      }
    };
    return "";
  }




  // Formatting of a PubMed or ISBNDB record


  function FormatBib($authors, $title, $origin, 
		     $pmid, $doi, $isbn, $isbndbref) {
    $title = $this->period($title);
    $title = $this->italic($title);
    
    $authors = $this->period($authors);
    $origin = $this->period($origin);
    
    $codes = '';
    if ($doi != '') $codes .= " doi:$doi";
    if ($pmid != '') $codes .= " pmid:$pmid";
    if ($isbn != '') $codes .= " isbn:$isbn";
    $codes = $this->period($codes);
    
    $result = "$authors $title $origin";
    $style = 'class="extiw" style="color:Black; text-decoration:none"';
    
    if ($doi != '') {
      $title = 'title="View or buy article from publisher"';
      $result = "<a href=\"http://dx.doi.org/$doi\" $title $style>$result</a>";
    } else if ($pmid != '') { 
      $title = 'title="View or buy article from publisher (if available)"';
      $result = 
	"<a href=\"http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?cmd=prlinks&dbfrom=pubmed&retmode=ref&id=$pmid\" $title $style>$result</a>";
    } else if ($isbn != '') {
      $title = 'title="Book information at isbndb.org"';
      $result = "<a href=\"http://isbndb.com/d/book/$isbndbref.html\" $title $style>$result</a>";
    }
    return $result . $codes;
  }
  
  function ConcatAuthors($authors) {
    $n = count($authors);
    $result = "";
    if ($n > 0) {
      $result = $authors[0];
      for ($i = 1; $i <= $n - 2; $i++) {
	$result .= ", $authors[$i]";
      }
      if ($n == 2) {
	$auth = $authors[$n-1];
	$result .= " and $auth.";
      } else if( $n > 2 ) {
	$auth = $authors[$n-1];
	$result .= ", and $auth.";
      }
    }
    return $result;
  }
  
  // Queries to PubMed eSummary service
  
  function FormatPubMed($pmdata) {
    $result = array();
    $docsum = $pmdata['DocSum'];
	if (!$docsum) return $result;
    //$data = is_null($docsum['Item']) ? $docsum : array($docsum);
    $data = array_key_exists('Item',$docsum) ? array($docsum) : $docsum;
    foreach ($data as $entry) {
      $pmid = $entry['Id'];
      $items = $entry['Item'];
      $authors = 
	$this->find_all_fields('Author', 
			       $this->find_field('AuthorList',
						 $items));
      $title = $this->find_field('Title', $items);
      $source = $this->find_field('Source', $items);
      $pubdate = $this->find_field('PubDate', $items);
      $issue = $this->find_field('Issue', $items);
      $volume = $this->find_field('Volume', $items);
      $pages = $this->find_field('Pages', $items);
      $doi = $this->find_field('DOI', $items);
      
      $authors = $this->ConcatAuthors($authors);
      $origin = "$source $pubdate";
      $origin .= $volume == '' ? '' : 
	"; $volume" . ($issue == '' ? '' : "($issue)");
      $origin .= $pages == '' ? '' : " $pages";
      
      $result["$pmid"] = $this->FormatBib($authors, $title, $origin, 
					  $pmid, $doi, '', '');
    }
    return $result;
  }

  function PubMedUrl($pmids) {
    $list_uids = implode(",", $pmids);
    return "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?cmd=Retrieve&db=pubmed&dopt=Abstract&list_uids=$list_uids";
  }
  
  function HubMedUrl($pmids) {
    $list_uids = implode(",", $pmids);
    return "http://www.hubmed.org/display.cgi?uids=$list_uids";
  }
  
  // ISBNDB queries
  
  function FormatAuthors($text) {
    $patterns = array('/\s+([:,;.!?])/',
		      '/c(\d+)/',
		      '/[:,;!?]\s*$/m');
    $replacements = array('\1',
			  '\1',
			  '.');
    return preg_replace($patterns, $replacements, $text);
  }
  
  function FormatPublisher($text) {
    $patterns = array('/\s+([:,;.!?])/',
		      '/c(\d+)/',
		      '/[:,;!?]\s*$/m');
    $replacements = array('\1',
			  '\1',
			  '.');
    return preg_replace($patterns, $replacements, $text);
  }
  
  function ar($a) { return is_array($a) ? $a : array(); }
  
  function Biblio_isbndb_keys () {
    global $isbndb_access_key, $isbndb_access_keys;
    if (isset($isbndb_access_key))
      return array($isbndb_access_key);
    else if (isset($isbndb_access_keys))
      return $isbndb_access_keys;
    else
      return array('9EOE2OGZ');
  }

  function pick ($a) {
    return $a[rand(0,count($a)-1)];
  }

  function IsbnDbQuery_one($isbn) {
    $access_key = $this->pick ($this->Biblio_isbndb_keys ());
    $url = "http://isbndb.com/api/books.xml?access_key=${access_key}&index1=isbn&value1=${isbn}";
    $text = Biblio::fetch_url($url);
    $xml_parser = new Biblio_xml();
    $data = $xml_parser->parse($text);
    
    $thisbook = 
      $this->get($this->get($this->get(
      $this->get($this->get($data, 0), 'child'), 0), 'child'), 0);
    $isbndbref = $this->get($this->get($thisbook, 'attributes'), 'BOOK_ID');
    $bookinfo = $this->ar($this->get($thisbook, 'child'));
    $authors = '';
    $title = '';
    $origin = '';
    foreach ($bookinfo as $field) {
      switch ($this->get($field, 'name')) {
      case 'TITLE': 
	$title = $this->get($field, 'content'); 
	break;
      case 'AUTHORSTEXT': 
	$authors = $this->FormatAuthors($this->get($field, 'content')); 
	break;
      case 'PUBLISHERTEXT': 
	$origin = $this->FormatPublisher($this->get($field, 'content')); 
	break;
      }
    }
    
    $result = $this->FormatBib($authors, $title, $origin, 
			       '', '', $isbn, $isbndbref);
    return $result;
  }
  
  function IsbnDbQuery($isbns) {
    $result = array();
    $cache = &wfGetMainCache();
    global $wgDBname;
    foreach ($isbns as $isbn) {
      $cache_key = "$wgDBname:Biblio:$isbn";
      if ($res = $cache->get($cache_key)) {
	wfDebug("Biblio cache hit $cache_key\n");
      }
      else {
	wfDebug("Biblio cache miss $cache_key\n");
	$res = $this->IsbnDbQuery_one($isbn);
      }
      $cache->set($cache_key, $res, CACHE_TTL);
      $result["$isbn"] = $res;
    }
    return $result;
  }
  
  // General formatting functions
  
  function HtmlLink($url, $text) {
    return "<a href=\"$url\">$text</a>";
  }
  
  function HtmlInterLink($url, $text, $title) {
    return "<a href=\"$url\" class=extiw title=\"$title\"" .
      "rel=\"nofollow\">$text</a>";
  }
  
  function HtmlExtLink($url, $text, $title) {
    return "<a href=\"$url\" class=\"external\" title=\"$title\"" .
      "rel=\"nofollow\">$text</a>";
  }
  
  function noprint($s) {
    return "<span class=noprint>$s</span>";
  }
  
  function error($s) {
    return "<span class=error>$s</span>";
  }
  
  function errorbox($s) {
    return "<div style='float:none;' class=errorbox>$s</div>";
  }
  
  function cleanLi($text) {
    return trim(str_replace(array("\n", "\r"), " ", $text));
  }
  
  function split_biblio($input) {
    return preg_split("/[[:space:]]*^[ \t]*#[[:space:]]*/m",
		      $input, -1, PREG_SPLIT_NO_EMPTY);
  }

  // expandList expands URLs non-recursively
  function expandList($list) {
    $result = array();
    foreach ($list as $ref) {
      $matches = array();
      preg_match ('/ ^ \[ ( \[[^\]]*\] | [^\]]* ) \] /sx', $ref, $matches);
      if (isset($matches[1])) { 
	// It is a link to a list of references
	$link = $matches[1];
	$name = Biblio::unbracket($link);
	if (isset($name)) {
	  // It is a wiki or interwiki link
	  $title = Title::newFromText($name);
	  if ($title->isLocal()) 
	    // It is a local page
	    $x = Biblio::fetch_page($title);
	  else {
	    // It is a shortcut for an external URL ("interwiki").
	    /*
	     * It must point directly to the raw bibliography database, 
	     * not to a regular HTML wiki page.
	     */
	    $url = Biblio::make_url($link);
	    $x = Biblio::fetch_url($url);
	  }
	}
	else {
	  // It is a plain URL
	  $url = Biblio::make_url($link);
	  $x = Biblio::fetch_url($url);
	}

	$biburl = Biblio::make_url($link);
	foreach ($this->split_biblio($x) as $item) {
	  $result[] = array('ref' => $item,
			    'biburl' => $biburl);
	}
      }
      else 
	// A single reference
	$result[] = array('ref' => $ref);
    }
    return $result;
  }

  function parseBiblio($list) {
    $result = array();
    $pmids = array();
    $isbns = array();

    foreach ($list as $ref) {
      $matches = array();
      preg_match ('/([-+A-Za-z_0-9]+)(.*?)(?:[[:space:]]\/\/(.*))?$/s', 
		  $ref['ref'], $matches);
      $key = $this->get($matches,1);
      $srctext = $this->cleanLi($this->get($matches,2));
      $annot = $this->get($matches,3);
      $m = array();
      preg_match ('/^[[:space:]]*pmid=([0-9]+)/', $srctext, $m);
      $pmid = $this->get($m,1);
      preg_match ('/^[[:space:]]*isbn=([-0-9]+[Xx]?)/', $srctext, $m);
      $isbn = $this->get($m,1);
      $x = array('key' => $key, 
		 'annot' => $annot);
      if ($pmid != '') {
	$x['pmid'] = $pmid;
	$pmids[] = $pmid;
      } else if ($isbn != '') {
	$x['isbn'] = $isbn;
	$isbns[] = $isbn;
      } else { // free wikitext
	$x['wikitext'] = $srctext;
      }
      if (isset($ref['biburl']))
	$x['biburl'] = $ref['biburl'];
      $result[] = $x;
    }
    return array('entries' => $result, 
		 'pmids' => $pmids,
		 'isbns' => $isbns);
  }
  
  // Parse wikitext

  function parse_freetext ($pdata, $wikitext) {
    $localParser = new Parser();
    $parserResult = 
      $localParser->parse($wikitext, 
			  $pdata['title'], $pdata['options'], false);
    return trim($parserResult->getText());
  }

  function format_annot ($s) {
    // Any suggestion is appreciated
    return "<dd><dl><table style=\"border:1px dashed #aaa; padding-left:1.5em; padding-right:1.5em; margin-bottom:1em\"><tr><td>$s</td></tr></table></dd></dl>";
  }
  
  function parse_annot ($pdata, $wikitext) {
    $text = trim ($wikitext);
    $result = $text == '' ? '' : 
      $this->format_annot($this->parse_freetext ($pdata, $text));
    return $result;
  }

  // Conversion of the contents of <cite> tags
  function render_cite($input, $pdata, $render = true) {
    $keys = preg_split('/[^-+A-Za-z_0-9]+/', $input, -1, PREG_SPLIT_NO_EMPTY);
    $list = array();
    foreach ($keys as $key) {
      $index = $this->CitationIndex($key, true);
      $list[] = array($index,$key);
    }
    if ($render) {
      sort($list);
      $links = array();
      foreach ($list as $ent) {
	$link = $this->HtmlLink("#bibkey_$ent[1]", $ent[0]+1);
	$links[] = $link;
      }
      return "[". implode(", ", $links) ."]";
    }
    else 
      return "";
  }
  
  // Conversion of the contents of <nocite> tags
  function render_nocite($input, $pdata) {
    return $this->render_cite($input, $pdata, false);
  }
  
  // Conversion of the contents of <biblio> tags
  function render_biblio($input, $pdata, $force) {
    global $Biblio;
    global $wgDBname;
    
    $refs = array();
    $list = Biblio::expandList($this->split_biblio($input));
    $parseResult = Biblio::parseBiblio($list);
    $entries = $parseResult['entries'];
    $pmids = $parseResult['pmids'];
    $isbns = $parseResult['isbns'];
    $pmentries = array();
    $pmids_to_fetch = array();
    $cache = &wfGetMainCache();
    foreach ($pmids as $pmid) {
      $cache_key = "$wgDBname:Biblio:$pmid";
      if ($res = $cache->get($cache_key)) {
	wfDebug("Biblio cache hit $cache_key\n");
	$pmentries["$pmid"] = $res;
      }
      else {
	wfDebug("Biblio cache miss $cache_key\n");
	array_push($pmids_to_fetch, $pmid);
      }
    }

    if (count($pmids_to_fetch)) {
      $pmdata = $this->eSummary($pmids_to_fetch);
      $pmerror = $pmdata["ERROR"];
      $pmentries1 = $this->FormatPubMed($pmdata);
      foreach ($pmentries1 as $pmid => $value) {
	$pmentries[$pmid] = $value;
      }
    }

    foreach ($pmentries as $pmid => $value) {
      $cache_key = "$wgDBname:Biblio:$pmid";
      $cache->set($cache_key, $value, CACHE_TTL);
    }

    $isbnentries = $this->IsbnDbQuery($isbns);
    $refs = array();
    $errors = array();
    foreach ($entries as $ref) {
      $key = $this->get($ref, 'key');
      $annot = $this->parse_annot($pdata, $this->get($ref, 'annot'));
      $pmid = $this->get($ref, 'pmid');
      $isbn = $this->get($ref, 'isbn');
      $wikitext = $this->get($ref, 'wikitext');
      $biburl = $this->get($ref, 'biburl');
      $text = '';
      if (!is_null($pmid)) { // PubMed result
	$pmlink = $this->HtmlInterLink($this->PubMedUrl(array($pmid)), 
				"PubMed", "PMID $pmid");
	$hmlink = $this->HtmlInterLink($this->HubMedUrl(array($pmid)), 
				"HubMed", "PMID $pmid");
	if (array_key_exists($pmid, $pmentries)) {
	  $text = $pmentries["$pmid"] . $this->noprint(" $pmlink $hmlink");
	}
	else {
	  $error = "Error fetching PMID $pmid: $pmerror";
	  array_push($errors, $error);
	  $text = $this->error($error);
	}
      } else if (!is_null($isbn)) { // ISBN
	$text = $isbnentries["$isbn"];
      } else if (!is_null($wikitext)) { // plain wikitext
	$text = $this->parse_freetext($pdata, $wikitext);
      }
      $index = $this->CitationIndex($key, $force);
      if ($index >= 0)
	$refs[] = array('index' => $index, 
			'key' => $key, 
			'text' => $text, 
			'pmid' => $pmid,
			'isbn' => $isbn,
			'annot' => $annot,
			'biburl' => $biburl);
    }
    sort($refs); reset($refs);
    $sorted_pmids = array ();
    foreach ($refs as $ref) {
      $pmid = $this->get($ref,'pmid');
      if (!is_null($pmid)) { $sorted_pmids[] = $pmid; }
    }
    $header = "";
    $footer = "";
    if (count($errors)) {
	$header = $this->errorbox(implode("<br>", $errors));
    }
    if (count($sorted_pmids) > 1) { 
      $footer .= 'All Medline abstracts: ' . 
	$this->HtmlInterLink($this->PubMedUrl($sorted_pmids), 
			     "PubMed", "All abstracts at PubMed") . " " .
	$this->HtmlInterLink($this->HubMedUrl($sorted_pmids), 
			     "HubMed", "All abstracts at HubMed");
      $footer = $this->noprint($footer);
    }
    $result = array();
    foreach ($refs as $ref) {
      $index = $this->get($ref, 'index') + 1;
      $key = $this->get($ref, 'key');
      $annot = $this->get($ref, 'annot');
      $text = $this->get($ref, 'text');
      $vkey = "<span style=\"color:#aaa\">[$key]</span>";
      if (isset($ref['biburl'])) {
	$biburl = htmlspecialchars($ref['biburl']);
	$vkey = "<a href=\"$biburl\" class=\"extiw\" style=\"text-decoration:none\" title=\"link to bibliography database\">$vkey</a>";
      }
      $vkey = $this->noprint($vkey);
      $vkey .= " $annot";
      $result[] = 
	"<li id=\"bibkey_$key\" value=\"$index\"> $text $vkey\n</li>";
    }
    
    //error_reporting($initial_error_reporting);
    $version = $this->BiblioVersion;
    return $header . "<!-- produced by Biblio.php version $version -->" .
      '<ol>' . implode ("", $result) . '</ol>' . $footer;
  }
}

$Biblio = new Biblio;

// Compatibility between MediaWiki 1.5 and later versions
function Biblio_get_parser_data($parser) {
  $parser_data = array();
  
  if ($parser != null) {
    $parser_data['parser'] = $parser;
    $parser_data['title'] = $parser->getTitle();
    $parser_data['options'] = $parser->getOptions();
  } else {
    global $wgParser, $wgTitle, $wgOut;
    $parser_data['parser'] = $wgParser;
    $parser_data['title'] = $wgTitle;
    $parser_data['options'] = $wgOut->mParserOptions;
  }
  return $parser_data;
}

// Conversion of the contents of <cite> tags
function Biblio_render_cite($input, $params, $parser = null) {
  global $Biblio;
  return $Biblio->render_cite($input, 
			      Biblio_get_parser_data($parser), true);
}

// Conversion of the contents of <nocite> tags
function Biblio_render_nocite($input, $params, $parser = null) {
  global $Biblio;
  return $Biblio->render_nocite($input, 
				Biblio_get_parser_data($parser), false);
}

// Conversion of the contents of <biblio> tags
function Biblio_render_biblio($input, $params, $parser = null) {
  global $Biblio, $BiblioForce;
  $force = @isset($params['force']) ? 
              ($params['force'] == "true") : $BiblioForce;
  return $Biblio->render_biblio($input, 
				Biblio_get_parser_data($parser), $force);
}
?>
