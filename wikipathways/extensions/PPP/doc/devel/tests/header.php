<?php

require_once("../../CryptoParser.php");

$h = new CryptoParser();
$r = $h->CreateCryptogram("This is a text\nmultilined\nsiefca\ttralalalala\n\nok\n",
		 'klucz123123123123',
		 'rsa',
		 '  Heaven Key 01',
		 "blowfish\n\r \n",
		 ' ,cbc ',
		 56,
		 8);

print "\nMessage:\n" . $r . "\n\n";
print "-------------------------------------------reloaded:-------\n\n";
$r = $h->LoadCryptogram($r);
print $h->mHeader;

print "\n";

?>
