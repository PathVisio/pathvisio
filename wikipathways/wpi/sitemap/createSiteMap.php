<?php
# (TK) Modified from:
# -----------------------------------------------------
# MediaWiki - Google Sitemaps generation. v0.3
# 
# A page that'll generate valid Google Sitemaps code
# from the current MediaWiki installation.
# v0.3: Small changes to fix others situations
# v0.2: Updated for MediaWiki 1.5.x
# v0.1: First attempt for MediaWiki 1.4.x
#
# See http://www.thinklemon.com/wiki/MediaWiki:Google_Sitemaps
#
# TODO: Further refinements like caching...
# -----------------------------------------------------

$dir = getcwd();
chdir("/var/www/wikipathways/wpi");
require_once("wpi.php");
chdir($dir);

# -----------------------------------------------------
# Send XML header, tell agents this is XML.
# -----------------------------------------------------

header("Content-Type: application/xml; charset=UTF-8");

# -----------------------------------------------------
# Send xml-prolog
# -----------------------------------------------------

echo '<'.'?xml version="1.0" encoding="utf-8" ?'.">\n"; 

# -----------------------------------------------------
# Start connection
# -----------------------------------------------------

$connWikiDB = mysql_pconnect($wgDBserver, $wgDBuser, $wgDBpassword) 
	or trigger_error(mysql_error(),E_USER_ERROR); 
mysql_select_db($wgDBname, $connWikiDB);

# -----------------------------------------------------
# Build query
# Skipping redirects and MediaWiki namespace
# -----------------------------------------------------

$namespaces = array(NS_PATHWAY => 1, NS_HELP => 0.8, NS_MAIN => 0.5, NS_PATHWAY_TALK => 0.3);
$namespaceString = implode(', ', array_keys($namespaces));
$query_rsPages = "SELECT page_namespace, page_title, page_touched ".
	"FROM ".$wgDBprefix."page ".
	"WHERE (page_is_redirect = 0 AND page_namespace IN ($namespaceString)) ".
	"ORDER BY page_touched DESC";

# -----------------------------------------------------
# Fetch the data from the DB
# -----------------------------------------------------

$rsPages = mysql_query($query_rsPages, $connWikiDB) or die(mysql_error());
# Fetch the array of pages
$row_rsPages = mysql_fetch_assoc($rsPages);
$totalRows_rsPages = mysql_num_rows($rsPages);

# -----------------------------------------------------
# Start output
# -----------------------------------------------------
error_reporting(E_ALL);
?>
<!-- MediaWiki - Google Sitemaps - v0.3 -->
<!-- <?php echo $totalRows_rsPages ?> wikipages found. -->
<!-- Created on <?php echo fnTimestampToIso(time()); ?> -->
<urlset xmlns="http://www.google.com/schemas/sitemap/0.84">
<?php 

do { 
	$nPriority = 0.1;
	$ns = $row_rsPages['page_namespace'];
	$title = Title::makeTitle($ns, $row_rsPages['page_title']);

	//Use latest modification of both GPML and Pathway pages	
	if($ns == NS_PATHWAY) {
		$pathway = Pathway::newFromTitle($title);
		$mod = $pathway->getGpmlModificationTime();
		$row_rsPages['page_touched'] = max($mod, $row_rsPages['page_touched']);
	}

	$sPageName = $title->getFullUrl();
	$nPriority = $namespaces[$ns];

# -----------------------------------------------------
# Start output
# -----------------------------------------------------

?>
	<url>
		<loc><?php echo fnXmlEncode( $sPageName ) ?></loc>
		<lastmod><?php echo fnTimestampToIso($row_rsPages['page_touched']); ?></lastmod>
		<changefreq>daily</changefreq>
		<priority><?php echo $nPriority ?></priority>
	</url>
<?php } while ($row_rsPages = mysql_fetch_assoc($rsPages)); ?>
</urlset>
<?php
# -----------------------------------------------------
# Clear Connection
# -----------------------------------------------------

mysql_free_result($rsPages);

# -----------------------------------------------------
# General functions
# -----------------------------------------------------

// Convert timestamp to ISO format
function fnTimestampToIso($ts) {
	# $ts is a MediaWiki Timestamp (TS_MW)
	# ISO-standard timestamp (YYYY-MM-DDTHH:MM:SS+00:00)

	return gmdate( 'Y-m-d\TH:i:s\+00:00', wfTimestamp( TS_UNIX, $ts ) );
}

// Convert string to XML safe encoding
function fnXmlEncode( $string ) {
	$string = str_replace( "\r\n", "\n", $string );
	$string = preg_replace( '/[\x00-\x08\x0b\x0c\x0e-\x1f]/', '', $string );
	return htmlspecialchars( $string );
}

?>
