<?php

$wgExtensionFunctions[] = "wfButton";

function wfButton() {
    global $wgParser;
    $wgParser->setHook( "button", "renderButton" );
}

function renderButton( $input, $argv, &$parser ) {
    $href = attr('href', $argv['href']);
	$src = attr('src', $argv['image']);
	$width = attr('width', $argv['width']);
	$height = attr('height', $argv['height']);

	$mOver = jsSetAttr('onmouseOver', 'src', $argv['mouseoverimg']);
	$mOut = jsSetAttr('onmouseOut', 'src', $argv['mouseoutimg']);
	$mDown = jsSetAttr('onmouseDown', 'src', $argv['mousedownimg']);

	$output = "<a $href><img $src $width $height $mOver $mOut $mDown></a>";
    return $output;
}

function attr($name, $value) {
	return $value ? "$name='$value'" : "";
}

function jsSetAttr($name, $attr, $value) {
	if($value) {
		return "$name=\"this.setAttribute('$attr','$value');\"";
	} else {
		return '';
	}
}

?>
