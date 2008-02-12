#!/usr/bin/perl

use strict;
use warnings;

use PathwayTools::Pathway;
use PathwayTools::PathwayElement;

my %output;

my $labelText;
my $geneText;

my $MARGIN_X = 450;
my $MARGIN_Y = 600;
my $DEFAULT_WIDTH = 1200;
my $DEFAULT_HEIGHT = 300;
my $COLUMN_SPACING = 300;
my $BOARD_HEIGHT = 12000;
my $ORGANISM = "Mus Musculus";

my $xco = $MARGIN_X;
my $yco = $MARGIN_Y;


my $mappname;

my @elements;

while (<>)
{
	chomp;
	
	# skip header row(s)	
	if (/Gene\s*ID\s+System\s*Code\s+Label/i) { next; }
	
	my @fields = split /\t/;

	if ($fields[0] eq "LABEL")
	{
		#start a new column
		$yco = $MARGIN_Y;
		$xco += $DEFAULT_WIDTH + $COLUMN_SPACING;
		
		my $label = new PathwayTools::PathwayElement (
				element => "Label",
				centerx => $xco + $DEFAULT_WIDTH / 2,
				centery => $yco + $DEFAULT_HEIGHT / 2,
				textlabel => $fields[2],
				fontweight => "Bold",
			);

		push @elements, $label;
	}
	else
	{
		my %mapping =
		(
			'X' => "Affy",
			'S' => "SwissProt",
			'O' => "Other",
			'L' => "Entrez Gene",
			'M' => "MGI",
			'' => "",
		) ;

		my $syscode = $mapping{$fields[1]};
		if (!defined $syscode) { die "Unknown systemcode '" . $fields[1] . "'"; }

		$mappname = $fields[5];
		
		my $datanode = new PathwayTools::PathwayElement (
			element => "DataNode",
			id => $fields[0],
			database => $syscode,
			textlabel => $fields[2],
			backpagehead => $fields[3],
			comment => $fields[4],
			centerx => $xco + $DEFAULT_WIDTH / 2,
			centery => $yco + $DEFAULT_HEIGHT / 2, 
		);
		
		push @elements, $datanode;
	}
	
	$yco += $DEFAULT_HEIGHT;
	if ($yco + $DEFAULT_HEIGHT > $BOARD_HEIGHT)
	{
		$yco = $MARGIN_Y;
		$xco += $DEFAULT_WIDTH + $COLUMN_SPACING;		
	}
}

my $pathway = new PathwayTools::Pathway (
		boardwidth => $xco + $DEFAULT_WIDTH + $MARGIN_X,
		boardheight => $BOARD_HEIGHT,
		organism => $ORGANISM,
		name => $mappname,
	);

for my $elt (@elements)
{
	$pathway->add_element ($elt);
}

# sanity check
$pathway->validate();

print $pathway->to_string();