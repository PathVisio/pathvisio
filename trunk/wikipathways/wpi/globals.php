<?php

//File types
define("FILETYPE_IMG", "svg");
define("FILETYPE_GPML", "gpml");
define("FILETYPE_MAPP", "mapp");
define("FILETYPE_PNG", "png");

//Script info
$wpiScriptPath = realpath(dirname(__FILE__));
$wpiScriptFile = 'wpi.php';
$wpiScript = "$wpiScriptPath/$wpiScriptFile"; 
$wpiTmpPath = "$wpiScriptPath/tmp";
$siteURL = "http://{$_SERVER['HTTP_HOST']}/$wgScriptPath";
$wpiURL = "$siteURL/wpi";
$wpiScriptURL =  "$wpiURL/$wpiScript";
$wpiTmpURL = "http://{$_SERVER['HTTP_HOST']}/$wgScriptPath/$wpiTmpPath";

$wpiScript = 'wpi.php'; //name of the wpi script
$wpiPath = 'wpi'; //path containing wpi script relative to url
$wpiTmpPath = 'tmp'; //temp path, relative to wpi path

define("WPI_SCRIPT_PATH", $wpiScriptPath);
define("WPI_SCRIPT", realpath($wpiScriptFile));
define("WPI_TMP_PATH", realpath($wpiTmpPath));
define("SITE_URL", $siteURL);
define("WPI_URL",  $wpiURL);
define("WPI_SCRIPT_URL", WPI_URL . '/' . $wpiScriptFile);
define("WPI_TMP_URL", WPI_URL . '/' . $wpiPath . '/' . $wpiTmpPath);

//JS info
define("JS_SRC_EDITAPPLET", $wgScriptPath . "/wpi/js/editapplet.js");
define("JS_SRC_RESIZE", $wgScriptPath . "/wpi/js/resize.js");
define("JS_SRC_PROTOTYPE", $wgScriptPath . "/wpi/js/prototype.js");
define("JS_SRC_APPLETOBJECT", $wgScriptPath . "/wpi/js/appletobject.js");

//Users
define("USER_MAINT_BOT", "MaintBot"); //User account for maintenance scripts

?>
