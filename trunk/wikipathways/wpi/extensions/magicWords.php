<?php

require_once("wpi/wpi.php");
require_once("PathwayOfTheDay.php");

$wgCustomVariables = array(	'PATHWAYNAME','PATHWAYSPECIES', 
							'PATHWAYIMAGEPAGE', 'PATHWAYGPMLPAGE',
							'PATHWAYOFTHEDAY'
					);

$wgHooks['MagicWordMagicWords'][]          = 'wfAddCustomVariable';
$wgHooks['MagicWordwgVariableIDs'][]       = 'wfAddCustomVariableID';
$wgHooks['LanguageGetMagic'][]             = 'wfAddCustomVariableLang';
$wgHooks['ParserGetVariableValueSwitch'][] = 'wfGetCustomVariable';

function wfAddCustomVariable(&$magicWords) {
        foreach($GLOBALS['wgCustomVariables'] as $var) $magicWords[] = "MAG_$var";
        return true;
        }

function wfAddCustomVariableID(&$variables) {
        foreach($GLOBALS['wgCustomVariables'] as $var) $variables[] = constant("MAG_$var");
        return true;
        }

function wfAddCustomVariableLang(&$langMagic, $langCode = 0) {
        foreach($GLOBALS['wgCustomVariables'] as $var) {
                $magic = "MAG_$var";
                $langMagic[defined($magic) ? constant($magic) : $magic] = array(0,$var);
                }
        return true;
        }

function wfGetCustomVariable(&$parser,&$cache,&$index,&$ret) {
        switch ($index) {
				case MAG_PATHWAYOFTHEDAY:
						$pwd = new PathwayOfTheDay(null);
						$pw = $pwd->todaysPathway();
						$ret = $pw->getTitleObject()->getFullText();
						break;
                case MAG_PATHWAYNAME:
				case MAG_PATHWAYSPECIES:
				case MAG_PATHWAYIMAGEPAGE:
				case MAG_PATHWAYGPMLPAGE:
						$title = $parser->mTitle;
						if($title->getNamespace() == NS_PATHWAY) {
							$pathway = Pathway::newFromTitle($title);
							$ret = getPathwayVariable($pathway, $index);
						} else {
							$ret = "NOT A PATHWAY";
						}
                        break;
                }
        return true;
}

function getPathwayVariable($pathway, $index) {
	switch($index) {
		case MAG_PATHWAYNAME:
			return $pathway->name();
		case MAG_PATHWAYSPECIES:
			return $pathway->species();	
		case MAG_PATHWAYIMAGEPAGE:
			return $pathway->getFileTitle(FILETYPE_IMG)->getFullText();
		case MAG_PATHWAYGPMLPAGE:
			return $pathway->getFileTitle(FILETYPE_GPML)->getFullText();					
	}
}

?>
