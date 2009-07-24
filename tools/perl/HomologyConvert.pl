#!/usr/bin/perl

#use warnings;
#use strict;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use PathwayTools;
use SOAP::Lite;

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

#Define parameters for conversion. Change these to correct history cutoff date (original upload) and available species.
my %species = ("hsa"=>"Homo sapiens", "human"=>"Homo sapiens", "rat"=>"Rattus norvegicus", "rno"=>"Rattus norvegicus", 
"mmu"=>"Mus musculus", "mouse"=>"Mus musculus", "sce"=>"Saccharomyces cerevisiae", "yeast"=>"Saccharomyces cerevisiae",
"cel"=>"Caenorhabditis elegans", "worm"=>"Caenorhabditis elegans", "celegans"=>"Caenorhabditis elegans",
"dme"=>"Drosophila melanogaster", "fruitfly"=>"Drosophila melanogaster", "osa"=>"Oryza sativa",
"rice"=>"Oryza sativa", "aga"=>"Anopheles gambiae", "mosquito"=>"Anopheles gambiae", "chimp"=>"Pan troglodytes",
"ptr"=>"Pan troglodytes","eca"=>"Equus caballus", "horse"=>"Equus caballus", "ata"=>"Arabidopsis thaliana",
"arabidopsis"=>"Arabidopsis thaliana", "bsu"=>"Bacillus subtilis", "bsubtilis"=>"Bacillus subtilis", 
"bta"=>"Bos taurus", "cow"=>"Bos taurus", "cfa"=>"Canis familiaris", "dog"=>"Canis familiaris", "dre"=>"Danio rerio",
"zebrafish"=>"Danio rerio", "eco"=>"Escherichia coli", "ecoli"=>"Escherichia coli", "gga"=>"Gallus gallus", 
"chicken"=>"Gallus gallus", "xtr"=>"Xenopus tropicalis", "xenopus"=>"Xenopus tropicalis");

my %taxids = ("hsa"=>"9606", "rno"=>"10116", "mmu"=>"10090", "sce"=>"4932", "cel"=>"6239",
"dme"=>"7227", "osa"=>"4530", "aga"=>"7165", "ptr"=>"9598","eca"=>"9796", "ata"=>"3702",  
"bsu"=>"1423", "bta"=>"9913", "cfa"=>"9615", "dre"=>"7955", "eco"=>"562", "gga"=>"9031", "xtr"=>"8364");

my %codes = ("Homo sapiens"=>"hsa", "Rattus norvegicus"=>"rno", "Mus musculus"=>"mmu",
"Saccharomyces cerevisiae"=>"sce","Caenorhabditis elegans"=>"cel", "Drosophila melanogaster"=>"dme", 
"Oryza sativa"=>"osa", "Anopheles gambiae"=>"osa", "Pan troglodytes"=>"ptr", "Equus caballus"=>"eca",
"Arabidopsis thaliana"=>"ata", "Bacillus subtilis"=>"bsu", "Bos taurus"=>"bta", "Canis familiaris"=>"cfa", 
"Danio rerio"=>"dre", "Escherichia coli"=>"eco", "Gallus gallus"=>"aga", "Xenopus tropicalis"=>"xtr");

my %ensemblname = ("hsa"=>"Human", "rno"=>"Rat", "mmu"=>"Mouse", "sce"=>"Yeast", "cel"=>"C. elegans",
"dme"=>"Fruitfly", "osa"=>"Rice", "aga"=>"Mosquito", "ptr"=>"Chimp","eca"=>"Horse", "ata"=>"Arabidopsis",  
"bsu"=>"B. subtilis", "bta"=>"Cow", "cfa"=>"Dog", "dre"=>"Zebrafish", "eco"=>"E. coli", "gga"=>"Chicken",
"xtr"=>"Xenopus");

my $day = (localtime(time))[3];
my $month = 1+(localtime(time))[4];
my $year = 1900+(localtime(time))[5];
my $date = "$month/$day/$year";
my $cutoff = "20070522222100";
my $maintbot = "MaintBot";
my $fnGPML = "GPML.xsd";

#Ask user for target and ref species
my $refcode = "";
while (!$species{$refcode})
	{
	print "\nEnter the three-letter (Unigene) species code or common name for reference species to convert FROM: ";
	$refcode = <STDIN>;
	chomp ($refcode);
	$refcode =~ tr/A-Z/a-z/;
	$refcode =~s/\s//;
	$refcode =~s/\.//;
	}
my $REFORGANISM = $species{$refcode};
$refcode = $codes{$REFORGANISM};
my $REFTAXID = $taxids{$refcode};

my $targetcode = "";
while (!$species{$targetcode})
	{
	print "\nEnter the three-letter (Unigene) species code or common name for target species to convert TO: ";
	$targetcode = <STDIN>;
	chomp ($targetcode);
	$targetcode =~ tr/A-Z/a-z/;
	$targetcode =~s/\s//;
	$targetcode =~s/\.//;
	}
my $TARGETORGANISM = $species{$targetcode};
$targetcode = $codes{$TARGETORGANISM};
my $TARGETTAXID = $taxids{$targetcode};

#Ask user for WP login password
print "\nEnter WikiPathways password for user $maintbot: ";
my $password = <STDIN>;
chomp ($password);

print "Converting from $REFORGANISM to $TARGETORGANISM\n";

#Define log files
#Tracks IDs that didn't convert and percentage conversion per pathway.
my $outfilename1 = "HomologyConvert-DataNodeConversion.txt";	
unless ( open(LOGFILE1, ">$outfilename1") )
       {
         print "could not open file $outfilename1\n";
         exit;
 	}
print LOGFILE1 "Pathway\tNon-converted IDs\tPercent converted\n";

#Tracks non-converted pathways
my $outfilename2 = "HomologConvert-NonConverted.txt";	
unless ( open(LOGFILE2, ">$outfilename2") )
       {
         print "could not open file $outfilename2\n";
         exit;
 	}
print LOGFILE2 "Pathway\n";

#Tracks all uploaded pathways
my $outfilename3 = "HomologConvert-Uploaded.txt";	
unless ( open(LOGFILE3, ">$outfilename3") )
       {
         print "could not open file $outfilename3\n";
         exit;
 	}
print LOGFILE3 "Pathway\tPercent converted\n";

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
            exit;
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
            exit;
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
            exit;
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
unless ( open(FLATFILE, "wikipathways_data_validate.tab") )
        {
            print "could not open file wikipathways_data_validate.tab\n";
            exit;
    	}

<FLATFILE>; #skip header line
my %refs = ();
my %targets = ();

while (my $line = <FLATFILE>)
      {
      chomp $line;
      my $pathwayname = (split (/\t/, $line))[0];
      my $organism = (split (/\t/, $line))[1];
      my $url = (split (/\t/, $line))[3];
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
my $wp_soap = SOAP::Lite
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
my $auth = SOAP::Data->name('auth' => \SOAP::Data->value(SOAP::Data->name('user' => 'MaintBot'), SOAP::Data->name('key', $wpauth)));			

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
					my $convert = checkHistory($targetid);
					if ($convert eq "true")
						{
						$converts{$refid} = $refs{$refid};
						$relation{$refid} = $targetid;	
						my $pwId = SOAP::Data->name(pwId => $targetid);
						my $pwinfo = $wp_soap->getPathwayInfo($pwId)->result;
						$updates{$targetid} = $pwinfo->{revision};
						}
					elsif ($convert eq "false")
						{
						$nonconverts{$refid} = $refs{$refid};	
						}
					}
				}
		}

#Go through reference pathways and collect tags
foreach my $ref (keys %refs)
	{
	unless (exists $nonconverts{$ref})
	{
	print "Checking tags for $refs{$ref}\n";
	my $convert = "true";
	my $pwId = SOAP::Data->name(pwId => $ref);
	my @tags = $wp_soap->getCurationTags($pwId)->paramsout;
	unshift(@tags, $wp_soap->getCurationTags($pwId)->result);
		
	#loop through tags
	foreach my $tag (@tags) 
		{
		if((($tag->{displayName}) eq "Proposed deletion") || (($tag->{displayName}) eq "Tutorial pathway") 
		|| (($tag->{displayName}) eq "Inappropriate content") || (($tag->{displayName}) eq "Under construction"))
			{
			$convert = "false";
			}
		}
		
		
		if ($convert eq "true")
			{
			$converts{$ref} = $refs{$ref};
			}
		elsif ($convert eq "false")
			{
			$nonconverts{$ref} = $refs{$ref};
			}		
		}
	}

######################

#Loop through pathways to update, get ref pathway and convert

mkdir("Updated");
mkdir("New");

foreach my $pw (keys %converts)
	{
	print "Requesting pathway $refs{$pw}\n";
	print LOGFILE1 "$refs{$pw}\t";	

	#Collect reference pathway from WP
	my $refId = SOAP::Data->name(pwId => $pw);
	my $rev = SOAP::Data->name(revision => '0');
	my @pathwayResults = $wp_soap->getPathway($refId, $rev)->paramsout;	
	unshift(@pathwayResults, $wp_soap->getPathway($refId, $rev)->result);
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
	my $categorized = checkCategories($root);
	
	if ($categorized eq "true")
	{
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
		$pathway->validate();
		
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
				print LOGFILE3 "$refs{$pw}\t$convscore\n";
				print "$refs{$pw} updated at WP and written to file\n";
				my $uploadId = SOAP::Data->name(pwId => $relation{$pw});
				$pathway->to_file($updatefilename);
				$wp_soap->updatePathway($uploadId, $description, $gpmlcode, $baserevision, $auth);
				}
			else 
				{
				#Print new pathway to local file and upload to WP
				print LOGFILE3 "$refs{$pw}\t$convscore\n";
				$pathway->to_file($createfilename);
				print "$refs{$pw} created at WP and written to file\n";
				$wp_soap->createPathway($gpmlcode, $auth);
				}
		}
	}
	}
	
print LOGFILE3 "\n";

#Write non-converted pathways to log file
foreach my $c (keys %nonconverts)
	{
	print LOGFILE2 "$nonconverts{$c}\n";
	}

print "\n\nDone.\n";	
close LOGFILE1;
close LOGFILE2;
close LOGFILE3;
close FLATFILE;

#subroutine that removes whitespaces, parenthesis, slashes and references to human
sub correctNames ($)
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
sub remCharacter ($)
{
my $text = shift;

$text =~ s/amp;//; 
$text =~ s/&//; 

return $text;
}

#subroutine that checks if any user other than MaintBot has made changes
sub checkHistory($)
{
my $pw = shift;
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
sub checkCategories($)
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
