<?php

/**
 * ProtectTag allows to do the following operations:
 *     - modify protect-tags
 *     - set attributes (parameters) for protect tags
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

require_once("AccessList.php");
require_once("CipherSuite.php");
    
/**
 * Helper for modifying protect-tags.
 * Initialise with setters and then call getStart() and getEnd().
 * @todo Add getters and fromString-method.
 */
class ProtectTag
{
    public $mAccess = null;
    public $mShow = "";
    public $mErrorPage = "";
    public $mCipher = "";
    private $mValidShow = array("warning", "crypt", "page", "none", "text");
    private $mCipherSuite = null;

    /**
     * Constructor. Creates default-tag.
     */
    function ProtectTag()
    {
        $this->setShow("warning");
	$this->setAccessList(new AccessList());
	$this->setCipherSuite(new CipherSuite());
    }

    /**
     * Set access list for current tag
     * @param access AccessList-Object
     */
    public function setAccessList($access)
    {
        $this->mAccess = $access;
    }

    /**
     * Set ciphersuite for current tag.
     * @param ciphersuite-object
     */
    public function setCipherSuite($ciphersuite)
    {
        $this->mCipherSuite = $ciphersuite;
    }

    /**
     * Sets parameter "cipher" for current tag.
     * @param cipher name
     */
    public function setCipher($cipher)
    {
	$cipher = $this->mCipherSuite->correct_cipher_name($cipher);
	if ($cipher === false) {
	    $cipher = "";
	}
	$this->mCipher = $cipher;
    }

    /**
     * Sets parameter "show" and checks for allowed values.
     * @param show "show"-parameter
     */
    public function setShow($show)
    {
        if (in_array($show, $this->mValidShow)) {
            $this->mShow = $show;
        }
    }
    
    /**
     * Sets parameter "errorpage".
     * @param page "errorpage"
     */
    public function setErrorPage($page)
    {
        $this->mErrorPage = $page;
    }

    /**
     * Gets formatted start-tag of this object. It contains only the
     * needed parameters.
     * @return String with start-tag
     */
    public function getStart()
    {
        $tag = "<".PROTECT_TAG." ";
        if ($this->mShow != "") {
            $tag = $tag."show=\"".$this->mShow."\"";
        }
        
        if ($this->mErrorPage != "") {
            $tag = $tag." errorpage=\"".$this->mErrorPage."\"";
        }

        if ($this->mCipher != "") {
            $tag = $tag." cipher=\"".$this->mCipher."\"";
        }

        $tag = $tag
                ." "
                .$this->mAccess->getUsersParam()
                ." "
                .$this->mAccess->getGroupsParam()
                .">";
        return $tag;
    }
    
    /**
     * Gets the end-tag of this protect-tag.
     * @return String with end-tag
     */
    public function getEnd()
    {
        return "</".PROTECT_TAG.">";
    }
}

?>
