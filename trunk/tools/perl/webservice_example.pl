#!/usr/bin/perl

use warnings;
use strict;

use Frontier::Client;
use Data::Dumper;
use PathwayTools;
use PathwayTools::Pathway;
use PathwayTools::WikiPathways;

############
#   main   #
############

# read username, password and url from a config file
my $login = PathwayTools::read_config();

print "Logging in to wikipathways\n";

# log in to wikipathways
my $wikipathways = new PathwayTools::WikiPathways (
	'user' => $login->{user}, 
	'pass' => $login->{pass}, 
	'url' => $login->{server},
	'debug' => 0);

sub test_adjust_pathway()
{
	# name of the pathway we are going to work with
	my $PATHWAY = "RPCTEST";
	
	print "Requesting pathway\n";
	# get current version of pathway from wikipathways
	my $result = $wikipathways->get_pathway_with_revision("Homo sapiens", $PATHWAY);

	my $pathway = $result->[0];
	my $revision = $result->[1];
	
	#~ my $pathway = new PathwayTools::Pathway();
	#~ $pathway->from_file ("/home/martijn/Desktop/test.gpml");
	#~ my $revision = 0;

	print "Adding datanode to pathway\n";

	# add a datanode to the pathway
	$pathway->create_element (
		element => "DataNode",
		textlabel => "HelloWorld",
		centerx => rand (10000), 
		centery => rand (10000), 
		database => "Ensemlb", 
		id => "1234");
	
	#~ print "I'm going to send:\n", "-" x 40, "\n";	
	#~ print $pathway->to_string();
	#~ print "\n", "-" x 40, "\n";
	
	
	print "Validating pathway\n";
	$pathway->validate();

	print "Sending pathway\n";
	# submit the pathway again
	$wikipathways->update_pathway (
		$pathway, 
		"Homo sapiens", 
		$PATHWAY, 
		$revision, 
		"webservice test"
	);

	print "Done.\n";
}


for (1..1)
{
	test_adjust_pathway();
}


