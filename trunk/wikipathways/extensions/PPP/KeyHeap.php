<?php

/**
 * KeyHeap allows to do the following operations:
 *     - store asymmetric keys in memory
 *     - get the asymmetric key identified by key id
 *     - create needed directories if they don't exit
 *     - generate needed keys (default, lite) if they don't exit
 *     - copy previous key to keys' subdirectory for compatibility reasons
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
require_once("CipherSuite.php");

require_once("LocalSettings.php");

/**
 * Manages the heap of keys and takes care
 * abous files and directories.
 */
class KeyHeap {
    private $mCS;
    private $mDir;
    private $mKeyPairs;
    private $mKeyPairsizes;
    private $mDefaultKeyID;
    private $mLiteKeyID;
    private $mCompatKeyID;
    private static $mInitPaths = array();

    function KeyHeap($dir='', &$cipher_suite=false)
    {
	global $PageProtectionRSAKeyDir;

	/* set up or fetch the ciphersuite */
	if ($cipher_suite === false) {
	    $cipher_suite = new CipherSuite();
	}
	$this->mCS = &$cipher_suite;


    	/* set up the directory for storing keys */
	if ($dir === false || $dir === '') {
	    $dir = $PageProtectionRSAKeyDir;
	    if ($dir === false || $dir === '') {
		throw new Exception("KeyHeap: key directory is not set - aborting");
		return false;
	    }
	}

	/* set up the PEMs using cached defaults */
	$dir = $this->InitializeFiles($dir);
	$this->mCompatKeyID	= self::$mInitPaths[$dir]['compat_key_id'];
	$this->mLiteKeyID 	= self::$mInitPaths[$dir]['lite_key_id'];
	$this->mDefaultKeyID	= self::$mInitPaths[$dir]['default_key_id'];
	if ($this->mDefaultKeyID === false) {
	    throw new Exception("KeyHeap: unknown ID of the default key");
	    return false;
	}
	if ($this->mLiteKeyID === false) {
	    $this->mLiteKeyID = $this->mDefaultKeyID;
	}

	/* read files and fill up the table */
	$this->mDir = $dir;
	$handle = opendir($dir);
	if (!$handle) {
	    if (!mkdir($dir)) {
		throw new Exception("KeyHeap: cannot create directory for storing keys - aborting");
		return false;
	    }
	}
	$handle = opendir($dir);
	if (!$handle) {
	    throw new Exception("KeyHeap: cannot open directory for storing keys - aborting");
	    return false;
	}
	while (false !== ($file = readdir($handle))) {
          if ($file != "." && $file != ".." && substr($file,-4) === '.pem') {
            $file = $dir . "/" . $file;
            $pemdata = file_get_contents($file);
            $id = $this->readKeyID($pemdata);
            $this->mKeyPairs[$id] = $pemdata;
	    $this->mKeyPairsizes[$id] = $this->readKeySize($pemdata);
          }
	}
	closedir($handle);
    }

    /**
     * Initializes files and directories and puts needed pathnames
     * into the static caching array shared between object's instances.
     * @param Dir default directory for the heap of keys.
     * @return Returns the directory name for the heap of keys.
     */
    protected function InitializeFiles($dir)
    {
	global $wgPEMold;
	global $PageProtectionRSAKeyDir;
	global $PageProtectionRSAKeyFile;
	global $PageProtectionRSALiteKeyFile;
	global $PageProtectionRSAKeySize;
	global $PageProtectionRSALiteKeySize;
	global $PageProtectionRSACompatFile;

	/* check whether we have to repeat it more than once */
	if (array_key_exists($dir, self::$mInitPaths)) {
	    return $dir;
	}

	/* check for compatible key files */
	if (defined('RSA_PEM_FILE')) {
	    $compat_filename = RSA_PEM_FILE;
	} else {
	    if (isset($wgPEMold)	&&
		$wgPEMold !== false	&&
		$wgPEMold !== '') {
		$compat_filename = $wgPEMold;
	    } else {
		$compat_filename = 'private.pem';
	    }
	}
	
	/* read compatibility key pair from PEM file */
	self::$mInitPaths[$dir]['compat_filename'] = false;
	self::$mInitPaths[$dir]['compat_key'] = false;
	self::$mInitPaths[$dir]['compat_key_id'] = false;
	$compat_file_b = $dir . "/" . basename($PageProtectionRSACompatFile);
	if (isset($PageProtectionRSACompatFile)		&&
	    $PageProtectionRSACompatFile !== false	&&
	    $PageProtectionRSACompatFile !== ''		&&
	    !file_exists($compat_file_b)		&&
	    file_exists($compat_filename) ) {
	    $c = file_get_contents($compat_filename);
	    file_put_contents($compat_file_b, $c);
	}
	if (file_exists($compat_file_b)) {
	    $pemdata = file_get_contents($compat_file_b);
	    self::$mInitPaths[$dir]['compat_filename'] = $compat_file_b;
	    self::$mInitPaths[$dir]['compat_key'] = $pemdata;
	    self::$mInitPaths[$dir]['compat_key_id'] = $this->readKeyID($pemdata);
	}

	/* check for default key file */
	self::$mInitPaths[$dir]['default_filename'] = false;
	self::$mInitPaths[$dir]['default_key'] = false;
	self::$mInitPaths[$dir]['default_key_id'] = false;
	$default_file = $dir . "/" . basename($PageProtectionRSAKeyFile);
	$lite_file = $dir . "/" . basename($PageProtectionRSALiteKeyFile);
	if (!file_exists($default_file)) {
	    /* create new PEM file */
	    $pem = $this->mCS->call_cipher_func('rsa', 'create_pem', $PageProtectionRSAKeySize);
	    if (file_put_contents($default_file, $pem) === false) {
		throw new Exception("KeyHeap: cannot write the default key file to disk");
		return false;
	    }
	} else {
	    $pem = file_get_contents($default_file);
	    if ($pem === '' || $pem === false) {
		throw new Exception("KeyHeap: cannot see the default key in: $pem");
	    }
	}
	self::$mInitPaths[$dir]['default_filename'] = $default_file;
	self::$mInitPaths[$dir]['default_key'] = $pem;
	self::$mInitPaths[$dir]['default_key_id'] = $this->readKeyID($pem);
	
	/* check for default 'lite' key file */
	self::$mInitPaths[$dir]['lite_filename'] = false;
	self::$mInitPaths[$dir]['lite_key'] = false;
	self::$mInitPaths[$dir]['lite_key_id'] = false;
	if (!file_exists($lite_file)) {
	    /* create new PEM file for 'lite' key */
	    $pem = $this->mCS->call_cipher_func('rsa', 'create_pem', $PageProtectionRSALiteKeySize);
	    file_put_contents($lite_file, $pem);
	} else {
	    $pem = file_get_contents($lite_file);
	}
	if ($pem !== false) {
    	    self::$mInitPaths[$dir]['lite_filename'] = $lite_file;
    	    self::$mInitPaths[$dir]['lite_key'] = $pem;
	    self::$mInitPaths[$dir]['lite_key_id'] = $this->readKeyID($pem);
	}

	/* FIXME: set permissions according to UID, GID and ownership! */
	/* do it just after creating specific files and before writing */

	return $dir;
    }

    /**
     * Get the default pair of keys.
     * @return Returns the default key pair.
     */
    public function GetDefaultKeyPair()
    {
	return $this->mKeyPairs[$this->mDefaultKeyID];
    }

    /**
     * Get the default pair of 'lite' keys.
     * @return Returns the default key pair.
     */
    public function GetLiteKeyPair()
    {
	return $this->mKeyPairs[$this->mLiteKeyID];
    }

    /**
     * Get the ID of the default pair of keys.
     * @return Returns the ID.
     */
    public function GetDefaultKeyID()
    {
	return $this->mDefaultKeyID;
    }

    /**
     * Get the ID of the default pair of 'lite' keys.
     * @return Returns the ID.
     */
    public function GetLiteKeyID()
    {
	return $this->mLiteKeyID;
    }

    /**
     * Get the ID of the pair of 'compat' keys.
     * @return Returns the ID.
     */
    public function GetCompatKeyID()
    {
	return $this->mCompatKeyID;
    }

    /**
     * Get the pair of keys by their ID.
     * @param Id Key pair identifier or empty string for 'compat' pair.
     * @return Returns wanted key pair.
     */
    public function GetKeyPair($id='')
    {
	if ($id === false || $id === '') {
	    $id = $this->mCompatKeyID;
	}
	if (array_key_exists($id, $this->mKeyPairs)) {
	    return $this->mKeyPairs[$id];
	}
	throw new Exception("KeyHeap: keypair identified by $id not found in heap");
	return false;
    }

    /**
     * Get the pair of keys by their ID.
     * @param Id Key pair identifier or empty string for 'compat' pair.
     * @return Returns wanted key pair.
     */
    public function GetCompatKeyPair($id='')
    {
	return $this->GetKeyPair($id);
    }

    /**
     * Get the size of a public key in key pair identified by ID.
     * @param Id Key pair identifier.
     * @return Returns wanted key pair.
     */
    public function GetKeySize($id=false)
    {
	if (array_key_exists($id, $this->mKeyPairsizes)) {
	    return $this->mKeyPairsizes[$id];
	}
	throw new Exception("KeyHeap: keypair identified by $id not found in heap");
	return false;
    }

    /**
     * Calculates the identifier of a key pair.
     * @param Pemstring Key pair string in a PEM format.
     * @return Returns wanted key's ID in V4 format.
     */
    protected function readKeyID($pemstring)
    {
	return $this->mCS->call_cipher_func('rsa', 'get_key_id', $pemstring);
    }

    /**
     * Calculates the size of a key in pair.
     * @param Pemstring Key pair string in a PEM format.
     * @return Returns wanted key's length.
     */
    protected function readKeySize($pemstring)
    {
	return $this->mCS->call_cipher_func('rsa', 'get_key_size', $pemstring);
    }

    /**
     * Counts number of keys in heap.
     * @return Returns the total number of keys in heap.
     */
    public function CountKeys()
    {
	return count($this->mKeyPairs);
    }
}

?>
