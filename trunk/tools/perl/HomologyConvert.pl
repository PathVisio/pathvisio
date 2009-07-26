#!/usr/bin/perl

#use warnings;
use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;
use HashSpeciesList;

#
# Script to convert gpml files between species. Pathway files are collected from WP. 
# Species are enetered by user, available WP species hard-coded in %species and %taxids.
# Necessary input files: 
# Homology information from Homologene
# Homology information from BioMART
# Flatfile listing all pathways at WP (http://www.wikipathways.org/wpi/pathway_content_flatfile.php?output=tab)
# Output:
# Uploads converted pathways to WP
# Saves local copy of converted pathway
# Saves log file for conversion

#####################

#Define an array for checking input species codes
my %speciesTable = getSpeciesTable();
my @codeArray = ();
for my $key (sort keys %speciesTable){
	unless ($speciesTable{$key}[2] =~ /^\s*$/){
		push(@codeArray, $speciesTable{$key}[2]);
	}
}

#Define and hash for ensemblnames
my %ensemblname = ();
for my $key (sort keys %speciesTable){
	$ensemblname{$speciesTable{$key}[2]} = $key;
}

#Define log files
#Tracks IDs that didn't convert and percentage conversion per pathway.
my $outfilename1 = "Log-HomologyConvert-DataNodeConversion.txt";	
unless ( open(LOGFILE1, ">$outfilename1") )
       {
         print "could not open file $outfilename1\n";
         exit;
 	}
print LOGFILE1 "Species\tPathway\tNon-converted IDs\tPercent converted\n";

#Tracks non-converted pathways
my $outfilename2 = "Log-HomologyConvert-NonConverted.txt";	
unless ( open(LOGFILE2, ">$outfilename2") )
       {
         print "could not open file $outfilename2\n";
         exit;
 	}
print LOGFILE2 "Species\tPathway\n";

#Tracks all uploaded pathways
my $outfilename3 = "Log-HomologyConvert-Uploaded.txt";	
unless ( open(LOGFILE3, ">$outfilename3") )
       {
         print "could not open file $outfilename3\n";
         exit;
 	}
print LOGFILE3 "Species\tPathway\tPercent converted\n";


my $day = (localtime(time))[3];
my $month = 1+(localtime(time))[4];
my $year = 1900+(localtime(time))[5];
my $date = "$month/$day/$year";
my $cutoff = "20070522222100";
my $maintbot = "MaintBot";
my $fnGPML = "GPML.xsd";
my $allMode = 0;

#Ask user for target and ref species
my $refcode = "";
while (!(in_array(\@codeArray, $refcode)))
	{
	print "\nEnter the two-letter species code for reference species to convert FROM: ";
	$refcode = <STDIN>;
	chomp ($refcode);
        $refcode = lc($refcode);
	}

my $REFORGANISM = 0;
my $REFTAXID = 0;

for my $key (sort keys %speciesTable){
	if ($speciesTable{$key}[2] =~ $refcode){
		$REFORGANISM = $speciesTable{$key}[0];
		$REFTAXID = $speciesTable{$key}[1];
	}
}

my $tocode = "";
while (!(in_array(\@codeArray, $tocode)))
	{
	print "\nEnter the two-letter species code (or \'all\') for target species to convert TO: ";
	$tocode = <STDIN>;
	chomp ($tocode);
        $tocode = lc($tocode);
	# support 'all' mode
	if ($tocode =~ /all/)
		{
		$allMode = 1;
		$tocode = $codeArray[0];	
		} 
	else 
		{
		@codeArray = $tocode;
		}
	}

#Ask user for WP login password
print "\nEnter WikiPathways password for user $maintbot: ";
my $password = <STDIN>;
chomp ($password);

foreach my $targetcode (@codeArray) 
{

my $TARGETORGANISM = 0;
my $TARGETTAXID = 0;

for my $key (sort keys %speciesTable){
        if ($speciesTable{$key}[2] =~ $targetcode){
                $TARGETORGANISM = $speciesTable{$key}[0];
                $TARGETTAXID = $speciesTable{$key}[1];
        }
}



print "Converting from $REFORGANISM to $TARGETORGANISM\n";


######################

#Homology information:
#Define data structures to store homology information
my %idlookup = ();
my %symbollookup = ();
my %refsymbol = ();
my $ref = ();
my $refsymbol = ();
my $target = ();
my $targetsymbol =();

#Figure out which inout files to use
my $ensemblinput = "mart_".$refcode."-".$targetcode.".txt";
my $targetsymbolinput = "mart_".$targetcode.".txt";
my $refsymbolinput = "mart_".$refcode.".txt";

#Read Ensembl homology file which contains 2 columns, ref gene ID and target gene ID
unless ( open(ENSEMBL, $ensemblinput) )
        {
            print "could not open file $ensemblinput\n";
            next;
    	}

while (my $line = <ENSEMBL>)
      {
      chomp $line;
      ($ref, $target)= split("\t", $line);
 	  unless ($ref eq "" || $target eq "")
 	  		{
  	  		if (exists $idlookup{$ref}) 
  	  			{
  	  			$idlookup{$ref} = 0; #AP# overwrite with zero to skip duplicate ref IDs later on 
				}
  	  		else
  	  			{
  	  			$idlookup{$ref} = $target;
  	  			}
 	  		}
      } 

#Read homologene data.
unless ( open(HOMOLOGENE, "homologene.data") )
        {
            print "could not open file homologene.data\n";
            exit;
    	}

my %refcluster = ();
my %targetcluster = ();
    	
while (my $line = <HOMOLOGENE>)
      {
      chomp $line;
      my $cluster = (split("\t", $line))[0];
      my $taxid = (split("\t", $line))[1];
      my $id = (split("\t", $line))[2];
      $targetsymbol = (split("\t", $line))[3];
      
      if ($taxid eq $REFTAXID)
         {
          if (exists $refcluster{$cluster}) 
  	  			{
  	  			$refcluster{$cluster} = ""; #AP# overwrite with zero to skip duplicate ref IDs later on 
				}
  	  		else
  	  			{
  	  			$refcluster{$cluster} = $id;
  	  			}
         }
      elsif ($taxid eq $TARGETTAXID)
      	{
      	if (exists $targetcluster{$cluster}) 
  	  			{
  	  			$targetcluster{$cluster} = ""; #AP# overwrite with zero to skip duplicate ref IDs later on 
				}
  	  		else
  	  			{
  	  			$targetcluster{$cluster} = $id;
      			$symbollookup{$id} = $targetsymbol;
  	  			}
      	}
      }
      
#Switch Homologene data from two hashes to the existing idlookup hash, overwriting Mart data where redundant
foreach my $key (keys %{refcluster})
		{ 
		if (exists $targetcluster{$key})
			{
			$ref = $refcluster{$key};
			$target = $targetcluster{$key};
			$idlookup{$ref} = $target;
			}
		}
      
#Read symbols from Ensembl for ref and target

unless ( open(REFSYMBOL, $refsymbolinput) )
        {
            print "could not open file $refsymbolinput\n";
            next;
    	}
    	
while (my $line = <REFSYMBOL>)
      {
      chomp $line;
      ($ref, $refsymbol)= split("\t", $line);
 	  unless ($refsymbol eq "")
 	  		{
 	  		#Note: this hash is reveresed compared to others, the key is the symbol
  	  		$refsymbol{$refsymbol} = $ref;
 	  		}
      }
      
unless ( open(TARGETSYMBOL, $targetsymbolinput) )
        {
            print "could not open file $targetsymbolinput\n";
            next;
    	}
    	
while (my $line = <TARGETSYMBOL>)
      {
      chomp $line;
      ($target, $targetsymbol)= split("\t", $line);
 	  unless ($targetsymbol eq "")
 	  		{
  	  		$symbollookup{$target} = $targetsymbol;
 	  		}
      }

######################

#Read in pathway content flatfile to get all pathways for both species.
my $flatfile = "wikipathways_data_.tab";
unless ( open(FLATFILE, $flatfile) )
        {
	    #then download it
	    `wget http://www.wikipathways.org/wpi/pathway_content_flatfile.php?output=tab`;

	    #and try again
	    unless ( open(FLATFILE, $flatfile) )
	        {

        	    print "could not open file wikipathways_data_.tab\n";
            	    exit;
		}
    	}

<FLATFILE>; #skip header line
my %refs = ();
my %targets = ();

while (my $line = <FLATFILE>)
      {
      chomp $line;
      my @splitline = split (/\t/, $line);
      my $pathwayname = $splitline[0];
      my $organism = $splitline[1];
      my $url = $splitline[3];
      my $pwid = (split("Pathway:",$url))[1];
      
      #switch to lower case, remove parenthesis 
	  $pathwayname =~ s/\(//;
	  $pathwayname =~ s/\)//;
	  $pathwayname =~ tr/A-Z/a-z/;
	  
	  my $fixedname = correctNames($pathwayname);
      	  
	  if ($organism eq $REFORGANISM)
		{
			my $refname = $fixedname;
			$refs{$pwid} = $refname;
		}
	  elsif ($organism eq $TARGETORGANISM)
		{
			my $targetname = $fixedname;
			$targets{$pwid} = $targetname;
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

my $wp_soap_LIVE = SOAP::Lite
        ->proxy('http://www.wikipathways.org/wpi/webservice/webservice.php')
        ->uri('http://www.wikipathways.org/webservice')
        ->on_fault(sub {
                        my $soap = shift;
                        my $res = shift;
                        if(ref($res) eq '') { die($res);}
                        else {die($res->faultstring);}
                return new SOAP::SOM;
                                } );
my $name2 = SOAP::Data->name(name => "MaintBot");
my $pass2 = SOAP::Data->name(pass => $password);
my $wpauth2 = $wp_soap_LIVE->login($name2, $pass2)->result;
my $auth_LIVE = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth2)));

######################

#Define target pathways for update and collect corresponding revision number and ref pathway id
my %updates = (); 
my %relation = ();
my %nonconverts = ();
my %converts = ();

#Compare pw names in refs and targets
foreach my $refid (keys %refs)
		{
		foreach my $targetid (keys %targets)
				{
				if ($refs{$refid} eq $targets{$targetid})
					{
					print "Checking history for $targets{$targetid}\n";
					my $convert = checkHistory($targetid, $wp_soap_TEST);
					if ($convert eq "true")
						{
						$converts{$refid} = $refs{$refid};
						$relation{$refid} = $targetid;	
						my $pwId = SOAP::Data->name(pwId => $targetid);
						my $pwinfo = $wp_soap_TEST->getPathwayInfo($pwId)->result;
						$updates{$targetid} = $pwinfo->{revision};
						}
					elsif ($convert eq "false")
						{
						$nonconverts{$refid} = $refs{$refid};	
						}
					}
				}
		}

#Go through reference pathways and convert
foreach my $ref (keys %refs)
   {
	unless (exists $nonconverts{$ref})
	{
		$converts{$ref} = $refs{$ref};
	}
   }

######################

#Loop through pathways to update, get ref pathway and convert

mkdir("Updated");
mkdir("New");

foreach my $pw (keys %converts)
	{
	print "Requesting pathway $converts{$pw}\n";
	print LOGFILE1 "$TARGETORGANISM\t$converts{$pw}\t";	

	#Collect reference pathway from WP
	my $refId = SOAP::Data->name(pwId => $pw);
	my $rev = SOAP::Data->name(revision => '0');
	my @pathwayResults = $wp_soap_LIVE->getPathway($refId, $rev)->paramsout;	
	unshift(@pathwayResults, $wp_soap_LIVE->getPathway($refId, $rev)->result);
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
	
	#Process gpml: Change IDs and labels using homology information, rename file and add homology label
	$pathway->from_string($gpml);
	my $root = $pathway->{document}->getDocumentElement();
	my $pwname = $root->getAttribute("Name");
	my $newname = correctNames($pwname);	
	my $updatefilename = "Updated/$newname.gpml";
	my $createfilename = "New/$newname.gpml";
	my $nodecount = 0;
	my $convertcount = 0;
	my $convscore = 0;
		
	$root->setAttribute("Organism", $TARGETORGANISM);
	$root->setAttribute("Name", $newname);
	$root->setAttribute("Last-Modified", $date);
	#my $categorized = checkCategories($root);
	
	#if ($categorized eq "true")
	#{
	if ($root->getChildrenByTagName("DataNode"))
	{
	for my $datanode ($root->getChildrenByTagName("DataNode"))
		{
			my $textlabel = $datanode->getAttribute ("TextLabel");
			my $xref = ($datanode->getChildrenByTagName("Xref"))[0];
			my $id = $xref->getAttribute ("ID");
			my $type = $datanode->getAttribute ("Type");
			my $system = $xref->getAttribute ("Database");
	
			if ($type eq "GeneProduct" || $type eq "Unknown" || $type eq "Protein" || $type eq "Complex")
				{
				$nodecount ++;
				if ($type eq "Unknown")
					{
					$type = "GeneProduct";
					}
				if ($system =~ /Ensembl/ || $system eq "Entrez Gene")
					{
					if ($idlookup{$id}) #AP# if value is true (i.e., non-zero)
  	  					{
  	  					$convertcount ++;
  	  					my $targetID = $idlookup{$id};
						my $targetlabel = $symbollookup{$targetID};
						$xref->setAttribute ("ID", $targetID);
						$datanode->setAttribute ("BackpageHead", $targetlabel);
						$datanode->setAttribute ("Type", $type);
						if ($system =~ /Ensembl/)
							{
							$system = "Ensembl ".$ensemblname{$targetcode};
							$xref->setAttribute ("Database", $system);
							}
						unless ($targetlabel eq "")
							{
							$datanode->setAttribute("TextLabel", $targetlabel);
							}
						}
					#If gene is annotated with ref symbol, lookup ref id and then assign targetid
					elsif ($id=~ /^\D$/)
						{
						if (exists $refsymbol{$id})
							{
							$convertcount ++;
							my $numericid = $refsymbol{$id};
							my $targetID = $idlookup{$numericid};
							my $targetlabel = $symbollookup{$targetID};
							$xref->setAttribute ("ID", $targetID);
							$datanode->setAttribute ("BackpageHead", $targetlabel);
							$datanode->setAttribute ("Type", $type);
							unless ($targetlabel eq "")
								{
								$datanode->setAttribute("TextLabel", $targetlabel);
								}
							}
						}
  	  				else
  	  					{
  	  					my $targetID = "";
						$system = "";
						$xref->setAttribute ("ID", $targetID);
						$xref->setAttribute ("Database", $system);
  	  					$datanode->setAttribute ("Type", $type);
  	  					$datanode->setAttribute ("BackpageHead", "");
						print LOGFILE1 "$textlabel|$id ";	
  	  					}
					}
				else
					{
					my $targetID = "";
					$system = "";
					$xref->setAttribute ("ID", $targetID);
  	  				$datanode->setAttribute ("Type", $type);
  	  				$datanode->setAttribute ("BackpageHead", "");
					print LOGFILE1 "$textlabel|$id ";
					}
				}
			}
		}
		
		#Clean up empty Comments to avoid truncated tags
		if ($root->getChildrenByTagName("Comment"))
		{
		for my $datanode ($root->getChildrenByTagName("Comment"))
			{
				my $comment = $datanode->textContent;
				#delete empty Comment
				if ($comment eq "")
				{
				$root->removeChild ($datanode);
				}
			}
		}
		
		#Clean up empty bp attributes
		if ($root->getChildrenByTagName("Biopax"))
		{
		for my $datanode ($root->getChildrenByTagName("Biopax"))
			{
				if ($datanode->getChildrenByTagName("bp:PublicationXRef"))
					{
					for my $xref ($datanode->getChildrenByTagName("bp:PublicationXRef"))
						{
						my @childnodes = $xref->childNodes;
						foreach my $child (@childnodes)
							{
							my $content = $child->textContent;
							if ($content eq "")
								{
								$xref->removeChild ($child);
								}
							}
						}
					}
			}
		}
		else 
			{
			print LOGFILE1 "\n";
			}
	
		# Calculate conversion score
		if ($nodecount != 0)
			{
			$convscore = int(100*($convertcount/$nodecount));
			}	
		print LOGFILE1 "\t$convscore\n";
		
		# Create a comment
		my $comment = $root->addNewChild ($NS, "Comment");
		# source attribute can be anything you want, for example "HomologyConvert"
		$comment->setAttribute ("Source", "HomologyConvert");
		# use appendText to set the text value of the comment.
		$comment->appendText ("This pathway was converted from $REFORGANISM with a conversion score of $convscore\%");
		
		# validate
		#$pathway->validate();
		
		#Upload file to WikiPathways and save to local files
		my $description = SOAP::Data->name(description => "Converted from $REFORGANISM");
		my $newgpml = $pathway->to_string();
		my $gpmlcode = SOAP::Data->name(gpml => $newgpml);
		
		my $baserevision = SOAP::Data->name(revision => $updates{($relation{$pw})});
				
		if ($convscore >= 50)
			{
			if ($relation{$pw})
				{
				#Print converted pathway to local file and upload to WP
				print LOGFILE3 "$TARGETORGANISM\t$converts{$pw}\t$convscore\n";
				print "$converts{$pw} updated at WP and written to file\n";
				my $uploadId = SOAP::Data->name(pwId => $relation{$pw});
				$pathway->to_file($updatefilename);
				$wp_soap_TEST->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth_TEST);
				}
			else 
				{
				#Print new pathway to local file and upload to WP
				print LOGFILE3 "$TARGETORGANISM\t$converts{$pw}\t$convscore\n";
				$pathway->to_file($createfilename);
				print "$converts{$pw} created at WP and written to file\n";
				$wp_soap_TEST->createPathway($gpmlcode, $auth_TEST);
				}
		}
	}
      #	}
	
print LOGFILE3 "\n";

#Write non-converted pathways to log file
foreach my $c (keys %nonconverts)
	{
	print LOGFILE2 "$TARGETORGANISM\t$nonconverts{$c}\n";
	}

} # close foreach targetcode

print "\n\nDone.\n";	
close LOGFILE1;
close LOGFILE2;
close LOGFILE3;
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
$name =~ s/\(\)//;
$name =~s/\s+$//;
$name =~ s/^\s+//;
$name =~ s/\// /;
return $name;
}

#subroutine that removes special characters
sub remCharacter 
{
my $text = shift;

$text =~ s/amp;//; 
$text =~ s/&//; 

return $text;
}

#subroutine that checks if any user other than MaintBot has made changes
sub checkHistory{
my ($pw, $wp_soap_TEST) = @_;
my $pwId = SOAP::Data->name(pwId => $pw);
my $timestamp = SOAP::Data->name(timestamp => $cutoff);
my $pwhistory = $wp_soap_TEST->getPathwayHistory($pwId, $timestamp)->result;
my $update = "true";

foreach my $key (keys %$pwhistory) 
						{
							foreach my $history ($pwhistory->{history})
								{
								if (ref($history) eq "ARRAY")
									{
									foreach my $row (@$history)
										{
										foreach my $info (keys %$row)
											{
											if ($info =~ /^user$/)
												{
												if ($row->{$info} ne $maintbot)
													{
													$update = "false";
													}
												}
											}
										}
									}
								else
									{
									foreach my $info1 (keys %$history)
										{
										if ($info1 =~ /^user$/)
											{
											if ($history->{$info1} ne $maintbot)
												{
												$update = "false";
												}
											}
										}
									} 
								}
						
						}
return $update;
}

#subroutine that checks if a pathway is categorized
sub checkCategories
{
my $pw = shift;
my $wpcategories = "false";

if ($pw->getChildrenByTagName("Comment"))
	{
	foreach my $comment ($pw->getChildrenByTagName("Comment"))
		{
		my $source = $comment->getAttribute("Source");
		if ($source eq "WikiPathways-category")
			{
			$wpcategories = "true";
			}
		}
	}
return $wpcategories;
}

sub in_array
 {
     my ($arr,$search_for) = @_;
     my %items = map {$_ => 1} @$arr; # create a hash out of the array values
     return (exists($items{$search_for}))?1:0;
 }
