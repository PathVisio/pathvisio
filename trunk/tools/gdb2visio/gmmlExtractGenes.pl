#!/usr/bin/perl
#
#  

use strict;
use warnings;

use Data::Dumper;
use File::Basename;

######################
#  config
######################

my $fnGmml = "d:/prg/gmml-visio/trunk/gmml_mapp_examples/Hs_Cell_cycle_KEGG.xml";

######################
#  main
######################

open GMML, "$fnGmml" or die;
my @genes;

my %systemCodes = (
	"SGD"          => "D",
	"FlyBase"      => "F",
	"GenBank"      => "G",
	"InterPro"     => "I",
	"LocusLink"    => "L",
	"MGI"          => "M",
	"RefSeq"       => "Q",
	"RGD"          => "R",
	"SwissProt"    => "S",
	"GeneOntology" => "T",
	"UniGene"      => "U",
	"WormBase"     => "W",
	"ZFIN"         => "Z",
	"Affy"         => "X",
	"Other"        => "O",
);

#extract genes from gmml mapps, the easy way, without bothering 
#about xml and all that
for (<GMML>)
{
	if (/<GeneProduct\s.*?\sGeneProduct-Data-Source="(.*?)".*?\sName="(.*?)".*?>/s)
	{
		my ($id, $code) = ($2, $1);
		if (exists ($systemCodes{$code}))
		{
			$code = $systemCodes{$code};
		}
		else { die "Unrecognized: $code"; }
		push @genes, [$code, $id];
	}
}

close GMML;

#print "Info:  Read ", scalar @genes, " gene id's from ", basename($fnGmml), "\n";

#print in tab-del format:
#~ for (@genes) { print $$_[0], "\t", $$_[1], "\n"; }

#print in perl format:
#~ print Dumper (\@genes);

#print in java format:
for (@genes) { print "\t{\"", $$_[0], "\", \"", $$_[1], "\"},\n"; }
