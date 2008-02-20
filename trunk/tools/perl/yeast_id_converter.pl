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

open INFILE, "pathway_content_flatfile.txt" or die $!;

<INFILE>; # skip header line

while (my $line = <INFILE>)
{
	my $PATHWAY = (split (/\t/, $line))[0];
	
	my $modified = 0;

	print "Requesting pathway $PATHWAY\n";

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
		
		if ($database eq "SGD" &&
			$id =~ /Y[A-Z0-9]+/)
		{
			$xref->setAttribute ("Database", "Ensembl");
			$modified = 1;
		}
	}
	#save to new location

	if ($modified)
	{
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
	}
	else
	{
		print "No modifications. skipping\n";
	}
	
	print "Done.\n";	
}
	
close INFILE;