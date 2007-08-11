<?php
if ( ! defined( 'MEDIAWIKI' ) )
	die();
/**#@+
 * A parser extension that adds two functions, #lst and #lstx, and the 
 * <section> tag, for transcluding marked sections of text.
 *
 * @addtogroup Extensions
 *
 * @link http://www.mediawiki.org/wiki/Extension:Labeled_Section_Transclusion Documentation
 *
 * @bug 5881
 *
 * @author Steve Sanbeg
 * @copyright Copyright © 2006, Steve Sanbeg
 * @license http://www.gnu.org/copyleft/gpl.html GNU General Public License 2.0 or later
 */

##
# Standard initialisation code
##

$wgExtensionFunctions[]="wfLabeledSectionTransclusion";
$wgHooks['LanguageGetMagic'][]       = 'wfLabeledSectionTransclusionMagic';

$wgExtensionCredits['parserhook'][] = array(
        'name' => 'LabeledSectionTransclusion',
        'author' => 'Steve Sanbeg',
        'description' => 'adds #lst and #lstx functions and &lt;section&gt; tag, enables marked sections of text to be transcluded',
        'url' => 'http://www.mediawiki.org/wiki/Extension:Labeled_Section_Transclusion'
        );
$wgParserTestFiles[] = dirname( __FILE__ ) . "/lstParserTests.txt";

function wfLabeledSectionTransclusion() 
{
  global $wgParser, $wgVersion, $wgHooks;
  
  $wgParser->setHook( 'section', 'wfLstNoop' );
  $wgParser->setFunctionHook( 'lst', 'wfLstInclude' );
  $wgParser->setFunctionHook( 'lstx', 'wfLstExclude' );
}

/// Add the magic words - possibly with more readable aliases
function wfLabeledSectionTransclusionMagic( &$magicWords, $langCode ) {
  $magicWords['lst'] = array( 0, 'lst', 'section' );
  $magicWords['lstx'] = array( 0, 'lstx', 'section-x' );
  return true;
}

##############################################################
# To do transclusion from an extension, we need to interact with the parser
# at a low level.  This is the general transclusion functionality
##############################################################

///Register what we're working on in the parser, so we don't fall into a trap.
function wfLst_open_($parser, $part1) 
{
  // Infinite loop test
  if ( isset( $parser->mTemplatePath[$part1] ) ) {
    wfDebug( __METHOD__.": template loop broken at '$part1'\n" );
    return false;
  } else {
    $parser->mTemplatePath[$part1] = 1;
    return true;
  }
  
}

///Finish processing the function.
function wfLst_close_($parser, $part1) 
{
  // Infinite loop test
  if ( isset( $parser->mTemplatePath[$part1] ) ) {
    unset( $parser->mTemplatePath[$part1] );
  } else {
    wfDebug( __METHOD__.": close unopened template loop at '$part1'\n" );
  }
}

/**
 * Handle recursive substitution here, so we can break cycles, and set up
 * return values so that edit sections will resolve correctly.
 **/
function wfLst_parse_($parser, $title, $text, $part1, $skiphead=0) 
{
  // if someone tries something like<section begin=blah>lst only</section>
  // text, may as well do the right thing.
  $text = str_replace('</section>', '', $text);

  if (wfLst_open_($parser, $part1)) {
    //Handle recursion here, so we can break cycles.
    global $wgVersion;
    if( version_compare( $wgVersion, "1.9" ) < 0 ) {
      $text = $parser->replaceVariables($text);
      wfLst_close_($parser, $part1);
    }
    
    //Try to get edit sections correct by munging around the parser's guts.
    return array($text, 'title'=>$title, 'replaceHeadings'=>true, 
		 'headingOffset'=>$skiphead);
  }  else {
    return "[[" . $title->getPrefixedText() . "]]". 
      "<!-- WARNING: LST loop detected -->";
  }
  
}

##############################################################
# And now, the labeled section transclusion
##############################################################

///The section markers aren't paired, so we only need to remove them.
function wfLstNoop( $in, $assocArgs=array(), $parser=null ) {
  return '';
}

///Generate a regex to match the section(s) we're interested in.
function wfLst_pat_($sec, $to) 
{
  $to_sec = ($to == '')?$sec : $to;
  $sec = preg_quote($sec, '/');
  $to_sec = preg_quote($to_sec, '/');
  $ws="(?:\s+[^>]+)?"; //was like $ws="\s*"
  return "/<section$ws\s+(?i:begin)=".
    "(?:$sec|\"$sec\"|'$sec')".
    "$ws\/?>(.*?)\n?<section$ws\s+(?:[^>]+\s+)?(?i:end)=".
    "(?:$to_sec|\"$to_sec\"|'$to_sec')".
    "$ws\/?>/s";
}

///Count headings in skipped text; the $parser arg could go away in the future.
function wfLst_count_headings_($text,$limit) 
{
  //count skipped headings, so parser (as of r18218) can skip them, to
  //prevent wrong heading links (see bug 6563).
  $pat = '^(={1,6}).+\1\s*$';
  return preg_match_all( "/$pat/im", substr($text,0,$limit), $m);
}

function wfLst_text_($parser, $page, &$title, &$text) 
{
  $title = Title::newFromText($page);
  
  if (is_null($title) ) {
    $text = '';
    return true;
  } else {
    if (method_exists($parser, 'fetchTemplateAndTitle')) {
      list($text,$title) = $parser->fetchTemplateAndTitle($title);
    } else {
      $text = $parser->fetchTemplate($title);
    }
  }
  
  //if article doesn't exist, return a red link.
  if ($text == false) {
    $text = "[[" . $title->getPrefixedText() . "]]";
    return false;
  } else {
    return true;
  }
}

///section inclusion - include all matching sections
function wfLstInclude($parser, $page='', $sec='', $to='')
{
  if (wfLst_text_($parser, $page, $title, $text) == false)
    return $text;
  $pat = wfLst_pat_($sec,$to);

  if(preg_match_all( $pat, $text, $m, PREG_OFFSET_CAPTURE)) {
    $headings = wfLst_count_headings_($text, $m[0][0][1]);
  } else {
    $headings = 0;
  }
  
  $text = '';
  foreach ($m[1] as $piece)  {
    $text .= $piece[0];
  }

  //wfDebug("wfLstInclude: skip $headings headings");
  return wfLst_parse_($parser,$title,$text, "#lst:${page}|${sec}", $headings);
}
  
///section exclusion, with optional replacement
function wfLstExclude($parser, $page='', $sec='', $repl='',$to='')
{
  if (wfLst_text_($parser, $page, $title, $text) == false)
    return $text;
  $pat = wfLst_pat_($sec,$to);
  $text = preg_replace( $pat, $repl, $text);
  return wfLst_parse_($parser,$title,$text, "#lstx:$page|$sec");
}

?>
