<?php

/**
 * PageProtectionPlus extension.
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Fabian Schmitt <fs@u4m.de>, Pawel Wilk <pw@gnu.org>
 * @copyright  2006, 2007 Fabian Schmitt, Pawel Wilk
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */

define("PROTECT_TAG",	"protect");
define("VAR_USERS",	"{{{USERS}}}");
define("VAR_GROUPS",	"{{{GROUPS}}}");

require_once("PageProtection.i18n.php");

$wgExtensionFunctions[] = "wfPageProtection";

/* register parser hook */
$wgExtensionCredits['parserhook'][] = array(
    'name' => 'PPP',
    'author' => 'Fabian Schmitt, Paweł Wilk',
    'version' => '2.0b',
    'url' => 'http://meta.wikimedia.org/wiki/PPP' );

/* register special page hook */
$wgExtensionCredits['specialpage'][] = array(
    'name' => 'PPP',
    'author' => 'Paweł Wilk, Fabian Schmitt',
    'version' => '2.1b',
    'url' => 'http://meta.wikimedia.org/wiki/PPP' );

/**
 * Extension-function. Registers special page for displaying ciphersuite
 */

if ( !function_exists( 'extAddSpecialPage' ) ) {
	require( dirname(__FILE__) . '/../ExtensionFunctions.php' );
}
extAddSpecialPage( dirname(__FILE__) . '/SpecialCipherSuite.php', 'PageProtectionCipherSuite', 'SpecialPageProtectionCipherSuite' );


/**
 * Extension-function. Registers parser, hook, messages.
 */
function wfPageProtection() {
    global $wgParser;

    global $wgMessageCache, $wgPageProtectionMessages;
    foreach( $wgPageProtectionMessages as $key => $value ) {
	$wgMessageCache->addMessages( $wgPageProtectionMessages[$key], $key );
    }

    $wgParser->setHook( PROTECT_TAG, "protectPage" );

    global $wgHooks;
    $wgHooks['AlternateEdit'][] = 'protectedEdit';
    $wgHooks['ArticleSave'][] = 'protectSave';

}

require_once("ProtectPage.php");
require_once("ErrorHandler.php");

/**
 * Callback function for the hook to the protect-tag.
 * @param text Text to be protected
 * @param params Parameters supplied to the tag
 * @param parser Global parser-object
 * @return If current user is allowed to read the page, $text will be returned.
 *         Otherwise,  an error-page will be returned.
 */
function protectPage( $text, $params, &$parser) {
    $parser->disableCache();

    global $wgUser;
    global $wgOut;

    try {
	$protect = new ProtectPage($parser);
	$protect->initShow($params["users"], $params["groups"], $params["show"]);

	if ($protect->hasAccess($wgUser)) {
    	    global $wgRequest;
    	    try {
    		$rtext = $protect->mEnc->Decrypt($text);
    		$rtext = $protect->parseTag($rtext);
    	    } catch (Exception $e) {
    		$emsg   = $e->getMessage();
    		$etrace = $e->getTraceAsString();
					  /* nasty hack, because we would like to be */
					  /* backward compatible and we cannot check */
					  /* whether some part was encrypted or not  */
					  /* when doing page preview. also there is  */
					  /* no such hook as AlternatePreview.	     */
					  /* as a result we may experience doubled   */
					  /* decryption action, which tries to 	     */
					  /* decrypt already decrypted data causing  */
					  /* error in the wrapper                    */
		/* FIXME: find another way to determine we're in preview  */
		/* rather than looking at the exception's trace		  */
		if (strpos($emsg, 'Error tail') > 10 &&
		    strpos($etrace, 'EditPage->getPreviewText') > 10) {
		    $rtext = $text;
    		    $rtext = $protect->parseTag($rtext);
    		} else {
    		    throw $e;		  /* not that type of error, pass it  */
    		}
	    }
	    return ($rtext);
	}
	$show = $params["show"];
	$page = null;
	if (isset($params["errorpage"])) {
    	    $page = $params["errorpage"];
	}
	$err = new ErrorHandler($protect->mAccess,
				$protect,
            		        $show,
                    		$page);
	return $err->showError($text);
    } catch (Exception $e) {
	    $err = new ErrorHandler($protect->mAccess, $protect);
	    return $err->InternalError('decrypt_error', $e->getMessage());
    }
}

/**
 * Callback function for the hook to ArticleSave. Encrypts the
 * data between protect-tags and ensures the current user is in 
 * list of permitted users.
 */
function protectSave(&$article, &$user, &$text, &$summary, &$minoredit, &$watchthis, &$sectionanchor) {
    global $wgParser;

    try {
	$protect = new ProtectPage($wgParser);
	$protect->mParser = new ProtectionParser($text, $protect->mEnc);
	$protect->mParser->parseText(false);
	$protect->encryptTags($text, $user->getName(), false);
    } catch (Exception $e) {
    	$err = new ErrorHandler($protect->mAccess, $protect);
	return $err->InternalError('encrypt_error', $e->getMessage());
    }

    return true;
}

/**
 * Callback-function for hook to 'AlternateEdit'. Checks if the current user
 * is allowed to edit the current article / section and if so returns the
 * current editpage-object.
 * @param editpage EditPage-object
 * @return editpage if user is allowed to edit the section, null otherwise.
 */
function protectedEdit($editpage) {
    global $wgUser, $wgParser;

    try {
	$protect = new ProtectPage($wgParser);
	$protect->initEdit($editpage);

	if (!$protect->mParser->isProtected() ) {
    	    return $editpage;
	}

	if ($protect->mAccess->hasAccess($wgUser) ) {
    	    $editpage->mArticle->mContent =  $protect->decryptPage();
    	    return true;
	} else {    
    	    $err = new ErrorHandler($protect->mAccess, $wgParser);
    	    return $err->stopEditing();
	}
    } catch (Exception $e) {
	$err = new ErrorHandler($protect->mAccess, $wgParser);
	return $err->InternalError('decrypt_error', $e->getMessage());
    }

    return false;
}

?>
