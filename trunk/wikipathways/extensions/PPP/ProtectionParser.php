<?php

/**
 * ProtectionParser allows to do the following operations:
 *     - parse text for protection tags
 *     - parse protection tags for attributes
 *     - decrypt data enclosed with protection tags
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Fabian Schmitt <fs@u4m.de>, Pawel Wilk <pw@gnu.org>, Mike Dillon <mdillon@citysearch.com>
 * @copyright  2006, 2007 Fabian Schmitt, Pawel Wilk
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */


require_once("AccessList.php");

/**
 * Parses a text for protection tags and helps decrypting and encrypting these.
 */
class ProtectionParser
{
    protected $mParams = array();
    protected $mContent = array();
    protected $mDecrypted = array();
    private $mParsedText;
    private $mText;
    private $mEnc;

    /**
     * Constructor. Creates a new Parser. Does not automatically parse
     * the text. Call parseText afterwards.
     * @param t Text (String) or EditPage object to be parsed.
     * @param enc Encryption-object to use for encryption and decryption.
     */
    function ProtectionParser($t, &$enc)
    {
        $this->mEnc = $enc;
        if (strtolower(get_class($t)) == "editpage") {
            $text = $t->mArticle->getContent();
            $section = $this->getSection();
            if ($section > 0) {
                $art = new Article($t->mTitle);
                $art->getContent();
                $text = $art->mContent;
            }
            $this->mText = $text;
        }
        else {
            $this->mText = $t;
        }
    }

    /**
     * Content decryption wrapper. Makes sure that a text region
     * wasn't decrypted already and if it's so it decrypts it using
     * the Encryption object.
     * @param key Key which identifies the content part.
     * @param text Text to be decrypted.
     * @return Decryption result or previuos decryption result.
     */
    private function DecryptContent($key, $text)
    {
	if (!array_key_exists($key, $this->mDecrypted)) {
	    $this->mDecrypted[$key] = true;
    	    return $this->mEnc->Decrypt($text);
    	} else {
    	    return $text;
    	}
    }

    /**
     * Reads currently displayed or edited section from request.
     * @return Current section or 0 if no section is beeing edited.
     */
    public function getSection()
    {
        global $wgRequest;
        // section when editing
        $section = $wgRequest->getText('section');
        if (!$section) {
            // for preview and finish editing, section is in wpSection
            $section = $wgRequest->getText('wpSection');
            if (!$section) {
                $section = 0;
            }
        }
        return $section;
    }

    /**
     * Parses a text for protect-tags and stores the texts in member-fields.
     * The resulting array are mContent, mParams and mSections.
     * @param is_encrypted Boolean value, that indicates, if the parsed text
     *                     is encrypted at all (dafault is true).
     */
    public function parseText($is_encrypted = true) {
        $this->mContent = array();
        $this->mParams = array();
        $this->mElements = array (PROTECT_TAG);
	$this->mParsedText = Parser::extractTagsAndParams(
               $this->mElements,
               $this->mText,
               $this->mContent );

        // Strip out anything except <protect> (i.e. <!-- --> comments)
        foreach ($this->mContent as $key => $content) {
            if (strtolower($content[0]) === PROTECT_TAG) continue;

            // Put back text by replacing random key with original text
	    $this->mParsedText = str_replace(
                $key, $content[3], $this->mParsedText);

            // Remove content for replaced $key from $this->mContent
	    unset($this->mContent[$key]);
        }

        // decrypt all tags that are stored encrypted
        foreach($this->mContent as $key => $content)
        {
    	    $dec = false;
    	    if ($is_encrypted === true) {
    		$dec = $this->DecryptContent($key, $content[1]);
    	    }
    	    if ($dec !== false && $dec !== "") {
            	    $this->mContent[$key][1] = $dec;
    	    }
        }
    }
    
    /**
     * Retrieves access-list of users and groups found in all tags
     * in the text supplied in constructor.
     * @return AccessList-object with users that have permissions for
     *         all tags.
     */
    public function getAccessList()
    {
        $list = new AccessList();
        foreach ($this->mContent as $param) {
            $list->RestrictUsers($param[2]["users"]);
            $list->RestrictGroups($param[2]["groups"]);
        }
        return $list;
    }
    
    /**
     * Retrieves text of this object with all protect-tags beeing
     * decrypted before.
     * @return Text with decrypted tags.
     */
    public function getDecrypted() {
        $text = $this->mParsedText;
        foreach ($this->mContent as $rand => $cnt) {
            $mTags = $cnt[0];
           foreach ($cnt[2] as $tag => $value) {
                $mTags .= " $tag=\"$value\"";
            }
            $text = str_replace($rand,
        			"<" . $mTags . ">"				.
		    		      $this->DecryptContent($rand, $cnt[1])	.
                    		    "</protect>",
                		$text);
        }
        return $text;
    }
    
    /**
     * Checks if the currently edited page contains protect-tags
     * @return true if Page is protected.
     */
    public function isProtected() {
        if (count($this->mContent) != 0) {
            return true;
        }
        return false;
    }
   
    /**
     * Retrieves text of this object with all protect-tags beeing
     * encrypted before.
     * @param userName a name of a current user
     * @return Text with encrypted tags.
     */
    public function getEncrypted($userName)
    {
        require_once("ProtectTag.php");
        $text = $this->mParsedText;
        foreach ($this->mContent as $rand => $cnt) {
    	    $ary_users = null;
    	    $ary_groups = null;
    	    if (array_key_exists('users', $cnt[2])) {
    		$ary_users = $cnt[2]['users'];
    	    }
    	    if (array_key_exists('groups', $cnt[2])) {
    		$ary_groups = $cnt[2]['groups'];
    	    }
    	    $access = new AccessList($ary_users, $ary_groups);
            $access->AddUser($userName);
            
            $tag = new ProtectTag();
            $tag->setAccessList($access);
            
            if (array_key_exists('show', $cnt[2])) {
        	$tag->setShow($cnt[2]['show']);
    	    }
	    if (array_key_exists('cipher', $cnt[2])) {
		$tag->setCipher($cnt[2]['cipher']);
	    }
            if (array_key_exists('errorpage', $cnt[2])) {
        	$tag->setErrorPage($cnt[2]['errorpage']);
    	    }
            $text = str_replace($rand,
                $tag->getStart()."\n"
                    .$this->mEnc->Encrypt($cnt[1], $tag->mCipher)."\n"
                    .$tag->getEnd(),
                $text);
        }
    
        return $text;
    }
}

?>
