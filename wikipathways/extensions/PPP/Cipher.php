<?php

/**
 * Cipher allows to do the following operations:
 *     - encrypt a text using one of the algorithms delivered by CipherSuite class
 *     - decrypt a text using one of the algorithms delivered by CiphreSuite class
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

require_once("CipherSuite.php");
require_once("engines/RandKeyGen.php");

/**
 * Handles encryption and decryption.
 */
class Cipher extends CipherSuite {
    private $mCipher;
    private $mKey;
    private $mIV;
    public $mLastKey;
    public $mLastIV;
    public $mLastKeySize;
    public $mLastIVSize;
    public $mLastCipher;

    /**
     * Constructor.
     */
    function Cipher($cipher='', $key='', $iv='')
    {
	$this->mErrorText = false;
	$this->mCipher = '';
	$this->mKey = '';
	$this->mIV = '';
	$this->mLastKey = '';
	$this->mLastIV = '';
	$this->mLastCipher = '';
	$this->mLastKeySize = 0;
	$this->mLastIVSize = 0;


	$this->CipherSuite();

	if ($cipher !== '') {
	    if (!$this->SetCipher($cipher)) {
		return $this;
	    }
	}
	if ($key !== '') {
	    if (!$this->SetKey($key)) {
	    	return $this;
	    }
	}
	if ($iv !== '') {
	    if (!$this->SetIV($iv)) {
	    	return $this;
	    }
	}
    }

    /**
     * Verifies cipher's identifier used to encrypt/decrypt data.
     * @param Cipher cipher's identifier.
     * @param For_encrypt set it to true to check the give cipher name against only encryption ciphers (default is false).
     * @return Returns true on success or false when the given cipher is unknown or not cupported in encryption mode.
     */
    protected function VerifyCipher($cipher,$for_encrypt=false)
    {
	if ($for_encrypt === true && $this->have_cipher($cipher)) {
	    if (!$this->have_enc_cipher($cipher) ) {
	        $this->mErrorText = 'cipher ' . $cipher . ' is not supported for the encryption';
		throw new Exception($this->mErrorText);
	        return false;
	    } else {
	        return true;
	    }
	}

	if ( !$this->have_cipher($cipher) ) {
	    $this->mErrorText = 'unknown cipher: ' . $cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}

	return true;
    }

    /**
     * Sets cipher used to encrypt/decrypt data. Additionaly it checks compliance with key and IV sizes.
     * @param Cipher sets the default cipher by its name or sets most preferred if not given.
     * @param For_encrypt set it to true to check the give cipher name against only encryption ciphers (default is false).
     * @return Returns true on success or false when the given cipher is unknown.
     */
    public function SetCipher($cipher='', $for_encrypt=false)
    {
	if ($cipher === '') {
	    $cipher = $this->get_preferred_cipher();
	} else {
	    $cipher = $this->correct_cipher_name($cipher);
	    if ( !$this->VerifyCipher($cipher, $for_encrypt) ) {
		$this->mErrorText = 'Cipher::SetCipher(): ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
		return false;
	    }
	}

	if ($key !== false && $key !== '' &&
	    $iv !== false && $iv !== '') {
	    $key_size = 0;
	    $iv_size = 0;
	    $this->validate_sizes('SetCipher', $cipher, $key, $iv, $key_size, $iv_size);
	}

	$this->mCipher = $cipher;
	return true;
    }

    /**
     * Sets key used to encrypt/decrypt data.
     * @param Key key or empty string (which means to generate random key).
     * @return Returns true on success or false when key size is wrong.
     */
    public function SetKey($key='')
    {
        if ($this->mCipher === '') {
    	    $cipher = $this->get_preferred_cipher();
        } else {
    	    $cipher = $this->mCipher;
    	}

	$key_size = 0;
	$iv_size = 0;
	if ($key !== '') {
	    if ($this->mCipher !== '') {
		$key_size = 0;
		$iv_size = 0;
		$this->validate_sizes('SetCipher', $cipher, $key, $iv, $key_size, $iv_size);
	    }
	    $this->mKey = $key;
	    return true;
	} else {
	    $this->mKey = RandKeyGen($key_size);
	    return true;
        }
    }

    /**
     * Sets IV used to encrypt/decrypt data.
     * @param IV initialization vactor or empty string (which means to generate random IV).
     * @return Returns true on success or false when IV size is wrong.
     */
    public function SetIV($iv='')
    {
        if ($this->mCipher === '') {
    	    $cipher = $this->get_preferred_cipher();
        } else {
    	    $cipher = $this->mCipher;
    	}   

	if ($key !== '') {
	    if ($this->mCipher !== '') {
		$key_size = 0;
		$iv_size = 0;
    		$this->validate_sizes('SetIV', $cipher, $key, $iv, $key_size, $iv_size);
	    }
	    $this->mIV = $iv;
	    return true;
	} else {
	    $this->mKey = RandKeyGen($liked_iv_size);
	    return true;
        }
    }

    /**
     * Encrypts portion of a data.
     * @param Text data to be encrypted.
     * @param Cipher cipher to use (if empty, then the already set default will be used or default)
     * @param Key key for the cipher (if empty, the random key will be generated and stored into Cipher::mLastKey)
     * @param IV initialization vector to use (if empty, the random will be picked up and stored into Cipher::mLastIV)
     * @return encoded text or false - see also Cipher::last_error()
     */
    public function Encipher($text, $cipher='', $key='', $iv='')
    {
	if ($cipher === '') {
	    if ($this->mCipher !== '') {
		$cipher = $this->mCipher;
	    } else {
		$cipher = $this->get_preferred_cipher();
	    }
	}
	$cipher = $this->correct_cipher_name($cipher);
	if ( !$this->VerifyCipher($cipher, true) ) {
		$this->mErrorText = 'Cipher::Encipher(): ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
		return false;
	}

	/* caution: key and IV sizes may be smaller! */
	$key_size = 0;
	$iv_size = 0;
	$this->validate_sizes('Encipher', $cipher, $key, $iv, $key_size, $iv_size);
	if ($this->type($cipher) === 'symmetric') {
	    if ($key === '' && $this->mKey !== '') {
		$key = $this->mKey;
	    } else {
		$key = RandKeyGen($key_size);
	    }
	    if ($iv === '' && $this->mIV !== '') {
	        $iv = $this->mIV;
	    } else {
		$iv = RandKeyGen($iv_size);
	    }
	    $this->validate_sizes('Encipher', $cipher, $key, $iv, $key_size, $iv_size);
	}

	$this->mLastKey = $key;
	$this->mLastIV = $iv;
	$this->mLastKeySize = $key_size;
	$this->mLastIVSize = $iv_size;
	$this->mLastCipher = $cipher;

/*	$ret = $this->call_cipher_func($cipher, 'init');
	if ($ret === false) {
	    if ($this->mErrorText !== false && $this->mErrorText != '') {
	        $this->mErrorText = 'Cipher: error while initializing: ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
	    }
	    throw new Exception("Cipher: internal error while initializing");
	    return false;
	}
*/

	$text = $this->call_cipher_func($cipher, 'encrypt', $cipher, $key, $text, $iv);
	if ($text === false) {
	    if ($this->mErrorText !== false && $this->mErrorText != '') {
		$this->mErrorText = 'Cipher: error while encrypting: ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
	    }
	    throw new Exception("Cipher: internal error while encrypting");
	}

	return $text;
    }

    /**
     * Decrypts portion of a data.
     * @param Text data to be encrypted.
     * @param Cipher cipher to use (if undefined the previously set cipher will be used).
     * @param Key key for the cipher (if undefined the previously set key will be used).
     * @param IV IV to use; if undefined (ok for some ciphers) the empty string will be passed to the engine, but only if the key was empty too.
     * @return decoded text or false - see also Cipher::last_error()
     */
    public function Decipher($text, $cipher='', $key='', $iv='')
    {
	if ($cipher === '' || $cipher === false) {
	    if ($this->mCipher === '') {
		$this->mErrorText = 'Cipher::Decipher(): error while decrypting: cipher not specified';
		throw new Exception($this->mErrorText);
		return false;
	    } else {
		$cipher = $this->mCipher;
	    }
	}
	if ($key === '' || $key === false) {
	    if ($this->mKey === false) {
		$this->mErrorText = 'Cipher::Decipher(): error while decrypting: key not specified';
		throw new Exception($this->mErrorText);
		return false;
	    } else {
		$key = $this->mKey;
		if ($iv === '' || $iv === false) {
		    $iv = $this->mIV;
		}
	    }
	}

	$cipher = $this->correct_cipher_name($cipher);
	if ( !$this->VerifyCipher($cipher, true) ) {
		$this->mErrorText = 'Cipher::Decipher(): ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
		return false;
	}

	$this->validate_sizes('Decipher', $cipher, $key, $iv, $key_size, $iv_size);

	$text = $this->call_cipher_func($cipher, 'decrypt', $cipher, $key, $text, $iv);
	if ($text === false) {
	    if ($this->mErrorText !== false && $this->mErrorText != '') {
		$this->mErrorText = 'Cipher: error while decrypting: ' . $this->mErrorText;
		throw new Exception($this->mErrorText);
	    }
	}

	return $text;	
    }

    protected function validate_sizes($name, $cipher, $key, $iv, &$key_size, &$iv_size)
    {
	$iv_size = strlen($iv);
	if ($cipher === false || $cipher === '') {
	    $cipher = $this->get_preferred_cipher();
	}
	
	$liked_iv_size = $this->iv_size($cipher);
	if ($iv === false || $iv === '') {
	    $iv_size = $liked_iv_size;
	}
	if ($this->type($cipher) === 'asymmetric') {
	    $key_size = $this->asymmetric_key_size($key);
	    $liked_key_size = $this->asymmetric_key_size();
	} else {
	    $key_size = strlen($key);
	    $liked_key_size = $this->key_size($cipher);
	}
    
	if ($key === false || $key === '') {
	    $key_size = $liked_key_size;
	}

	if ($key !== '' && $key_size > $liked_key_size) {
	    $this->mErrorText = 'Cipher::' . $name	.
				' given key size '	.
				$key_size		.
				' is bigger than ' 	.
				$liked_key_size		.
				' for the alghoritm '	.
				$cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}
	if ($iv !== '' && $iv_size > $liked_iv_size) {
	    $this->mErrorText = 'Cipher::' . $name	.
				' given IV size '	.
				$iv_size		.
				' is bigger than ' 	.
				$liked_iv_size	 	.
				' for the alghoritm '	.
				$cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}
    }

    /**
     * Checks whether last operation returned a text containing error message.
     * @return True when error message was generated or false if not.
     */
    public function HasError()
    {
	if ($this->mErrorText !== false) {
	    return true;
	}
	return false;
    }
}

?>
