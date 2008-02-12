#!/usr/bin/perl

use warnings;
use strict;

use Frontier::Client;
use Data::Dumper;
use PathwayTools;
use PathwayTools::Pathway;
use PathwayTools::WikiPathways;

# TODO: check that password authentication actually checks password???

############
#   main   #
############

# read username, password and url from a config file
my $login = PathwayTools::read_config();

# log in to wikipathways
#~ my $wikipathways = new PathwayTools::WikiPathways (
	#~ $login->{user}, 
	#~ $login->{pass}, 
	#~ $login->{server});

sub test_adjust_pathway()
{
	# name of the pathway we are going to work with
	my $PATHWAY = "RPCTEST2";
	
	# get current version of pathway from wikipathways
	#~ my $result = $wikipathways->get_pathway_with_revision("Homo sapiens", $PATHWAY);

	#~ my $pathway = $result->[0];
	#~ my $revision = $result->[1];
	
	my $pathway = new PathwayTools::Pathway();
	$pathway->from_file ("/home/martijn/Desktop/test.gpml");
	my $revision = 0;
	
	# add a datanode to the pathway
	$pathway->add_data_node (
		centerx => rand (10000), 
		centery => rand (10000), 
		database => "Ensemlb", 
		id => "1234");
	
	print "I'm going to send:\n", "-" x 40, "\n";
	
	print $pathway->to_string();
	print "\n", "-" x 40, "\n";
	
	
	$pathway->validate();

	# submit the pathway again
	#~ $wikipathways->update_pathway (
		#~ $pathway, 
		#~ "Homo sapiens", 
		#~ $PATHWAY, 
		#~ $revision, 
		#~ "webservice test"
	#~ );

}


for (1..1)
{
	test_adjust_pathway();
}


