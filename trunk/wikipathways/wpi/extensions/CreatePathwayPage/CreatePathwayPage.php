<?php
# Not a valid entry point, skip unless MEDIAWIKI is defined
if (!defined('MEDIAWIKI')) {
        echo <<<EOT
To install CreatePathwayPage, put the following line in LocalSettings.php:
require_once( "$IP/extensions/CreatePathwayPage/CreatePathwayPage.php" );
EOT;
        exit( 1 );
}

$wgAutoloadClasses['CreatePathwayPage'] = dirname(__FILE__) . '/CreatePathwayPage_body.php';
$wgSpecialPages['CreatePathwayPage'] = 'CreatePathwayPage';
$wgHooks['LoadAllMessages'][] = 'CreatePathwayPage::loadMessages';

?>