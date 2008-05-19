#!/usr/bin/perl

use warnings;
use strict;

use Frontier::Client;
use Data::Dumper;
use PathwayTools;
use PathwayTools::Pathway;
use PathwayTools::WikiPathways;
use File::Temp qw / mktemp /;
use XML::Twig;

# This script downloads pathways from wikipathways,
# checks if they contain labels that look like metabolites,
# and converts them to a metabolite with an associated CAS number.

############
#   main   #
############

# this uses a pre-created tab-delimited text file containing two (or more) columns
# The first column should contain a single CAS number
# The second column should be a metabolite name / symbol.
my $fnMetabolites = "/home/martijn/uni/wrk/metabolomics/maintable.txt";

open INPUT, "$fnMetabolites" or die "Can't find $fnMetabolites, $!";

# create index of metabolites
my %index;

while (my $line = <INPUT>)
{
	# obtain first two columns
	my ($idCas,$name) = split /\t/, $line;
	# mangle the name a bit to make matches less strict.
	my $fuzzy = $name;
	$fuzzy =~ tr/A-Z/a-z/; #lowercase ("D-Glucose" -> "d-glucose")
	$fuzzy =~ s/[^a-z]/ /g; #keep only letters ("d-glucose" -> "d glucose")
	$fuzzy =~ s/\b[a-z]\b//g; #remove words of a single letter ("d glucose" -> " glucose")
	$fuzzy =~ s/\s+/ /g; #join consecutive whitespaces
	$fuzzy =~ s/^ //; # trim start
	$fuzzy =~ s/ $//; # trim end
	if (length $fuzzy > 2)
	{
		$index {$fuzzy} = [$name, $idCas];
	}
}

# Go to wikipathways
# read username, password and url from a config file
my $login = PathwayTools::read_config();

print "Logging in to wikipathways\n";

# log in to wikipathways
my $wikipathways = new PathwayTools::WikiPathways (
	'user' => $login->{user}, 
	'pass' => $login->{pass}, 
	'url' => $login->{server},
	'debug' => 0);

# Process labels on a downloaded pathway
sub process_metabolite_labels($)
{
	my $fnGpml = shift;
	my $count = 0;
	
	my $t= XML::Twig->new();
	$t->set_pretty_print ('indented');
	$t->parsefile($fnGpml);
	
	print "Processing $fnGpml\n";
	foreach my $elt ($t->findnodes('Label'))
    { 
		my $text = $elt->{'att'}->{'TextLabel'};
		my $fuzzy = $text;
		$fuzzy =~ tr/A-Z/a-z/;
		$fuzzy =~ s/[^a-z]/ /g;
		$fuzzy =~ s/\b[a-z]\b//g;
		$fuzzy =~ s/\s+/ /g;
		$fuzzy =~ s/^ //;
		$fuzzy =~ s/ $//;
		if (exists($index{$fuzzy}))
		{
			print "  found $text\n";
			$count++;
			$elt->set_name ("DataNode");
			$elt->first_child("Graphics")->del_att ("FontWeight", "FontSize", "FontStyle");
			
			my $genmapp_xref = $elt->{'att'}->{"Xref"};
			if (defined $genmapp_xref)
			{
				$elt->set_att ("GenMAPP-Xref", $genmapp_xref);
				$elt->del_att ("Xref");
			}
			
			$elt->first_child("Graphics")->set_att ("Color", "0000ff");		
			my $new_elt= XML::Twig::Elt->new( Xref => { Database => "CAS", ID => $index{$fuzzy}[1] } );
			$new_elt->paste_last_child ($elt);
		}
    }	
	
	my $r = $t->root;
	$r->sort_children(
			sub { 	
				my %lookup = 
				(
					"Comment" => 0,
					"Graphics" => 5,
					"DataNode" => 10,
					"Line" => 20,
					"Label" => 30,
					"Shape" => 40,
					"InfoBox" => 50,
					"Legend" => 60,
				);
				
				$lookup{ $_[0]->name() }; 
			},
			type => 'numeric'
		);
	
	my $fnOut = $fnGpml;
	open my $fhOut, "> $fnOut" or die "$!";
	$t->flush($fhOut);
	close $fhOut;
	return $count;
}

# download a pathway, process it and upload it again
sub replace_metabolite_labels($)
{
	# name of the pathway we are going to work with
	my $pathwayName = shift;
	my $SPECIES = "Homo_sapiens";

	my $tempfile = mktemp ("tmpXXXX");
	
	print "Requesting pathway\n";
	# get current version of pathway from wikipathways
	my $revision = $wikipathways->get_pathway_to_file ($SPECIES, $pathwayName, $tempfile);
	
	print $tempfile, "\n";
	my $count = process_metabolite_labels ($tempfile);
	#~ my $pathway = new PathwayTools::Pathway();
	#~ $pathway->from_file ("/home/martijn/Desktop/test.gpml");
	#~ my $revision = 0;

	if ($count > 0)
	{
		print "Sending pathway\n";
		# submit the pathway again
		my $newrevision = $wikipathways->update_pathway_from_file (
			$tempfile, 
			$SPECIES, 
			$pathwayName, 
			$revision, 
			"automated metabolite conversion"
		);
		print "New revision: $newrevision\n";
	}
	else
	{
		print "Nothing changed in this pahtway";
	}
	print "Done.\n";
}

# get a list of pathways to work with
my $pathways = $wikipathways->get_pathway_list();

# process them one by one
for my $pathwayName (grep {/^Homo_sapiens/} @$pathways)
{
	eval {
		
		print "Pathway $pathwayName\n";
		my ($species, $name) = split /:/, $pathwayName;
		replace_metabolite_labels($name);
	}
}



