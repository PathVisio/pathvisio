<?php

## Does some preparations for all maintenance scripts

$dir = getcwd();
chdir("../"); //Ugly, but we need to change to the MediaWiki install dir to include these files, otherwise we'll get an error
require_once('wpi.php');
chdir($dir);


set_time_limit(0);
	
//Do a dry run by default, only write database
//when called with doit=true!
$doit = $_GET['doit'] == 'true';
if($doit) {
	echo "WRITE MODE<BR>\n";
	if(!($wgUser->getName() == USER_MAINT_BOT)) {
		echo "WRONG USER {$wgUser->getName()}! Please log in as " . USER_MAINT_BOT . "<BR>\n";
		exit();
	}
} else {
	echo "DRY RUN<BR>\n";
}

?>
