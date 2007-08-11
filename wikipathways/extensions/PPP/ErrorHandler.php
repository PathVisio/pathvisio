<?php

/**
 * ErrorHandler allows to do the following operations:
 *     - provides showing errors for protection-extension.
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Fabian Schmitt <fs@u4m.de>, Pawel Wilk <pw@gnu.org>
 * @copyright  2006, 2007 Fabian Schmitt
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */


/**
 * Provides showing errors for protection-extension.
 */
class ErrorHandler
{
    public $mShow;
    public $mPage;
    public $mAccess;
    public $mParser1;

    /**
     * Constructor.
     * @param accss AccessList-object for current protection-tag.
     * @param show Parameter for protect-tag with type of error.
     *                (warning, page, none, text, crypt)
     * @param page Optional name of page to display if show="page"
     */
    function ErrorHandler($access, &$parser, $show = "warning", $page = "")
    {
        $this->mShow = $show;
        $this->mPage = $page;
        $this->mAccess = $access;
        $this->mParser1 = $parser;
    }
    
    /**
     * Shows error-message. Is initialised with constructor.
     * @param text Optional encrypted text to show if show-parameter
     *             was crypt.
     */
    public function showError($text = "") {
        if ($this->mShow == "crypt") {
            return "<pre>" . $text . "</pre>";
        } else if ($this->mShow == "page" && $this->mPage != "") {
            return $this->getErrorPage();
        } else if ($this->mShow == "none") {
            return "";
        }
    
        return $this->getErrorMessage(true);
    }

    /**
     * Reads the error-message 'ProtectedSite' from the message-cache and
     * replaces
     * all {{{USERS}}} by a comma-separated list of allowed users and all
     * {{{GROUPS}}} by a comma-separated lsit of allowed groups.
     * @param parseWiki If true, the text will be parsed before returning.
     * @return Error-message.
     */
    public function getErrorMessage($parseWiki = true) {
	global $wgSitename;

        $msg = wfMsg('ProtectedSite', $wgSitename, VAR_USERS, VAR_GROUPS);
        return $this->formatMessage($msg, $parseWiki);
    }
    
    /**
     * Replaces parameters {{{USERS}}} and {{{GROUPS}}} with the 
     * comma-separated users- and groups-list and optionally parses
     * the output for wiki-syntax.
     * @param msg Text to be parsed.
     * @param parseWiki If true, text will be parsed for wiki-syntax.
     * @return Returns (wiki-formatted if requested) message.
     */
    public function formatMessage($msg, $parseWiki = true) {
        global $wgOut;

	if (method_exists($this->mAccess,'getUserList')) {
    	    $msg = str_replace(VAR_USERS, $this->mAccess->getUserList(), $msg);
	}
	if (method_exists($this->mAccess,'getGroupList')) {
    	    $msg = str_replace(VAR_GROUPS, $this->mAccess->getGroupList(), $msg);
	}
    
        if ($parseWiki) {
            return ($this->mParser1->parseTag ($msg));
        } else {
            return $msg;
        }
    }
    
    /**
     * Retrieves error-page submitted in custronctur.
     * @return Text from Article pointed to in errorpage-parameter.
     */
    public function getErrorPage() {
        $tit = Title::newFromText($this->mPage);
        $art = new Article($tit);
        return $this->formatMessage($art->getContent());
    }
    
    /**
     * Cancels the editing and prints error message.
     * @return Returns wiki-formatted message.
     */
    public function stopEditing() {
        global $wgOut, $wgSitename;

	$wgOut->setPageTitle( wfMsg( 'ProtectedTitle' ) );
        $wgOut->setRobotpolicy( 'noindex,nofollow' );
        $wgOut->setArticleRelated( false );
        $wgOut->addWikiText( $this->formatMessage( wfMsg ( 'ProtectedSiteEditing',
							    $wgSitename,
							    VAR_USERS, VAR_GROUPS),
                                                    false) );
        $wgOut->returnToMain( false );
    }

    /**
     * Cancels the process and shows internal error.
     * @param Subsystem String containing subsystem name (e.g. function name).
     * @param Message error message to display.
     * @return Returns wiki-formatted message.
     */
    public function InternalError($subsystem, $message='unknown') {
        global $wgOut, $wgSitename;

	$wgOut->setPageTitle( wfMsg( 'ProtectedTitle' ) );
        $wgOut->setRobotpolicy( 'noindex,nofollow' );
        $wgOut->setArticleRelated( false );
        $wgOut->addWikiText( $this->formatMessage( wfMsg (  $subsystem,
							    $wgSitename,
							    $message),
                                                    false) );
        $wgOut->returnToMain( false );
    }

}

?>
