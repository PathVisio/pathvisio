#!/usr/bin/perl

use warnings;
use strict;

package converter;

sub gmmlOld2New 
{
	my $fnGmml = shift;
	my $dirInput = shift;
	my $dirOutput = shift;

	# load GMMLv1 as DOM
	my $parser = XML::LibXML->new();
	my $tree = $parser->parse_file($dirInput . $fnGmml);
	
	print "-" x 20, "\n";
	# transform
	
	## first rename pathway elements to box elements
	{
		print "Transformation 1: Split GeneProduct to Box and PhysicalEntity\n";
		
		my $doc = $tree->getDocumentElement;
		my @geneProducts = $doc->getElementsByTagName('GeneProduct');
		foreach $b (@geneProducts)
		{
			
			#rename element
			$b->setNodeName ("Box");
			
			my $e = XML::LibXML::Element->new("PhysicalEntity");
			
			for my $attr (qw/Xref Type GeneProduct-Data-Source GeneID Name BackpageHead/)
			{
				$e->setAttribute ($attr, $b->getAttribute($attr));		
				$b->removeAttribute ($attr);
			}
			
			$doc->appendChild ($e);
		}
	}
	print "-" x 20, "\n";
	
	# save GMMLv2
	my $fh;
	my $fnGmmlNew = $dirOutput . $fnGmml;
	print "Writing to $fnGmmlNew\n";
	open $fh, "> $fnGmmlNew" or die;
	print $fh $tree->toString;
	close $fh;
}

sub GmmlNew2Old
{
	my $fnGmml = shift;
	my $dirInput = shift;
	my $dirOutput = shift;
}

1;