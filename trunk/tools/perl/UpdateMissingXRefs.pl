#!/usr/bin/perl

#use warnings;
#use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;
use HashSpeciesList;
use Cwd;

#
# Script to fill in missing xrefs in pathways, based on xref label and Entrez gene tables.
# Also changes metabolites that are labeled as GeneProduct to Metabolite, based on Xref
# Pathway files are collected from WP. 
#
# Note that script contains code for running on both test and live site, with the live site
# commented out.
#
# Necessary input files: 
# Species-specific Entrez gene table with ID and symbol info (in 2nd and 3rd column, respectively)
# File should be namedas follows: lower case two-letter species code followed by -Entrez, extension .txt
# Example: hs-Entrez.txt
#
# Output:
# Uploads updated pathways to WP
# Saves local log file

#####################

#Define an array for checking input species codes (two-letter codes)
my %speciesTable = getSpeciesTable();
my @codeArray = ();
for my $key (sort keys %speciesTable){
	unless ($speciesTable{$key}[3] =~ /^\s*$/){
		push(@codeArray, lc($speciesTable{$key}[3]));
	}
}

#Define and hash for ensemblnames
my %ensemblname = ();
for my $key (sort keys %speciesTable){
	$ensemblname{$speciesTable{$key}[2]} = $key;
}

#Ask user for species
my $refcode = "";
while (!(in_array(\@codeArray, $refcode)))
	{
	print "\nEnter the two-letter species code for the relevant pathways: ";
	$refcode = <STDIN>;
	chomp ($refcode);
    $refcode = lc($refcode);
	}

my $REFORGANISM = 0;
my $REFTAXID = 0;
my $TWOLETTERCODE = 0;

for my $key (sort keys %speciesTable){
	if ($speciesTable{$key}[3] =~ m/^$refcode$/i){
		$REFORGANISM = $speciesTable{$key}[0];
		$REFTAXID = $speciesTable{$key}[1];
		$TWOLETTERCODE = $speciesTable{$key}[3];
	}
}

#Define log files
#Tracks labels of IDs that were not updated.
my $outfilename1 = $TWOLETTERCODE."-Log-UpdateXRefs.txt";	
unless ( open(LOGFILE1, ">$outfilename1") )
       {
         print "could not open file $outfilename1\n";
         exit;
 	}
print LOGFILE1 "Species\tPathway\tAdded xrefs\tEmpty nodes\tNumber of updated nodes\tNumber of updated metabolites\n";

###########

print "Updating pathways for $REFORGANISM\n";

#Ask user for WP login password
print "\nEnter WikiPathways password for user $maintbot: ";
my $password = <STDIN>;
chomp ($password);

######################

#Define data structures to store gene information
my %symbollookup = ();
my $xref = ();
my $symbol = ();
my $lcsymbol = ();

#Figure out which input file to use
my $entrezinput = $refcode."-Entrez.txt";

#Read Entrez file which contains 2 columns, gene ID and symbol
unless ( open(ENTREZ, $entrezinput))
        {
            print "could not open file $entrezinput\n";
            next;
    	}

while (my $line = <ENTREZ>)
      {
      chomp $line;
      $xref = (split("\t", $line))[1];
      $symbol = (split("\t", $line))[2];
      $lcsymbol = lc($symbol);
 	  unless ($xref eq "" || $symbol eq "")
 	  		{
  	  		if (exists $symbollookup{$symbol}) 
  	  			{
  	  			$symbollookup{$lcsymbol} = [0,0]; #AP# overwrite with zero to skip duplicate ref IDs later on 
				}
  	  		else
  	  			{
  	  			$symbollookup{$lcsymbol} = [$xref, $symbol];
  	  			}
 	  		}
      }

######################

my $maintbot = "MaintBot";

#Create service interface with fault handler and login

my $wp_soap_TEST = SOAP::Lite
        ->proxy('http://137.120.14.24/wikipathways-test/wpi/webservice/webservice.php')
        ->uri('http://www.wikipathways.org/webservice')
        ->on_fault(sub {
                	my $soap = shift;
                	my $res = shift;
                	# Map faults to exceptions
                	if(ref($res) eq '') { die($res);} 
                	else {die($res->faultstring);}
                return new SOAP::SOM;
           			} );

my $name = SOAP::Data->name(name => "MaintBot"); 
my $pass = SOAP::Data->name(pass => $password);
my $wpauth = $wp_soap_TEST->login($name, $pass)->result;
my $auth_TEST = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));			

#my $wp_soap_LIVE = SOAP::Lite
#        ->proxy('http://www.wikipathways.org/wpi/webservice/webservice.php')
#        ->uri('http://www.wikipathways.org/webservice')
#        ->on_fault(sub {
#                        my $soap = shift;
#                        my $res = shift;
#                        if(ref($res) eq '') { die($res);}
#                        else {die($res->faultstring);}
#                return new SOAP::SOM;
#                                } );
#my $name2 = SOAP::Data->name(name => "MaintBot");
#my $pass2 = SOAP::Data->name(pass => $password);
#my $wpauth2 = $wp_soap_LIVE->login($name2, $pass2)->result;
#my $auth_LIVE = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth2)));

######################

# Get list of all pathways for relevant species
print "Getting list of $REFORGANISM pathways from WikiPathways\n";
my $species = SOAP::Data->name(organism => $REFORGANISM);
#my @pathwaylist = $wp_soap_LIVE->listPathways($species)->paramsout;
#unshift(@pathwaylist, $wp_soap_LIVE->listPathways($species)->result);
my @pathwaylist = $wp_soap_TEST->listPathways($species)->paramsout;
unshift(@pathwaylist, $wp_soap_TEST->listPathways($species)->result);

my %pathways = ();

foreach my $pathway (@pathwaylist)
{
	$pathways{$pathway->{id}} = $pathway->{name};
}

# Loop through pathways to update, get ref pathway and convert
foreach my $pw (keys %pathways)
	{
	print "Requesting pathway $pw: $pathways{$pw}\n";
	print LOGFILE1 "$REFORGANISM\t$pathways{$pw}\t";	

	#Collect pathway from WP
	my $refId = SOAP::Data->name(pwId => $pw);
	my $rev = SOAP::Data->name(revision => '0');
	my @pathwayResults = $wp_soap_TEST->getPathway($refId, $rev)->paramsout;	
	unshift(@pathwayResults, $wp_soap_TEST->getPathway($refId, $rev)->result);
#	my @pathwayResults = $wp_soap_LIVE->getPathway($refId, $rev)->paramsout;	
#	unshift(@pathwayResults, $wp_soap_LIVE->getPathway($refId, $rev)->result);
	my $pathway = new PathwayTools::Pathway();
	my $gpml = ();
	my $revision = ();
	
	foreach my $reference (@pathwayResults)
		{
		$gpml = $reference->{gpml};
		$revision = $reference->{revision};
		}

	$gpml =~ m/Pathway xmlns="(.*?)"/;
	my $NS = $1;
	
	#Process gpml: Update empty IDs using entrez information, rename file
	$pathway->from_string($gpml);
	my $root = $pathway->{document}->getDocumentElement();
	my $updatecount = 0; # Keep track of if nodes are updated
	my $emptynodes = 0; #Keep track of number of empty nodes per pathway
	my $metupdatecount = 0; #Keep track of mislabeled metabolites that need to be updated
	$root->setAttribute("Last-Modified", $date);
	
	if ($root->getChildrenByTagName("DataNode"))
	{
	for my $datanode ($root->getChildrenByTagName("DataNode"))
		{
			my $textlabel = lc($datanode->getAttribute ("TextLabel")); #need lowercase for checking against symbolloockup
			my $xref = ($datanode->getChildrenByTagName("Xref"))[0];
			my $id = $xref->getAttribute ("ID");
			my $type = $datanode->getAttribute ("Type");
			my $system = $xref->getAttribute ("Database");
	
			if ($type eq "GeneProduct" || $type eq "Unknown" || $type eq "Protein" || $type eq "Complex")
				{
				if ($system eq "CAS" || $system eq "HMDB" || $system eq "Kegg Compound" 
				|| $system eq "Chemspider" || $system eq "ChEBI" || $system eq "PubChem")
					{
					$type = "Metabolite";
					$datanode->setAttribute ("Type", $type);
					$metupdatecount ++;
					}
				else 
					{
					if ($id eq "")
						{
						$emptynodes ++;
						if ($symbollookup{$textlabel}) #use lowercase textlabel for lookup since key is lowercase
  	  						{
  	  						$updatecount ++;
  	  						$system = "Entrez Gene";
  	  						my $targetID = $symbollookup{$textlabel}[0];
  	  						$textlabel = $symbollookup{$textlabel}[1]; #use actual gene symbol for node label
							$xref->setAttribute ("ID", $targetID);
							$datanode->setAttribute ("BackpageHead", $textlabel);
							$xref->setAttribute ("Database", $system);
							print LOGFILE1 " $textlabel|$targetID";
							}
						}
					}
				}
		}# close foreach datanode
	}
		
print LOGFILE1 "\t$emptynodes\t$updatecount\t$metupdatecount\n";

#Upload file to WikiPathways and save to local file
my $description = SOAP::Data->name(description => "Automatic update of empty xrefs");
my $newgpml = $pathway->to_string();
$newgpml = "<![CDATA[$newgpml]]>"; #Encapsulate in CDATA tag instead of base64 encoding
my $gpmlcode = SOAP::Data->name(gpml => $newgpml)->type("xsd:string");
my $baserevision = SOAP::Data->name(revision => $revision);

#Upload only those files with modifications to WP			
if ($updatecount > 0 || $metupdatecount > 0)
	{
	print "$pathways{$pw} updated at WP\n";
	my $uploadId = SOAP::Data->name(pwId => $pw);
	$wp_soap_TEST->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth_TEST);
	#$wp_soap_LIVE->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth_LIVE);
	}
	
} #close foreach pathway
      
print "\n\nDone.\n";	
close LOGFILE1;
close FLATFILE;

sub in_array
 {
     my ($arr,$search_for) = @_;
     my %items = map {$_ => 1} @$arr; # create a hash out of the array values
     return (exists($items{$search_for}))?1:0;
 }
