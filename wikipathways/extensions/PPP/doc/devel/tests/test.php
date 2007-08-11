<?php

/* testing file */
/* move this file to your MediaWiki directory if you're too lazy to re-set this requires */

require_once("PEAR.php");
require_once("Crypt/RSA.php");
require_once("extensions/PageProtection/Cipher.php");

$plaintext = "This is plaintext.";

/* create object */

$c = new Cipher();
if ($c->has_error()) {
    print "Error: " . $c->get_last_error() . "\n";
    exit;
}

/* encrypt message using default cipher */

$encrypted = $c->encrypt($plaintext);
if ($c->has_error()) {
    print "Error: " . $c->get_last_error() . "\n";
    exit;
}


/* get parameters used during encyprion proccess */

$key	= $c->mLastKey;
$iv 	= $c->mLastIV;
$cipher	= $c->mLastCipher;

/* decrypt the encrypted message using obtained values */

$result	= $c->decrypt($encrypted, $cipher, $key, $iv);
if ($c->has_error()) {
    print "Error: " . $c->get_last_error() . "\n";
    exit;
}

/* display results */

print "algorithm: $cipher\n";
print "previous key: " . base64_encode($key) . "\n";
print "previous IV: " . base64_encode($iv) . "\n\n";
print "plaintext: $result\n";


?>