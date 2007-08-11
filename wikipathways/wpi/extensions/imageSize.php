<?php

#### DEFINE EXTENSION
# Define a setup function
$wgExtensionFunctions[] = 'wfImageSize';
# Add a hook to initialise the magic word
$wgHooks['LanguageGetMagic'][]  = 'wfImageSize_Magic';

function wfImageSize() {
        global $wgParser;
        # Set a function hook associating the "example" magic word with our function
        $wgParser->setFunctionHook( 'maxImageSize', 'getSize' );
}

function wfImageSize_Magic( &$magicWords, $langCode ) {
        # Add the magic word
        # The first array element is case sensitive, in this case it is not case sensitive
        # All remaining elements are synonyms for our parser function
        $magicWords['maxImageSize'] = array( 0, 'maxImageSize' );
        # unless we return true, other parser functions extensions won't get loaded.
        return true;
}

function getSize( &$parser, $image, $maxWidth ) {
	try {
		$img = new Image(Title::newFromText($image));
		$w = $img->getWidth();
		if($w > $maxWidth) $w = $maxWidth;
		return $w . 'px';
	} catch (Exception $e) {
		return "Error: $e";
	}
}
?>
