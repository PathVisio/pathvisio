#!/usr/bin/perl

#use warnings;
#use strict;
use HashSpeciesList;
use PathwayTools::Pathway;
use PathwayTools::PathwayElement;
use Cwd;

#
# This script was designed to make modifictions to converted KEGG pathways. 
# It has two main functions and works on a local set of gpml files:
# 1. Rename local gpml files: The new name is collected from the gpml and the three-letter species code is added as a prefix.
# 2. Add the species assignment: The latin species name is added as an Organism attribute
# Output: updated gpml files in the same folder as original gpmls.

#Define an array for checking input species codes

my %speciesTable = getSpeciesTable();
my @codeArray = ();
for my $key (sort keys %speciesTable){
	unless ($speciesTable{$key}[2] =~ /^\s*$/){
		push(@codeArray, $speciesTable{$key}[2]);
	}
}

#Get path to input files
my $dir = getcwd;

print "Enter the path to the folder containing INPUT files (gpml).
Example: C:\/GenMAPP 2 Data\/MAPPs\/\n
Or Press Enter for current Directory ($dir)\n";

print "\nDirectory Path: ";

my $path = <STDIN>;
if ($path eq "\n") {
     $path = $dir;
     }
     else {
     chomp ($path);
     }
     
#Get input files
chdir "$path" or die "Cannot Find Directory Path!\n";
my @gpmlfiles = <*.gpml>;
my $gpmlcount = scalar(@gpmlfiles);

#Get species code from the KEGG file name
my $refcode = "";
my $REFORGANISM = 0;
my $REFTAXID = 0;

if ($gpmlcount > 0)
	{
	my $f = $gpmlfiles[0];
	$refcode = (split(/\d/,$f))[0];
	$refcode  = lc $refcode;
	}

for my $key (sort keys %speciesTable){
	if ($speciesTable{$key}[2] =~ $refcode){
		$REFORGANISM = $speciesTable{$key}[0];
	}
}
print "Three-letter species code is $refcode, species is $REFORGANISM\n";
	
#Loop through gpml files

my $NS = "http://genmapp.org/GPML/2007";
if ($gpmlcount > 0)
{
foreach my $f (@gpmlfiles)
		{
#Read from pathway file
		my $pathway = new PathwayTools::Pathway();
		$pathway->from_file($f);
		my $root = $pathway->{document}->getDocumentElement();

#Get name of pathway to use as file name
		my $name = $root->getAttribute("Name");
		$name =~ s|/ ||;
		my $newfilename = "$refcode"."_"."$name.gpml";
		
#Create organism attribute and fill in species name
		my $name = "Organism";
		if ($REFORGANISM ne "NULL")
		{
		$root->setAttribute($name, $REFORGANISM);
		}
	
#print pathway to file
		my $result = $pathway->to_file($newfilename);
		}
}
print "\n\nDone.\n";	