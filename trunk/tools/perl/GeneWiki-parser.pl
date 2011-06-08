#!/usr/bin/perl

use strict;
use warnings;
use Switch;
use Cwd;
use LWP::UserAgent qw();

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
#### new text file with imagemap and wiki template syntax, with added extension ".txt"

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
my $wikipedia = "http://en.wikipedia.org/wiki/";

#### Find file			
my $html = <*.html>;
my $pathway = (split('\.', $html))[0];

#### Define output file
my $outfile = $html.".txt";

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

$pathway =~ /.*_(WP\d*)$/;
my $WPID = $1;

#variable for width of png image
my $imgWidth = 0;
my $imgHeight = 0;
my %seen = ();
my @linkArray = ();
my $color;

print OUTFILE "<noinclude>\n<!--\nChecklist:\n1. Locate appropriate pathway article and update imagemap default link accordingly. Also consider modifying the \"Description\" at the very bottom of the template to provide a more descriptive pathway name.\n2. Check pathway for \"search for\" links when hovering and attempt to locate an appropriate wikipedia article. Update imagemap link, link color, and highlight references accordingly.\n3. Check pathway for external links in green and attempt to locate appropriate wikipedia content instead. Update imagemap link, link color, and highlight references accordingly.\n4. Delete this checklist from the template :)\n-->\n"; 
print OUTFILE "{{Documentation|Template:Interactive_pathway_maps\/doc}}<\/noinclude>{{{header|\'\'Click on genes, proteins and metabolites below to link to respective articles.\'\' <ref name=\"WikiPathways\">The interactive pathway map can be edited at WikiPathways: {{cite web | url = http:\/\/www.wikipathways.org\/index.php\/Pathway:$WPID | title = $pathway | author =  | date = | work = | publisher = | pages = | accessdate = }}</ref> }}}\n\n<div style=\"overflow:auto\; width:{{{width}}}px\; height:{{{height}}}px\">\n\n"; 

while (my $line = <HTML>)
      {
      if (!($line =~ m/href/))
      	{
      	if ($line =~ m/image/)
      		{
      		$line =~ s/<HTML><BODY><IMG src="image\/.+?\.png/Image:$pathway\.png/;
      		$line =~ s/" usemap="#pathwaymap" width="(\d+)" height="(\d+)"><\/IMG>/|right|alt=/;
		$imgWidth = $1;
		$imgHeight = $2;
		print OUTFILE "{{Preview Crop\n|Image={{Annotated image |float=none|image-width=".$imgWidth."|annot-color=white|imagemap=<imagemap>\n";
    		chomp $line;
     		$line .= $pathway;
     		$line .="\n";
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
      	my $newlink = "";
      	my $id = "";
	$color = 'red';
      	
      	switch($system)
      		{
      		case "L"	
      			{ 
      			#print "system is Entrez\n";
      			$xref =~ m/L_(.+)/;
      			$id = $1;
         		$newlink = $genewiki.$id;
			$newlink = FetchArticleURL($newlink);
			if ($newlink =~ m/Special:Search/){
				$color = 'red';
				$newlink =~ s/#Interactive_pathway_map go to/ search for/;
			} else {
				$newlink =~ m/wiki\/(.+)#Interactive/;
				$id = $1;
      				$color = 'blue';
				}
      			}
      		case "EnHs"
      			{
      			#print "system is Ensembl\n";
      			$xref =~ m/EnHs_(.+)/;
      			$id = $1;
      			my $entrez = BackpageLookup($xref, 'Entrez Gene'); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $ensembl.$id." Go to Ensembl";
      				$color = 'green';
      				}
      			else
      				{
      				$id = $entrez;
 	        		$newlink = $genewiki.$id;
				$newlink = FetchArticleURL($newlink);
				if ($newlink =~ m/Special:Search/){
					$color = 'red';
					$newlink =~ s/#Interactive_pathway_map go to/ search for/;
				} else {
					$newlink =~ m/wiki\/(.+)#Interactive/;
					$id = $1;
       					$color = 'blue';
					}
      				}
      			}
      		case "S"
      			{
      			#print "system is Uniprot\n";
      			$xref =~ m/S_(.+)/;
      			$id = $1;
      			my $entrez = BackpageLookup($xref, 'Entrez Gene'); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $uniprot.$id." Go to UniProt";
      				$color = 'green';
      				}
      			else
      				{
      				$id = $entrez;
 	        		$newlink = $genewiki.$id;
				$newlink = FetchArticleURL($newlink);
				if ($newlink =~ m/Special:Search/){
					$color = 'red';
					$newlink =~ s/#Interactive_pathway_map go to/ search for/;
				} else {
      					$newlink =~ m/wiki\/(.+)#Interactive/;
					$id = $1;
 					$color = 'blue';
					}
      				}
      			}
      		case "U"
      			{
      			#print "system is Unigene\n";
      			$xref =~ m/U_(.+)/;
      			$id = $1;
      			my $entrez = BackpageLookup($xref, 'Entrez Gene'); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $unigene.$id." Go to UniGene";
      				$color = 'green';
      				}
      			else
      				{
      				$id = $entrez;
 	        		$newlink = $genewiki.$id;
				$newlink = FetchArticleURL($newlink);
				if ($newlink =~ m/Special:Search/){
					$color = 'red';
					$newlink =~ s/#Interactive_pathway_map go to/ search for/;
				} else {
      					$newlink =~ m/wiki\/(.+)#Interactive/;
					$id = $1;
 					$color = 'blue';
					}
      				}
      			}
      		case "H"
      			{
      			#print "system is HUGO\n";
      			$xref =~ m/H_(.+)/;
      			$id = $1;
      			my $entrez = BackpageLookup($xref, 'Entrez Gene'); #Use full Xref since that's the name of the backpage file
      			if ($entrez eq "null")
      				{
      				$newlink = $hugo.$id." Go to HGNC";
				$color = 'green';
      				}
      			else
      				{
      				$id = $entrez;
 	        		$newlink = $genewiki.$id;
				$newlink = FetchArticleURL($newlink);
				if ($newlink =~ m/Special:Search/){
					$color = 'red';
					$newlink =~ s/#Interactive_pathway_map go to/ search for/;
				} else {
					$newlink =~ m/wiki\/(.+)#Interactive/;
					$id = $1;
       					$color = 'blue';
					}
      				}
      			}
      		case "Ch"
      			{
      			#print "system is HMDB\n";
      			$xref =~ /^Ch_(HMDB\d+)$/;
			$id = $1;
			my $wp = BackpageLookup($xref, 'Wikipedia');
			if ($wp eq "null"){
				$newlink = $hmdb.$id." Go to HMDB";
				$color = 'green';
				}
			else {
				$id = $wp;
				$newlink = $wikipedia.$wp." Go to article";
				$color = 'blue';
				}
			}
      		case "Ca"
      			{
      			#print "system is CAS\n";
      			$xref =~ m/Ca_(.+)/;
      			$id = $1;
			my $wp = BackpageLookup($xref, 'Wikipedia');
			if ($wp eq "null"){
				$newlink = $cas.$id." Go to CAS";
				$color = 'green';
				}
			else {
				$id = $wp;
				$newlink = $wikipedia.$wp." Go to article";
				$color = 'blue';
				}
			}
      		case "Ck"
      			{
      			#print "system is KEGG compound\n";
      			$xref =~ m/Ck_(.+)/;
      			$id = $1;
			my $wp = BackpageLookup($xref, 'Wikipedia');
			if ($wp eq "null"){
				$newlink = $keggcomp.$id." Go to KEGG";
				$color = 'green';
				}
			else {
				$id = $wp;
				$newlink = $wikipedia.$wp." Go to article";
				$color = 'blue';
				}
   			}
      		case "E"
      			{
      			#print "system is EC\n";
      			$xref =~ m/E_(.+)/;
      			$id = $1;
      			$newlink = $ec.$id." Go to BRENDA";	
			$color = 'green';
      			}
      		case "enzyme"
      			{
      			#print "system is EC\n";
      			$xref =~ m/enzyme_(.+)/;
      			$id = $1;
      			$newlink = $ec.$id." Go to BRENDA";	
			$color = 'green';
      			}
      		else
      			{
      			print "undefined system found: $system\n";
      			}
      		}

	#remove #interactive_pathway_map anchor from links
	$newlink =~ s/#Interactive_pathway_map//;
	
	#make unique references to copies
	if ($seen{$id}++){
		my $alt = $seen{$id} - 1;
		$id = $id."__alt".$alt;
	}
      	$line =~ m/coords="(\d+\.\d+),(\d+\.\d+),(\d+\.\d+),(\d+\.\d+)/;
	my @links = ($1, $2, $3, $4, $color, $id);
	push @linkArray, [@links];
      	$line =~ s/<AREA shape="rect" coords="/rect /;
      	$line =~ s/,/ /g;
   	$line =~ s/" href=".+AREA>/ [$newlink]/;

      	print OUTFILE "$line";
      	
      	} #end elsif
      } #end while

print OUTFILE "default [[#Interactive_pathway_map|Go to pathway article]]
desc none
</imagemap>\n|annotations=\n";    

#color link lines
for my $i ( 0 .. $#linkArray ) {
	my $left = $linkArray[$i][0];
        my $right = $imgWidth - $left + 5;
	my $top = $linkArray[$i][3] -15;# -$i*1;
	my $width = $linkArray[$i][2] - $linkArray[$i][0];
	my $color = $linkArray[$i][4];
	print OUTFILE "{{Annotation|0|0|[[<div style=\"display:block; width:".$width."px; height:0px; overflow:hidden; position:relative; left:".$left."px; top:".$top."px; background:transparent; border-top:3px ".$color." solid\"></div>]]}}\n";       
}

#switch to highlight per gene
print OUTFILE "{{#switch:{{{highlight}}}\n";
for my $i ( 0 .. $#linkArray ) {
	my $left = $linkArray[$i][0];
	my $width = $linkArray[$i][2] - $linkArray[$i][0] - 7;
	my $height =  $linkArray[$i][3] - $linkArray[$i][1] - 8;
	my $top = $linkArray[$i][1] - 12;
	my $color = 'black';
	my $key = $linkArray[$i][5];
	print OUTFILE "|".$key."=\n";
	print OUTFILE "{{Annotation|0|0|[[<div style=\"display:block; width:".$width."px; height:".$height."px;
overflow:hidden; position:relative; left:".$left."px; top:".$top."px;
background:transparent; border:4px ".$color." solid\"></div>]]}}\n";
}
print OUTFILE "}}\n";

print OUTFILE "|caption=\n}}\n|bsize=\n";

#crop variables
my $cwidth = 375;
my $cheight = 350;
my $otop = 25;
my $oleft = 10;

print OUTFILE "|cWidth={{#switch:{{{highlight}}}\n";
for my $i ( 0 .. $#linkArray ) {
	my $key = $linkArray[$i][5];
	print OUTFILE "|".$key."=".$cwidth."\n";
}
print OUTFILE "|#default=$imgWidth\n}}\n";

print OUTFILE "|cHeight={{#switch:{{{highlight}}}\n";
for my $i ( 0 .. $#linkArray ) {
	my $key = $linkArray[$i][5];
	print OUTFILE "|".$key."=".$cheight."\n";
}
print OUTFILE "|#default=$imgHeight\n}}\n";

print OUTFILE "|oTop={{#switch:{{{highlight}}}\n";
for my $i ( 0 .. $#linkArray ) {
	my $key = $linkArray[$i][5];
	my $min = $otop;
	my $max = $imgHeight - $cheight + $min;
	my $offset = $linkArray[$i][3] + $min - ($cheight / 2) - (($linkArray[$i][3] - $linkArray[$i][1]) / 2);
	if ($offset > $max){
		$offset = $max;
	}
	if ($offset < $min ){
		$offset = $min;
	}
	print OUTFILE "|".$key."=$offset\n";
}
print OUTFILE "|#default=".$otop."\n}}\n";

print OUTFILE "|oLeft={{#switch:{{{highlight}}}\n";
for my $i ( 0 .. $#linkArray ) {
	my $key = $linkArray[$i][5];
	my $min = $oleft;
	my $max = $imgWidth - $cwidth + $min; 
	my $offset = $linkArray[$i][0] + $min - ($cwidth / 2) + (($linkArray[$i][2] - $linkArray[$i][0]) / 2);
	if ($offset > $max){
		$offset = $max;
	}
	if ($offset < $min ){
		$offset = $min;
	}
	print OUTFILE "|".$key."=$offset\n";
}
print OUTFILE "|#default=".$oleft."\n}}\n";

print OUTFILE "|Location=left\n|Description=".$pathway."  [http:\/\/www.wikipathways.org\/index.php\/Pathway:$WPID edit]\n}}\n</div><noinclude>{{reflist}}[[Category:WikiPathways]]</noinclude>";
close OUTFILE;
close HTML;

print "\n\n\nDone!\n\n";


sub BackpageLookup
{
	my ($xref, $system) = @_;
	chdir("backpage");
	my $dir = getcwd;
	print "Directory is: $dir\n";
	my $hit = "null";
	
	my $backpage = $xref.".html";
	
	unless ( open(BACK, $backpage) )
      {
      # Open failed
        print "could not open file $backpage\n";
        exit;
       }
       
    while (my $line = <BACK>)
    	{
    		if ($line =~ m/<\/a>, $system<br></)
    		{
    		$line =~ m/.+>(.+?)<\/a>, $system<br></;
    		$hit = $1;
		$hit =~ s/ /_/g;
    		}
    	}
       return $hit;
}

sub FetchArticleURL
{
	my $newlink = shift;
	my $ua = LWP::UserAgent->new;
	my $response = $ua->get($newlink);
	$newlink = $response->request->url."#Interactive_pathway_map go to article";
	return $newlink;
}
