#!/usr/bin/perl

use strict;
use warnings;
use Switch;
use Cwd;

#### This script parses html pathway output from PathVisio into the MediaWiki ImageMap format 
#### (http://www.mediawiki.org/wiki/Extension:ImageMap). It also changes hyperlinks, so that 
#### any Entrez ID links to the corresponding Gene Wiki entry using the Gene Wiki Entrez Linker
#### plugin (http://plugins.gnf.org/cgi-bin/wp.cgi). Hyperlinks for other IDs are changed to
#### point to the corresponding entry at the resource.
#### This script is specifically designed to process human pathways only.

#### Input 
#### Single html file, folder containing backpage information (titled "backpage"). 
#### The html file and backpage folder need to be in the same directory

#### Output
#### new html file with imagemap in new format, with the prefix "new-"

## Define links to resources
my $genewiki = "http://plugins.gnf.org/cgi-bin/wp.cgi?id=";
my $chebi = "http://www.ebi.ac.uk/chebi/searchId=CHEBI:";
my $hmdb = "http://www.hmdb.ca/metabolites/";
my $cas = "http://chem.sis.nlm.nih.gov/chemidplus/direct.jsp?regno=";
my $ensembl = "http://www.ensembl.org/Homo_sapiens/Search/Summary?_q=";
my $uniprot = "http://www.expasy.org/uniprot/";
my $unigene = "http://www.ncbi.nlm.nih.gov/UniGene/clust.cgi?UGID=1548618&SEARCH=";
my $keggcomp = "http://www.genome.jp/dbget-bin/www_bget?cpd:";
my $hugo = "http://www.genenames.org/data/hgnc_data.php?hgnc_id=";
my $ec = "http://www.brenda-enzymes.info/php/result_flat.php4?ecno=";

#### Find file			
my $html = <*.html>;
my $pathway = (split('\.', $html))[0];

#### Define output file
my $outfile = "new-".$html;

unless ( open(OUTFILE, ">$outfile") )
        	{
          	#Open failed
         	print "could not open file $outfile\n";
         	exit;
 			}

#### Process the file

print "Processing pathway $pathway\n";

unless ( open(HTML, $html) )
      {
      # Open failed
        print "could not open file $html\n";
        exit;
       }

print OUTFILE "<imagemap>\n"; 

while (my $line = <HTML>)
      {
      if (!($line =~ m/href/))
      	{
      	if ($line =~ m/image/)
      		{
      		$line =~ s/<HTML><BODY><IMG src="/Image:/;
      		$line =~ s/" usemap="#pathwaymap".+IMG>/|alt=/;
    		chomp $line;
     		$line .= $pathway;
     		$line .="\n\n";
      		}
     	else{
     		$line =~ s/<\/MAP><\/BODY><\/HTML>//;
     		}
      	print OUTFILE "$line";
      	}
      elsif ($line =~ m/href/)
      	{
      	if ($line =~ m/^<MAP name="pathwaymap">/) # remove trailing syntax in first line
      		{
      		$line =~ s/<MAP name="pathwaymap">//;
      		}
      	$line =~ m/href="(.+)" alt/;
      	my $link = $1;
      	$link =~ m/backpage\/(.+).html/;
      	my $xref = $1;
      	$xref =~ m/(.+)_/;
      	my $system = $1;
      	my $newlink;
      	my $id;
      	
      	switch($system)
      		{
      		case "L"	
      			{ 
      			#print "system is Entrez\n";
      			$xref =~ m/L_(.+)/;
      			$id = $1;
         		$newlink = $genewiki.$id;
      			}
      		case "EnHs"
      			{
      			#print "system is Ensembl\n";
      			$xref =~ m/EnHs_(.+)/;
      			$id = $1;
      			my $entrez = EntrezLookup($xref); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $ensembl.$id;
      				}
      			else
      				{
      				$id = $entrez;
      				$newlink = $genewiki.$id;
      				}
      			}
      		case "S"
      			{
      			#print "system is Uniprot\n";
      			$xref =~ m/S_(.+)/;
      			$id = $1;
      			my $entrez = EntrezLookup($xref); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $uniprot.$id;
      				}
      			else
      				{
      				$id = $entrez;
      				$newlink = $genewiki.$entrez;
      				}
      			}
      		case "U"
      			{
      			#print "system is Unigene\n";
      			$xref =~ m/U_(.+)/;
      			$id = $1;
      			my $entrez = EntrezLookup($xref); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $unigene.$id;
      				}
      			else
      				{
      				$id = $entrez;
      				$newlink = $genewiki.$entrez;
      				}
      			}
      		case "H"
      			{
      			#print "system is HUGO\n";
      			$xref =~ m/H_(.+)/;
      			$id = $1;
      			my $entrez = EntrezLookup($xref); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $hugo.$id;
      				}
      			else
      				{
      				$id = $entrez;
      				$newlink = $genewiki.$entrez;
      				}
      			}
      		case "Ch"
      			{
      			#print "system is HMDB\n";
      			$xref =~ m/Ch_(.+)/;
      			$id = $1;
      			$newlink = $hmdb.$id;
      			}
      		case "Ca"
      			{
      			#print "system is CAS\n";
      			$xref =~ m/Ca_(.+)/;
      			$id = $1;
      			$newlink = $cas.$id;
      			}
      		case "Ck"
      			{
      			#print "system is KEGG compound\n";
      			$xref =~ m/Ck_(.+)/;
      			$id = $1;
      			$newlink = $keggcomp.$id;	
      			}
      		case "E"
      			{
      			#print "system is EC\n";
      			$xref =~ m/E_(.+)/;
      			$id = $1;
      			$newlink = $ec.$id;	
      			}
      		case "Enzyme"
      			{
      			#print "system is EC\n";
      			$xref =~ m/E_(.+)/;
      			$id = $1;
      			$newlink = $ec.$id;	
      			}
      		else
      			{
      			print "undefined system found: $system\n";
      			}
      		}
      	
      	$line =~ s/<AREA shape="rect" coords="/rect /;
      	$line =~ s/,/ /g;
      	$line =~ s/" href=".+AREA>/ [$newlink|$id]/;

      	print OUTFILE "$line";
      	
      	} #end elsif
      } #end while

print OUTFILE "</imagemap>\n";
close OUTFILE;
close HTML;

print "\n\n\nDone!\n\n";


sub EntrezLookup
{
	my $xref = shift;
	chdir("backpage");
	my $dir = getcwd;
	print "Directory is: $dir\n";
	my $entrez = "null";
	
	my $backpage = $xref.".html";
	
	unless ( open(BACK, $backpage) )
      {
      # Open failed
        print "could not open file $backpage\n";
        exit;
       }
       
    while (my $line = <BACK>)
    	{
    		if ($line =~ m/>\d{1,}<\/a>, Entrez Gene<br><a/)
    		{
    		$line =~ m/>(\d{1,})<\/a>, Entrez Gene<br><a/;
    		$entrez = $1;
    		}
    	}
       return $entrez;
}