#!/usr/bin/perl

# This script looks at the xsd
# finds out which attributes are optional, and what the default values are
# and generates java code that can be copied to data/GmmlData.java
#
# (maybe this proves that we should have used jaxb after all :))

use strict;
use warnings;
use XML::Twig;

my @list;

my $twig = XML::Twig->new( twig_handlers => 
	{ 
		"xsd:attribute" => \&attrib,
	}
);

$twig->parsefile ("../../GPML.xsd");
$twig->purge;


sub attrib 
{
	my $t = shift;
	my $s = shift;
	
	my $name = $s->att("name"); 
	my $type = $s->att("type");
	if (!defined $type)
	{
		$type = $s->first_child("xsd:simpleType")->first_child("xsd:restriction")->att("base");
	}
	my $use = $s->att("use"); 
	if (!defined $use)
	{
		$use="optional";
	}
	my @containers;
	my $default = $s->att("default");
	
	my $pp = $s;
	do 
	{
		$pp = $pp->parent ("xsd:element");
		if (defined $pp)
		{
			push @containers, $pp->att("name");
		}
	}
	while (defined $pp);
	
	my $containingtype = join ".", reverse @containers;
	
	#~ if ($containingtype eq "Graphics")
	#~ {
		#~ $containingtype = $s->parent->parent->parent->parent->parent->att("name") . ".Graphics";
	#~ }
	
	print "result.put(\"$containingtype\@$name\", new AttributeInfo (\"$type\", ", 
		(defined($default) ? "\"$default\"" : "null"), 
		", \"$use\"));\n";
	
	#~ print "$name\t$type\t$use\t$default\t$containingtype\n";
}
