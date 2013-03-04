#!/usr/bin/perl

# This script looks at the xsd
# finds out which attributes are optional, and what the default values are
# and generates java code that can be copied to data/GmmlData.java
#
# (maybe this proves that we should have used jaxb after all :))

use strict;
use warnings;
use XML::Twig;

# %group is used to store attribGroup information
my %group;

#define a set of handlers
my $twig = XML::Twig->new( twig_handlers => 
	{ 
		# handle all attribute tags below an element tag
		"xsd:complexType/xsd:attribute" => \&attrib,

		# handle attribute tags in an element with simple content type
		"xsd:simpleContent/xsd:extension/xsd:attribute" => \&attrib,

		# handle all attribute tags below an attributeGroup tag
		"xsd:attributeGroup/xsd:attribute" => \&groupAttrib,
		
		# handle all attributeGroups below complexType
		"xsd:complexType/xsd:attributeGroup" => \&groupRef,
	}
);

# run all the handlers on the XSD
$twig->parsefile ("../../GPML2013a.xsd");
$twig->purge;

# handler for references to attribute Groups
sub groupRef
{
	my $t = shift;
	my $s = shift;
	my $ref = $s->att("ref"); 

	my $containingtype = get_containing_type($s);
	
	# look up the stuff previously stored in %group.
	# for each attribute stored, print a java statement
	for my $attr (@{$group{$ref}})
	{
		printStatement($containingtype, 
			$$attr{name}, $$attr{type}, 
			$$attr{default}, $$attr{use});
	}
}

# handler for declaration of attribGroups
# this will store info on an attribGroup in a %group
sub groupAttrib
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
	
	my $default = $s->att("default");
	
	my $pp = $s->parent("xsd:attributeGroup");
	my $groupName = "gpml:" . $pp->{'att'}->{'name'}; 
	
	push @{$group{$groupName}}, 
		{
			name => $name,
			type => $type,
			use => $use,
			default => $default
		};	
}

# print a java statement for a particular attribute
sub printStatement
{
	my $containingtype = shift;
	my $name = shift;
	my $type = shift;
	my $default = shift;
	my $use = shift;

	print "result.put(\"$containingtype\@$name\", new AttributeInfo (\"$type\", ", 
		(defined($default) ? "\"$default\"" : "null"), 
		", \"$use\"));\n";
	
}

# find the containing elements for a certain attribute,
# e.g. it will try to find "Label.Graphics" for the attribute "FontWeight"
sub get_containing_type
{
	my $pp = shift;
	my @containers;
	do 
	{
		# look for any ancestor element of the type "xsd:element"
		$pp = $pp->parent ("xsd:element");
		if (defined $pp)
		{
			push @containers, $pp->att("name");
		}
	}
	while (defined $pp);	
	my $containingtype = join ".", reverse @containers;
}

# handle an ordinary attribute definition
sub attrib 
{
	my $t = shift;
	my $s = shift;
	
	# if a parent element is xsd:group, then we're in an element
	# group definition such as BiopaxGroup. Skip.
	
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
	my $default = $s->att("default");
	
	my $containingtype;
	if (defined $s->parent("xsd:group"))
	{
		$containingtype = get_containing_type($s);
	}
	else
	{
		$containingtype = get_containing_type($s);
	}
	
	printStatement($containingtype, $name, $type, $default, $use);
}
