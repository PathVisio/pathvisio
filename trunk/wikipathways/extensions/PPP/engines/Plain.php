<?php

/* globals */
global $last_plain_error;
global $CipherSuiteHooks;

/* check whether plaintext engine is available */
if (true) {
    /* register hooks */
    $CipherSuiteHooks['plaintext'] = array(
	'type'			=>	'symmetric',
	'url'			=>	'http://en.wikipedia.org/wiki/plaintext',
	'init'			=> 	'wrap_plain_init',
	'encrypt'		=> 	'wrap_plain_encrypt',
	'decrypt'		=>	'wrap_plain_decrypt',
	'get_ciphers'		=>	'wrap_plain_get_ciphers',
	'get_ciphers_params'	=>	'wrap_plain_get_ciphers_params',
	'get_key_size'		=>	'wrap_plain_get_key_size',
	'get_iv_size'		=>	'wrap_plain_get_iv_size',
	'error'			=>	'wrap_plain_error',
    );


    /* create wrappers */
    function wrap_plain_init()
    {
	$last_plain_error = false;
	return true;
    }

    function wrap_plain_error()
    {
	global $last_plain_error;

        $e = $last_plain_error;
	$last_plain_error = false;
	return $e;
    }

    function wrap_plain_encrypt($cipher_id, $key, $text, $iv)
    {
	return $text;
    }

    function wrap_plain_decrypt($cipher_id, $key, $text, $iv)
    {
	return $text;
    }

    function wrap_plain_get_ciphers()
    {
	$ary = array();
	array_push($ary, 'plaintext');

	return $ary;
    }

    function wrap_plain_get_ciphers_params()
    {
	$ary = array();
	array_push ( $ary, array( 'plaintext', 32, 32 ) );

        return $ary;
    }


    function wrap_plain_get_key_size($cipher_id)
    {
	return 32;
    }

    function wrap_plain_get_iv_size($cipher_id)
    {
	return 32;
    }
}

?>
