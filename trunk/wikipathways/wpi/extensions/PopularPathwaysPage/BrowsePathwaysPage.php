<?php
# Not a valid entry point, skip unless MEDIAWIKI is defined
if (!defined('MEDIAWIKI')) {
        echo <<<EOT
To install BrowsePathwaysPage, put the following line in LocalSettings.php:
require_once( "$IP/extensions/BrowsePathwayPage/BrowsePathwayPage.php" );
EOT;
        exit( 1 );
}

$wgAutoloadClasses['BrowsePathwaysPage'] = dirname(__FILE__) . '/BrowsePathwaysPage_body.php';
$wgSpecialPages['BrowsePathwaysPage'] = 'BrowsePathwaysPage';
$wgHooks['LoadAllMessages'][] = 'BrowsePathwaysPage::loadMessages';

?>
