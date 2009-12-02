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
# Pathway files are collected from WP. 
# Necessary input files: 
# Entrez gene table with ID and symbol info
# Flatfile listing all pathways at WP (http://www.wikipathways.org/wpi/pathway_content_flatfile.php?output=tab)
# Output:
# Uploads improved pathways to WP
# Saves local copy of converted pathway
# Saves log file for conversion

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

my $day = (localtime(time))[3];
my $month = 1+(localtime(time))[4];
my $year = 1900+(localtime(time))[5];
my $date = "$month/$day/$year";
my $cutoff = "20070522222100";
my $maintbot = "MaintBot";
my $fnGPML = "GPML.xsd";

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

#Keepp track of bad labels that are scrubbed
my $outfilename2 = $TWOLETTERCODE."-Labels.txt";	
unless ( open(LOGFILE2, ">$outfilename2") )
       {
         print "could not open file $outfilename2\n";
         exit;
 	}
print LOGFILE2 "Pathway\tLabels\n";

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

#Create service interface with fault handler and login
#TODO: change from test server to main wp server
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

my $newdir = "New/".$refcode;
mkdir($newdir);

# Get list of all pathways for relevant species

print "Getting list of $REFORGANISM pathways from WikiPathways\n";
my $species = SOAP::Data->name(organism => $REFORGANISM);
#my @pathways = $wp_soap_LIVE->listPathways($species)->paramsout;
#unshift(@pathways, $wp_soap_LIVE->listPathways($species)->result);
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
	print LOGFILE2 "$pathways{$pw}\t";

	#Collect pathway from WP
	my $refId = SOAP::Data->name(pwId => $pw);
	my $rev = SOAP::Data->name(revision => '0');
	my @pathwayResults = $wp_soap_TEST->getPathway($refId, $rev)->paramsout;	
	unshift(@pathwayResults, $wp_soap_TEST->getPathway($refId, $rev)->result);
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
	my $pwname = $root->getAttribute("Name");
	my $newname = correctNames($pwname);	
	#my $createfilename = "$newdir/$newname.gpml";
	my $updatecount = 0; # Keep track of if nodes are updated
	my $emptynodes = 0; #Keep track of number of empty nodes per pathway
	my $metupdatecount = 0; #Keep track of mislabeled metabolites that need to be updated
		
	#$root->setAttribute("Name", $newname);
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
		
print LOGFILE2 "\n";
print LOGFILE1 "\t$emptynodes\t$updatecount\t$metupdatecount\n";

#Upload file to WikiPathways and save to local file
my $description = SOAP::Data->name(description => "Automatic update of empty xrefs");
my $newgpml = $pathway->to_string();
my $gpmlcode = SOAP::Data->name(gpml => $newgpml);
my $baserevision = SOAP::Data->name(revision => $revision);
				
if ($updatecount > 0 || $metupdatecount > 0)
	{
	#Print converted pathway to local file and upload to WP
	print "$pathways{$pw} updated at WP\n";
	#print "$newgpml\n\n";
	my $uploadId = SOAP::Data->name(pwId => $pw);
	$wp_soap_TEST->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth_TEST);
	#$pathway->to_file($createfilename);
	}
	
} #close foreach pathway
      
print "\n\nDone.\n";	
close LOGFILE1;
close LOGFILE2;
close FLATFILE;

#subroutine that removes whitespaces, parenthesis, slashes and references to human
sub correctNames 
{
my $name = shift;
#Remove any references to reference species and parenthesis from pathway name. 
my $speciesname = lc($REFORGANISM);
my $commonname = lc($ensemblname{$refcode});
$name =~ s/$speciesname//i; 
$name =~ s/$commonname//i; 

#Remove whitespace etc.
$name =~ s/\(\)//;
$name =~s/\s+$//;
$name =~ s/^\s+//;
$name =~ s/\-&gt;/-/; 

return $name;
}

sub in_array
 {
     my ($arr,$search_for) = @_;
     my %items = map {$_ => 1} @$arr; # create a hash out of the array values
     return (exists($items{$search_for}))?1:0;
 }
 
#subroutine that removes special characters. Currently not used
sub remCharacter 
{
my $text = shift;

$text =~ s/&#xfffd;/ /g;
$text =~ s/&#x3ba;/ /g;
$text =~ s/&#x3b1;/ /g;

return $text;
}
