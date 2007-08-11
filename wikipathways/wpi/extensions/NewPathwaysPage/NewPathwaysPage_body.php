<?php
require_once("QueryPage.php");

class NewPathwaysPage extends SpecialPage
{		
        function NewPathwaysPage() {
                SpecialPage::SpecialPage("NewPathwaysPage");
                self::loadMessages();
        }

        function execute( $par ) {
                global $wgRequest, $wgOut;
                
                $this->setHeaders();

                list( $limit, $offset ) = wfCheckLimits();

				$rcp = new RCQueryPage();

				return $rcp->doQuery( $offset, $limit );
        }

        function loadMessages() {
                static $messagesLoaded = false;
                global $wgMessageCache;
                if ( $messagesLoaded ) return;
                $messagesLoaded = true;

                require( dirname( __FILE__ ) . '/NewPathwaysPage.i18n.php' );
                foreach ( $allMessages as $lang => $langMessages ) {
                        $wgMessageCache->addMessages( $langMessages, $lang );
                }
        }
}

class RCQueryPage extends QueryPage {

	function getName() {
		return "NewPathways";
	}

	function isExpensive() {
		# page_counter is not indexed
		return true;
	}
	function isSyndicated() { return false; }

	function getSQL() {
		$dbr =& wfGetDB( DB_SLAVE );
		$page = $dbr->tableName( 'page');
		$recentchanges = $dbr->tableName( 'recentchanges');

		return
			"SELECT 'Newpathwaypages' as type,
			        page_namespace as namespace,
			        page_title as title,
				rc_user as user_id,
				rc_user_text as utext,
				rc_timestamp as value
			FROM $page, $recentchanges
			WHERE page_title=rc_title AND rc_new=1 AND page_namespace=".NS_PATHWAY." AND page_is_redirect=0";
	}

	function formatResult( $skin, $result ) {
		global $wgLang, $wgContLang, $wgUser;
		$title = Title::makeTitle( $result->namespace, $result->title );
		$link = $skin->makeKnownLinkObj( $title, htmlspecialchars( $wgContLang->convert( $title->getBaseText() ) ) );
		$nv = "created ". $wgLang->timeanddate($result->value) . " by " . $wgUser->getSkin()->userlink($result->user_id, $result->utext);
		return wfSpecialList($link, $nv);
	}
}
?>
