#!/usr/bin/perl
#
# PathVisio,
# a tool for data visualization and analysis using Biological Pathways
# Copyright 2006-2007 BiGCaT Bioinformatics
#
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
#  
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.
#
#
#  

use strict;
use warnings;

use Data::Dumper;
use File::Basename;

######################
#  config
######################

my $fnGpml = "d:/prg/gmml-visio/trunk/gmml_mapp_examples/Hs_Cell_cycle_KEGG.xml";

######################
#  main
######################

open GPML, "$fnGpml" or die;
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

#extract genes from gpml files, the easy way, without bothering 
#about xml and all that
for (<GPML>)
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

close GPML;

#print "Info:  Read ", scalar @genes, " gene id's from ", basename($fnGpml), "\n";

#print in tab-del format:
#~ for (@genes) { print $$_[0], "\t", $$_[1], "\n"; }

#print in perl format:
#~ print Dumper (\@genes);

#print in java format:
for (@genes) { print "\t{\"", $$_[0], "\", \"", $$_[1], "\"},\n"; }
