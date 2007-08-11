<?php

/**
 * CipherSuite allows to do the following operations:
 *     - register and initialize supported cryptographic engines
 *     - register supported algorithms and associate them with their engines
 *     - get a list of supported ciphers for an engine
 *     - call a basic cryptographic routines using cipher's identifier
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

require_once("PageProtectionSettings.php");

/**
 * Handles information about
 * algorithms, which are available
 * and routines hooks, which can be invoked.
 *
 * See Cipher class, which extends it and
 * is more friendly to use.
 */
class CipherSuite{
    protected $mErrorText;
    private $mEngines;
    private $mAllCiphers;
    private $mEncryptionCiphers;

    /**
     * Registers cryptographic engines.
     */
    protected function register_engines()
    {
	global $CipherSuiteHooks;

	foreach ($CipherSuiteHooks as $engine => $functions) {
	    $this->mEngines[$engine] = $functions;
	}
    }

    /**
     * Registers ciphers and key/IV sizes.
     */
    protected function register_ciphers()
    {
	foreach ($this->mEngines as $engine => $functions) {
	    $ciphersets = $this->call_engine_func($engine, 'get_ciphers_params');
	    foreach ($ciphersets as $cipherset) {
		list($cipher, $key_size, $iv_size) = $cipherset;
		$this->mAllCiphers[$cipher]['engine'] = $engine;
		$this->mAllCiphers[$cipher]['key_size'] = $key_size;
		$this->mAllCiphers[$cipher]['iv_size'] = $iv_size;
	    }
	}
    }

    /**
     * Registers preferred ciphers used to encryption.
     */
    protected function register_preferred_ciphers()
    {
	global $PageProtectionPreferredCiphers;

	foreach($PageProtectionPreferredCiphers as $k => $v) {
	    if (isset($PageProtectionPreferredCiphers[$k]) &&
		      $PageProtectionPreferredCiphers[$k] === true) {
		if ( array_key_exists($k, $this->mAllCiphers) ) {
		    $this->mEncryptionCiphers[$k] = $this->mAllCiphers[$k];
		}
	    }
	}
    }


    /**
     * Initialize engines.
     */
    protected function initialize_engines()
    {
	global $PageProtectionRSAKeySize;

	foreach ($this->mEngines as $engine => $functions) {
	    $this->call_engine_func($engine, 'init');
	    if ($this->mErrorText) {
		throw new Exception($this->mErrorText);
		return false;
	    }
	}
	return true;
    }

    /**
     * Initialization.
     */
    function CipherSuite()
    {
	$this->mErrorText = false;
	$this->register_engines();
	$this->initialize_engines();
	$this->register_ciphers();
	$this->register_preferred_ciphers();
    }

    /**
     * Tells whether the given engine was detected.
     * @return TRUE when an engine is usable, FALSE if not.
     */
    public function have_engine($engine)
    {
	return array_key_exists($engine, $this->mEngines);
    }

    /**
     * Tells whether the given cipher is usable for encryption.
     * @return TRUE when it is usable, FALSE if not.
     */
    public function have_enc_cipher($cipher)
    {
	if ($cipher === '' || $cipher === false) {
	    return false;
	}
	return array_key_exists($cipher, $this->mEncryptionCiphers);
    }

    /**
     * Tells whether the given cipher is usable.
     * @return TRUE when it is usable, FALSE if not.
     */
    public function have_cipher($cipher)
    {
	if ($cipher === '' || $cipher === false) {
	    return false;
	}
	return array_key_exists($cipher, $this->mAllCiphers);
    }

    /**
     * Lists cryptographic engines.
     * @return Array containing engines' identifiers.
     */
    public function list_engines()
    {
	return array_keys($engine, $this->mEngines);
    }

    /**
     * Obtains the most preferred and available symmetric cipher.
     * @return Cipher identifier compliant to MCRYPT naming convention.
     */
    public function get_preferred_cipher()
    {
	$k = array_keys($this->mEncryptionCiphers);
	return $k[0];
    }

    /**
     * Obtains the optimal key's length for a given cipher.
     * @param cipher_id string containing cipher identifier.
     * @return Length of a cipher's key in bytes.
     */
    public function key_size($cipher_id)
    {
	return $this->mAllCiphers[$cipher_id]['key_size'];
    }

    /**
     * Obtains the key's length for a given cipher and keypair.
     * @param cipher_id string containing cipher identifier.
     * @param keypair string containing key pair in PEM format.
     * @return Length of a cipher's key in bytes.
     */
    public function asymmetric_key_size($keypair='', $cipher_id='rsa')
    {
	return $this->call_cipher_func($cipher_id, 'get_key_size', $keypair);
    }

    /**
     * Obtains the cipher's type.
     * @param cipher_id string containing cipher identifier.
     * @return Cipher's type.
     */
    public function type($cipher_id)
    {
	if ($cipher_id === false ||
	    $cipher_id === '') {
	    return false;
	}
	$engine = $this->mAllCiphers[$cipher_id]['engine'];
	return $this->mEngines[$engine]['type'];
    }

    /**
     * Obtains cipher's engine for a given cipher.
     * @param cipher_id string containing cipher identifier.
     * @return Engine name.
     */
    public function engine($cipher_id)
    {
	return $this->mAllCiphers[$cipher_id]['engine'];
    }

    /**
     * Obtains engine's URL.
     * @param engine string containing engine identifier.
     * @return URL string or string containing space.
     */
    public function engine_url($engine)
    {
	if ( !array_key_exists($engine, $this->mEngines) ) {
	    return ' ';
	}
	if ( !array_key_exists('url', $this->mEngines[$engine]) ) {
	    return ' ';
	}
	return $this->mEngines[$engine]['url'];
    }

    /**
     * Obtains the IV's length for a given cipher.
     * @param cipher_id string containing cipher identifier
     * @return Length of a cipher's Initialization Vector in bytes
     */
    public function iv_size($cipher_id)
    {
	return $this->mAllCiphers[$cipher_id]['iv_size'];
    }

    /**
     * Obtains the name of a given cipher in uppercase.
     * @param cipher_id string containing cipher identifier
     * @return Name of a cipher (which you can see in the header) or False if cipher is unknown
     */
    public function keyname($cipher_id)
    {
    	if ( isset($this->mAllCiphers[$cipher_id]) ) {
	    return strtoupper($cipher_id);
	}
	return false;
    }

    /**
     * List ciphers' identifiers.
     * @return Array containng list of all known ciphers' identifiers.
     */
    public function list_ciphers($engine='')
    {
	$tabik = array();
	if ($engine === '') {
	    foreach ($this->mAllCiphers as $k => $v) {
		array_push($tabik,$k);
	    }
	    return $tabik;
	} else {
	    if ($this->mAllCiphers[$k]['engine'] === $engine) {
		array_push($tabik,$k);
	    }
	    return $tabik;
	}
    }

    /**
     * List ciphers' identifiers used for encryption and marked in PreferredCiphers.php as usable.
     * @return Array containng list of all ciphers' identifiers, which can be used to encrypt data.
     */
    public function list_usable_ciphers($engine='')
    {
	$tabik = array();
	if ($engine === '') {
	    foreach ($this->mEncryptionCiphers as $k => $v) {
		array_push($tabik,$k);
	    }
	    return $tabik;
	} else {
	    foreach ($this->mEncryptionCiphers as $k => $v) {
		if ($this->mEncryptionCiphers[$k]['engine'] === $engine) {
		    array_push($tabik,$k);
		}
	    }
	    return $tabik;
	}
    }

    /**
     * Correct the given cipher's name (uses list of usable ciphers).
     * @param Cipher cipher's identifier
     * @return String containng cipher's name in correct case or FALSE if the give cipher is not usable.
     */
    public function correct_cipher_name($cipher)
    {
	$tabik = array();
	$cipher = trim(strtolower(str_replace('_', '-', $cipher)));
	if (isset($this->mEncryptionCiphers[$cipher])) {
	    return $cipher;
	}
	return false;
    }

    /**
     * Calls crypto-engine's function by its name.
     * @param Engine is engine name.
     * @param Function routine name.
     * @param ... routine's arguments.
     * @return Function's result or FALSE when engine or function wasn't found.
     */
    protected function call_engine_func($engine, $function)
    {
	if ( !array_key_exists($engine, $this->mEngines) ) {
	    $this->mErrorText = 'CipherSuite: not supported crypto-engine: ' . $engine;
	    throw new Exception($this->mErrorText);
	    return false;
	}

	if ( !array_key_exists($function, $this->mEngines[$engine]) ) {
	    $this->mErrorText = 'CipherSuite: crypto-engine ' . $engine . ' has not function called ' . $function;
	    throw new Exception($this->mErrorText);
	    return false;
	}

	$args = func_get_args();
	array_shift($args);
	array_shift($args);
	$ret = call_user_func_array($this->mEngines[$engine][$function], $args);
	if (array_key_exists('error', $this->mEngines[$engine])) {
	    $err = call_user_func_array($this->mEngines[$engine]['error'], $args);
	    if ($err !== false && $err != '') {
		$this->mErrorText = 'CipherSuite: error while calling routine '		.
				    $function . ' for crypto-engine ' . $engine . ': '	.
				    $err;
		throw new Exception($this->mErrorText);
	    }
	}
	return $ret;
    }

    /**
     * Calls crypto-engine's function for the given cipher.
     * @param cipher a cipher name.
     * @param function a routine name.
     * @param ... routine's arguments.
     * @return Function's result or FALSE when cipher, engine or function wasn't found.
     */
    public function call_cipher_func($cipher, $function)
    {
	if ( !array_key_exists($cipher, $this->mAllCiphers) ) {
	    $this->mErrorText = 'CipherSuite: not supported cipher: ' . $cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}
	if ( !array_key_exists('engine', $this->mAllCiphers[$cipher]) )	{
	    $this->mErrorText = 'CipherSuite: crypto-engine not found for cipher: ' . $cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}
	
	$engine = $this->mAllCiphers[$cipher]['engine'];
	if ( !array_key_exists($engine, $this->mEngines) ) {
	    $this->mErrorText = 'CipherSuite: crypto-engine not registered for cipher: ' . $cipher;
	    throw new Exception($this->mErrorText);
	    return false;
	}
	if ( !array_key_exists($function, $this->mEngines[$engine]) ) {
	    $this->mErrorText = 'CipherSuite: routine ' . $function . ' not registered for crypto-engine ' . $engine;
	    throw new Exception($this->mErrorText);
	    return false;
	}

	$args = func_get_args();
	array_shift($args);
	array_shift($args);
	$ret = call_user_func_array($this->mEngines[$engine][$function], $args);
	if (array_key_exists('error', $this->mEngines[$engine])) {
	    $err = call_user_func_array($this->mEngines[$engine]['error'], $args);
	    if ($err !== false && $err != '') {
		$this->mErrorText = 'CipherSuite: error while calling routine '		.
				    $function . ' for crypto-engine ' . $engine . ': '	.
				    $err;
		throw new Exception($this->mErrorText);
	    }
	}
	
	return $ret;
    }

    /**
     * Checks whether last operation returned a text containing error message and resets the message.
     * @return Error text message or FALSE.
     */
    public function get_last_error()
    {
	$etxt = $this->mErrorText;
	$this->mErrorText = false;
	return $etxt;
    }
}

?>
