<?php

require_once("PEAR.php");
@include_once("Crypt/Blowfish.php");

/* globals */
global $last_bp_error;
global $CipherSuiteHooks;

/* check whether Crypt/Blowfish engine is available */
if (class_exists('Crypt_Blowfish')) {
    /* register hooks */
    $CipherSuiteHooks['PEAR/Crypt/Blowfish'] = array(
	'type'			=>	'symmetric',
	'url'			=>	'http://pear.php.net/package/Crypt_Blowfish',
	'init'			=> 	'wrap_bp_init',
	'encrypt'		=> 	'wrap_bp_encrypt',
	'decrypt'		=>	'wrap_bp_decrypt',
	'get_ciphers'		=>	'wrap_bp_get_ciphers',
	'get_ciphers_params'	=>	'wrap_bp_get_ciphers_params',
	'get_key_size'		=>	'wrap_bp_get_key_size',
	'get_iv_size'		=>	'wrap_bp_get_iv_size',
	'error'			=>	'wrap_bp_error',
    );


    /* create wrappers */
    function wrap_bp_init()
    {
	$last_bp_error = false;
	return true;
    }

    function wrap_bp_error()
    {
	global $last_bp_error;

        $e = $last_bp_error;
	$last_bp_error = false;
	return $e;
    }

    function wrap_bp_encrypt($cipher_id, $key, $text, $iv)
    {
	if ($cipher_id !== 'blowfish') {
	    $last_bp_error = 'PEAR/Crypt/Blowfish: encrypt: unknown_cipher';
	    return false;
	}

	$bf = new Crypt_Blowfish('cbc');

	$iv_size = strlen($iv);
	if ($iv_size !== false && $iv_size > 0) {
	    $bf->setKey($key, $iv);
	} else {
    	    $bf->setKey($key);
	}
	if (PEAR::isError($text)) {
	    $last_bp_error = 'PEAR/Crypt/Blowfish: encrypt: ' . $text->getMessage();
    	    return false;
	}

	$text = $bf->encrypt($text);
	if (PEAR::isError($text)) {
	    $last_bp_error = 'PEAR/Crypt/Blowfish: encrypt: ' . $text->getMessage();
    	    return false;
	}

	return $text;
    }

    function wrap_bp_decrypt($cipher_id, $key, $text, $iv)
    {
	$bf = new Crypt_Blowfish('cbc');

	$iv_size = strlen($iv);
	if ($iv_size !== false && $iv_size > 0) {
    	    $bf->setKey($key, $iv);
	} else {
    	    $bf->setKey($key);
	}

	$text = $bf->decrypt($text);
	if (PEAR::isError($text)) {
    	    $last_bp_error = 'blowfish_decrypt_error ' . $text->getMessage();
    	    return false;
	}

	$text = rtrim($text, "\0");
	return $text;
    }

    function wrap_bp_get_ciphers()
    {
	$ary = array();
	array_push($ary, 'blowfish');

	return $ary;
    }

    function wrap_bp_get_ciphers_params()
    {
	$ary = array();
	$bf = new Crypt_Blowfish('cbc');
    
	array_push ( $ary, array( 'blowfish',
				  $bf->getMaxKeySize(),
				  $bf->getIVSize()
				)
		   );

        return $ary;
    }


    function wrap_bp_get_key_size($cipher_id)
    {
	$bf = new Crypt_Blowfish('cbc');
	return $bf->getMaxKeySize();
    }

    function wrap_bp_get_iv_size($cipher_id)
    {
	$bf = new Crypt_Blowfish('cbc');
	return $bf->getIVSize();
    }
}

?>
