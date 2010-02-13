#!/usr/bin/perl

#use warnings;
use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;
use HashSpeciesList;

#
# Script to convert single target pathway at WikiPathways from a source file.
# User enters WP identifiers for from and to pathways
# Necessary input files: 
# Homology information from Homologene for relevant species conversion
# Homology information from BioMART for relevant species conversion
# Gene symbol information for source and target species from BioMART
# Output:
# Updates (overwrites) target pathway at WP 

#####################

#Define log files
#Tracks IDs that didn't convert and percentage conversion per pathway.
my $outfilename1 = "Log-HomologyConvert-DataNodeConversion.txt";	
unless ( open(LOGFILE1, ">$outfilename1") )
       {
         print "could not open file $outfilename1\n";
         exit;
 	}
print LOGFILE1 "Pathway\tNon-converted IDs\tRedundant Conversions\tPercent converted\n";

####################

my $day = (localtime(time))[3];
my $month = 1+(localtime(time))[4];
my $year = 1900+(localtime(time))[5];
my $date = "$month/$day/$year";
my $cutoff = "20070522222100";
my $maintbot = "MaintBot";
my $fnGPML = "GPML.xsd";

# Use this flag for testing the script on the TEST site
my testing = "false";

#Ask user for pathway ID to convert FROM
print "\nEnter the wikipathways ID of the pathway you want to convert FROM (for example WP56) : ";
my $id1 = <STDIN>;
chomp ($id1);

my $sourceid = ();
my $targetid = ();

if ($id1 =~ /^WP/)
	{
	$sourceid = $id1;
	}
elsif ($id1 =~ /^\d/)
	{
	$sourceid = "WP".$id1;
	}

#Ask user for pathway ID to convert TO
print "\nEnter the wikipathways ID of the pathway you want to convert TO (the pathway will be overwritten) : ";
my $id2 = <STDIN>;
chomp ($id2);

if ($id2 =~ /^WP/)
	{
	$targetid = $id2;
	}
elsif ($id2 =~ /^\d/)
	{
	$targetid = "WP".$id2;
	}
	
#Ask user for WP login password
print "\nEnter WikiPathways password for user $maintbot: ";
my $password = <STDIN>;
chomp ($password);

######################

#Create service interface with fault handler and login
my $wp_soap;
my $auth;

if (testing) {
$wp_soap = SOAP::Lite
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
my $wpauth = $wp_soap->login($name, $pass)->result;
$auth = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));		
} else {
$wp_soap = SOAP::Lite
        ->proxy('http://www.wikipathways.org/wpi/webservice/webservice.php')
        ->uri('http://www.wikipathways.org/webservice')
        ->on_fault(sub {
                        my $soap = shift;
                        my $res = shift;
                        if(ref($res) eq '') { die($res);}
                        else {die($res->faultstring);}
                return new SOAP::SOM;
                                } );
my $name = SOAP::Data->name(name => "MaintBot");
my $pass = SOAP::Data->name(pass => $password);
my $wpauth = $wp_soap->login($name, $pass)->result;
$auth = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));
}

######################

#Get the pathway info for source and target

print "Requesting source pathway: $sourceid\n";
print LOGFILE1 "$sourceid\t";	

my $refId = SOAP::Data->name(pwId => $sourceid);
my $rev = SOAP::Data->name(revision => '0');
my @sourceResults = $wp_soap->getPathway($refId, $rev)->paramsout;	
unshift(@sourceResults, $wp_soap->getPathway($refId, $rev)->result);
my $pathway = new PathwayTools::Pathway();
my $gpml = ();
my $sourcerevision = 0;
my $sourcespecies = ();
my $sourcename = ();
	
foreach my $ref1 (@sourceResults)
	{
	$gpml = $ref1->{gpml};
	$sourcerevision = $ref1->{revision};
	print "sourcerevision $sourcerevision\n";
	$sourcespecies = $ref1->{species};
	$sourcename = $ref1->{name};
	}

$gpml =~ m/Pathway xmlns="(.*?)"/;
my $NS = $1;

print "Requesting info for target pathway: $targetid\n";
my $refId2 = SOAP::Data->name(pwId => $targetid);
my @targetResults = $wp_soap->getPathwayInfo($refId2)->paramsout;	
unshift(@targetResults, $wp_soap->getPathwayInfo($refId2)->result);
my $targetspecies = ();
my $targetname = ();
my $targetrevision = 0;

foreach my $ref2 (@targetResults)
	{
	$targetspecies = $ref2->{species};
	$targetname = $ref2->{name};
	$targetrevision = $ref2->{revision};
	}
print "targetrevision $targetrevision\n";	

my $confirm = ();

while (!(($confirm =~ /y/) || ($confirm =~ /n/)))
	{
	print "Ready to overwrite $targetspecies pathway $targetname with $sourcespecies pathway $sourcename\n";
	print "Continue? (y/n):";	
	$confirm = <STDIN>;
	chomp $confirm;
	}

if ($confirm =~ m/y/)
	{
	print "OK, continuing....\n";
	}
elsif ($confirm =~ m/n/)
	{
	die;
	}

######################

#Translate organism names into three-letter codes

#Define an array for checking input species codes
my %speciesTable = getSpeciesTable();
my @codeArray = ();
for my $key (sort keys %speciesTable){
	unless ($speciesTable{$key}[0] =~ /^\s*$/){
		push(@codeArray, $speciesTable{$key}[0]);
	}
}

#Define and hash for ensemblnames
my %ensemblname = ();
for my $key (sort keys %speciesTable){
	$ensemblname{$speciesTable{$key}[2]} = $key;
}

my $refcode = 0;
my $targetcode = 0;
my $REFTAXID = 0;
my $TARGETTAXID = 0;

for my $key (sort keys %speciesTable){
	if ($speciesTable{$key}[0] =~ $sourcespecies){
		$refcode = $speciesTable{$key}[2];
		$REFTAXID = $speciesTable{$key}[1];
	}
	elsif ($speciesTable{$key}[0] =~ $targetspecies){
		$targetcode = $speciesTable{$key}[2];
		$TARGETTAXID = $speciesTable{$key}[1];
	}
}

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

#Loop through pathways to update, get ref pathway and convert

#mkdir("Updated");
#mkdir("New");

	
#Process gpml: Change IDs and labels using homology information, rename file and add homology label
$pathway->from_string($gpml);
my $root = $pathway->{document}->getDocumentElement();
my $pwname = $root->getAttribute("Name");
my $newname = correctNames($pwname);	
#	my $updatefilename = "Updated/$newname.gpml";
#	my $createfilename = "New/$newname.gpml";
my $nodecount = 0;
my $convertcount = 0;
my $convscore = 0;
my %seenTargets = (); #for tracking multiple conversions to the same targetlabel in a given pathway
		
$root->setAttribute("Organism", $targetspecies);
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
						$seenTargets{$targetlabel}++;
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
							$seenTargets{$targetlabel}++;
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
		} # close foreach datanode
		
		#Log pathways with potentially redundant conversions to a given targetlabel
		print LOGFILE1 "\t";
		foreach my $key (sort keys %seenTargets){
			if ($seenTargets{$key} > 1) {
				print LOGFILE1 "$key($seenTargets{$key}x) ";
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
	
		# Calculate conversion score
		if ($nodecount != 0)
		{
			$convscore = int(100*($convertcount/$nodecount));
			print "Conversion score is $convscore\n";
			print LOGFILE1 "\t$convscore\n";
		}
                else
                {       
                        print LOGFILE1 "\t\n";
                }       

		# Create a comment
		my $comment = $root->addNewChild ($NS, "Comment");
		# source attribute can be anything you want, for example "HomologyConvert"
		$comment->setAttribute ("Source", "HomologyConvert");
		# use appendText to set the text value of the comment.
		$comment->appendText ("This pathway was inferred from $sourcespecies pathway [http://www.wikipathways.org/index.php?title=Pathway:$sourceid&oldid=$sourcerevision $sourceid(r$sourcerevision)] with a $convscore\% conversion rate.");
		
		# validate
		#$pathway->validate();
		
		#Upload file to WikiPathways and save to local files
		my $description = SOAP::Data->name(description => "Converted from $sourcespecies");
		my $newgpml = $pathway->to_string();
		my $gpmlcode = SOAP::Data->name(gpml => $newgpml);
		
		my $baserevision = SOAP::Data->name(revision => $targetrevision);
				
		#if ($convscore >= 50)
		#	{
			my $uploadId = SOAP::Data->name(pwId => $targetid);
			$wp_soap->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth);
			print "$targetname updated at WikiPathways\n";
			#}

print "\n\nDone.\n";	
close LOGFILE1;

#subroutine that removes whitespaces, parenthesis, slashes and references to human
sub correctNames 
{
my $name = shift;
#Remove any references to reference species and parenthesis from pathway name. 

my $speciesname = lc($sourcespecies);
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
my ($pw, $wp_soap) = @_;
my $pwId = SOAP::Data->name(pwId => $pw);
my $timestamp = SOAP::Data->name(timestamp => $cutoff);
my $pwhistory = $wp_soap->getPathwayHistory($pwId, $timestamp)->result;
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
