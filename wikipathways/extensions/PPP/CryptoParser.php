<?php

/**
 * CryptoParser allows to do the following operations:
 *     - generate a header (containing keys' parameters and an encrypted key)
 *     - fetch a header from message
 *     - fetch a header and an encrypted message from an ascii-armored cryptogram
 *
 * PHP version 5
 *
 * @category   Encryption
 * @package    PageProtectionPlus
 * @author     Pawel Wilk <pw@gnu.org>
 * @copyright  2006, 2007 Pawel Wilk
 * @license    http://www.gnu.org/licenses/gpl.html  General Public License version 2 or higher
 * @version    2.1b
 * @link       http://meta.wikimedia.org/PPP
 */

define ('BEGIN_KEY_PREFIX', '--- BEGIN KEY:');
define ('BEGIN_KEY_SUFFIX', '---');
define ('END_KEY', '--- END KEY ---');

define ('HEADER_CORRUPTED', 0);
define ('HEADER_NOT_FOUND', 1);
define ('HEADER_NATIVE', 2);

define ('PARAMETER_SEPARATOR', ',');

/**
 * Trims white characters and removes separator in the given variable.
 */
function trim_param(&$value)
{
    $value = trim( strtr ($value, array(PARAMETER_SEPARATOR=>'') ) );
}

/**
 * Makes all characters lower-case in the given variable.
 */
function tolower_value(&$value)
{
    $value = strtolower($value);
}

/**
 * Provides headers parsing functions.
 */
class CryptoParser {
    public $mTextpartCipherName;
    public $mTextpartCipherMode;
    public $mTextpartKeyData;
    public $mTextpartArmoredKeyData;
    public $mTextpartKeySize;
    public $mTextpartIVSize;
    public $mKeypartCipherName;
    public $mKeypartCipherKeyID;
    public $mHeaderType;
    public $mHeadLine;
    public $mHeader;
    public $mText;
    public $mArmoredText;
    
    protected $mRemoveWhite;
    protected $mRemoveNotB64;


    /**
     * Initializes parser.
     */
    public function CryptoParser()
    {
	$this->mTextpartCipherName	= false;
	$this->mTextpartCipherMode	= false;
	$this->mTextpartKeyData		= false;
	$this->mTextpartArmoredKeyData	= false;
        $this->mTextpartKeySize		= false;
        $this->mTextpartIVSize		= false;
        $this->mKeypartCipherName	= false;
	$this->mKeypartCipherKeyID	= false;
        $this->mHeadLine		= false;
        $this->mParameters		= false;
        $this->mText			= false;
        $this->mArmoredText		= false;
        $this->mHeader			= false;
        $this->mHeaderType		= HEADER_NOT_FOUND;

        $this->mRemoveWhite		= array("\r"	=> '',
    						"\t"	=> '',
    						" "	=> '',
    						"\0"	=> '');
	$this->mRemoveNotB64		= $this->mRemoveWhite;
	$this->mRemoveNotB64["\n"]	= '';
    }

    /**
     * Husks header from an ascii-armored text and sets the corresponding members.
     * @param Text text containing header.
     * @return true on success, false on errors and sets mHeaderType member.
     */
    public function LoadCryptogram($text)
    {
	$text = ltrim($text);
	$from_headline = strpos($text, BEGIN_KEY_PREFIX);
	if ($from_headline !== 0) {
	    $from_headline = strpos(ltrim($text), '--- BEGIN');
	    if ($from_headline === 0) {
		$this->mHeaderType	= HEADER_CORRUPTED;
		throw new Exception("Header corrupted: cannot find a proper header in: |" .
				     substr($text, 0, 64) . "|");
		return false;
	    }
	    $this->mHeaderType		= HEADER_NOT_FOUND;
	    $this->mArmoredText		= $text;
	    $this->mText		= base64_decode(strtr($this->mArmoredText, $this->mRemoveNotB64));
	    $this->mTextpartArmoredKeyData = false;
	    $this->mTextpartKeyData	   = false;
	    return $this->mHeaderType;
	}

	$to_headline = strpos($text, BEGIN_KEY_SUFFIX, strlen(BEGIN_KEY_PREFIX)+4);
	if ($to_headline === false) {
	    $this->mHeaderType = HEADER_CORRUPTED;
	    throw new Exception("Header corrupted: cannot find end of headline in: |" .
				substr($text, 0, 64) . "|");
	    return false;
	}

	/* get parameters from headline */
	$parstart = $from_headline + strlen(BEGIN_KEY_PREFIX);
	$parameters = trim(substr($text, $parstart, ($to_headline-$parstart)));
	if (strpos($parameters, "\n") !== false) {
	    $this->mHeaderType = HEADER_CORRUPTED;
	    throw new Exception("Header corrupted: cannot find key parameters in: |" .
				trim(substr($text, $from_headline, ($to_headline-$from_headline))) . "|");
	    return false;
	}

	$this->mHeaderType = HEADER_NATIVE;
	$this->mHeadLine = trim(substr($text, $from_headline, ($to_headline - $from_headline + strlen(BEGIN_KEY_SUFFIX))));
	$parameters = strtolower($parameters);
	$this->mParameters = explode(PARAMETER_SEPARATOR, $parameters);
	if ( count($this->mParameters) !== 6 ) {
	    $this->mHeaderType = HEADER_CORRUPTED;
	    throw new Exception("Header corrupted: field count is " . count($this->mParameters) .
				" but 7 was expected in: |" . $parameters . "|");
	    return false;
	}
	//array_walk($this->mParameters, 'trim_param');
	list (	$this->mKeypartCipherName,
		$this->mKeypartCipherKeyID,
		$this->mTextpartCipherName,
		$this->mTextpartCipherMode,
		$this->mTextpartKeySize,
		$this->mTextpartIVSize ) = $this->mParameters;

	/* get an encrypted key+iv+salt */
	$from_key = $to_headline + strlen(BEGIN_KEY_SUFFIX);
	$to_key = strpos($text, END_KEY, $from_key);
	if ($to_key === false) {
	    $this->mHeaderType = HEADER_CORRUPTED;
	    throw new Exception("Header corrupted: cannot find symmetric key in: |" .
				substr($text, $from_key, 64) . "|");
	    return false;
	}
	$this->mTextpartArmoredKeyData = substr($text, $from_key, ($to_key-$from_key));
	$this->mTextpartKeyData = strtr($this->mTextpartArmoredKeyData, $this->mRemoveNotB64);
	$this->mTextpartKeyData = base64_decode($this->mTextpartKeyData);
	
	/* get a message body */
	if ($this->mTextpartCipherName === 'plain') {
	    $this->mText = substr($text, $to_key+strlen(END_KEY));
	} else {
	    $this->mArmoredText = substr($text, $to_key+strlen(END_KEY));
	    $this->mText = base64_decode(strtr($this->mArmoredText, $this->mRemoveNotB64));
	}

	/* get a header */
	$this->mHeader = trim(substr($text, 0, $to_key+strlen(END_KEY)));

	return true;
    }

    /**
     * Creates header for a given parameters and sets all corresponding members.
     * @param TextpartKeyData ascii-armored, ENCRYPTED symmetric key (in the Base64 format)
     * @param KeypartCipherName algorithm identifier used for key encryption
     * @param KeypartCipherKeyID identifier of a key used to encrypt key
     * @param TextpartCipherName algorithm identifier used for text encryption
     * @param TextpartCipherMode mode identifier used for text encryption
     * @param TextpartKeySize key size used for text encryption
     * @param TextpartIVSize IV size used for text encryption
     * @return returns header string on success or false on errors.
     */
    public function CreateHeader()
    {
	$this->mText = false;
	$this->mHeader = false;
	if (func_num_args() !== 7) {
	    $this->mHeaderType = HEADER_NOT_FOUND;
	    throw new Exception("Wrong count of arguments when calling CreateHeader()");
	    return false;
	}

	/* fetch parameters */
	$this->mParameters = func_get_args();
	$this->mTextpartArmoredKeyData = array_shift($this->mParameters);
	$this->mTextpartKeyData = strtr($this->mTextpartArmoredKeyData, $this->mRemoveNotB64);
	$this->mTextpartKeyData = base64_decode($this->mTextpartArmoredKeyData);
	array_walk($this->mParameters, 'trim_param');
	array_walk($this->mParameters, 'tolower_value');
    	list (	$this->mKeypartCipherName,
		$this->mKeypartCipherKeyID,
		$this->mTextpartCipherName,
		$this->mTextpartCipherMode,
		$this->mTextpartKeySize,
		$this->mTextpartIVSize ) = $this->mParameters;

	/* create headline */
	$parameters = $this->mParameters;
	$parameters[0] = strtoupper($parameters[0]);
	$parameters[2] = strtoupper($parameters[2]);
	$parameters[3] = strtoupper($parameters[3]);
	$this->mHeadLine =  BEGIN_KEY_PREFIX . " "			.
			    implode(PARAMETER_SEPARATOR, $parameters)	.
			    " " . BEGIN_KEY_SUFFIX;

	/* create header */
	$this->mHeader = $this->mHeadLine		. "\n\r" .
			 $this->mTextpartArmoredKeyData	. "\n\r" .
			 END_KEY;

	$this->mHeaderType = HEADER_NATIVE;
	return $this->mHeader;
    }

    /**
     * Creates header and text in the Base64 for a given parameters and sets all corresponding members.
     * @param Text text to be encrypted (not encoded in any way)
     * @param TextpartKeyData ENCRYPTED symmetric key[+iv+salt] (binary format)
     * @param KeypartCipherName algorithm identifier used for key encryption
     * @param KeypartCipherKeyID identifier of a key used to encrypt key
     * @param TextpartCipherName algorithm identifier used for text encryption
     * @param TextpartCipherMode mode identifier used for text encryption
     * @param TextpartKeySize key size used for text encryption
     * @param TextpartIVSize IV size used for text encryption
     * @return returns ascii-armored text containing a header on success or false on errors.
     */
    public function CreateCryptogram($text, $TextpartKeyData, $KeypartCipherName, $KeypartCipherKeyID,
				     $TextpartCipherName, $TextpartCipherMode, $TextpartKeySize,
				     $TextpartIVSize)
    {
	$TextpartArmoredKeyData = '';

	if ($TextpartKeyData !== false && $TextpartKeyData !== '') {
	    $TextpartArmoredKeyData = trim(chunk_split(base64_encode($TextpartKeyData)));
	}

	$this->CreateHeader ($TextpartArmoredKeyData, $KeypartCipherName, $KeypartCipherKeyID,
			     $TextpartCipherName, $TextpartCipherMode, $TextpartKeySize,
			     $TextpartIVSize);

	if ($this->mHeaderType !== HEADER_NATIVE) {
	    return false;
	}

	$this->mText = $text;
	$this->mArmoredText = trim(chunk_split(base64_encode($text)));

	return $this->mHeader . "\n\r" . $this->mArmoredText . "\n\r";
    }
}

?>
