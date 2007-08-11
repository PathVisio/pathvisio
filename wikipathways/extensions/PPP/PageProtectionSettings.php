<?php

/* This file contains setting for PageProtectionPlus		*/
/* You should NOT modify this file, use LocalSettings instead	*/
/* after reading the comments, which can tell you about names.	*/

require_once("LocalSettings.php");

/* The user will be allowed to use these ciphers' */
/* names when encrypting pages.			  */

/* For defaults (user not decided) the order has  */
/* meaning. Please put most preferred ciphers at  */
/* the top. It may be overriden by the variable   */
/* called $wgPreferredCiphers place in the file   */
/* LocalSettings.php				  */

$PageProtectionPreferredCiphers = array(
	'rijndael-256'	=> true,
	'blowfish'	=> true,
	'tripledes'	=> true,
	'saferplus'	=> true,
	'serpent'	=> true,
	'loki97'	=> true,
	'cast-256'	=> true,
	'rsa'		=> true,
	'plaintext'	=> true,
);

/* Note, that each one of the ciphers will use  */
/* RSA to encrypt the random key before putting */
/* it to the database. However, the 'rsa' means	*/
/* here, that the users are allowed to use pure */
/* RSA encryption (hard for your CPU), which 	*/
/* will use smaller, second key called 'lite'.	*/


/* Enabled cryptographic engines.		*/

require_once("engines/CryptRSA.php");
require_once("engines/CryptBlowfish.php");
require_once("engines/Plain.php");
require_once("engines/MCrypt.php");	//this should be the last

/* RSA default key size in bits.		*/

$PageProtectionRSAKeySize = 2048;

/* RSA default 'lite' key size in bits.		*/

$PageProtectionRSALiteKeySize = 512;

/* RSA keys directory.	 			*/
/* It may be overriden by $wgPEMdir variable	*/
/* placed in LocalSettings.php 			*/

$PageProtectionRSAKeyDir = "keys";

/* RSA default key filename. 			*/
/* It may be overriden by $wgPEMfile variable	*/
/* placed in LocalSettings.php			*/

$PageProtectionRSAKeyFile = "default.pem";

/* RSA default 'lite' key filename.		*/
/* It may be overriden by $wgPEMlite_file	*/
/* variable placed in LocalSettings.php		*/

$PageProtectionRSALiteKeyFile = "lite.pem";

/* RSA compatible key filename. 		*/
/* This file should contain the PEM key pair	*/
/* used in PageProtection version < 1.5		*/

$PageProtectionRSACompatFile = "compat.pem";





/************************************************/
/* overridings..				*/
/************************************************/

if (isset($wgPEMsize) && is_int($wgPEMsize)) {
    $PageProtectionRSAKeySize = $wgPEMsize;
}
if (!isset($PageProtectionRSAKeySize)	||
    !is_int($PageProtectionRSAKeySize)	||
    $PageProtectionRSAKeySize <= 1) {
    $PageProtectionRSAKeySize = 2048;
}

if (isset($wgPEMlite_size) && is_int($wgPEMlite_size)) {
    $PageProtectionRSALiteKeySize = $wgPEMlite_size;
}
if (!isset($PageProtectionRSALiteKeySize) ||
    !is_int($PageProtectionRSALiteKeySize)||
    $PageProtectionRSALiteKeySize <= 1) {
    $PageProtectionRSALiteKeySize = 256;
}


if (isset($wgPEMfile)			&&
    $wgPEMfile !== false		&&
    $wgPEMfile != '') {
    $PageProtectionRSAKeyFile = $wgPEMfile;
}

if (isset($wgPEMlite_file)		&&
    $wgPEMlite_file !== false		&&
    $wgPEMlite_file != '') {
    $PageProtectionRSALiteKeyFile = $wgPEMlite_file;
}

if (isset($wgPEMdir)			&&
    $wgPEMdir !== false			&&
    $wgPEMdir !== '') {
    $PageProtectionRSAKeyDir = $wgPEMdir;
}

if (isset($wgPreferredCiphers)		&&
    $wgPreferredCiphers !== false	&&
    is_array($wgPreferredCiphers)) {
    $PageProtectionPreferredCiphers = $wgPreferredCiphers;
}

?>
