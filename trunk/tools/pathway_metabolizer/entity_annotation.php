<link rel="stylesheet" type="text/css" href="tm_ebi.css">
	
<?php
print "<h1>GPML Annotator: ".$_GET["pathway"]."</h1>";

if ($_GET["pathway"] != "") {

	if (substr_count($_GET["pathway"], ".gpml") == 0) die("Not a valid gpml file. ".$_GET["pathway"]);
	 $elements = array();
	  $xml = simplexml_load_file("pathways/".$_GET["pathway"]);
	  foreach ($xml->{"DataNode"} as $datanode) {
		array_push($elements, (string) $datanode["TextLabel"]);
		 
	  }
      foreach ($xml->{"Label"} as $datanode) {
	      $item = (string) $datanode["TextLabel"];
	      array_push($elements, $item);
      }
      print "<table>";
      $metabolites = array();
      foreach ($elements as $element) {
	 	 print "<tr><td>".$element. "</td>";
	     $metabolites = whatizit_metabolites($element);
	     if (count($metabolites)>0){
			foreach ($metabolites as $metabolite) {
	         if ($metabolite == $element)
	             if ($metabolite["sem"]=="hmdbid") {
					print " <td><a href=\"http://www.hmdb.ca/scripts/show_card.cgi?METABOCARD=".$metabolite["ids"].".txt\">".$metabolite["sem"].":".$metabolite["ids"]."</a></td>";
				} else {
					if (substr_count($metabolite["ontIDs"], "CHEBI")>0)
						print "<td><a href=\"http://www.ebi.ac.uk/chebi/searchId.do?chebiId=".$metabolite["ontIDs"]."\">".$metabolite["ontIDs"]."</a></td>";
					else
					    print "<td>".$metabolite["sem"]."</td>";
				} 
		   }

	     }
	     $proteins = whatizit_uniprot($element);
	     if (count($proteins)>0){
		   foreach ($proteins as $protein) {
			  
			  if ($protein == $element) { 
		         print "<td><SPAN title=\" uniprot: ".$proteins[0]["ids"]." \" class=\"popup\">";
		         print "<a href=\"http://www.ebi.uniprot.org/uniprot-srv/elSearch.do?querytext=".$protein."\" ONMOUSEOVER=\"popup(\'Link description here\',\'yellow\')\"; ONMOUSEOUT=\"kill()\">uniprot</a>";
		         print "</span></td>";
	          } 
	          else print "<td></td>";
	}  
	}
    print "</tr>";
}
  print "</table>";
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

function whatizit_uniprot($term){
$whatizit = new SoapClient('http://www.ebi.ac.uk/webservices/whatizit/ws?wsdl');

try{
  $result = $whatizit->contact(array(
    'pipelineName' => 'whatizitSwissprotGo2',
    'text' => $term,
    'convertToHtml' => false,
    ));
} catch (SoapFault $exception) { return FALSE; }

if ($xml = simplexml_load_string($result->return)){
  $xml->registerXPathNamespace('z', 'http://www.ebi.ac.uk/z');
  $proteins = $xml->xpath('//z:uniprot');
  }

  return $proteins;
}

function whatizit_pathways($term){
$whatizit = new SoapClient('http://www.ebi.ac.uk/webservices/whatizit/ws?wsdl');

try{
  $result = $whatizit->contact(array(
    'pipelineName' => 'whatizitPathwaywiki',
    'text' => $term,
    'convertToHtml' => false,
    ));
} catch (SoapFault $exception) { return FALSE; }

if ($xml = simplexml_load_string($result->return)){
  $xml->registerXPathNamespace('z', 'http://www.ebi.ac.uk/z');
  $pathways = $xml->xpath('//genmappid');
  }

  return $pathways;
}
?>