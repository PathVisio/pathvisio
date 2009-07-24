#!/usr/bin/perl

use warnings;
use strict;
use PathwayTools::Pathway;

my $fnPathway08 = "../../testData/test2.gpml";
my $fnPathway07 = "../../testData/test.gpml";

for my $file ($fnPathway07, $fnPathway08)
{
	die "File not found $!" unless -e $file;
	
	my $pathway = new PathwayTools::Pathway();
	$pathway->from_file ($file);

	print "File: $file\n";
	print "Namespace: " . $pathway->get_namespace() . "\n";
	$pathway->validate();
	print "Pathway validates.\n";



}
