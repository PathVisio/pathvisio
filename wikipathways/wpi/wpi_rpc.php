<?php

error_reporting(E_ERROR); //Supress warnings etc...will disrupt the rpc response

//Load XML-RCP libraries
require("includes/xmlrpc.inc");
require("includes/xmlrpcs.inc");

//Load WikiPathways Interface
require("wpi.php");

//Definition of functions
$updatePathway_sig=array(array(
							$xmlrpcBoolean, 
							$xmlrpcString, $xmlrpcString, $xmlrpcString, $xmlrpcBase64
						));

$updatePathway_doc='updatePathway';

$convertPathway_sig= array(array(
	$xmlrpcBase64,
	$xmlrpcBase64, $xmlrpcString
));

$convertPathway_sig='convertPathway';

//Definition of dispatch map
$disp_map=array("WikiPathways.updatePathway" => 
                        array("function" => "updatePathway",
                        "signature" => $updatePathway_sig,
			"docstring" => $updatePathway_doc),
		"WikiPathways.convertPathway" =>
			array("function" => "convertPathway",
			"signature" => $exportPathway_sig,
			"docstring" => $exportPathway_doc),
);

//Setup the XML-RPC server
$s=new xmlrpc_server($disp_map,0);
$s->functions_parameters_type = 'phpvals';
//$s->setDebug(3);
$s->service();

//Functions
function updatePathway($pwName, $pwSpecies, $description, $gpmlData64) {
	global $xmlrpcerruser;
	
	$resp = TRUE;
	try {
		$pathway = new Pathway($pwName, $pwSpecies);
		$gpmlData = base64_decode($gpmlData64);
		$pathway->updatePathway($gpmlData, $description);
	} catch(Exception $e) {
		wfDebug("XML-RPC ERROR: $e");
		$resp = new xmlrpcresp(0, $xmlrpcerruser, $e);
	}
	ob_clean(); //Clean the output buffer, so nothing is printed before the xml response
	return $resp;
}

/**
 * Convert the given GPML data to a file of the given filetype
 * Returns bas64 encoded result of the conversion
 */
function convertPathway($gpmlData64, $fileType) {
	global $xmlrpcerruser;
	
	$gpmlData = base64_decode($gpmlData64);
	$gpmlFile = tempnam(WPI_TMP_PATH, "gpml");
	writeFile($gpmlFile, $gpmlData);
	$imgFile = tempnam(WPI_TMP_PATH, $fileType) . ".$fileType";
	$cmd = "java -jar bin/pathvisio_converter.jar $gpmlFile $imgFile 2>&1";
	wfDebug($cmd);
	exec($cmd, $output, $status);
	
	foreach ($output as $line) {
		$msg .= $line . "\n";
	}
	wfDebug("Converting to $fileType:\nStatus:$status\nMessage:$msg");
	if($status != 0 ) {
		return new xmlrpcresp(0, $xmlrpcerruser, "Unable to convert:\nStatus:$status\nMessage:$msg");
	}
	$imgData = file_get_contents($imgFile);
	$imgData64 = base64_encode($imgData);
	unlink($gpmlFile);
	unlink($imgFile);
	ob_clean(); //Clean the output buffer, so nothing is printed before the xml response
	return $imgData64;
}
?>
