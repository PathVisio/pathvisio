<?php

/**
 * SpecialCipherSuite shows special MediaWiki page containing list of supported ciphers.
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

require_once("CipherSuite.php");
require_once("KeyHeap.php");
require_once("PageProtectionSettings.php");
require_once("PageProtection.i18n.php");

/* add messages */
global $wgMessageCache, $wgPageProtectionMessages;
foreach( $wgPageProtectionMessages as $key => $value ) {
    $wgMessageCache->addMessages( $wgPageProtectionMessages[$key], $key );
}


if (!defined('MEDIAWIKI')) {
	echo "Special:PageProtectionCipherSuite extension\n";
	die( 1 );
}

class SpecialPageProtectionCipherSuite extends SpecialPage {
	function SpecialPageProtectionCipherSuite() {
		SpecialPage::SpecialPage( 'PageProtectionCipherSuite' );
	}

	function execute( $par ) {
		global $wgRequest, $wgOut;
		$cs = new CipherSuite();
		$heap = new KeyHeap('', $cs);

		$this->setHeaders();

		$rsa_default = $heap->GetDefaultKeyID();
		$key_size = $heap->GetKeySize($rsa_default);
		$rsa_count = $heap->CountKeys();
		
		$wgOut->addWikiText( "== "				.
				    wfMsg( 'asymmetric_parameters' )	.
				    " ==\n"				.
				    "*" . wfMsg( 'rsa_length_note' )	.
				    ": " . $key_size			.
				    "b\n*" . wfMsg( 'rsa_id_note' )	.
				    ": " . $rsa_default	. "\n" 		.
				    "*" . wfMsg( 'rsa_count_note' )	.
				    ": " . $rsa_count . "\n" );

		$wgOut->addWikiText( "== "				.
				    wfMsg( 'ciphers_parameters' )	.
				    " ==\n");
		$wgOut->addWikiText( wfMsg( 'CiphersList' ) . "\n");
		$i = 0;
		$adnote = " &ndash; '''" . wfMsg( 'default_algo' ) . "'''";
		foreach ($cs->list_usable_ciphers() as $cipher_id) {
		    if ($i == 1) {
			$adnote='';
		    } else {
			$i=1;
		    }

		    $engine = $cs->engine($cipher_id);
		    $engine = "[" . $cs->engine_url($engine) . " " . $engine . "]";
		    $iv_size = $cs->iv_size($cipher_id);
		    $type = $cs->type($cipher_id);

		    if ('rsa' !== $cipher_id) {
			$key_size = $cs->key_size($cipher_id);
			$iv_size *= 8;
			$key_size *= 8;
		    } else {
			$lite_id = $heap->GetLiteKeyID();
			$key_size = $heap->GetKeySize($lite_id);
		    }

		    if ($iv_size > 0) {
			$iv_size = wfMsg( 'IV length' ) . ": " . $iv_size . "b, ";
		    } else {
			$iv_size = '';
		    }

		    if ($type === 'symmetric') {
			$plusrsa =	 "<sup>"				.
					 "<span style=\"font-size:90%\">" 	.
					 "[[rsa|+ rsa]]"			.
					 "</span>"				.
					 "</sup>  ";
		    } else {
			$plusrsa = '';
		    }

		    $wgOut->addWikiText( "* '''[[" . $cipher_id . 
					 "]]''' " 				.
					 $plusrsa				.
					 "<span style=\"font-size:85%\">" 	.
					 "(" . wfMsg( $type ) . ", "		.
					 wfMsg( 'key length' ) . ": "		.
					 $key_size . "b, "			.
					 $iv_size				.
					 wfMsg( 'crypto_engine' ) . ": "	.
					 $engine . ")</span>" 			.
					 $adnote . "\n" );

		    if ('rsa' === $cipher_id) {
			$wgOut->addWikiText( ":&nbsp;&nbsp;<span style=\"font-size:85%\">"	.
					wfMsg( 'rsa_warning' ) 					.
					"</span>\n");
		    }
		    if ('plaintext' === $cipher_id) {
			$wgOut->addWikiText( ":&nbsp;&nbsp;<span style=\"font-size:85%\">"	.
					wfMsg( 'plaintext_warning' )				.
					"</span>\n");
		    }

		}

		$all_ciphers = $cs->list_ciphers();
		sort($all_ciphers);
		$wgOut->addWikiText( "\n\n"				.
				     wfMsg( 'AllCiphersList' )		.
				     " ''"				.
				     implode(', ', $all_ciphers)	.
				     "''." );
	}
}

?>
