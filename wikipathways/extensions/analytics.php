<?php
# Google Analytics WikiMedia extension
# by Naoise Golden Santos (naoise at goldensantos dot com)
# http://www.goldensantos.com

# Usage:
# <analytics uacct="UA-XXXXXX-X" ></analytics>

# To install it put this file in the extensions directory 
# To activate the extension, include it from your LocalSettings.php
# with: require("extensions/analytics.php");

$wgExtensionFunctions[] = "wfAnalytics";

function wfAnalytics() {
    global $wgParser;
    # registers the <analytics> extension with the WikiText parser
    $wgParser->setHook( "analytics", "renderAnalytics" );
}

# The callback function for converting the input text to HTML output
function renderAnalytics( $input, $argv ) {
        
        $output = '<script src="http://www.google-analytics.com/urchin.js" type="text/javascript">';
	$output .= '</script>';
        $output .= '<script type="text/javascript">';
        $output .= '_uacct = "'.$argv["uacct"].'";';
        $output .= 'urchinTracker();';
        $output .= '</script>';
        
    return $output;
}
?>
