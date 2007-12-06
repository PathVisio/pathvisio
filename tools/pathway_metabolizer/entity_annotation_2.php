<link rel="stylesheet" type="text/css" href="tm_ebi.css">
	
<?php
set_time_limit(0);
 print "<h1>GPML Annotator: ".$_GET["pathway"]."</h1>";
$new_gpml = new DOMDocument('1.0', 'iso-8859-1');
$element = $new_gpml->createElementNS('http://genmapp.org/GPML/2007', 'Pathway');
$root = $new_gpml->appendChild($element);


if ($_GET["pathway"] != "") {

	if (substr_count($_GET["pathway"], ".gpml") == 0) die("Not a valid gpml file. ".$_GET["pathway"]);

	 $elements = array();
	  $xml = simplexml_load_file("pathways/".$_GET["pathway"]);
	  $root->setAttribute("Name", $xml["Name"]);
	  $root->setAttribute("Name", $xml["Data-Source"]);
	  $root->setAttribute("Version", $xml["Version"]);
	  $root->setAttribute("Author", $xml["Author"]);
	  $root->setAttribute("Email", $xml["Email"]);
	  $root->setAttribute("Organism", $xml["Organism"]);
	  foreach ($xml->{"Graphics"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	  foreach ($xml->{"DataNode"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
	    $lastdatanode = $root->appendChild($domnode);
	  }
	  foreach ($xml->{"Line"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	foreach ($xml->{"Label"} as $datanode) {
	      $item = (string) $datanode["TextLabel"];
	      array_push($elements, $item);
	      print $item."<br>";
      }
      print "<table>";
      $metabolites = array();
	foreach ($xml->{"Label"} as $datanode) {
	 	 print "<tr><td>".$datanode["TextLabel"]. "</td>";
	     $element = (string) $datanode["TextLabel"];
	     $metabolites = whatizit_metabolites($element);
	     if (count($metabolites)>0){
			foreach ($metabolites as $metabolite) {
	         if ($metabolite == $element)
	             if ($metabolite["sem"]=="hmdbid") {
		            $source="HMDB";
		            $source_id = $metabolite["ids"];
					print " <td><a href=\"http://www.hmdb.ca/scripts/show_card.cgi?METABOCARD=".$metabolite["ids"].".txt\">".$metabolite["sem"].":".$metabolite["ids"]."</a></td>";
				} else {
					if (substr_count($metabolite["ontIDs"], "CHEBI")>0) {
						print "<td><a href=\"http://www.ebi.ac.uk/chebi/searchId.do?chebiId=".$metabolite["ontIDs"]."\">".$metabolite["ontIDs"]."</a></td>";
						$source="CHEBI";
						$source_id = $metabolite["ontIDs"];
					}
					else {
					    print "<td>".$metabolite["sem"]."</td>";
							$domnode = dom_import_simplexml($datanode);
						    $domnode = $new_gpml->importNode($domnode, true);
							$root->appendChild($domnode);
					}
				}
				} 
		   
            if ($source != "") {
	            $datanode_n = $new_gpml->createElement("DataNode");
	            $new_datanode = $root->insertBefore($datanode_n, $lastdatanode);
	            $new_datanode->setAttribute("TextLabel", (string) $datanode["TextLabel"]);
	            $new_datanode->setAttribute("Type", "Metabolite");
	            $new_datanode->setAttribute("BackpageHead", (string) $datanode["TextLabel"]);
	            $new_datanode->setAttribute("GraphId", (string) $datanode["GraphID"]);

	            $graphnode = $new_gpml->createElement("Graphics");
	            $new_graphnode = $new_datanode->appendChild($graphnode);
	            $new_graphnode->setAttribute("Color", (string) $datanode->Graphics["Color"]);
	            $new_graphnode->setAttribute("CenterX", (string) $datanode->Graphics["CenterX"]);
	            $new_graphnode->setAttribute("CenterY", (string) $datanode->Graphics["CenterY"]);
	            $new_graphnode->setAttribute("Width", (string) $datanode->Graphics["Width"]);
	            $new_graphnode->setAttribute("Height", (string) $datanode->Graphics["Height"]);

	            $refnode = $new_gpml->createElement("Xref");
	            $new_refnode = $new_datanode->appendChild($refnode);
	            $new_refnode->setAttribute("Database", $source);
	            $new_refnode->setAttribute("ID", $source_id);


	            $source = "";
	 			$source_id = "";
	           
            }
	     }
	     else
	     {
			$domnode = dom_import_simplexml($datanode);
		    $domnode = $new_gpml->importNode($domnode, true);
			$root->appendChild($domnode);
	} 

    print "</tr>";
} 
  print "</table>";

  foreach ($xml->{"Link"} as $datanode) {
	$domnode = dom_import_simplexml($datanode);
    $domnode = $new_gpml->importNode($domnode, true);
	$root->appendChild($domnode);
  }

	  foreach ($xml->{"Shape"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	  foreach ($xml->{"Group"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	  foreach ($xml->{"InfoBox"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	  foreach ($xml->{"Legend"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
	  foreach ($xml->{"Biopax"} as $datanode) {
		$domnode = dom_import_simplexml($datanode);
	    $domnode = $new_gpml->importNode($domnode, true);
		$root->appendChild($domnode);
	  }
      
  print $new_gpml->save("metabolised_pathways/".$_GET["pathway"]);
}


// Functions start here
function whatizit_metabolites($term){
$whatizit = new SoapClient('http://www.ebi.ac.uk/webservices/whatizit/ws?wsdl');

try{
  $result = $whatizit->contact(array(
    'pipelineName' => 'whatizitChemicalsMeta',
    'text' => $term,
    'convertToHtml' => false,
    ));
} catch (SoapFault $exception) { return FALSE; }

if ($xml = simplexml_load_string($result->return)){
  $xml->registerXPathNamespace('z', 'http://www.ebi.ac.uk/z');
  $chemicals = $xml->xpath('//z:e');
  }

  return $chemicals;
}

?>