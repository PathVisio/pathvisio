<?php

/**
 * Encryption allows to do the following operations:
 *     - encrypt pages and/or sections
 *     - decrypt pages and/or sections
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

require_once("Cipher.php");
require_once("KeyHeap.php");
require_once("CryptoParser.php");

require_once("engines/RandKeyGen.php");

/**
 * Handles encryption.
 */
class Encryption extends Cipher
{
    private $mPEMKeys;
    private $mParser;

    /**
    * Constructor. Prepares ciphersuite, parser
    * and reads keypairs from files.
    */
    function Encryption()
    {
	$this->Cipher();
	$this->mPEMKeys = new KeyHeap('', $this);
	$this->mParser = new CryptoParser();
    }

    /**
    * Decrypts a text according to its headers.
    * @param text Encrypted text.
    * @return Plaintext.
    */
    public function Decrypt($text) {
	$key_id = false;

	$this->mParser->LoadCryptogram($text);
	switch ($this->mParser->mHeaderType) {
	    case HEADER_CORRUPTED:
		return false;

	    case HEADER_NOT_FOUND:
		$keypair = $this->mPEMKeys->GetKeyPair();
	        return $this->Decipher($this->mParser->mText, 'rsa', $keypair);

	    case HEADER_NATIVE:
		if ($this->type($this->mParser->mTextpartCipherName) === 'symmetric') {
		    $key_id  = $this->mParser->mKeypartCipherKeyID;
		    $keypair = $this->mPEMKeys->GetKeyPair($key_id);
		    if ($this->mParser->mTextpartCipherMode !== 'cbc') {
			throw new Exception("Decrypt: Unsupported symmetric cipher mode: " . 
						$this->mParser->mTextpartCipherMode);
			return false;
		    }
		    $keydata = $this->Decipher (
				$this->mParser->mTextpartKeyData,
				$this->mParser->mKeypartCipherName,
				$keypair
				);
		    $key = substr($keydata, 0, $this->mParser->mTextpartKeySize);
		    $iv  = substr($keydata, $this->mParser->mTextpartKeySize, $this->mParser->mTextpartIVSize);
		    return $this->Decipher (
				$this->mParser->mText,
				$this->mParser->mTextpartCipherName,
				$key,
				$iv
				);
		} else {
		    /* pure-RSA encoded text */
		    $keypair_id  = $this->mParser->mKeypartCipherKeyID;
		    $keypair = $this->mPEMKeys->GetKeyPair($keypair_id);
		    return $this->Decipher (
				$this->mParser->mText,
				$this->mParser->mTextpartCipherName,
				$keypair);
		}
	    }
	throw new Exception("Decrypt: unknown header type");
	return false;
    }

    /**
    * Encrypts a text using given cipher or default.
    * @param text Plaintext.
    * @return Encrypted text with RSA-encrypted key in the header.
    */
    function Encrypt($text, $cipher='')
    {
	if ($cipher !== '') {
	    $cipher = $this->correct_cipher_name($cipher);
	    if ( !$this->VerifyCipher($cipher, true) ) {
		$cipher = '';
	    }
	}

	$sym_mode = 'cbc';
	/* check whether it is symmetric cipher */
	if ($cipher === '' || $this->type($cipher) !== 'asymmetric') {
	
	    /* encrypt text using choosen cipher */
	    $text = $this->Encipher($text, $cipher);
	    if (!$text) {
		return false;
	    }

	    $salt = sha1(base64_encode(microtime()) . mt_rand(0,111111));
	    $salt = substr($salt, 0, mt_rand(7,35));
	    $keypart_data	= $this->mLastKey . $this->mLastIV . $salt;
	    $textpart_cipher	= $this->mLastCipher;
	    $textpart_key_size	= $this->mLastKeySize;
	    $textpart_iv_size	= $this->mLastIVSize;

	    /* encrypt symmetric key using asymmetric cipher */
	    $key_pair		= $this->mPEMKeys->GetDefaultKeyPair();
	    $key_pair_id	= $this->mPEMKeys->GetDefaultKeyID();
	    $keypart_data	= $this->Encipher($keypart_data, 'rsa', $key_pair);
	    $keypart_cipher	= $this->mLastCipher;

	    /* create cryptogram */
	    if (!$key_pair) { throw new Exception("Encrypt: no asymmetric key found"); }
	    if (!$keypart_data) { throw new Exception("Encrypt: symmetric key encryption error"); }
	}

	/* check whether it's a pure-asymmetric cipher */
	if ($this->type($cipher) === 'asymmetric') {
	    $sym_mode		= '';
	    $keypart_data	= '';
	    $keypart_cipher	= 'NULL';
	    $textpart_cipher	= 'rsa';
	    $key_pair		= $this->mPEMKeys->GetLiteKeyPair();
	    $key_pair_id	= $this->mPEMKeys->GetLiteKeyID();
	    $text		= $this->Encipher($text, $textpart_cipher, $key_pair);
	    if (!$text) { return false; }
	    $textpart_key_size	= $this->mLastKeySize;
	    $textpart_iv_size	= $this->mLastIVSize;
	}

	$ret = $this->mParser->CreateCryptogram($text, $keypart_data, $keypart_cipher, $key_pair_id,
						$textpart_cipher, $sym_mode, $textpart_key_size,
						$textpart_iv_size);

	return $ret;
    }
}

?>
