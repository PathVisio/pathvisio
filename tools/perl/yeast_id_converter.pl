#!/usr/bin/perl

use warnings;
use strict;

use PathwayTools::Pathway;
use PathwayTools;
use PathwayTools::WikiPathways;

#
# Script to adjust yeast database references
# as discussed: http://groups.google.com/group/wikipathways-dev/browse_thread/thread/74a50e6dc1269aef/00fd677a1551646b
#

my $fileset1 = "/home/martijn/PathVisio-Data/pathways/Sc_*/*.gpml";
my $fileset2 = "/home/martijn/PathVisio-Data/pathways/Sc_*/**/*.gpml";
my $PATHWAY = "Heme Biosynthesis";
my $ORGANISM = "Saccharomyces cerevisiae";

# read username, password and url from a config file
my $login = PathwayTools::read_config();

print "Logging in to wikipathways\n";

# log in to wikipathways
my $wikipathways = new PathwayTools::WikiPathways (
	'user' => $login->{user}, 
	'pass' => $login->{pass}, 
	'url' => $login->{server},
	'debug' => 0);

#~ print join ("\t", qw/FileName TextLabel Xref Database/);
# for all yeast pathways
#~ for my $fnGpml (glob ($fileset1), glob ($fileset2))
{
	my $modified = 0;
	#~ $fnGpml =~ /pathways(.*)/;
	#~ print $1, "\n";
	
	#~ #read file
	
	#~ my $pathway = new PathwayTools::Pathway();
	#~ $pathway->from_file ($fnGpml);

	print "Requesting pathway\n";
	# get current version of pathway from wikipathways
	my $result = $wikipathways->get_pathway_with_revision($ORGANISM, $PATHWAY);

	my $pathway = $result->[0];
	my $revision = $result->[1];

	my $root = $pathway->{document}->getDocumentElement();
	
	#for each datanode
	for my $datanode ($root->getChildrenByTagName("DataNode"))
	{
		my $textlabel = $datanode->getAttribute ("TextLabel");
		my $xref = ($datanode->getChildrenByTagName("Xref"))[0];
		my $database = $xref->getAttribute ("Database");
		my $id = $xref->getAttribute ("ID");
		
		#~ print join ("\t", ($textlabel, $database, $id)), "\n";
		
		if ($database eq "SGD" &&
			$id =~ /Y[A-Z0-9]+/)
		{
			$xref->setAttribute ("Database", "Ensembl");
			$modified = 1;
		}
	}
	#save to new location
	
	print "Validating pathway\n";
	$pathway->validate();

	print "Sending pathway\n";
	# submit the pathway again
	$wikipathways->update_pathway (
		$pathway, 
		$ORGANISM, 
		$PATHWAY, 
		$revision, 
		"yeast id converter"
	);

	
	print "Done.\n";	
}
	