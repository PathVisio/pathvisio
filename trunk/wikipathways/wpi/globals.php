<?php

//File types
define("FILETYPE_IMG", "svg");
define("FILETYPE_GPML", "gpml");
define("FILETYPE_MAPP", "mapp");
define("FILETYPE_PNG", "png");

//Script info
$wpiScriptPath = 'wpi';
$wpiScriptFile = 'wpi.php';
$wpiScript = "$wpiScriptPath/$wpiScriptFile"; 
$wpiTmpPath = "$wpiScriptPath/tmp";
$wpiScriptURL =  "http://" . $_SERVER['HTTP_HOST'] . '/' . $wpiScript; //TODO: use these variables
$wpiTmpURL = "http://" . $_SERVER['HTTP_HOST'] . '/' . $wpiTmpPath;

$wpiScript = 'wpi.php'; //name of the wpi script
$wpiPath = 'wpi'; //path containing wpi script relative to url
$wpiTmpPath = 'tmp'; //temp path, relative to wpi path

define("WPI_SCRIPT_PATH", realpath('.'));
define("WPI_SCRIPT", realpath($wpiScriptFile));
define("WPI_TMP_PATH", realpath($wpiTmpPath));
define("WPI_URL",  "http://" . $_SERVER['HTTP_HOST'] . '/' . $wpiPath);
define("WPI_SCRIPT_URL", WPI_URL . '/' . $wpiScriptFile);
define("WPI_TMP_URL", WPI_URL . '/' . $wpiPath . '/' . $wpiTmpPath);

//JS info
define("JS_SRC_EDITAPPLET", "/wpi/js/editapplet.js");
define("JS_SRC_RESIZE", "/wpi/js/resize.js");
define("JS_SRC_PROTOTYPE", "/wpi/js/prototype.js");
define("JS_SRC_APPLETOBJECT", "/wpi/js/appletobject.js");

//Users
define("USER_MAINT_BOT", "MaintBot"); //User account for maintenance scripts

?>
