<?php

/**
 * ProtectPage allows to do the following operations:
 *     - show articles' sections that contain <protect> tags
 *     - edit articles' sections that contain <protect> tags
 *     - save articles' sections that contain <protect> tags
 *     - check whether user has access to the specific objects
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Fabian Schmitt <fs@u4m.de>
 * @copyright  2006, 2007 Fabian Schmitt
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */


require_once("AccessList.php");
require_once("Encryption.php");
require_once("ProtectionParser.php");

/**
 * Handles showing, editing and saving articles that contain <protect>-tags.
 */
class ProtectPage {
    public $mAccess = null;
    public $mParser = null;
    public $mEnc = null;
    private $mParser1 = null;
    private $mCipher = null;
    private $mDecrypted = false;
    private $mShow = "";

    /**
     * Constructor.
     */
    function ProtectPage(&$parser) {
        $this->mEnc = new Encryption();
        $this->mParser1 = $parser;
    }

    /**
     * Initialises showing a protected area.
     * @param params Parameters of tag.
     */
    public function initShow($users, $groups, $show) {
        $this->mAccess = new AccessList($users, $groups);
        $this->mShow = $show;
    }

    /**
     * Checks if the user can read the current object.
     * @param user Current User-object.
     * @return true if user can read the text, false otherwise.
     */
    public function hasAccess(&$user)
    {
        if ($this->mShow == "text") {
            return true;
        }
        return $this->mAccess->hasAccess($user);
    }

    /**
     * Encrypts all protected tags within a text-block and 
     * ensures a given username is listed in the list of
     * allowed users.
     * @param text Text to encrypt protect-tags in.
     * @param userName Name of user to ensure to be in permitted list
     *                   (usually current username).
     * @param Is_encrypted (default: true) tells how to treat text
     *	                   to be parsed.
     */
    public function encryptTags(&$text, $userName, $is_encrypted = true) {
        $this->mParser = new ProtectionParser($text, $this->mEnc);
        $this->mParser->parseText($is_encrypted);
        $text = $this->mParser->getEncrypted($userName);
    }
    
    /**
     * Initialises editing-process by parsing the editpage and getting
     * permissions.
     * @editpage EditPage object
    */
    public function initEdit($editpage) {
        $this->mParser = new ProtectionParser($editpage, $this->mEnc);
        $this->mParser->parseText();
        $this->mAccess = $this->mParser->getAccessList();
    }

    /**
     * Returns decrypted text of editpage initilised with initEdit.
     * @return Decrypted text.
     */
    public function decryptPage() {
        return $this->mParser->getDecrypted();
    }

    /**
     * Decryption wrapper.
     * @return Decrypted text.
     */
    public function Decrypt($text) {
        return $this->mEnc->Decrypt($text);
    }

    /*
     * From Cipe.php
     */
    public function parseTag ($text) {
        $text = $this->mParser1->parse( $text,
                                        $this->mParser1->mTitle,
                                        $this->mParser1->mOptions,
                                        false, false );
        $text = $text->getText();
        $text = preg_replace ('~^<p>\s*~', '', $text );
        $text = preg_replace ('~\s*</p>\s*~', '', $text );
        $text = preg_replace ('~\n$~', '', $text );
        return ($text);
    }
}

?>
