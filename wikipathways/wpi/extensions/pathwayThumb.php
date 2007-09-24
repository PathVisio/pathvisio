<?php

define("JS_OPEN_EDITOR_APPLET", "JS_OPEN_EDITOR_APPLET");

$wgExtensionFunctions[] = 'wfPathwayThumb';
$wgHooks['LanguageGetMagic'][]  = 'wfPathwayThumb_Magic';

function wfPathwayThumb() {
    global $wgParser;
    $wgParser->setFunctionHook( "pwImage", "renderPathwayImage" );
}

function wfPathwayThumb_Magic( &$magicWords, $langCode ) {
        $magicWords['pwImage'] = array( 0, 'pwImage' );
        return true;
}

function renderPathwayImage( &$parser, $pwTitle, $width = 0, $align = '', $caption = '', $href = '', $tooltip = '', $id='pwthumb') {      
	global $wgUser;
	$parser->disableCache();
      try {
                $pathway = Pathway::newFromTitle($pwTitle);
                $img = new Image($pathway->getFileTitle(FILETYPE_IMG));
                switch($href) {
                        case 'svg':
                                $href = Image::imageUrl($pathway->getFileTitle(FILETYPE_IMG)->getPartialURL());
                                break;
                        case 'pathway':
                                $href = $pathway->getFullURL();
                                break;
                        default:
                                if(!$href) $href = $pathway->getFullURL();
                }
		
		$pathwayURL = $pathway->getTitleObject()->getPrefixedURL();
		switch($caption) {
			case 'edit':
			//AP20070918
			if (!$wgUser->isLoggedIn()){
				$href = SITE_URL . "/index.php?title=Special:Userlogin&returnto=$pathwayURL";
				$label = "Log in to edit pathway";
			} else {
				$href = "javascript:;";
				$label = "Edit pathway";
			}
			$caption = "<a href='$href' title='$label' id='edit' ". 
				"class='button'><span>$label</span></a>";
			break;
			case 'view':
				$caption = $pathway->name() . " (" . $pathway->species() . ")";
			break;
			default:
			$caption = html_entity_decode($caption);        //This can be quite dangerous (injection),
                                                                //we would rather parse wikitext, let me know if
                                                                //you know a way to do that (TK)
		}

                $output = makeThumbLinkObj($pathway, $caption, $href, $tooltip, $align, $id, $width);

        } catch(Exception $e) {
                return "invalid pathway title: $e";
        }
        return array($output, 'isHTML'=>1, 'noparse'=>1);
}

    /** MODIFIED FROM Linker.php
        * Make HTML for a thumbnail including image, border and caption
        * $img is an Image object
        */
    function makeThumbLinkObj( $pathway, $label = '', $href = '', $alt, $align = 'right', $id = 'thumb', $boxwidth = 180, $boxheight=false, $framed=false ) {
            global $wgStylePath, $wgContLang;

            $img = new Image($pathway->getFileTitle(FILETYPE_IMG));
            $imgURL = $img->getURL();

            $thumbUrl = '';
            $error = '';

            $width = $height = 0;
            if ( $img->exists() ) {
                    $width  = $img->getWidth();
                    $height = $img->getHeight();
            }
            if ( 0 == $width || 0 == $height ) {
                    $width = $height = 180;
            }
            if ( $boxwidth == 0 ) {
                    $boxwidth = 180;
            }
            if ( $framed ) {
                    // Use image dimensions, don't scale
                    $boxwidth  = $width;
                    $boxheight = $height;
                    $thumbUrl  = $img->getViewURL();
            } else {
                    if ( $boxheight === false ) $boxheight = -1;
                    $thumb = $img->getThumbnail( $boxwidth, $boxheight );
                    if ( $thumb ) {
                            $thumbUrl = $thumb->getUrl();
                            $boxwidth = $thumb->width;
                            $boxheight = $thumb->height;
                    } else {
                            $error = $img->getLastError();
                    }
            }
            $oboxwidth = $boxwidth + 2;

            $more = htmlspecialchars( wfMsg( 'thumbnail-more' ) );
            $magnifyalign = $wgContLang->isRTL() ? 'left' : 'right';
            $textalign = $wgContLang->isRTL() ? ' style="text-align:right"' : '';

            $s = "<div id=\"{$id}\" class=\"thumb t{$align}\"><div class=\"thumbinner\" style=\"width:{$oboxwidth}px;\">";
            if( $thumbUrl == '' ) {
                    // Couldn't generate thumbnail? Scale the image client-side.
                    $thumbUrl = $img->getViewURL();
                    if( $boxheight == -1 ) {
                            // Approximate...
                            $boxheight = intval( $height * $boxwidth / $width );
                    }
            }
            if ( $error ) {
                    $s .= htmlspecialchars( $error );
                    $zoomicon = '';
            } elseif( !$img->exists() ) {
                    $s .= "Image does not exist";
                    $zoomicon = '';
            } else {
                    $s .= '<a href="'.$href.'" class="internal" title="'.$alt.'">'.
                            '<img src="'.$thumbUrl.'" alt="'.$alt.'" ' .
                            'width="'.$boxwidth.'" height="'.$boxheight.'" ' .
                            'longdesc="'.$href.'" class="thumbimage" /></a>';
                    if ( $framed ) {
                            $zoomicon="";
                    } else {
                            $zoomicon =  '<div class="magnify" style="float:'.$magnifyalign.'">'.
                                    '<a href="'.$imgURL.'" class="internal" title="'.$more.'">'.
                                    '<img src="'.$wgStylePath.'/common/images/magnify-clip.png" ' .
                                    'width="15" height="11" alt="" /></a></div>';
                    }
            }
            $s .= '  <div class="thumbcaption"'.$textalign.'>'.$zoomicon.$label."</div></div></div>";
            return str_replace("\n", ' ', $s);
            //return $s;
    }

?>
