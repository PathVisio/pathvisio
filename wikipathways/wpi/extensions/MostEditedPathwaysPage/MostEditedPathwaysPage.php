<?php
# Not a valid entry point, skip unless MEDIAWIKI is defined
if (!defined('MEDIAWIKI')) {
        echo <<<EOT
To install MostEditedPathwaysPage, put the following line in LocalSettings.php:
require_once( "$IP/extensions/MostEditedPathwaysPage/MostEditedPathwaysPage.php" );
EOT;
        exit( 1 );
}

$wgAutoloadClasses['MostEditedPathwaysPage'] = dirname(__FILE__) . '/MostEditedPathwaysPage_body.php';
$wgSpecialPages['MostEditedPathwaysPage'] = 'MostEditedPathwaysPage';
$wgHooks['LoadAllMessages'][] = 'MostEditedPathwaysPage::loadMessages';

?>