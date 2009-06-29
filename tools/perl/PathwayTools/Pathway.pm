use strict;
use warnings;

=head1 NAME

PathwayTools::Pathway - a GPML Pathway

=head1 SYNOPSIS

 use PathwayTools::Pathway;
 
 my $pathway = new PathwayTools::Pathway();
 $pathway->from_file ("Hs_Apoptosis.gpml");
 
 # add a datanode to the pathway
 $pathway->add_data_node (
 	centerx => 7000, 
 	centery => 8000, 
 	database => "Entrez gene", 
 	id => "1234");
 
 # check for schema validity
 $pathway->validate();

=head1 DESCRIPTION

The following functions are provided by this module

=over 3

=cut

package PathwayTools::Pathway;

use PathwayTools::PathwayElement;
use XML::LibXML;
use Data::Dumper;

#TODO: redundant with PathwayElement::$NS
my $NS = "http://genmapp.org/GPML/2008a";
my $fnGPML = "../../GPML.xsd";

=item new PathwayTools::Pathway()

create a new Pathway object.

=cut

sub new
{
	my $class = shift;
	my %specs = @_;
	
	my %defaults = 
	(
		'name' => "",
		'datasource' => "PathwayTools.pm",
		'version' => "20080212",
		'author' => "",
		'maintainer' => "",
		'email' => "",
		'organism' => "Homo sapiens",
		'boardwidth' => 10000,
		'boardheight' => 10000,
	);
	
	my $doc = new XML::LibXML::Document("1.0", "ISO-8859-1");
	my $self = { 'document' => $doc };
	
	for my $key (keys %defaults)
	{
		$self->{$key} = (exists $specs{$key} ? $specs{$key} : $defaults{$key});
	}
	
	my $pwy = new XML::LibXML::Element("Pathway");
	$pwy->setNamespace ($NS);
	$doc->setDocumentElement ($pwy);
		
	$pwy->setAttribute ("Name", $self->{name});
	$pwy->setAttribute ("Data-Source", $self->{datasource});
	$pwy->setAttribute ("Version", $self->{version});
	$pwy->setAttribute ("Author", $self->{author});
	$pwy->setAttribute ("Maintainer", $self->{maintainer});
	$pwy->setAttribute ("Email", $self->{email});
	$pwy->setAttribute ("Organism", $self->{organism});
	
	my $infobox = $pwy->addNewChild($NS, "InfoBox");
	$infobox->setAttribute ("CenterX", 0);
	$infobox->setAttribute ("CenterY", 0);
	
	my $graphics = $pwy->addNewChild($NS, "Graphics");
	$graphics->setAttribute ("BoardWidth", $self->{boardwidth});
	$graphics->setAttribute ("BoardHeight", $self->{boardheight});
	$graphics->setAttribute ("WindowWidth", 10000);
	$graphics->setAttribute ("WindowHeight", 10000);
	
	my $legend = $pwy->addNewChild($NS, "Legend");
	$legend->setAttribute ("CenterX", 0);
	$legend->setAttribute ("CenterY", 0);
	
			
	bless $self, $class;
}

=item $pathway->from_string ($string)

parse the pathway from a string

=cut

sub from_string($)
{
	my $self = shift;
	my $gpml = shift;

	my $parser = XML::LibXML->new();
	$self->{document} = $parser->parse_string($gpml);	
}

=item $pathway->to_string()

return a string containing the pathway in gpml format.

=cut

sub to_string()
{
	my $self = shift;

	# remove whitespace and sort
	my $e = $self->{document}->getDocumentElement();
	rem_whitespace ($e);
	sort_element ($e);
	
	return $self->{document}->toString(1);
}

=item $pathway->from_file ($filename)

read a pathway from a file

=cut

sub from_file($)
{
	my $self = shift;
	my $fnGpml = shift;
	my $parser = XML::LibXML->new();
	$self->{document} = $parser->parse_file($fnGpml);
	$self->{filename} = $fnGpml;
	
}

=item $pathway->to_file ($filename)

save this pathway to a file

=cut

sub to_file($)
{
	my $self = shift;
	my $fnGpml = shift;
	
	# remove whitespace and sort
	my $e = $self->{document}->getDocumentElement();
	rem_whitespace ($e);
	sort_element ($e);
	
	$self->{document}->toFile ($fnGpml, 1);
	$self->{filename} = $fnGpml;
	
}

=item $pathway->validate()

validate that the internal representation
of this pathway conforms to the XML Schema for GPML.

This function will 'die' and print an error message
if the XML Schema fails to validate.

NOTE, 5/6/09: This method needs to be debugged.

=cut

sub validate()
{
	my $self = shift;
	
	# remove whitespace and sort
	my $e = $self->{document}->getDocumentElement();
	rem_whitespace ($e);
	sort_element ($e);

	my $schema = new XML::LibXML::Schema ( location => $fnGPML );
	
	$schema->validate ($self->{document})
}


=item $pathway->create_element (element => ..., width => ..., height => ..., ...)

create a new element with the specified attributes and add it to 
the pathway.

Some of the parameters you can specify:

centerx, centery, width, height, database, id, type,
backpage, textlabel, color

The only parameter that is compulsory is element.

See the GPML specification for more details.

=cut
	
sub create_element
{
	my $self = shift;
	my %specs = @_;
	
	my $data = new PathwayTools::PathwayElement (%specs);
	
	my $root = $self->{document}->documentElement();
	my $elt = $data->get_xml_node();
	$root->appendChild ($elt);
	
	return $elt;
}


=item add_element ($pathway_element)

add an element previously created with new PathwayElement() to this pathway

=cut

sub add_element
{
	my $self = shift;
	my $data = shift;
	
	my $root = $self->{document}->documentElement();
	my $elt = $data->get_xml_node();
	$root->appendChild ($elt);
	
	return $elt;		
}

=item rem_whitespace ($node)

This is a helper function
it trims whitespace recursively from any XML::LibXML::Node

=cut

#
# usually, after sorting, whitespace is totally mixed up.
# better to remove it altogether
# 
sub rem_whitespace
{
	my $e = shift;
	my $p = $e->firstChild();
	
	while (defined $p)
	{
		my $n = $p->nextSibling();
		
		if (ref $p eq "XML::LibXML::Element") 
		{
			#recursive...
			rem_whitespace ($p);
		}
		elsif (ref $p eq "XML::LibXML::Text")
		{
			#only remove elements that contain nothing but whitespace
			if ($p->data =~ /^\s*$/)
			{
				$e->removeChild ($p);
			}
		}
		$p = $n;
	}
}

=item sort_element ($root)

This is a helper function

$root is the rootelement of the XML::LibXML representation of gpml.
this function will sort the elements below the Pathway element
so that it is valid according to the GPML definition.

=cut 

sub sort_element
{
	my $e = shift;	
	
	if (!$e->hasChildNodes()) { return; }
	
	my @nodes;
	
	my $p = $e->firstChild();
	while (defined $p)
	{
		my $n = $p->nextSibling();
		if (ref $p eq "XML::LibXML::Element") 
		{
			#~ #recursive...
			#~ sort_element ($p);
			
			#temporarily remove node and place it in an array
			push @nodes, ($e->removeChild ($p));
		}
		$p = $n;
	} 
	
	# the proper order for valid gpml
	my %order =
	(
		Comment => 0,
		Graphics => 1,
		DataNode => 2,
		Line => 3,
		Label => 4,
		Link => 5,
		Shape => 6,
		Group => 7,
		InfoBox => 8,
		Legend => 9,
		Biopax => 10,
	);
	
	#sort array of nodes on their nodeName.
	@nodes = sort { $order{$a->nodeName} <=> $order{$b->nodeName} } @nodes;
	
	# and put them back again
	for (@nodes)
	{
		$e->insertAfter ($_, $e->lastChild());
	}	
}

=back

=cut

1;