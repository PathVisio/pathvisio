<?php
/**
 * Internationalisation file for PageProtectionPlus extension. (encoding: UTF-8)
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

/**
 * note for translators
 *
 * ProtectedSite and ProtectedSiteEditing can have 3 substitutions:
 *               $1 will change into a base namespace (taken from $wgSitename)
 *               $2 will change into a list of the allowed users
 *               $3 will change into a list of the allowed groups
 *
 * Hovever it is also nice to use the templates. See TEMPLATES-MSGS for more info.
 */

$wgPageProtectionMessages = array();

$wgPageProtectionMessages['en'] = array(
	'ProtectedTitle'		=> 'Page is protected',
	'ProtectedSite'			=> 'This area is [[$1:PageProtectionPlus|protected]]. You are not allowed to read or edit this area.<br/>' .
					   "\n" . '* Allowed users: $2 <br/>' . "\n" . '* Allowed groups: $3 <br/>',
	'ProtectedSiteEditing'		=> 'This page is [[$1:PageProtectionPlus|protected]]. You are not allowed to edit this page.<br/>' . "\n" . 
					   '* Allowed users: $2 <br/>' . "\n" . '* Allowed groups: $3 <br/>',
	'CiphersList'			=> 'Here is the list of the available names of ciphers, which you can use when protecting your pages:',
	'AllCiphersList'		=> 'Here is the list of the available names of ciphers, which can be used while decrypting data:',
	'pageprotectionciphersuite'	=> 'List of ciphers available to use for protecting pages',
	'key length'			=> 'key length',
	'IV length'			=> 'vector length',
	'crypto_engine'			=> 'engine',
	'default_algo'			=> 'default algorithm',
	'rsa_warning'			=> 'caution: strong, pure-RSA encryption &ndash; avoid using on large pages cause it costs a lot of time',
	'plaintext_warning'		=> 'caution: dummy cipher &ndash; use for debugging and/or profiling purposes',
	'asymmetric_parameters'		=> 'Parameters of asymmetric keys',
	'ciphers_parameters'		=> 'Parameters of encryption',
	'rsa_length_note'		=> 'Default RSA key length',
	'rsa_id_note'			=> 'Default RSA key ID',
	'rsa_count_note'		=> 'Total count of the available keys',
	'symmetric'			=> 'symmetric',
	'asymmetric'			=> 'asymmetric',
	'decrypt_error'			=> '* \'\'\'Encryption routine encountered an error, while decrypting [[$1:PageProtectionPlus|protected]] section:\'\'\'' .
					    "\n" . '<pre>$2</pre>',
	'encrypt_error'			=> '* \'\'\'Encryption routine encountered an error, while encrypting section to make it [[$1:PageProtectionPlus|protected]]:\'\'\'' .
					    "\n" . '<pre>$2</pre>',

);

$wgPageProtectionMessages['de'] = array(
	'ProtectedTitle'		=> 'Page is protected',
	'ProtectedSite'			=> 'This area is [[$1:PageProtectionPlus|protected]]. You are not allowed to read or edit this area.<br/>' .
					   "\n" . '* Allowed users: $2 <br/>' . "\n" . '* Allowed groups: $3 <br/>',
	'ProtectedSiteEditing'		=> 'This page is [[$1:PageProtectionPlus|protected]]. You are not allowed to edit this page.<br/>' . "\n" . 
					   '* Allowed users: $2 <br/>' . "\n" . '* Allowed groups: $3 <br/>',
	'CiphersList'			=> 'Here is the list of the available names of ciphers, which you can use when protecting your pages:',
	'AllCiphersList'		=> 'Here is the list of the available names of ciphers, which can be used while decrypting data:',
	'pageprotectionciphersuite'	=> 'List of ciphers available to use for protecting pages',
	'key length'			=> 'key length',
	'IV length'			=> 'vector length',
	'crypto_engine'			=> 'engine',
	'default_algo'			=> 'default algorithm',
	'rsa_warning'			=> 'caution: strong, pure-RSA encryption &ndash; avoid using on large pages cause it costs a lot of time',
	'plaintext_warning'		=> 'caution: dummy cipher &ndash; use for debugging and/or profiling purposes',
	'asymmetric_parameters'		=> 'Parameters of asymmetric keys',
	'ciphers_parameters'		=> 'Parameters of encryption',
	'rsa_length_note'		=> 'Default RSA key length',
	'rsa_id_note'			=> 'Default RSA key ID',
	'rsa_count_note'		=> 'Total count of the available keys',
	'symmetric'			=> 'symmetric',
	'asymmetric'			=> 'asymmetric',
	'decrypt_error'			=> '* \'\'\'Encryption routine encountered an error, while decrypting [[$1:PageProtectionPlus|protected]] section:\'\'\'' .
					    "\n" . '<pre>$2</pre>',
	'encrypt_error'			=> '* \'\'\'Encryption routine encountered an error, while encrypting section to make it [[$1:PageProtectionPlus|protected]]:\'\'\'' .
					    "\n" . '<pre>$2</pre>',

);

$wgPageProtectionMessages['pl'] = array(
	'ProtectedTitle'		=> 'Strona zabezpieczona',
	'ProtectedSite'			=> '{{ProtectedSite_pl}}',
	'ProtectedSiteEditing'		=> '{{ProtectedSiteEditing_pl}}',
	'CiphersList'			=> 'Oto lista nazw dostępnych szyfrów, których możesz użyć zabezpieczając strony:', 
	'AllCiphersList'		=> 'Oto lista nazw dostępnych szyfrów, które są rozpoznawalne przy dekodowaniu danych:',
	'pageprotectionciphersuite'	=> 'Lista szyfrów do użytku w ochronie zawartości',
	'key length'			=> 'dł. klucza',
	'IV length'			=> 'dł. wektora inicjującego',
	'crypto_engine'			=> 'silnik',
	'default_algo'			=> 'algorytm domyślny',
	'rsa_warning'			=> 'uwaga: mocne szyfrowanie w oparciu o czyste RSA &ndash; unikaj dla większych stron ze względu na ' .
					   'czas wykonywania',
	'plaintext_warning'		=> 'uwaga: to naprawdę nie jest szyfr &ndash; używa się go w celach profilowania i odpluskwiania',
	'asymmetric_parameters'		=> 'Parametry kluczy asymetrycznych',
	'ciphers_parameters'		=> 'Parametry szyfrowania',
	'rsa_length_note'		=> 'Długość domyślnego klucza',
	'rsa_id_note'			=> 'Identyfikator domyślnego klucza',
	'rsa_count_note'		=> 'Całkowita liczba dostępnych kluczy',
	'symmetric'			=> 'symetryczny',
	'asymmetric'			=> 'asymetryczny',
	'decrypt_error'			=> '* \'\'\'Mechanizm szyfrujący wykrył błąd podczas odszyfrowywania [[$1:PageProtectionPlus|chronionej]] sekcji:\'\'\'' .
					    "\n" . '<pre>$2</pre>',
	'encrypt_error'			=> '* \'\'\'Mechanizm szyfrujący wykrył błąd podczas szyfrowania [[$1:PageProtectionPlus|chronionej]] sekcji:\'\'\'' .
					    "\n" . '<pre>$2</pre>',
);

//$wgRenameuserMessages['zh-hk'] = $wgRenameuserMessages['zh-tw'];
//$wgRenameuserMessages['zh-sg'] = $wgRenameuserMessages['zh-cn'];
?>

