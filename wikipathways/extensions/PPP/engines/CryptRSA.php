<?php

require_once("PEAR.php");
require_once("Crypt/RSA.php");
require_once("RandKeyGen.php");

/* globals */
global $CipherSuiteHooks;
global $last_rsa_error;
global $wrap_rsa_default_key_size;
global $wrap_rsa_factory;

/* check whether rsa engine is available */
if (class_exists('Crypt_RSA_KeyPair')) {

/* register hooks */
    $CipherSuiteHooks['PEAR/Crypt_RSA'] = array(
	'type'			=>	'asymmetric',
	'url'			=>	'http://pear.php.net/package/Crypt_RSA',
	'init'			=> 	'wrap_rsa_init',
	'error'			=> 	'wrap_rsa_error',
	'get_ciphers'		=>	'wrap_rsa_get_ciphers',
	'get_ciphers_params'	=>	'wrap_rsa_get_ciphers_params',
	'get_fingerprint'	=>	'wrap_rsa_get_fingerprint',
	'get_key_id'		=>	'wrap_rsa_get_key_id',
	'encrypt'		=> 	'wrap_rsa_encrypt',
	'decrypt'		=>	'wrap_rsa_decrypt',
	'create_pem'		=>	'wrap_rsa_create_pem',
	'get_public_key'	=>	'wrap_rsa_get_public_key',
	'get_private_key'	=>	'wrap_rsa_get_private_key',
	'get_key_size'		=>	'wrap_rsa_get_key_size',
	'get_iv_size'		=>	'wrap_rsa_get_iv_size',
    );


function wrap_rsa_cerr($obj, $prefix='')
{
    global $last_rsa_error;

    if ($obj->isError()) {
        $error = $obj->getLastError();
        if ($prefix !== '') {
    	    $last_rsa_error = $prefix . ": " . $error->getMessage();
    	} else {
    	    $last_rsa_error = $error->getMessage();
    	}
	return true;
    }
    return false;
}

/* create wrappers */
    function wrap_rsa_init($key_size=16384)
    {
	static $initialized_once = false;
	global $wrap_rsa_factory;
	global $last_rsa_error;
	global $wrap_rsa_default_key_size;
	
	$last_rsa_error = false;
	$wrap_rsa_default_key_size = $key_size;
	if (!$initialized_once) {
	    $wrap_rsa_factory = new Crypt_RSA;
	    $initialized_once = true;
	}
	return true;
    }

    function wrap_rsa_create_pem($key_size) 
    {
        $keypair = new Crypt_RSA_KeyPair($key_size, 'default', '', 'RandIntGen');
        if (wrap_rsa_cerr($keypair, 'wrap_rsa_create_pem')) { return false; }
        $str = $keypair->toPEMString();
        if (wrap_rsa_cerr($keypair, 'wrap_rsa_create_pem')) { return false; }
        return $str;
    }

    function wrap_rsa_error()
    {
	global $last_rsa_error;

	$e = $last_rsa_error;
	$last_rsa_error = false;
	return $e;
    }

    /**
    * Encrypts an text with RSA
    * @param text Plaintext
    * @return RSA-encrypted text.
    */
    function wrap_rsa_encrypt($cipher_id, $keypair, $text, $iv_size='', $iv='')
    {
	global $wrap_rsa_factory;
	global $last_rsa_error;

	$rsa = $wrap_rsa_factory;
	if (wrap_rsa_cerr($rsa, 'wrap_rsa_encrypt')) { return false; }

	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 

        $enc = $rsa->encryptBinary($text, $key_pair->getPublicKey());
        if (wrap_rsa_cerr($rsa, 'wrap_rsa_encrypt')) { return false; }

	return $enc;
    }

    /**
    * Decrypts an RSA-encrypted text.
    * @param text Encrypted text
    * @return Plaintext
    */
    function wrap_rsa_decrypt($cipher_id, $keypair, $text, $iv_size='')
    {
	global $wrap_rsa_factory;
	global $last_rsa_error;

	$rsa = $wrap_rsa_factory;

	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 

        $dec = $rsa->decryptBinary($text, $key_pair->getPrivateKey());
        if (wrap_rsa_cerr($rsa, 'wrap_rsa_decrypt')) {return false; }

        if ($dec === '') {
            return $text;
        }
        return $dec;
    }

    function wrap_rsa_get_public_key($keypair)
    {
    	global $last_rsa_error;

	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 

        $public_key = $key_pair->getPublicKey();
        if (wrap_rsa_cerr($public_key, 'wrap_rsa_get_public_key')) { return false; }

	$r = $public_key->toString();
	if (wrap_rsa_cerr($public_key, 'wrap_rsa_get_public_key')) { return false; }
	return $r;
    }

    function wrap_rsa_get_private_key($keypair)
    {
	global $last_rsa_error;

	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 

        $private_key = $key_pair->getPrivateKey();
        if (wrap_rsa_cerr($private_key, 'wrap_rsa_get_private_key')) { return false; }

	$r = $private_key->toString();
	if (wrap_rsa_cerr($private_key, 'wrap_rsa_get_private_key')) { return false; }
	return $r;
    }

    /**
     * Create RFC-2440 (p. 11 - Enhanced Key Formats) compliant fingerprint.
     * It should be V4 fingerprint (160-bit SHA1 hash).
     */
    function wrap_rsa_get_fingerprint($keypair)
    {
	global $last_rsa_error;
    
	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 
	$public_key = $key_pair->getPublicKey();
	if (wrap_rsa_cerr($public_key)) { return false; }
	$length = $public_key->getKeyLength();
	$modulus = $public_key->getModulus();
	$exponent = $public_key->getExponent();
	if (wrap_rsa_cerr($public_key)) { return false; }

	$length = (int)$length;
	$buf  = pack ("H1", 0x99);	/* 8 bits of Packet Tag				*/
	$buf .= pack("n1", $length);	/* 16 bits of Length, big-endian		*/
	$buf .= pack("c1", 4);		/* 8 bits of Version Number			*/
	$buf .= pack("L1", 0);		/* 32 bits of Creation Timestamp (unsupported)	*/
	$buf .= pack("c1", 1);		/* 8 bits of Algorithm	Identifier		*/
	$buf .= $modulus . $exponent;	/* Algorithm Specific Fields for RSA		*/

	return sha1($buf);
    }

    /**
     * Create RFC-2440 compliant key identifier.
     */
    function wrap_rsa_get_key_id($keypair)
    {
	return substr(wrap_rsa_get_fingerprint($keypair), 0, 8);
    }

    function wrap_rsa_get_ciphers()
    {
	$ary = array();
	array_push($ary, 'rsa');

	return $ary;
    }

    function wrap_rsa_get_ciphers_params()
    {
    	global $wrap_rsa_default_key_size;
	$ary = array ( array( 'rsa',
			       $wrap_rsa_default_key_size,
			       0
		     ) );

        return $ary;
    }

    function wrap_rsa_get_key_size($keypair='')
    {
	global $last_rsa_error;
	global $wrap_rsa_default_key_size;

	if ($keypair === '') {
	    return $wrap_rsa_default_key_size;
	}

	$key_pair = Crypt_RSA_KeyPair::fromPEMString($keypair, 'default', 'wrap_rsa_cerr');
	if ($last_rsa_error !== false) { return false; } 
	$public_key = $key_pair->getPublicKey();
	if (wrap_rsa_cerr($public_key)) { return false; }
	$length = (int)$public_key->getKeyLength();
	if (wrap_rsa_cerr($public_key)) { return false; }

	return $length;	
    }

    function wrap_rsa_get_iv_size($cipher_id)
    {
	return 0;
    }

}

?>
