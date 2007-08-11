<?php

/* globals */
global $CipherSuiteHooks;
global $last_mcrypt_error;

/* check whether mcrypt engine is available */
if (function_exists('mcrypt_list_algorithms')) {

/* register hooks */
    $CipherSuiteHooks['mcrypt'] = array(
	'type'			=>	'symmetric',
	'url'			=>	'http://mcrypt.sourceforge.net/',
	'init'			=> 	'wrap_mcrypt_init',
	'error'			=> 	'wrap_mcrypt_error',
	'get_ciphers'		=>	'wrap_mcrypt_get_ciphers',
	'get_ciphers_params'	=>	'wrap_mcrypt_get_ciphers_params',
	'encrypt'		=> 	'wrap_mcrypt_encrypt',
	'decrypt'		=>	'wrap_mcrypt_decrypt',
	'get_key_size'		=>	'wrap_mcrypt_get_key_size',
	'get_iv_size'		=>	'wrap_mcrypt_get_iv_size',
    );

/* create wrappers */
    function wrap_mcrypt_init()
    {
    	$last_mcrypt_error = false;
	return true;
    }

    function wrap_mcrypt_error()
    {
	global $last_mcrypt_error;

	$e = $last_mcrypt_error;
	$last_mcrypt_error = false;
	return $e;
    }

    function wrap_mcrypt_encrypt($cipher_id, $key, $text, $iv='')
    {
	$iv_size = strlen($iv);
	if ($iv_size !== false && $iv_size > 0) {
    	    $text	= mcrypt_encrypt($cipher_id, $key, $text, MCRYPT_MODE_CBC, $iv);
	} else {
	    $iv		= mcrypt_create_iv($iv_size, MCRYPT_RAND);
    	    $text	= mcrypt_encrypt($cipher_id, $key, $text, MCRYPT_MODE_CBC);
	}
	return $text;
    }

    function wrap_mcrypt_decrypt($cipher_id, $key, $text, $iv)
    {
	$iv_size = strlen($iv);
	
	if ($iv_size !== false && $iv_size > 0) {
	    $text	= mcrypt_decrypt($cipher_id, $key, $text, MCRYPT_MODE_CBC, $iv);
	} else {
	    $text	= mcrypt_decrypt($cipher_id, $key, $text, MCRYPT_MODE_CBC);
	}

	$text		= rtrim($text, "\0");
	return $text;
    }

    function wrap_mcrypt_get_ciphers()
    {
	$ary = array();
	$algorithms = mcrypt_list_algorithms();
	foreach ($algorithms as $cipher) {
    	    $key_size = @mcrypt_get_key_size($cipher,'cbc');
    	    if ($key_size && $key_size > 1) {
    		array_push($ary, $cipher);
    	    }
	}

	return $ary;
    }

    function wrap_mcrypt_get_ciphers_params()
    {
	$ary = array();
	$algorithms = mcrypt_list_algorithms();
	foreach ($algorithms as $cipher) {
    	    $key_size = @mcrypt_get_key_size($cipher,'cbc');
    	    $iv_size = @mcrypt_get_iv_size($cipher,'cbc');
    	    if ($key_size && $key_size > 1) {
    		array_push($ary, array($cipher, $key_size, $iv_size));
    	    }
	}

	return $ary;
    }

    function wrap_mcrypt_get_key_size($cipher_id)
    {
	return @mcrypt_get_key_size($cipher_id, 'cbc');
    }

    function wrap_mcrypt_get_iv_size($cipher_id)
    {
	return @mcrypt_get_iv_size($cipher_id, 'cbc');
    }

}
?>
