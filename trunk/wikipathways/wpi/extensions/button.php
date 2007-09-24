<?php

$wgExtensionFunctions[] = "wfButton";

function wfButton() {
    global $wgParser;
    $wgParser->setHook( "fancyButton", "renderButton" );
}

function renderButton( $input, $argv, &$parser ) {
	$parser->disableCache();
	$href = attr('href', $argv['href']);
	$style = attr('style', $argv['style']);
	$title = attr('title', $argv['title']);
	$id = attr('id', $argv['id']);
	$output = "<a $href $style $title $id class='button'><span>$input</span></a>";
	return $output;
}

function attr($name, $value) {
	return $value ? "$name='$value'" : "";
}
?>
