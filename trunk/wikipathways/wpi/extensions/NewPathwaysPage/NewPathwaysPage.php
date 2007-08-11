<?php
# Not a valid entry point, skip unless MEDIAWIKI is defined
if (!defined('MEDIAWIKI')) {
        echo <<<EOT
To install NewPathwaysPage, put the following line in LocalSettings.php:
require_once( "$IP/extensions/NewPathwaysPage/NewPathwaysPage.php" );
EOT;
        exit( 1 );
}

$wgAutoloadClasses['NewPathwaysPage'] = dirname(__FILE__) . '/NewPathwaysPage_body.php';
$wgSpecialPages['NewPathwaysPage'] = 'NewPathwaysPage';
$wgHooks['LoadAllMessages'][] = 'NewPathwaysPage::loadMessages';

?>