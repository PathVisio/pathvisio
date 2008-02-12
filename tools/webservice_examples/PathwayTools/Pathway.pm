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


my $fnGPML = "/home/martijn/prg/pathvisio-trunk/GPML.xsd";

=item new PathwayTools::Pathway()

create a new Pathway object.

=cut

sub new
{
	my $class = shift;
	my $self = {};
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
	
	return $self->{document}->toString(2);
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
	
	$self->{document}->toFile ($fnGpml, 2);
	$self->{filename} = $fnGpml;
}

=item $pathway->validate()

validate that the internal representation
of this pathway conforms to the XML Schema for GPML.

This function will 'die' and print an error message
if the XML Schema fails to validate.

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


=item $pathway->add_data_node (width => ..., height => ..., ...)

add a datanode to the pathway.
You may specify the following parameters:

centerx, centery, width, height, database, id, type,
backpage, textlabel, color

See the GPML specification for more details.

=cut
	
sub add_data_node
{
	my $self = shift;
	my %specs = @_;
	
	my $data = new PathwayTools::PathwayElement ("PathwayElement", %specs);
	
	my $root = $self->{document}->documentElement();

	my $datanode = $data->get_xml_node();
	
	$root->appendChild ($datanode);

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
	@nodes = sort { $order{$a->nodeName} <=> $order {$b->nodeName} } @nodes;
	
	# and put them back again
	for (@nodes)
	{
		$e->insertAfter ($_, $e->lastChild());
	}	
}

=back

=cut

1;