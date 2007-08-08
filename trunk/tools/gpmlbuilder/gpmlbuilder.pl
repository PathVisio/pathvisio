#!/usr/bin/perl

use strict;
use warnings;

use XML::LibXML;

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
my $boardWidth;

while (<>)
{
	# skip header row(s)
	
	if (/Gene\s*ID\s+System\s*Code\s+Label/i) { next; }
	my $elt;
	
	chomp;
	my @fields = split /\t/;

	if ($fields[0] eq "LABEL")
	{
		#start a new column
		$yco = $MARGIN_Y;
		$xco += $DEFAULT_WIDTH + $COLUMN_SPACING;
		
		my $centerx = $xco + $DEFAULT_WIDTH / 2;
		my $centery = $yco + $DEFAULT_HEIGHT / 2;
		my $textLabel = $fields[2];
		$labelText .= qq~	<Label TextLabel="$textLabel">
		<Graphics FontWeight="Bold" FontSize="150" Color="000000" CenterX="$centerx" CenterY="$centery" Width="$DEFAULT_WIDTH" Height="$DEFAULT_HEIGHT"/>
	</Label>
~;
	}
	else
	{
		my %mapping =
		(
			'X' => "Affy",
			'S' => "SwissProt",
			'O' => "Other",
			'' => "",
		) ;
		my $id = $fields[0];
		my $syscode = $mapping{$fields[1]};
		if (!defined $syscode) { die "Unknown systemcode '" . $fields[1] . "'"; }
		my $label = $fields[2];
		my $head = $fields[3];
		my $comment = $fields[4];		
		$mappname = $fields[5];
		my $centerx = $xco + $DEFAULT_WIDTH / 2;
		my $centery = $yco + $DEFAULT_HEIGHT / 2;
		$geneText .= qq~	<DataNode TextLabel="$label" Type="GeneProduct" BackpageHead="$head">
		<Graphics Color="Transparent" CenterX="$centerx" CenterY="$centery" Width="$DEFAULT_WIDTH" Height="$DEFAULT_HEIGHT"/>
		<Xref Database="$syscode" ID="$id" />
	</DataNode>
~;
	}
	
	$yco += $DEFAULT_HEIGHT;
	if ($yco + $DEFAULT_HEIGHT > $BOARD_HEIGHT)
	{
		$yco = $MARGIN_Y;
		$xco += $DEFAULT_WIDTH + $COLUMN_SPACING;		
	}
}

$boardWidth = $xco + $DEFAULT_WIDTH + $MARGIN_X;

print qq~<?xml version="1.0" encoding="ISO-8859-1"?>
<Pathway xmlns="http://genmapp.org/GPML/2007" Name="$mappname" Data-Source="" Version="20041216" Author="" Maintainer="" Email="" Organism="$ORGANISM">
	<Graphics BoardWidth="$boardWidth" BoardHeight="$BOARD_HEIGHT" WindowWidth="19304.0" WindowHeight="13230.0" />
~;

print $geneText;
print $labelText;
print qq~	<InfoBox CenterX="0.0" CenterY="0.0" />
	<Legend CenterX="17433.0" CenterY="132.7069" />
~;
print "</Pathway>\n";