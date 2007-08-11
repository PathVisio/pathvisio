<?php

/* testing file */
/* move this file to your MediaWiki directory if you're too lazy to re-set this requires */

require_once("PEAR.php");
require_once("Crypt/RSA.php");
require_once("extensions/PageProtection/Cipher.php");

/* try to add more characters to key 1234555.... to cause error */

$c	= new Cipher('blowfish', '1234555555555555555555555');
if ($c->has_error()) {
    print "An error has occured: " . $c->get_last_error() . "\n";
    exit;
}

$tekst = "plain text siefca";
$encoded = $c->encrypt($tekst);

$key	= $c->mLastKey;
$iv 	= $c->mLastIV;
$cipher	= $c->mLastCipher;

$result	= $c->decrypt($encoded, $cipher, $key, $iv);

print "alghoritm: $cipher\n";
print "plaintext: $result" . "\n";

?>
