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
 * @param $par String: becomes "FOO" when called like Special:Allpages/FOO (default NULL)
 * @param $specialPage @see SpecialPage object.
 */
function wfSpecialAllpages( $par=NULL, $specialPage ) {
	global $wgRequest, $wgOut, $wgContLang;

	# GET values
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

	$indexPage = new SpecialAllpages();

	if( !in_array($namespace, array_keys($namespaces)) )
		$namespace = 0;

/** AP20070419	
 *	$wgOut->setPagetitle( $namespace > 0 ?
 *		wfMsg( 'allinnamespace', str_replace( '_', ' ', $namespaces[$namespace] ) ) :
 *		wfMsg( 'allarticles' )
 *		);
 *
 *	Set Pagetitle to "Browse Pathways"
 */
	$wgOut->setPagetitle("Browse Pathways");

/** AP20070419
 *	Set default $indexPage to show Human
 */

	if ( isset($par) ) {
		$indexPage->showChunk( $namespace, $par, $specialPage->including() );
	} elseif ( isset($from) ) {
		$indexPage->showChunk( $namespace, $from, $specialPage->including() );
	} else {
		$indexPage->showChunk( $namespace, 'Human',  $specialPage->including() );
	}
}

class SpecialAllpages {
	var $maxPerPage=960;
	var $topLevelMax=50;
	var $name='Allpages';
	# Determines, which message describes the input field 'nsfrom' (->SpecialPrefixindex.php)
	var $nsfromMsg='allpagesfrom';

/**
 * HTML for the top form
 * @param integer $namespace A namespace constant (default NS_PATHWAY).
 * @param string $from Article name we are starting listing at.
 */
function namespaceForm ( $namespace = NS_PATHWAY, $from = '' ) {
	global $wgScript, $wgContLang;
	$t = SpecialPage::getTitleFor( $this->name );

/** AP20070419
 *	$namespaceselect = HTMLnamespaceselector($namespace, null);
 *
 *	$frombox = "<input type='text' size='20' name='from' id='nsfrom' value=\""
 *	            . htmlspecialchars ( $from ) . '"/>';
 */

                $speciesselect = "\n<select id='nsfrom' name='from' class='namespaceselector'>\n";

		$arr = Pathway::getAvailableSpecies();
		asort($arr);
		$selected = $from;

                foreach ($arr as $index) {
                        if ($index === $selected) {
                                $speciesselect .= "\t" . Xml::element("option",
                                                array("value" => $index, "selected" => "selected"), $index) . "\n";
                        } else {
                                $speciesselect .= "\t" . Xml::element("option", array("value" => $index), $index) . "\n";
                        }
                }
                $speciesselect .= "</select>\n";

	$submitbutton = '<input type="submit" value="' . wfMsgHtml( 'allpagessubmit' ) . '" />';

	$out = "<div class='namespaceoptions'><form method='get' action='{$wgScript}'>";
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

	$out .= '</form></div>';
		return $out;
}

/**
 * @param integer $namespace (default NS_PATHWAY)
 */
function showToplevel ( $namespace = NS_PATHWAY, $including = false ) {
	global $wgOut;
	$fname = "indexShowToplevel";

	# TODO: Either make this *much* faster or cache the title index points
	# in the querycache table.

	$dbr =& wfGetDB( DB_SLAVE );
	$out = "";
	$where = array( 'page_namespace' => $namespace );

	global $wgMemc;
	$key = wfMemcKey( 'allpages', 'ns', $namespace );
	$lines = $wgMemc->get( $key );

	if( !is_array( $lines ) ) {
		$firstTitle = $dbr->selectField( 'page', 'page_title', $where, $fname, array( 'LIMIT' => 1 ) );
		$lastTitle = $firstTitle;

		# This array is going to hold the page_titles in order.
		$lines = array( $firstTitle );

		# If we are going to show n rows, we need n+1 queries to find the relevant titles.
		$done = false;
		for( $i = 0; !$done; ++$i ) {
			// Fetch the last title of this chunk and the first of the next
			$chunk = is_null( $lastTitle )
				? ''
				: 'page_title >= ' . $dbr->addQuotes( $lastTitle );
			$res = $dbr->select(
				'page', /* FROM */
				'page_title', /* WHAT */
				$where + array( $chunk),
				$fname,
				array ('LIMIT' => 2, 'OFFSET' => $this->maxPerPage - 1, 'ORDER BY' => 'page_title') );

			if ( $s = $dbr->fetchObject( $res ) ) {
				array_push( $lines, $s->page_title );
			} else {
				// Final chunk, but ended prematurely. Go back and find the end.
				$endTitle = $dbr->selectField( 'page', 'MAX(page_title)',
					array(
						'page_namespace' => $namespace,
						$chunk
					), $fname );
				array_push( $lines, $endTitle );
				$done = true;
			}
			if( $s = $dbr->fetchObject( $res ) ) {
				array_push( $lines, $s->page_title );
				$lastTitle = $s->page_title;
			} else {
				// This was a final chunk and ended exactly at the limit.
				// Rare but convenient!
				$done = true;
			}
			$dbr->freeResult( $res );
		}
		$wgMemc->add( $key, $lines, 3600 );
	}

	// If there are only two or less sections, don't even display them.
	// Instead, display the first section directly.
	if( count( $lines ) <= 2 ) {
		$this->showChunk( $namespace, '', $including );
		return;
	}

	# At this point, $lines should contain an even number of elements.
	$out .= "<table class='allpageslist' style='background: inherit;'>";
	while ( count ( $lines ) > 0 ) {
		$inpoint = array_shift ( $lines );
		$outpoint = array_shift ( $lines );
		$out .= $this->showline ( $inpoint, $outpoint, $namespace, false );
	}
	$out .= '</table>';
	$nsForm = $this->namespaceForm ( $namespace, '', false );

	# Is there more?
	if ( $including ) {
		$out2 = '';
	} else {
		$morelinks = '';
		if ( $morelinks != '' ) {
			$out2 = '<table style="background: inherit;" width="100%" cellpadding="0" cellspacing="0" border="0">';
			$out2 .= '<tr valign="top"><td align="left">' . $nsForm;
			$out2 .= '</td><td align="right" style="font-size: smaller; margin-bottom: 1em;">';
			$out2 .= $morelinks . '</td></tr></table><hr />';
		} else {
			$out2 = $nsForm . '<hr />';
		}
	}

	$wgOut->addHtml( $out2 . $out );
}

/**
 * @todo Document
 * @param string $from
 * @param integer $namespace (Default NS_PATHWAY)
 */
function showline( $inpoint, $outpoint, $namespace = NS_PATHWAY ) {
	$inpointf = htmlspecialchars( str_replace( '_', ' ', $inpoint ) );
	$outpointf = htmlspecialchars( str_replace( '_', ' ', $outpoint ) );
	$queryparams = ($namespace ? "namespace=$namespace" : '');
	$special = SpecialPage::getTitleFor( $this->name, $inpoint );
	$link = $special->escapeLocalUrl( $queryparams );

	$out = wfMsgHtml(
		'alphaindexline',
		"<a href=\"$link\">$inpointf</a></td><td><a href=\"$link\">",
		"</a></td><td align=\"left\"><a href=\"$link\">$outpointf</a>"
	);
	return '<tr><td align="right">'.$out.'</td></tr>';
}

/**
 * @param integer $namespace (Default NS_PATHWAY)
 * @param string $from list all pages from this name (default FALSE)
 */
function showChunk( $namespace = NS_PATHWAY, $from, $including = false ) {
	global $wgOut, $wgUser, $wgContLang;

	$fname = 'indexShowChunk';

	$sk = $wgUser->getSkin();

	$fromList = $this->getNamespaceKeyAndText($namespace, $from);
	$n = 0;
        
	if ( !$fromList ) {
		$out = wfMsgWikiHtml( 'allpagesbadtitle' );
	} else {
		list( $namespace, $fromKey, $from ) = $fromList;

		$dbr =& wfGetDB( DB_SLAVE );
		$res = $dbr->select( 'page',
			array( 'page_namespace', 'page_title', 'page_is_redirect' ),
			array(
				'page_namespace' => $namespace,
				'page_title >= ' . $dbr->addQuotes( $fromKey ),
				'page_is_redirect' => 0
			),
			$fname,
			array(
				'ORDER BY'  => 'page_title',
				'LIMIT'     => $this->maxPerPage + 1,
				'USE INDEX' => 'name_title',
			)
		);

		$out = '<table style="background: inherit;" border="0" width="100%"><tr><td valign="top"><table>';

		while( ($n < $this->maxPerPage) && ($s = $dbr->fetchObject( $res )) ) {
			$t = Title::makeTitle( $s->page_namespace, $s->page_title );
/** AP20070419
 *  Added species match if-loop around what is shown.
 *  The default "from" strategy wasn't sufficient.
 *  And organize into columns instead of rows for clarity
 */
			$parts = explode(':', $s->page_title);
                	if(count($parts) < 1) {
                        		 throw new Exception("Invalid pathway article title: $s->page_title");
                	}
                	$species = array_shift($parts);
                        $title_only = array_shift($parts);

			if ($species == $from) {

			if( $t ) {
				$link = ($s->page_is_redirect ? '<div class="allpagesredirect">' : '' ) .
					$sk->makeKnownLinkObj( $t, htmlspecialchars( $title_only ), false, false ) .
					($s->page_is_redirect ? '</div>' : '' );
			} else {
				$link = '[[' . htmlspecialchars( $s->page_title ) . ']]';
			}
			if( $n % 40 == 0 ) {
                                $out .= '</table></td><td valign="top"><table>';
                        }
                        $out .= "<tr><td>$link</td></tr>";
                        $n++;
/**			if( $n % 3 == 0 ) {
				$out .= '<tr>';
			}
			$out .= "<td>$link</td>";
			$n++;
			if( $n % 3 == 0 ) {
				$out .= '</tr>';
			}
*/			}			
		}
/**		if( ($n % 3) != 0 ) {
			$out .= '</tr>';
		}
*/		$out .= '</table></td></tr></table>';
	}

	if ( $including ) {
		$out2 = '';
	} else {

		# Get the last title from previous chunk
		$dbr =& wfGetDB( DB_SLAVE );
		$res_prev = $dbr->select(
			'page',
			'page_title',
			array( 'page_namespace' => $namespace, 'page_title < '.$dbr->addQuotes($from) ),
			$fname,
			array( 'ORDER BY' => 'page_title DESC', 'LIMIT' => $this->maxPerPage, 'OFFSET' => ($this->maxPerPage - 1 ) )
		);

		# Get first title of previous complete chunk
		if( $dbr->numrows( $res_prev ) >= $this->maxPerPage ) {
			$pt = $dbr->fetchObject( $res_prev );
			$prevTitle = Title::makeTitle( $namespace, $pt->page_title );
		} else {
		       $prevTitle = null;

/** AP20070420
 *			# The previous chunk is not complete, need to link to the very first title
 *			# available in the database
 *			$reallyFirstPage_title = $dbr->selectField( 'page', 'page_title', array( 'page_namespace' => $namespace ), $fname, array( 'LIMIT' => 1) ); 
 *
 *			# Show the previous link if it s not the current requested chunk
 * 			if ($from != $reallyFirstPage_title) {
 *				$prevTitle =  Title::makeTitle( $namespace, $reallyFirstPage_title );
 *			} else {
 *  				$prevTitle = null;
 *			}
 */
		}

		$nsForm = $this->namespaceForm ( $namespace, $from );
		$out2 = '<table style="background: inherit;" width="100%" cellpadding="0" cellspacing="0" border="0">';
		$out2 .= '<tr valign="top"><td align="left">' . $nsForm;
 		$out2 .= '</td><td align="right" style="font-size: smaller; margin-bottom: 1em;">';
/** AP20070420
 *			      .	$sk->makeKnownLink( $wgContLang->specialPage( "Allpages" ),
 *					wfMsgHtml ( 'allpages' ) );
 */
		$self = SpecialPage::getTitleFor( 'Allpages' );

		# Do we put a previous link ?
		if( isset( $prevTitle ) &&  $pt = $prevTitle->getText() ) {
			$q = 'from=' . $prevTitle->getPartialUrl() . ( $namespace ? '&namespace=' . $namespace : '' );
			$prevLink = $sk->makeKnownLinkObj( $self, wfMsgHTML( 'prevpage', $pt ), $q );
			$out2 .= $prevLink;
		}

		if( $n == $this->maxPerPage && $s = $dbr->fetchObject($res) ) {
			# $s is the first link of the next chunk
			$t = Title::MakeTitle($namespace, $s->page_title);
			$q = 'from=' . $t->getPartialUrl() . ( $namespace ? '&namespace=' . $namespace : '' );
			$nextLink = $sk->makeKnownLinkObj( $self, wfMsgHtml( 'nextpage', $t->getText() ), $q );
			$out2 .= $nextLink;
		}
		$out2 .= "</td></tr></table><hr />";
	}

	$wgOut->addHtml( $out2 . $out );
	if( isset($prevLink) or isset($nextLink) ) {
		$wgOut->addHtml( '<hr/><p style="font-size: smaller; float: right;">' );
		if( isset( $prevLink ) )
			$wgOut->addHTML( $prevLink . ' | ');
		if( isset( $nextLink ) )
			$wgOut->addHTML( $nextLink );
		$wgOut->addHTML( '</p>' );

	}
	
}
	
/**
 * @param int $ns the namespace of the article
 * @param string $text the name of the article
 * @return array( int namespace, string dbkey, string pagename ) or NULL on error
 * @static (sort of)
 * @access private
 */
function getNamespaceKeyAndText ($ns, $text) {
	if ( $text == '' )
		return array( $ns, '', '' ); # shortcut for common case
	
/** AP20070420
 * Use pathway name when passed via prev/next links
 */
	if ($from_pathway) { $text = $from_pathway;}

	$t = Title::makeTitleSafe($ns, $text);
	if ( $t && $t->isLocal() ) {
		return array( $t->getNamespace(), $t->getDBkey(), $t->getText() );
	} else if ( $t ) {
		return NULL;
	}

	# try again, in case the problem was an empty pagename
	$text = preg_replace('/(#|$)/', 'X$1', $text);
	$t = Title::makeTitleSafe($ns, $text);
	if ( $t && $t->isLocal() ) {
		return array( $t->getNamespace(), '', '' );
	} else {
		return NULL;
	}
}
}

?>
