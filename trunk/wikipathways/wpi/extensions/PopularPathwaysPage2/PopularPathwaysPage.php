<?php
# Not a valid entry point, skip unless MEDIAWIKI is defined
if (!defined('MEDIAWIKI')) {
        echo <<<EOT
To install PopularPathwaysPage, put the following line in LocalSettings.php:
require_once( "$IP/extensions/PopularPathwayPage2/PopularPathwayPage.php" );
EOT;
        exit( 1 );
}

$wgAutoloadClasses['PopularPathwaysPage'] = dirname(__FILE__) . '/PopularPathwaysPage_body.php';
$wgSpecialPages['PopularPathwaysPage'] = 'PopularPathwaysPage';
$wgHooks['LoadAllMessages'][] = 'PopularPathwaysPage::loadMessages';

?>
